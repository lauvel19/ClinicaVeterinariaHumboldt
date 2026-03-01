package com.tuorg.veterinaria.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura el CacheManager con Caffeine para gestionar cachés de la
     * aplicación.
     * 
     * Cachés configurados:
     * - productos: Lista completa de productos (TTL: 10 minutos)
     * - productosPorTipo: Productos filtrados por tipo (TTL: 10 minutos)
     * - veterinariosActivos: Lista de veterinarios activos (TTL: 30 minutos)
     * - configuraciones: Configuraciones del sistema (TTL: 1 hora)
     * - pacientesPorCliente: Pacientes de un cliente específico (TTL: 5 minutos)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "productos",
                "productosPorTipo",
                "veterinariosActivos",
                "configuraciones",
                "pacientesPorCliente");

        cacheManager.setCaffeine(Objects.requireNonNull(caffeineCacheBuilder()));
        return cacheManager;
    }

    /**
     * Configura el builder de Caffeine con parámetros de expiración y tamaño.
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES) // TTL por defecto
                .maximumSize(1000) // Máximo 1000 entradas por caché
                .recordStats(); // Habilita estadísticas de caché
    }
}
