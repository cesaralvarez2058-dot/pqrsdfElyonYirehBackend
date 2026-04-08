package com.elyonyireh.pqrsdfelyonyirehbackend.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.ConsultaPublicaRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.ConsultaPublicaResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.CalificarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.mappers.PqrsdfMapper;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Evidencia;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Solicitante;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Area;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Sede;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.EvidenciaRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.PqrsdfRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.SolicitanteRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.AreaRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.SedeRepository;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.PqrsdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.ByteArrayOutputStream;

import com.elyonyireh.pqrsdfelyonyirehbackend.services.CaptchaService;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.EmailService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PqrsdfServiceImpl implements PqrsdfService {

    private final SolicitanteRepository solicitanteRepository;
    private final PqrsdfRepository pqrsdfRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final AreaRepository areaRepository;
    private final SedeRepository sedeRepository;
    private final PqrsdfMapper pqrsdfMapper;
    private final EmailService emailService;
    private final CaptchaService captchaService;

    @Autowired
    public PqrsdfServiceImpl(SolicitanteRepository solicitanteRepository,
                             PqrsdfRepository pqrsdfRepository,
                             EvidenciaRepository evidenciaRepository,
                             AreaRepository areaRepository,
                             SedeRepository sedeRepository,
                             PqrsdfMapper pqrsdfMapper,
                             EmailService emailService,
                             CaptchaService captchaService) {
        this.solicitanteRepository = solicitanteRepository;
        this.pqrsdfRepository = pqrsdfRepository;
        this.evidenciaRepository = evidenciaRepository;
        this.areaRepository = areaRepository;
        this.sedeRepository = sedeRepository;
        this.pqrsdfMapper = pqrsdfMapper;
        this.emailService = emailService;
        this.captchaService = captchaService;
    }

    @Override
    @Transactional
    public RadicarPqrsdfResponseDTO radicarPqrsdf(RadicarPqrsdfRequestDTO requestDTO, List<MultipartFile> archivos) {
        log.info("=== INICIO DE TRANSACCIÓN: RADICAR PQRSDF ===");
        log.info("-> Identificación del Solicitante: {}", requestDTO.getNumeroIdentificacion());
        log.info("-> Tipo de PQRSD solicitada: {}", requestDTO.getTipoPqrsd());
        
        // --- 0. Validación de CAPTCHA ---
        if (!captchaService.validarCaptcha(requestDTO.getCaptchaId(), requestDTO.getCaptchaValue())) {
            log.warn("=== CAPTCHA RECHAZADO ===");
            return new RadicarPqrsdfResponseDTO(false, "El Captcha es inválido o ha expirado.", null);
        }
        
        // --- 0.1 Validación Especial Tipo Usuario ---
        if (!"Estudiante".equalsIgnoreCase(requestDTO.getTipoUsuario())) {
            requestDTO.setSemestre(null);
            requestDTO.setProgramaFormacion(null);
        } else {
            if (requestDTO.getSemestre() == null || requestDTO.getSemestre().trim().isEmpty() ||
                requestDTO.getProgramaFormacion() == null || requestDTO.getProgramaFormacion().trim().isEmpty()) {
                log.warn("=== DATOS ESTUDIANTE INCOMPLETOS ===");
                return new RadicarPqrsdfResponseDTO(false, "Semestre y Programa de Formación son obligatorios para estudiantes.", null);
            }
        }

        // --- 0.2 Validación Límite de Descripción ---
        if (requestDTO.getDescripcion() != null && requestDTO.getDescripcion().length() > 1334) {
            log.warn("=== DESCRIPCIÓN EXCEDE LÍMITE ===");
            return new RadicarPqrsdfResponseDTO(false, "La descripción excede el límite máximo de 1334 caracteres.", null);
        }
        
        try {
            // 1. Buscar o Crear Solicitante
            Optional<Solicitante> solicitanteOpt = solicitanteRepository.findByNumeroIdentificacion(requestDTO.getNumeroIdentificacion());
            Solicitante solicitante;

            if (solicitanteOpt.isPresent()) {
                solicitante = solicitanteOpt.get();
                log.info("-> Actualizando datos de Solicitante existente.");
                solicitante.setNombres(requestDTO.getNombres());
                solicitante.setApellidos(requestDTO.getApellidos());
                solicitante.setCorreo(requestDTO.getCorreo());
                solicitante.setCelular(requestDTO.getCelular());
            } else {
                log.info("-> Creando nuevo Solicitante en el sistema.");
                solicitante = pqrsdfMapper.toSolicitante(requestDTO);
            }
            
            solicitanteRepository.save(solicitante);

            // 2. Crear PQRSD y Relacionar Sede
            Pqrsdf pqrsdf = pqrsdfMapper.toPqrsdf(requestDTO);
            pqrsdf.setNumeroRadicado(generarNumeroRadicado());
            pqrsdf.setEstado("SIN ASIGNAR");
            pqrsdf.setSolicitante(solicitante);

            if (requestDTO.getSedeId() == null) {
                throw new RuntimeException("Debe seleccionar una Sede válida para enviar la PQRSD.");
            }
            Sede sedeSeleccionada = sedeRepository.findById(requestDTO.getSedeId())
                    .orElseThrow(() -> new RuntimeException("La Sede seleccionada no existe en el sistema."));
            pqrsdf.setSede(sedeSeleccionada);

            pqrsdfRepository.save(pqrsdf);
            log.info("-> PQRSD persistida correctamente. Número de Radicado: {}", pqrsdf.getNumeroRadicado());

            // 3. Procesar y guardar evidencias si existen
            if (archivos != null && !archivos.isEmpty()) {
                log.info("-> Procesando {} archivo(s) adjunto(s) a la solicitud.", archivos.size());
                List<Evidencia> listaEvidencias = new ArrayList<>();
                for (MultipartFile archivo : archivos) {
                    if (archivo.isEmpty()) continue;
                    try {
                        String nombreOriginal = archivo.getOriginalFilename();
                        String extension = nombreOriginal != null && nombreOriginal.contains(".") ? 
                                nombreOriginal.substring(nombreOriginal.lastIndexOf(".")) : "";
                        String nombreGuardado = UUID.randomUUID().toString() + extension;
                        
                        // Subida Local
                        String uploadsDir = "uploads/evidencias/";
                        Path uploadPath = Paths.get(uploadsDir);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        Path filePath = uploadPath.resolve(nombreGuardado);
                        Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        Evidencia evidencia = new Evidencia();
                        evidencia.setNombreArchivoOriginal(nombreOriginal != null ? nombreOriginal : "archivo_sin_nombre");
                        evidencia.setRutaAlmacenamiento(filePath.toString()); // Guardamos ruta local
                        evidencia.setTipoMime(archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream");
                        evidencia.setTamano(archivo.getSize());
                        evidencia.setPqrsdf(pqrsdf);
                        
                        listaEvidencias.add(evidencia);
                        log.debug("-> Archivo guardado remotamente en Storage Supabase: {}", nombreOriginal);

                    } catch (IOException e) {
                        log.error("-> FALLO al procesar la evidencia: {} - Error: {}", archivo.getOriginalFilename(), e.getMessage());
                        throw new RuntimeException("Error al guardar archivo remotamente: " + e.getMessage());
                    }
                }
                if (!listaEvidencias.isEmpty()) {
                    evidenciaRepository.saveAll(listaEvidencias);
                    log.info("-> Evidencias vinculadas al radicado exitosamente.");
                }
            }

            log.info("=== TRANSACCIÓN RADICAR PQRSDF COMPLETADA CON ÉXITO ===");

            // === ENVÍO DE CORREOS (asíncronos, no bloquean la respuesta) ===
            try {
                // 1. Correo al usuario: solo el número de radicado
                emailService.enviarConfirmacionUsuario(
                    solicitante.getCorreo(),
                    solicitante.getNombres() + " " + (solicitante.getApellidos() != null ? solicitante.getApellidos() : ""),
                    pqrsdf.getNumeroRadicado()
                );

                // 2. Correo al admin: resumen + Word adjunto con todos los datos
                byte[] docWord = generarDocumentoWord(pqrsdf.getId());
                emailService.enviarNotificacionAdministrador(
                    solicitante.getNombres() + " " + (solicitante.getApellidos() != null ? solicitante.getApellidos() : ""),
                    pqrsdf.getNumeroRadicado(),
                    pqrsdf.getTipoPqrsd().name(),
                    docWord
                );
            } catch (Exception mailEx) {
                log.warn("⚠️ PQRSDF guardada correctamente pero hubo un error al enviar correos: {}", mailEx.getMessage());
            }

            return new RadicarPqrsdfResponseDTO(true, "PQRSD radicada exitosamente", pqrsdf.getNumeroRadicado());

        } catch (Exception e) {
            log.error("=== ERROR CRÍTCIO EN TRANSACCIÓN RADICAR PQRSDF ===");
            log.error("-> Causa del error: {}", e.getMessage(), e);
            throw e; // Lanzamos de nuevo para que @Transactional aplique rollback automático en la Base de Datos
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultaPublicaResponseDTO consultarPublico(ConsultaPublicaRequestDTO requestDTO) {
        if (!captchaService.validarCaptcha(requestDTO.getCaptchaId(), requestDTO.getCaptchaValue())) {
            return new ConsultaPublicaResponseDTO(false, "El Captcha es inválido o expiró. Inténtalo de nuevo.", null, null, null, null, null, null, null, null, null, null, null);
        }

        Optional<Pqrsdf> pqrsdfOpt = pqrsdfRepository.findByNumeroRadicado(requestDTO.getNumeroRadicado());
        if (pqrsdfOpt.isEmpty()) {
            return new ConsultaPublicaResponseDTO(false, "No se encontró ningún radicado con el número especificado.", null, null, null, null, null, null, null, null, null, null, null);
        }

        Pqrsdf pqrsdf = pqrsdfOpt.get();
        Solicitante solicitante = pqrsdf.getSolicitante();

        if (!solicitante.getNumeroIdentificacion().equals(requestDTO.getNumeroIdentificacion())) {
            return new ConsultaPublicaResponseDTO(false, "El número de documento de identidad no corresponde al solicitante original de este radicado.", null, null, null, null, null, null, null, null, null, null, null);
        }
        
        return new ConsultaPublicaResponseDTO(
            true, 
            "Consulta exitosa.", 
            pqrsdf.getNumeroRadicado(),
            pqrsdf.getEstado(), 
            pqrsdf.getFechaRadicacion(), 
            pqrsdf.getTipoPqrsd(),
            solicitante.getNombres(),
            solicitante.getApellidos(),
            pqrsdf.getDescripcion(),
            pqrsdf.getRespuestaAdmin(),
            pqrsdf.getFechaRespuesta(),
            pqrsdf.getCalificacionUsuario(),
            pqrsdf.getObservacionUsuario()
        );
    }

    @Override
    @Transactional
    public RadicarPqrsdfResponseDTO calificarPqrsdfPublico(CalificarPqrsdfRequestDTO requestDTO) {
        Optional<Pqrsdf> pqrsdfOpt = pqrsdfRepository.findByNumeroRadicado(requestDTO.getNumeroRadicado());
        if (pqrsdfOpt.isEmpty()) {
            return new RadicarPqrsdfResponseDTO(false, "No se encontró ningún radicado con el número especificado.", null);
        }

        Pqrsdf pqrsdf = pqrsdfOpt.get();
        Solicitante solicitante = pqrsdf.getSolicitante();

        if (!solicitante.getNumeroIdentificacion().equals(requestDTO.getNumeroIdentificacion())) {
            return new RadicarPqrsdfResponseDTO(false, "El documento de identidad no corresponde al solicitante.", null);
        }

        if (pqrsdf.getCalificacionUsuario() != null) {
            return new RadicarPqrsdfResponseDTO(false, "Esta solicitud ya fue calificada previamente.", null);
        }

        pqrsdf.setCalificacionUsuario(requestDTO.getCalificacionUsuario());
        pqrsdf.setObservacionUsuario(requestDTO.getObservacionUsuario());
        pqrsdf.setEstado("CERRADO");

        pqrsdfRepository.save(pqrsdf);
        return new RadicarPqrsdfResponseDTO(true, "Calificación guardada exitosamente y solicitud cerrada.", pqrsdf.getNumeroRadicado());
    }

    @Override
    public Page<Pqrsdf> listarPqrsdfs(Pageable pageable) {
        return pqrsdfRepository.findAll(pageable);
    }

    @Override
    public Optional<Pqrsdf> obtenerPqrsdfPorId(Long id) {
        return pqrsdfRepository.findById(id);
    }

    @Override
    public Optional<Evidencia> obtenerEvidenciaPorId(Long id) {
        return evidenciaRepository.findById(id);
    }

    @Override
    public byte[] generarDocumentoWord(Long id) {
        Optional<Pqrsdf> pqrsdfOpt = pqrsdfRepository.findById(id);
        if (pqrsdfOpt.isEmpty()) {
            throw new RuntimeException("PQRSD no encontrada");
        }

        Pqrsdf pqrsdf = pqrsdfOpt.get();
        Solicitante s = pqrsdf.getSolicitante();

        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // === TÍTULO ===
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("DETALLE DE RADICADO: " + pqrsdf.getNumeroRadicado());
            titleRun.setBold(true);
            titleRun.setFontSize(18);
            titleRun.setColor("1F3864");

            crearLinea(document);

            // === DATOS DEL REMITENTE ===
            crearEncabezadoSeccion(document, "1. Información del Remitente");
            crearParrafo(document, "Nombre completo: " + s.getNombres() + " " + (s.getApellidos() != null ? s.getApellidos() : ""), false);
            crearParrafo(document, "Documento de Identificación (" + s.getTipoIdentificacion().toUpperCase() + "): " + s.getNumeroIdentificacion(), false);
            crearParrafo(document, "Correo electrónico: " + s.getCorreo(), false);
            crearParrafo(document, "Celular: " + s.getCelular(), false);
            crearParrafo(document, "Tipo de Usuario: " + s.getTipoUsuario(), false);
            if (s.getProgramaFormacion() != null && !s.getProgramaFormacion().isEmpty()) {
                crearParrafo(document, "Programa de Formación: " + s.getProgramaFormacion(), false);
                crearParrafo(document, "Semestre: " + s.getSemestre(), false);
            }

            crearLinea(document);

            // === DETALLES DEL RADICADO ===
            crearEncabezadoSeccion(document, "2. Detalles de la Solicitud");
            crearParrafo(document, "Tipo de PQRSD: " + pqrsdf.getTipoPqrsd().getLabel(), false);
            crearParrafo(document, "Fecha de Radicación: " + pqrsdf.getFechaRadicacion().toString(), false);
            crearParrafo(document, "Estado Actual: " + pqrsdf.getEstado(), false);
            crearParrafo(document, "Dependencia Destino: " + pqrsdf.getDependenciaDestino(), false);
            crearParrafo(document, "Funcionario Asignado: " + pqrsdf.getFuncionarioDestino(), false);

            crearLinea(document);

            // === DESCRIPCIÓN ===
            crearEncabezadoSeccion(document, "3. Contenido / Descripción de la Solicitud");
            crearParrafo(document, pqrsdf.getDescripcion(), false);

            // === EVIDENCIAS / IMÁGENES ===
            List<Evidencia> evidencias = evidenciaRepository.findByPqrsdfId(id);
            if (evidencias != null && !evidencias.isEmpty()) {
                crearLinea(document);
                crearEncabezadoSeccion(document, "4. Evidencias Anexadas");

                for (Evidencia ev : evidencias) {
                    crearParrafo(document, "Archivo: " + ev.getNombreArchivoOriginal() + " (" + ev.getTipoMime() + ")", false);

                    // Solo insertar como imagen si es tipo imagen
                    if (ev.getTipoMime() != null && ev.getTipoMime().startsWith("image/")) {
                        try {
                            // Cargar la imagen local a memoria
                            byte[] imgBytes = Files.readAllBytes(Paths.get(ev.getRutaAlmacenamiento()));

                            int pictureType;
                            String mime = ev.getTipoMime().toLowerCase();
                            if (mime.contains("png")) {
                                pictureType = XWPFDocument.PICTURE_TYPE_PNG;
                            } else if (mime.contains("gif")) {
                                pictureType = XWPFDocument.PICTURE_TYPE_GIF;
                            } else {
                                pictureType = XWPFDocument.PICTURE_TYPE_JPEG; // jpg y cualquier otro
                            }

                            XWPFParagraph imgParagraph = document.createParagraph();
                            imgParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
                            XWPFRun imgRun = imgParagraph.createRun();

                            // Insertar imagen: 12cm x auto (proporción fija)
                            imgRun.addPicture(
                                new java.io.ByteArrayInputStream(imgBytes),
                                pictureType,
                                ev.getNombreArchivoOriginal(),
                                org.apache.poi.util.Units.toEMU(340),  // ancho ~12cm
                                org.apache.poi.util.Units.toEMU(240)   // alto ~8.5cm
                            );
                            crearLinea(document);
                        } catch (Exception imgEx) {
                            log.warn("No se pudo insertar imagen en Word: {}", ev.getNombreArchivoOriginal());
                            crearParrafo(document, "[No se pudo incrustar la imagen: " + ev.getNombreArchivoOriginal() + "]", false);
                        }
                    }
                }
            }

            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el documento Word", e);
        }
    }

    private void crearEncabezadoSeccion(XWPFDocument doc, String texto) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setBold(true);
        r.setFontSize(13);
        r.setColor("2B579A"); // Azul Word corporativo
    }

    private void crearLinea(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(" ");
    }

    private void crearParrafo(XWPFDocument doc, String texto, boolean esNegrita) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(texto);
        if(esNegrita) {
            r.setBold(true);
            r.setFontSize(12);
        }
    }

    @Override
    @Transactional
    public RadicarPqrsdfResponseDTO actualizarEstadoPqrsdf(Long id, String nuevoEstado) {
        Optional<Pqrsdf> pqrsdfOpt = pqrsdfRepository.findById(id);
        if (pqrsdfOpt.isPresent()) {
            Pqrsdf pqrsdf = pqrsdfOpt.get();
            pqrsdf.setEstado(nuevoEstado);
            pqrsdfRepository.save(pqrsdf);
            return new RadicarPqrsdfResponseDTO(true, "Estado de PQRSD actualizado a: " + nuevoEstado, pqrsdf.getNumeroRadicado());
        }
        return new RadicarPqrsdfResponseDTO(false, "PQRSD no encontrada", null);
    }

    @Override
    @Transactional
    public RadicarPqrsdfResponseDTO asignarArea(Long pqrsdfId, Long areaId) {
        Optional<Pqrsdf> pqrsdfOpt = pqrsdfRepository.findById(pqrsdfId);
        if (pqrsdfOpt.isEmpty()) {
            return new RadicarPqrsdfResponseDTO(false, "PQRSD no encontrada.", null);
        }
        Optional<Area> areaOpt = areaRepository.findById(areaId);
        if (areaOpt.isEmpty()) {
            return new RadicarPqrsdfResponseDTO(false, "Área no encontrada.", null);
        }

        Pqrsdf pqrsdf = pqrsdfOpt.get();
        Area area = areaOpt.get();
        pqrsdf.setArea(area);
        pqrsdf.setEstado("EN REPARTO");
        pqrsdf.setDependenciaDestino(area.getNombre());
        pqrsdf.setFuncionarioDestino(area.getResponsableNombre());
        pqrsdf.setFechaAsignacion(java.time.LocalDateTime.now());
        pqrsdf.setRecordatorioEnviado(false);
        
        pqrsdfRepository.save(pqrsdf);

        try {
            // Generar documento Word
            byte[] docWord = generarDocumentoWord(pqrsdf.getId());

            // Enviar notificación al responsable del Área en lugar de solo al Administrador
            emailService.enviarNotificacionAreaAsignada(
                area.getResponsableEmail(),
                area.getResponsableNombre(),
                pqrsdf.getSolicitante().getNombres(),
                pqrsdf.getNumeroRadicado(),
                pqrsdf.getTipoPqrsd().getLabel(),
                docWord
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar correo al asignar área o generar el Word: {}", e.getMessage(), e);
        }

        return new RadicarPqrsdfResponseDTO(true, "PQRSD asignada exitosamente y documento enviado al área " + area.getNombre(), pqrsdf.getNumeroRadicado());
    }

    @Override
    @Transactional
    public boolean eliminarPqrsdf(Long id) {
        if (pqrsdfRepository.existsById(id)) {
            // Eliminar archivos físicos aquí sería ideal si existen. Por ahora eliminamos la entidad
            pqrsdfRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String generarNumeroRadicado() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "PQR-" + fecha + "-" + random;
    }
}
