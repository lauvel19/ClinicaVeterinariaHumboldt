package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad LogSistema.
 * 
 * Proporciona operaciones de base de datos para gestionar
 * los logs del sistema.
 */
@Repository
public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {

    /**
     * Busca logs por nivel.
     * 
     * @param nivel Nivel del log (INFO, WARN, ERROR, DEBUG)
     * @return Lista de logs con el nivel especificado
     */
    List<LogSistema> findByNivel(String nivel);

    /**
     * Busca logs por componente.
     * 
     * @param componente Componente que generó el log
     * @return Lista de logs del componente especificado
     */
    List<LogSistema> findByComponente(String componente);

    /**
     * Busca logs entre dos fechas.
     * 
     * @param inicio Fecha y hora de inicio
     * @param fin Fecha y hora de fin
     * @return Lista de logs en el rango especificado
     */
    @Query("SELECT l FROM LogSistema l WHERE l.fechaHora BETWEEN :inicio AND :fin ORDER BY l.fechaHora DESC")
    List<LogSistema> findByFechaHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Busca logs por nivel y componente.
     * 
     * @param nivel Nivel del log
     * @param componente Componente que generó el log
     * @return Lista de logs que cumplen los criterios
     */
    List<LogSistema> findByNivelAndComponente(String nivel, String componente);

    /**
     * Busca logs por usuario.
     * 
     * @param usuario Usuario relacionado con el log
     * @return Lista de logs del usuario especificado
     */
    List<LogSistema> findByUsuario(String usuario);

    /**
     * Busca los últimos N logs.
     * 
     * @param limit Número máximo de logs a retornar
     * @return Lista de los últimos logs
     */
    @Query("SELECT l FROM LogSistema l ORDER BY l.fechaHora DESC")
    List<LogSistema> findTopByOrderByFechaHoraDesc();

    /**
     * Elimina logs anteriores a una fecha específica.
     * 
     * @param fecha Fecha límite
     */
    @Query("DELETE FROM LogSistema l WHERE l.fechaHora < :fecha")
    void deleteByFechaHoraBefore(@Param("fecha") LocalDateTime fecha);

    /**
     * Cuenta logs por nivel en un rango de fechas.
     * 
     * @param nivel Nivel del log
     * @param inicio Fecha y hora de inicio
     * @param fin Fecha y hora de fin
     * @return Cantidad de logs
     */
    @Query("SELECT COUNT(l) FROM LogSistema l WHERE l.nivel = :nivel AND l.fechaHora BETWEEN :inicio AND :fin")
    Long countByNivelAndFechaHoraBetween(@Param("nivel") String nivel, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
