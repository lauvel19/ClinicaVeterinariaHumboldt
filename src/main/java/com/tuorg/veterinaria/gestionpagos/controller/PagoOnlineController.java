package com.tuorg.veterinaria.gestionpagos.controller;

import com.tuorg.veterinaria.gestionpagos.dto.EpaycoWebhookRequest;
import com.tuorg.veterinaria.gestionpagos.dto.PagoOnlineRequest;
import com.tuorg.veterinaria.gestionpagos.dto.PagoOnlineResponse;
import com.tuorg.veterinaria.gestionpagos.service.EpaycoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para pagos online con ePayco.
 * 
 * Endpoints:
 * - POST /facturas/{id}/iniciar-pago-online: Inicia un pago
 * - POST /pagos/webhook/epayco: Recibe confirmación de ePayco (público)
 * - GET /pagos-online/{id}: Consulta un pago
 * - GET /facturas/{id}/pagos-online: Lista pagos de una factura
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping
@Tag(name = "Pagos Online", description = "Gestión de pagos online con ePayco")
public class PagoOnlineController {

    private final EpaycoService epaycoService;

    @Autowired
    public PagoOnlineController(EpaycoService epaycoService) {
        this.epaycoService = epaycoService;
    }

    /**
     * Inicia un pago online para una factura.
     * El cliente es redirigido a ePayco para completar el pago.
     */
    @PostMapping("/facturas/{facturaId}/iniciar-pago-online")
    @PreAuthorize("hasAnyRole('CLIENTE', 'SECRETARIO', 'ADMIN')")
    @Operation(summary = "Iniciar pago online", 
               description = "Genera una transacción de pago y retorna la URL de ePayco")
    public ResponseEntity<PagoOnlineResponse> iniciarPago(
            @Parameter(description = "ID de la factura a pagar") 
            @PathVariable Long facturaId,
            @Valid @RequestBody PagoOnlineRequest request,
            HttpServletRequest httpRequest) {
        
        String ipCliente = obtenerIpCliente(httpRequest);
        log.info("Iniciando pago online para factura {} desde IP {}", facturaId, ipCliente);
        
        PagoOnlineResponse response = epaycoService.iniciarPago(facturaId, request, ipCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Webhook de confirmación de ePayco.
     * ePayco llama a este endpoint cuando el pago es procesado.
     * 
     * IMPORTANTE: Este endpoint debe ser accesible públicamente (sin autenticación)
     * y la URL debe ser accesible desde internet (usar ngrok en desarrollo).
     */
    @PostMapping("/pagos/webhook/epayco")
    @Operation(summary = "Webhook de confirmación de ePayco", 
               description = "Endpoint público para recibir notificaciones de ePayco")
    public ResponseEntity<String> webhookEpayco(@RequestBody EpaycoWebhookRequest webhook) {
        log.info("Webhook recibido de ePayco. Ref: {}, Estado: {}", 
                webhook.getX_ref_payco(), webhook.getX_transaction_state());
        
        try {
            epaycoService.procesarWebhook(webhook);
            return ResponseEntity.ok("Webhook procesado exitosamente");
        } catch (Exception e) {
            log.error("Error procesando webhook de ePayco", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando webhook: " + e.getMessage());
        }
    }

    /**
     * Consulta información de un pago online.
     */
    @GetMapping("/pagos-online/{pagoId}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'VETERINARIO', 'SECRETARIO', 'ADMIN')")
    @Operation(summary = "Consultar pago online", 
               description = "Obtiene la información de un pago online por su ID")
    public ResponseEntity<PagoOnlineResponse> obtenerPago(
            @Parameter(description = "ID del pago online") 
            @PathVariable Long pagoId) {
        
        PagoOnlineResponse response = epaycoService.obtenerPago(pagoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los pagos online de una factura.
     */
    @GetMapping("/facturas/{facturaId}/pagos-online")
    @PreAuthorize("hasAnyRole('CLIENTE', 'VETERINARIO', 'SECRETARIO', 'ADMIN')")
    @Operation(summary = "Listar pagos de una factura", 
               description = "Obtiene todos los intentos de pago online de una factura")
    public ResponseEntity<List<PagoOnlineResponse>> obtenerPagosPorFactura(
            @Parameter(description = "ID de la factura") 
            @PathVariable Long facturaId) {
        
        List<PagoOnlineResponse> pagos = epaycoService.obtenerPagosPorFactura(facturaId);
        return ResponseEntity.ok(pagos);
    }

    /**
     * Consulta el estado actual de un pago en ePayco.
     * Útil para verificar pagos pendientes.
     */
    @PostMapping("/pagos-online/{pagoId}/consultar-estado")
    @PreAuthorize("hasAnyRole('SECRETARIO', 'ADMIN')")
    @Operation(summary = "Consultar estado en ePayco", 
               description = "Consulta el estado actual de un pago directamente en ePayco")
    public ResponseEntity<String> consultarEstado(
            @Parameter(description = "ID del pago online") 
            @PathVariable Long pagoId) {
        
        epaycoService.consultarEstadoPago(pagoId);
        return ResponseEntity.ok("Consulta realizada. Revise los logs del servidor.");
    }

    /**
     * Obtiene la IP real del cliente considerando proxies y load balancers.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs, tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
