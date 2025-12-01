package com.tuorg.veterinaria.config;

import com.tuorg.veterinaria.config.security.JwtAuthenticationEntryPoint;
import com.tuorg.veterinaria.config.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Configuración de seguridad de Spring Security.
 * 
 * Esta clase configura la seguridad de la aplicación, incluyendo:
 * - Autenticación JWT
 * - Autorización basada en roles
 * - Configuración CORS
 * - Filtros de seguridad
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Punto de entrada para manejar errores de autenticación.
     */
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Filtro JWT para validar tokens en cada petición.
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Orígenes permitidos para CORS desde configuración.
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Métodos HTTP permitidos para CORS.
     */
    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    /**
     * Headers permitidos para CORS.
     */
    @Value("${app.cors.allowed-headers}")
    private String allowedHeaders;

    /**
     * Permitir credenciales en CORS.
     */
    @Value("${app.cors.allow-credentials}")
    private boolean allowCredentials;


    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * @param http HttpSecurity para configurar
     * @return SecurityFilterChain configurado
     * @throws Exception Si hay error en la configuración
     */
    @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                // === FRONTEND ===
                .requestMatchers(
                        "/",                  
                        "/index.html",
                        "/static/**",
                        "/assets/**",
                        "/favicon.ico",
                        "/manifest.json",
                        "/logo192.png",
                        "/logo512.png"
                ).permitAll()

                // === ENDPOINTS PÚBLICOS DE LA API (/api/...) ===
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/configuracion/parametros/**").permitAll()

                // === SWAGGER ===
                .requestMatchers(
                        "/swagger-ui", "/swagger-ui.html", "/swagger-ui/**", 
                        "/v3/api-docs", "/v3/api-docs/**",
                        "/swagger-resources", "/swagger-resources/**", 
                        "/webjars/**"
                ).permitAll()

                // === TODO LO DEMÁS DE LA API VA CON JWT ===
                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
        }


    /**
     * Bean para el codificador de contraseñas.
     * Utiliza BCrypt para hashear contraseñas.
     * 
     * @return PasswordEncoder configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Configurar orígenes permitidos
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Configurar métodos HTTP permitidos
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Configurar headers
        if ("*".equals(allowedHeaders)) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // Configurar credenciales
        configuration.setAllowCredentials(allowCredentials);

        // Exponer headers de autorización
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Bean para el gestor de autenticación.
     * 
     * @param http HttpSecurity para configurar
     * @param passwordEncoder Codificador de contraseñas
     * @param userDetailsService Servicio de detalles de usuario
     * @return AuthenticationManager
     * @throws Exception Si hay error en la configuración
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       PasswordEncoder passwordEncoder,
                                                       UserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }
}
