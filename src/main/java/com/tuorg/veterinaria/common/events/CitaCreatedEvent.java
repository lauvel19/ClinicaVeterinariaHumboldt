package com.tuorg.veterinaria.common.events;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class CitaCreatedEvent extends ApplicationEvent {

    private final Long citaId;
    private final Long pacienteId;
    private final Long veterinarioId;
    private final LocalDateTime fechaHora;
    private final String clienteEmail;
    private final String clienteTelefono;
    private final String clienteNombre;
    private final String pacienteNombre;
    private final String veterinarioNombre;

    @Builder
    public CitaCreatedEvent(Object source,
                            Long citaId,
                            Long pacienteId,
                            Long veterinarioId,
                            LocalDateTime fechaHora,
                            String clienteEmail,
                            String clienteTelefono,
                            String clienteNombre,
                            String pacienteNombre,
                            String veterinarioNombre) {
        super(source);
        this.citaId = citaId;
        this.pacienteId = pacienteId;
        this.veterinarioId = veterinarioId;
        this.fechaHora = fechaHora;
        this.clienteEmail = clienteEmail;
        this.clienteTelefono = clienteTelefono;
        this.clienteNombre = clienteNombre;
        this.pacienteNombre = pacienteNombre;
        this.veterinarioNombre = veterinarioNombre;
    }
}
