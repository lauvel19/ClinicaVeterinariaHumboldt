package com.tuorg.veterinaria.gestionpacientes.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.gestionpacientes.dto.PacienteRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.PacienteResponse;
import com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.HistoriaClinicaRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PacienteService.
 * 
 * Verifica validación de especie inválida, fecha futura, cliente inexistente
 * y mapeo a PacienteResponse.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de PacienteService")
@SuppressWarnings("null")
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private PacienteRequest pacienteRequest;
    private Cliente cliente;
    private Paciente pacienteGuardado;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdUsuario(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");

        pacienteRequest = new PacienteRequest();
        pacienteRequest.setNombre("Max");
        pacienteRequest.setEspecie("perro");
        pacienteRequest.setRaza("Labrador");
        pacienteRequest.setFechaNacimiento(LocalDate.of(2020, 1, 15));
        pacienteRequest.setSexo("M");
        pacienteRequest.setPesoKg(BigDecimal.valueOf(25.5));
        pacienteRequest.setEstadoSalud("Saludable");
        pacienteRequest.setClienteId(1L);

        pacienteGuardado = new Paciente();
        pacienteGuardado.setIdPaciente(1L);
        pacienteGuardado.setNombre("Max");
        pacienteGuardado.setEspecie("perro");
        pacienteGuardado.setRaza("Labrador");
        pacienteGuardado.setFechaNacimiento(LocalDate.of(2020, 1, 15));
        pacienteGuardado.setSexo("M");
        pacienteGuardado.setPesoKg(BigDecimal.valueOf(25.5));
        pacienteGuardado.setEstadoSalud("Saludable");
        pacienteGuardado.setCliente(cliente);
        pacienteGuardado.setIdentificadorExterno(UUID.randomUUID());
    }

    @Test
    @DisplayName("Registrar paciente exitoso: debe crear paciente e historia clínica")
    void registrarPacienteExitoso_DeberiaCrearPacienteEHistoriaClinica() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(pacienteGuardado);
        when(historiaClinicaRepository.save(any(HistoriaClinica.class))).thenReturn(new HistoriaClinica());

        // Act
        PacienteResponse response = pacienteService.registrarPaciente(pacienteRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Max");
        assertThat(response.getEspecie()).isEqualTo("perro");
        assertThat(response.getRaza()).isEqualTo("Labrador");

        verify(usuarioRepository).findById(1L);
        verify(pacienteRepository).save(any(Paciente.class));
        verify(historiaClinicaRepository).save(any(HistoriaClinica.class));
    }

    @Test
    @DisplayName("Registrar paciente: especie inválida debe lanzar excepción")
    void registrarPacienteEspecieInvalida_DeberiaLanzarExcepcion() {
        // Arrange
        pacienteRequest.setEspecie("conejo"); // Especie no válida

        // Act & Assert
        assertThatThrownBy(() -> pacienteService.registrarPaciente(pacienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La especie debe ser 'perro' o 'gato'");

        verify(usuarioRepository, never()).findById(any());
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar paciente: fecha de nacimiento futura debe lanzar excepción")
    void registrarPacienteFechaFutura_DeberiaLanzarExcepcion() {
        // Arrange
        pacienteRequest.setFechaNacimiento(LocalDate.now().plusDays(1)); // Fecha futura

        // Act & Assert
        assertThatThrownBy(() -> pacienteService.registrarPaciente(pacienteRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("La fecha de nacimiento no puede ser futura");

        verify(usuarioRepository, never()).findById(any());
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar paciente: cliente inexistente debe lanzar excepción")
    void registrarPacienteClienteInexistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());
        pacienteRequest.setClienteId(999L);

        // Act & Assert
        assertThatThrownBy(() -> pacienteService.registrarPaciente(pacienteRequest))
                .isInstanceOf(com.tuorg.veterinaria.common.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Cliente");

        verify(usuarioRepository).findById(999L);
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar paciente: debe mapear correctamente a PacienteResponse")
    void registrarPaciente_DeberiaMapearCorrectamente() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(pacienteGuardado);
        when(historiaClinicaRepository.save(any(HistoriaClinica.class))).thenReturn(new HistoriaClinica());

        // Act
        PacienteResponse response = pacienteService.registrarPaciente(pacienteRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Max");
        assertThat(response.getEspecie()).isEqualTo("perro");
        assertThat(response.getRaza()).isEqualTo("Labrador");
        assertThat(response.getCliente()).isNotNull();
        assertThat(response.getCliente().getNombre()).isEqualTo("Juan");
    }
}
