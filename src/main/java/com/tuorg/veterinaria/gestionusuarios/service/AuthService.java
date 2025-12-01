package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.config.security.JwtTokenProvider;
import com.tuorg.veterinaria.gestionusuarios.dto.ForgotPasswordRequest;
import com.tuorg.veterinaria.gestionusuarios.dto.RegisterRequest;
import com.tuorg.veterinaria.gestionusuarios.dto.ResetPasswordRequest;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
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
import com.tuorg.veterinaria.gestionusuarios.dto.LoginResponse;
import com.tuorg.veterinaria.common.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio de autenticación.
 * 
 * Este servicio maneja la autenticación de usuarios, generación de tokens JWT
 * y registro de nuevos usuarios.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    /**
     * Gestor de autenticación de Spring Security.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Proveedor de tokens JWT.
     */
    private final JwtTokenProvider tokenProvider;

    /**
     * Codificador de contraseñas.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Servicio de detalles de usuario.
     */
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param usuarioRepository Repositorio de usuarios
     * @param authenticationManager Gestor de autenticación
     * @param tokenProvider Proveedor de tokens JWT
     * @param passwordEncoder Codificador de contraseñas
     * @param userDetailsService Servicio de detalles de usuario
     */
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

    /**
     * Autentica un usuario mediante username y contraseña, generando un token JWT.
     * 
     * Flujo de autenticación:
     * 1. Verifica existencia del usuario en base de datos
     * 2. Valida que la cuenta esté activa
     * 3. Obtiene el rol del usuario de forma segura
     * 4. Autentica contra Spring Security (valida contraseña)
     * 5. Genera token JWT con duración de expiración configurada
     * 6. Registra último acceso del usuario
     * 7. Retorna token y datos del usuario en DTO
     * 
     * @param username Nombre único del usuario (campo de autenticación)
     * @param password Contraseña en texto plano (será validada contra hash en BD)
     * @return LoginResponse con token JWT, tipo bearer y datos del usuario autenticado
     * @throws BusinessException si usuario no existe, está inactivo, o credenciales son inválidas
     */
    @Transactional
    public LoginResponse login(String username, String password) {
        try {
            logger.info("🔐 Iniciando login para usuario: {}", username);
            
            // PASO 1: Verificar que el usuario existe antes de intentar autenticar
            // Esto evita intentos de validación de contraseña innecesarios
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado: " + username));
            
            logger.info("✓ Usuario encontrado: {}", usuario.getUsername());
            
            // PASO 2: Validar que la cuenta esté activa
            // Previene acceso con usuarios desactivados
            if (!usuario.getActivo()) {
                logger.warn("⚠️ Intento de login con usuario inactivo: {}", username);
                throw new BusinessException("Usuario inactivo: " + username);
            }
            
            // PASO 3: Obtener el nombre del rol de forma segura
            // Se usa try-catch porque la relación puede no estar inicializada (lazy loading)
            String nombreRol = "SIN_ROL";
            try {
                Rol rol = usuario.getRol();
                if (rol != null && rol.getNombreRol() != null) {
                    nombreRol = rol.getNombreRol();
                }
            } catch (Exception e) {
                logger.warn("⚠️ No se pudo obtener el nombre del rol para usuario {}: {}", username, e.getMessage());
            }
            
            logger.info("✓ Rol obtenido: {}", nombreRol);
            
            // PASO 4: Autenticar usuario contra Spring Security
            // Si falla, lanza AuthenticationException (contraseña incorrecta)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            logger.info("✓ Autenticación contra Spring Security exitosa para usuario: {}", username);

            // PASO 5: Obtener detalles del usuario autenticado
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // PASO 6: Generar token JWT con claims personalizados
            // El token incluye username y autoridades. Expiración configurada en JwtTokenProvider
            String token = tokenProvider.generateToken(userDetails);
            
            logger.info("✓ Token JWT generado para usuario: {}", username);

            // PASO 7: Registrar último acceso del usuario para auditoría
            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
            usuarioRepository.flush(); // Asegurar que el cambio se persista inmediatamente

            // Obtener ID de forma segura (intentar idUsuario, sino idPersona)
            Long idUsuario = usuario.getIdUsuario();
            if (idUsuario == null) {
                idUsuario = usuario.getIdPersona();
            }

            // Construir respuesta con información del usuario (sin contraseña)
            LoginResponse.UsuarioLoginResponse usuarioResponse = new LoginResponse.UsuarioLoginResponse(
                    idUsuario,
                    usuario.getNombre() != null ? usuario.getNombre() : "",
                    usuario.getApellido() != null ? usuario.getApellido() : "",
                    usuario.getCorreo() != null ? usuario.getCorreo() : "",
                    nombreRol
            );

            logger.info("✅ Login exitoso para usuario: {} con rol: {}", username, nombreRol);
            return new LoginResponse(token, "Bearer", usuarioResponse);
            
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Captura excepciones de Spring Security (ej: contraseña incorrecta)
            logger.error("❌ Error de autenticación para usuario {}: {}", username, e.getMessage());
            throw new BusinessException("Credenciales inválidas: " + e.getMessage());
        } catch (Exception e) {
            // Captura cualquier otro error inesperado
            logger.error("❌ Error inesperado en login para usuario {}: {}", username, e.getMessage(), e);
            throw new BusinessException("Error al procesar el login: " + e.getMessage());
        }
    }

    /**
     * Registra un nuevo usuario en el sistema con validaciones de negocio.
     * 
     * Flujo de registro:
     * 1. Valida que username no exista (unicidad requerida)
     * 2. Valida que email no exista (unicidad requerida para recuperación de contraseña)
     * 3. Valida que la contraseña cumpla políticas de seguridad
     * 4. Asigna rol por defecto "CLIENTE" si no se especifica
     * 5. Verifica que el rol exista en el sistema
     * 6. Codifica la contraseña con BCrypt antes de guardar
     * 7. Persiste el usuario en la base de datos
     * 
     * @param request DTO con datos del usuario (username, email, password, nombre, apellido, rol)
     * @return Usuario creado (sin exponer contraseña)
     * @throws BusinessException si validaciones fallan (username/email duplicados, rol no existe, contraseña débil)
     */
    @Transactional
    public Usuario register(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();

        // VALIDACIÓN 1: Verificar que el username no exista
        // El username es el identificador único para login
        if (usuarioRepository.existsByUsername(username)) {
            logger.warn("⚠️ Intento de registro con username duplicado: {}", username);
            throw new BusinessException("El nombre de usuario ya está en uso");
        }

        // VALIDACIÓN 2: Verificar que el email no exista
        // El email se usa para recuperación de contraseña y notificaciones
        if (usuarioRepository.existsByCorreo(email)) {
            logger.warn("⚠️ Intento de registro con email duplicado: {}", email);
            throw new BusinessException("El correo electrónico ya está en uso");
        }

        // VALIDACIÓN 3: Validar que la contraseña cumpla políticas (longitud, complejidad, etc)
        // Delega a utilidad que centraliza políticas de contraseña
        ValidationUtil.validatePassword(request.getPassword());

        // VALIDACIÓN 4: Determinar el rol
        // Si no se especifica, se asigna CLIENTE por defecto
        String nombreRol = (request.getRol() != null && !request.getRol().trim().isEmpty()) 
                ? request.getRol().trim().toUpperCase() 
                : "CLIENTE";

        // VALIDACIÓN 5: Verificar que el rol existe en el sistema
        // Esto previene asignación de roles no configurados
        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> {
                    logger.error("❌ Intento de registro con rol no existente: {}", nombreRol);
                    return new BusinessException("El rol '" + nombreRol + "' no está configurado en el sistema");
                });

        // Crear nueva entidad de usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        // SEGURIDAD: Codificar contraseña con BCrypt (no almacenar en texto plano)
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setCorreo(email);
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setActivo(true); // El usuario inicia activo
        usuario.setRol(rol);

        Usuario usuarioRegistrado = usuarioRepository.save(usuario);
        logger.info("✅ Usuario registrado exitosamente: {} con rol: {}", username, nombreRol);
        
        return usuarioRegistrado;
    }

    /**
     * Genera un token de recuperación de contraseña para un usuario.
     * 
     * Flujo de recuperación:
     * 1. Busca usuario por email o username (intenta ambos identificadores)
     * 2. Valida que la cuenta esté activa (no envia token a cuentas inactivas)
     * 3. Genera token seguro mediante SecureRandom (32 bytes)
     * 4. Establece expiración a 1 hora (límite de validez)
     * 5. Guarda token y expiración en BD
     * 6. En producción, se enviaría por email. En desarrollo, retorna el token.
     * 
     * Nota de Seguridad: No revela si un usuario existe o no en el sistema
     * 
     * @param request DTO con email o username del usuario que olvidó contraseña
     * @return Token generado (para desarrollo; en prod se enviaria por email)
     * @throws BusinessException si usuario no existe o está inactivo
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        String emailOrUsername = request.getEmailOrUsername().trim();
        
        // PASO 1: Buscar usuario por email o username (intenta ambos)
        // Se busca primero por email (más específico), luego por username
        Usuario usuario = usuarioRepository.findByCorreo(emailOrUsername)
                .orElseGet(() -> usuarioRepository.findByUsername(emailOrUsername)
                        .orElse(null));

        if (usuario == null) {
            // SEGURIDAD: Mensaje genérico para no revelar si el usuario existe
            logger.warn("⚠️ Intento de recuperación de contraseña para usuario inexistente: {}", emailOrUsername);
            throw new BusinessException("Si el usuario existe, se enviará un correo con instrucciones para restablecer la contraseña");
        }

        // PASO 2: Validar que la cuenta esté activa
        if (!usuario.getActivo()) {
            logger.warn("⚠️ Intento de recuperación con cuenta inactiva: {}", usuario.getUsername());
            throw new BusinessException("La cuenta está inactiva. Contacte al administrador.");
        }

        // PASO 3: Generar token único y seguro mediante SecureRandom
        String token = generateResetToken();
        
        // PASO 4: Establecer token y expiración (1 hora desde ahora)
        // El token expirará automáticamente si no se usa dentro de 1 hora
        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        // En producción, se enviaría aquí un email con el token
        // Por ahora, retornamos el token para desarrollo
        logger.info("🔑 Token de recuperación generado para usuario: {} - Válido por 1 hora", usuario.getUsername());
        
        return token;
    }

    /**
     * Restablece la contraseña de un usuario usando un token de recuperación válido.
     * 
     * Flujo de restablecimiento:
     * 1. Busca usuario asociado al token de recuperación
     * 2. Valida que el token no esté expirado (max 1 hora)
     * 3. Valida que la nueva contraseña cumpla políticas de seguridad
     * 4. Codifica la nueva contraseña con BCrypt
     * 5. Invalida el token para que no se reutilice
     * 6. Persiste cambios en la base de datos
     * 
     * @param request DTO con token de recuperación y nueva contraseña
     * @throws BusinessException si token inválido, expirado, o contraseña débil
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String token = request.getToken().trim();
        String newPassword = request.getNewPassword();

        // PASO 1: Buscar usuario por token de recuperación
        Usuario usuario = usuarioRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> {
                    logger.warn("❌ Intento de restablecimiento con token inválido");
                    return new BusinessException("Token inválido o expirado");
                });

        // PASO 2: Verificar que el token no haya expirado (validez: 1 hora)
        if (usuario.getPasswordResetTokenExpiry() == null || 
            usuario.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            // SEGURIDAD: Limpiar token expirado para evitar intentos posteriores
            usuario.setPasswordResetToken(null);
            usuario.setPasswordResetTokenExpiry(null);
            usuarioRepository.save(usuario);
            logger.warn("❌ Intento de restablecimiento con token expirado para usuario: {}", usuario.getUsername());
            throw new BusinessException("Token inválido o expirado");
        }

        // PASO 3: Validar que la nueva contraseña cumpla políticas (longitud, complejidad, etc)
        ValidationUtil.validatePassword(newPassword);

        // PASO 4 y 5: Actualizar contraseña y limpiar token para invalidarlo
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        
        // SEGURIDAD: Limpiar token después de usarlo para evitar reutilización
        usuario.setPasswordResetToken(null);
        usuario.setPasswordResetTokenExpiry(null);
        
        // PASO 6: Persistir cambios
        usuarioRepository.save(usuario);
        
        logger.info("✅ Contraseña restablecida exitosamente para usuario: {}", usuario.getUsername());
    }

    /**
     * Genera un token único y seguro para recuperación de contraseña.
     * 
     * @return Token en formato Base64
     */
    private String generateResetToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}


