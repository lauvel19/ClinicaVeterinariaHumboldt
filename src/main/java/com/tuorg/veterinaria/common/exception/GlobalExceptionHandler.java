package com.tuorg.veterinaria.common.exception;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 *
 * Esta clase captura todas las excepciones no manejadas y las convierte
 * en respuestas HTTP apropiadas con formato estándar.
 *
 * Buenas prácticas aplicadas:
 * - Uso de SLF4J Logger en lugar de System.out
 * - Validación de posibles null en excepciones de validación
 * - Eliminación de parámetros o métodos redundantes
 * - Logging seguro y consistente
 * - Cumplimiento de advertencias de SonarQube
 *
 * @author Equipo de Desarrollo
 * @version 1.1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de recursos no encontrados (404).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // Logging de advertencia para recursos no encontrados
        logger.warn("Recurso no encontrado: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Maneja excepciones de negocio (400).
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        // Logging de advertencia para errores de negocio
        logger.warn("Error de negocio: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja excepciones de validación de argumentos (400).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        // Logging de advertencia de validación
        logger.warn("Error de validación: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        // Iteramos directamente sobre todos los errores
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error("Error de validación");
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja excepciones genéricas no previstas (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        // Logging seguro de excepción completa sin redundancias
        logger.error("Error inesperado", ex);

        ApiResponse<Object> response = ApiResponse.error(
                "Ocurrió un error inesperado. Por favor, contacte al administrador.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
