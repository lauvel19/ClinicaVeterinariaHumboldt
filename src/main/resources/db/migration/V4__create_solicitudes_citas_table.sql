-- V4__create_solicitudes_citas_table.sql

CREATE TABLE IF NOT EXISTS solicitudes_citas (
    id_solicitud BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    paciente_id BIGINT NOT NULL,
    fecha_solicitada DATE NOT NULL,
    hora_solicitada TIME NOT NULL,
    tipo_servicio VARCHAR(100),
    motivo TEXT,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'CANCELADA')),
    motivo_rechazo TEXT,
    cita_id BIGINT,
    observaciones TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_solicitud_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_solicitud_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id_paciente) ON DELETE CASCADE,
    CONSTRAINT fk_solicitud_cita FOREIGN KEY (cita_id) REFERENCES citas(id_cita) ON DELETE SET NULL
);

CREATE INDEX idx_solicitudes_cliente ON solicitudes_citas(cliente_id);
CREATE INDEX idx_solicitudes_paciente ON solicitudes_citas(paciente_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes_citas(estado);
CREATE INDEX idx_solicitudes_fecha ON solicitudes_citas(fecha_solicitada);

-- Comentario para descripción
COMMENT ON TABLE solicitudes_citas IS 'Tabla para almacenar solicitudes de cita del portal del cliente que requieren aprobación del secretario';
COMMENT ON COLUMN solicitudes_citas.estado IS 'Estado de la solicitud: PENDIENTE (en espera de aprobación), APROBADA (convertida en cita), RECHAZADA (rechazada por el secretario), CANCELADA (cancelada por el cliente)';
