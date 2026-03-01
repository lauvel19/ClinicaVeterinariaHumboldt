package com.tuorg.veterinaria.gestioninventario.service;

import com.tuorg.veterinaria.common.exception.ResourceNotFoundException;
import com.tuorg.veterinaria.gestioninventario.dto.ProveedorRequest;
import com.tuorg.veterinaria.gestioninventario.dto.ProveedorResponse;
import com.tuorg.veterinaria.gestioninventario.model.Proveedor;
import com.tuorg.veterinaria.gestioninventario.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de proveedores.
 * 
 * Este servicio proporciona métodos para crear, actualizar, eliminar
 * y consultar proveedores.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@SuppressWarnings("null")
@Service
public class ProveedorService {

    /**
     * Repositorio de proveedores.
     */
    private final ProveedorRepository proveedorRepository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param proveedorRepository Repositorio de proveedores
     */
    @Autowired
    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    /**
     * Crea un nuevo proveedor.
     * 
     * @param request Datos del proveedor a crear
     * @return Proveedor creado
     */
    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(request.getNombre());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setCorreo(request.getCorreo());

        Proveedor guardado = proveedorRepository.save(proveedor);
        return mapToResponse(guardado);
    }

    /**
     * Obtiene un proveedor por su ID.
     * 
     * @param id ID del proveedor
     * @return Proveedor encontrado
     */
    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Long id) {
        Proveedor proveedor = obtenerEntidad(id);
        return mapToResponse(proveedor);
    }

    /**
     * Obtiene todos los proveedores.
     * 
     * @return Lista de proveedores
     */
    @Transactional(readOnly = true)
    public List<ProveedorResponse> obtenerTodos() {
        return proveedorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un proveedor existente.
     * 
     * @param id      ID del proveedor
     * @param request Datos actualizados del proveedor
     * @return Proveedor actualizado
     */
    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = obtenerEntidad(id);

        if (request.getNombre() != null) {
            proveedor.setNombre(request.getNombre());
        }
        if (request.getContacto() != null) {
            proveedor.setContacto(request.getContacto());
        }
        if (request.getTelefono() != null) {
            proveedor.setTelefono(request.getTelefono());
        }
        if (request.getDireccion() != null) {
            proveedor.setDireccion(request.getDireccion());
        }
        if (request.getCorreo() != null) {
            proveedor.setCorreo(request.getCorreo());
        }

        Proveedor actualizado = proveedorRepository.save(proveedor);
        return mapToResponse(actualizado);
    }

    /**
     * Elimina un proveedor.
     * 
     * @param id ID del proveedor
     */
    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = obtenerEntidad(id);
        proveedorRepository.delete(proveedor);
    }

    @Transactional(readOnly = true)
    private Proveedor obtenerEntidad(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
    }

    private ProveedorResponse mapToResponse(Proveedor proveedor) {
        return ProveedorResponse.builder()
                .idProveedor(proveedor.getIdProveedor())
                .nombre(proveedor.getNombre())
                .contacto(proveedor.getContacto())
                .telefono(proveedor.getTelefono())
                .direccion(proveedor.getDireccion())
                .correo(proveedor.getCorreo())
                .build();
    }
}
