package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionusuarios.dto.ClienteRequest;
import com.tuorg.veterinaria.gestionusuarios.dto.ClienteResponse;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.repository.ClienteRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ClienteService.
 * 
 * Verifica que asigna rol CLIENTE, codifica contraseña y rechaza datos
 * inválidos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de ClienteService")
@SuppressWarnings("null")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequest clienteRequest;
    private Rol rolCliente;
    private Cliente clienteGuardado;

    @BeforeEach
    void setUp() {
        rolCliente = new Rol();
        rolCliente.setIdRol(1L);
        rolCliente.setNombreRol("CLIENTE");

        clienteRequest = new ClienteRequest();
        clienteRequest.setUsername("cliente1");
        clienteRequest.setPassword("password123");
        clienteRequest.setCorreo("cliente@example.com");
        clienteRequest.setNombre("Juan");
        clienteRequest.setApellido("Pérez");
        clienteRequest.setTelefono("1234567890");
        clienteRequest.setDireccion("Calle 123");
        clienteRequest.setDocumentoIdentidad("12345678");

        clienteGuardado = new Cliente();
        clienteGuardado.setIdUsuario(1L);
        clienteGuardado.setUsername("cliente1");
        clienteGuardado.setPasswordHash("encodedPassword");
        clienteGuardado.setCorreo("cliente@example.com");
        clienteGuardado.setNombre("Juan");
        clienteGuardado.setApellido("Pérez");
        clienteGuardado.setTelefono("1234567890");
        clienteGuardado.setDireccion("Calle 123");
        clienteGuardado.setDocumentoIdentidad("12345678");
        clienteGuardado.setRol(rolCliente);
        clienteGuardado.setActivo(true);
        clienteGuardado.setFechaRegistro(LocalDateTime.now());
    }

    @Test
    @DisplayName("Crear cliente exitoso: debe asignar rol CLIENTE y codificar contraseña")
    void crearClienteExitoso_DeberiaAsignarRolClienteYCodificarPassword() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(rolRepository.findByNombreRol("CLIENTE")).thenReturn(Optional.of(rolCliente));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        // Act
        ClienteResponse response = clienteService.crear(clienteRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("cliente1");
        assertThat(response.getCorreo()).isEqualTo("cliente@example.com");
        assertThat(response.getNombre()).isEqualTo("Juan");
        assertThat(response.getApellido()).isEqualTo("Pérez");

        verify(usuarioRepository).existsByUsername("cliente1");
        verify(usuarioRepository).existsByCorreo("cliente@example.com");
        verify(rolRepository).findByNombreRol("CLIENTE");
        verify(passwordEncoder).encode("password123");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Crear cliente: debe rechazar datos inválidos (username duplicado)")
    void crearClienteUsernameDuplicado_DeberiaRechazar() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.crear(clienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El nombre de usuario ya está en uso");

        verify(usuarioRepository).existsByUsername("cliente1");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente: debe rechazar datos inválidos (email duplicado)")
    void crearClienteEmailDuplicado_DeberiaRechazar() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> clienteService.crear(clienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El correo electrónico ya está en uso");

        verify(usuarioRepository).existsByUsername("cliente1");
        verify(usuarioRepository).existsByCorreo("cliente@example.com");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente: debe rechazar contraseña corta")
    void crearClienteContrasenaCorta_DeberiaRechazar() {
        // Arrange
        clienteRequest.setPassword("123"); // Contraseña muy corta

        // Act & Assert
        assertThatThrownBy(() -> clienteService.crear(clienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("contraseña");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente: rol CLIENTE no configurado debe lanzar excepción")
    void crearClienteRolNoConfigurado_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(rolRepository.findByNombreRol("CLIENTE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> clienteService.crear(clienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El rol CLIENTE no está configurado");

        verify(rolRepository).findByNombreRol("CLIENTE");
        verify(clienteRepository, never()).save(any());
    }
}
