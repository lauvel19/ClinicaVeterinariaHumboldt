package com.tuorg.veterinaria.gestionpagos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.config.EpaycoConfig;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import com.tuorg.veterinaria.gestionpagos.dto.EpaycoWebhookRequest;
import com.tuorg.veterinaria.gestionpagos.dto.PagoOnlineRequest;
import com.tuorg.veterinaria.gestionpagos.dto.PagoOnlineResponse;
import com.tuorg.veterinaria.gestionpagos.model.PagoOnline;
import com.tuorg.veterinaria.gestionpagos.repository.PagoOnlineRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para integración con ePayco.
 * 
 * Maneja la creación de transacciones de pago online,
 * generación de URLs de pago y procesamiento de webhooks.
 * 
 * Documentación ePayco: https://docs.epayco.co
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Slf4j
@Service
public class EpaycoService {

    private final EpaycoConfig epaycoConfig;
    private final PagoOnlineRepository pagoOnlineRepository;
    private final FacturaRepository facturaRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    @Autowired
    public EpaycoService(EpaycoConfig epaycoConfig,
                        PagoOnlineRepository pagoOnlineRepository,
                        FacturaRepository facturaRepository,
                        ObjectMapper objectMapper) {
        this.epaycoConfig = epaycoConfig;
        this.pagoOnlineRepository = pagoOnlineRepository;
        this.facturaRepository = facturaRepository;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * Inicia un pago online para una factura.
     * 
     * Crea un registro de pago, genera una referencia única
     * y construye la URL para redirigir al cliente a ePayco.
     * 
     * @param facturaId ID de la factura a pagar
     * @param request Datos del pagador
     * @param ipCliente IP del cliente
     * @return Información del pago con URL de redirección
     */
    @Transactional
    public PagoOnlineResponse iniciarPago(Long facturaId, PagoOnlineRequest request, String ipCliente) {
        log.info("Iniciando pago online para factura ID: {}", facturaId);

        // Validar factura
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", facturaId));

        if (!AppConstants.ESTADO_FACTURA_PENDIENTE.equals(factura.getEstado())) {
            throw new BusinessException("Solo se pueden pagar facturas en estado PENDIENTE");
        }

        // Verificar si ya existe un pago aprobado
        if (pagoOnlineRepository.existePagoAprobadoParaFactura(facturaId)) {
            throw new BusinessException("La factura ya tiene un pago aprobado");
        }

        // Generar referencia única
        String referencia = generarReferencia(facturaId);

        // Crear registro de pago
        PagoOnline pago = new PagoOnline();
        pago.setFactura(factura);
        pago.setReferenciaEpayco(referencia);
        pago.setMonto(factura.getTotal());
        pago.setMoneda(epaycoConfig.getCurrency());
        pago.setEstado("PENDIENTE");
        pago.setNombrePagador(request.getNombrePagador());
        pago.setEmailPagador(request.getEmailPagador());
        pago.setTelefonoPagador(request.getTelefonoPagador());
        pago.setIpCliente(ipCliente);
        pago.setFechaTransaccion(LocalDateTime.now());

        PagoOnline pagoGuardado = pagoOnlineRepository.save(pago);
        log.info("Pago online creado con ID: {} y referencia: {}", pagoGuardado.getIdPagoOnline(), referencia);

        // Construir URL de pago de ePayco
        String urlPago = construirUrlPago(pagoGuardado, factura);

        return PagoOnlineResponse.builder()
                .idPagoOnline(pagoGuardado.getIdPagoOnline())
                .facturaId(factura.getIdFactura())
                .numeroFactura(factura.getNumero())
                .referenciaEpayco(referencia)
                .monto(factura.getTotal())
                .moneda(epaycoConfig.getCurrency())
                .estado("PENDIENTE")
                .urlPago(urlPago)
                .fechaCreacion(pagoGuardado.getCreatedAt())
                .build();
    }

    /**
     * Procesa el webhook de confirmación de ePayco.
     * 
     * Valida la firma, actualiza el estado del pago y
     * si es aprobado, marca la factura como pagada.
     * 
     * @param webhook Datos del webhook
     */
    @Transactional
    public void procesarWebhook(EpaycoWebhookRequest webhook) {
        log.info("Procesando webhook de ePayco. Ref: {}, Estado: {}", 
                webhook.getX_ref_payco(), webhook.getX_transaction_state());

        // Validar firma para seguridad
        if (!validarFirma(webhook)) {
            log.error("Firma inválida en webhook de ePayco. Posible intento de fraude.");
            throw new BusinessException("Firma de webhook inválida");
        }

        // Buscar pago por referencia
        String referencia = webhook.getX_extra1(); // Nuestra referencia
        PagoOnline pago = pagoOnlineRepository.findByReferenciaEpayco(referencia)
                .orElseThrow(() -> new ResourceNotFoundException("PagoOnline", "referencia", referencia));

        // Actualizar información del pago
        pago.setRefPayco(webhook.getX_ref_payco());
        pago.setCodigoRespuesta(webhook.getX_cod_response());
        pago.setMensajeRespuesta(webhook.getX_response_reason_text());
        pago.setCodigoAutorizacion(webhook.getX_approval_code());
        pago.setReciboPago(webhook.getX_id_invoice());
        pago.setMetodoPago(webhook.getX_transaction_type());
        pago.setBanco(webhook.getX_bank_name());

        // Guardar metadata completa
        try {
            pago.setMetadataEpayco(objectMapper.writeValueAsString(webhook));
        } catch (Exception e) {
            log.warn("No se pudo serializar metadata de webhook", e);
        }

        // Actualizar estado según respuesta de ePayco
        String estadoEpayco = webhook.getX_transaction_state();
        switch (estadoEpayco.toUpperCase()) {
            case "ACEPTADA":
            case "APROBADA":
                pago.setEstado("APROBADA");
                pago.setFechaAprobacion(LocalDateTime.now());
                aprobarFactura(pago);
                log.info("Pago aprobado para factura ID: {}", pago.getFactura().getIdFactura());
                break;
            case "RECHAZADA":
                pago.setEstado("RECHAZADA");
                log.warn("Pago rechazado para factura ID: {}", pago.getFactura().getIdFactura());
                break;
            case "PENDIENTE":
                pago.setEstado("PROCESANDO");
                break;
            case "FALLIDA":
                pago.setEstado("FALLIDA");
                log.error("Pago fallido para factura ID: {}", pago.getFactura().getIdFactura());
                break;
            default:
                pago.setEstado("PROCESANDO");
                log.warn("Estado desconocido de ePayco: {}", estadoEpayco);
        }

        pagoOnlineRepository.save(pago);
        log.info("Webhook procesado exitosamente. Pago ID: {}, Estado: {}", 
                pago.getIdPagoOnline(), pago.getEstado());
    }

    /**
     * Consulta el estado de un pago en ePayco.
     * Útil para verificar pagos pendientes.
     * 
     * @param pagoId ID del pago a consultar
     */
    @Transactional
    public void consultarEstadoPago(Long pagoId) {
        PagoOnline pago = pagoOnlineRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("PagoOnline", "id", pagoId));

        if (pago.getRefPayco() == null) {
            log.warn("No se puede consultar pago sin ref_payco. Pago ID: {}", pagoId);
            return;
        }

        try {
            String url = epaycoConfig.getApiUrl() + "/validation/v1/reference/" + pago.getRefPayco();
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + epaycoConfig.getPrivateKey())
                    .build();

            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                log.info("Estado de pago consultado: {}", jsonResponse);
                
                // Aquí podrías actualizar el estado basado en la respuesta
                // Por ahora solo lo logueamos
            }
        } catch (IOException e) {
            log.error("Error al consultar estado de pago en ePayco", e);
        }
    }

    /**
     * Genera una referencia única para la transacción.
     * Formato: VET-YYYYMMDD-FACTURAID-RANDOM
     */
    private String generarReferencia(Long facturaId) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.valueOf((int)(Math.random() * 10000));
        return String.format("VET-%s-%d-%s", fecha, facturaId, random);
    }

    /**
     * Construye la URL de pago de ePayco con todos los parámetros.
     * 
     * ePayco usa el método de "Checkout Standard" donde el cliente
     * es redirigido a la pasarela con parámetros en la URL.
     */
    private String construirUrlPago(PagoOnline pago, Factura factura) {
        String baseUrl = epaycoConfig.getApiUrl() + "/checkout.php";
        
        Map<String, String> params = new HashMap<>();
        params.put("public-key", epaycoConfig.getPublicKey());
        params.put("description", "Pago Factura " + factura.getNumero());
        params.put("invoice", pago.getReferenciaEpayco());
        params.put("currency", epaycoConfig.getCurrency());
        params.put("amount", pago.getMonto().toString());
        params.put("tax_base", "0");
        params.put("tax", "0");
        params.put("country", epaycoConfig.getCountry());
        params.put("lang", epaycoConfig.getLang());
        params.put("external", "false");
        
        // Datos del cliente
        params.put("name_billing", pago.getNombrePagador());
        params.put("email_billing", pago.getEmailPagador());
        params.put("mobilephone_billing", pago.getTelefonoPagador());
        
        // URLs de respuesta
        params.put("confirmation", epaycoConfig.getConfirmationUrl());
        params.put("response", epaycoConfig.getResponseUrl());
        
        // Extra para identificar la transacción
        params.put("extra1", pago.getReferenciaEpayco());
        params.put("extra2", factura.getIdFactura().toString());
        
        // Modo de prueba
        params.put("test", epaycoConfig.isTestMode() ? "true" : "false");

        // Construir query string
        StringBuilder urlBuilder = new StringBuilder(baseUrl + "?");
        params.forEach((key, value) -> {
            urlBuilder.append(key).append("=").append(encodeValue(value)).append("&");
        });
        
        return urlBuilder.toString();
    }

    /**
     * Valida la firma del webhook para verificar que proviene de ePayco.
     * 
     * La firma se calcula con SHA-256 de: 
     * p_cust_id_cliente^p_key^x_ref_payco^x_transaction_id^x_amount^x_currency_code
     */
    private boolean validarFirma(EpaycoWebhookRequest webhook) {
        try {
            String datos = String.join("^",
                    epaycoConfig.getCustomerId(),
                    epaycoConfig.getPrivateKey(),
                    webhook.getX_ref_payco(),
                    webhook.getX_id_invoice(),
                    webhook.getX_amount(),
                    webhook.getX_currency_code()
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(datos.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            String firmaCalculada = hexString.toString();
            boolean valida = firmaCalculada.equals(webhook.getX_signature());
            
            if (!valida) {
                log.warn("Firma inválida. Calculada: {}, Recibida: {}", 
                        firmaCalculada, webhook.getX_signature());
            }
            
            return valida;
        } catch (NoSuchAlgorithmException e) {
            log.error("Error al validar firma de webhook", e);
            return false;
        }
    }

    /**
     * Aprueba la factura cuando el pago es confirmado.
     */
    private void aprobarFactura(PagoOnline pago) {
        Factura factura = pago.getFactura();
        factura.setEstado(AppConstants.ESTADO_FACTURA_PAGADA);
        factura.setFormaPago("PAGO_ONLINE_EPAYCO");
        factura.setFechaPago(LocalDateTime.now());
        facturaRepository.save(factura);
        
        log.info("Factura {} marcada como PAGADA", factura.getNumero());
    }

    /**
     * Codifica valores para URLs.
     */
    private String encodeValue(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * Obtiene información de un pago por su ID.
     */
    @Transactional(readOnly = true)
    public PagoOnlineResponse obtenerPago(Long pagoId) {
        PagoOnline pago = pagoOnlineRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("PagoOnline", "id", pagoId));

        return mapToResponse(pago);
    }

    /**
     * Obtiene todos los pagos de una factura.
     */
    @Transactional(readOnly = true)
    public java.util.List<PagoOnlineResponse> obtenerPagosPorFactura(Long facturaId) {
        return pagoOnlineRepository.findByFacturaId(facturaId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PagoOnlineResponse mapToResponse(PagoOnline pago) {
        return PagoOnlineResponse.builder()
                .idPagoOnline(pago.getIdPagoOnline())
                .facturaId(pago.getFactura().getIdFactura())
                .numeroFactura(pago.getFactura().getNumero())
                .referenciaEpayco(pago.getReferenciaEpayco())
                .monto(pago.getMonto())
                .moneda(pago.getMoneda())
                .estado(pago.getEstado())
                .urlPago(null) // Ya no es necesaria después de creada
                .fechaCreacion(pago.getCreatedAt())
                .build();
    }
}
