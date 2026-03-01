package com.tuorg.veterinaria.notificaciones.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.util.Objects;

/**
 * Implementación concreta de CanalEnvio para envío por email.
 * 
 * Esta clase extiende CanalEnvio e implementa la estrategia de envío
 * por correo electrónico (Strategy pattern).
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Slf4j
@Entity
@Table(name = "canales_email", schema = "public")
@PrimaryKeyJoinColumn(name = "id_canal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CanalEmail extends CanalEnvio {

    /**
     * Servidor SMTP para el envío de emails.
     */
    @Column(name = "smtp_server", length = 150)
    private String smtpServer;

    /**
     * Dirección de correo del remitente.
     */
    @Column(name = "from_address", length = 150)
    private String fromAddress;

    /**
     * JavaMailSender inyectado por Spring para envío de emails.
     * 
     * @Transient para que JPA no intente persistirlo en la BD.
     */
    @Transient
    private JavaMailSender mailSender;

    /**
     * Implementación del método enviar para email.
     * 
     * @param notificacion Notificación a enviar
     * @return true si el envío fue exitoso, false en caso contrario
     */
    @Override
    public boolean enviar(Notificacion notificacion) {
        Objects.requireNonNull(notificacion);
        // Validar que el JavaMailSender esté configurado
        if (mailSender == null) {
            log.error("JavaMailSender no está configurado. Verifica la configuración de Spring Mail.");
            return false;
        }

        try {
            // Crear mensaje MIME para HTML
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(Objects.requireNonNull(fromAddress));
            helper.setTo(Objects.requireNonNull(extraerEmailDestinatario(notificacion)));
            helper.setSubject(Objects.requireNonNull(construirAsunto(notificacion)));

            // Construir contenido HTML profesional
            String htmlContent = construirEmailHTML(notificacion);
            helper.setText(Objects.requireNonNull(htmlContent), true); // true = es HTML

            // Enviar el email
            mailSender.send(mimeMessage);

            log.info("Email HTML enviado exitosamente a través de {}", smtpServer);
            log.debug("Para: {}, Asunto: {}", extraerEmailDestinatario(notificacion), construirAsunto(notificacion));

            return true;

        } catch (Exception e) {
            log.error("Error al enviar email: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extrae el email del destinatario desde los datos de la notificación.
     * Lee el campo "destinatario" del JSON almacenado en notificacion.datos
     */
    private String extraerEmailDestinatario(Notificacion notificacion) {
        try {
            Objects.requireNonNull(notificacion);
            // Parsear el JSON de datos para obtener el destinatario
            String datosJson = notificacion.getDatos();
            if (datosJson != null && !datosJson.isEmpty()) {
                // Buscar el campo "destinatario" en el JSON
                // Formato esperado: {"destinatario":"email@ejemplo.com", ...}
                if (datosJson.contains("\"destinatario\"")) {
                    int inicioEmail = datosJson.indexOf("\"destinatario\":\"") + 16;
                    int finEmail = datosJson.indexOf("\"", inicioEmail);
                    if (finEmail > inicioEmail) {
                        String emailDestinatario = datosJson.substring(inicioEmail, finEmail);
                        log.debug("Destinatario extraído: {}", emailDestinatario);
                        return emailDestinatario;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error al extraer destinatario, usando from_address: {}", e.getMessage());
        }

        // Fallback: Si no se puede extraer el destinatario, usar from_address
        log.warn("No se pudo extraer destinatario, usando from_address: {}", fromAddress);
        return fromAddress;
    }

    /**
     * Construye el asunto del email basado en el tipo de notificación.
     */
    private String construirAsunto(Notificacion notificacion) {
        Objects.requireNonNull(notificacion);
        String tipoFormateado = notificacion.getTipo()
                .replace("_", " ")
                .toLowerCase();

        // Capitalizar primera letra de cada palabra
        String[] palabras = tipoFormateado.split(" ");
        StringBuilder asunto = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                asunto.append(Character.toUpperCase(palabra.charAt(0)))
                        .append(palabra.substring(1))
                        .append(" ");
            }
        }

        return "🐾 Clínica Veterinaria Humboldt - " + asunto.toString().trim();
    }

    /**
     * Construye el contenido HTML profesional del email con diseño responsive y
     * colores.
     */
    private String construirEmailHTML(Notificacion notificacion) {
        Objects.requireNonNull(notificacion);
        String mensaje = notificacion.getMensaje();
        String tipo = notificacion.getTipo();

        // Colores según el tipo de notificación - Azul profesional
        String colorPrincipal = "#1565C0"; // Azul oscuro profesional
        String colorSecundario = "#1976D2"; // Azul medio
        String tituloNotificacion = "Confirmación de Cita";

        if (tipo.contains("CANCELADA")) {
            colorPrincipal = "#C62828"; // Rojo
            colorSecundario = "#EF5350";
            tituloNotificacion = "Cita Cancelada";
        } else if (tipo.contains("REPROGRAMADA")) {
            colorPrincipal = "#F57C00"; // Naranja
            colorSecundario = "#FF9800";
            tituloNotificacion = "Cita Reprogramada";
        }

        // Limpiar mensaje: remover asteriscos y emojis excesivos
        String mensajeLimpio = mensaje
                .replace("🐾 *", "")
                .replace("*", "")
                .replace("📅 ", "")
                .replace("🐕 ", "")
                .replace("👨‍⚕️ ", "")
                .replace("🏥 ", "")
                .replace("📍 ", "")
                .replace("📞 ", "")
                .replace("📧 ", "")
                .replace("🔄 ", "")
                .replace("❌ ", "")
                .trim();

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Clínica Veterinaria Humboldt</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; background-color: #f0f2f5;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f0f2f5; padding: 30px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 6px 16px rgba(0,0,0,0.12); overflow: hidden; max-width: 100%%;">

                                    <!-- Header con gradiente azul -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 50px 40px; text-align: center; position: relative;">
                                            <div style="background-color: rgba(255,255,255,0.15); width: 80px; height: 80px; margin: 0 auto 20px; border-radius: 50%%; display: inline-block; line-height: 80px; font-size: 40px;">
                                                🐾
                                            </div>
                                            <h1 style="color: #ffffff; margin: 0; font-size: 32px; font-weight: 700; letter-spacing: -0.5px; text-shadow: 0 2px 8px rgba(0,0,0,0.15);">
                                                Clínica Veterinaria Humboldt
                                            </h1>
                                            <p style="color: rgba(255,255,255,0.95); margin: 12px 0 0 0; font-size: 16px; font-weight: 400;">
                                                Cuidando a tus mascotas con amor y profesionalismo
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Título de la notificación -->
                                    <tr>
                                        <td style="padding: 35px 40px 25px; text-align: center;">
                                            <h2 style="color: %s; margin: 0; font-size: 24px; font-weight: 600;">
                                                %s
                                            </h2>
                                        </td>
                                    </tr>

                                    <!-- Contenido del mensaje -->
                                    <tr>
                                        <td style="padding: 0 40px 40px;">
                                            <div style="background-color: #f8f9fa; border-left: 4px solid %s; border-radius: 6px; padding: 25px; color: #2c3e50; font-size: 15px; line-height: 1.8;">
                                                %s
                                            </div>
                                        </td>
                                    </tr>

                                    <!-- Divider decorativo -->
                                    <tr>
                                        <td style="padding: 0 40px;">
                                            <div style="border-top: 1px solid #e1e4e8; margin: 20px 0;"></div>
                                        </td>
                                    </tr>

                                    <!-- Footer con información de contacto -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 40px; text-align: center;">
                                            <table width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td style="padding-bottom: 20px;">
                                                        <h3 style="color: #2c3e50; font-size: 18px; font-weight: 600; margin: 0 0 20px 0;">
                                                            Información de Contacto
                                                        </h3>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td style="color: #5a6c7d; font-size: 14px; line-height: 2; text-align: center;">
                                                        <strong style="color: #2c3e50;">Dirección:</strong> Cl. 6 Nte. #14-26, Armenia, Quindío<br>
                                                        <strong style="color: #2c3e50;">Teléfono:</strong> 3112324283<br>
                                                        <strong style="color: #2c3e50;">Email:</strong> contacto@veterinariahumboldt.com
                                                    </td>
                                                </tr>
                                            </table>

                                            <div style="margin-top: 30px; padding-top: 25px; border-top: 1px solid #e1e4e8;">
                                                <p style="color: #8b95a1; font-size: 12px; margin: 0; line-height: 1.6;">
                                                    Este es un correo automático generado por el sistema de gestión.<br>
                                                    Por favor no responder a este mensaje.
                                                </p>
                                                <p style="color: #b4bcc4; font-size: 11px; margin: 15px 0 0 0;">
                                                    © 2025 Clínica Veterinaria Humboldt. Todos los derechos reservados.
                                                </p>
                                            </div>
                                        </td>
                                    </tr>

                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(
                        colorPrincipal,
                        colorSecundario,
                        colorPrincipal,
                        tituloNotificacion,
                        colorPrincipal,
                        mensajeLimpio.replace("\n", "<br>"));
    }
}
