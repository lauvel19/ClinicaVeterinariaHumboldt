package com.tuorg.veterinaria.gestionpacientes.service;

import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestionpacientes.dto.DesparasitacionRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.DesparasitacionResponse;
import com.tuorg.veterinaria.gestionpacientes.model.Desparasitacion;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.DesparasitacionRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Servicio para la gestión de desparasitaciones.
 * 
 * Este servicio proporciona métodos para registrar y consultar
 * desparasitaciones.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class DesparasitacionService {

    /**
     * Repositorio de desparasitaciones.
     */
    private final DesparasitacionRepository desparasitacionRepository;

    /**
     * Repositorio de pacientes.
     */
    private final PacienteRepository pacienteRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param desparasitacionRepository Repositorio de desparasitaciones
     * @param pacienteRepository        Repositorio de pacientes
     */
    @Autowired
    public DesparasitacionService(DesparasitacionRepository desparasitacionRepository,
            PacienteRepository pacienteRepository) {
        this.desparasitacionRepository = desparasitacionRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Registra una nueva desparasitación.
     * 
     * @param request Datos de la desparasitación
     * @return Desparasitación registrada
     */
    @Transactional
    public DesparasitacionResponse registrar(DesparasitacionRequest request) {
        Paciente paciente = pacienteRepository.findById(Objects.requireNonNull(request.getPacienteId()))
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", request.getPacienteId()));

        Desparasitacion desparasitacion = new Desparasitacion();
        desparasitacion.setPaciente(paciente);
        desparasitacion.setProductoUsado(request.getProductoUsado());
        desparasitacion.setFechaAplicacion(request.getFechaAplicacion());
        desparasitacion.setProximaAplicacion(request.getProximaAplicacion());

        Desparasitacion guardada = desparasitacionRepository.save(Objects.requireNonNull(desparasitacion));
        return mapToResponse(guardada);
    }

    /**
     * Obtiene todas las desparasitaciones de un paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Lista de desparasitaciones
     */
    @Transactional(readOnly = true)
    public List<DesparasitacionResponse> obtenerPorPaciente(Long pacienteId) {
        return desparasitacionRepository.findByPacienteIdPaciente(Objects.requireNonNull(pacienteId))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene desparasitaciones pendientes.
     * 
     * @param dias Días de anticipación (por defecto 30)
     * @return Lista de desparasitaciones pendientes
     */
    @Transactional(readOnly = true)
    public List<DesparasitacionResponse> obtenerDesparasitacionesPendientes(int dias) {
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(dias);

        return desparasitacionRepository.findByProximaAplicacionBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene todas las desparasitaciones.
     * 
     * @return Lista de desparasitaciones
     */
    @Transactional(readOnly = true)
    public List<DesparasitacionResponse> obtenerTodas() {
        return desparasitacionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private DesparasitacionResponse mapToResponse(Desparasitacion desparasitacion) {
        Paciente paciente = desparasitacion.getPaciente();
        return DesparasitacionResponse.builder()
                .idDesparasitacion(Objects.requireNonNull(desparasitacion.getIdDesparasitacion()))
                .paciente(DesparasitacionResponse.PacienteSummary.builder()
                        .id(Objects.requireNonNull(paciente.getIdPaciente()))
                        .nombre(paciente.getNombre())
                        .build())
                .productoUsado(desparasitacion.getProductoUsado())
                .fechaAplicacion(desparasitacion.getFechaAplicacion())
                .proximaAplicacion(desparasitacion.getProximaAplicacion())
                .build();
    }
}
