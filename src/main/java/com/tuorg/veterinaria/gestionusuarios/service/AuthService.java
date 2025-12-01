package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.config.security.JwtTokenProvider;
import com.tuorg.veterinaria.gestionusuarios.dto.*;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.common.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public AuthService(UsuarioRepository usuarioRepository,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       PasswordEncoder passwordEncoder,
                       CustomUserDetailsService userDetailsService,
                       RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public LoginResponse login(String username, String password) {
        // Validar entrada
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("El nombre de usuario es requerido");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("La contraseña es requerida");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    String msg = "Usuario no encontrado: " + username;
                    logger.warn(msg);
                    return new BusinessException(msg);
                });

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            String msg = "Usuario inactivo: " + username;
            logger.warn(msg);
            throw new BusinessException(msg);
        }

        String nombreRol = getNombreRol(usuario);

        authenticateUser(username, password);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = tokenProvider.generateToken(userDetails);

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        Long idUsuario = usuario.getIdUsuario() != null ? usuario.getIdUsuario() : usuario.getIdPersona();

        LoginResponse.UsuarioLoginResponse usuarioResponse = new LoginResponse.UsuarioLoginResponse(
                idUsuario,
                usuario.getNombre() != null ? usuario.getNombre() : "",
                usuario.getApellido() != null ? usuario.getApellido() : "",
                usuario.getCorreo() != null ? usuario.getCorreo() : "",
                nombreRol
        );

        logger.info("Login exitoso para usuario: {}", username);
        return new LoginResponse(token, "Bearer", usuarioResponse);
    }

    private void authenticateUser(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        // Si la autenticación falla, AuthenticationException se propaga automáticamente
        // y será manejada por el GlobalExceptionHandler
    }

    private String getNombreRol(Usuario usuario) {
        Rol rol = usuario.getRol();
        if (rol != null && rol.getNombreRol() != null && !rol.getNombreRol().isEmpty()) {
            return rol.getNombreRol();
        }
        return "SIN_ROL";
    }

    @Transactional
    public Usuario register(RegisterRequest request) {
        // Validar request
        if (request == null) {
            throw new BusinessException("Los datos de registro son requeridos");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException("El nombre de usuario es requerido");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BusinessException("El correo electrónico es requerido");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("La contraseña es requerida");
        }

        validateUserAvailability(request.getUsername(), request.getEmail());
        ValidationUtil.validatePassword(request.getPassword());

        String nombreRol = (request.getRol() != null && !request.getRol().trim().isEmpty())
                ? request.getRol().trim().toUpperCase()
                : "CLIENTE";

        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> {
                    String msg = "El rol '" + nombreRol + "' no está configurado";
                    logger.warn(msg);
                    return new BusinessException(msg);
                });

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setCorreo(request.getEmail());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setActivo(true);
        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

    private void validateUserAvailability(String username, String email) {
        if (usuarioRepository.existsByUsername(username)) {
            String msg = "El nombre de usuario ya está en uso: " + username;
            logger.warn(msg);
            throw new BusinessException(msg);
        }
        if (usuarioRepository.existsByCorreo(email)) {
            String msg = "El correo electrónico ya está en uso: " + email;
            logger.warn(msg);
            throw new BusinessException(msg);
        }
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        String emailOrUsername = request.getEmailOrUsername().trim();
        Usuario usuario = usuarioRepository.findByCorreo(emailOrUsername)
                .orElseGet(() -> usuarioRepository.findByUsername(emailOrUsername).orElse(null));

        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            logger.warn("Intento de recuperación de contraseña fallido para: {}", emailOrUsername);
            throw new BusinessException("Si el usuario existe, se enviará un correo con instrucciones");
        }

        String token = generateResetToken();
        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        logger.info("🔑 Token de recuperación generado para usuario: {} - Token: {}", usuario.getUsername(), token);
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validar request
        if (request == null || request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new BusinessException("Token de recuperación es requerido");
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new BusinessException("La nueva contraseña es requerida");
        }

        Usuario usuario = usuarioRepository.findByPasswordResetToken(request.getToken().trim())
                .orElseThrow(() -> new BusinessException("Token inválido o expirado"));

        boolean tokenValido = usuario.getPasswordResetTokenExpiry() != null &&
                usuario.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now());

        if (!tokenValido) {
            usuario.setPasswordResetToken(null);
            usuario.setPasswordResetTokenExpiry(null);
            usuarioRepository.save(usuario);
            logger.warn("Token expirado para usuario: {}", usuario.getUsername());
            throw new BusinessException("Token inválido o expirado");
        }

        ValidationUtil.validatePassword(request.getNewPassword());

        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuario.setPasswordResetToken(null);
        usuario.setPasswordResetTokenExpiry(null);
        usuarioRepository.save(usuario);

        logger.info("✅ Contraseña restablecida exitosamente para usuario: {}", usuario.getUsername());
    }

    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}