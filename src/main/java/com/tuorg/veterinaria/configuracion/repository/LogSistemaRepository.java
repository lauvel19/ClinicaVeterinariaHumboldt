package com.tuorg.veterinaria.configuracion.repository;

import com.tuorg.veterinaria.configuracion.model.LogSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogSistemaRepository extends JpaRepository<LogSistema, Long> {
    List<LogSistema> findByNivel(String nivel);

    List<LogSistema> findByComponente(String componente);

    List<LogSistema> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
