package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Entidad que representa los horarios de atención de la clínica.
 * 
 * Define los horarios de operación para cada día de la semana,
 * permitiendo configurar horarios especiales o cierres temporales.
 */
@Entity
@Table(name = "horarios_atencion", schema = "public",
    uniqueConstraints = @UniqueConstraint(columnNames = {"dia_semana"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioAtencion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long idHorario;

    /**
     * Día de la semana.
     * Valores: LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    private DiaSemana diaSemana;

    /**
     * Hora de apertura.
     */
    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    /**
     * Hora de cierre.
     */
    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    /**
     * Indica si la clínica está cerrada ese día.
     */
    @Column(name = "cerrado", nullable = false)
    private Boolean cerrado = false;

    /**
     * Notas adicionales sobre el horario.
     * Ejemplo: "Horario reducido por festivo"
     */
    @Column(name = "notas", length = 200)
    private String notas;

    /**
     * Enum para los días de la semana.
     */
    public enum DiaSemana {
        LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
    }
}
