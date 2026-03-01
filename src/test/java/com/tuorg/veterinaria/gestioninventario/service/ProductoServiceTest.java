package com.tuorg.veterinaria.gestioninventario.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoRequest;
import com.tuorg.veterinaria.gestioninventario.dto.ProductoResponse;
import com.tuorg.veterinaria.gestioninventario.model.Producto;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ProductoService.
 * 
 * Verifica SKU duplicado, stock negativo y mapToResponse.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ProductoService")
@SuppressWarnings("null")
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private ProductoRequest productoRequest;
    private Producto productoGuardado;

    @BeforeEach
    void setUp() {
        productoRequest = new ProductoRequest();
        productoRequest.setSku("PROD-001");
        productoRequest.setNombre("Medicamento A");
        productoRequest.setDescripcion("Descripción del medicamento");
        productoRequest.setTipo("medicamento");
        productoRequest.setPrecioUnitario(BigDecimal.valueOf(50.00));
        productoRequest.setUm("unidad");
        productoRequest.setStock(100);

        productoGuardado = new Producto();
        productoGuardado.setIdProducto(1L);
        productoGuardado.setSku("PROD-001");
        productoGuardado.setNombre("Medicamento A");
        productoGuardado.setDescripcion("Descripción del medicamento");
        productoGuardado.setTipo("medicamento");
        productoGuardado.setPrecioUnitario(BigDecimal.valueOf(50.00));
        productoGuardado.setUm("unidad");
        productoGuardado.setStock(100);
    }

    @Test
    @DisplayName("Crear producto exitoso: debe mapear correctamente")
    void crearProductoExitoso_DeberiaMapearCorrectamente() {
        // Arrange
        when(productoRepository.existsBySku(anyString())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        // Act
        ProductoResponse response = productoService.crear(productoRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("PROD-001");
        assertThat(response.getNombre()).isEqualTo("Medicamento A");
        assertThat(response.getStock()).isEqualTo(100);
        assertThat(response.getPrecioUnitario()).isEqualTo(BigDecimal.valueOf(50.00));

        verify(productoRepository).existsBySku("PROD-001");
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("Crear producto: SKU duplicado debe lanzar excepción")
    void crearProductoSkuDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoRepository.existsBySku(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productoService.crear(productoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El SKU ya está en uso");

        verify(productoRepository).existsBySku("PROD-001");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear producto: stock negativo debe lanzar excepción")
    void crearProductoStockNegativo_DeberiaLanzarExcepcion() {
        // Arrange
        productoRequest.setStock(-10);
        when(productoRepository.existsBySku(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productoService.crear(productoRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El stock inicial no puede ser negativo");

        verify(productoRepository).existsBySku("PROD-001");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener producto: debe mapear correctamente a ProductoResponse")
    void obtenerProducto_DeberiaMapearCorrectamente() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoGuardado));

        // Act
        ProductoResponse response = productoService.obtener(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("PROD-001");
        assertThat(response.getNombre()).isEqualTo("Medicamento A");
        assertThat(response.getStock()).isEqualTo(100);

        verify(productoRepository).findById(1L);
    }
}
