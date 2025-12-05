package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa parámetros de configuración avanzada del sistema.
 * 
 * Almacena configuraciones técnicas y de negocio en formato clave-valor,
 * permitiendo personalización sin necesidad de redeploy.
 * 
 * Ejemplos: duracion_cita_minutos, recordatorio_dias_previos, max_intentos_login
 */
@Entity
@Table(name = "configuracion_avanzada", schema = "public",
    uniqueConstraints = @UniqueConstraint(columnNames = {"clave"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionAvanzada extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Long idConfiguracion;

    /**
     * Clave única del parámetro de configuración.
     * Usar formato snake_case. Ejemplos: duracion_cita_minutos, smtp_host, logo_max_size_kb
     */
    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    /**
     * Valor del parámetro.
     * Puede ser número, texto, JSON, etc.
     */
    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor;

    /**
     * Tipo de dato del valor.
     * Valores: STRING, INTEGER, DECIMAL, BOOLEAN, JSON, DATE, TIME
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dato", nullable = false, length = 20)
    private TipoDato tipoDato;

    /**
     * Categoría de la configuración.
     * Ejemplos: SISTEMA, NOTIFICACIONES, SEGURIDAD, INTERFAZ, NEGOCIO
     */
    @Column(name = "categoria", length = 50)
    private String categoria;

    /**
     * Descripción del parámetro (para la UI).
     */
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    /**
     * Indica si el parámetro es editable desde la interfaz.
     */
    @Column(name = "editable", nullable = false)
    private Boolean editable = true;

    /**
     * Indica si el parámetro está activo.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Enum para tipos de dato.
     */
    public enum TipoDato {
        STRING, INTEGER, DECIMAL, BOOLEAN, JSON, DATE, TIME
    }
}
