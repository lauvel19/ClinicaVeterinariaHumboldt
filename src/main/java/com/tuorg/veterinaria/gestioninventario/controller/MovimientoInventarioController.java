package com.tuorg.veterinaria.gestioninventario.controller;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoEntradaRequest;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoInventarioResponse;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoSalidaRequest;
import com.tuorg.veterinaria.gestioninventario.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de movimientos de inventario.
 * 
 * Este controlador expone endpoints para registrar entradas y salidas
 * de inventario (implementando el patrón Command).
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/movimientos-inventario")
public class MovimientoInventarioController {

    /**
     * Servicio de gestión de movimientos de inventario.
     */
    private final MovimientoInventarioService movimientoInventarioService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param movimientoInventarioService Servicio de movimientos
     */
    @Autowired
    public MovimientoInventarioController(MovimientoInventarioService movimientoInventarioService) {
        this.movimientoInventarioService = movimientoInventarioService;
    }

    /**
     * Registra una entrada de inventario.
     * 
     * @param requestBody Cuerpo de la petición con los datos del movimiento
     * @return Respuesta con el movimiento creado
     */
    @PostMapping("/entrada")
    public ResponseEntity<ApiResponse<MovimientoInventarioResponse>> registrarEntrada(
            @Valid @RequestBody MovimientoEntradaRequest request) {
        MovimientoInventarioResponse movimiento = movimientoInventarioService.registrarEntrada(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entrada de inventario registrada exitosamente", movimiento));
    }

    /**
     * Registra una salida de inventario.
     * 
     * @param requestBody Cuerpo de la petición con los datos del movimiento
     * @return Respuesta con el movimiento creado
     */
    @PostMapping("/salida")
    public ResponseEntity<ApiResponse<MovimientoInventarioResponse>> registrarSalida(
            @Valid @RequestBody MovimientoSalidaRequest request) {
        MovimientoInventarioResponse movimiento = movimientoInventarioService.registrarSalida(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salida de inventario registrada exitosamente", movimiento));
    }

    /**
     * Obtiene todos los movimientos de un producto.
     * 
     * @param productoId ID del producto
     * @return Respuesta con la lista de movimientos
     */
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<ApiResponse<List<MovimientoInventarioResponse>>> obtenerPorProducto(
            @PathVariable Long productoId) {
        List<MovimientoInventarioResponse> movimientos = movimientoInventarioService.obtenerPorProducto(productoId);
        return ResponseEntity.ok(ApiResponse.success("Movimientos obtenidos exitosamente", movimientos));
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio (formato: yyyy-MM-ddTHH:mm:ss)
     * @param fechaFin    Fecha de fin (formato: yyyy-MM-ddTHH:mm:ss)
     * @return Respuesta con la lista de movimientos
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<ApiResponse<List<MovimientoInventarioResponse>>> obtenerPorRangoFechas(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        List<MovimientoInventarioResponse> movimientos = movimientoInventarioService.obtenerPorRangoFechas(
                LocalDateTime.parse(fechaInicio),
                LocalDateTime.parse(fechaFin));
        return ResponseEntity.ok(ApiResponse.success("Movimientos obtenidos exitosamente", movimientos));
    }
}
