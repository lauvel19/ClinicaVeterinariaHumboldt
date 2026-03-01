package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.common.util.ValidationUtil;
import com.tuorg.veterinaria.gestionusuarios.dto.PermisoResponse;
import com.tuorg.veterinaria.gestionusuarios.dto.RolResponse;
import com.tuorg.veterinaria.gestionusuarios.dto.UsuarioRequest;
import com.tuorg.veterinaria.gestionusuarios.dto.UsuarioResponse;
import com.tuorg.veterinaria.gestionusuarios.dto.UsuarioUpdateRequest;
import com.tuorg.veterinaria.gestionusuarios.model.Permiso;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioVeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios.
 * 
 * Este servicio proporciona métodos para crear, actualizar, eliminar
 * y consultar usuarios del sistema.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Service
public class UsuarioService {

    /**
     * Repositorio de usuarios.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Repositorio de veterinarios.
     */
    private final UsuarioVeterinarioRepository veterinarioRepository;

    /**
     * Codificador de contraseñas.
     */
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param usuarioRepository Repositorio de usuarios
     * @param veterinarioRepository Repositorio de veterinarios
     * @param passwordEncoder Codificador de contraseñas
     * @param rolRepository Repositorio de roles
     */
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository,
                          UsuarioVeterinarioRepository veterinarioRepository,
                          PasswordEncoder passwordEncoder,
                          RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
    }

    /**
     * Crea un nuevo usuario.
     * 
     * @param request Datos del nuevo usuario
     * @return Usuario creado
     */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        // Validar username
        ValidationUtil.validateUsername(request.getUsername());
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El nombre de usuario ya está en uso");
        }

        // Validar email
        ValidationUtil.validateEmail(request.getCorreo());
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("El correo electrónico ya está en uso");
        }

        // Validar teléfono si viene informado
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            ValidationUtil.validatePhone(request.getTelefono());
        }

        // Validar y codificar contraseña
        ValidationUtil.validatePassword(request.getPassword());

        Rol rol = obtenerRol(request.getRolId());

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setUsername(request.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(request.getActivo() != null ? request.getActivo() : Boolean.TRUE);

        Usuario guardado = usuarioRepository.save(usuario);
        return mapToResponse(guardado);
    }

    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return mapToResponse(usuario);
    }

    /**
     * Obtiene todos los usuarios.
     * 
     * @return Lista de usuarios
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Actualiza un usuario existente.
     * 
     * @param id ID del usuario
     * @param usuario Datos actualizados del usuario
     * @return Usuario actualizado
     */
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (request.getNombre() != null) {
            usuarioExistente.setNombre(request.getNombre());
        }
        if (request.getApellido() != null) {
            usuarioExistente.setApellido(request.getApellido());
        }
        if (request.getCorreo() != null) {
            ValidationUtil.validateEmail(request.getCorreo());
            if (!request.getCorreo().equalsIgnoreCase(usuarioExistente.getCorreo())
                    && usuarioRepository.existsByCorreo(request.getCorreo())) {
                throw new BusinessException("El correo electrónico ya está en uso");
            }
            usuarioExistente.setCorreo(request.getCorreo());
        }
        if (request.getTelefono() != null) {
            ValidationUtil.validatePhone(request.getTelefono());
            usuarioExistente.setTelefono(request.getTelefono());
        }
        if (request.getDireccion() != null) {
            usuarioExistente.setDireccion(request.getDireccion());
        }
        if (request.getUsername() != null) {
            ValidationUtil.validateUsername(request.getUsername());
            if (!request.getUsername().equals(usuarioExistente.getUsername())
                    && usuarioRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("El nombre de usuario ya está en uso");
            }
            usuarioExistente.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            ValidationUtil.validatePassword(request.getPassword());
            usuarioExistente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRolId() != null) {
            Rol rol = obtenerRol(request.getRolId());
            usuarioExistente.setRol(rol);
        }
        if (request.getActivo() != null) {
            usuarioExistente.setActivo(request.getActivo());
        }

        Usuario actualizado = usuarioRepository.save(usuarioExistente);
        return mapToResponse(actualizado);
    }

    /**
     * Elimina un usuario (soft delete - desactiva).
     * 
     * @param id ID del usuario
     */
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Actualiza el último acceso del usuario.
     * 
     * @param username Nombre de usuario
     */
    @Transactional
    public void actualizarUltimoAcceso(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    /**
     * Obtiene todos los veterinarios activos.
     * 
     * @return Lista de veterinarios activos
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "veterinariosActivos")
    public List<UsuarioResponse> obtenerVeterinariosActivos() {
        return veterinarioRepository.findAllActivos()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Rol obtenerRol(Long rolId) {
        if (rolId == null) {
            throw new BusinessException("Debe especificar un rol válido");
        }
        return rolRepository.findById(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", rolId));
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        RolResponse rolResponse = null;
        Rol rol = usuario.getRol();
        if (rol != null) {
            Set<PermisoResponse> permisos = rol.getPermisos()
                    .stream()
                    .map(this::mapToPermisoResponse)
                    .collect(Collectors.toSet());
            rolResponse = new RolResponse(rol.getIdRol(), rol.getNombreRol(), rol.getDescripcion(), permisos);
        }

        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getUsername(),
                usuario.getActivo(),
                usuario.getUltimoAcceso(),
                rolResponse
        );
    }

    private PermisoResponse mapToPermisoResponse(Permiso permiso) {
        return new PermisoResponse(permiso.getIdPermiso(), permiso.getNombre(), permiso.getDescripcion());
    }
}


