package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import com.tuorg.veterinaria.gestionusuarios.model.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa los permisos asignados a cada rol de usuario.
 * 
 * Permite configuración dinámica de permisos granulares por módulo y acción,
 * implementando un sistema RBAC (Role-Based Access Control) flexible.
 * 
 * Ejemplo: rol VETERINARIO puede tener permiso "historias:leer" y "historias:escribir"
 */
@Entity
@Table(name = "permisos_rol", schema = "public",
    uniqueConstraints = @UniqueConstraint(columnNames = {"rol_id", "modulo", "accion"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoRol extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    /**
     * Rol al que se le asigna el permiso.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    /**
     * Módulo del sistema al que aplica el permiso.
     * Ejemplos: usuarios, pacientes, citas, historias, inventario, reportes, configuracion
     */
    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;

    /**
     * Acción específica permitida dentro del módulo.
     * Ejemplos: leer, crear, editar, eliminar, exportar, aprobar
     */
    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    /**
     * Descripción del permiso para la interfaz de usuario.
     */
    @Column(name = "descripcion", length = 200)
    private String descripcion;

    /**
     * Indica si el permiso está activo.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
