package com.tuorg.veterinaria.gestionfacturacion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.gestionfacturacion.dto.FacturaPagoRequest;
import com.tuorg.veterinaria.gestionfacturacion.dto.FacturaRequest;
import com.tuorg.veterinaria.gestionfacturacion.dto.FacturaResponse;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
/**
 * Servicio para la gestión de facturas.
 *
 * Implementa el patrón Factory/Builder para encapsular la lógica de creación
 * y expone DTOs para separar la capa de exposición de las entidades JPA.
 */
@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final FacturaPDFService facturaPDFService;

    @Autowired
    public FacturaService(FacturaRepository facturaRepository,
                          UsuarioRepository usuarioRepository,
                          ObjectMapper objectMapper,
                          FacturaPDFService facturaPDFService) {
        this.facturaRepository = facturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
        this.facturaPDFService = facturaPDFService;
    }

    /**
     * Crea una nueva factura con validaciones de negocio.
     */
    @Transactional
    public FacturaResponse crear(FacturaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getClienteId()));

        if (!(usuario instanceof Cliente cliente)) {
            throw new BusinessException("El identificador proporcionado no corresponde a un cliente registrado");
        }

        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setTotal(request.getTotal());
        factura.setFormaPago(request.getFormaPago());
        factura.setContenido(asJsonString(request.getContenido()));

        // Generar número único
        String numeroFactura = generarNumeroFactura();
        while (facturaRepository.findByNumero(numeroFactura).isPresent()) {
            numeroFactura = generarNumeroFactura();
        }
        factura.setNumero(numeroFactura);
        factura.setNumeroFactura(numeroFactura); // Legacy column, mantener sincronizado

        if (factura.getTotal() == null || factura.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El total de la factura debe ser mayor o igual a cero");
        }

        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(AppConstants.ESTADO_FACTURA_PENDIENTE);

        Factura guardada = facturaRepository.save(factura);
        return mapToResponse(guardada);
    }

    private static final SecureRandom secureRandom = new SecureRandom();

    private String generarNumeroFactura() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String secuencia = String.format("%04d", secureRandom.nextInt(10000));
        return "FACT-" + fecha + "-" + secuencia;
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtener(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));
        return mapToResponse(factura);
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> obtenerTodas() {
        return facturaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> obtenerPorCliente(Long clienteId) {
        return facturaRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    /**
     * Genera un PDF para la factura especificada
     * 
     * @param facturaId ID de la factura
     * @return Array de bytes con el PDF generado
     * @throws ResourceNotFoundException Si la factura no existe
     * @throws BusinessException Si ocurre un error en la generación
     */
    public byte[] generarPDF(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", facturaId));
        
        return facturaPDFService.generarPDFFactura(factura);
    }

    @Transactional
    public FacturaResponse anular(Long facturaId) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", facturaId));

        if (AppConstants.ESTADO_FACTURA_ANULADA.equals(factura.getEstado())) {
            throw new BusinessException("La factura ya está anulada");
        }

        if (AppConstants.ESTADO_FACTURA_PAGADA.equals(factura.getEstado())) {
            throw new BusinessException("No se puede anular una factura ya pagada");
        }

        factura.setEstado(AppConstants.ESTADO_FACTURA_ANULADA);
        Factura anulada = facturaRepository.save(factura);
        return mapToResponse(anulada);
    }

    @Transactional
    public FacturaResponse registrarPago(Long facturaId, FacturaPagoRequest request) {
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", facturaId));

        if (!AppConstants.ESTADO_FACTURA_PENDIENTE.equals(factura.getEstado())) {
            throw new BusinessException("Solo se pueden pagar facturas en estado PENDIENTE");
        }

        // Validar que el monto pagado coincida con el total de la factura
        if (request.getMontoPagado().compareTo(factura.getTotal()) != 0) {
            throw new BusinessException(
                String.format("El monto pagado (%s) no coincide con el total de la factura (%s)",
                    request.getMontoPagado(), factura.getTotal())
            );
        }

        factura.setEstado(AppConstants.ESTADO_FACTURA_PAGADA);
        factura.setFormaPago(request.getFormaPago());
        factura.setFechaPago(LocalDateTime.now());

        Factura pagada = facturaRepository.save(factura);
        return mapToResponse(pagada);
    }

    private FacturaResponse mapToResponse(Factura factura) {
        Cliente cliente = factura.getCliente();
        return FacturaResponse.builder()
                .idFactura(factura.getIdFactura())
                .numero(factura.getNumero())
                .fechaEmision(factura.getFechaEmision())
                .fechaPago(factura.getFechaPago())
                .total(factura.getTotal())
                .formaPago(factura.getFormaPago())
                .estado(factura.getEstado())
                .contenido(asMap(factura.getContenido()))
                .cliente(cliente != null ? FacturaResponse.ClienteSummary.builder()
                        .id(cliente.getIdUsuario())
                        .nombreCompleto(cliente.getNombre() + " " + cliente.getApellido())
                        .correo(cliente.getCorreo())
                        .telefono(cliente.getTelefono())
                        .build() : null)
                .build();
    }

    private String asJsonString(Map<String, Object> contenido) {
        if (contenido == null || contenido.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(contenido);
        } catch (JsonProcessingException e) {
            throw new BusinessException("El contenido de la factura no tiene un formato JSON válido");
        }
    }

    private Map<String, Object> asMap(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(contenido, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}

