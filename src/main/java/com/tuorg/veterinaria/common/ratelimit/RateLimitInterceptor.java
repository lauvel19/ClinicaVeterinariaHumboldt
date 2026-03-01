package com.tuorg.veterinaria.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para aplicar rate limiting a endpoints específicos.
 * 
 * Verifica si la IP del cliente ha excedido el límite de peticiones
 * permitidas y retorna 429 Too Many Requests si es necesario.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitService rateLimitService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler)
            throws Exception {

        String clientIp = getClientIP(request);

        if (!rateLimitService.tryConsume(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\": false, \"message\": \"Demasiadas peticiones. Por favor, espere un momento antes de intentar nuevamente.\"}");
            return false;
        }

        return true;
    }

    /**
     * Extrae la dirección IP real del cliente considerando proxies y balanceadores.
     * 
     * @param request Petición HTTP
     * @return Dirección IP del cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
