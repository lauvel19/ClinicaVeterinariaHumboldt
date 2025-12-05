package com.tuorg.veterinaria.prestacioneservicios.repository;

import com.tuorg.veterinaria.prestacioneservicios.model.Cita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Cita.
 * 
 * Proporciona métodos de acceso a datos para citas
 * utilizando Spring Data JPA con soporte de paginación.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca citas por paciente.
     * 
     * @param pacienteId ID del paciente
     * @return Lista de citas del paciente
     */
    @Query("SELECT c FROM Cita c WHERE c.paciente.idPaciente = :pacienteId")
    List<Cita> findByPacienteId(@Param("pacienteId") Long pacienteId);

    /**
     * Busca citas por paciente con paginación.
     * 
     * @param pacienteId ID del paciente
     * @param pageable Información de paginación
     * @return Página de citas del paciente
     */
    @Query("SELECT c FROM Cita c WHERE c.paciente.idPaciente = :pacienteId")
    Page<Cita> findByPacienteId(@Param("pacienteId") Long pacienteId, Pageable pageable);

    /**
     * Busca citas por veterinario.
     * 
     * @param veterinarioId ID del veterinario
     * @return Lista de citas del veterinario
     */
    @Query("SELECT c FROM Cita c WHERE c.veterinario.idPersona = :veterinarioId")
    List<Cita> findByVeterinarioId(@Param("veterinarioId") Long veterinarioId);

    /**
     * Busca citas por veterinario con paginación.
     * 
     * @param veterinarioId ID del veterinario
     * @param pageable Información de paginación
     * @return Página de citas del veterinario
     */
    @Query("SELECT c FROM Cita c WHERE c.veterinario.idPersona = :veterinarioId")
    Page<Cita> findByVeterinarioId(@Param("veterinarioId") Long veterinarioId, Pageable pageable);

    /**
     * Busca citas por estado.
     * 
     * @param estado Estado de la cita
     * @return Lista de citas con el estado especificado
     */
    List<Cita> findByEstado(String estado);

    /**
     * Verifica si existe una cita programada para un veterinario en una fecha/hora específica.
     * 
     * @param veterinarioId ID del veterinario
     * @param fechaHora Fecha y hora de la cita
     * @param estado Estado de la cita (solo PROGRAMADA)
     * @return true si existe conflicto, false en caso contrario
     */
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.veterinario.idPersona = :veterinarioId " +
           "AND c.fechaHora = :fechaHora AND c.estado = :estado")
    boolean existeCitaEnFechaHora(@Param("veterinarioId") Long veterinarioId,
                                   @Param("fechaHora") LocalDateTime fechaHora,
                                   @Param("estado") String estado);

    /**
     * Verifica si existe una cita programada para un veterinario en un rango de tiempo.
     * Considera un margen de tiempo antes y después de la fecha/hora solicitada.
     * 
     * @param veterinarioId ID del veterinario
     * @param fechaHoraInicio Inicio del rango de tiempo
     * @param fechaHoraFin Fin del rango de tiempo
     * @param estado Estado de la cita (solo PROGRAMADA)
     * @return true si existe conflicto, false en caso contrario
     */
    @Query("SELECT COUNT(c) > 0 FROM Cita c WHERE c.veterinario.idPersona = :veterinarioId " +
           "AND c.estado = :estado " +
           "AND c.fechaHora >= :fechaHoraInicio AND c.fechaHora < :fechaHoraFin")
    boolean existeCitaEnRango(@Param("veterinarioId") Long veterinarioId,
                              @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio,
                              @Param("fechaHoraFin") LocalDateTime fechaHoraFin,
                              @Param("estado") String estado);

    /**
     * Verifica si un cliente tiene citas en un estado específico.
     * 
     * @param clienteId ID del cliente
     * @param estado Estado de la cita
     * @return true si existe al menos una cita en ese estado
     */
    @Query("SELECT COUNT(c) > 0 FROM Cita c " +
           "JOIN c.paciente p " +
           "JOIN p.cliente cl " +
           "WHERE cl.id = :clienteId AND c.estado = :estado")
    boolean existsByPaciente_Cliente_IdUsuarioAndEstado(
            @Param("clienteId") Long clienteId,
            @Param("estado") String estado);

    /**
     * Busca una cita por ID cargando todas las relaciones necesarias para notificaciones.
     * Realiza JOIN FETCH para evitar LazyInitializationException.
     * 
     * @param id ID de la cita
     * @return Cita con paciente, cliente y veterinario cargados
     */
    @Query("SELECT c FROM Cita c " +
           "LEFT JOIN FETCH c.paciente p " +
           "LEFT JOIN FETCH p.cliente cl " +
           "LEFT JOIN FETCH c.veterinario v " +
           "WHERE c.idCita = :id")
    java.util.Optional<Cita> findByIdWithDetails(@Param("id") Long id);

    /**
     * Busca citas de un veterinario en un rango de fechas.
     * 
     * @param veterinario Veterinario
     * @param inicio Fecha y hora de inicio
     * @param fin Fecha y hora de fin
     * @return Lista de citas en el rango
     */
    @Query("SELECT c FROM Cita c LEFT JOIN FETCH c.paciente p " +
           "WHERE c.veterinario = :veterinario " +
           "AND c.fechaHora BETWEEN :inicio AND :fin " +
           "ORDER BY c.fechaHora ASC")
    List<Cita> findByVeterinarioAndFechaHoraBetween(
            @Param("veterinario") com.tuorg.veterinaria.gestionusuarios.model.UsuarioVeterinario veterinario,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}

