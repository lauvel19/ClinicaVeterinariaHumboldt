package com.tuorg.veterinaria.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT para autenticación en cada petición.
 * 
 * Este filtro intercepta todas las peticiones HTTP y valida el token JWT
 * presente en el header Authorization. Si el token es válido, establece
 * la autenticación en el contexto de seguridad de Spring.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Proveedor de tokens JWT.
     */
    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Servicio para cargar detalles del usuario.
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Prefijo del token en el header Authorization.
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Nombre del header que contiene el token.
     */
    private static final String HEADER_STRING = "Authorization";

    /**
     * Filtra cada petición para validar el token JWT.
     * 
     * @param request Petición HTTP
     * @param response Respuesta HTTP
     * @param filterChain Cadena de filtros
     * @throws ServletException Si hay error en el servlet
     * @throws IOException Si hay error de I/O
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Ignorar rutas públicas (login, registro, etc.)
        // El context-path es /api, así que las rutas aquí son relativas a ese path
        String servletPath = request.getServletPath(); // Esto devuelve la ruta sin el context-path
        
        // Verificar si es una ruta pública (sin /api porque el context-path ya lo incluye)
        if (servletPath.startsWith("/auth/") || 
            servletPath.startsWith("/configuracion/parametros/") ||
            servletPath.equals("/health") ||
            servletPath.equals("/ping") ||
            servletPath.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (tokenProvider.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("No se pudo establecer la autenticación del usuario: " + ex.getMessage(), ex);
            // No lanzar excepción, permitir que continúe el filtro para rutas públicas
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * 
     * @param request Petición HTTP
     * @return Token JWT o null si no está presente
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_STRING);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
