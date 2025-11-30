package com.tuorg.veterinaria.gestionpagos.model;

import com.tuorg.veterinaria.common.audit.Auditable;
import com.tuorg.veterinaria.gestionfacturacion.model.Factura;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un pago online realizado a través de ePayco.
 * 
 * Registra toda la información de la transacción incluyendo
 * referencia de ePayco, estado, método de pago y respuestas.
 * 
 * @author Equipo de Desarrollo
 * @version 1.0.0
 */
@Entity
@Table(name = "pagos_online", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoOnline extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago_online")
    private Long idPagoOnline;

    /**
     * Factura asociada al pago.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    private Factura factura;

    /**
     * Referencia única generada para la transacción en ePayco.
     * Se genera antes de enviar al cliente a la pasarela.
     */
    @Column(name = "referencia_epayco", nullable = false, unique = true, length = 100)
    private String referenciaEpayco;

    /**
     * Referencia devuelta por ePayco después del pago exitoso.
     * Solo se llena cuando el pago es confirmado.
     */
    @Column(name = "ref_payco", length = 100)
    private String refPayco;

    /**
     * Monto del pago.
     */
    @Column(name = "monto", nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    /**
     * Moneda del pago (COP, USD, etc.).
     */
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "COP";

    /**
     * Estado del pago:
     * - PENDIENTE: Transacción creada, esperando pago del cliente
     * - PROCESANDO: Cliente redirigido a ePayco, transacción en proceso
     * - APROBADA: Pago confirmado por ePayco
     * - RECHAZADA: Pago rechazado por entidad financiera
     * - FALLIDA: Error en la transacción
     * - CANCELADA: Cliente canceló el pago
     */
    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "PENDIENTE";

    /**
     * Nombre completo del pagador.
     */
    @Column(name = "nombre_pagador")
    private String nombrePagador;

    /**
     * Email del pagador.
     */
    @Column(name = "email_pagador")
    private String emailPagador;

    /**
     * Teléfono del pagador.
     */
    @Column(name = "telefono_pagador", length = 50)
    private String telefonoPagador;

    /**
     * Método de pago usado (PSE, TARJETA_CREDITO, EFECTIVO, etc.).
     */
    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    /**
     * Banco utilizado (para pagos PSE).
     */
    @Column(name = "banco", length = 100)
    private String banco;

    /**
     * Código de respuesta de ePayco.
     */
    @Column(name = "codigo_respuesta", length = 20)
    private String codigoRespuesta;

    /**
     * Mensaje de respuesta de ePayco.
     */
    @Column(name = "mensaje_respuesta", columnDefinition = "TEXT")
    private String mensajeRespuesta;

    /**
     * Código de autorización de la entidad financiera.
     */
    @Column(name = "codigo_autorizacion", length = 50)
    private String codigoAutorizacion;

    /**
     * Número de recibo generado por ePayco.
     */
    @Column(name = "recibo_pago", length = 100)
    private String reciboPago;

    /**
     * Fecha y hora de la transacción.
     */
    @Column(name = "fecha_transaccion")
    private LocalDateTime fechaTransaccion;

    /**
     * Fecha y hora de aprobación del pago.
     */
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    /**
     * Metadata adicional de ePayco en formato JSON.
     * Contiene la respuesta completa para auditoría.
     */
    @Column(name = "metadata_epayco", columnDefinition = "JSONB")
    private String metadataEpayco;

    /**
     * IP del cliente que inició el pago.
     */
    @Column(name = "ip_cliente", length = 45)
    private String ipCliente;
}
