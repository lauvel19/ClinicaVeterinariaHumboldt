package com.tuorg.veterinaria.configuracion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa logs del sistema para auditoría y seguimiento.
 * 
 * Registra eventos importantes del sistema como errores, advertencias,
 * información y eventos de depuración.
 */
@Entity
@Table(name = "logs_sistema", schema = "public", indexes = {
    @Index(name = "idx_log_fecha", columnList = "fecha_hora"),
    @Index(name = "idx_log_nivel", columnList = "nivel"),
    @Index(name = "idx_log_componente", columnList = "componente")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    /**
     * Fecha y hora en que se generó el log.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Componente o módulo que generó el log.
     * Ejemplos: AUTENTICACION, CITAS, INVENTARIO, FACTURACION
     */
    @Column(name = "componente", nullable = false, length = 100)
    private String componente;

    /**
     * Nivel del log.
     * Valores: INFO, WARN, ERROR, DEBUG
     */
    @Column(name = "nivel", nullable = false, length = 20)
    private String nivel;

    /**
     * Mensaje descriptivo del evento.
     */
    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    /**
     * Usuario relacionado con el evento (opcional).
     */
    @Column(name = "usuario", length = 100)
    private String usuario;

    /**
     * Dirección IP desde donde se generó el evento (opcional).
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Información adicional en formato JSON (opcional).
     */
    @Column(name = "detalles", columnDefinition = "TEXT")
    private String detalles;
}
