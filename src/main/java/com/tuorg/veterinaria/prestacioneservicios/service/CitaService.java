package com.tuorg.veterinaria.prestacioneservicios.service;

import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaCancelarRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaRequest;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaResponse;
import com.tuorg.veterinaria.prestacioneservicios.dto.CitaReprogramarRequest;
import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import com.tuorg.veterinaria.prestacioneservicios.repository.CitaRepository;
import com.tuorg.veterinaria.notificaciones.service.NotificacionService;
import com.tuorg.veterinaria.notificaciones.dto.NotificacionEnviarRequest;
import com.tuorg.veterinaria.common.events.CitaCancelledEvent;
import com.tuorg.veterinaria.common.events.CitaCreatedEvent;
import com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica;
import com.tuorg.veterinaria.gestionpacientes.model.RegistroMedico;
import com.tuorg.veterinaria.gestionpacientes.repository.HistoriaClinicaRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.RegistroMedicoRepository;
import com.tuorg.veterinaria.common.events.CitaReprogrammedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Servicio para la gestión de citas.
 *
 * Se encarga de coordinar la lógica de negocio relacionada con el ciclo de vida
 * de una cita (programar, reprogramar, cancelar y completar) y de exponer las
 * operaciones en DTOs desacoplados de las entidades JPA.
 */
