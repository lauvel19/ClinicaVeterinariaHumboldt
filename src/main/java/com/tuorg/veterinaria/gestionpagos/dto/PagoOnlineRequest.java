package com.tuorg.veterinaria.gestionpagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para solicitar inicio de pago online.
 */
@Data
@Schema(name = "PagoOnlineRequest", description = "Datos del cliente para iniciar pago online con ePayco")
public class PagoOnlineRequest {

    @NotBlank(message = "El nombre del pagador es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    @Schema(description = "Nombre completo del pagador", example = "Juan Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombrePagador;

    @NotBlank(message = "El email del pagador es obligatorio")
    @Email(message = "Debe ser un email válido")
    @Schema(description = "Email del pagador", example = "juan.perez@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String emailPagador;

    @NotBlank(message = "El teléfono del pagador es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Teléfono inválido (10-15 dígitos)")
    @Schema(description = "Teléfono del pagador", example = "+573001234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String telefonoPagador;
}
