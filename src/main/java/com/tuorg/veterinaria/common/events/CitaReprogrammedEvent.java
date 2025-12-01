package com.tuorg.veterinaria.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class CitaReprogrammedEvent extends ApplicationEvent {

    private final Long citaId;
    private final LocalDateTime fechaAnterior;
    private final LocalDateTime fechaNueva;
    private final String clienteEmail;
    private final String clienteTelefono;
    private final String clienteNombre;
    private final String pacienteNombre;
    private final String veterinarioNombre;

    private CitaReprogrammedEvent(Builder builder) {
        super(builder.source);
        this.citaId = builder.citaId;
        this.fechaAnterior = builder.fechaAnterior;
        this.fechaNueva = builder.fechaNueva;
        this.clienteEmail = builder.clienteEmail;
        this.clienteTelefono = builder.clienteTelefono;
        this.clienteNombre = builder.clienteNombre;
        this.pacienteNombre = builder.pacienteNombre;
        this.veterinarioNombre = builder.veterinarioNombre;
    }

    public static class Builder {
        private final Object source;
        private Long citaId;
        private LocalDateTime fechaAnterior;
        private LocalDateTime fechaNueva;
        private String clienteEmail;
        private String clienteTelefono;
        private String clienteNombre;
        private String pacienteNombre;
        private String veterinarioNombre;

        public Builder(Object source) { this.source = source; }

        public Builder citaId(Long citaId) { this.citaId = citaId; return this; }
        public Builder fechaAnterior(LocalDateTime fechaAnterior) { this.fechaAnterior = fechaAnterior; return this; }
        public Builder fechaNueva(LocalDateTime fechaNueva) { this.fechaNueva = fechaNueva; return this; }
        public Builder clienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; return this; }
        public Builder clienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; return this; }
        public Builder clienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; return this; }
        public Builder pacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; return this; }
        public Builder veterinarioNombre(String veterinarioNombre) { this.veterinarioNombre = veterinarioNombre; return this; }

        public CitaReprogrammedEvent build() { return new CitaReprogrammedEvent(this); }
    }
}
