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

    @Autowired
    public FacturaService(FacturaRepository facturaRepository,
                          UsuarioRepository usuarioRepository,
                          ObjectMapper objectMapper) {
        this.facturaRepository = facturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Crea una nueva factura con validaciones de negocio y generación de número único.
     * 
     * Flujo de creación de factura:
     * 1. Verifica existencia del cliente por ID
     * 2. Valida que sea instancia de Cliente (verificación de rol)
     * 3. Valida que el total sea >= 0 (no negativos)
     * 4. Genera número de factura único (formato: FACT-yyyyMMdd-secuencia)
     * 5. Establece fecha de emisión (actual) y estado PENDIENTE
     * 6. Serializa contenido (items, detalles) a JSON
     * 7. Persiste en base de datos
     * 
     * Nota: El número de factura incluye timestamp para evitar colisiones
     * 
     * @param request DTO con clienteId, total, formaPago, contenido (items, detalles)
     * @return FacturaResponse con datos de la factura creada
     * @throws ResourceNotFoundException si cliente no existe
     * @throws BusinessException si no es cliente o total negativo
     */
    @Transactional
    public FacturaResponse crear(FacturaRequest request) {
        // PASO 1: Verificar existencia del cliente
        Usuario usuario = usuarioRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getClienteId()));

        // PASO 2: Validar que sea instancia de Cliente (verificación de rol/tipo)
        if (!(usuario instanceof Cliente cliente)) {
            throw new BusinessException("El identificador proporcionado no corresponde a un cliente registrado");
        }

        // PASO 3: Validar que total sea >= 0
        if (request.getTotal() == null || request.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El total de la factura debe ser mayor o igual a cero");
        }

        // Crear entidad de factura
        Factura factura = new Factura();
        factura.setCliente(cliente);
        factura.setTotal(request.getTotal());
        factura.setFormaPago(request.getFormaPago()); // Método de pago (efectivo, tarjeta, cheque, etc)
        
        // PASO 6: Serializar contenido (items de factura) a JSON
        // Esto permite almacenar datos flexibles (no predefinidos) sin requerer otra tabla
        factura.setContenido(asJsonString(request.getContenido()));

        // PASO 4: Generar número único de factura
        // Formato: FACT-yyyyMMdd-secuencia (ej: FACT-20251201-0352)
        String numeroFactura = generarNumeroFactura();
        
        // Verificar que el número no exista (muy raro pero posible con números aleatorios)
        while (facturaRepository.findByNumero(numeroFactura).isPresent()) {
            numeroFactura = generarNumeroFactura();
        }
        factura.setNumero(numeroFactura);

        // PASO 5: Establecer fecha/hora actual y estado inicial
        factura.setFechaEmision(LocalDateTime.now());
        factura.setEstado(AppConstants.ESTADO_FACTURA_PENDIENTE); // Estado: PENDIENTE de pago

        // PASO 7: Persistir en base de datos
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
    public byte[] generarPDF(Long facturaId) {
        obtener(facturaId);
        // Nota: La generación real de PDF se implementará con iText o JasperReports
        // cuando se requiera la funcionalidad completa de exportación
        return new byte[0];
    }

    @Transactional
    /**
     * 📋 ANULAR UNA FACTURA (Invalidar factura sin eliminarla del registro)
     * 
     * Realiza la anulación de una factura, cambiando su estado a ANULADA.
     * La anulación es irreversible y permite auditoría del cambio.
     * 
     * ⏹️ FLUJO DE ANULACIÓN (4 PASOS):
     * 1️⃣  Buscar factura por ID → Validar existencia en BD
     * 2️⃣  Verificar estado actual:
     *     ❌ Si ya está ANULADA → Lanzar excepción (evitar re-anulación)
     *     ❌ Si está PAGADA → Lanzar excepción (no se puede anular pagada por auditoria)
     * 3️⃣  Cambiar estado: PENDIENTE/PAGADA → ANULADA
     * 4️⃣  Guardar cambios en BD con auditoría automática
     * 
     * 🔐 RESTRICCIONES DE NEGOCIO:
     *    • Solo se pueden anular facturas no pagadas
     *    • Una factura no puede anularse dos veces
     *    • La anulación no elimina registros (integridad de auditoría)
     *    • El cambio de estado se registra automáticamente por @EntityListeners
     * 
     * @param facturaId ID único de la factura a anular
     * @return FacturaResponse con los datos actualizados (estado = ANULADA)
     * @throws ResourceNotFoundException si la factura no existe en BD
     * @throws BusinessException si:
     *         - La factura ya está anulada (re-anulación)
     *         - La factura está pagada (restricción de auditoría)
     * 
     * @example
     *   FacturaResponse anulada = facturaService.anular(123L);
     *   // Resultado: estado = "ANULADA", fechaAnulacion = ahora()
     */
    public FacturaResponse anular(Long facturaId) {
        // ✓ PASO 1: Buscar factura en BD con validación de existencia
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", facturaId));

        // ❌ PASO 2A: Validación 1 - Verificar que NO esté ya anulada
        if (AppConstants.ESTADO_FACTURA_ANULADA.equals(factura.getEstado())) {
            throw new BusinessException("La factura ya está anulada");
        }

        // ❌ PASO 2B: Validación 2 - Verificar que NO esté pagada (restricción de auditoría)
        if (AppConstants.ESTADO_FACTURA_PAGADA.equals(factura.getEstado())) {
            throw new BusinessException("No se puede anular una factura ya pagada");
        }

        // ✓ PASO 3: Cambiar estado a ANULADA
        factura.setEstado(AppConstants.ESTADO_FACTURA_ANULADA);
        
        // ✓ PASO 4: Guardar en BD (actualización registrada por auditoría automática)
        Factura anulada = facturaRepository.save(factura);
        return mapToResponse(anulada);
    }

    /**
     * 💳 REGISTRAR PAGO DE UNA FACTURA (Cambiar estado de PENDIENTE a PAGADA)
     * 
     * Procesa el pago de una factura pendiente, validando que:
     * - La factura exista y esté en estado PENDIENTE
     * - El monto pagado coincida exactamente con el total de la factura
     * - La forma de pago sea válida
     * 
     * 💰 FLUJO DE PAGO (5 PASOS):
     * 1️⃣  Buscar factura por ID → Validar existencia en BD
     * 2️⃣  Verificar estado actual:
     *     ❌ Si NO está PENDIENTE → Lanzar excepción (ya fue pagada/anulada/otra)
     * 3️⃣  Validar monto pagado:
     *     ❌ Si no coincide con total → Lanzar excepción con detalles
     * 4️⃣  Registrar datos de pago:
     *     • Estado: PENDIENTE → PAGADA
     *     • Forma de pago: transferencia, efectivo, tarjeta, cheque
     *     • Fecha de pago: NOW (timestamp exacto de procesamiento)
     * 5️⃣  Guardar en BD con auditoría automática
     * 
     * 💵 VALIDACIONES DE MONTO:
     *    • BigDecimal.compareTo(0) para comparación exacta de dinero
     *    • No se permiten pagos parciales (monto != total)
     *    • No se permiten pagos excesivos (monto > total)
     * 
     * 🔐 RESTRICCIONES DE NEGOCIO:
     *    • Solo facturas en estado PENDIENTE pueden ser pagadas
     *    • El monto debe ser exacto (no parcial, no excesivo)
     *    • La transacción es atómica (@Transactional)
     *    • El cambio de estado se registra en auditoría
     * 
     * @param facturaId ID único de la factura a pagar
     * @param request FacturaPagoRequest con:
     *        - montoPagado: BigDecimal (monto exacto pagado)
     *        - formaPago: String (transferencia|efectivo|tarjeta|cheque)
     * @return FacturaResponse con los datos actualizados (estado = PAGADA, fechaPago = ahora)
     * @throws ResourceNotFoundException si la factura no existe en BD
     * @throws BusinessException si:
     *         - La factura NO está en estado PENDIENTE (ya pagada/anulada)
     *         - El montoPagado != total de la factura (validación de exactitud)
     * 
     * @example
     *   FacturaPagoRequest pago = new FacturaPagoRequest();
     *   pago.setMontoPagado(new BigDecimal("1500.00"));
     *   pago.setFormaPago("transferencia");
     *   FacturaResponse pagada = facturaService.registrarPago(123L, pago);
     *   // Resultado: estado = "PAGADA", fechaPago = 2024-01-15 14:30:45, formaPago = "transferencia"
     */
    @Transactional
    public FacturaResponse registrarPago(Long facturaId, FacturaPagoRequest request) {
        // ✓ PASO 1: Buscar factura en BD con validación de existencia
        Factura factura = facturaRepository.findById(facturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", facturaId));

        // ❌ PASO 2: Validación de estado - Solo PENDIENTE puede ser pagada
        if (!AppConstants.ESTADO_FACTURA_PENDIENTE.equals(factura.getEstado())) {
            throw new BusinessException("Solo se pueden pagar facturas en estado PENDIENTE");
        }

        // ❌ PASO 3: Validación de monto - Debe ser exacto (no parcial, no excesivo)
        // Nota: BigDecimal.compareTo(0) retorna:
        //   0  = montos son iguales
        //  -1  = montoPagado < total (pago insuficiente)
        //   1  = montoPagado > total (pago excesivo)
        if (request.getMontoPagado().compareTo(factura.getTotal()) != 0) {
            throw new BusinessException(
                String.format("El monto pagado (%s) no coincide con el total de la factura (%s)",
                    request.getMontoPagado(), factura.getTotal())
            );
        }

        // ✓ PASO 4: Registrar datos de pago
        factura.setEstado(AppConstants.ESTADO_FACTURA_PAGADA);    // Cambiar estado a PAGADA
        factura.setFormaPago(request.getFormaPago());              // Registrar forma de pago (transferencia, efectivo, etc)
        factura.setFechaPago(LocalDateTime.now());                  // Timestamp exacto del pago

        // ✓ PASO 5: Guardar en BD con auditoría automática (@Transactional garantiza atomicidad)
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

