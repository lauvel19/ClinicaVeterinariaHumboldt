package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.PermisoRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para PermisoRol (RBAC - Role-Based Access Control).
 */
@Repository
public interface PermisoRolRepository extends JpaRepository<PermisoRol, Long> {

    /**
     * Obtiene todos los permisos activos de un rol.
     * 
     * @param rolId ID del rol
     * @return Lista de permisos activos del rol
     */
    @Query("SELECT pr FROM PermisoRol pr WHERE pr.rol.idRol = :rolId AND pr.activo = true")
    List<PermisoRol> findByRolIdAndActivoTrue(@Param("rolId") Long rolId);

    /**
     * Obtiene permisos de un rol para un módulo específico.
     * 
     * @param rolId ID del rol
     * @param modulo Nombre del módulo
     * @return Lista de permisos del rol en ese módulo
     */
    @Query("SELECT pr FROM PermisoRol pr WHERE pr.rol.idRol = :rolId AND pr.modulo = :modulo AND pr.activo = true")
    List<PermisoRol> findByRolIdAndModulo(@Param("rolId") Long rolId, @Param("modulo") String modulo);

    /**
     * Verifica si un rol tiene un permiso específico.
     * 
     * @param rolId ID del rol
     * @param modulo Nombre del módulo
     * @param accion Acción a verificar
     * @return true si el rol tiene ese permiso activo
     */
    @Query("SELECT COUNT(pr) > 0 FROM PermisoRol pr WHERE pr.rol.idRol = :rolId AND pr.modulo = :modulo AND pr.accion = :accion AND pr.activo = true")
    boolean existsByRolIdAndModuloAndAccionAndActivoTrue(
        @Param("rolId") Long rolId, 
        @Param("modulo") String modulo, 
        @Param("accion") String accion
    );

    /**
     * Obtiene un permiso específico por rol, módulo y acción.
     * 
     * @param rolId ID del rol
     * @param modulo Nombre del módulo
     * @param accion Nombre de la acción
     * @return Permiso encontrado
     */
    @Query("SELECT pr FROM PermisoRol pr WHERE pr.rol.idRol = :rolId AND pr.modulo = :modulo AND pr.accion = :accion")
    Optional<PermisoRol> findByRolIdAndModuloAndAccion(
        @Param("rolId") Long rolId, 
        @Param("modulo") String modulo, 
        @Param("accion") String accion
    );

    /**
     * Obtiene todos los permisos activos.
     * 
     * @return Lista de todos los permisos activos
     */
    List<PermisoRol> findByActivoTrue();
}
