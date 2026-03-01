package com.tuorg.veterinaria.gestionpacientes.service;

import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.common.util.ValidationUtil;
import com.tuorg.veterinaria.gestionpacientes.dto.PacienteOwnerResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.PacienteRequest;
import com.tuorg.veterinaria.gestionpacientes.dto.PacienteResponse;
import com.tuorg.veterinaria.gestionpacientes.dto.PacienteUpdateRequest;
import com.tuorg.veterinaria.gestionpacientes.model.HistoriaClinica;
import com.tuorg.veterinaria.gestionpacientes.model.Paciente;
import com.tuorg.veterinaria.gestionpacientes.repository.HistoriaClinicaRepository;
import com.tuorg.veterinaria.gestionpacientes.repository.PacienteRepository;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
/**
 * Servicio para la gestión de pacientes.
 * 
 * Este servicio proporciona métodos para crear, actualizar, eliminar
 * y consultar pacientes. Al crear un paciente, automáticamente se crea
 * su historia clínica asociada.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class PacienteService {

    /**
     * Repositorio de pacientes.
     */
    private final PacienteRepository pacienteRepository;

    /**
     * Repositorio de historias clínicas.
     */
    private final HistoriaClinicaRepository historiaClinicaRepository;

    /**
     * Repositorio de usuarios (para validar cliente).
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param pacienteRepository Repositorio de pacientes
     * @param historiaClinicaRepository Repositorio de historias clínicas
     * @param usuarioRepository Repositorio de usuarios
     */
    @Autowired
    public PacienteService(PacienteRepository pacienteRepository,
                          HistoriaClinicaRepository historiaClinicaRepository,
                          UsuarioRepository usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * 🐕 REGISTRAR UN NUEVO PACIENTE (Crear paciente + Historia Clínica automática)
     * 
     * Realiza el registro completo de un paciente (mascota) con su historia clínica
     * asociada. Esta operación es fundamental en el flujo de la clínica veterinaria.
     * 
     * 📋 FLUJO DE REGISTRO (8 PASOS):
     * 1️⃣  Validar especie: Solo PERRO o GATO (restricción de negocio)
     * 2️⃣  Validar fecha de nacimiento: No puede ser futura (lógica temporal)
     * 3️⃣  Validar peso: Debe ser positivo (BigDecimal > 0)
     * 4️⃣  Validar que cliente exista: El paciente debe tener propietario válido
     * 5️⃣  Crear entidad Paciente con datos básicos:
     *     • nombre, especie, raza, sexo
     *     • fechaNacimiento, pesoKg, estadoSalud
     *     • Generar UUID externo para identificación única
     * 6️⃣  Guardar paciente en BD (retorna entidad con ID autogenerado)
     * 7️⃣  Crear Historia Clínica AUTOMÁTICAMENTE (Invariante: todo paciente debe tenerla)
     *     • Fecha de apertura = NOW
     *     • Resumen inicial: "Historia clínica creada automáticamente..."
     *     • Metadata: origen=automático, creadoPor=sistema
     * 8️⃣  Guardar historia en BD
     * 
     * 🔐 VALIDACIONES Y RESTRICCIONES:
     *    • Especie: Solo PERRO o GATO permitidas (validación enum)
     *    • Fecha: No puede ser futura (temporal)
     *    • Peso: Debe ser > 0 (lógica médica)
     *    • Cliente: Debe existir en BD (referencia FK)
     *    • Transacción atómica: Si falla historia, revierte paciente
     * 
     * 📚 INVARIANTES DEL DOMINIO:
     *    • Todo paciente DEBE tener una historia clínica asociada
     *    • La historia se crea en el mismo registro (atomicidad)
     *    • El UUID externo es único y persiste con el paciente
     * 
     * @param request PacienteRequest con:
     *        - nombre: String (nombre de la mascota)
     *        - especie: String (PERRO | GATO) - validado
     *        - raza: String (Labrador, Siamés, etc.)
     *        - sexo: String (M | H)
     *        - fechaNacimiento: LocalDate (no futura)
     *        - pesoKg: BigDecimal (> 0)
     *        - estadoSalud: String (SALUDABLE | ENFERMO | BAJO_TRATAMIENTO)
     *        - clienteId: Long (ID del propietario)
     *        - identificadorExterno: UUID (opcional, se genera si no existe)
     * 
     * @return PacienteResponse con:
     *         - idPaciente: Long (ID generado por BD)
     *         - identificadorExterno: UUID (único)
     *         - nombre, especie, raza, sexo, fechaNacimiento, pesoKg
     *         - cliente: ClienteSummary con datos del propietario
     * 
     * @throws BusinessException si:
     *         - Especie no es PERRO ni GATO
     *         - Fecha de nacimiento es futura
     *         - Peso no es positivo
     *         - Cliente no existe
     * @throws DataIntegrityViolationException si falla el save en BD
     * 
     * @example
     *   PacienteRequest req = new PacienteRequest();
     *   req.setNombre("Max");
     *   req.setEspecie("perro");
     *   req.setRaza("Labrador");
     *   req.setClienteId(42L);
     *   req.setPesoKg(new BigDecimal("28.5"));
     *   PacienteResponse respuesta = pacienteService.registrarPaciente(req);
     *   // Resultado: idPaciente=1, nombre=Max, especie=perro, historia=creada automáticamente
     */
    @Transactional
    public PacienteResponse registrarPaciente(PacienteRequest request) {
        // ❌ PASO 1: Validación de especie (solo PERRO o GATO)
        if (!AppConstants.ESPECIE_PERRO.equalsIgnoreCase(request.getEspecie()) &&
            !AppConstants.ESPECIE_GATO.equalsIgnoreCase(request.getEspecie())) {
            throw new BusinessException("La especie debe ser 'perro' o 'gato'");
        }

        // ❌ PASO 2: Validación de fecha de nacimiento (no puede ser futura)
        if (request.getFechaNacimiento() != null &&
            request.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de nacimiento no puede ser futura");
        }

        // ❌ PASO 3: Validación de peso (debe ser positivo para lógica médica)
        if (request.getPesoKg() != null) {
            ValidationUtil.validatePositiveNumber(
                    request.getPesoKg().doubleValue(), "peso_kg");
        }

        // ❌ PASO 4: Validación de cliente (debe existir en BD como FK)
        Cliente cliente = obtenerCliente(request.getClienteId());

        // ✓ PASO 5: Crear entidad Paciente con datos básicos
        Paciente paciente = new Paciente();
        paciente.setNombre(request.getNombre());                              // Nombre de la mascota
        paciente.setEspecie(request.getEspecie());                            // PERRO o GATO (validado)
        paciente.setRaza(request.getRaza());                                  // Raza específica
        paciente.setFechaNacimiento(request.getFechaNacimiento());            // Fecha de nacimiento
        paciente.setSexo(request.getSexo());                                  // M o H
        paciente.setPesoKg(request.getPesoKg());                              // Peso en kg
        paciente.setEstadoSalud(request.getEstadoSalud());                    // Estado actual
        paciente.setCliente(cliente);                                         // Relación con propietario

        // Generar identificador externo (UUID único para identificación sin exponer ID interno)
        paciente.setIdentificadorExterno(
                request.getIdentificadorExterno() != null ? request.getIdentificadorExterno() : UUID.randomUUID());

        // ✓ PASO 6: Guardar paciente en BD (obtiene ID autogenerado)
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // ✓ PASO 7 & 8: INVARIANTE - Crear Historia Clínica automáticamente
        // Justificación: Todo paciente veterinario DEBE tener al menos una historia clínica
        // Esta creación es ATÓMICA con el registro del paciente (@Transactional)
        HistoriaClinica historiaClinica = new HistoriaClinica();
        historiaClinica.setPaciente(pacienteGuardado);                         // Relación FK
        historiaClinica.setFechaApertura(LocalDateTime.now());                 // Timestamp exacto
        historiaClinica.setResumen("Historia clínica creada automáticamente al registrar el paciente");
        
        // Metadata para auditoría de origen de creación
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origen", "registro automático");
        metadata.put("creadoPor", "sistema");
        historiaClinica.setMetadatos(metadata);

        // Guardar historia en BD (si falla, @Transactional revierte paciente también)
        historiaClinicaRepository.save(historiaClinica);

        return mapToResponse(pacienteGuardado);
    }

    /**
     * Obtiene un paciente por su ID.
     * 
     * @param id ID del paciente
     * @return Paciente encontrado
     */
    @Transactional(readOnly = true)
    public PacienteResponse obtener(Long id) {
        Paciente paciente = obtenerPacienteEntidad(id);
        return mapToResponse(paciente);
    }

    /**
     * Obtiene todos los pacientes.
     * 
     * @return Lista de pacientes
     */
    @Transactional(readOnly = true)
    public List<PacienteResponse> obtenerTodos() {
        return pacienteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Obtiene pacientes por cliente (dueño).
     * 
     * @param clienteId ID del cliente
     * @return Lista de pacientes del cliente
     */
    @Transactional(readOnly = true)
    public List<PacienteResponse> obtenerPorCliente(Long clienteId) {
        obtenerCliente(clienteId);
        return pacienteRepository.findByClienteId(clienteId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * 📝 ACTUALIZAR DATOS DE UN PACIENTE (Modificación parcial de atributos)
     * 
     * Realiza la actualización parcial de datos de un paciente existente.
     * Utiliza patrón de "actualización selectiva": solo campos no-null en el request
     * son actualizados, preservando valores existentes para campos null.
     * 
     * 🔄 FLUJO DE ACTUALIZACIÓN (7 PASOS):
     * 1️⃣  Buscar paciente por ID → Validar existencia
     * 2️⃣  Para cada campo del request (si no es null):
     *     - Validar nuevo valor según reglas de negocio
     *     - Aplicar nueva valor a entidad
     * 3️⃣  Validaciones por campo:
     *     • Nombre: Update si no es null
     *     • Especie: Validar PERRO o GATO
     *     • Raza: Update si no es null
     *     • FechaNacimiento: No puede ser futura
     *     • Sexo: M o H
     *     • PesoKg: Debe ser positivo (validación médica)
     *     • EstadoSalud: SALUDABLE | ENFERMO | BAJO_TRATAMIENTO
     *     • ClienteId: Validar que cliente exista (FK)
     *     • IdentificadorExterno: UUID válido
     * 4️⃣  Invalidar caché: @CacheEvict en "pacientesPorCliente"
     * 5️⃣  Guardar cambios en BD
     * 6️⃣  Auditoría automática: Timestamp de actualización
     * 
     * 🛡️ PATRÓN DE ACTUALIZACIÓN SELECTIVA (Partial Update):
     *    • Si campo en request = null → No se actualiza (preserva valor actual)
     *    • Si campo en request != null → Se valida y actualiza
     *    • Esto evita sobrescribir accidentalmente con valores null
     * 
     * 💾 GESTIÓN DE CACHÉ:
     *    • @CacheEvict invalida cache de "pacientesPorCliente"
     *    • Garantiza que próximas consultas obtengan datos actualizados
     *    • Importante si cliente cambió
     * 
     * @param id ID del paciente a actualizar
     * @param request PacienteUpdateRequest con campos a actualizar (solo no-null):
     *        - nombre: String (opcional)
     *        - especie: String (opcional, PERRO | GATO si no es null)
     *        - raza: String (opcional)
     *        - fechaNacimiento: LocalDate (opcional, no futura)
     *        - sexo: String (opcional)
     *        - pesoKg: BigDecimal (opcional, > 0)
     *        - estadoSalud: String (opcional)
     *        - clienteId: Long (opcional, debe existir)
     *        - identificadorExterno: UUID (opcional)
     * 
     * @return PacienteResponse con datos actualizados
     * @throws ResourceNotFoundException si paciente no existe
     * @throws BusinessException si:
     *         - Especie no es PERRO ni GATO
     *         - Fecha de nacimiento es futura
     *         - Peso no es positivo
     *         - ClienteId no existe
     * 
     * @example
     *   PacienteUpdateRequest actualizar = new PacienteUpdateRequest();
     *   actualizar.setNombre("Max Actualizado");
     *   actualizar.setPesoKg(new BigDecimal("30.0")); // Ganó peso
     *   actualizar.setEstadoSalud("BAJO_TRATAMIENTO");
     *   // Los demás campos (raza, fecha, etc.) mantienen sus valores
     *   PacienteResponse resultado = pacienteService.actualizarDatos(1L, actualizar);
     */
    @Transactional
    @CacheEvict(value = "pacientesPorCliente", allEntries = true)
    public PacienteResponse actualizarDatos(Long id, PacienteUpdateRequest request) {
        // ✓ PASO 1: Buscar paciente existente (lanza excepción si no existe)
        Paciente pacienteExistente = obtenerPacienteEntidad(id);

        // ✓ PASO 2 & 3: Actualización selectiva - Solo campos no-null son actualizados
        
        // 📝 Campo: Nombre
        if (request.getNombre() != null) {
            pacienteExistente.setNombre(request.getNombre());
        }
        
        // 📝 Campo: Especie (validar PERRO o GATO)
        if (request.getEspecie() != null) {
            if (!AppConstants.ESPECIE_PERRO.equalsIgnoreCase(request.getEspecie()) &&
                !AppConstants.ESPECIE_GATO.equalsIgnoreCase(request.getEspecie())) {
                throw new BusinessException("La especie debe ser 'perro' o 'gato'");
            }
            pacienteExistente.setEspecie(request.getEspecie());
        }
        
        // 📝 Campo: Raza
        if (request.getRaza() != null) {
            pacienteExistente.setRaza(request.getRaza());
        }
        
        // 📝 Campo: Fecha de Nacimiento (validar no futura)
        if (request.getFechaNacimiento() != null) {
            if (request.getFechaNacimiento().isAfter(LocalDate.now())) {
                throw new BusinessException("La fecha de nacimiento no puede ser futura");
            }
            pacienteExistente.setFechaNacimiento(request.getFechaNacimiento());
        }
        
        // 📝 Campo: Sexo
        if (request.getSexo() != null) {
            pacienteExistente.setSexo(request.getSexo());
        }
        
        // 📝 Campo: Peso en kg (validar positivo para lógica médica)
        if (request.getPesoKg() != null) {
            ValidationUtil.validatePositiveNumber(
                    request.getPesoKg().doubleValue(), "peso_kg");
            pacienteExistente.setPesoKg(request.getPesoKg());
        }
        
        // 📝 Campo: Estado de Salud
        if (request.getEstadoSalud() != null) {
            pacienteExistente.setEstadoSalud(request.getEstadoSalud());
        }
        
        // 📝 Campo: Cliente (propietario) - validar que nuevo cliente exista
        if (request.getClienteId() != null) {
            Cliente nuevoCliente = obtenerCliente(request.getClienteId());
            pacienteExistente.setCliente(nuevoCliente);
        }
        
        // 📝 Campo: Identificador Externo
        if (request.getIdentificadorExterno() != null) {
            pacienteExistente.setIdentificadorExterno(request.getIdentificadorExterno());
        }

        // ✓ PASO 4 & 5: Guardar cambios en BD (@Transactional garantiza atomicidad)
        Paciente actualizado = pacienteRepository.save(pacienteExistente);
        
        // ✓ PASO 6: Auditoría - timestamp actualizado automáticamente por @EntityListeners
        // @CacheEvict invalida cache de pacientes por cliente
        
        return mapToResponse(actualizado);
    }

    /**
     * Genera un resumen clínico del paciente.
     * 
     * @param id ID del paciente
     * @return Resumen clínico en formato String
     */
    @Transactional(readOnly = true)
    public String generarResumenClinico(Long id) {
        Paciente paciente = obtenerPacienteEntidad(id);
        HistoriaClinica historia = historiaClinicaRepository.findByPacienteId(id)
                .orElseThrow(() -> new ResourceNotFoundException("HistoriaClinica", "paciente_id", id));

        StringBuilder resumen = new StringBuilder();
        resumen.append("Resumen Clínico - ").append(paciente.getNombre()).append("\n");
        resumen.append("Especie: ").append(paciente.getEspecie()).append("\n");
        resumen.append("Raza: ").append(paciente.getRaza() != null ? paciente.getRaza() : "N/A").append("\n");
        resumen.append("Peso: ").append(paciente.getPesoKg() != null ? 
                paciente.getPesoKg() + " kg" : "N/A").append("\n");
        resumen.append("Estado de salud: ").append(paciente.getEstadoSalud() != null ? 
                paciente.getEstadoSalud() : "N/A").append("\n");
        resumen.append("Historia clínica abierta: ").append(historia.getFechaApertura()).append("\n");

        return resumen.toString();
    }

    private Cliente obtenerCliente(Long clienteId) {
        Usuario usuario = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));

        if (!(usuario instanceof Cliente)) {
            throw new BusinessException("El usuario con id " + clienteId + " no corresponde a un cliente");
        }

        return (Cliente) usuario;
    }

    private Paciente obtenerPacienteEntidad(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", id));
    }

    private PacienteResponse mapToResponse(Paciente paciente) {
        Cliente cliente = paciente.getCliente();
        PacienteOwnerResponse owner = new PacienteOwnerResponse(
                cliente.getIdUsuario(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getCorreo()
        );

        return new PacienteResponse(
                paciente.getIdPaciente(),
                paciente.getNombre(),
                paciente.getEspecie(),
                paciente.getRaza(),
                paciente.getFechaNacimiento(),
                paciente.getSexo(),
                paciente.getPesoKg(),
                paciente.getEstadoSalud(),
                owner,
                paciente.getIdentificadorExterno()
        );
    }
}


