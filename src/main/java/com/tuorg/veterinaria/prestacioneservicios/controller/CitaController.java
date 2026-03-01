package com.tuorg.veterinaria.prestacioneservicios.controller;

import com.tuorg.veterinaria.common.dto.ApiResponse;
import com.tuorg.veterinaria.common.dto.PageResponse;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaCancelarRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaResponse;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaReprogramarRequest;
import com.tuorg.veterinaria.prestacioneservicios.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * Controlador REST para la gestión de citas.
 * 
 * Este controlador expone endpoints para programar, reprogramar,
 * cancelar y consultar citas médicas con soporte de paginación.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/citas")
public class CitaController {

    /**
     * Servicio de gestión de citas.
     */
    private final CitaService citaService;
    private final com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param citaService       Servicio de citas
     * @param usuarioRepository Repositorio de usuarios
     */
    @Autowired
    public CitaController(
            CitaService citaService,
            com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository usuarioRepository) {
        this.citaService = citaService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Programa una nueva cita.
     * - CLIENTE: Solo puede agendar citas para sus propios pacientes
     * - VETERINARIO/SECRETARIO: Puede agendar citas para cualquier paciente
     * 
     * @param cita           Cita a programar
     * @param authentication Información del usuario autenticado
     * @return Respuesta con la cita creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'VETERINARIO', 'SECRETARIO')")
    public ResponseEntity<ApiResponse<CitaResponse>> programar(
            @RequestBody @Valid CitaRequest cita,
            Authentication authentication) {

        // Obtener el ID del usuario autenticado desde username
        String username = authentication.getName();
        Long userId = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getIdUsuario();

        // Determinar si es cliente para validar pertenencia del paciente
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        CitaResponse citaCreada = esCliente
                ? citaService.programar(cita, userId)
                : citaService.programar(cita);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cita programada exitosamente", citaCreada));
    }

    /**
     * Reprograma una cita existente.
     * - CLIENTE: Solo puede reprogramar sus propias citas
     * - VETERINARIO/SECRETARIO: Puede reprogramar cualquier cita
     * 
     * @param citaId         ID de la cita
     * @param request        Cuerpo con la nueva fecha y hora
     * @param authentication Información del usuario autenticado
     * @return Respuesta con la cita actualizada
     */
    @PutMapping("/{citaId}/reprogramar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'VETERINARIO', 'SECRETARIO')")
    public ResponseEntity<ApiResponse<CitaResponse>> reprogramar(
            @PathVariable Long citaId,
            @RequestBody @Valid CitaReprogramarRequest request,
            Authentication authentication) {

        // Obtener el ID del usuario autenticado desde username
        String username = authentication.getName();
        Long userId = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getIdUsuario();

        // Determinar si es cliente para validar pertenencia del paciente
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        CitaResponse cita = esCliente
                ? citaService.reprogramar(citaId, request, userId)
                : citaService.reprogramar(citaId, request);

        return ResponseEntity.ok(ApiResponse.success("Cita reprogramada exitosamente", cita));
    }

    /**
     * Cancela una cita.
     * - CLIENTE: Solo puede cancelar sus propias citas
     * - VETERINARIO/SECRETARIO: Puede cancelar cualquier cita
     * 
     * @param citaId         ID de la cita
     * @param request        Cuerpo con el motivo de cancelación
     * @param authentication Información del usuario autenticado
     * @return Respuesta con la cita cancelada
     */
    @PutMapping("/{citaId}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'VETERINARIO', 'SECRETARIO')")
    public ResponseEntity<ApiResponse<CitaResponse>> cancelar(
            @PathVariable Long citaId,
            @RequestBody @Valid CitaCancelarRequest request,
            Authentication authentication) {

        // Obtener el ID del usuario autenticado desde username
        String username = authentication.getName();
        Long userId = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getIdUsuario();

        // Determinar si es cliente para validar pertenencia del paciente
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

        CitaResponse cita = esCliente
                ? citaService.cancelar(citaId, request, userId)
                : citaService.cancelar(citaId, request);

        return ResponseEntity.ok(ApiResponse.success("Cita cancelada exitosamente", cita));
    }

    /**
     * Marca una cita como completada.
     * Solo veterinarios pueden marcar citas como completadas.
     * 
     * @param citaId ID de la cita
     * @return Respuesta con la cita completada
     */
    @PutMapping("/{citaId}/completar")
    @PreAuthorize("hasRole('VETERINARIO')")
    public ResponseEntity<ApiResponse<CitaResponse>> completar(@PathVariable Long citaId) {
        CitaResponse cita = citaService.completar(citaId);
        return ResponseEntity.ok(ApiResponse.success("Cita completada exitosamente", cita));
    }

