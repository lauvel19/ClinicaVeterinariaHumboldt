-- =====================================================
-- Migración V10: Crear tabla pagos_online
-- =====================================================
-- Descripción: Tabla para registrar transacciones de pago
--             online realizadas a través de ePayco.
-- Autor: Sistema
-- Fecha: 2025-11-29
-- =====================================================

CREATE TABLE IF NOT EXISTS pagos_online (
    id_pago_online BIGSERIAL PRIMARY KEY,
    
    -- Relación con factura
    factura_id BIGINT NOT NULL REFERENCES facturas(id_factura) ON DELETE RESTRICT,
    
    -- Datos de la transacción ePayco
    referencia_epayco VARCHAR(100) UNIQUE NOT NULL,
    ref_payco VARCHAR(100), -- Referencia única de ePayco después del pago
    
    -- Información del pago
    monto NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    moneda VARCHAR(3) NOT NULL DEFAULT 'COP',
    
    -- Estado del pago
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE' 
        CHECK (estado IN ('PENDIENTE', 'PROCESANDO', 'APROBADA', 'RECHAZADA', 'FALLIDA', 'CANCELADA')),
    
    -- Datos del pagador
    nombre_pagador VARCHAR(255),
    email_pagador VARCHAR(255),
    telefono_pagador VARCHAR(50),
    
    -- Método de pago usado en ePayco
    metodo_pago VARCHAR(50), -- PSE, TARJETA_CREDITO, EFECTIVO, etc.
    banco VARCHAR(100), -- Si es PSE, nombre del banco
    
    -- Respuesta de ePayco
    codigo_respuesta VARCHAR(20),
    mensaje_respuesta TEXT,
    codigo_autorizacion VARCHAR(50),
    recibo_pago VARCHAR(100), -- Número de recibo de ePayco
    
    -- Datos de transacción
    fecha_transaccion TIMESTAMP,
    fecha_aprobacion TIMESTAMP,
    
    -- Metadata adicional en JSON
    metadata_epayco JSONB,
    
    -- IP del cliente que inició el pago
    ip_cliente VARCHAR(45),
    
    -- Auditoría
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_pagos_online_factura ON pagos_online(factura_id);
CREATE INDEX idx_pagos_online_referencia ON pagos_online(referencia_epayco);
CREATE INDEX idx_pagos_online_estado ON pagos_online(estado);
CREATE INDEX idx_pagos_online_fecha_transaccion ON pagos_online(fecha_transaccion);

-- Comentarios
COMMENT ON TABLE pagos_online IS 'Registro de transacciones de pago online realizadas a través de ePayco';
COMMENT ON COLUMN pagos_online.referencia_epayco IS 'Referencia única generada para la transacción en ePayco';
COMMENT ON COLUMN pagos_online.ref_payco IS 'Referencia devuelta por ePayco después del pago exitoso';
COMMENT ON COLUMN pagos_online.estado IS 'Estado actual del pago: PENDIENTE, PROCESANDO, APROBADA, RECHAZADA, FALLIDA, CANCELADA';
COMMENT ON COLUMN pagos_online.metadata_epayco IS 'Respuesta completa de ePayco en formato JSON';
