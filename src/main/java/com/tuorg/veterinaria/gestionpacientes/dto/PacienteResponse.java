package com.tuorg.veterinaria.gestionpacientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta con la información de un paciente")
public class PacienteResponse {

    @Schema(description = "Identificador del paciente", example = "1")
    private Long id;

    @Schema(description = "Nombre del paciente", example = "Firulais")
    private String nombre;

    @Schema(description = "Especie", example = "perro")
    private String especie;

    @Schema(description = "Raza", example = "Labrador")
    private String raza;

    @Schema(description = "Fecha de nacimiento", example = "2020-05-15")
    private LocalDate fechaNacimiento;

    @Schema(description = "Sexo", example = "Macho")
    private String sexo;

    @Schema(description = "Peso en kilogramos", example = "25.6")
    private BigDecimal pesoKg;

    @Schema(description = "Estado de salud", example = "Estable")
    private String estadoSalud;

    @Schema(description = "URL de la foto de perfil", example = "/uploads/pacientes/paciente_1_abc123.jpg")
    private String fotoPerfil;

    @Schema(description = "Cliente dueño")
    private PacienteOwnerResponse cliente;

    @Schema(description = "Identificador externo", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID identificadorExterno;
}


