package com.tuorg.veterinaria.gestionpacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "Request para actualizar los datos de un paciente")
public class PacienteUpdateRequest {

    @Schema(description = "Nombre del paciente", example = "Max")
    private String nombre;

    @Schema(description = "Especie del paciente", allowableValues = {"perro", "gato"}, example = "gato")
    private String especie;

    @Schema(description = "Raza del paciente", example = "Criollo")
    private String raza;

    @Schema(description = "Fecha de nacimiento", example = "2022-08-01")
    private LocalDate fechaNacimiento;

    @Schema(description = "Sexo del paciente", example = "Hembra")
    private String sexo;

    @Positive(message = "El peso debe ser mayor a cero")
    @Schema(description = "Peso en kilogramos", example = "4.2")
    private BigDecimal pesoKg;

    @Schema(description = "Estado de salud", example = "Recuperación")
    private String estadoSalud;

    @Schema(description = "URL o path de la foto de perfil", example = "/uploads/pacientes/paciente_1_abc123.jpg")
    private String fotoPerfil;

    @Schema(description = "Identificador del cliente dueño", example = "4")
    private Long clienteId;

    @Schema(description = "Identificador externo opcional", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID identificadorExterno;
}


