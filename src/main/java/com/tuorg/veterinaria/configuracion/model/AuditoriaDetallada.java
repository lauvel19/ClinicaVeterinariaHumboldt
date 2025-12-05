package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa la auditoría detallada del sistema.
 * 
 * Implementa patrón Memento: almacena el estado anterior y posterior de cada operación,
 * permitiendo análisis forense y potencial reversión de cambios.
 * 
 * Registra TODAS las operaciones críticas: crear, editar, eliminar, exportar, aprobar.
 */
@Entity
@Table(name = "auditoria_detallada", schema = "public",
    indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "created_at"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario_id"),
        @Index(name = "idx_auditoria_entidad", columnList = "entidad,entidad_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaDetallada extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    /**
     * Usuario que realizó la acción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Tipo de acción realizada.
     * Valores: CREAR, EDITAR, ELIMINAR, EXPORTAR, APROBAR, LOGIN, LOGOUT, etc.
     */
    @Column(name = "tipo_accion", nullable = false, length = 50)
    private String tipoAccion;

    /**
     * Nombre de la entidad afectada.
     * Ejemplos: Usuario, Paciente, Cita, HistoriaClinica, Factura
     */
    @Column(name = "entidad", nullable = false, length = 100)
    private String entidad;

    /**
     * ID del registro afectado.
     */
    @Column(name = "entidad_id")
    private Long entidadId;

    /**
     * Estado anterior del registro en formato JSON (patrón Memento).
     * Permite reversión de cambios y auditoría forense.
     */
    @Column(name = "datos_antes", columnDefinition = "JSONB")
    private String datosAntes;

    /**
     * Estado posterior del registro en formato JSON.
     */
    @Column(name = "datos_despues", columnDefinition = "JSONB")
    private String datosDespues;

    /**
     * Dirección IP desde donde se realizó la acción.
     */
    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    /**
     * User agent del navegador.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Descripción adicional de la acción.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}
