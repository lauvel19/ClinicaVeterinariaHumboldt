package com.tuorg.veterinaria.notificaciones.config;

import com.tuorg.veterinaria.notificaciones.model.CanalEmail;
import com.tuorg.veterinaria.notificaciones.repository.CanalEnvioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Configuración para inyectar JavaMailSender en los canales de email existentes.
 *
 * Esta clase se encarga de configurar automáticamente el JavaMailSender
 * en todos los canales de email que estén registrados en la base de datos.
 *
 * @author Equipo de Desarrollo
 * @version 1.0.3
 */
@Slf4j
@Configuration
public class EmailConfig {

    private final JavaMailSender mailSender;
    private final CanalEnvioRepository canalEnvioRepository;

    public EmailConfig(JavaMailSender mailSender, CanalEnvioRepository canalEnvioRepository) {
        this.mailSender = mailSender;
        this.canalEnvioRepository = canalEnvioRepository;
    }

    @PostConstruct
    public void configurarCanalesEmail() {
        try {
            List<?> canales = canalEnvioRepository.findAll();

            int contador = 0;
            for (Object obj : canales) {
                if (obj instanceof CanalEmail canalEmail) {
                    canalEmail.setMailSender(mailSender);
                    contador++;
                }
            }

            log.info("JavaMailSender configurado en {} canales de email", contador);

        } catch (Exception e) {
            log.warn(
                    "No se pudieron configurar los canales de email: {}. Esto es normal si la base de datos aún no tiene registros de canales.",
                    e.getMessage()
            );
        }
    }
}
