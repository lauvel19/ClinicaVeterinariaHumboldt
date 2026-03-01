package com.tuorg.veterinaria.common.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Objects;

/**
 * Implementación de AuditorAware para capturar el usuario actual.
 * 
 * Utiliza Spring Security para obtener el username del usuario autenticado
 * y registrarlo en los campos de auditoría.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * Obtiene el usuario actual del contexto de seguridad.
     * 
     * @return Optional con el username del usuario autenticado,
     *         o "system" si no hay usuario autenticado
     */
    @Override
    @org.springframework.lang.NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Objects.requireNonNull(Optional.of("system"));
        }

        return Objects.requireNonNull(Optional.of(authentication.getName()));
    }
}
