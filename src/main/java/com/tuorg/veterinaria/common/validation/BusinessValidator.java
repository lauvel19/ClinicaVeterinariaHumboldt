package com.tuorg.veterinaria.common.validation;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import com.tuorg.veterinaria.gestioninventario.model.Producto;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Componente para validaciones de negocio avanzadas.
 * 
 * Centraliza validaciones complejas que involucran múltiples entidades
 * o lógica de negocio específica.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Component
public class BusinessValidator {

    private final ProductoRepository productoRepository;
    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final FacturaRepository facturaRepository;

    @Autowired
    public BusinessValidator(ProductoRepository productoRepository,
                            PacienteRepository pacienteRepository,
                            CitaRepository citaRepository,
                            FacturaRepository facturaRepository) {
        this.productoRepository = productoRepository;
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.facturaRepository = facturaRepository;
    }

    /**
     * Valida que un movimiento de inventario no deje el stock en negativo.
     * 
     * @param productoId ID del producto
     * @param cantidadSalida Cantidad a descontar
     * @throws BusinessException Si el stock resultante sería negativo
     */
    public void validarStockSuficiente(Long productoId, Integer cantidadSalida) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
        
        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        int stockResultante = stockActual - cantidadSalida;
        
        if (stockResultante < 0) {
            throw new BusinessException(
                String.format("Stock insuficiente. Disponible: %d, Solicitado: %d. " +
                             "Se requieren %d unidades adicionales.", 
                             stockActual, cantidadSalida, Math.abs(stockResultante))
            );
        }
    }

    /**
     * Valida que no existan citas solapadas para un mismo paciente.
     * 
     * @param pacienteId ID del paciente
     * @param fechaHora Fecha y hora de la cita
     * @param duracionMinutos Duración estimada de la cita en minutos
     * @throws BusinessException Si existe solapamiento
     */
    public void validarSolapamientoCitasPaciente(Long pacienteId, LocalDateTime fechaHora, 
                                                 int duracionMinutos) {
        LocalDateTime inicioRango = fechaHora.minusMinutes(duracionMinutos);
        LocalDateTime finRango = fechaHora.plusMinutes(duracionMinutos);
        
        List<Cita> citasExistentes = citaRepository.findByPacienteId(pacienteId)
            .stream()
            .filter(c -> "PROGRAMADA".equals(c.getEstado()))
            .filter(c -> {
                LocalDateTime fechaCita = c.getFechaHora();
                return fechaCita.isAfter(inicioRango) && fechaCita.isBefore(finRango);
            })
            .toList();
        
        if (!citasExistentes.isEmpty()) {
            throw new BusinessException(
                String.format("El paciente ya tiene una cita programada cerca de ese horario (%s). " +
                             "Por favor, seleccione otra fecha y hora.",
                             citasExistentes.get(0).getFechaHora())
            );
        }
    }

    /**
     * Detecta y advierte sobre posibles pacientes duplicados.
     * 
     * @param nombre Nombre del paciente
     * @param clienteId ID del cliente propietario
     * @param especie Especie del paciente
     * @return Lista de pacientes similares que podrían ser duplicados
     */
    public List<Paciente> detectarPacientesDuplicados(String nombre, Long clienteId, String especie) {
        // Buscar pacientes con el mismo nombre y propietario
        List<Paciente> pacientesSimilares = pacienteRepository.buscarPorNombre(nombre)
            .stream()
            .filter(p -> p.getCliente() != null && p.getCliente().getIdUsuario().equals(clienteId))
            .filter(p -> especie == null || especie.equalsIgnoreCase(p.getEspecie()))
            .toList();
        
        return pacientesSimilares;
    }

    /**
     * Valida que un cliente no tenga deudas pendientes antes de agendar una cita.
     * 
     * @param clienteId ID del cliente
     * @param montoMaximoPermitido Monto máximo de deuda permitido
     * @throws BusinessException Si el cliente tiene deuda excesiva
     */
    public void validarDeudaCliente(Long clienteId, double montoMaximoPermitido) {
        if (montoMaximoPermitido < 0) {
            throw new BusinessException("El monto máximo permitido no puede ser negativo");
        }

        BigDecimal deudaPendiente = facturaRepository.findByClienteId(clienteId)
                .stream()
                .filter(factura -> "PENDIENTE".equalsIgnoreCase(factura.getEstado()))
                .map(Factura::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limite = BigDecimal.valueOf(montoMaximoPermitido);
        if (deudaPendiente.compareTo(limite) > 0) {
            throw new BusinessException(
                    String.format("El cliente supera la deuda permitida. Deuda actual: %.2f, límite: %.2f",
                            deudaPendiente.doubleValue(), montoMaximoPermitido)
            );
        }
    }

    /**
     * Valida que un producto tenga precio válido antes de venderlo.
     * 
     * @param productoId ID del producto
     * @throws BusinessException Si el precio es inválido o no está configurado
     */
    public void validarPrecioProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new BusinessException("Producto no encontrado"));
        
        if (producto.getPrecioUnitario() == null || producto.getPrecioUnitario().doubleValue() <= 0) {
            throw new BusinessException(
                String.format("El producto '%s' no tiene un precio configurado o es inválido. " +
                             "Por favor, configure el precio antes de venderlo.",
                             producto.getNombre())
            );
        }
    }
}
