package com.elyonyireh.pqrsdfelyonyirehbackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Usamos el correo del admin (remitente verificado) en lugar del login de Brevo
    @Value("${pqrsdf.admin-email}")
    private String from;

    @Value("${pqrsdf.admin-email}")
    private String adminEmail;

    /**
     * Envía correo al USUARIO confirmando su número de radicado.
     */
    @Async
    public void enviarConfirmacionUsuario(String correoUsuario, String nombreUsuario, String numeroRadicado) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(correoUsuario);
            helper.setSubject("✅ Confirmación de Radicado PQRSDF - " + numeroRadicado);
            helper.setText(buildHtmlUsuario(nombreUsuario, numeroRadicado), true);
            mailSender.send(message);
            log.info("📧 Correo de confirmación enviado al usuario: {}", correoUsuario);
        } catch (MessagingException e) {
            log.error("❌ Error enviando correo al usuario {}: {}", correoUsuario, e.getMessage());
        }
    }

    /**
     * Envía correo al ADMINISTRADOR con el documento Word adjunto.
     */
    @Async
    public void enviarNotificacionAdministrador(String nombreUsuario, String numeroRadicado,
                                                 String tipoPqrsd, byte[] documentoWord) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(adminEmail);
            helper.setSubject("🔔 Nueva PQRSDF Recibida - " + numeroRadicado);
            helper.setText(buildHtmlAdmin(nombreUsuario, numeroRadicado, tipoPqrsd), true);

            if (documentoWord != null) {
                // Adjuntar el documento Word con el nombre del usuario
                String nombreArchivo = nombreUsuario.replaceAll("\\s+", "_") + "_PQRSDF.docx";
                helper.addAttachment(nombreArchivo, new ByteArrayResource(documentoWord),
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }

            mailSender.send(message);
            log.info("📧 Correo enviado al administrador: {}", adminEmail);
        } catch (MessagingException e) {
            log.error("❌ Error enviando correo al administrador: {}", e.getMessage());
        }
    }

    /**
     * Envía correo al RESPONSABLE DE ÁREA con el documento Word adjunto al ser asignado un caso.
     */
    @Async
    public void enviarNotificacionAreaAsignada(String responsableEmail, String responsableNombre, String nombreUsuario, String numeroRadicado, String tipoPqrsd, byte[] documentoWord) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(responsableEmail);
            helper.setSubject("📌 Nueva Asignación de Caso PQRSDF - " + numeroRadicado);
            helper.setText(buildHtmlAreaAsignada(responsableNombre, nombreUsuario, numeroRadicado, tipoPqrsd), true);

            if (documentoWord != null) {
                String nombreArchivo = nombreUsuario.replaceAll("\\s+", "_") + "_Detalle_PQRSDF.docx";
                helper.addAttachment(nombreArchivo, new ByteArrayResource(documentoWord),
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }

            mailSender.send(message);
            log.info("📧 Correo de asignación enviado al responsable del área: {}", responsableEmail);
        } catch (MessagingException e) {
            log.error("❌ Error enviando correo al área responsable {}: {}", responsableEmail, e.getMessage());
        }
    }

    /**
     * Envía un recordatorio al RESPONSABLE DE ÁREA si el caso no ha sido respondido en N días.
     */
    @Async
    public void enviarRecordatorioArea(String responsableEmail, String responsableNombre, String nombreUsuario, String numeroRadicado, String tipoPqrsd, byte[] documentoWord) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(responsableEmail);
            helper.setSubject("⏰ URGENTE: Recordatorio PQRSDF sin respuesta - " + numeroRadicado);
            helper.setText(buildHtmlRecordatorioArea(responsableNombre, nombreUsuario, numeroRadicado, tipoPqrsd), true);

            if (documentoWord != null) {
                String nombreArchivo = nombreUsuario.replaceAll("\\s+", "_") + "_Detalle_PQRSDF_Recordatorio.docx";
                helper.addAttachment(nombreArchivo, new ByteArrayResource(documentoWord),
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            }

            mailSender.send(message);
            log.info("📧 Correo de RECORDATORIO enviado al responsable del área: {}", responsableEmail);
        } catch (MessagingException e) {
            log.error("❌ Error enviando recordatorio al área {}: {}", responsableEmail, e.getMessage());
        }
    }

    // =================== TEMPLATES HTML ===================

    private String buildHtmlUsuario(String nombre, String radicado) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;">
              <div style="background: linear-gradient(135deg, #1e3a5f, #2b79c2); padding: 32px 24px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 22px;">Portal PQRSDF</h1>
                <p style="color: #bfdbfe; margin: 6px 0 0; font-size: 14px;">Tecnológico Elyon Yireh</p>
              </div>
              <div style="padding: 32px 24px; background: #ffffff;">
                <p style="color: #1e293b; font-size: 16px;">Estimado(a) <strong>%s</strong>,</p>
                <p style="color: #475569;">Su solicitud PQRSDF ha sido <strong style="color: #16a34a;">radicada exitosamente</strong> en nuestro sistema. A continuación, encontrará su número de radicado oficial:</p>

                <div style="background: #f0f9ff; border: 2px dashed #0284c7; border-radius: 10px; padding: 20px; text-align: center; margin: 24px 0;">
                  <p style="margin: 0; color: #64748b; font-size: 13px; text-transform: uppercase; letter-spacing: 1px;">Número de Radicado</p>
                  <p style="margin: 8px 0 0; color: #0284c7; font-size: 28px; font-weight: bold; letter-spacing: 2px;">%s</p>
                </div>

                <p style="color: #475569; font-size: 14px;">Guarde este número. Con él podrá hacer seguimiento al estado de su solicitud en cualquier momento a través de nuestro portal web.</p>
                <p style="color: #475569; font-size: 13px; margin-top: 24px;">Nuestro equipo dará respuesta en los tiempos establecidos por la Ley 1755 de 2015.</p>
              </div>
              <div style="background: #f8fafc; padding: 16px 24px; text-align: center; border-top: 1px solid #e2e8f0;">
                <p style="color: #94a3b8; font-size: 12px; margin: 0;">Este correo es generado automáticamente. Por favor no responda a este mensaje.</p>
              </div>
            </div>
            """.formatted(nombre, radicado);
    }

    private String buildHtmlAdmin(String nombreUsuario, String radicado, String tipoPqrsd) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;">
              <div style="background: linear-gradient(135deg, #7c2d12, #c2410c); padding: 32px 24px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 22px;">⚠️ Nueva PQRSDF Recibida</h1>
                <p style="color: #fed7aa; margin: 6px 0 0; font-size: 14px;">Notificación Automática del Sistema</p>
              </div>
              <div style="padding: 32px 24px; background: #ffffff;">
                <p style="color: #1e293b; font-size: 16px;">Se ha registrado una nueva <strong>%s</strong> en el sistema.</p>
                <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                  <tr style="background: #f8fafc;">
                    <td style="padding: 12px; font-weight: bold; color: #475569; border: 1px solid #e2e8f0;">Radicado:</td>
                    <td style="padding: 12px; color: #0284c7; font-weight: bold; border: 1px solid #e2e8f0;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding: 12px; font-weight: bold; color: #475569; border: 1px solid #e2e8f0;">Solicitante:</td>
                    <td style="padding: 12px; color: #1e293b; border: 1px solid #e2e8f0;">%s</td>
                  </tr>
                  <tr style="background: #f8fafc;">
                    <td style="padding: 12px; font-weight: bold; color: #475569; border: 1px solid #e2e8f0;">Estado Inicial:</td>
                    <td style="padding: 12px; color: #c2410c; font-weight: bold; border: 1px solid #e2e8f0;">SIN ASIGNAR</td>
                  </tr>
                </table>
                <p style="color: #475569; font-size: 14px;">📎 <strong>Adjunto encontrará el documento Word</strong> con todos los datos del solicitante y el detalle completo de la solicitud.</p>
                <p style="color: #475569; font-size: 14px;">Por favor, ingrese al sistema de administración para asignar y gestionar esta solicitud.</p>
              </div>
              <div style="background: #fff7ed; padding: 16px 24px; text-align: center; border-top: 1px solid #fed7aa;">
                <p style="color: #9a3412; font-size: 12px; margin: 0;">⏰ Recuerde: Esta solicitud requiere ser asignada y respondida en los tiempos de ley.</p>
              </div>
            </div>
            """.formatted(tipoPqrsd, radicado, nombreUsuario);
    }

    private String buildHtmlAreaAsignada(String responsableNombre, String nombreUsuario, String radicado, String tipoPqrsd) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;">
              <div style="background: linear-gradient(135deg, #0f766e, #047857); padding: 32px 24px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 22px;">📌 Nuevo Caso Asignado a su Área</h1>
                <p style="color: #a7f3d0; margin: 6px 0 0; font-size: 14px;">Sistema de Gestión PQRSDF</p>
              </div>
              <div style="padding: 32px 24px; background: #ffffff;">
                <p style="color: #1e293b; font-size: 16px;">Estimado(a) <strong>%s</strong>,</p>
                <p style="color: #475569;">El administrador del sistema ha asignado un nuevo caso de PQRSDF a su cargo resolutivo.</p>
                
                <div style="background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 16px; margin: 20px 0;">
                  <p style="margin: 0 0 8px 0; color: #166534;"><strong>Tipo de Solicitud:</strong> %s</p>
                  <p style="margin: 0 0 8px 0; color: #166534;"><strong>Radicado:</strong> %s</p>
                  <p style="margin: 0; color: #166534;"><strong>Nombre del Solicitante:</strong> %s</p>
                </div>

                <p style="color: #475569; font-size: 14px;">📎 Se adjunta a este correo un documento de Word con <strong>el detalle completo del caso, datos de contacto del estudiante y evidencias aportadas.</strong></p>
                <p style="color: #475569; font-size: 14px;">Agradecemos iniciar la gestión pertinente dentro de los tiempos de la ley estipulada.</p>
              </div>
              <div style="background: #f8fafc; padding: 16px 24px; text-align: center; border-top: 1px solid #e2e8f0;">
                <p style="color: #94a3b8; font-size: 12px; margin: 0;">Este mensaje es automático. Puede consultar más detalles ingresando con sus credenciales al sistema si aplica.</p>
              </div>
            </div>
            """.formatted(responsableNombre, tipoPqrsd, radicado, nombreUsuario);
    }

    private String buildHtmlRecordatorioArea(String responsableNombre, String nombreUsuario, String radicado, String tipoPqrsd) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;">
              <div style="background: linear-gradient(135deg, #b91c1c, #991b1b); padding: 32px 24px; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 22px;">⏰ URGENTE: Caso Sin Respuesta</h1>
                <p style="color: #fca5a5; margin: 6px 0 0; font-size: 14px;">Recordatorio del Sistema PQRSDF</p>
              </div>
              <div style="padding: 32px 24px; background: #ffffff;">
                <p style="color: #1e293b; font-size: 16px;">Estimado(a) <strong>%s</strong>,</p>
                <p style="color: #475569;">Le recordamos que han transcurrido <strong>varios días</strong> desde que se le asignó el siguiente caso y aún no ha registrado una respuesta ni cambiado de estado.</p>
                
                <div style="background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 16px; margin: 20px 0;">
                  <p style="margin: 0 0 8px 0; color: #991b1b;"><strong>Tipo de Solicitud:</strong> %s</p>
                  <p style="margin: 0 0 8px 0; color: #991b1b;"><strong>Radicado:</strong> %s</p>
                  <p style="margin: 0; color: #991b1b;"><strong>Nombre del Solicitante:</strong> %s</p>
                </div>

                <p style="color: #475569; font-size: 14px;">📎 Adjuntamos nuevamente los detalles de la solicitud.</p>
                <p style="color: #475569; font-size: 14px; font-weight: bold;">Le solicitamos gestionar este caso a la mayor brevedad para cumplir con los tiempos de la ley estipulada (15 días).</p>
              </div>
              <div style="background: #f8fafc; padding: 16px 24px; text-align: center; border-top: 1px solid #e2e8f0;">
                <p style="color: #94a3b8; font-size: 12px; margin: 0;">Este es un recordatorio automático del sistema.</p>
              </div>
            </div>
            """.formatted(responsableNombre, tipoPqrsd, radicado, nombreUsuario);
    }
}
