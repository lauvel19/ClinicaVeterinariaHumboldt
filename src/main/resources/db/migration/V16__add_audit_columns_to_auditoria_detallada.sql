-- V16__add_audit_columns_to_auditoria_detallada.sql
-- Agregar columnas de auditoría heredadas de Auditable

ALTER TABLE auditoria_detallada 
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- Comentarios
COMMENT ON COLUMN auditoria_detallada.created_at IS 'Fecha de creación del registro de auditoría';
COMMENT ON COLUMN auditoria_detallada.updated_at IS 'Fecha de última actualización del registro';
COMMENT ON COLUMN auditoria_detallada.created_by IS 'Usuario que creó el registro de auditoría';
COMMENT ON COLUMN auditoria_detallada.updated_by IS 'Usuario que actualizó el registro';
