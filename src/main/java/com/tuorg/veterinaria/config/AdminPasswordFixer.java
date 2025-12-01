package com.tuorg.veterinaria.config;

import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * Inicializador que fuerza la actualización del password del admin.
 */
@Component
@Order(1)
public class AdminPasswordFixer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminPasswordFixer.class);
    private static final String ADMIN_USERNAME = "admin";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    @Autowired
    public AdminPasswordFixer(UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            logger.warn("⚠️ No se configuró la variable ADMIN_PASSWORD. Saltando inicialización.");
            return;
        }

        logger.info("🔧 Ejecutando corrección de password para usuario admin...");

        usuarioRepository.findByUsername(ADMIN_USERNAME).ifPresentOrElse(admin -> {
            String nuevoHash = passwordEncoder.encode(adminPassword);
            admin.setPasswordHash(nuevoHash);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            logger.info("✅ Password del admin actualizado correctamente!");
        }, () -> logger.error("❌ Usuario admin no encontrado en la base de datos!"));
    }
}
