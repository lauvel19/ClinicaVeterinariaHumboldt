package com.tuorg.veterinaria.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuración para auditoría de JPA.
 * 
 * Define los beans necesarios para que Spring Data JPA automáticamente
 * llene los campos de auditoría (@CreatedBy, @LastModifiedBy, etc.)
 * 
 * Nota: Spring Boot 3.2.0 habilita JPA Auditing automáticamente,
 * solo necesitamos proporcionar el bean AuditorAware.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Configuration
public class AuditingConfiguration {

    /**
     * Bean que proporciona el usuario actual para auditoría.
     * 
     * Obtiene el usuario del contexto de seguridad de Spring Security.
     * Si no hay usuario autenticado, usa "SYSTEM" como valor por defecto.
     * 
     * @return AuditorAware que resuelve el usuario actual
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                // Obtener el contexto de autenticación
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                
                // Si no hay autenticación o es anónima, usar SYSTEM
                if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                    return Optional.of("SYSTEM");
                }
                
                // Retornar el nombre del usuario actual
                return Optional.of(authentication.getName());
            }
        };
    }
}
