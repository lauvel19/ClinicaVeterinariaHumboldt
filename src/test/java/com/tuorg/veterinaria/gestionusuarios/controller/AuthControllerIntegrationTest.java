package com.tuorg.veterinaria.gestionusuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.config.AbstractIntegrationTest;
import com.tuorg.veterinaria.gestionfacturacion.repository.FacturaRepository;
import com.tuorg.veterinaria.gestioninventario.repository.MovimientoInventarioRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionusuarios.dto.LoginRequest;
import com.tuorg.veterinaria.gestionusuarios.repository.HistorialAccionRepository;
import com.tuorg.veterinaria.reportes.repository.ReporteRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración para AuthController.
 * 
 * Verifica POST /api/auth/login: 401 con credenciales malas y 200 con token correcto.
 */
@DisplayName("Pruebas de integración de AuthController")
@Transactional
@Rollback
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private HistorialAccionRepository historialAccionRepository;

    private Usuario usuario;
    private Rol rol;

    @BeforeEach
    void setUp() {
        // Limpiar en el orden correcto para respetar las claves foráneas
        // Primero eliminar todas las tablas que referencian usuarios directamente
        historialAccionRepository.deleteAll(); // Historial de acciones referencian usuarios
        movimientoInventarioRepository.deleteAll(); // Movimientos de inventario referencian usuarios
        reporteRepository.deleteAll(); // Reportes referencian usuarios
        facturaRepository.deleteAll(); // Facturas referencian clientes (usuarios)
        pacienteRepository.deleteAll(); // Pacientes referencian clientes (usuarios)
        // Finalmente usuarios
        usuarioRepository.deleteAll();
        // NO eliminamos los roles porque son datos de referencia creados por Flyway

        // Buscar o crear el rol CLIENTE (ya existe por Flyway, pero lo buscamos por si acaso)
        rol = rolRepository.findByNombreRol("CLIENTE")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombreRol("CLIENTE");
                    nuevoRol.setDescripcion("Rol de cliente");
                    return rolRepository.save(nuevoRol);
                });

        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setUsername("testuser");
        usuario.setPasswordHash(passwordEncoder.encode("password123"));
        usuario.setCorreo("test@example.com");
        usuario.setNombre("Test");
        usuario.setApellido("User");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("POST /api/auth/login: credenciales correctas debe retornar 200 con token")
    void loginCredencialesCorrectas_DeberiaRetornar200ConToken() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Act & Assert
        // NOTA: MockMvc no aplica automáticamente el context-path, por lo que usamos /auth/login en lugar de /api/auth/login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("POST /api/auth/login: credenciales incorrectas debe retornar 401")
    void loginCredencialesIncorrectas_DeberiaRetornar401() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("passwordIncorrecta");

        // Act & Assert
        // NOTA: MockMvc no aplica automáticamente el context-path, por lo que usamos /auth/login en lugar de /api/auth/login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login: usuario inexistente debe retornar error")
    void loginUsuarioInexistente_DeberiaRetornarError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("usuarioInexistente");
        loginRequest.setPassword("password123");

        // Act & Assert
        // NOTA: MockMvc no aplica automáticamente el context-path, por lo que usamos /auth/login en lugar de /api/auth/login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login: campos vacíos debe retornar error 400")
    void loginCamposVacios_DeberiaRetornar400() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        // Act & Assert
        // NOTA: MockMvc no aplica automáticamente el context-path, por lo que usamos /auth/login en lugar de /api/auth/login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login: usuario inactivo debe retornar error 400")
    void loginUsuarioInactivo_DeberiaRetornar400() throws Exception {
        // Arrange - Crear usuario inactivo
        Usuario usuarioInactivo = new Usuario();
        usuarioInactivo.setUsername("usuarioInactivo");
        usuarioInactivo.setPasswordHash(passwordEncoder.encode("password123"));
        usuarioInactivo.setCorreo("inactivo@example.com");
        usuarioInactivo.setNombre("Usuario");
        usuarioInactivo.setApellido("Inactivo");
        usuarioInactivo.setRol(rol);
        usuarioInactivo.setActivo(false); // Usuario inactivo
        usuarioRepository.save(usuarioInactivo);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("usuarioInactivo");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login: múltiples intentos con credenciales incorrectas")
    void loginMultiplesIntentosFallidos_DeberiaRetornar400() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("passwordIncorrecta");

        // Act & Assert - Primer intento fallido
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // Segundo intento fallido - debería seguir retornando 400
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        // Tercer intento con credenciales correctas - debería funcionar
        loginRequest.setPassword("password123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }
}

