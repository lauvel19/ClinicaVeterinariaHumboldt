package com.tuorg.veterinaria.gestionusuarios.controller;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import com.tuorg.veterinaria.gestionusuarios.dto.*;
import com.tuorg.veterinaria.gestionusuarios.exception.AuthException;
import com.tuorg.veterinaria.gestionusuarios.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("🔐 Intento de login recibido - Username: {}", loginRequest.getUsername());
        try {
            LoginResponse tokenResponse = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            logger.info("✅ Login exitoso para usuario: {}", loginRequest.getUsername());
            return ResponseEntity.ok(ApiResponse.success("Login exitoso", tokenResponse));
        } catch (IllegalArgumentException | SecurityException e) {
            throw logAndThrowAuthException("Error de autenticación para el usuario: " + loginRequest.getUsername(), e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente"));
        } catch (IllegalArgumentException e) {
            throw logAndThrowAuthException("Error al registrar usuario: " + registerRequest.getUsername(), e);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("🔑 Solicitud de recuperación de contraseña recibida para: {}", request.getEmailOrUsername());
        try {
            String token = authService.forgotPassword(request);
            return ResponseEntity.ok(ApiResponse.success(
                    "Si el usuario existe, se enviará un correo con instrucciones. Token (solo desarrollo): " + token,
                    token
            ));
        } catch (IllegalArgumentException e) {
            throw logAndThrowAuthException("Error en recuperación de contraseña para: " + request.getEmailOrUsername(), e);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("🔐 Solicitud de restablecimiento de contraseña recibida");
        try {
            authService.resetPassword(request);
            logger.info("✅ Contraseña restablecida exitosamente");
            return ResponseEntity.ok(ApiResponse.success("Contraseña restablecida exitosamente"));
        } catch (IllegalArgumentException e) {
            throw logAndThrowAuthException("Error al restablecer contraseña con el token proporcionado", e);
        }
    }

    /**
     * Método utilitario para loguear y relanzar AuthException.
     */
    private AuthException logAndThrowAuthException(String message, Throwable cause) {
        logger.error(message, cause);
        return new AuthException(message, cause);
    }
}

