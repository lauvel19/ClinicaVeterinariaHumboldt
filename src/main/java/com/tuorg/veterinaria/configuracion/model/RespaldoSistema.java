package com.tuorg.veterinaria.configuracion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import com.tuorg.veterinaria.gestionusuarios.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa los respaldos del sistema.
 * 
 * Registra información sobre backups de base de datos y configuración,
 * incluyendo verificación de integridad mediante hash SHA-256.
 * 
 * Soporta operaciones de backup y restore con patrón Memento.
 */
@Entity
@Table(name = "respaldos_sistema", schema = "public",
    indexes = @Index(name = "idx_respaldos_fecha", columnList = "fecha_respaldo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespaldoSistema extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respaldo")
    private Long idRespaldo;

    /**
     * Usuario que generó el respaldo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Fecha y hora en que se realizó el respaldo.
     */
    @Column(name = "fecha_respaldo", nullable = false)
    private LocalDateTime fechaRespaldo;

    /**
     * Tipo de respaldo realizado.
     * Valores: COMPLETO, INCREMENTAL, DIFERENCIAL, CONFIGURACION
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_respaldo", nullable = false, length = 20)
    private TipoRespaldo tipoRespaldo;

    /**
     * Ruta o ubicación del archivo de respaldo.
     */
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    /**
     * Tamaño del archivo en bytes.
     */
    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    /**
     * Hash SHA-256 para verificación de integridad.
     */
    @Column(name = "hash_verificacion", length = 64)
    private String hashVerificacion;

    /**
     * Estado del respaldo.
     * Valores: COMPLETADO, EN_PROCESO, FALLIDO, CORRUPTO, RESTAURADO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRespaldo estado;

    /**
     * Descripción o notas sobre el respaldo.
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Información de error si el respaldo falló.
     */
    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    /**
     * Enum para tipos de respaldo.
     */
    public enum TipoRespaldo {
        COMPLETO, INCREMENTAL, DIFERENCIAL, CONFIGURACION
    }

    /**
     * Enum para estados de respaldo.
     */
    public enum EstadoRespaldo {
        COMPLETADO, EN_PROCESO, FALLIDO, CORRUPTO, RESTAURADO
    }
}
