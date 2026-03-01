package com.tuorg.veterinaria.common.config;

import com.tuorg.veterinaria.common.ratelimit.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Objects;

/**
 * Configuración de Spring MVC.
 * 
 * Registra interceptores personalizados incluyendo rate limiting
 * para endpoints sensibles de autenticación.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Aplicar rate limiting a endpoints de autenticación
        registry.addInterceptor(Objects.requireNonNull(rateLimitInterceptor))
                .addPathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/reset-password",
                        "/api/auth/forgot-password");
    }
}
