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
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // OJO: estos nombres deben existir en application.yml
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // API REST sin CSRF
            .csrf(csrf -> csrf.disable())

            // CORS con nuestra configuración
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Manejo de errores 401
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )

            // JWT ⇒ sin sesión en servidor
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // 1) Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/").permitAll()

                // 2) FRONTEND – recursos estáticos
                // incluimos rutas con y sin /api por si usas context-path
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/static/", "/api/static/",
                        "/assets/", "/api/assets/",
                        "/favicon.ico", "/api/favicon.ico",
                        "/manifest.json", "/api/manifest.json",
                        "/logo192.png", "/api/logo192.png",
                        "/logo512.png", "/api/logo512.png",
                        "/LogoClinicaVeterinaria.png", "/api/LogoClinicaVeterinaria.png"
                ).permitAll()

                // 3) ENDPOINTS PÚBLICOS DE LA API (login, parámetros)
                .requestMatchers("/auth/", "/api/auth/").permitAll()
                .requestMatchers(
                        "/configuracion/parametros/",
                        "/api/configuracion/parametros/"
                ).permitAll()

                // 4) Swagger público (si lo usas en prod)
                .requestMatchers(
                        "/swagger-ui", "/swagger-ui.html", "/swagger-ui/",
                        "/v3/api-docs", "/v3/api-docs/",
                        "/swagger-resources", "/swagger-resources/",
                        "/webjars/"
                ).permitAll()

                // 5) Todo lo demás requiere JWT
                .anyRequest().authenticated()
            )

            // Filtro JWT antes del UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración global de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos (separados por coma)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Métodos permitidos
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Headers permitidos
        if ("*".equals(allowedHeaders.trim())) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // ¿Permitimos credenciales?
        configuration.setAllowCredentials(allowCredentials);

        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Se aplica a TODAS las rutas
        source.registerCorsConfiguration("/", configuration);

        return source;
    }

    /**
     * AuthenticationManager para usar UserDetailsService + BCrypt.
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
