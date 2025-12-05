package com.tuorg.veterinaria.gestionfacturacion.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import com.tuorg.veterinaria.common.constants.AppConstants;
import com.tuorg.veterinaria.gestionusuarios.model.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una factura.
 * 
 * Esta clase almacena información sobre las facturas generadas
 * por los servicios prestados.
 * 
 * Implementa el patrón Factory/Builder para su creación.
 * Extiende de Auditable para trazabilidad automática.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Entity
@Table(name = "facturas", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Factura extends Auditable {

    /**
     * Identificador único de la factura (clave primaria).
     * Se genera automáticamente mediante BIGSERIAL en PostgreSQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura")
    private Long idFactura;

    /**
     * Número único de la factura.
     * Debe ser único en toda la tabla (constraint UNIQUE).
     */
    @Column(name = "numero", nullable = false, unique = true, length = 50)
    private String numero;

    /**
     * Fecha y hora de emisión de la factura.
     * Incluye información de zona horaria.
     */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    /**
     * Total de la factura.
     * Debe ser igual a la suma de los detalles ± impuestos.
     * Debe ser mayor o igual a cero (constraint CHECK).
     */
    @Column(name = "total", nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    /**
     * Forma de pago utilizada.
     */
    @Column(name = "forma_pago", length = 50)
    private String formaPago;

    /**
     * Estado de la factura: PENDIENTE, PAGADA, ANULADA.
     * Debe cumplir con el constraint CHECK en la base de datos.
     */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = AppConstants.ESTADO_FACTURA_PENDIENTE;

    /**
     * Fecha y hora en que se registró el pago de la factura.
     * Solo aplicable cuando el estado es PAGADA.
     */
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    /**
     * Cliente al que se emite la factura.
     * Relación Many-to-One con la entidad Cliente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Contenido de la factura en formato JSON.
     * Incluye detalles de servicios, impuestos, descuentos, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contenido", columnDefinition = "JSONB")
    private String contenido;
}

