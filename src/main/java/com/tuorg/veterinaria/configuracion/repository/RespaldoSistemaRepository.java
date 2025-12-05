package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.RespaldoSistema;
import com.tuorg.veterinaria.configuracion.model.RespaldoSistema.EstadoRespaldo;
import com.tuorg.veterinaria.configuracion.model.RespaldoSistema.TipoRespaldo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para RespaldoSistema (backups).
 */
@Repository
public interface RespaldoSistemaRepository extends JpaRepository<RespaldoSistema, Long> {

    /**
     * Obtiene todos los respaldos ordenados por fecha descendente.
     * 
     * @param pageable Configuración de paginación
     * @return Página de respaldos
     */
    @Query("SELECT rs FROM RespaldoSistema rs ORDER BY rs.fechaRespaldo DESC")
    Page<RespaldoSistema> findAllOrderByFechaDesc(Pageable pageable);

    /**
     * Obtiene respaldos por tipo.
     * 
     * @param tipoRespaldo Tipo de respaldo
     * @return Lista de respaldos de ese tipo
     */
    List<RespaldoSistema> findByTipoRespaldoOrderByFechaRespaldoDesc(TipoRespaldo tipoRespaldo);

    /**
     * Obtiene respaldos por estado.
     * 
     * @param estado Estado del respaldo
     * @return Lista de respaldos con ese estado
     */
    List<RespaldoSistema> findByEstadoOrderByFechaRespaldoDesc(EstadoRespaldo estado);

    /**
     * Obtiene el último respaldo completado exitosamente.
     * 
     * @return Último respaldo exitoso
     */
    @Query("SELECT rs FROM RespaldoSistema rs WHERE rs.estado = 'COMPLETADO' ORDER BY rs.fechaRespaldo DESC LIMIT 1")
    RespaldoSistema findUltimoRespaldoExitoso();

    /**
     * Obtiene respaldos en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @param pageable Configuración de paginación
     * @return Página de respaldos en ese rango
     */
    @Query("SELECT rs FROM RespaldoSistema rs WHERE rs.fechaRespaldo BETWEEN :fechaInicio AND :fechaFin ORDER BY rs.fechaRespaldo DESC")
    Page<RespaldoSistema> findByFechaRespaldoBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable
    );

    /**
     * Cuenta respaldos por estado.
     * 
     * @param estado Estado del respaldo
     * @return Número de respaldos con ese estado
     */
    long countByEstado(EstadoRespaldo estado);
}
