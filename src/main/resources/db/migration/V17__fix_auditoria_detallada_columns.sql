-- V17__fix_auditoria_detallada_columns.sql
-- Renombrar y agregar columnas faltantes en auditoria_detallada para alinear con la entidad

-- Renombrar columnas existentes
ALTER TABLE auditoria_detallada 
    RENAME COLUMN datos_anteriores TO datos_antes;

ALTER TABLE auditoria_detallada 
    RENAME COLUMN datos_nuevos TO datos_despues;

ALTER TABLE auditoria_detallada 
    RENAME COLUMN ip_address TO ip_origen;

-- Agregar columnas faltantes
ALTER TABLE auditoria_detallada 
    ADD COLUMN IF NOT EXISTS tipo_accion VARCHAR(50);

ALTER TABLE auditoria_detallada 
    ADD COLUMN IF NOT EXISTS descripcion TEXT;

-- Eliminar columnas que no están en la entidad
ALTER TABLE auditoria_detallada 
    DROP COLUMN IF EXISTS historial_accion_id;

ALTER TABLE auditoria_detallada 
    DROP COLUMN IF EXISTS rol_nombre;

ALTER TABLE auditoria_detallada 
    DROP COLUMN IF EXISTS modulo;

ALTER TABLE auditoria_detallada 
    DROP COLUMN IF EXISTS relevancia;

ALTER TABLE auditoria_detallada 
    DROP COLUMN IF EXISTS requiere_revision;

ALTER TABLE auditoria_detallada 
    DROP COLUMN IF EXISTS fecha_accion;

-- Comentarios
COMMENT ON COLUMN auditoria_detallada.datos_antes IS 'Estado anterior del registro en formato JSON (patrón Memento)';
COMMENT ON COLUMN auditoria_detallada.datos_despues IS 'Estado posterior del registro en formato JSON';
COMMENT ON COLUMN auditoria_detallada.tipo_accion IS 'Tipo de acción realizada (CREAR, EDITAR, ELIMINAR, etc.)';
