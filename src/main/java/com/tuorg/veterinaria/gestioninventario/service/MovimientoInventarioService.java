package com.tuorg.veterinaria.gestioninventario.service;

import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.common.validation.BusinessValidator;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoEntradaRequest;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoInventarioResponse;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoSalidaRequest;
import com.tuorg.veterinaria.gestioninventario.model.MovimientoInventario;
import com.tuorg.veterinaria.gestioninventario.model.Producto;
import com.tuorg.veterinaria.gestioninventario.model.Proveedor;
import com.tuorg.veterinaria.gestioninventario.repository.MovimientoInventarioRepository;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import com.tuorg.veterinaria.gestioninventario.repository.ProveedorRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la gestión de movimientos de inventario.
 * 
 * Este servicio implementa el patrón Command para registrar
 * movimientos de inventario de forma transaccional.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@SuppressWarnings("null")
@Service
public class MovimientoInventarioService {

    /**
     * Repositorio de movimientos de inventario.
     */
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    /**
     * Repositorio de productos.
     */
    private final ProductoRepository productoRepository;

    /**
     * Repositorio de proveedores.
     */
    private final ProveedorRepository proveedorRepository;

    /**
     * Repositorio de usuarios.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Servicio de productos.
     */
    private final ProductoService productoService;

    /**
     * Validador de reglas de negocio.
     */
    private final BusinessValidator businessValidator;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param movimientoInventarioRepository Repositorio de movimientos
     * @param productoRepository             Repositorio de productos
     * @param proveedorRepository            Repositorio de proveedores
     * @param usuarioRepository              Repositorio de usuarios
     * @param productoService                Servicio de productos
     */
    @Autowired
    public MovimientoInventarioService(
            MovimientoInventarioRepository movimientoInventarioRepository,
            ProductoRepository productoRepository,
            ProveedorRepository proveedorRepository,
            UsuarioRepository usuarioRepository,
            ProductoService productoService,
            BusinessValidator businessValidator) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoService = productoService;
        this.businessValidator = businessValidator;
    }

    /**
     * Registra una entrada de inventario (patrón Command).
     * 
     * Esta operación es transaccional: registra el movimiento y
     * actualiza el stock del producto.
     * 
     * @param productoId  ID del producto
     * @param proveedorId ID del proveedor (opcional)
     * @param cantidad    Cantidad de entrada
     * @param referencia  Referencia del movimiento
     * @param usuarioId   ID del usuario que realiza el movimiento
     * @return MovimientoInventario creado
     */
    @Transactional
    public MovimientoInventarioResponse registrarEntrada(MovimientoEntradaRequest request) {
        if (request.getCantidad() <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero");
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(AppConstants.TIPO_MOVIMIENTO_ENTRADA);
        movimiento.setCantidad(request.getCantidad());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setReferencia(request.getReferencia());

        if (request.getProveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.getProveedorId()));
            movimiento.setProveedor(proveedor);
        }

        if (request.getUsuarioId() != null) {
            movimiento.setUsuario(usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUsuarioId())));
        }

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        Producto actualizado = productoService.actualizarStock(request.getProductoId(), request.getCantidad());
        return mapToResponse(guardado, actualizado.getStock());
    }

    /**
     * Registra una salida de inventario (patrón Command).
     * 
     * Esta operación es transaccional: verifica stock disponible,
     * registra el movimiento y actualiza el stock del producto.
     * 
     * @param productoId ID del producto
     * @param cantidad   Cantidad de salida
     * @param referencia Referencia del movimiento
     * @param usuarioId  ID del usuario que realiza el movimiento
     * @return MovimientoInventario creado
     */
    @Transactional
    public MovimientoInventarioResponse registrarSalida(MovimientoSalidaRequest request) {
        if (request.getCantidad() <= 0) {
            throw new BusinessException("La cantidad debe ser mayor que cero");
        }

        // Validación mejorada de stock con mensaje detallado
        businessValidator.validarStockSuficiente(request.getProductoId(), request.getCantidad());

        if (!productoService.verificarDisponibilidad(request.getProductoId(), request.getCantidad())) {
            Producto producto = productoService.obtenerEntidad(request.getProductoId());
            throw new BusinessException("Stock insuficiente. Stock disponible: " + producto.getStock());
        }

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(AppConstants.TIPO_MOVIMIENTO_SALIDA);
        movimiento.setCantidad(request.getCantidad());
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setReferencia(request.getReferencia());

        if (request.getUsuarioId() != null) {
            movimiento.setUsuario(usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUsuarioId())));
        }

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        Producto actualizado = productoService.actualizarStock(request.getProductoId(), -request.getCantidad());
        return mapToResponse(guardado, actualizado.getStock());
    }

    /**
     * Obtiene todos los movimientos de un producto.
     * 
     * @param productoId ID del producto
     * @return Lista de movimientos del producto
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> obtenerPorProducto(Long productoId) {
        return movimientoInventarioRepository.findByProductoId(productoId)
                .stream()
                .map(mov -> mapToResponse(mov, null))
                .toList();
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin    Fecha de fin
     * @return Lista de movimientos en el rango especificado
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> obtenerPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return movimientoInventarioRepository.findByFechaBetween(fechaInicio, fechaFin)
                .stream()
                .map(mov -> mapToResponse(mov, null))
                .toList();
    }

    /**
     * Revierte un movimiento de inventario (patrón Command con reversión).
     * 
     * Esta operación es transaccional: crea un movimiento inverso y
     * actualiza el stock del producto. Solo se pueden revertir movimientos
     * que no hayan sido revertidos previamente.
     * 
     * @param movimientoId ID del movimiento a revertir
     * @param usuarioId    ID del usuario que realiza la reversión
     * @return MovimientoInventario de reversión creado
     */
    @Transactional
    public MovimientoInventarioResponse revertirMovimiento(Long movimientoId, Long usuarioId) {
        MovimientoInventario movimientoOriginal = movimientoInventarioRepository.findById(movimientoId)
                .orElseThrow(() -> new ResourceNotFoundException("MovimientoInventario", "id", movimientoId));

        // Verificar que el movimiento no haya sido revertido previamente
        // (esto se puede hacer buscando si existe un movimiento de reversión para este)
        boolean yaRevertido = movimientoInventarioRepository.existsByReferencia(
                "REVERSION-" + movimientoId);
        if (yaRevertido) {
            throw new BusinessException("Este movimiento ya ha sido revertido");
        }

        // Crear movimiento inverso
        MovimientoInventario movimientoReversion = new MovimientoInventario();
        movimientoReversion.setProducto(movimientoOriginal.getProducto());

        // Invertir el tipo de movimiento
        if (AppConstants.TIPO_MOVIMIENTO_ENTRADA.equals(movimientoOriginal.getTipoMovimiento())) {
            movimientoReversion.setTipoMovimiento(AppConstants.TIPO_MOVIMIENTO_SALIDA);
        } else if (AppConstants.TIPO_MOVIMIENTO_SALIDA.equals(movimientoOriginal.getTipoMovimiento())) {
            movimientoReversion.setTipoMovimiento(AppConstants.TIPO_MOVIMIENTO_ENTRADA);
        } else {
            throw new BusinessException("No se puede revertir un movimiento de tipo AJUSTE");
        }

        // Mantener la misma cantidad (pero con signo inverso implícito en el tipo)
        movimientoReversion.setCantidad(movimientoOriginal.getCantidad());
        movimientoReversion.setFecha(LocalDateTime.now());
        movimientoReversion.setReferencia("REVERSION-" + movimientoId);
        movimientoReversion.setProveedor(movimientoOriginal.getProveedor());

        if (usuarioId != null) {
            movimientoReversion.setUsuario(usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId)));
        }

        MovimientoInventario guardado = movimientoInventarioRepository.save(movimientoReversion);

        // Actualizar stock (invertir el cambio original)
        Integer cantidadAjuste;
        if (AppConstants.TIPO_MOVIMIENTO_ENTRADA.equals(movimientoOriginal.getTipoMovimiento())) {
            // Si era entrada, ahora es salida (restar)
            cantidadAjuste = -movimientoOriginal.getCantidad();
        } else {
            // Si era salida, ahora es entrada (sumar)
            cantidadAjuste = movimientoOriginal.getCantidad();
        }

        Producto actualizado = productoService.actualizarStock(
                movimientoOriginal.getProducto().getIdProducto(),
                cantidadAjuste);

        return mapToResponse(guardado, actualizado.getStock());
    }

    private MovimientoInventarioResponse mapToResponse(MovimientoInventario movimiento, Integer stockResultante) {
        MovimientoInventarioResponse.ProductoSummary productoSummary = new MovimientoInventarioResponse.ProductoSummary(
                movimiento.getProducto().getIdProducto(),
                movimiento.getProducto().getSku(),
                movimiento.getProducto().getNombre());

        MovimientoInventarioResponse.ProveedorSummary proveedorSummary = null;
        if (movimiento.getProveedor() != null) {
            proveedorSummary = new MovimientoInventarioResponse.ProveedorSummary(
                    movimiento.getProveedor().getIdProveedor(),
                    movimiento.getProveedor().getNombre());
        }

        MovimientoInventarioResponse.UsuarioSummary usuarioSummary = null;
        if (movimiento.getUsuario() != null) {
            Usuario usuario = movimiento.getUsuario();
            usuarioSummary = new MovimientoInventarioResponse.UsuarioSummary(
                    usuario.getIdUsuario(),
                    usuario.getUsername(),
                    usuario.getNombre(),
                    usuario.getApellido());
        }

        return new MovimientoInventarioResponse(
                movimiento.getIdMovimiento(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getFecha(),
                movimiento.getReferencia(),
                productoSummary,
                proveedorSummary,
                usuarioSummary,
                stockResultante);
    }
}
