package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.AuditoriaDetallada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para AuditoriaDetallada (patrón Memento).
 * 
 * Permite consultas avanzadas de auditoría y análisis forense.
 */
@Repository
public interface AuditoriaDetalladaRepository extends JpaRepository<AuditoriaDetallada, Long> {

    /**
     * Obtiene auditorías de un usuario específico (paginado).
     * 
     * @param usuarioId ID del usuario
     * @param pageable Configuración de paginación
     * @return Página de auditorías del usuario
     */
    @Query("SELECT ad FROM AuditoriaDetallada ad WHERE ad.usuario.idPersona = :usuarioId ORDER BY ad.createdAt DESC")
    Page<AuditoriaDetallada> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /**
     * Obtiene auditorías de una entidad específica.
     * 
     * @param entidad Nombre de la entidad
     * @param entidadId ID del registro
     * @return Lista de auditorías de ese registro
     */
    @Query("SELECT ad FROM AuditoriaDetallada ad WHERE ad.entidad = :entidad AND ad.entidadId = :entidadId ORDER BY ad.createdAt DESC")
    List<AuditoriaDetallada> findByEntidadAndEntidadId(
        @Param("entidad") String entidad, 
        @Param("entidadId") Long entidadId
    );

    /**
     * Obtiene auditorías por tipo de acción en un rango de fechas.
     * 
     * @param tipoAccion Tipo de acción (CREAR, EDITAR, ELIMINAR, etc.)
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @param pageable Configuración de paginación
     * @return Página de auditorías
     */
    @Query("SELECT ad FROM AuditoriaDetallada ad WHERE ad.tipoAccion = :tipoAccion AND ad.createdAt BETWEEN :fechaInicio AND :fechaFin ORDER BY ad.createdAt DESC")
    Page<AuditoriaDetallada> findByTipoAccionAndFechaBetween(
        @Param("tipoAccion") String tipoAccion,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable
    );

    /**
     * Obtiene auditorías por entidad y tipo de acción.
     * 
     * @param entidad Nombre de la entidad
     * @param tipoAccion Tipo de acción
     * @param pageable Configuración de paginación
     * @return Página de auditorías
     */
    @Query("SELECT ad FROM AuditoriaDetallada ad WHERE ad.entidad = :entidad AND ad.tipoAccion = :tipoAccion ORDER BY ad.createdAt DESC")
    Page<AuditoriaDetallada> findByEntidadAndTipoAccion(
        @Param("entidad") String entidad,
        @Param("tipoAccion") String tipoAccion,
        Pageable pageable
    );

    /**
     * Cuenta auditorías por usuario en un rango de fechas.
     * 
     * @param usuarioId ID del usuario
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Número de auditorías
     */
    @Query("SELECT COUNT(ad) FROM AuditoriaDetallada ad WHERE ad.usuario.idPersona = :usuarioId AND ad.createdAt BETWEEN :fechaInicio AND :fechaFin")
    long countByUsuarioIdAndFechaBetween(
        @Param("usuarioId") Long usuarioId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );

    /**
     * Obtiene las últimas N auditorías del sistema.
     * 
     * @param pageable Configuración de paginación
     * @return Página de auditorías recientes
     */
    @Query("SELECT ad FROM AuditoriaDetallada ad ORDER BY ad.createdAt DESC")
    Page<AuditoriaDetallada> findRecentAudits(Pageable pageable);
}
