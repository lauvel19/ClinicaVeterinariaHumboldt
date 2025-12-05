package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa parámetros de configuración del sistema.
 * 
 * Permite almacenar configuraciones clave-valor que pueden ser
 * modificadas dinámicamente sin necesidad de reiniciar la aplicación.
 */
@Entity
@Table(name = "parametros_sistema", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParametroSistema extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Long idParametro;

    /**
     * Clave única del parámetro.
     * Ejemplos: sistema.nombre, sistema.version, email.smtp.host
     */
    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    /**
     * Valor del parámetro.
     */
    @Column(name = "valor", columnDefinition = "TEXT")
    private String valor;

    /**
     * Descripción del parámetro.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Tipo de dato del parámetro.
     * Ejemplos: STRING, INTEGER, BOOLEAN, DECIMAL, DATE
     */
    @Column(name = "tipo_dato", length = 50)
    private String tipoDato;

    /**
     * Categoría del parámetro para agrupar configuraciones relacionadas.
     * Ejemplos: SISTEMA, EMAIL, SEGURIDAD, BACKUP
     */
    @Column(name = "categoria", length = 50)
    private String categoria;

    /**
     * Indica si el parámetro es editable por el usuario.
     */
    @Column(name = "editable", nullable = false)
    private Boolean editable = true;

    /**
     * Indica si el parámetro está activo.
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
