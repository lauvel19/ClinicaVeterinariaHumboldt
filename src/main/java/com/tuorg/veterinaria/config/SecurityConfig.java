package com.tuorg.veterinaria.config;

import com.tuorg.veterinaria.config.security.JwtAuthenticationEntryPoint;
import com.tuorg.veterinaria.config.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Configura la cadena de filtros de seguridad.
     * 
     * @param http HttpSecurity para configurar
     * @return SecurityFilterChain configurado
     * @throws Exception Si hay error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF porque usamos JWT
                .csrf(csrf -> csrf.disable())
                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configurar manejo de excepciones
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                // Configurar política de sesiones (stateless porque usamos JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Configurar autorización de endpoints
                // NOTA: El context-path es /api, por lo que las rutas aquí NO deben incluir /api
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin /api porque el context-path ya lo incluye)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/configuracion/parametros/**").permitAll()
                        .requestMatchers("/pagos/webhook/epayco").permitAll() // Webhook de ePayco
                        // Swagger/OpenAPI endpoints públicos para documentación
                        .requestMatchers(
                                "/swagger-ui", "/swagger-ui.html", "/swagger-ui/**", 
                                "/v3/api-docs", "/v3/api-docs/**",
                                "/swagger-resources", "/swagger-resources/**", 
                                "/webjars/**"
                        ).permitAll()
                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated())
                // Agregar filtro JWT antes del filtro de autenticación por defecto
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración CORS para permitir peticiones desde el frontend.
     * 
     * @return CorsConfigurationSource configurado
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader("Content-Type");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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

    /**
     * Bean para el gestor de autenticación.
     * 
     * @param authConfig Configuración de autenticación
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
