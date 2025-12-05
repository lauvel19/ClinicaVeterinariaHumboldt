package com.tuorg.veterinaria.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Procesa la variable DATABASE_URL de Render y la convierte al formato JDBC.
 * Render provee: postgresql://user:pass@host:port/database
 * Spring necesita: jdbc:postgresql://host:port/database
 */
public class RenderDatabaseUrlProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            
            // Convertir postgresql:// a jdbc:postgresql://
            // También maneja postgres:// (alias común)
            String jdbcUrl = databaseUrl;
            if (jdbcUrl.startsWith("postgres://")) {
                jdbcUrl = jdbcUrl.replace("postgres://", "jdbc:postgresql://");
            } else if (jdbcUrl.startsWith("postgresql://")) {
                jdbcUrl = jdbcUrl.replace("postgresql://", "jdbc:postgresql://");
            }
            
            // Extraer usuario y contraseña de la URL si están incluidos
            // Formato: jdbc:postgresql://user:password@host:port/database
            if (jdbcUrl.contains("@")) {
                try {
                    // Parsear la URL para extraer componentes
                    String urlWithoutPrefix = jdbcUrl.substring("jdbc:postgresql://".length());
                    int atIndex = urlWithoutPrefix.indexOf("@");
                    String credentials = urlWithoutPrefix.substring(0, atIndex);
                    String hostAndDb = urlWithoutPrefix.substring(atIndex + 1);
                    
                    // Extraer usuario y contraseña
                    String[] credParts = credentials.split(":", 2);
                    if (credParts.length == 2) {
                        props.put("spring.datasource.username", credParts[0]);
                        props.put("spring.datasource.password", credParts[1]);
                    }
                    
                    // Reconstruir URL sin credenciales
                    jdbcUrl = "jdbc:postgresql://" + hostAndDb;
                } catch (Exception e) {
                    // Si hay error parseando, usar la URL como está
                    System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                }
            }
            
            props.put("spring.datasource.url", jdbcUrl);
            
            environment.getPropertySources().addFirst(
                new MapPropertySource("renderDatabaseConfig", props)
            );
            
            System.out.println("Render DATABASE_URL processed successfully");
        }
    }
}
