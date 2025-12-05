package com.tuorg.veterinaria.prestacioneservicios.service;

import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaCancelarRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaReprogramarRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaResponse;
import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Pruebas unitarias para CitaService.
 * 
 * Verifica programar con hora en pasado, reprogramar/completar/cancelar
 * con estados inválidos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de CitaService")
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private com.tuorg.veterinaria.gestionpacientes.repository.HistoriaClinicaRepository historiaClinicaRepository;

    @Mock
    private com.tuorg.veterinaria.gestionpacientes.repository.RegistroMedicoRepository registroMedicoRepository;

    @InjectMocks
    private CitaService citaService;

    private CitaRequest citaRequest;
    private Paciente paciente;
    private UsuarioVeterinario veterinario;
    private Cita cita;

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

        citaRequest = new CitaRequest();
        citaRequest.setPacienteId(1L);
        citaRequest.setVeterinarioId(1L);
        citaRequest.setFechaHora(LocalDateTime.now().plusDays(1));
        citaRequest.setTipoServicio("Consulta");
        citaRequest.setMotivo("Revisión general");
        citaRequest.setTriageNivel("Normal");

        cita = new Cita();
        cita.setIdCita(1L);
        cita.setPaciente(paciente);
        cita.setVeterinario(veterinario);
        cita.setFechaHora(LocalDateTime.now().plusDays(1));
        cita.setTipoServicio("Consulta");
        cita.setMotivo("Revisión general");
        cita.setTriageNivel("Normal");
        cita.setEstado(AppConstants.ESTADO_CITA_PROGRAMADA);
    }

    @Test
    @DisplayName("Programar cita exitosa: debe crear cita en estado PROGRAMADA")
    void programarCitaExitoso_DeberiaCrearCitaProgramada() {
        // Arrange - Use 30-minute interval time
        LocalDateTime fechaValida = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        citaRequest.setFechaHora(fechaValida);
        cita.setFechaHora(fechaValida);
        
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));
        lenient().when(citaRepository.existeCitaEnRango(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), anyString()))
                .thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenReturn(cita);

        // Act
        CitaResponse response = citaService.programar(citaRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(AppConstants.ESTADO_CITA_PROGRAMADA);
        assertThat(response.getTipoServicio()).isEqualTo("Consulta");

        verify(pacienteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    @DisplayName("Programar cita: hora en pasado debe lanzar excepción")
    void programarCitaHoraPasado_DeberiaLanzarExcepcion() {
        // Arrange
        citaRequest.setFechaHora(LocalDateTime.now().minusHours(1));
        
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(veterinario));

        // Act & Assert
        assertThatThrownBy(() -> citaService.programar(citaRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha pasada");

        verify(pacienteRepository).findById(1L);
        verify(usuarioRepository).findById(1L);
    }

    @Test
    @DisplayName("Reprogramar cita: estado inválido debe lanzar excepción")
    void reprogramarCitaEstadoInvalido_DeberiaLanzarExcepcion() {
        // Arrange
        cita.setEstado(AppConstants.ESTADO_CITA_REALIZADA);
        CitaReprogramarRequest request = new CitaReprogramarRequest();
        request.setFechaHora(LocalDateTime.now().plusDays(2));

        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act & Assert
        assertThatThrownBy(() -> citaService.reprogramar(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo se pueden reprogramar citas en estado PROGRAMADA");

        verify(citaRepository).findById(1L);
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelar cita: cita ya realizada debe lanzar excepción")
    void cancelarCitaYaRealizada_DeberiaLanzarExcepcion() {
        // Arrange
        cita.setEstado(AppConstants.ESTADO_CITA_REALIZADA);
        CitaCancelarRequest request = new CitaCancelarRequest();
        request.setMotivo("Motivo de cancelación");

        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act & Assert
        assertThatThrownBy(() -> citaService.cancelar(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("No se puede cancelar una cita ya realizada");

        verify(citaRepository).findById(1L);
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Completar cita: estado inválido debe lanzar excepción")
    void completarCitaEstadoInvalido_DeberiaLanzarExcepcion() {
        // Arrange
        cita.setEstado(AppConstants.ESTADO_CITA_CANCELADA);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act & Assert
        assertThatThrownBy(() -> citaService.completar(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo se pueden completar citas en estado PROGRAMADA");

        verify(citaRepository).findById(1L);
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Completar cita exitosa: debe cambiar estado a REALIZADA")
    void completarCitaExitoso_DeberiaCambiarEstado() {
        // Arrange
        cita.setEstado(AppConstants.ESTADO_CITA_PROGRAMADA);
        Cita citaCompletada = new Cita();
        citaCompletada.setIdCita(1L);
        citaCompletada.setPaciente(paciente);
        citaCompletada.setVeterinario(veterinario);
        citaCompletada.setEstado(AppConstants.ESTADO_CITA_REALIZADA);

        // Mock HistoriaClinica
        com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica historiaClinica = 
            new com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica();
        historiaClinica.setIdHistoria(1L);
        historiaClinica.setPaciente(paciente);

        com.tuorg.veterinaria.gestionpacientes.model.RegistroMedico registroMedico = 
            new com.tuorg.veterinaria.gestionpacientes.model.RegistroMedico();
        registroMedico.setIdRegistro(1L);

        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));
        when(historiaClinicaRepository.findByPacienteId(1L)).thenReturn(Optional.of(historiaClinica));
        when(registroMedicoRepository.save(any())).thenReturn(registroMedico);
        when(citaRepository.save(any(Cita.class))).thenReturn(citaCompletada);

        // Act
        CitaResponse response = citaService.completar(1L, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(AppConstants.ESTADO_CITA_REALIZADA);

        verify(citaRepository).findById(1L);
        verify(citaRepository).save(any(Cita.class));
    }
}

