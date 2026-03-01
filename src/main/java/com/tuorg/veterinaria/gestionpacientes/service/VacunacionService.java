package com.tuorg.veterinaria.gestionpacientes.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Servicio para la gestión de vacunaciones.
 * 
 * Este servicio proporciona métodos para registrar vacunaciones
 * y programar próximas dosis.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class VacunacionService {

    /**
     * Repositorio de vacunaciones.
     */
    private final VacunacionRepository vacunacionRepository;

    /**
     * Repositorio de pacientes.
     */
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param vacunacionRepository Repositorio de vacunaciones
     * @param pacienteRepository   Repositorio de pacientes
     */
    @Autowired
    public VacunacionService(VacunacionRepository vacunacionRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository) {
        this.vacunacionRepository = vacunacionRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Registra una nueva vacunación.
     * 
     * @param vacunacion Vacunación a registrar
     * @return Vacunación creada
     */
    @Transactional
    public VacunacionResponse registrarVacuna(VacunacionRequest request) {
        Paciente paciente = pacienteRepository.findById(Objects.requireNonNull(request.getPacienteId()))
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", request.getPacienteId()));

        if (request.getFechaAplicacion().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de aplicación no puede ser futura");
        }

        UsuarioVeterinario veterinario = null;
        if (request.getVeterinarioId() != null) {
            Usuario usuario = usuarioRepository.findById(Objects.requireNonNull(request.getVeterinarioId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getVeterinarioId()));
            if (!(usuario instanceof UsuarioVeterinario)) {
                throw new BusinessException("El usuario indicado no corresponde a un veterinario");
            }
            veterinario = (UsuarioVeterinario) usuario;
        }

        Vacunacion vacunacion = new Vacunacion();
        vacunacion.setPaciente(paciente);
        vacunacion.setTipoVacuna(request.getTipoVacuna());
        vacunacion.setFechaAplicacion(request.getFechaAplicacion());
        vacunacion.setProximaDosis(request.getProximaDosis());
        vacunacion.setVeterinario(veterinario);

        Vacunacion guardada = vacunacionRepository.save(Objects.requireNonNull(vacunacion));
        return mapToResponse(guardada);
    }

    /**
     * Programa la próxima dosis de una vacunación.
     * 
     * @param vacunacionId ID de la vacunación
     * @param proximaDosis Fecha de la próxima dosis
     * @return Vacunación actualizada
     */
    @Transactional
    public VacunacionResponse programarProximaDosis(Long vacunacionId, ProgramarProximaDosisRequest request) {
        Vacunacion vacunacion = vacunacionRepository.findById(Objects.requireNonNull(vacunacionId))
                .orElseThrow(() -> new ResourceNotFoundException("Vacunacion", "id", vacunacionId));

        LocalDate proximaDosis = request.getProximaDosis();
        if (proximaDosis.isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de próxima dosis no puede ser pasada");
        }

        vacunacion.setProximaDosis(proximaDosis);
        Vacunacion actualizada = vacunacionRepository.save(Objects.requireNonNull(vacunacion));
        return mapToResponse(actualizada);
    }

    /**
     * Obtiene todas las vacunaciones de un paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Lista de vacunaciones
     */
    @Transactional(readOnly = true)
    public List<VacunacionResponse> obtenerPorPaciente(Long pacienteId) {
        return vacunacionRepository.findByPacienteId(Objects.requireNonNull(pacienteId))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene vacunaciones pendientes (próximas a vencer).
     * 
     * @param dias Días de anticipación para considerar pendiente
     * @return Lista de vacunaciones pendientes
     */
    @Transactional(readOnly = true)
    public List<VacunacionResponse> obtenerVacunacionesPendientes(int dias) {
        LocalDate fechaLimite = LocalDate.now().plusDays(dias);
        return vacunacionRepository.findVacunacionesPendientes(fechaLimite)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private VacunacionResponse mapToResponse(Vacunacion vacunacion) {
        Paciente paciente = vacunacion.getPaciente();
        VacunacionResponse.PacienteSummary pacienteSummary = new VacunacionResponse.PacienteSummary(
                Objects.requireNonNull(paciente.getIdPaciente()),
                paciente.getNombre());

        VacunacionResponse.VeterinarioSummary veterinarioSummary = null;
        if (vacunacion.getVeterinario() != null) {
            UsuarioVeterinario vet = vacunacion.getVeterinario();
            veterinarioSummary = new VacunacionResponse.VeterinarioSummary(
                    Objects.requireNonNull(vet.getIdUsuario()),
                    vet.getNombre(),
                    vet.getApellido(),
                    vet.getEspecialidad());
        }

        return new VacunacionResponse(
                Objects.requireNonNull(vacunacion.getIdVacunacion()),
                pacienteSummary,
                vacunacion.getTipoVacuna(),
                vacunacion.getFechaAplicacion(),
                vacunacion.getProximaDosis(),
                veterinarioSummary);
    }
}
