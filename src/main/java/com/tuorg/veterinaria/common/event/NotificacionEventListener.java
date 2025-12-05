package com.tuorg.veterinaria.common.event;

// TODO: Reactivar cuando se implemente LogSistemaService
// import com.tuorg.veterinaria.configuracion.service.LogSistemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener para eventos de notificaciones (Observer pattern).
 * 
 * Escucha eventos de notificaciones y realiza acciones automáticas
 * como logging, auditoría, etc.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Component
public class NotificacionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEventListener.class);

    // TODO: Reactivar cuando se implemente LogSistemaService
    // private final LogSistemaService logSistemaService;

    @Autowired
    public NotificacionEventListener(/* LogSistemaService logSistemaService */) {
        // this.logSistemaService = logSistemaService;
    }

    /**
     * Escucha eventos de notificaciones enviadas.
     * 
     * @param event Evento de notificación
     */
    @EventListener
    @Async
    public void handleNotificacionEnviada(NotificacionEvent event) {
        logger.info("Evento de notificación recibido: {} - ID: {}", 
                event.getTipoEvento(), event.getNotificacion().getIdNotificacion());
        
        // TODO: Reactivar cuando se implemente LogSistemaService
        // Registrar en log del sistema
        /* logSistemaService.registrarEvento(
                "NotificacionService",
                "INFO",
                String.format("Notificación %s: %s (ID: %d)",
                        event.getTipoEvento(),
                        event.getNotificacion().getMensaje(),
                        event.getNotificacion().getIdNotificacion())
        ); */
        
        // Aquí se pueden agregar más acciones automáticas:
        // - Enviar notificaciones a administradores
        // - Actualizar métricas
        // - Trigger de alertas si hay muchas fallidas
    }
}

