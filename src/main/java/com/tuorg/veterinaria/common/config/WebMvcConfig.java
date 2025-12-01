package com.tuorg.veterinaria.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC.
 * Registra interceptores personalizados.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Nota: RateLimitInterceptor comentado temporalmente
    // @Autowired
    // private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Nota: Rate limiting deshabilitado temporalmente
        // registry.addInterceptor(rateLimitInterceptor)
        //         .addPathPatterns(
        //             "/api/auth/login",
        //             "/api/auth/register", 
        //             "/api/auth/reset-password",
        //             "/api/auth/forgot-password"
        //         );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // NOTA: Los view controllers están deshabilitados porque el frontend se sirve
        // por separado con Vite en desarrollo y como build estático en producción.
        // Si se necesita servir el frontend desde Spring Boot, descomentar y usar
        // patrones compatibles con Spring Boot 3.2+
        
        // registry.addViewController("/").setViewName("forward:/index.html");
    }
}
