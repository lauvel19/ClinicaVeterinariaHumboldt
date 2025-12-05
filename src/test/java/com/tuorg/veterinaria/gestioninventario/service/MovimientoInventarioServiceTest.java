package com.tuorg.veterinaria.gestioninventario.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.validation.BusinessValidator;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoEntradaRequest;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoInventarioResponse;
import com.tuorg.veterinaria.gestioninventario.dto.MovimientoSalidaRequest;
import com.tuorg.veterinaria.gestioninventario.model.MovimientoInventario;
import com.tuorg.veterinaria.gestioninventario.model.Producto;
import com.tuorg.veterinaria.gestioninventario.repository.MovimientoInventarioRepository;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import com.tuorg.veterinaria.gestioninventario.repository.ProveedorRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MovimientoInventarioService.
 * 
 * Verifica entrada con cantidad ≤ 0, salida sin stock,
 * y verifica stock resultante.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de MovimientoInventarioService")
class MovimientoInventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private BusinessValidator businessValidator;

    @InjectMocks
    private MovimientoInventarioService movimientoInventarioService;

    private Producto producto;
    private MovimientoEntradaRequest entradaRequest;
    private MovimientoSalidaRequest salidaRequest;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setIdProducto(1L);
        producto.setSku("PROD-001");
        producto.setNombre("Medicamento A");
        producto.setStock(50);

        entradaRequest = new MovimientoEntradaRequest();
        entradaRequest.setProductoId(1L);
        entradaRequest.setCantidad(20);
        entradaRequest.setReferencia("Compra 001");

        salidaRequest = new MovimientoSalidaRequest();
        salidaRequest.setProductoId(1L);
        salidaRequest.setCantidad(10);
        salidaRequest.setReferencia("Venta 001");
    }

    @Test
    @DisplayName("Registrar entrada exitosa: debe actualizar stock")
    void registrarEntradaExitoso_DeberiaActualizarStock() {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setIdProducto(1L);
        productoActualizado.setStock(70); // 50 + 20

        MovimientoInventario movimientoGuardado = new MovimientoInventario();
        movimientoGuardado.setIdMovimiento(1L);
        movimientoGuardado.setProducto(producto);
        movimientoGuardado.setCantidad(20);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardado);
        when(productoService.actualizarStock(eq(1L), eq(20))).thenReturn(productoActualizado);

        // Act
        MovimientoInventarioResponse response = movimientoInventarioService.registrarEntrada(entradaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCantidad()).isEqualTo(20);
        assertThat(response.getStockResultante()).isEqualTo(70);

        verify(productoRepository).findById(1L);
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
        verify(productoService).actualizarStock(1L, 20);
    }

    @Test
    @DisplayName("Registrar entrada: cantidad ≤ 0 debe lanzar excepción")
    void registrarEntradaCantidadInvalida_DeberiaLanzarExcepcion() {
        // Arrange
        entradaRequest.setCantidad(0);

        // Act & Assert
        assertThatThrownBy(() -> movimientoInventarioService.registrarEntrada(entradaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La cantidad debe ser mayor que cero");

        verify(productoRepository, never()).findById(any());
        verify(movimientoInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar salida exitosa: debe verificar stock y actualizar")
    void registrarSalidaExitoso_DeberiaVerificarStockYActualizar() {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setIdProducto(1L);
        productoActualizado.setStock(40); // 50 - 10

        MovimientoInventario movimientoGuardado = new MovimientoInventario();
        movimientoGuardado.setIdMovimiento(1L);
        movimientoGuardado.setProducto(producto);
        movimientoGuardado.setCantidad(10);

        doNothing().when(businessValidator).validarStockSuficiente(eq(1L), eq(10));
        when(productoService.verificarDisponibilidad(eq(1L), eq(10))).thenReturn(true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenReturn(movimientoGuardado);
        when(productoService.actualizarStock(eq(1L), eq(-10))).thenReturn(productoActualizado);

        // Act
        MovimientoInventarioResponse response = movimientoInventarioService.registrarSalida(salidaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCantidad()).isEqualTo(10);
        assertThat(response.getStockResultante()).isEqualTo(40);

        verify(businessValidator).validarStockSuficiente(1L, 10);
        verify(productoService).verificarDisponibilidad(1L, 10);
        verify(productoRepository).findById(1L);
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
        verify(productoService).actualizarStock(1L, -10);
    }

    @Test
    @DisplayName("Registrar salida: sin stock suficiente debe lanzar excepción")
    void registrarSalidaSinStock_DeberiaLanzarExcepcion() {
        // Arrange
        salidaRequest.setCantidad(100); // Más que el stock disponible (50)
        when(productoService.verificarDisponibilidad(eq(1L), eq(100))).thenReturn(false);
        when(productoService.obtenerEntidad(eq(1L))).thenReturn(producto);

        // Act & Assert
        assertThatThrownBy(() -> movimientoInventarioService.registrarSalida(salidaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(productoService).verificarDisponibilidad(1L, 100);
        verify(movimientoInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar salida: cantidad ≤ 0 debe lanzar excepción")
    void registrarSalidaCantidadInvalida_DeberiaLanzarExcepcion() {
        // Arrange
        salidaRequest.setCantidad(-5);

        // Act & Assert
        assertThatThrownBy(() -> movimientoInventarioService.registrarSalida(salidaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La cantidad debe ser mayor que cero");

        verify(productoService, never()).verificarDisponibilidad(any(), any());
        verify(movimientoInventarioRepository, never()).save(any());
    }
}

