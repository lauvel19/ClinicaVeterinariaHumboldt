package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.config.security.JwtTokenProvider;
import com.tuorg.veterinaria.gestionusuarios.dto.LoginResponse;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AuthService.
 * 
 * Verifica la autenticación de usuarios, generación de tokens JWT
 * y actualización del último acceso.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de AuthService")
@SuppressWarnings("null")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private Rol rol;
    private UserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        rol = new Rol();
        rol.setIdRol(1L);
        rol.setNombreRol("CLIENTE");

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("testuser");
        usuario.setPasswordHash("encodedPassword");
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setCorreo("test@example.com");
        usuario.setActivo(true);
        usuario.setRol(rol);
        usuario.setUltimoAcceso(null);

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("encodedPassword")
                .authorities("ROLE_CLIENTE")
                .build();

        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("Login exitoso: debe generar token y actualizar último acceso")
    void loginExitoso_DeberiaGenerarTokenYActualizarUltimoAcceso() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        String token = "jwt-token-123";

        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(tokenProvider.generateToken(userDetails)).thenReturn(token);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        LoginResponse response = authService.login(username, password);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUsuario()).isNotNull();
        assertThat(response.getUsuario().getNombre()).isEqualTo("Test");

        verify(usuarioRepository).findByUsername(username);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(userDetails);
        verify(usuarioRepository).save(usuario);
        assertThat(usuario.getUltimoAcceso()).isNotNull();
    }

    @Test
    @DisplayName("Login fallido: usuario no encontrado")
    void loginUsuarioNoEncontrado_DeberiaLanzarExcepcion() {
        // Arrange
        String username = "usuarioInexistente";
        String password = "password123";

        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository).findByUsername(username);
        verify(authenticationManager, never()).authenticate(any());
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login fallido: usuario inactivo")
    void loginUsuarioInactivo_DeberiaLanzarExcepcion() {
        // Arrange
        String username = "testuser";
        String password = "password123";
        usuario.setActivo(false);

        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuario inactivo");

        verify(usuarioRepository).findByUsername(username);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    @DisplayName("Login fallido: credenciales inválidas")
    void loginCredencialesInvalidas_DeberiaLanzarExcepcion() {
        // Arrange
        String username = "testuser";
        String password = "passwordIncorrecta";

        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(username, password))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Credenciales inválidas");

        verify(usuarioRepository).findByUsername(username);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, never()).generateToken(any());
        verify(usuarioRepository, never()).save(any());
    }
}
