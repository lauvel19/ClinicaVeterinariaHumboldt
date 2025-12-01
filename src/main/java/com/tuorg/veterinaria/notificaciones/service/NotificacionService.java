package com.tuorg.veterinaria.notificaciones.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.notificaciones.dto.NotificacionEnviarRequest;
import com.tuorg.veterinaria.notificaciones.dto.NotificacionProgramarRequest;
import com.tuorg.veterinaria.notificaciones.dto.NotificacionResponse;
import com.tuorg.veterinaria.notificaciones.model.CanalEnvio;
import com.tuorg.veterinaria.notificaciones.model.Notificacion;
import com.tuorg.veterinaria.notificaciones.repository.CanalEnvioRepository;
import com.tuorg.veterinaria.notificaciones.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de notificaciones.
 *
 * Implementa el patrón Strategy para delegar el envío en los canales concretos
 * y expone DTOs desacoplados de las entidades JPA.
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final CanalEnvioRepository canalEnvioRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificacionService(NotificacionRepository notificacionRepository,
                               CanalEnvioRepository canalEnvioRepository,
                               ObjectMapper objectMapper) {
        this.notificacionRepository = notificacionRepository;
        this.canalEnvioRepository = canalEnvioRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * 📧 PROGRAMAR ENVÍO DE UNA NOTIFICACIÓN (Envío diferido en fecha futura)
     * 
     * Registra una notificación para ser enviada en una fecha/hora futura específica.
     * La notificación se almacena en estado PENDIENTE y será procesada por un scheduler
     * (Quartz/TaskScheduler) cuando llegue la fecha programada.
     * 
     * ⏰ FLUJO DE PROGRAMACIÓN (4 PASOS):
     * 1️⃣  Crear entidad Notificacion con datos del request
     * 2️⃣  Establecer estado = PENDIENTE (aún no enviada)
     * 3️⃣  Serializar datos JSON (payload adicional)
     * 4️⃣  Guardar en BD con fecha de envío programada
     * 
     * 🗓️ CARACTERÍSTICAS:
     *    • Envío diferido: Se ejecuta en fecha futura programada
     *    • Canal de envío: Se especifica en tiempo de programación
     *    • Payload flexible: JSON con datos adicionales
     *    • Auditoría: Timestamp de programación se registra automáticamente
     * 
     * 📋 PATRÓN UTILIZADO:
     *    • DTO de request → Entity → Guardar → DTO de response
     *    • Desacoplamiento: DTOs no exponen detalles de entidades JPA
     *    • ObjectMapper: Manejo de JSON para datos adicionales
     * 
     * 🔐 FLUJO DE EJECUCIÓN:
     *    • Scheduler externo (Quartz/TaskScheduler) consulta PENDIENTES
     *    • Para cada notificación expirada: llama a enviarAhora()
     *    • Si envío exitoso: PENDIENTE → ENVIADA
     *    • Si falla: PENDIENTE → FALLIDA (reintentos posibles)
     * 
     * @param request NotificacionProgramarRequest con:
     *        - tipo: String (EMAIL | SMS | PUSH | NOTIFICACION_INTERNA)
     *        - mensaje: String (contenido a enviar)
     *        - fechaEnvio: LocalDateTime (cuándo enviar)
     *        - datos: Map<String, Object> (payload adicional, ej: {"asunto": "Cita", "destinatario": "email"})
     * 
     * @return NotificacionResponse con:
     *         - idNotificacion: Long
     *         - tipo: String
     *         - mensaje: String
     *         - estado: PENDIENTE
     *         - fechaEnvioProgramada: LocalDateTime (la especificada en request)
     *         - fechaCreacion: LocalDateTime (timestamp actual)
     * 
     * @throws JsonProcessingException si falla la serialización del JSON
     * 
     * @example
     *   NotificacionProgramarRequest req = new NotificacionProgramarRequest();
     *   req.setTipo("EMAIL");
     *   req.setMensaje("Recuerdo de cita veterinaria");
     *   req.setFechaEnvio(LocalDateTime.now().plusDays(1));
     *   Map<String, Object> datos = Map.of(
     *       "destinatario", "cliente@email.com",
     *       "asunto", "Recordatorio de Cita",
     *       "citaId", 42L
     *   );
     *   req.setDatos(datos);
     *   NotificacionResponse respuesta = notificacionService.programarEnvio(req);
     *   // Resultado: estado = PENDIENTE, se enviará mañana a la hora especificada
     */
    @Transactional
    public NotificacionResponse programarEnvio(NotificacionProgramarRequest request) {
        // ✓ PASO 1: Crear entidad Notificacion
        Notificacion notificacion = new Notificacion();
        notificacion.setTipo(request.getTipo());                                // EMAIL | SMS | PUSH | etc
        notificacion.setMensaje(request.getMensaje());                          // Contenido a enviar
        notificacion.setFechaEnvioProgramada(request.getFechaEnvio());          // Cuándo enviar
        
        // ✓ PASO 2: Establecer estado inicial = PENDIENTE
        notificacion.setEstado(AppConstants.ESTADO_NOTIFICACION_PENDIENTE);
        
        // ✓ PASO 3: Serializar datos adicionales a JSON
        notificacion.setDatos(toJson(request.getDatos()));                      // Payload flexible

        // ✓ PASO 4: Guardar en BD (@Transactional garantiza consistencia)
        Notificacion guardada = notificacionRepository.save(notificacion);
        
        return mapToResponse(guardada);
    }

    /**
     * 📤 ENVIAR NOTIFICACIÓN INMEDIATAMENTE (Envío sincrónico con Strategy pattern)
     * 
     * Envía una notificación de forma inmediata usando el canal de envío especificado.
     * Implementa el patrón Strategy: cada CanalEnvio tiene su propia lógica de envío
     * (EMAIL, SMS, PUSH, etc.) encapsulada en el método enviar().
     * 
     * 🔄 FLUJO DE ENVÍO INMEDIATO (6 PASOS):
     * 1️⃣  Crear entidad Notificacion con datos del request
     * 2️⃣  Establecer estado inicial = PENDIENTE
     * 3️⃣  Serializar datos JSON (payload adicional)
     * 4️⃣  Buscar CanalEnvio por ID (ej: EMAIL, SMS, PUSH)
     * 5️⃣  Ejecutar estrategia del canal: canal.enviar(notificacion)
     *     • Si exitoso: retorna true
     *     • Si falla: retorna false (error en envío)
     * 6️⃣  Actualizar estado basado en resultado:
     *     • true  → ENVIADA + fechaEnvioReal = NOW
     *     • false → FALLIDA + fechaEnvioReal = NOW (para auditoría)
     * 7️⃣  Guardar en BD con auditoría completa
     * 
     * 🏛️ PATRÓN STRATEGY IMPLEMENTADO:
     *    • CanalEnvio: Interfaz con método enviar()
     *    • EmailCanalEnvio: Implementación para EMAIL (SMTP)
     *    • SmsCanalEnvio: Implementación para SMS (API)
     *    • PushCanalEnvio: Implementación para PUSH
     *    • Ventaja: Fácil agregar nuevos canales sin cambiar NotificacionService
     * 
     * 📧 CANALES DISPONIBLES:
     *    • EMAIL: SMTP directo, HTML templates
     *    • SMS: API de SMS (Twilio, AWS SNS, etc.)
     *    • PUSH: Notificaciones push (Firebase, etc.)
     *    • NOTIFICACION_INTERNA: Sistema interno de la app
     * 
     * 🔐 AUDITORÍA Y TRAZABILIDAD:
     *    • fechaEnvioReal: Timestamp exacto de intento (exitoso o fallido)
     *    • estado: ENVIADA o FALLIDA (trazar problemas)
     *    • datos: Payload completo almacenado para debugging
     *    • @Transactional: Garantiza consistencia (todo o nada)
     * 
     * @param request NotificacionEnviarRequest con:
     *        - tipo: String (EMAIL | SMS | PUSH | NOTIFICACION_INTERNA)
     *        - mensaje: String (contenido a enviar)
     *        - canalId: Long (ID del CanalEnvio a usar)
     *        - datos: Map<String, Object> (payload adicional con config del canal)
     * 
     * @return NotificacionResponse con:
     *         - idNotificacion: Long
     *         - tipo: String
     *         - mensaje: String
     *         - estado: ENVIADA (si fue exitoso) o FALLIDA (si hubo error)
     *         - fechaEnvioReal: LocalDateTime (timestamp del intento)
     *         - datos: Map con payload
     * 
     * @throws ResourceNotFoundException si canalId no existe en BD
     * 
     * @example
     *   NotificacionEnviarRequest req = new NotificacionEnviarRequest();
     *   req.setTipo("EMAIL");
     *   req.setMensaje("Tu cita está confirmada para mañana");
     *   req.setCanalId(1L); // ID del canal EMAIL
     *   Map<String, Object> datos = Map.of(
     *       "destinatario", "cliente@email.com",
     *       "asunto", "Confirmación de Cita",
     *       "citaId", 42L
     *   );
     *   req.setDatos(datos);
     *   NotificacionResponse respuesta = notificacionService.enviarAhora(req);
     *   // Resultado: estado = ENVIADA (si SMTP fue exitoso) o FALLIDA (si error)
     *   // Timestamp de intento guardado en fechaEnvioReal
     */
    @Transactional
    public NotificacionResponse enviarAhora(NotificacionEnviarRequest request) {
        // ✓ PASO 1: Crear entidad Notificacion
        Notificacion notificacion = new Notificacion();
        notificacion.setTipo(request.getTipo());                               // EMAIL | SMS | PUSH | etc
        notificacion.setMensaje(request.getMensaje());                         // Contenido a enviar
        
        // ✓ PASO 2: Establecer estado inicial = PENDIENTE (hasta verificar envío)
        notificacion.setEstado(AppConstants.ESTADO_NOTIFICACION_PENDIENTE);
        
        // ✓ PASO 3: Serializar datos adicionales a JSON
        notificacion.setDatos(toJson(request.getDatos()));

        // ✓ PASO 4: Buscar CanalEnvio en BD (lanza excepción si no existe)
        CanalEnvio canal = canalEnvioRepository.findById(request.getCanalId())
                .orElseThrow(() -> new ResourceNotFoundException("CanalEnvio", "id", request.getCanalId()));

        // ✓ PASO 5: Ejecutar estrategia del canal (patrón Strategy)
        // Cada canal tiene su propia implementación:
        // - EmailCanalEnvio.enviar() → SMTP, templates HTML
        // - SmsCanalEnvio.enviar() → API SMS (Twilio, AWS)
        // - PushCanalEnvio.enviar() → Firebase, OneSignal, etc.
        boolean enviado = canal.enviar(notificacion);

        // ✓ PASO 6 & 7: Actualizar estado basado en resultado del envío
        notificacion.setFechaEnvioReal(LocalDateTime.now());                  // Timestamp exacto del intento
        notificacion.setEstado(enviado
                ? AppConstants.ESTADO_NOTIFICACION_ENVIADA             // ✓ Exitoso
                : AppConstants.ESTADO_NOTIFICACION_FALLIDA);            // ❌ Falló

        // Guardar en BD con auditoría completa
        Notificacion guardada = notificacionRepository.save(notificacion);
        return mapToResponse(guardada);
    }

    /**
     * 🔍 OBTENER NOTIFICACIONES PENDIENTES (Consultar las que deben enviarse ahora)
     * 
     * Recupera todas las notificaciones que están en estado PENDIENTE y cuya
     * fecha de envío programada ya ha llegado (o es anterior a ahora).
     * 
     * Este método es típicamente llamado por un SCHEDULER (Quartz, Spring @Scheduled)
     * para procesar envíos pendientes de forma automática:
     * 
     * 📅 FLUJO DE PROCESAMIENTO CON SCHEDULER:
     * 1️⃣  Scheduler se ejecuta cada N minutos (ej: cada 5 minutos)
     * 2️⃣  Consulta obtenerPendientes() → obtiene notificaciones para enviar
     * 3️⃣  Para cada notificación:
     *     • Llamar a enviarAhora() con canal apropiado
     *     • Actualizar estado a ENVIADA o FALLIDA
     *     • Registrar resultado en BD
     * 4️⃣  Próxima ejecución en el siguiente ciclo
     * 
     * ✅ CONDICIÓN DE FILTRADO:
     *    • Estado = PENDIENTE (aún no enviada)
     *    • fechaEnvioProgramada <= LocalDateTime.now() (la hora ya llegó)
     * 
     * 💾 PERFORMANCE Y QUERIES:
     *    • Query optimizada con índice en (estado, fechaEnvioProgramada)
     *    • Solo-lectura: @Transactional(readOnly = true)
     *    • N+1 query problem evitado con eager loading si aplica
     *    • Pagination recomendada para volúmenes altos
     * 
     * 🔄 REINTENTOS Y MANEJO DE ERRORES:
     *    • Si FALLIDA: puede llamarse nuevamente en siguiente ciclo
     *    • Contador de reintentos: puede existir en modelo Notificacion
     *    • Backoff exponencial: aumentar delay entre reintentos
     *    • DLQ (Dead Letter Queue): después de N reintentos fallidos
     * 
     * @return List<NotificacionResponse> con notificaciones PENDIENTES con
     *         fechaEnvioProgramada <= NOW, convertidas a DTO
     *         Lista vacía si no hay notificaciones pendientes
     * 
     * @example
     *   // En una clase Scheduled:
     *   @Scheduled(fixedDelay = 300000) // Cada 5 minutos
     *   public void procesarNotificacionesPendientes() {
     *       List<NotificacionResponse> pendientes = notificacionService.obtenerPendientes();
     *       for (NotificacionResponse notif : pendientes) {
     *           // Procesar con canal apropiado según tipo
     *           if ("EMAIL".equals(notif.getTipo())) {
     *               enviarConCanalEmail(notif);
     *           } else if ("SMS".equals(notif.getTipo())) {
     *               enviarConCanalSms(notif);
     *           }
     *       }
     *   }
     */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerPendientes() {
        // Consultar todas las notificaciones PENDIENTE con fecha programada <= NOW
        return notificacionRepository.findNotificacionesPendientes(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)                                     // Convertir Entidad → DTO
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las notificaciones.
     */
    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerTodas() {
        return notificacionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NotificacionResponse mapToResponse(Notificacion notificacion) {
        return NotificacionResponse.builder()
                .id(notificacion.getIdNotificacion())
                .tipo(notificacion.getTipo())
                .mensaje(notificacion.getMensaje())
                .estado(notificacion.getEstado())
                .fechaEnvioProgramada(notificacion.getFechaEnvioProgramada())
                .fechaEnvioReal(notificacion.getFechaEnvioReal())
                .datos(toMap(notificacion.getDatos()))
                .build();
    }

    private String toJson(Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new BusinessException("Los datos de la notificación no tienen un formato JSON válido");
        }
    }

    private Map<String, Object> toMap(String datosJson) {
        if (datosJson == null || datosJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(datosJson, Map.class);
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }
}

