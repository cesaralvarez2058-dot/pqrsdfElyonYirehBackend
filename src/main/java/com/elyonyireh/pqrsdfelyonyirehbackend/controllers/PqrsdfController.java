package com.elyonyireh.pqrsdfelyonyirehbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.enums.TipoPqrsd;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.CalificarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.ConsultaPublicaRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.ConsultaPublicaResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfRequestDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Evidencia;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.PqrsdfService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/pqrsdf")
@Slf4j
public class PqrsdfController {

    private final PqrsdfService pqrsdfService;

    @Autowired
    public PqrsdfController(PqrsdfService pqrsdfService) {
        this.pqrsdfService = pqrsdfService;
    }

    @PostMapping(value = "/radicar", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<RadicarPqrsdfResponseDTO> radicar(
            @RequestPart("datos") RadicarPqrsdfRequestDTO requestDTO,
            @RequestPart(value = "evidencias", required = false) List<MultipartFile> evidencias) {
        
        log.info("Recibiendo petición POST /radicar del frontend. Cédula solicitante: {}", requestDTO.getNumeroIdentificacion());
        if (evidencias != null) {
            log.info("Cantidad de evidencias recibidas: {}", evidencias.size());
        }

        try {
            // Note: For now, evidence is received and passed to the Service to be saved locally.
            RadicarPqrsdfResponseDTO response = pqrsdfService.radicarPqrsdf(requestDTO, evidencias);
            log.info("Petición exitosa. Radicado generado: {}", response.getNumeroRadicado());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error interno al radicar PQRSDF. Datos: {} - Excepción: {}", requestDTO, e.getMessage(), e);
            return new ResponseEntity<>(new RadicarPqrsdfResponseDTO(false, "Error al radicar: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<Map<String, String>>> obtenerTiposPqrsd() {
        List<Map<String, String>> tipos = Arrays.stream(TipoPqrsd.values())
            .map(tipo -> {
                Map<String, String> map = new HashMap<>();
                map.put("value", tipo.name());
                map.put("label", tipo.getLabel());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }
    @GetMapping
    public ResponseEntity<Page<Pqrsdf>> listarPqrsdfs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRadicacion").descending());
        return ResponseEntity.ok(pqrsdfService.listarPqrsdfs(pageable));
    }

    @GetMapping("/evidencia/{id}")
    public ResponseEntity<Resource> descargarEvidencia(@PathVariable Long id) {
        Optional<Evidencia> evidenciaOpt = pqrsdfService.obtenerEvidenciaPorId(id);
        if (evidenciaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Evidencia evidencia = evidenciaOpt.get();
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(evidencia.getRutaAlmacenamiento());
            Resource resource = new org.springframework.core.io.UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, evidencia.getTipoMime())
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + evidencia.getNombreArchivoOriginal() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error cargando recurso: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pqrsdf> obtenerPqrsdfPorId(@PathVariable Long id) {
        log.info("Recibiendo petición GET /api/pqrsdf/{}", id);
        Optional<Pqrsdf> pqrsdf = pqrsdfService.obtenerPqrsdfPorId(id);
        if (pqrsdf.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pqrsdf.get());
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<byte[]> descargarDocumentoWord(@PathVariable Long id) {
        try {
            Optional<Pqrsdf> pqrsdfOpt = pqrsdfService.obtenerPqrsdfPorId(id);
            if (pqrsdfOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Pqrsdf pqrsdf = pqrsdfOpt.get();
            // Formatear el nombre usando Regex para cambiar espacios a guiones bajos
            String nombreLimpio = pqrsdf.getSolicitante().getNombres().replaceAll("\\s+", "_");
            String nombreArchivo = nombreLimpio + "_PQRSDF.docx";
            
            byte[] documento = pqrsdfService.generarDocumentoWord(id);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .body(documento);
        } catch (Exception e) {
            log.error("Error construyendo Word: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<RadicarPqrsdfResponseDTO> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam String nuevoEstado) {
        RadicarPqrsdfResponseDTO response = pqrsdfService.actualizarEstadoPqrsdf(id, nuevoEstado);
        if (response.isExito()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PutMapping("/{id}/asignar")
    public ResponseEntity<RadicarPqrsdfResponseDTO> asignarArea(
            @PathVariable Long id,
            @RequestParam Long areaId) {
        RadicarPqrsdfResponseDTO response = pqrsdfService.asignarArea(id, areaId);
        if (response.isExito()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPqrsdf(@PathVariable Long id) {
        boolean eliminado = pqrsdfService.eliminarPqrsdf(id);
        if (eliminado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/consulta-publica")
    public ResponseEntity<ConsultaPublicaResponseDTO> consultarEstado(@RequestBody ConsultaPublicaRequestDTO requestDTO) {
        log.info("REST: Consulta pública solicitada para radicado: {}", requestDTO.getNumeroRadicado());
        ConsultaPublicaResponseDTO response = pqrsdfService.consultarPublico(requestDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/publico/calificar")
    public ResponseEntity<RadicarPqrsdfResponseDTO> calificarPqrsdfPublico(@RequestBody CalificarPqrsdfRequestDTO requestDTO) {
        log.info("REST: Calificación pública solicitada para radicado: {}", requestDTO.getNumeroRadicado());
        RadicarPqrsdfResponseDTO response = pqrsdfService.calificarPqrsdfPublico(requestDTO);
        if (response.isExito()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
