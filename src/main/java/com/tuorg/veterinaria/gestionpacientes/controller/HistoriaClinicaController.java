package com.tuorg.veterinaria.gestionpacientes.controller;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.HistoriaClinicaResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.RegistroMedicoRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.RegistroMedicoResponse;
import com.tuorg.veterinaria.gestionpacientes.service.HistoriaClinicaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de historias clínicas.
 * 
 * Este controlador expone endpoints para consultar historias clínicas,
 * agregar registros médicos y exportar información.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/historias-clinicas")
public class HistoriaClinicaController {

    /**
     * Servicio de gestión de historias clínicas.
     */
    private final HistoriaClinicaService historiaClinicaService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param historiaClinicaService Servicio de historias clínicas
     */
    @Autowired
    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService) {
        this.historiaClinicaService = historiaClinicaService;
    }

    /**
     * Obtiene la historia clínica de un paciente.
     * Accesible por veterinarios, secretarios y el dueño del paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Respuesta con la historia clínica
     */
    @GetMapping("/paciente/{pacienteId}")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HistoriaClinicaResponse>> obtenerPorPaciente(@PathVariable Long pacienteId) {
        HistoriaClinicaResponse historia = historiaClinicaService.obtenerPorPaciente(pacienteId);
        return ResponseEntity.ok(ApiResponse.success("Historia clínica obtenida exitosamente", historia));
    }

    /**
     * Agrega un registro médico a una historia clínica.
     * DESHABILITADO: Los registros se crean automáticamente al completar consultas.
     * Solo ADMIN puede agregar registros manualmente en casos excepcionales.
     * 
     * @param historiaId ID de la historia clínica
     * @param registro Registro médico a agregar
     * @return Respuesta con el registro médico creado
     */
    @PostMapping("/{historiaId}/registros")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RegistroMedicoResponse>> agregarRegistro(
            @PathVariable Long historiaId,
            @Valid @RequestBody RegistroMedicoRequest request) {
        RegistroMedicoResponse registroCreado = historiaClinicaService.agregarRegistro(historiaId, request);
        return ResponseEntity.ok(ApiResponse.success("Registro médico agregado exitosamente", registroCreado));
    }

    /**
     * Obtiene todos los registros médicos de una historia clínica.
     * Accesible por veterinarios, secretarios y el dueño del paciente.
     * 
     * @param historiaId ID de la historia clínica
     * @return Respuesta con la lista de registros médicos
     */
    @GetMapping("/{historiaId}/registros")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RegistroMedicoResponse>>> obtenerRegistros(@PathVariable Long historiaId) {
        List<RegistroMedicoResponse> registros = historiaClinicaService.obtenerRegistros(historiaId);
        return ResponseEntity.ok(ApiResponse.success("Registros médicos obtenidos exitosamente", registros));
    }

    /**
     * Actualiza un registro médico existente.
     * RESTRINGIDO: Solo ADMIN puede editar registros médicos históricos para corregir errores.
     * Los registros médicos deben ser inmutables una vez creados por razones legales.
     * 
     * @param registroId ID del registro médico
     * @param request Datos actualizados del registro
     * @return Respuesta con el registro actualizado
     */
    @PutMapping("/registros/{registroId}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RegistroMedicoResponse>> actualizarRegistro(
            @PathVariable Long registroId,
            @Valid @RequestBody RegistroMedicoRequest request) {
        RegistroMedicoResponse registroActualizado = historiaClinicaService.actualizarRegistro(registroId, request);
        return ResponseEntity.ok(ApiResponse.success("Registro médico actualizado exitosamente", registroActualizado));
    }

    /**
     * Exporta la historia clínica como PDF.
     * 
     * @param historiaId ID de la historia clínica
     * @return Respuesta con el PDF en bytes
     */
    @GetMapping("/{historiaId}/exportar-pdf")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportarPDF(@PathVariable Long historiaId) {
        byte[] pdf = historiaClinicaService.exportarPDF(historiaId);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=historia_clinica_" + historiaId + ".pdf")
                .body(pdf);
    }
}

