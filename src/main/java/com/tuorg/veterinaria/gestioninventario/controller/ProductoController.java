package com.tuorg.veterinaria.gestioninventario.controller;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoRequest;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoResponse;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoUpdateRequest;
import com.tuorg.veterinaria.gestioninventario.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de productos del inventario.
 * 
 * Este controlador expone endpoints para crear, consultar, actualizar
 * y gestionar productos del inventario.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    /**
     * Servicio de gestión de productos.
     */
    private final ProductoService productoService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param productoService Servicio de productos
     */
    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Crea un nuevo producto.
     * Solo ADMIN puede crear productos.
     * 
     * @param producto Producto a crear
     * @return Respuesta con el producto creado
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse productoCreado = productoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Producto creado exitosamente", productoCreado));
    }

    /**
     * Obtiene un producto por su ID.
     * Todos los usuarios autenticados pueden ver productos.
     * 
     * @param id ID del producto
     * @return Respuesta con el producto
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtener(@PathVariable Long id) {
        ProductoResponse producto = productoService.obtener(id);
        return ResponseEntity.ok(ApiResponse.success("Producto obtenido exitosamente", producto));
    }

    /**
     * Obtiene todos los productos.
     * Todos los usuarios autenticados pueden ver el inventario.
     * 
     * @return Respuesta con la lista de productos
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> obtenerTodos() {
        List<ProductoResponse> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(ApiResponse.success("Productos obtenidos exitosamente", productos));
    }

    /**
     * Verifica la disponibilidad de stock para una cantidad solicitada.
     * VETERINARIO, SECRETARIO y ADMIN pueden verificar disponibilidad.
     * 
     * @param id ID del producto
     * @param cantidad Cantidad solicitada
     * @return Respuesta con el resultado de la verificación
     */
    @GetMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAnyRole('VETERINARIO', 'SECRETARIO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> verificarDisponibilidad(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        boolean disponible = productoService.verificarDisponibilidad(id, cantidad);
        return ResponseEntity.ok(ApiResponse.success("Disponibilidad verificada", disponible));
    }

    /**
     * Obtiene productos con stock bajo.
     * SECRETARIO, VETERINARIO y ADMIN pueden ver productos con stock bajo.
     * 
     * @param nivelStock Nivel mínimo de stock (por defecto 10)
     * @return Respuesta con la lista de productos con stock bajo
     */
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('SECRETARIO', 'VETERINARIO', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> obtenerProductosConStockBajo(
            @RequestParam(defaultValue = "10") Integer nivelStock) {
        List<ProductoResponse> productos = productoService.obtenerProductosConStockBajo(nivelStock);
        return ResponseEntity.ok(ApiResponse.success("Productos con stock bajo obtenidos exitosamente", productos));
    }

    /**
     * Actualiza un producto existente.
     * Solo ADMIN puede actualizar productos.
     * 
     * @param id ID del producto
     * @param request Datos actualizados del producto
     * @return Respuesta con el producto actualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoUpdateRequest request) {
        ProductoResponse producto = productoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success("Producto actualizado exitosamente", producto));
    }
}

