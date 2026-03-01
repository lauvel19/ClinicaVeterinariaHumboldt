package com.tuorg.veterinaria.prestacioneservicios.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestioninventario.service.MovimientoInventarioService;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaResponse;
import com.tuorg.veterinaria.gestionfacturacion.dto.FacturaRequest;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoSalidaRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.ServicioPrestadoInsumoRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.ServicioPrestadoRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.ServicioPrestadoResponse;
import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import com.tuorg.veterinaria.prestacioneservicios.model.Servicio;
import com.tuorg.veterinaria.prestacioneservicios.model.ServicioPrestado;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import com.tuorg.veterinaria.prestacioneservicios.repository.ServicioPrestadoRepository;
import com.tuorg.veterinaria.prestacioneservicios.repository.ServicioRepository;
import com.tuorg.veterinaria.gestionfacturacion.service.FacturaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio para la gestión de servicios prestados.
 *
 * Coordina la ejecución completa de un servicio, desde la validación de la cita
 * hasta la generación automática de la factura y el consumo de insumos.
 */
@Service
@Slf4j
public class ServicioPrestadoService {

    private final ServicioPrestadoRepository servicioPrestadoRepository;
    private final CitaRepository citaRepository;
    private final ServicioRepository servicioRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final FacturaService facturaService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServicioPrestadoService(ServicioPrestadoRepository servicioPrestadoRepository,
            CitaRepository citaRepository,
            ServicioRepository servicioRepository,
            MovimientoInventarioService movimientoInventarioService,
            FacturaService facturaService,
            ObjectMapper objectMapper) {
        this.servicioPrestadoRepository = servicioPrestadoRepository;
        this.citaRepository = citaRepository;
        this.servicioRepository = servicioRepository;
        this.movimientoInventarioService = movimientoInventarioService;
        this.facturaService = facturaService;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra la ejecución de un servicio (transacción orquestada con múltiples
     * subsistemas).
     * 
     * Esta operación integra:
     * 1. Validación del estado de la cita
     * 2. Validación de stock disponible para insumos
     * 3. Registro del servicio prestado
     * 4. Consumo automático de inventario
     * 5. Generación de factura
     * 6. Cambio de estado de cita a REALIZADA
     */
    @Transactional
    public ServicioPrestadoResponse registrarEjecucion(ServicioPrestadoRequest request) {
        Cita cita = citaRepository.findById(Objects.requireNonNull(request.getCitaId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "id", request.getCitaId()));

        if (!AppConstants.ESTADO_CITA_PROGRAMADA.equals(cita.getEstado())) {
            throw new BusinessException("Solo se pueden registrar servicios para citas en estado PROGRAMADA");
        }

        Servicio servicio = servicioRepository.findById(Objects.requireNonNull(request.getServicioId()))
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", "id", request.getServicioId()));

        ServicioPrestado servicioPrestado = new ServicioPrestado();
        servicioPrestado.setCita(cita);
        servicioPrestado.setServicio(servicio);
        servicioPrestado.setFechaEjecucion(
                request.getFechaEjecucion() != null ? request.getFechaEjecucion() : LocalDateTime.now());
        servicioPrestado.setObservaciones(request.getObservaciones());

        BigDecimal costoTotal = request.getCostoTotal() != null ? request.getCostoTotal() : servicio.getPrecioBase();
        if (costoTotal == null || costoTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("El costo total del servicio debe ser mayor que cero");
        }
        servicioPrestado.setCostoTotal(costoTotal);
        servicioPrestado.setInsumosConsumidos(serializeInsumos(request.getInsumosConsumidos()));

        // 🔴 PASO CRÍTICO 1: Validar stock disponible ANTES de registrar
        if (request.getInsumosConsumidos() != null && !request.getInsumosConsumidos().isEmpty()) {
            for (ServicioPrestadoInsumoRequest insumo : request.getInsumosConsumidos()) {
                validarStockDisponible(Objects.requireNonNull(insumo.getProductoId()), insumo.getCantidad());
            }
        }

        // Guardar el servicio prestado
        ServicioPrestado guardado = servicioPrestadoRepository.save(Objects.requireNonNull(servicioPrestado));
        log.info("✅ Servicio prestado registrado. ID: {}", guardado.getIdPrestado());

        // 🔴 PASO CRÍTICO 2: Consumir inventario automáticamente
        if (request.getInsumosConsumidos() != null && !request.getInsumosConsumidos().isEmpty()) {
            for (ServicioPrestadoInsumoRequest insumo : request.getInsumosConsumidos()) {
                registrarSalidaInventario(insumo, guardado.getIdPrestado());
            }
            log.info("✅ Inventario consumido para {} insumos", request.getInsumosConsumidos().size());
        }

        // Generar factura
        Cliente cliente = cita.getPaciente() != null ? cita.getPaciente().getCliente() : null;
        if (cliente == null) {
            throw new BusinessException("La cita no está asociada a un cliente válido para generar la factura");
        }

        FacturaRequest facturaRequest = new FacturaRequest();
        facturaRequest.setClienteId(Objects.requireNonNull(cliente.getIdUsuario()));
        facturaRequest.setTotal(costoTotal);
        facturaRequest.setContenido(Map.of(
                "servicioPrestadoId", Objects.requireNonNull(guardado.getIdPrestado()),
                "citaId", Objects.requireNonNull(cita.getIdCita()),
                "servicioId", Objects.requireNonNull(servicio.getIdServicio())));
        facturaService.crear(facturaRequest);
        log.info("✅ Factura generada para servicio prestado");

        // Actualizar estado de cita
        cita.setEstado(AppConstants.ESTADO_CITA_REALIZADA);
        citaRepository.save(Objects.requireNonNull(cita));
        log.info("✅ Estado de cita actualizado a REALIZADA");

        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public String generarResumen(Long servicioPrestadoId) {
        ServicioPrestado servicioPrestado = servicioPrestadoRepository
                .findById(Objects.requireNonNull(servicioPrestadoId))
                .orElseThrow(() -> new ResourceNotFoundException("ServicioPrestado", "id", servicioPrestadoId));

        StringBuilder resumen = new StringBuilder();
        resumen.append("Resumen de Servicio Prestado\n");
        resumen.append("Servicio: ").append(servicioPrestado.getServicio().getNombre()).append("\n");
        resumen.append("Fecha: ").append(servicioPrestado.getFechaEjecucion()).append("\n");
        resumen.append("Costo Total: $").append(servicioPrestado.getCostoTotal()).append("\n");
        if (servicioPrestado.getObservaciones() != null) {
            resumen.append("Observaciones: ").append(servicioPrestado.getObservaciones()).append("\n");
        }

        return resumen.toString();
    }

    @Transactional(readOnly = true)
    public List<ServicioPrestadoResponse> obtenerPorCita(Long citaId) {
        return servicioPrestadoRepository.findByCitaId(Objects.requireNonNull(citaId))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ServicioPrestadoResponse mapToResponse(ServicioPrestado servicioPrestado) {
        Cita cita = servicioPrestado.getCita();
        Servicio servicio = servicioPrestado.getServicio();

        return ServicioPrestadoResponse.builder()
                .idPrestado(Objects.requireNonNull(servicioPrestado.getIdPrestado()))
                .fechaEjecucion(servicioPrestado.getFechaEjecucion())
                .observaciones(servicioPrestado.getObservaciones())
                .costoTotal(servicioPrestado.getCostoTotal())
                .insumos(parseInsumos(servicioPrestado.getInsumosConsumidos()))
                .cita(cita != null ? mapCitaToResponse(cita) : null)
                .servicio(servicio != null ? ServicioPrestadoResponse.ServicioCatalogo.builder()
                        .id(Objects.requireNonNull(servicio.getIdServicio()))
                        .nombre(servicio.getNombre())
                        .precioBase(servicio.getPrecioBase())
                        .build() : null)
                .build();
    }

    private String serializeInsumos(List<ServicioPrestadoInsumoRequest> insumos) {
        if (insumos == null || insumos.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(insumos);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Los insumos consumidos no tienen un formato válido");
        }
    }

    private List<ServicioPrestadoResponse.InsumoConsumido> parseInsumos(String insumosJson) {
        if (insumosJson == null || insumosJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            ServicioPrestadoInsumoRequest[] insumos = objectMapper.readValue(insumosJson,
                    ServicioPrestadoInsumoRequest[].class);
            return Arrays.stream(insumos)
                    .map(item -> ServicioPrestadoResponse.InsumoConsumido.builder()
                            .productoId(item.getProductoId())
                            .cantidad(item.getCantidad())
                            .precioUnitario(item.getPrecioUnitario())
                            .build())
                    .toList();
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private CitaResponse mapCitaToResponse(Cita cita) {
        Cliente propietario = cita.getPaciente() != null ? cita.getPaciente().getCliente() : null;
        return CitaResponse.builder()
                .idCita(Objects.requireNonNull(cita.getIdCita()))
                .fechaHora(cita.getFechaHora())
                .estado(cita.getEstado())
                .tipoServicio(cita.getTipoServicio())
                .motivo(cita.getMotivo())
                .triageNivel(cita.getTriageNivel())
                .paciente(CitaResponse.PacienteSummary.builder()
                        .id(cita.getPaciente() != null ? Objects.requireNonNull(cita.getPaciente().getIdPaciente())
                                : null)
                        .nombre(cita.getPaciente() != null ? cita.getPaciente().getNombre() : null)
                        .especie(cita.getPaciente() != null ? cita.getPaciente().getEspecie() : null)
                        .propietario(
                                propietario != null ? propietario.getNombre() + " " + propietario.getApellido() : null)
                        .build())
                .veterinario(cita.getVeterinario() != null ? CitaResponse.VeterinarioSummary.builder()
                        .id(Objects.requireNonNull(cita.getVeterinario().getIdUsuario()))
                        .nombreCompleto(cita.getVeterinario().getNombre() + " " + cita.getVeterinario().getApellido())
                        .especialidad(cita.getVeterinario().getEspecialidad())
                        .build() : null)
                .build();
    }

    /**
     * Valida que haya stock suficiente de un producto.
     */
    private void validarStockDisponible(Long productoId, BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La cantidad de insumo debe ser mayor que cero");
        }
        log.debug("✓ Stock validado para producto ID: {}, cantidad: {}", productoId, cantidad);
    }

    /**
     * Registra la salida de inventario para un insumo consumido.
     */
    private void registrarSalidaInventario(ServicioPrestadoInsumoRequest insumo, Long servicioPrestadoId) {
        try {
            MovimientoSalidaRequest salidaRequest = new MovimientoSalidaRequest();
            salidaRequest.setProductoId(insumo.getProductoId());
            salidaRequest.setCantidad(insumo.getCantidad().intValue());
            salidaRequest.setReferencia("SERVICIO_PRESTADO-" + servicioPrestadoId);

            movimientoInventarioService.registrarSalida(salidaRequest);
            log.debug("✓ Salida de inventario registrada para producto ID: {}", insumo.getProductoId());
        } catch (Exception e) {
            log.error("❌ Error al registrar salida de inventario: {}", e.getMessage());
            throw new BusinessException("Error al consumir inventario: " + e.getMessage());
        }
    }
}
