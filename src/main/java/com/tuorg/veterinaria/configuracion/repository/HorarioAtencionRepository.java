package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.HorarioAtencion;
import com.tuorg.veterinaria.configuracion.model.HorarioAtencion.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para HorarioAtencion (horarios de la clínica).
 */
@Repository
public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {

    /**
     * Obtiene el horario de un día específico.
     * 
     * @param diaSemana Día de la semana
     * @return Horario del día
     */
    Optional<HorarioAtencion> findByDiaSemana(DiaSemana diaSemana);

    /**
     * Obtiene todos los horarios ordenados por día de la semana.
     * 
     * @return Lista de horarios
     */
    @Query("SELECT ha FROM HorarioAtencion ha ORDER BY ha.diaSemana")
    List<HorarioAtencion> findAllOrdenadoPorDia();

    /**
     * Obtiene los días en que la clínica está abierta.
     * 
     * @return Lista de horarios de días abiertos
     */
    @Query("SELECT ha FROM HorarioAtencion ha WHERE ha.cerrado = false ORDER BY ha.diaSemana")
    List<HorarioAtencion> findDiasAbiertos();

    /**
     * Verifica si la clínica está abierta en un día específico.
     * 
     * @param diaSemana Día de la semana
     * @return true si está abierta
     */
    @Query("SELECT COUNT(ha) > 0 FROM HorarioAtencion ha WHERE ha.diaSemana = :diaSemana AND ha.cerrado = false")
    boolean isClinicaAbierta(@Param("diaSemana") DiaSemana diaSemana);
}