    /**
     * Obtiene todas las citas.
     * Solo personal (veterinarios y secretarios) puede ver todas las citas.
     * 
     * @return Respuesta con la lista de todas las citas
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('VETERINARIO', 'SECRETARIO')")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> obtenerTodas() {
        List<CitaResponse> citas = citaService.obtenerTodas();
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", citas));
    }

    /**
     * Obtiene todas las citas de un paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Respuesta con la lista de citas
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> obtenerPorPaciente(@PathVariable Long pacienteId) {
        List<CitaResponse> citas = citaService.obtenerPorPaciente(pacienteId);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", citas));
    }

    /**
     * Obtiene todas las citas de un paciente con paginación.
     * 
     * @param pacienteId ID del paciente
     * @param page       Número de página (base 0)
     * @param size       Tamaño de página
     * @param sortBy     Campo por el que ordenar
     * @param direction  Dirección de ordenamiento (ASC o DESC)
     * @return Respuesta con la página de citas
     */
    @GetMapping("/paciente/{pacienteId}/paginado")
    public ResponseEntity<ApiResponse<PageResponse<CitaResponse>>> obtenerPorPacientePaginado(
            @PathVariable Long pacienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(Objects.requireNonNull(direction));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, Objects.requireNonNull(sortBy)));

        Page<CitaResponse> citasPage = citaService.obtenerPorPacientePaginado(pacienteId, pageable);
        PageResponse<CitaResponse> response = new PageResponse<>(citasPage);

        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", response));
    }

    /**
     * Obtiene todas las citas de un veterinario.
     * 
     * @param veterinarioId ID del veterinario
     * @return Respuesta con la lista de citas
     */
    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> obtenerPorVeterinario(@PathVariable Long veterinarioId) {
        List<CitaResponse> citas = citaService.obtenerPorVeterinario(veterinarioId);
        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", citas));
    }

    /**
     * Obtiene todas las citas de un veterinario con paginación.
     * 
     * @param veterinarioId ID del veterinario
     * @param page          Número de página (base 0)
     * @param size          Tamaño de página
     * @param sortBy        Campo por el que ordenar
     * @param direction     Dirección de ordenamiento (ASC o DESC)
     * @return Respuesta con la página de citas
     */
    @GetMapping("/veterinario/{veterinarioId}/paginado")
    public ResponseEntity<ApiResponse<PageResponse<CitaResponse>>> obtenerPorVeterinarioPaginado(
            @PathVariable Long veterinarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaHora") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort.Direction sortDirection = Sort.Direction.fromString(Objects.requireNonNull(direction));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, Objects.requireNonNull(sortBy)));

        Page<CitaResponse> citasPage = citaService.obtenerPorVeterinarioPaginado(veterinarioId, pageable);
        PageResponse<CitaResponse> response = new PageResponse<>(citasPage);

        return ResponseEntity.ok(ApiResponse.success("Citas obtenidas exitosamente", response));
    }

    /**
     * Verifica si una fecha y hora está disponible para un veterinario.
     * Cualquier usuario autenticado puede verificar disponibilidad.
     * 
     * @param veterinarioId ID del veterinario
     * @param fechaHora     Fecha y hora a verificar (formato: yyyy-MM-ddTHH:mm)
     * @return Respuesta indicando si está disponible
     */
    @GetMapping("/disponibilidad")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> verificarDisponibilidad(
            @RequestParam Long veterinarioId,
            @RequestParam String fechaHora) {
        java.time.LocalDateTime fechaHoraParsed = java.time.LocalDateTime.parse(Objects.requireNonNull(fechaHora));
        boolean disponible = citaService.verificarDisponibilidad(veterinarioId, fechaHoraParsed);
        return ResponseEntity.ok(ApiResponse.success(
                disponible ? "La fecha y hora están disponibles" : "La fecha y hora no están disponibles",
                disponible));
    }

    /**
     * Obtiene los horarios disponibles y ocupados para un veterinario en una fecha
     * específica.
     * 
     * @param veterinarioId ID del veterinario
     * @param fecha         Fecha en formato YYYY-MM-DD
     * @return Lista de horarios con su estado de disponibilidad
     */
    @GetMapping("/horarios-disponibles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse>>> obtenerHorariosDelDia(
            @RequestParam Long veterinarioId,
            @RequestParam String fecha) {
        java.time.LocalDate fechaParsed = java.time.LocalDate.parse(Objects.requireNonNull(fecha));
        List<com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse> horarios = citaService
                .obtenerHorariosDelDia(veterinarioId, fechaParsed);
        return ResponseEntity.ok(ApiResponse.success("Horarios obtenidos exitosamente", horarios));
    }
}
