package com.tuorg.veterinaria.gestionfacturacion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.gestionfacturacion.dto.FacturaRequest;
import com.tuorg.veterinaria.gestionfacturacion.dto.FacturaResponse;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Pruebas unitarias para FacturaService.
 * 
 * Verifica totales negativos, cliente inexistente y mapeo de líneas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de FacturaService")
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FacturaService facturaService;

    private FacturaRequest facturaRequest;
    private Cliente cliente;
    private Factura facturaGuardada;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdUsuario(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setCorreo("juan@example.com");
        cliente.setTelefono("1234567890");

        facturaRequest = new FacturaRequest();
        facturaRequest.setClienteId(1L);
        facturaRequest.setTotal(BigDecimal.valueOf(150.00));
        facturaRequest.setFormaPago("Efectivo");
        
        Map<String, Object> contenido = new HashMap<>();
        contenido.put("servicios", "Consulta veterinaria");
        contenido.put("subtotal", 150.00);
        facturaRequest.setContenido(contenido);

        facturaGuardada = new Factura();
        facturaGuardada.setIdFactura(1L);
        facturaGuardada.setNumero("FACT-20250101-0001");
        facturaGuardada.setCliente(cliente);
        facturaGuardada.setTotal(BigDecimal.valueOf(150.00));
        facturaGuardada.setFormaPago("Efectivo");
    }

    @Test
    @DisplayName("Crear factura exitosa: debe mapear correctamente")
    void crearFacturaExitoso_DeberiaMapearCorrectamente() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);
        try {
            lenient().when(objectMapper.writeValueAsString(any())).thenReturn("{\"servicios\":\"Consulta veterinaria\"}");
            lenient().when(objectMapper.readValue(anyString(), eq(Map.class))).thenReturn(facturaRequest.getContenido());
        } catch (Exception e) {
            // Ignorar
        }

        // Act
        FacturaResponse response = facturaService.crear(facturaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotal()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(response.getCliente()).isNotNull();
        assertThat(response.getCliente().getNombreCompleto()).contains("Juan");

        verify(usuarioRepository).findById(1L);
        verify(facturaRepository).save(any(Factura.class));
    }

    @Test
    @DisplayName("Crear factura: total negativo debe lanzar excepción")
    void crearFacturaTotalNegativo_DeberiaLanzarExcepcion() {
        // Arrange
        facturaRequest.setTotal(BigDecimal.valueOf(-50.00));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(facturaRepository.findByNumero(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> facturaService.crear(facturaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El total de la factura debe ser mayor o igual a cero");

        verify(usuarioRepository).findById(1L);
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear factura: cliente inexistente debe lanzar excepción")
    void crearFacturaClienteInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        facturaRequest.setClienteId(999L);

        // Act & Assert
        assertThatThrownBy(() -> facturaService.crear(facturaRequest))
                .isInstanceOf(com.tuorg.veterinaria.common.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Cliente");

        verify(usuarioRepository).findById(999L);
        verify(facturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear factura: usuario no es cliente debe lanzar excepción")
    void crearFacturaUsuarioNoCliente_DeberiaLanzarExcepcion() {
        // Arrange
        Usuario usuarioNoCliente = new Usuario();
        usuarioNoCliente.setIdUsuario(2L);
        usuarioNoCliente.setNombre("Usuario Normal");

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioNoCliente));
        facturaRequest.setClienteId(2L);

        // Act & Assert
        assertThatThrownBy(() -> facturaService.crear(facturaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no corresponde a un cliente registrado");

        verify(usuarioRepository).findById(2L);
        verify(facturaRepository, never()).save(any());
    }
}

