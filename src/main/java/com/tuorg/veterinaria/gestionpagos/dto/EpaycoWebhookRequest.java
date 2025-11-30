package com.tuorg.veterinaria.gestionpagos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * DTO para recibir el webhook de confirmación de ePayco.
 * 
 * ePayco envía esta información cuando el pago es procesado.
 * Documentación: https://docs.epayco.co/tools/webhooks
 */
@Data
@Schema(name = "EpaycoWebhookRequest", description = "Datos enviados por ePayco en el webhook de confirmación")
public class EpaycoWebhookRequest {

    @Schema(description = "Referencia única de la transacción")
    private String x_ref_payco;

    @Schema(description = "ID de la transacción en ePayco")
    private String x_id_invoice;

    @Schema(description = "Nuestra referencia enviada a ePayco")
    private String x_extra1;

    @Schema(description = "Monto de la transacción")
    private String x_amount;

    @Schema(description = "Moneda")
    private String x_currency_code;

    @Schema(description = "Estado: Aceptada, Rechazada, Pendiente, Fallida")
    private String x_transaction_state;

    @Schema(description = "Código de autorización")
    private String x_approval_code;

    @Schema(description = "Código de respuesta CUS")
    private String x_cod_response;

    @Schema(description = "Código de respuesta de la entidad financiera")
    private String x_response;

    @Schema(description = "Mensaje descriptivo del resultado")
    private String x_response_reason_text;

    @Schema(description = "Fecha de la transacción")
    private String x_transaction_date;

    @Schema(description = "Método de pago usado")
    private String x_transaction_type;

    @Schema(description = "Banco (para PSE)")
    private String x_bank_name;

    @Schema(description = "Nombre del pagador")
    private String x_customer_name;

    @Schema(description = "Email del pagador")
    private String x_customer_email;

    @Schema(description = "Teléfono del pagador")
    private String x_customer_phone;

    @Schema(description = "Firma para validar integridad")
    private String x_signature;

    @Schema(description = "Indica si es transacción de prueba")
    private String x_test_request;
}
