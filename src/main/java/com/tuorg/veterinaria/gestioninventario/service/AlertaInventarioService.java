package com.tuorg.veterinaria.gestioninventario.service;

import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestioninventario.model.AlertaInventario;
import com.tuorg.veterinaria.gestioninventario.model.Producto;
import com.tuorg.veterinaria.gestioninventario.repository.AlertaInventarioRepository;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Servicio para la gestión de alertas de inventario.
 * 
 * Este servicio proporciona métodos para generar y consultar
 * alertas cuando el stock de productos alcanza niveles críticos.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class AlertaInventarioService {

    /**
     * Repositorio de alertas de inventario.
     */
    private final AlertaInventarioRepository alertaInventarioRepository;

    /**
     * Repositorio de productos.
     */
    private final ProductoRepository productoRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param alertaInventarioRepository Repositorio de alertas
     * @param productoRepository         Repositorio de productos
     */
    @Autowired
    public AlertaInventarioService(AlertaInventarioRepository alertaInventarioRepository,
            ProductoRepository productoRepository) {
        this.alertaInventarioRepository = alertaInventarioRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Genera una alerta para un producto.
     * 
     * @param productoId ID del producto
     * @return AlertaInventario creada
     */
    @Transactional
    public AlertaInventario generarAlerta(Long productoId) {
        Producto producto = productoRepository.findById(Objects.requireNonNull(productoId))
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));

        AlertaInventario alerta = new AlertaInventario();
        alerta.setProducto(producto);
        alerta.setNivelStock(producto.getStock());
        alerta.setFechaGenerada(LocalDateTime.now());
        alerta.setMensaje(String.format("Stock bajo para producto %s (SKU: %s). Stock actual: %d",
                producto.getNombre(), producto.getSku(), producto.getStock()));

        return Objects.requireNonNull(alertaInventarioRepository.save(Objects.requireNonNull(alerta)));
    }

    /**
     * Obtiene todas las alertas de un producto.
     * 
     * @param productoId ID del producto
     * @return Lista de alertas del producto
     */
    @Transactional(readOnly = true)
    public List<AlertaInventario> obtenerPorProducto(Long productoId) {
        return alertaInventarioRepository.findByProductoId(Objects.requireNonNull(productoId));
    }

    /**
     * Obtiene todas las alertas.
     * 
     * @return Lista de todas las alertas
     */
    @Transactional(readOnly = true)
    public List<AlertaInventario> obtenerTodas() {
        return alertaInventarioRepository.findAll();
    }
}
