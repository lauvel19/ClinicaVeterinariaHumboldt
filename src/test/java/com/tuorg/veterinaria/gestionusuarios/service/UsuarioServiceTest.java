package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestionusuarios.dto.UsuarioRequest;
import com.tuorg.veterinaria.gestionusuarios.dto.UsuarioResponse;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioVeterinarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para UsuarioService.
 * 
 * Verifica reglas de negocio (usuario duplicado, email en uso, contraseña
 * corta)
 * y mapeo DTO → entidad correctamente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de UsuarioService")
@SuppressWarnings("null")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioVeterinarioRepository veterinarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioRequest usuarioRequest;
    private Rol rol;
    private Usuario usuarioGuardado;

    @BeforeEach
    void setUp() {
        rol = new Rol();
        rol.setIdRol(1L);
        rol.setNombreRol("VETERINARIO");
        rol.setDescripcion("Rol de veterinario");
        rol.setPermisos(new HashSet<>());

        usuarioRequest = new UsuarioRequest();
        usuarioRequest.setUsername("nuevousuario");
        usuarioRequest.setPassword("password123");
        usuarioRequest.setCorreo("nuevo@example.com");
        usuarioRequest.setNombre("Nuevo");
        usuarioRequest.setApellido("Usuario");
        usuarioRequest.setTelefono("1234567890");
        usuarioRequest.setRolId(1L);
        usuarioRequest.setActivo(true);

        usuarioGuardado = new Usuario();
        usuarioGuardado.setIdUsuario(1L);
        usuarioGuardado.setUsername("nuevousuario");
        usuarioGuardado.setPasswordHash("encodedPassword");
        usuarioGuardado.setCorreo("nuevo@example.com");
        usuarioGuardado.setNombre("Nuevo");
        usuarioGuardado.setApellido("Usuario");
        usuarioGuardado.setTelefono("1234567890");
        usuarioGuardado.setRol(rol);
        usuarioGuardado.setActivo(true);
    }

    @Test
    @DisplayName("Crear usuario exitoso: debe validar y mapear correctamente")
    void crearUsuarioExitoso_DeberiaValidarYMapearCorrectamente() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(false);
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        // Act
        UsuarioResponse response = usuarioService.crear(usuarioRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("nuevousuario");
        assertThat(response.getCorreo()).isEqualTo("nuevo@example.com");
        assertThat(response.getNombre()).isEqualTo("Nuevo");
        assertThat(response.getApellido()).isEqualTo("Usuario");

        verify(usuarioRepository).existsByUsername("nuevousuario");
        verify(usuarioRepository).existsByCorreo("nuevo@example.com");
        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Crear usuario: username duplicado debe lanzar excepción")
    void crearUsuarioUsernameDuplicado_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.crear(usuarioRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El nombre de usuario ya está en uso");

        verify(usuarioRepository).existsByUsername("nuevousuario");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear usuario: email en uso debe lanzar excepción")
    void crearUsuarioEmailEnUso_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.crear(usuarioRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("El correo electrónico ya está en uso");

        verify(usuarioRepository).existsByUsername("nuevousuario");
        verify(usuarioRepository).existsByCorreo("nuevo@example.com");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear usuario: contraseña corta debe lanzar excepción")
    void crearUsuarioContrasenaCorta_DeberiaLanzarExcepcion() {
        // Arrange
        usuarioRequest.setPassword("123"); // Contraseña muy corta

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.crear(usuarioRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("contraseña");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Obtener usuario: debe mapear correctamente a DTO")
    void obtenerUsuario_DeberiaMapearCorrectamente() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioGuardado));

        // Act
        UsuarioResponse response = usuarioService.obtener(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("nuevousuario");
        assertThat(response.getRol()).isNotNull();
        assertThat(response.getRol().getNombre()).isEqualTo("VETERINARIO");

        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("Obtener usuario: usuario no encontrado debe lanzar excepción")
    void obtenerUsuarioNoEncontrado_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.obtener(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario");

        verify(usuarioRepository).findById(999L);
    }
}
