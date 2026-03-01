package com.tuorg.veterinaria.common.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de rate limiting utilizando Bucket4j.
 * 
 * Implementa el algoritmo Token Bucket para limitar la tasa de peticiones
 * por IP. Útil para prevenir ataques de fuerza bruta y DDoS.
 */
@Component
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resuelve un bucket para una clave específica (normalmente una IP).
     * Si no existe, lo crea con los parámetros por defecto.
     * 
     * @param key Clave del bucket (normalmente dirección IP)
     * @return Bucket asociado a la clave
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Crea un nuevo bucket con límite de 5 peticiones por minuto.
     * 
     * @return Nuevo bucket configurado
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillIntervally(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Crea un bucket personalizado con límite específico.
     * 
     * @param capacity Número máximo de peticiones
     * @param refillTokens Número de tokens a recargar
     * @param refillDuration Duración entre recargas
     * @return Bucket configurado con parámetros personalizados
     */
    public Bucket createCustomBucket(long capacity, long refillTokens, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillTokens, refillDuration)
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Verifica si una clave puede hacer una petición (consume un token).
     * 
     * @param key Clave del bucket (normalmente dirección IP)
     * @return true si la petición es permitida, false si excede el límite
     */
    public boolean tryConsume(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Limpia la caché de buckets (útil para tests o mantenimiento).
     */
    public void clearCache() {
        cache.clear();
    }
}
