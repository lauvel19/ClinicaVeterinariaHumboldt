package com.tuorg.veterinaria.gestionpacientes.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidad que representa un paciente (mascota) en el sistema.
 * 
 * Esta clase almacena información sobre las mascotas que son atendidas
 * en la clínica veterinaria.
 * Extiende de Auditable para trazabilidad automática.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Entity
@Table(name = "pacientes", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paciente extends Auditable {

    /**
     * Identificador único del paciente (clave primaria).
     * Se genera automáticamente mediante BIGSERIAL en PostgreSQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;

    /**
     * Nombre de la mascota.
     */
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Especie de la mascota (perro o gato).
     * Debe cumplir con el constraint CHECK en la base de datos.
     */
    @Column(name = "especie", nullable = false, length = 30)
    private String especie;

    /**
     * Raza de la mascota.
     */
    @Column(name = "raza", length = 80)
    private String raza;

    /**
     * Fecha de nacimiento de la mascota.
     */
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    /**
     * Sexo de la mascota (Macho, Hembra).
     */
    @Column(name = "sexo", length = 10)
    private String sexo;

    /**
     * Peso de la mascota en kilogramos.
     * Debe ser mayor que cero (constraint CHECK).
     */
    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    /**
     * Estado de salud actual de la mascota.
     */
    @Column(name = "estado_salud", length = 100)
    private String estadoSalud;

    /**
     * URL o path de la foto de perfil del paciente.
     * Si es null, se mostrará un avatar genérico en la UI.
     */
    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    /**
     * Cliente (dueño) de la mascota.
     * Relación Many-to-One con la entidad Cliente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Identificador externo opcional (UUID).
     * Útil para integraciones con sistemas externos.
     */
    @Column(name = "identificador_externo")
    private UUID identificadorExterno;
}

