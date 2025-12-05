package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entidad que representa el catálogo de servicios veterinarios ofrecidos.
 * 
 * Define los servicios disponibles para cobro, con sus precios y descripciones.
 * Estos servicios se utilizan en consultas, facturas y reportes.
 */
@Entity
@Table(name = "servicios_configuracion", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioConfiguracion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio_config")
    private Long idServicioConfig;

    /**
     * Referencia opcional al servicio ya prestado (si aplica).
     * Permite vincular configuración con servicios del módulo de consultas.
     */
    @Column(name = "servicio_id")
    private Long servicioId;

    /**
     * Nombre del servicio ofrecido.
     * Ejemplos: Consulta General, Vacunación, Cirugía, Baño, Desparasitación
     */
    @Column(name = "nombre_servicio", nullable = false, length = 100)
    private String nombreServicio;

    /**
     * Descripción detallada del servicio.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Precio estándar del servicio en la moneda local.
     */
    @Column(name = "precio", precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Duración aproximada en minutos.
     */
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    /**
     * Categoría del servicio.
     * Ejemplos: Consulta, Cirugía, Diagnóstico, Estética, Prevención
     */
    @Column(name = "categoria", length = 50)
    private String categoria;

    /**
     * Indica si el servicio está disponible actualmente.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
