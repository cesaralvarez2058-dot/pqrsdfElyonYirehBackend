package com.elyonyireh.pqrsdfelyonyirehbackend.services;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.Pqrsdf;
import com.elyonyireh.pqrsdfelyonyirehbackend.repositories.PqrsdfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordatorioSchedulerService {

    private final PqrsdfRepository pqrsdfRepository;
    private final EmailService emailService;
    private final PqrsdfService pqrsdfService;

    // Ejecutar todos los días a las 8:00 AM
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void verificarYEnviarRecordatorios() {
        log.info("Iniciando tarea programada: Verificación de PQRSDF sin respuesta luego de 5 días.");

        LocalDateTime limite = LocalDateTime.now().minusDays(5);
        List<Pqrsdf> atrasadas = pqrsdfRepository.findByEstadoAndFechaAsignacionBeforeAndRecordatorioEnviadoFalse("EN REPARTO", limite);

        if (atrasadas.isEmpty()) {
            log.info("No hay PQRSDFs retrasadas en espera de recordatorio el día de hoy.");
            return;
        }

        log.info("Se encontraron {} PQRSDFs sin respuesta. Enviando correos...", atrasadas.size());

        for (Pqrsdf pqrsdf : atrasadas) {
            try {
                // Generamos documento Word nuevamente como adjunto de contingencia
                byte[] docWord = pqrsdfService.generarDocumentoWord(pqrsdf.getId());

                emailService.enviarRecordatorioArea(
                    pqrsdf.getArea().getResponsableEmail(),
                    pqrsdf.getArea().getResponsableNombre(),
                    pqrsdf.getSolicitante().getNombres(),
                    pqrsdf.getNumeroRadicado(),
                    pqrsdf.getTipoPqrsd().getLabel(),
                    docWord
                );

                // Marcar para no volver a enviar el recordatorio mañana
                pqrsdf.setRecordatorioEnviado(true);
                pqrsdfRepository.save(pqrsdf);

            } catch (Exception e) {
                log.error("Error enviando recordatorio para PQRSDF {}: {}", pqrsdf.getNumeroRadicado(), e.getMessage());
            }
        }
        log.info("Finalizó tarea de recordatorios.");
    }
}
