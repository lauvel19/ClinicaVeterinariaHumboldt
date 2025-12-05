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
import org.springframework.cache.annotation.Cacheable;
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
     * Servicio de almacenamiento de archivos.
     */
    private final com.tuorg.veterinaria.common.storage.FileStorageService fileStorageService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param pacienteRepository Repositorio de pacientes
     * @param historiaClinicaRepository Repositorio de historias clínicas
     * @param usuarioRepository Repositorio de usuarios
     * @param fileStorageService Servicio de almacenamiento de archivos
     */
    @Autowired
    public PacienteService(PacienteRepository pacienteRepository,
                          HistoriaClinicaRepository historiaClinicaRepository,
                          UsuarioRepository usuarioRepository,
                          com.tuorg.veterinaria.common.storage.FileStorageService fileStorageService) {
        this.pacienteRepository = pacienteRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Registra un nuevo paciente y crea su historia clínica asociada.
     * 
     * Esta operación es transaccional: si falla la creación de la historia
     * clínica, se revierte la creación del paciente.
     * 
     * @param request Datos del paciente a registrar
     * @return Paciente creado con su historia clínica
     */
    @Transactional
    public PacienteResponse registrarPaciente(PacienteRequest request) {
        // Validar especie
        if (!AppConstants.ESPECIE_PERRO.equalsIgnoreCase(request.getEspecie()) &&
            !AppConstants.ESPECIE_GATO.equalsIgnoreCase(request.getEspecie())) {
            throw new BusinessException("La especie debe ser 'perro' o 'gato'");
        }

        // Validar fecha de nacimiento
        if (request.getFechaNacimiento() != null &&
            request.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new BusinessException("La fecha de nacimiento no puede ser futura");
        }

        // Validar peso
        if (request.getPesoKg() != null) {
            ValidationUtil.validatePositiveNumber(
                    request.getPesoKg().doubleValue(), "peso_kg");
        }

        // Validar que el cliente exista
        Cliente cliente = obtenerCliente(request.getClienteId());

        Paciente paciente = new Paciente();
        paciente.setNombre(request.getNombre());
        paciente.setEspecie(request.getEspecie());
        paciente.setRaza(request.getRaza());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setSexo(request.getSexo());
        paciente.setPesoKg(request.getPesoKg());
        paciente.setEstadoSalud(request.getEstadoSalud());
        paciente.setFotoPerfil(request.getFotoPerfil());
        paciente.setCliente(cliente);

        // Generar identificador externo si no existe
        paciente.setIdentificadorExterno(
                request.getIdentificadorExterno() != null ? request.getIdentificadorExterno() : UUID.randomUUID());

        // Guardar paciente
        Paciente pacienteGuardado = pacienteRepository.save(paciente);

        // Crear historia clínica asociada (invariante: cada paciente tiene al menos 1 historia)
        HistoriaClinica historiaClinica = new HistoriaClinica();
        historiaClinica.setPaciente(pacienteGuardado);
        historiaClinica.setFechaApertura(LocalDateTime.now());
        historiaClinica.setResumen("Historia clínica creada automáticamente al registrar el paciente");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origen", "registro automático");
        metadata.put("creadoPor", "sistema");
        historiaClinica.setMetadatos(metadata);

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
     * Actualiza los datos de un paciente.
     * 
     * @param id ID del paciente
     * @param request Datos actualizados del paciente
     * @return Paciente actualizado
     */
    @Transactional
    @CacheEvict(value = "pacientesPorCliente", allEntries = true)
    public PacienteResponse actualizarDatos(Long id, PacienteUpdateRequest request) {
        Paciente pacienteExistente = obtenerPacienteEntidad(id);

        // Actualizar campos permitidos
        if (request.getNombre() != null) {
            pacienteExistente.setNombre(request.getNombre());
        }
        if (request.getEspecie() != null) {
            if (!AppConstants.ESPECIE_PERRO.equalsIgnoreCase(request.getEspecie()) &&
                !AppConstants.ESPECIE_GATO.equalsIgnoreCase(request.getEspecie())) {
                throw new BusinessException("La especie debe ser 'perro' o 'gato'");
            }
            pacienteExistente.setEspecie(request.getEspecie());
        }
        if (request.getRaza() != null) {
            pacienteExistente.setRaza(request.getRaza());
        }
        if (request.getFechaNacimiento() != null) {
            if (request.getFechaNacimiento().isAfter(LocalDate.now())) {
                throw new BusinessException("La fecha de nacimiento no puede ser futura");
            }
            pacienteExistente.setFechaNacimiento(request.getFechaNacimiento());
        }
        if (request.getSexo() != null) {
            pacienteExistente.setSexo(request.getSexo());
        }
        if (request.getPesoKg() != null) {
            ValidationUtil.validatePositiveNumber(
                    request.getPesoKg().doubleValue(), "peso_kg");
            pacienteExistente.setPesoKg(request.getPesoKg());
        }
        if (request.getEstadoSalud() != null) {
            pacienteExistente.setEstadoSalud(request.getEstadoSalud());
        }
        if (request.getFotoPerfil() != null) {
            pacienteExistente.setFotoPerfil(request.getFotoPerfil());
        }
        if (request.getClienteId() != null) {
            Cliente nuevoCliente = obtenerCliente(request.getClienteId());
            pacienteExistente.setCliente(nuevoCliente);
        }
        if (request.getIdentificadorExterno() != null) {
            pacienteExistente.setIdentificadorExterno(request.getIdentificadorExterno());
        }

        Paciente actualizado = pacienteRepository.save(pacienteExistente);
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

    /**
     * Actualiza la foto de perfil de un paciente.
     * 
     * @param id ID del paciente
     * @param file Archivo de imagen
     * @return URL de la imagen guardada
     * @throws IOException Si hay error al guardar el archivo
     */
    @Transactional
    public String actualizarFotoPerfil(Long id, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        Paciente paciente = obtenerPacienteEntidad(id);
        
        // Eliminar foto anterior si existe
        if (paciente.getFotoPerfil() != null) {
            fileStorageService.eliminarFoto(paciente.getFotoPerfil());
        }
        
        // Guardar nueva foto
        String fotoUrl = fileStorageService.guardarFotoPaciente(file, id);
        paciente.setFotoPerfil(fotoUrl);
        pacienteRepository.save(paciente);
        
        return fotoUrl;
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
                paciente.getFotoPerfil(),
                owner,
                paciente.getIdentificadorExterno()
        );
    }
}


