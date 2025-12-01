package com.tuorg.veterinaria.common.event;

import com.tuorg.veterinaria.reportes.model.Reporte;
import org.springframework.context.ApplicationEvent;

/**
 * Evento que se publica cuando se genera un reporte.
 *
 * Implementa el patrón Observer para notificar a los listeners
 * cuando se genera un reporte en el sistema.
 *
 * @author Equipo de Desarrollo
 * @version 1.0.1
 */
public class ReporteGeneradoEvent extends ApplicationEvent {

    // Se marca como transient porque ApplicationEvent es Serializable
    private final transient Reporte reporte;
    private final String tipoReporte;

    /**
     * Constructor del evento.
     *
     * @param source Fuente del evento (normalmente el servicio que lo publica)
     * @param reporte Reporte generado
     * @param tipoReporte Tipo de reporte generado
     */
    public ReporteGeneradoEvent(Object source, Reporte reporte, String tipoReporte) {
        super(source);
        this.reporte = reporte;
        this.tipoReporte = tipoReporte;
    }

    public Reporte getReporte() {
        return reporte;
    }

    public String getTipoReporte() {
        return tipoReporte;
    }
}