@Slf4j
@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;
    private final ApplicationEventPublisher eventPublisher;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final RegistroMedicoRepository registroMedicoRepository;

    @Autowired
    public CitaService(CitaRepository citaRepository,
                       PacienteRepository pacienteRepository,
                       UsuarioRepository usuarioRepository,
                       NotificacionService notificacionService,
                       ApplicationEventPublisher eventPublisher,
                       HistoriaClinicaRepository historiaClinicaRepository,
                       RegistroMedicoRepository registroMedicoRepository) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
        this.eventPublisher = eventPublisher;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.registroMedicoRepository = registroMedicoRepository;
    }

    /**
     * Programa una nueva cita validando disponibilidad y referencias.
     */
    @Transactional
    public CitaResponse programar(CitaRequest request) {
        return programar(request, null);
    }
    
    /**
     * Programa una nueva cita validando disponibilidad, referencias y propiedad del paciente.
     * 
     * Flujo de programación de cita:
     * 1. Valida existencia del paciente
     * 2. Si es cliente, verifica que el paciente le pertenezca
     * 3. Valida existencia y rol del veterinario
     * 4. Valida fecha/hora no esté en el pasado
     * 5. Valida anticipación mínima (ej: 24 horas antes)
     * 6. Valida horario laboral permitido
     * 7. Valida límite de citas por día del cliente
     * 8. Valida disponibilidad del veterinario (sin conflictos)
     * 9. Crea cita con estado PROGRAMADA
     * 10. Publica evento asíncrono para notificaciones
     * 
     * @param request DTO con datos de la cita (pacienteId, veterinarioId, fechaHora, etc)
     * @param clienteId ID del cliente que crea la cita (null para veterinarios/secretarios)
     * @return CitaResponse con datos de la cita creada
     * @throws ResourceNotFoundException si paciente o veterinario no existen
     * @throws BusinessException si alguna validación falla (conflicto horario, anticipación, etc)
     */
    @Transactional
    public CitaResponse programar(CitaRequest request, Long clienteId) {
        // PASO 1: Validar existencia del paciente
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", request.getPacienteId()));

        // PASO 2: Si es cliente quien crea la cita, verificar propiedad del paciente
        // Previene que un cliente cree citas para pacientes de otros clientes
        if (clienteId != null) {
            Cliente cliente = paciente.getCliente();
            if (cliente == null || !cliente.getIdUsuario().equals(clienteId)) {
                log.warn("❌ Intento de crear cita de cliente {} para paciente que no le pertenece", clienteId);
                throw new BusinessException("Solo puede agendar citas para sus propios pacientes");
            }
        }

        // PASO 3: Validar existencia y rol del veterinario
        Usuario usuario = usuarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", "id", request.getVeterinarioId()));

        // Verificar que sea un veterinario (usar instanceof para verificar rol)
        if (!(usuario instanceof UsuarioVeterinario veterinario)) {
            log.error("❌ Usuario {} no es veterinario", request.getVeterinarioId());
            throw new BusinessException("El usuario indicado no corresponde a un veterinario activo");
        }

        LocalDateTime fechaHora = request.getFechaHora();
        
        // PASO 4: Validar que la fecha no sea en el pasado
        if (fechaHora.isBefore(LocalDateTime.now())) {
            log.warn("❌ Intento de agendar cita en el pasado: {}", fechaHora);
            throw new BusinessException("No se puede programar una cita en el pasado");
        }
        
        // PASO 5: Validar anticipación mínima
        // AppConstants.ANTICIPACION_MINIMA_HORAS define el tiempo mínimo (ej: 24 horas)
        LocalDateTime anticipacionMinima = LocalDateTime.now().plusHours(AppConstants.ANTICIPACION_MINIMA_HORAS);
        if (fechaHora.isBefore(anticipacionMinima)) {
            log.warn("❌ Anticipación insuficiente: solicitada {}, mínima {}", fechaHora, anticipacionMinima);
            throw new BusinessException("Debe agendar la cita con al menos " + 
                AppConstants.ANTICIPACION_MINIMA_HORAS + " horas de anticipación");
        }
        
        // PASO 6: Validar horario laboral permitido
        // Solo se permiten citas dentro de horarios configurados
        validarHorarioLaboral(fechaHora);
        
        // PASO 7: Validar límite de citas por día del cliente
        // Previene que un cliente agende demasiadas citas en un mismo día
        Cliente cliente = paciente.getCliente();
        if (cliente != null) {
            validarLimiteCitasPorDia(cliente.getIdUsuario(), fechaHora);
        }

        // PASO 8: Validar disponibilidad del veterinario
        // Verifica que no exista conflicto con otras citas en ese horario
        // Considerando duración estimada de la cita
        if (!validarDisponibilidad(veterinario.getIdUsuario(), fechaHora)) {
            log.warn("❌ Veterinario {} no disponible para fecha-hora: {}", veterinario.getIdUsuario(), fechaHora);
            throw new BusinessException("El veterinario ya tiene una cita programada en ese horario. Por favor, seleccione otra fecha y hora");
        }

        // PASO 9: Crear entidad de cita con estado inicial PROGRAMADA
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setVeterinario(veterinario);
        cita.setFechaHora(fechaHora);
        cita.setTipoServicio(request.getTipoServicio());
        cita.setMotivo(request.getMotivo());
        cita.setTriageNivel(request.getTriageNivel());
        cita.setEstado(AppConstants.ESTADO_CITA_PROGRAMADA);

        Cita guardada = citaRepository.save(cita);
        log.info("✅ Cita programada exitosamente: ID={}, Paciente={}, Veterinario={}, Fecha={}", 
            guardada.getIdCita(), paciente.getIdPaciente(), veterinario.getIdUsuario(), fechaHora);
        
        // PASO 10: Publicar evento asíncrono para notificaciones
        // El evento es procesado por NotificacionEventListener de forma asíncrona
        publicarEventoCitaCreada(guardada);
        
        return mapToResponse(guardada);
    }

    /**
     * Reprograma una cita existente garantizando la disponibilidad del veterinario.
     */
    @Transactional
    public CitaResponse reprogramar(Long citaId, CitaReprogramarRequest request) {
        return reprogramar(citaId, request, null);
    }
    
    /**
     * Reprograma una cita existente con validación de propiedad del paciente.
     * 
     * @param citaId ID de la cita
     * @param request Datos de reprogramación
     * @param clienteId ID del cliente que está reprogramando (null para veterinarios/secretarios)
     * @return Cita reprogramada
     */
    @Transactional
    public CitaResponse reprogramar(Long citaId, CitaReprogramarRequest request, Long clienteId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "id", citaId));
        
        // Si es un cliente quien reprograma, validar que la cita sea de su paciente
        if (clienteId != null) {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            if (cliente == null || !cliente.getIdUsuario().equals(clienteId)) {
                throw new BusinessException("Solo puede reprogramar citas de sus propios pacientes");
            }
        }

        if (!AppConstants.ESTADO_CITA_PROGRAMADA.equals(cita.getEstado())) {
            throw new BusinessException("Solo se pueden reprogramar citas en estado PROGRAMADA");
        }

        LocalDateTime nuevaFechaHora = request.getFechaHora();
        
        // Validar que la fecha no sea en el pasado
        if (nuevaFechaHora.isBefore(LocalDateTime.now())) {
            throw new BusinessException("No se puede reprogramar una cita al pasado");
        }
        
        // Validar anticipación mínima
        LocalDateTime anticipacionMinima = LocalDateTime.now().plusHours(AppConstants.ANTICIPACION_MINIMA_HORAS);
        if (nuevaFechaHora.isBefore(anticipacionMinima)) {
            throw new BusinessException("Debe reprogramar la cita con al menos " + 
                AppConstants.ANTICIPACION_MINIMA_HORAS + " horas de anticipación");
        }
        
        // Validar horario laboral
        validarHorarioLaboral(nuevaFechaHora);

        // Validar disponibilidad considerando la duración de la cita (excluyendo la cita actual)
        LocalDateTime fechaHoraActual = cita.getFechaHora();
        if (!validarDisponibilidadExcluyendo(cita.getVeterinario().getIdUsuario(), nuevaFechaHora, fechaHoraActual)) {
            throw new BusinessException("El veterinario ya tiene una cita programada en ese horario. Por favor, seleccione otra fecha y hora");
        }

        LocalDateTime fechaAnterior = cita.getFechaHora();
        cita.setFechaHora(nuevaFechaHora);
        Cita actualizada = citaRepository.save(cita);
        
        // Publicar evento para envío asíncrono de notificación
        publicarEventoCitaReprogramada(actualizada, fechaAnterior);
        
        return mapToResponse(actualizada);
    }

    /**
     * Cancela una cita agregando el motivo y cambiando su estado.
     */
    @Transactional
    public CitaResponse cancelar(Long citaId, CitaCancelarRequest request) {
        return cancelar(citaId, request, null);
    }
    
    /**
     * Cancela una cita con validación de propiedad del paciente.
     * 
     * @param citaId ID de la cita
     * @param request Datos de cancelación
     * @param clienteId ID del cliente que está cancelando (null para veterinarios/secretarios)
     * @return Cita cancelada
     */
    @Transactional
    public CitaResponse cancelar(Long citaId, CitaCancelarRequest request, Long clienteId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "id", citaId));
        
        // Si es un cliente quien cancela, validar que la cita sea de su paciente
        if (clienteId != null) {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            if (cliente == null || !cliente.getIdUsuario().equals(clienteId)) {
                throw new BusinessException("Solo puede cancelar citas de sus propios pacientes");
            }
        }

        if (AppConstants.ESTADO_CITA_REALIZADA.equals(cita.getEstado())) {
            throw new BusinessException("No se puede cancelar una cita ya realizada");
        }
        
        // Validar anticipación mínima para cancelación
        LocalDateTime anticipacionMinima = LocalDateTime.now().plusHours(AppConstants.ANTICIPACION_MINIMA_HORAS);
        if (cita.getFechaHora().isBefore(anticipacionMinima)) {
            throw new BusinessException("Debe cancelar la cita con al menos " + 
                AppConstants.ANTICIPACION_MINIMA_HORAS + " horas de anticipación");
        }

        cita.setEstado(AppConstants.ESTADO_CITA_CANCELADA);
        cita.setMotivo((cita.getMotivo() != null ? cita.getMotivo() + "\n" : "") +
                "Cancelada: " + request.getMotivo());

        Cita cancelada = citaRepository.save(cita);
        
        // Publicar evento para envío asíncrono de notificación
        publicarEventoCitaCancelada(cancelada, request.getMotivo());
        
        return mapToResponse(cancelada);
    }

    /**
     * Marca la cita como realizada cuando el servicio concluye.
     * Crea automáticamente un registro médico básico en la historia clínica del paciente.
     */
    @Transactional
    public CitaResponse completar(Long citaId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita", "id", citaId));

        if (!AppConstants.ESTADO_CITA_PROGRAMADA.equals(cita.getEstado())) {
            throw new BusinessException("Solo se pueden completar citas en estado PROGRAMADA");
        }

        cita.setEstado(AppConstants.ESTADO_CITA_REALIZADA);
        Cita completada = citaRepository.save(cita);

        // Crear registro médico automático en la historia clínica
        crearRegistroMedicoAutomatico(completada);

        return mapToResponse(completada);
    }

    /**
     * Crea un registro médico básico cuando se completa una cita.
     * Este registro puede ser editado posteriormente por el veterinario para agregar más detalles.
     */
    private void crearRegistroMedicoAutomatico(Cita cita) {
        // Obtener la historia clínica del paciente
        HistoriaClinica historia = historiaClinicaRepository.findByPacienteId(cita.getPaciente().getIdPaciente())
                .orElseThrow(() -> new ResourceNotFoundException("HistoriaClinica", "paciente_id", cita.getPaciente().getIdPaciente()));

        // Crear registro médico básico
        RegistroMedico registro = new RegistroMedico();
        registro.setHistoria(historia);
        registro.setFecha(cita.getFechaHora());
        registro.setVeterinario(cita.getVeterinario());
        
        // Construir motivo con información de la cita
        String motivo = cita.getMotivo() != null && !cita.getMotivo().isBlank() 
            ? cita.getMotivo() 
            : "Consulta programada";
        
        if (cita.getTipoServicio() != null && !cita.getTipoServicio().isBlank()) {
            motivo = cita.getTipoServicio() + " - " + motivo;
        }
        
        registro.setMotivo(motivo);
        registro.setDiagnostico("Pendiente de evaluación completa por el veterinario");
        registro.setTratamiento("Por definir");

        registroMedicoRepository.save(registro);
        
        log.info("Registro médico automático creado para cita ID: {} en historia clínica ID: {}", 
                 cita.getIdCita(), historia.getIdHistoria());
    }

    /**
     * Obtiene todas las citas devolviéndolas en formato DTO.
     */
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerTodas() {
        return citaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene todas las citas de un paciente devolviéndolas en formato DTO.
     */
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene todas las citas de un veterinario.
     */
    @Transactional(readOnly = true)
    public List<CitaResponse> obtenerPorVeterinario(Long veterinarioId) {
        return citaRepository.findByVeterinarioId(veterinarioId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene todas las citas de un veterinario con paginación.
     */
    @Transactional(readOnly = true)
    public Page<CitaResponse> obtenerPorVeterinarioPaginado(Long veterinarioId, Pageable pageable) {
        return citaRepository.findByVeterinarioId(veterinarioId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Obtiene todas las citas de un paciente con paginación.
     */
    @Transactional(readOnly = true)
    public Page<CitaResponse> obtenerPorPacientePaginado(Long pacienteId, Pageable pageable) {
        return citaRepository.findByPacienteId(pacienteId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Verifica si una fecha y hora está disponible para un veterinario.
     * Considera la duración estándar de las citas.
     * 
     * @param veterinarioId ID del veterinario
     * @param fechaHora Fecha y hora a verificar
     * @return true si está disponible, false si hay conflicto
     */
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long veterinarioId, LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            return false;
        }
        return validarDisponibilidad(veterinarioId, fechaHora);
    }

    /**
     * Valida la disponibilidad de un veterinario en una fecha/hora específica.
     * Considera un rango de tiempo basado en la duración estándar de las citas.
     * 
     * @param veterinarioId ID del veterinario
     * @param fechaHora Fecha y hora a validar
     * @return true si está disponible, false si hay conflicto
     */
    private boolean validarDisponibilidad(Long veterinarioId, LocalDateTime fechaHora) {
        LocalDateTime inicioRango = fechaHora.minusMinutes(AppConstants.DURACION_CITA_MINUTOS);
        LocalDateTime finRango = fechaHora.plusMinutes(AppConstants.DURACION_CITA_MINUTOS);
        
        return !citaRepository.existeCitaEnRango(
                veterinarioId,
                inicioRango,
                finRango,
                AppConstants.ESTADO_CITA_PROGRAMADA);
    }

    /**
     * Valida la disponibilidad excluyendo una cita específica (útil para reprogramar).
     * 
     * @param veterinarioId ID del veterinario
     * @param nuevaFechaHora Nueva fecha y hora a validar
     * @param fechaHoraExcluir Fecha y hora de la cita a excluir de la validación
     * @return true si está disponible, false si hay conflicto
     */
    private boolean validarDisponibilidadExcluyendo(Long veterinarioId, LocalDateTime nuevaFechaHora, LocalDateTime fechaHoraExcluir) {
        // Si la nueva fecha es la misma que la actual, está disponible
        if (nuevaFechaHora.equals(fechaHoraExcluir)) {
            return true;
        }
        
        // Verificar si hay otras citas en el rango (excluyendo la cita actual)
        LocalDateTime inicioRango = nuevaFechaHora.minusMinutes(AppConstants.DURACION_CITA_MINUTOS);
        LocalDateTime finRango = nuevaFechaHora.plusMinutes(AppConstants.DURACION_CITA_MINUTOS);
        
        // Obtener todas las citas en el rango
        List<Cita> citasEnRango = citaRepository.findByVeterinarioId(veterinarioId)
                .stream()
                .filter(c -> AppConstants.ESTADO_CITA_PROGRAMADA.equals(c.getEstado()))
                .filter(c -> {
                    LocalDateTime cFecha = c.getFechaHora();
                    return cFecha.isAfter(inicioRango) && cFecha.isBefore(finRango);
                })
                .filter(c -> !c.getFechaHora().equals(fechaHoraExcluir)) // Excluir la cita actual
                .toList();
        
        return citasEnRango.isEmpty();
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
                "Horario de atención: Lunes a Viernes 8:00-12:00 y 14:00-18:00, Sábados 8:00-12:00"
            );
        }
        
        // Para sábados: solo horario de mañana (8:00-12:00)
        if (diaSemana == java.time.DayOfWeek.SATURDAY) {
            boolean enHorarioSabado = 
                (hora >= AppConstants.HORARIO_MANANA_INICIO && hora < AppConstants.HORARIO_MANANA_FIN) ||
                (hora == AppConstants.HORARIO_MANANA_FIN && minuto == 0);
            
            if (!enHorarioSabado) {
                throw new BusinessException(
                    "Los sábados el horario de atención es de 8:00 AM a 12:00 PM"
                );
            }
            return;
        }
        
        // Para lunes a viernes: horario de mañana (8:00-12:00) y tarde (14:00-18:00)
        boolean enHorarioManana = 
            (hora >= AppConstants.HORARIO_MANANA_INICIO && hora < AppConstants.HORARIO_MANANA_FIN) ||
            (hora == AppConstants.HORARIO_MANANA_FIN && minuto == 0);
        
        boolean enHorarioTarde = 
            (hora >= AppConstants.HORARIO_TARDE_INICIO && hora < AppConstants.HORARIO_TARDE_FIN) ||
            (hora == AppConstants.HORARIO_TARDE_FIN && minuto == 0);
        
        if (!enHorarioManana && !enHorarioTarde) {
            throw new BusinessException(
                "La cita debe estar dentro del horario de atención: " +
                "Lunes a Viernes de 8:00 AM a 12:00 PM y de 2:00 PM a 6:00 PM, " +
                "Sábados de 8:00 AM a 12:00 PM"
            );
        }
    }
    
    /**
     * Valida que el cliente no exceda el límite de citas por día.
     * 
     * @param clienteId ID del cliente
     * @param fechaHora Fecha de la nueva cita
     * @throws BusinessException si excede el límite
     */
    private void validarLimiteCitasPorDia(Long clienteId, LocalDateTime fechaHora) {
        LocalDateTime inicioDia = fechaHora.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fechaHora.toLocalDate().atTime(23, 59, 59);
        
        long citasDelDia = citaRepository.findAll()
                .stream()
                .filter(c -> {
                    Paciente p = c.getPaciente();
                    if (p == null || p.getCliente() == null) return false;
                    return p.getCliente().getIdUsuario().equals(clienteId);
                })
                .filter(c -> AppConstants.ESTADO_CITA_PROGRAMADA.equals(c.getEstado()))
                .filter(c -> {
                    LocalDateTime cf = c.getFechaHora();
                    return !cf.isBefore(inicioDia) && !cf.isAfter(finDia);
                })
                .count();
        
        if (citasDelDia >= AppConstants.MAX_CITAS_POR_DIA_CLIENTE) {
            throw new BusinessException(String.format(
                "Ha alcanzado el límite máximo de %d citas por día. Por favor, contacte a la clínica si necesita más citas.",
                AppConstants.MAX_CITAS_POR_DIA_CLIENTE
            ));
        }
    }

    private CitaResponse mapToResponse(Cita cita) {
        Paciente paciente = cita.getPaciente();
        Cliente propietario = paciente != null ? paciente.getCliente() : null;
        UsuarioVeterinario veterinario = cita.getVeterinario();

        return CitaResponse.builder()
                .idCita(cita.getIdCita())
                .fechaHora(cita.getFechaHora())
                .estado(cita.getEstado())
                .tipoServicio(cita.getTipoServicio())
                .motivo(cita.getMotivo())
                .triageNivel(cita.getTriageNivel())
                .paciente(CitaResponse.PacienteSummary.builder()
                        .id(paciente != null ? paciente.getIdPaciente() : null)
                        .nombre(paciente != null ? paciente.getNombre() : null)
                        .especie(paciente != null ? paciente.getEspecie() : null)
                        .propietario(propietario != null ? propietario.getNombre() + " " + propietario.getApellido() : null)
                        .build())
                .veterinario(veterinario != null ? CitaResponse.VeterinarioSummary.builder()
                        .id(veterinario.getIdUsuario())
                        .nombreCompleto(veterinario.getNombre() + " " + veterinario.getApellido())
                        .especialidad(veterinario.getEspecialidad())
                        .build() : null)
                .build();
    }
    
    /**
     * Publica evento de cita creada para procesamiento asíncrono.
     */
    private void publicarEventoCitaCreada(Cita cita) {
        try {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            UsuarioVeterinario veterinario = cita.getVeterinario();
            
            if (cliente != null && cliente.getCorreo() != null && !cliente.getCorreo().isEmpty()) {
                CitaCreatedEvent event = new CitaCreatedEvent(
                    this,
                    cita.getIdCita(),
                    paciente.getIdPaciente(),
                    veterinario.getIdUsuario(),
                    cita.getFechaHora(),
                    cliente.getCorreo(),
                    cliente.getTelefono() != null ? cliente.getTelefono() : "",
                    cliente.getNombre() + " " + cliente.getApellido(),
                    paciente.getNombre(),
                    "Dr. " + veterinario.getNombre() + " " + veterinario.getApellido()
                );
                eventPublisher.publishEvent(event);
            }
        } catch (Exception e) {
            // Log pero no fallar la transacción
            log.warn("Error al publicar evento CitaCreatedEvent: {}", e.getMessage());
        }
    }
    
    /**
     * Publica evento de cita reprogramada para procesamiento asíncrono.
     */
    private void publicarEventoCitaReprogramada(Cita cita, LocalDateTime fechaAnterior) {
        try {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            UsuarioVeterinario veterinario = cita.getVeterinario();
            
            if (cliente != null && cliente.getCorreo() != null && !cliente.getCorreo().isEmpty()) {
                CitaReprogrammedEvent event = new CitaReprogrammedEvent(
                    this,
                    cita.getIdCita(),
                    fechaAnterior,
                    cita.getFechaHora(),
                    cliente.getCorreo(),
                    cliente.getTelefono() != null ? cliente.getTelefono() : "",
                    cliente.getNombre() + " " + cliente.getApellido(),
                    paciente.getNombre(),
                    "Dr. " + veterinario.getNombre() + " " + veterinario.getApellido()
                );
                eventPublisher.publishEvent(event);
            }
        } catch (Exception e) {
            log.warn("Error al publicar evento CitaReprogrammedEvent: {}", e.getMessage());
        }
    }
    
    /**
     * Publica evento de cita cancelada para procesamiento asíncrono.
     */
    private void publicarEventoCitaCancelada(Cita cita, String motivoCancelacion) {
        try {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            
            if (cliente != null && cliente.getCorreo() != null && !cliente.getCorreo().isEmpty()) {
                CitaCancelledEvent event = new CitaCancelledEvent(
                    this,
                    cita.getIdCita(),
                    cita.getFechaHora(),
                    cliente.getCorreo(),
                    cliente.getTelefono() != null ? cliente.getTelefono() : "",
                    cliente.getNombre() + " " + cliente.getApellido(),
                    paciente.getNombre(),
                    motivoCancelacion
                );
                eventPublisher.publishEvent(event);
            }
        } catch (Exception e) {
            log.warn("Error al publicar evento CitaCancelledEvent: {}", e.getMessage());
        }
    }
    
    /**
     * DEPRECATED - Método anterior de envío sincrónico.
     * Mantenido temporalmente para referencia.
     * 
     * @param cita La cita que se acaba de crear
     */
    @Deprecated
    @SuppressWarnings("unused")
    private void enviarNotificacionCitaCreada(Cita cita) {
        try {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            
            log.debug("Paciente ID: {}, Nombre: {}", paciente != null ? paciente.getIdPaciente() : null, paciente != null ? paciente.getNombre() : null);
            log.debug("Cliente ID: {}, Correo: {}", cliente != null ? cliente.getIdUsuario() : null, cliente != null ? cliente.getCorreo() : null);
            
            if (cliente == null || cliente.getCorreo() == null || cliente.getCorreo().isEmpty()) {
                log.warn("No se puede enviar email - Cliente o correo no disponible");
                return; // No enviar si no hay cliente o email
            }
            
            UsuarioVeterinario veterinario = cita.getVeterinario();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaFormateada = cita.getFechaHora().format(formatter);
            
            String mensaje = String.format(
                "Estimado/a %s %s,\n\n" +
                "Su cita ha sido programada exitosamente.\n\n" +
                "DETALLES DE LA CITA:\n\n" +
                "Fecha y Hora: %s\n" +
                "Paciente: %s (%s)\n" +
                "Veterinario: Dr. %s %s\n" +
                "Tipo de Servicio: %s\n" +
                "%s\n\n" +
                "RECORDATORIO IMPORTANTE:\n" +
                "Por favor, llegue 10 minutos antes de su cita para completar el registro.\n\n" +
                "Si necesita cancelar o reprogramar, contáctenos con al menos 24 horas de anticipación.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Clínica Veterinaria Humboldt",
                cliente.getNombre(),
                cliente.getApellido(),
                fechaFormateada,
                paciente.getNombre(),
                paciente.getEspecie(),
                veterinario.getNombre(),
                veterinario.getApellido(),
                cita.getTipoServicio() != null ? cita.getTipoServicio() : "Consulta General",
                cita.getMotivo() != null && !cita.getMotivo().isEmpty() ? "Motivo: " + cita.getMotivo() : ""
            );
            
            Map<String, Object> datos = new HashMap<>();
            datos.put("destinatario", cliente.getCorreo());
            datos.put("nombreCliente", cliente.getNombre() + " " + cliente.getApellido());
            datos.put("nombrePaciente", paciente.getNombre());
            datos.put("fechaCita", fechaFormateada);
            
            NotificacionEnviarRequest notifRequest = new NotificacionEnviarRequest();
            notifRequest.setTipo("CITA_PROGRAMADA");
            notifRequest.setMensaje(mensaje);
            notifRequest.setCanalId(1L); // ID del canal EMAIL (debe existir en la BD)
            notifRequest.setDatos(datos);
            
            notificacionService.enviarAhora(notifRequest);
            
        } catch (Exception e) {
            // Log del error pero no falla la creación de la cita
            log.error("Error al enviar notificación de cita: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Envía notificación por email al cliente cuando se reprograma una cita.
     * 
     * @param cita La cita que se acaba de reprogramar
     * @param fechaAnterior La fecha anterior de la cita
     */
    @SuppressWarnings("unused")
    private void enviarNotificacionCitaReprogramada(Cita cita, LocalDateTime fechaAnterior) {
        try {
            Paciente paciente = cita.getPaciente();
            Cliente cliente = paciente != null ? paciente.getCliente() : null;
            
            log.debug("Reprogramar - Paciente ID: {}, Cliente Correo: {}", 
                paciente != null ? paciente.getIdPaciente() : null,
                cliente != null ? cliente.getCorreo() : null);
            
            if (cliente == null || cliente.getCorreo() == null || cliente.getCorreo().isEmpty()) {
                log.warn("No se puede enviar email de reprogramación - Cliente o correo no disponible");
                return;
            }
            
            UsuarioVeterinario veterinario = cita.getVeterinario();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fechaNueva = cita.getFechaHora().format(formatter);
            String fechaVieja = fechaAnterior.format(formatter);
            
            String mensaje = String.format(
                "Estimado/a %s %s,\n\n" +
                "Le informamos que su cita ha sido reprogramada.\n\n" +
                "CAMBIO DE HORARIO:\n\n" +
                "Fecha Anterior: %s\n" +
                "Nueva Fecha: %s\n\n" +
                "DETALLES DE LA CITA:\n\n" +
                "Paciente: %s (%s)\n" +
                "Veterinario: Dr. %s %s\n\n" +
                "RECORDATORIO IMPORTANTE:\n" +
                "Por favor, tome nota del nuevo horario y llegue 10 minutos antes de su cita para completar el registro.\n\n" +
                "Si tiene alguna consulta, no dude en contactarnos.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Clínica Veterinaria Humboldt",
                cliente.getNombre(),
                cliente.getApellido(),
                fechaVieja,
                fechaNueva,
                paciente.getNombre(),
                paciente.getEspecie(),
                veterinario.getNombre(),
                veterinario.getApellido()
            );
            
            Map<String, Object> datos = new HashMap<>();
            datos.put("destinatario", cliente.getCorreo());
            datos.put("nombreCliente", cliente.getNombre() + " " + cliente.getApellido());
            datos.put("fechaNueva", fechaNueva);
            datos.put("fechaAnterior", fechaVieja);
            
            NotificacionEnviarRequest notifRequest = new NotificacionEnviarRequest();
            notifRequest.setTipo("CITA_REPROGRAMADA");
            notifRequest.setMensaje(mensaje);
            notifRequest.setCanalId(1L);
            notifRequest.setDatos(datos);
            
            notificacionService.enviarAhora(notifRequest);
            
        } catch (Exception e) {
            log.error("Error al enviar notificación de reprogramación: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene los horarios disponibles y ocupados para un veterinario en una fecha específica.
     * 
     * @param veterinarioId ID del veterinario
     * @param fecha Fecha para consultar (YYYY-MM-DD)
     * @return Lista de horarios con su estado de disponibilidad
     */
    @Transactional(readOnly = true)
    public List<com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse> obtenerHorariosDelDia(
            Long veterinarioId, 
            java.time.LocalDate fecha) {
        
        log.info("🔍 Obteniendo horarios disponibles para veterinario {} en fecha {}", veterinarioId, fecha);
        
        // Validar que el veterinario exista
        Usuario usuario = usuarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", "id", veterinarioId));
        
        if (!(usuario instanceof UsuarioVeterinario)) {
            throw new BusinessException("El usuario indicado no es un veterinario");
        }
        
        // Obtener todas las citas del veterinario para ese día
        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);
        
        List<Cita> citasDelDia = citaRepository.findByVeterinarioAndFechaHoraBetween(
                (UsuarioVeterinario) usuario, 
                inicioDia, 
                finDia
        );
        
        // Generar horarios cada 30 minutos según el día de la semana
        List<com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse> horarios = new java.util.ArrayList<>();
        int diaSemana = fecha.getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo
        
        // Domingo - Cerrado
        if (diaSemana == 7) {
            return horarios; // Lista vacía
        }
        
        // Sábado - Solo mañana (8:00 - 12:00)
        if (diaSemana == 6) {
            generarHorarios(fecha, 8, 12, citasDelDia, horarios);
        } else {
            // Lunes a Viernes - Mañana (8:00 - 12:00) y Tarde (14:00 - 18:00)
            generarHorarios(fecha, 8, 12, citasDelDia, horarios);
            generarHorarios(fecha, 14, 18, citasDelDia, horarios);
        }
        
        log.info("✅ Se generaron {} horarios para la fecha {}", horarios.size(), fecha);
        return horarios;
    }
    
    /**
     * Genera horarios cada 30 minutos en un rango específico.
     */
    private void generarHorarios(
            java.time.LocalDate fecha,
            int horaInicio,
            int horaFin,
            List<Cita> citasDelDia,
            List<com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse> horarios) {
        
        for (int hora = horaInicio; hora < horaFin; hora++) {
            for (int minuto : new int[]{0, 30}) {
                LocalDateTime horario = fecha.atTime(hora, minuto);
                
                // Buscar si hay una cita en este horario
                Cita citaExistente = citasDelDia.stream()
                        .filter(c -> c.getFechaHora().equals(horario))
                        .findFirst()
                        .orElse(null);
                
                com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse horarioResponse;
                
                if (citaExistente != null) {
                    // Horario ocupado
                    horarioResponse = com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse.builder()
                            .fechaHora(horario)
                            .disponible(false)
                            .estado(com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse.EstadoHorario.OCUPADO)
                            .duracionMinutos(30) // Por defecto 30 minutos
                            .citaId(citaExistente.getIdCita())
                            .nombrePaciente(citaExistente.getPaciente() != null ? 
                                    citaExistente.getPaciente().getNombre() : "N/A")
                            .build();
                } else {
                    // Horario disponible
                    horarioResponse = com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse.builder()
                            .fechaHora(horario)
                            .disponible(true)
                            .estado(com.tuorg.veterinaria.prestacioneservicios.dto.HorarioDisponibilidadResponse.EstadoHorario.DISPONIBLE)
                            .duracionMinutos(30)
                            .build();
                }
                
                horarios.add(horarioResponse);
            }
        }
    }
}


