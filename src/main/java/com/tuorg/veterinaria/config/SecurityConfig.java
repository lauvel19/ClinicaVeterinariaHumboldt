package com.tuorg.veterinaria.config;

import com.tuorg.veterinaria.config.security.JwtAuthenticationEntryPoint;
import com.tuorg.veterinaria.config.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración central de Spring Security.
 *
 * Aquí definimos:
 * - Qué endpoints son públicos y cuáles necesitan JWT
 * - Cómo se maneja CORS
 * - El filtro JWT
 * - El encoder de contraseñas
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Maneja los errores de autenticación (401).
     * Ej: cuando se llama a un endpoint protegido sin token o con token inválido.
     */
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Filtro que se ejecuta en cada request para validar el JWT
     * (salvo en las rutas que marcamos como públicas).
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Orígenes permitidos para CORS.
     * Viene de application.yml / variables de entorno:
     *   app.cors.allowed-origins
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Métodos HTTP permitidos para CORS (GET, POST, etc.).
     */
    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    /**
     * Headers permitidos para CORS.
     */
    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    /**
     * Si se permiten credenciales (cookies, Authorization header, etc.) en CORS.
     */
    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    /**
     * Cadena de filtros principal de Spring Security.
     *
     * Aquí configuramos:
     * - CORS
     * - CSRF
     * - Sesiones stateless (JWT)
     * - Rutas públicas vs protegidas
     * - Filtro JWT antes del filtro de usuario/contraseña
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST ⇒ no usamos CSRF
            .csrf(csrf -> csrf.disable())

            // Usar nuestra config CORS personalizada (ver método corsConfigurationSource)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Manejo centralizado de errores de autenticación
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // JWT ⇒ la API es stateless, no guarda sesión en el servidor
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configuración de permisos por ruta
            .authorizeHttpRequests(auth -> auth

                // 1) Permitir todas las peticiones OPTIONS (preflight de CORS)
                .requestMatchers(HttpMethod.OPTIONS, "/").permitAll()

                // 2) Recursos estáticos del frontend (los sirve el propio backend)
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/static/",
                        "/assets/",
                        "/favicon.ico",
                        "/manifest.json",
                        "/logo192.png",
                        "/logo512.png",
                        "/LogoClinicaVeterinaria.png"
                ).permitAll()

                // 3) ENDPOINTS PÚBLICOS DE LA API
                // Importante: permitimos tanto /auth/** como /api/auth/**
                // porque puedes tener context-path /api en server.servlet.context-path
                .requestMatchers("/auth/", "/api/auth/").permitAll()
                .requestMatchers(
                        "/configuracion/parametros/",
                        "/api/configuracion/parametros/"
                ).permitAll()

                // 4) Swagger / documentación abierta (si la usas en prod)
                .requestMatchers(
                        "/swagger-ui",
                        "/swagger-ui.html",
                        "/swagger-ui/",
                        "/v3/api-docs",
                        "/v3/api-docs/",
                        "/swagger-resources",
                        "/swagger-resources/",
                        "/webjars/"
                ).permitAll()

                // 5) Cualquier otra ruta requiere autenticación con JWT
                .anyRequest().authenticated()
            )

            // Insertar el filtro JWT antes del filtro estándar de login
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean para encriptar contraseñas usando BCrypt.
     * Se usa tanto al guardar usuarios como al validar el login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración global de CORS.
     * Permite que el frontend (por ejemplo en Railway) llame al backend sin errores CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (separados por coma en la variable de entorno)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Métodos HTTP permitidos
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Headers permitidos
        if ("*".equals(allowedHeaders.trim())) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // ¿Permitimos credenciales (Authorization, cookies)? Sí/No según config
        configuration.setAllowCredentials(allowCredentials);

        // Headers que exponemos al frontend (para leer Authorization si hace falta)
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Tiempo que el preflight (OPTIONS) se puede cachear en el navegador
        configuration.setMaxAge(3600L);

        // Aplicar esta configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/", configuration);

        return source;
    }

    /**
     * Configura el AuthenticationManager para que use nuestro UserDetailsService y BCrypt.
     * Es el que realmente valida usuario/contraseña en el login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) throws Exception {

        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return authBuilder.build();
    }
}
