package com.tuorg.veterinaria.gestionpagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para pago online iniciado.
 */
@Data
@Builder
@Schema(name = "PagoOnlineResponse", description = "Información de un pago online iniciado en ePayco")
public class PagoOnlineResponse {

    @Schema(description = "ID del pago online", example = "45")
    private Long idPagoOnline;

    @Schema(description = "ID de la factura asociada", example = "123")
    private Long facturaId;

    @Schema(description = "Número de la factura", example = "FACT-20251129-0042")
    private String numeroFactura;

    @Schema(description = "Referencia única de la transacción en ePayco", example = "VET-20251129-123-5678")
    private String referenciaEpayco;

    @Schema(description = "Monto a pagar", example = "145000.50")
    private BigDecimal monto;

    @Schema(description = "Moneda", example = "COP")
    private String moneda;

    @Schema(description = "Estado del pago", example = "PENDIENTE")
    private String estado;

    @Schema(description = "URL para redirigir al cliente a ePayco")
    private String urlPago;

    @Schema(description = "Fecha de creación del pago")
    private LocalDateTime fechaCreacion;
}
