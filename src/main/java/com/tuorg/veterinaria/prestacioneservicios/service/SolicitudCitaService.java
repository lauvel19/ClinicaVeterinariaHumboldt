package com.tuorg.veterinaria.prestacioneservicios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.prestacioneservicios.dto.SolicitudCitaRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.SolicitudCitaResponse;
import com.tuorg.veterinaria.prestacioneservicios.model.SolicitudCita;
import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import com.tuorg.veterinaria.prestacioneservicios.repository.SolicitudCitaRepository;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.gestioninventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Servicio para gestión de solicitudes de cita del portal del cliente.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitudCitaService {

    private final SolicitudCitaRepository solicitudCitaRepository;
    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaService citaService;
    private final ProductoRepository productoRepository;

    /**
     * Crea una nueva solicitud de cita.
     * 
     * VALIDACIONES CRÍTICAS:
     * 1. Cliente no puede tener otra solicitud pendiente/aprobada
     * 2. Validar que la fecha no sea en el pasado
     * 3. Validar anticipación mínima de 2 horas
     */
    @Transactional
    public SolicitudCitaResponse crear(SolicitudCitaRequest request, Long clienteId) {
        log.info("📝 Creando solicitud de cita para cliente: {}", clienteId);

        // Validar cliente
        Cliente cliente = (Cliente) usuarioRepository.findById(Objects.requireNonNull(clienteId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        // 🔴 VALIDACIÓN CRÍTICA 1: Cliente no puede tener solicitudes pendientes o
        // aprobadas
        long solicitudesActivas = solicitudCitaRepository
                .findByClienteId(clienteId)
                .stream()
                .filter(s -> AppConstants.ESTADO_SOLICITUD_PENDIENTE.equals(s.getEstado())
                        || AppConstants.ESTADO_SOLICITUD_APROBADA.equals(s.getEstado()))
                .count();

        if (solicitudesActivas > 0) {
            throw new BusinessException(
                    "Cliente ya tiene una solicitud pendiente o aprobada. " +
                            "Debe resolver la solicitud actual antes de crear una nueva.");
        }

        // Validar paciente
        Paciente paciente = pacienteRepository.findById(Objects.requireNonNull(request.getPacienteId()))
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", request.getPacienteId()));

        // Validar que el paciente pertenece al cliente
        if (!paciente.getCliente().getIdPersona().equals(clienteId)) {
            throw new BusinessException("El paciente no pertenece al cliente");
        }

        // Validar que la fecha no sea pasada
        if (request.getFechaSolicitada().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de la cita no puede ser en el pasado");
        }

        // 🔴 VALIDACIÓN CRÍTICA 2: Validar anticipación mínima
        LocalDateTime fechaHoraSolicitada = LocalDateTime.of(
                request.getFechaSolicitada(),
                request.getHoraSolicitada());
        LocalDateTime anticipacionMinima = LocalDateTime.now().plusHours(AppConstants.ANTICIPACION_MINIMA_HORAS);
        if (fechaHoraSolicitada.isBefore(anticipacionMinima)) {
            throw new BusinessException(
                    "Debe solicitar la cita con al menos " + AppConstants.ANTICIPACION_MINIMA_HORAS +
                            " horas de anticipación");
        }

        // 🔴 VALIDACIÓN CRÍTICA 4: Validar disponibilidad de stock
        long productosDisponibles = productoRepository.findAll()
                .stream()
                .filter(p -> p.getStock() > 0)
                .count();

        if (productosDisponibles == 0) {
            throw new BusinessException(
                    "No hay productos disponibles en inventario. " +
                            "No es posible agendar citas en este momento.");
        }

        // Crear solicitud
        SolicitudCita solicitud = new SolicitudCita();
        solicitud.setCliente(cliente);
        solicitud.setPaciente(paciente);
        solicitud.setFechaSolicitada(request.getFechaSolicitada());
        solicitud.setHoraSolicitada(request.getHoraSolicitada());
        solicitud.setTipoServicio(request.getTipoServicio());
        solicitud.setMotivo(request.getMotivo());
        solicitud.setObservaciones(request.getObservaciones());
        solicitud.setEstado(AppConstants.ESTADO_SOLICITUD_PENDIENTE);

        SolicitudCita guardada = solicitudCitaRepository.save(Objects.requireNonNull(solicitud));
        log.info("✅ Solicitud de cita creada. ID: {}", guardada.getIdSolicitud());

        return mapToResponse(guardada);
    }

    /**
     * Obtiene solicitudes de cita por cliente.
     */
    @Transactional(readOnly = true)
    public Page<SolicitudCitaResponse> obtenerPorCliente(Long clienteId, Pageable pageable) {
        log.info("🔍 Obteniendo solicitudes para cliente: {}", clienteId);

        // Validar que cliente existe
        usuarioRepository.findById(Objects.requireNonNull(clienteId))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        return solicitudCitaRepository.findByClienteId(clienteId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Obtiene una solicitud específica.
     */
    @Transactional(readOnly = true)
    public SolicitudCitaResponse obtener(Long id) {
        log.info("🔍 Obteniendo solicitud ID: {}", id);

        SolicitudCita solicitud = solicitudCitaRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de cita", "id", id));

        return mapToResponse(solicitud);
    }

    /**
     * Obtiene solicitudes pendientes.
     */
    @Transactional(readOnly = true)
    public Page<SolicitudCitaResponse> obtenerPendientes(Pageable pageable) {
        log.info("🔍 Obteniendo solicitudes pendientes");

        return solicitudCitaRepository.findByEstado(AppConstants.ESTADO_SOLICITUD_PENDIENTE, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Aprueba una solicitud de cita y crea la cita correspondiente.
     * Valida clinic hours, veterinarian availability y anticipation time.
     * 
     * 🟡 AUDIT TRAIL: Registra quién aprobó y cuándo
     */
    @Transactional
    public SolicitudCitaResponse aprobar(Long id, Long veterinarioId, Long secretarioId) {
        log.info("✅ Aprobando solicitud ID: {} por secretario: {}", id, secretarioId);

        SolicitudCita solicitud = solicitudCitaRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de cita", "id", id));

        if (!AppConstants.ESTADO_SOLICITUD_PENDIENTE.equals(solicitud.getEstado())) {
            throw new BusinessException("Solo se pueden aprobar solicitudes pendientes");
        }

        // Construir la fecha y hora completa de la solicitud
        LocalDateTime fechaHoraSolicitada = LocalDateTime.of(
                solicitud.getFechaSolicitada(),
                solicitud.getHoraSolicitada());

        // Validar que la fecha no sea en el pasado
        if (fechaHoraSolicitada.isBefore(LocalDateTime.now())) {
            throw new BusinessException("No se puede aprobar una solicitud con fecha en el pasado");
        }

        // Validar anticipación mínima (2 horas)
        LocalDateTime anticipacionMinima = LocalDateTime.now().plusHours(AppConstants.ANTICIPACION_MINIMA_HORAS);
        if (fechaHoraSolicitada.isBefore(anticipacionMinima)) {
            throw new BusinessException("La cita debe tener al menos " +
                    AppConstants.ANTICIPACION_MINIMA_HORAS + " horas de anticipación");
        }

        // Validar horario laboral (uses CitaService's validation logic)
        validarHorarioLaboral(fechaHoraSolicitada);

        // Validar disponibilidad del veterinario
        if (!citaService.verificarDisponibilidad(veterinarioId, fechaHoraSolicitada)) {
            throw new BusinessException(
                    "El veterinario no está disponible en esa fecha y hora. Por favor, seleccione otro horario");
        }

        // Validar que el veterinario existe y está activo
        Usuario usuarioVeterinario = usuarioRepository.findById(Objects.requireNonNull(veterinarioId))
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", "id", veterinarioId));

        if (!(usuarioVeterinario instanceof UsuarioVeterinario)) {
            throw new BusinessException("El usuario indicado no corresponde a un veterinario activo");
        }

        // Crear cita automáticamente
        try {
            Cita cita = new Cita();
            cita.setPaciente(solicitud.getPaciente());
            cita.setVeterinario((UsuarioVeterinario) usuarioVeterinario);
            cita.setFechaHora(fechaHoraSolicitada);
            cita.setTipoServicio(solicitud.getTipoServicio());
            cita.setMotivo(solicitud.getMotivo());
            cita.setEstado(AppConstants.ESTADO_CITA_PROGRAMADA);

            Cita citaGuardada = citaRepository.save(Objects.requireNonNull(cita));
            log.info("✅ Cita creada automáticamente. ID: {}", citaGuardada.getIdCita());

            // Actualizar solicitud con información de audit trail
            solicitud.setEstado(AppConstants.ESTADO_SOLICITUD_APROBADA);
            solicitud.setCitaId(citaGuardada.getIdCita());
            solicitud.setUpdatedAt(LocalDateTime.now());
            solicitud.setAprobadoPor(secretarioId);
            solicitud.setAprobadoEn(LocalDateTime.now());

            SolicitudCita actualizada = solicitudCitaRepository.save(Objects.requireNonNull(solicitud));
            log.info("✅ Solicitud aprobada por usuario: {}", secretarioId);

            return mapToResponse(actualizada);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException("Error al aprobar la solicitud: " + e.getMessage());
        }
    }

    /**
     * Rechaza una solicitud de cita.
     * 
     * 🟡 AUDIT TRAIL: Registra quién rechazó y cuándo
     */
    @Transactional
    public SolicitudCitaResponse rechazar(Long id, String motivo, Long secretarioId) {
        log.info("❌ Rechazando solicitud ID: {} por secretario: {}", id, secretarioId);

        SolicitudCita solicitud = solicitudCitaRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de cita", "id", id));

        if (!solicitud.getEstado().equals(AppConstants.ESTADO_SOLICITUD_PENDIENTE)) {
            throw new BusinessException("Solo se pueden rechazar solicitudes pendientes");
        }

        solicitud.setEstado(AppConstants.ESTADO_SOLICITUD_RECHAZADA);
        solicitud.setMotivoRechazo(motivo);
        solicitud.setUpdatedAt(LocalDateTime.now());
        solicitud.setRechazadoPor(secretarioId);
        solicitud.setRechazadoEn(LocalDateTime.now());

        SolicitudCita actualizada = solicitudCitaRepository.save(Objects.requireNonNull(solicitud));
        log.info("✅ Solicitud rechazada por usuario: {}", secretarioId);

        return mapToResponse(actualizada);
    }

    /**
     * Cancela una solicitud de cita.
     * 
     * 🔴 VALIDACIÓN CRÍTICA 3: Si la solicitud fue aprobada, cancela la cita
     * automáticamente
     * 🟡 AUDIT TRAIL: Registra quién canceló y cuándo
     */
    @Transactional
    public SolicitudCitaResponse cancelar(Long id, Long usuarioId) {
        log.info("🚫 Cancelando solicitud ID: {} por usuario: {}", id, usuarioId);

        SolicitudCita solicitud = solicitudCitaRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de cita", "id", id));

        if (solicitud.getEstado().equals(AppConstants.ESTADO_SOLICITUD_CANCELADA)) {
            throw new BusinessException("La solicitud ya está cancelada");
        }

        // 🔴 VALIDACIÓN CRÍTICA 3: Si fue aprobada, cancelar cita automáticamente
        if (AppConstants.ESTADO_SOLICITUD_APROBADA.equals(solicitud.getEstado()) && solicitud.getCitaId() != null) {
            log.info("🚫 Cancelando cita asociada ID: {}", solicitud.getCitaId());
            Cita cita = citaRepository.findById(Objects.requireNonNull(solicitud.getCitaId()))
                    .orElseThrow(() -> new BusinessException("Cita asociada no encontrada"));

            if (!AppConstants.ESTADO_CITA_CANCELADA.equals(cita.getEstado())) {
                cita.setEstado(AppConstants.ESTADO_CITA_CANCELADA);
                citaRepository.save(cita);
                log.info("✅ Cita cancelada automáticamente");
            }
        }

        solicitud.setEstado(AppConstants.ESTADO_SOLICITUD_CANCELADA);
        solicitud.setUpdatedAt(LocalDateTime.now());
        solicitud.setCanceladoPor(usuarioId);
        solicitud.setCanceladoEn(LocalDateTime.now());

        SolicitudCita actualizada = solicitudCitaRepository.save(Objects.requireNonNull(solicitud));
        log.info("✅ Solicitud cancelada por usuario: {}", usuarioId);

        return mapToResponse(actualizada);
    }

    /**
     * Obtiene el conteo de solicitudes pendientes.
     */
    @Transactional(readOnly = true)
    public Long contarPendientes() {
        return solicitudCitaRepository.countByEstado(AppConstants.ESTADO_SOLICITUD_PENDIENTE);
    }

    /**
     * Valida que la fecha/hora esté dentro del horario laboral de la clínica.
     * - Lunes a Viernes: 8:00-12:00 y 14:00-18:00
     * - Sábados: 8:00-12:00
     * - Domingos: Cerrado
     * 
     * @param fechaHora Fecha y hora a validar
     * @throws BusinessException si está fuera del horario laboral
     */
    private void validarHorarioLaboral(LocalDateTime fechaHora) {
        java.time.DayOfWeek diaSemana = fechaHora.getDayOfWeek();
        int hora = fechaHora.getHour();
        int minuto = fechaHora.getMinute();

        // Validar que no sea domingo
        if (diaSemana == java.time.DayOfWeek.SUNDAY) {
            throw new BusinessException(
                    "No se pueden agendar citas los domingos. " +
                            "Horario de atención: Lunes a Viernes 8:00-12:00 y 14:00-18:00, Sábados 8:00-12:00");
        }

        // Para sábados: solo horario de mañana (8:00-12:00)
        if (diaSemana == java.time.DayOfWeek.SATURDAY) {
            boolean enHorarioSabado = (hora >= AppConstants.HORARIO_MANANA_INICIO
                    && hora < AppConstants.HORARIO_MANANA_FIN) ||
                    (hora == AppConstants.HORARIO_MANANA_FIN && minuto == 0);

            if (!enHorarioSabado) {
                throw new BusinessException(
                        "Los sábados el horario de atención es de 8:00 AM a 12:00 PM");
            }
            return;
        }

        // Para lunes a viernes: horario de mañana (8:00-12:00) y tarde (14:00-18:00)
        boolean enHorarioManana = (hora >= AppConstants.HORARIO_MANANA_INICIO && hora < AppConstants.HORARIO_MANANA_FIN)
                ||
                (hora == AppConstants.HORARIO_MANANA_FIN && minuto == 0);

        boolean enHorarioTarde = (hora >= AppConstants.HORARIO_TARDE_INICIO && hora < AppConstants.HORARIO_TARDE_FIN) ||
                (hora == AppConstants.HORARIO_TARDE_FIN && minuto == 0);

        if (!enHorarioManana && !enHorarioTarde) {
            throw new BusinessException(
                    "La cita debe estar dentro del horario de atención: " +
                            "Lunes a Viernes de 8:00 AM a 12:00 PM y de 2:00 PM a 6:00 PM, " +
                            "Sábados de 8:00 AM a 12:00 PM");
        }
    }

    /**
     * Mapea SolicitudCita a DTO.
     */
    private SolicitudCitaResponse mapToResponse(SolicitudCita solicitud) {
        return SolicitudCitaResponse.builder()
                .idSolicitud(Objects.requireNonNull(solicitud.getIdSolicitud()))
                .clienteId(Objects.requireNonNull(solicitud.getCliente().getIdPersona()))
                .nombreCliente(solicitud.getCliente().getNombre() + " " + solicitud.getCliente().getApellido())
                .pacienteId(Objects.requireNonNull(solicitud.getPaciente().getIdPaciente()))
                .nombrePaciente(solicitud.getPaciente().getNombre())
                .fechaSolicitada(solicitud.getFechaSolicitada())
                .horaSolicitada(solicitud.getHoraSolicitada())
                .tipoServicio(solicitud.getTipoServicio())
                .motivo(solicitud.getMotivo())
                .estado(solicitud.getEstado())
                .motivoRechazo(solicitud.getMotivoRechazo())
                .citaId(solicitud.getCitaId())
                .observaciones(solicitud.getObservaciones())
                .fechaCreacion(solicitud.getCreatedAt())
                .fechaActualizacion(solicitud.getUpdatedAt())
                // Audit trail
                .aprobadoPor(solicitud.getAprobadoPor())
                .aprobadoEn(solicitud.getAprobadoEn())
                .rechazadoPor(solicitud.getRechazadoPor())
                .rechazadoEn(solicitud.getRechazadoEn())
                .canceladoPor(solicitud.getCanceladoPor())
                .canceladoEn(solicitud.getCanceladoEn())
                .build();
    }
}
