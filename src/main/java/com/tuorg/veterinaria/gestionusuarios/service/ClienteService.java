package com.tuorg.veterinaria.gestionusuarios.service;

import com.tuorg.veterinaria.common.exception.BusinessException;
import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.common.util.ValidationUtil;
import com.tuorg.veterinaria.gestionusuarios.dto.ClienteRequest;
import com.tuorg.veterinaria.gestionusuarios.dto.ClienteResponse;
import com.tuorg.veterinaria.gestionusuarios.dto.ClienteUpdateRequest;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import com.tuorg.veterinaria.gestionusuarios.repository.ClienteRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.RolRepository;
import com.tuorg.veterinaria.gestionusuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        ValidationUtil.validateUsername(request.getUsername());
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El nombre de usuario ya está en uso");
        }

        ValidationUtil.validateEmail(request.getCorreo());
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("El correo electrónico ya está en uso");
        }

        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            ValidationUtil.validatePhone(request.getTelefono());
        }

        ValidationUtil.validatePassword(request.getPassword());

        Rol rolCliente = rolRepository.findByNombreRol("CLIENTE")
                .orElseThrow(() -> new BusinessException("El rol CLIENTE no está configurado"));

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setCorreo(request.getCorreo());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setUsername(request.getUsername());
        cliente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        cliente.setDocumentoIdentidad(request.getDocumentoIdentidad());
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setRol(rolCliente);
        cliente.setActivo(true);

        Cliente guardado = clienteRepository.save(Objects.requireNonNull(cliente));
        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        Cliente cliente = clienteRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return mapToResponse(cliente);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteUpdateRequest request) {
        Cliente cliente = clienteRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            cliente.setNombre(request.getNombre());
        }
        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            cliente.setApellido(request.getApellido());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            ValidationUtil.validateEmail(request.getCorreo());
            if (usuarioRepository.existsByCorreoAndIdPersonaNot(request.getCorreo(), Objects.requireNonNull(id))) {
                throw new BusinessException("El correo electrónico ya está en uso por otro usuario");
            }
            cliente.setCorreo(request.getCorreo());
        }
        if (request.getTelefono() != null) {
            if (request.getTelefono().isBlank()) {
                cliente.setTelefono(null);
            } else {
                ValidationUtil.validatePhone(request.getTelefono());
                cliente.setTelefono(request.getTelefono());
            }
        }
        if (request.getDireccion() != null) {
            cliente.setDireccion(request.getDireccion().isBlank() ? null : request.getDireccion());
        }
        if (request.getDocumentoIdentidad() != null) {
            cliente.setDocumentoIdentidad(
                    request.getDocumentoIdentidad().isBlank() ? null : request.getDocumentoIdentidad());
        }

        Cliente actualizado = clienteRepository.save(Objects.requireNonNull(cliente));
        return mapToResponse(actualizado);
    }

    private ClienteResponse mapToResponse(Cliente cliente) {
        return new ClienteResponse(
                Objects.requireNonNull(cliente.getIdUsuario()),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getCorreo(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getDocumentoIdentidad(),
                cliente.getFechaRegistro(),
                cliente.getUsername(),
                cliente.getActivo());
    }
}
