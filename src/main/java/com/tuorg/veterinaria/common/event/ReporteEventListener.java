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
 * Listener para eventos de reportes (Observer pattern).
 * 
 * Escucha eventos de generación de reportes y realiza acciones automáticas
 * como logging, auditoría, notificaciones, etc.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Component
public class ReporteEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ReporteEventListener.class);

    // TODO: Reactivar cuando se implemente LogSistemaService
    // private final LogSistemaService logSistemaService;

    @Autowired
    public ReporteEventListener(/* LogSistemaService logSistemaService */) {
        // this.logSistemaService = logSistemaService;
    }

    /**
     * Escucha eventos de reportes generados.
     * 
     * @param event Evento de reporte generado
     */
    @EventListener
    @Async
    public void handleReporteGenerado(ReporteGeneradoEvent event) {
        logger.info("Evento de reporte generado recibido: {} - ID: {}", 
                event.getTipoReporte(), event.getReporte().getIdReporte());
        
        // TODO: Reactivar cuando se implemente LogSistemaService
        // Registrar en log del sistema
        /* logSistemaService.registrarEvento(
                "ReporteService",
                "INFO",
                String.format("Reporte generado: %s (Tipo: %s, ID: %d, Generado por: %s)",
                        event.getReporte().getNombre(),
                        event.getTipoReporte(),
                        event.getReporte().getIdReporte(),
                        event.getReporte().getGeneradoPor())
        ); */
        
        // Aquí se pueden agregar más acciones automáticas:
        // - Enviar notificación al usuario que generó el reporte
        // - Actualizar métricas de reportes generados
        // - Archivar reportes antiguos
        // - Enviar reportes automáticos por email
    }
}

