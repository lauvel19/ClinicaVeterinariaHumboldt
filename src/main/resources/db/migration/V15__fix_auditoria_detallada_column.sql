-- V15__fix_auditoria_detallada_column.sql
-- Renombrar la columna id a id_auditoria en auditoria_detallada

ALTER TABLE auditoria_detallada 
    RENAME COLUMN id TO id_auditoria;

-- Comentario
COMMENT ON COLUMN auditoria_detallada.id_auditoria IS 'Identificador único de la auditoría detallada';
