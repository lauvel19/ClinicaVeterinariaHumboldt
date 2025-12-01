package com.tuorg.veterinaria.common.events;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento de dominio que se dispara cuando una cita es cancelada.
 *
 * Utiliza el patrón Builder para facilitar la creación con múltiples parámetros
 * y mejorar la legibilidad del código.
 *
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Getter
public class CitaCancelledEvent extends ApplicationEvent {

    private final Long citaId;
    private final LocalDateTime fechaHora;
    private final String clienteEmail;
    private final String clienteTelefono;
    private final String clienteNombre;
    private final String pacienteNombre;
    private final String motivoCancelacion;

    /**
     * Constructor privado para forzar el uso del Builder.
     */
    private CitaCancelledEvent(Object source, CitaData citaData) {
        super(source);
        this.citaId = citaData.citaId;
        this.fechaHora = citaData.fechaHora;
        this.clienteEmail = citaData.clienteEmail;
        this.clienteTelefono = citaData.clienteTelefono;
        this.clienteNombre = citaData.clienteNombre;
        this.pacienteNombre = citaData.pacienteNombre;
        this.motivoCancelacion = citaData.motivoCancelacion;
    }

    /**
     * Crea un nuevo builder para construir el evento.
     *
     * @param source Fuente del evento (usualmente el servicio que lo publica)
     * @return Builder para configurar el evento
     */
    public static CitaDataBuilder builder(Object source) {
        return new CitaDataBuilder(source);
    }

    /**
     * DTO interno para encapsular los datos de la cita cancelada.
     */
    @Builder
    @Getter
    public static class CitaData {
        private final Long citaId;
        private final LocalDateTime fechaHora;
        private final String clienteEmail;
        private final String clienteTelefono;
        private final String clienteNombre;
        private final String pacienteNombre;
        private final String motivoCancelacion;
    }

    /**
     * Builder personalizado que incluye el source del ApplicationEvent.
     */
    public static class CitaDataBuilder {
        private final Object source;
        private Long citaId;
        private LocalDateTime fechaHora;
        private String clienteEmail;
        private String clienteTelefono;
        private String clienteNombre;
        private String pacienteNombre;
        private String motivoCancelacion;

        private CitaDataBuilder(Object source) {
            this.source = source;
        }

        public CitaDataBuilder citaId(Long citaId) {
            this.citaId = citaId;
            return this;
        }

        public CitaDataBuilder fechaHora(LocalDateTime fechaHora) {
            this.fechaHora = fechaHora;
            return this;
        }

        public CitaDataBuilder clienteEmail(String clienteEmail) {
            this.clienteEmail = clienteEmail;
            return this;
        }

        public CitaDataBuilder clienteTelefono(String clienteTelefono) {
            this.clienteTelefono = clienteTelefono;
            return this;
        }

        public CitaDataBuilder clienteNombre(String clienteNombre) {
            this.clienteNombre = clienteNombre;
            return this;
        }

        public CitaDataBuilder pacienteNombre(String pacienteNombre) {
            this.pacienteNombre = pacienteNombre;
            return this;
        }

        public CitaDataBuilder motivoCancelacion(String motivoCancelacion) {
            this.motivoCancelacion = motivoCancelacion;
            return this;
        }

        public CitaCancelledEvent build() {
            CitaData citaData = CitaData.builder()
                    .citaId(this.citaId)
                    .fechaHora(this.fechaHora)
                    .clienteEmail(this.clienteEmail)
                    .clienteTelefono(this.clienteTelefono)
                    .clienteNombre(this.clienteNombre)
                    .pacienteNombre(this.pacienteNombre)
                    .motivoCancelacion(this.motivoCancelacion)
                    .build();

            return new CitaCancelledEvent(source, citaData);
        }
    }
}