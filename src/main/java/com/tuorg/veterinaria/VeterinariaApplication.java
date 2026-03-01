package com.tuorg.veterinaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot.
 *
 * Esta clase inicia el contexto de Spring y levanta toda la aplicación
 * monolito-modular del sistema clínico veterinario.
 */
@SpringBootApplication
public class VeterinariaApplication {

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(VeterinariaApplication.class, args);
    }
}

