package com.tuorg.veterinaria.gestionpacientes.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionpacientes.dto.ProgramarProximaDosisRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.VacunacionRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.VacunacionResponse;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.model.Vacunacion;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.VacunacionRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para VacunacionService.
 * 
 * Verifica que impide fecha futura o veterinario no válido
 * y comprueba mapToResponse.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de VacunacionService")
@SuppressWarnings("null")
class VacunacionServiceTest {

    @Mock
    private VacunacionRepository vacunacionRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VacunacionService vacunacionService;

    private VacunacionRequest vacunacionRequest;
    private Paciente paciente;
    private UsuarioVeterinario veterinario;
    private Vacunacion vacunacionGuardada;

    @BeforeEach
    void setUp() {
        paciente = new Paciente();
        paciente.setIdPaciente(1L);
        paciente.setNombre("Max");

        veterinario = new UsuarioVeterinario();
        veterinario.setIdUsuario(1L);
        veterinario.setNombre("Dr. Juan");
        veterinario.setApellido("Veterinario");
        veterinario.setEspecialidad("General");

        vacunacionRequest = new VacunacionRequest();
        vacunacionRequest.setPacienteId(1L);
        vacunacionRequest.setTipoVacuna("Rabia");
        vacunacionRequest.setFechaAplicacion(LocalDate.now().minusDays(1));
        vacunacionRequest.setVeterinarioId(1L);

        vacunacionGuardada = new Vacunacion();
        vacunacionGuardada.setIdVacunacion(1L);
        vacunacionGuardada.setPaciente(paciente);
        vacunacionGuardada.setTipoVacuna("Rabia");
        vacunacionGuardada.setFechaAplicacion(LocalDate.now().minusDays(1));
        vacunacionGuardada.setVeterinario(veterinario);
    }

    @Test
    @DisplayName("Registrar vacunación exitosa: debe mapear correctamente")
    void registrarVacunacionExitoso_DeberiaMapearCorrectamente() {
        // Arrange
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        when(vacunacionRepository.save(any(Vacunacion.class))).thenReturn(vacunacionGuardada);

        // Act
        VacunacionResponse response = vacunacionService.registrarVacuna(vacunacionRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTipoVacuna()).isEqualTo("Rabia");
        assertThat(response.getPaciente()).isNotNull();
        assertThat(response.getPaciente().getNombre()).isEqualTo("Max");
        assertThat(response.getVeterinario()).isNotNull();
        assertThat(response.getVeterinario().getNombre()).isEqualTo("Dr. Juan");

        verify(pacienteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(vacunacionRepository).save(any(Vacunacion.class));
    }

    @Test
    @DisplayName("Registrar vacunación: fecha futura debe lanzar excepción")
    void registrarVacunacionFechaFutura_DeberiaLanzarExcepcion() {
        // Arrange
        vacunacionRequest.setFechaAplicacion(LocalDate.now().plusDays(1)); // Fecha futura
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        // Act & Assert
        assertThatThrownBy(() -> vacunacionService.registrarVacuna(vacunacionRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La fecha de aplicación no puede ser futura");

        verify(pacienteRepository).findById(1L);
        verify(vacunacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar vacunación: veterinario no válido debe lanzar excepción")
    void registrarVacunacionVeterinarioNoValido_DeberiaLanzarExcepcion() {
        // Arrange
        Usuario usuarioNoVeterinario = new Usuario();
        usuarioNoVeterinario.setIdUsuario(2L);
        usuarioNoVeterinario.setNombre("Usuario Normal");

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioNoVeterinario));
        vacunacionRequest.setVeterinarioId(2L);

        // Act & Assert
        assertThatThrownBy(() -> vacunacionService.registrarVacuna(vacunacionRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no corresponde a un veterinario");

        verify(pacienteRepository).findById(1L);
        verify(usuarioRepository).findById(2L);
        verify(vacunacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Programar próxima dosis: fecha pasada debe lanzar excepción")
    void programarProximaDosisFechaPasada_DeberiaLanzarExcepcion() {
        // Arrange
        ProgramarProximaDosisRequest request = new ProgramarProximaDosisRequest();
        request.setProximaDosis(LocalDate.now().minusDays(1)); // Fecha pasada

        when(vacunacionRepository.findById(1L)).thenReturn(Optional.of(vacunacionGuardada));

        // Act & Assert
        assertThatThrownBy(() -> vacunacionService.programarProximaDosis(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La fecha de próxima dosis no puede ser pasada");

        verify(vacunacionRepository).findById(1L);
        verify(vacunacionRepository, never()).save(any());
    }
}
