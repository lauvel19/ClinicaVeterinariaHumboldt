package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa la información general de la clínica veterinaria.
 * 
 * Implementa patrón Singleton a nivel de base de datos: solo puede existir un registro.
 * Esta tabla almacena datos como nombre, dirección, teléfono, email, etc.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Entity
@Table(name = "informacion_clinica", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InformacionClinica extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clinica")
    private Long idClinica;

    /**
     * Nombre oficial de la clínica veterinaria.
     */
    @Column(name = "nombre_clinica", nullable = false, length = 200)
    private String nombreClinica;

    /**
     * Dirección física de la clínica.
     */
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    /**
     * Teléfono principal de contacto.
     */
    @Column(name = "telefono", length = 20)
    private String telefono;

    /**
     * Email de contacto general.
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Sitio web de la clínica.
     */
    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    /**
     * Logo de la clínica (URL o ruta).
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * Misión de la clínica.
     */
    @Column(name = "mision", columnDefinition = "TEXT")
    private String mision;

    /**
     * Visión de la clínica.
     */
    @Column(name = "vision", columnDefinition = "TEXT")
    private String vision;

    /**
     * Horario general de atención (texto descriptivo).
     */
    @Column(name = "horario_atencion", columnDefinition = "TEXT")
    private String horarioAtencion;

    /**
     * Redes sociales en formato JSON.
     * Ejemplo: {"facebook": "url", "instagram": "url"}
     */
    @Column(name = "redes_sociales", columnDefinition = "JSONB")
    private String redesSociales;

    /**
     * Indica si el registro está activo.
     * Solo debe haber un registro activo (Singleton).
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
