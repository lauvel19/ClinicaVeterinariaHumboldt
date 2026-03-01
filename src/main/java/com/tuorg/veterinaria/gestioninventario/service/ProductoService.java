package com.tuorg.veterinaria.gestioninventario.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.common.util.ValidationUtil;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoRequest;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoResponse;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoUpdateRequest;
import com.tuorg.veterinaria.gestioninventario.model.Producto;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio para la gestión de productos del inventario.
 * 
 * Este servicio proporciona métodos para crear, actualizar, eliminar
 * y consultar productos, así como gestionar el stock.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@SuppressWarnings("null")
@Service
public class ProductoService {

    /**
     * Repositorio de productos.
     */
    private final ProductoRepository productoRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param productoRepository Repositorio de productos
     */
    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un nuevo producto en el inventario con validaciones de negocio.
     * 
     * Flujo de creación:
     * 1. Valida que el SKU sea único (no exista en BD)
     * 2. Valida que el precio unitario sea >= 0
     * 3. Valida que el stock inicial sea >= 0
     * 4. Crea entidad con datos suministrados
     * 5. Persiste en base de datos
     * 6. Invalida cache para reflejar cambios inmediatamente
     * 
     * La anotación @Caching invalida dos caches:
     * - "productos": Cache de listado completo
     * - "productosPorTipo": Cache de productos por tipo
     * 
     * @param request DTO con SKU, nombre, descripción, tipo, precio, UM, stock
     *                inicial, metadatos
     * @return ProductoResponse con datos del producto creado
     * @throws BusinessException si SKU duplicado, precio negativo, o stock negativo
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosPorTipo", allEntries = true)
    })
    public ProductoResponse crear(ProductoRequest request) {
        // VALIDACIÓN 1: Verificar unicidad de SKU
        // SKU (Stock Keeping Unit) es el identificador único del producto
        if (productoRepository.existsBySku(request.getSku())) {
            throw new BusinessException("El SKU ya está en uso");
        }

        // VALIDACIÓN 2: Verificar que precio sea >= 0
        // Usa utilidad centralizada para validaciones numétricas
        ValidationUtil.validateNonNegativeNumber(
                request.getPrecioUnitario().doubleValue(), "precio_unitario");

        // VALIDACIÓN 3: Verificar que stock inicial sea >= 0
        if (request.getStock() != null && request.getStock() < 0) {
            throw new BusinessException("El stock inicial no puede ser negativo");
        }

        // PASO 4: Crear entidad de producto con datos del request
        Producto producto = new Producto();
        producto.setSku(request.getSku());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setTipo(request.getTipo()); // Categoría del producto (medicamentos, instrumental, etc)
        producto.setPrecioUnitario(request.getPrecioUnitario());
        producto.setUm(request.getUm()); // Unidad de Medida (ml, mg, unidad, etc)
        // Si no se especifica stock, se inicia en 0
        producto.setStock(request.getStock() != null ? request.getStock() : 0);
        producto.setMetadatos(request.getMetadatos()); // JSON flexible para propiedades personalizadas

        // PASO 5: Persistir en base de datos
        Producto guardado = productoRepository.save(producto);

        return mapToResponse(guardado);
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @return Producto encontrado
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        Producto producto = obtenerEntidad(id);
        return mapToResponse(producto);
    }

    /**
     * Obtiene todos los productos.
     * 
     * @return Lista de productos
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "productos")
    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id      ID del producto
     * @param request Datos actualizados del producto
     * @return Producto actualizado
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosPorTipo", allEntries = true)
    })
    public ProductoResponse actualizar(Long id, ProductoUpdateRequest request) {
        Producto producto = obtenerEntidad(id);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            producto.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            producto.setDescripcion(request.getDescripcion());
        }
        if (request.getTipo() != null) {
            producto.setTipo(request.getTipo());
        }
        if (request.getPrecioUnitario() != null) {
            ValidationUtil.validateNonNegativeNumber(
                    request.getPrecioUnitario().doubleValue(), "precio_unitario");
            producto.setPrecioUnitario(request.getPrecioUnitario());
        }
        if (request.getUm() != null) {
            producto.setUm(request.getUm());
        }
        if (request.getStock() != null) {
            if (request.getStock() < 0) {
                throw new BusinessException("El stock no puede ser negativo");
            }
            producto.setStock(request.getStock());
        }
        if (request.getMetadatos() != null) {
            producto.setMetadatos(request.getMetadatos());
        }

        Producto actualizado = productoRepository.save(producto);
        return mapToResponse(actualizado);
    }

    /**
     * Actualiza el stock de un producto de forma atómica y transaccional.
     * 
     * Flujo de actualización de stock:
     * 1. Obtiene el producto por ID
     * 2. Calcula nuevo stock (stock_actual + delta)
     * 3. Valida que nuevo stock >= 0 (no permitir negativos)
     * 4. Persiste el cambio en BD
     * 5. Invalida caches para reflejar cambio inmediatamente
     * 
     * Nota: Delta puede ser positivo (entrada) o negativo (salida/consumo)
     * 
     * @param productoId ID del producto a actualizar
     * @param delta      Incremento (positivo) o decremento (negativo) del stock
     * @return Producto con stock actualizado
     * @throws ResourceNotFoundException si producto no existe
     * @throws BusinessException         si resultado sería negativo
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "productos", allEntries = true),
            @CacheEvict(value = "productosPorTipo", allEntries = true)
    })
    public Producto actualizarStock(Long productoId, Integer delta) {
        // PASO 1: Obtener producto por ID
        Producto producto = obtenerEntidad(productoId);

        // PASO 2: Calcular nuevo stock
        int nuevoStock = producto.getStock() + delta;

        // PASO 3: Validar que nuevo stock no sea negativo
        // Previene insolvencia de stock (situación crítica)
        if (nuevoStock < 0) {
            throw new BusinessException("No hay suficiente stock disponible. Stock actual: " + producto.getStock());
        }

        // PASO 4: Actualizar stock en entidad
        producto.setStock(nuevoStock);

        // PASO 5: Persistir cambio (dentro de transacción @Transactional)
        // Los caches se invalidan automáticamente por la anotación @Caching
        return productoRepository.save(producto);
    }

    /**
     * Verifica la disponibilidad de stock para una cantidad solicitada.
     * 
     * @param productoId ID del producto
     * @param cantidad   Cantidad solicitada
     * @return true si hay stock suficiente, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long productoId, Integer cantidad) {
        Producto producto = obtenerEntidad(productoId);
        return producto.getStock() >= cantidad;
    }

    /**
     * Obtiene productos con stock bajo.
     * 
     * @param nivelStock Nivel mínimo de stock para considerar bajo
     * @return Lista de productos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerProductosConStockBajo(Integer nivelStock) {
        return productoRepository.findProductosConStockBajo(nivelStock)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Producto obtenerEntidad(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
    }

    private ProductoResponse mapToResponse(Producto producto) {
        Map<String, Object> metadatos = producto.getMetadatos();
        return new ProductoResponse(
                producto.getIdProducto(),
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getTipo(),
                producto.getStock(),
                producto.getPrecioUnitario(),
                producto.getUm(),
                metadatos);
    }
}
