-- Renombrar columnas de auditoría en tabla configuracion_avanzada
-- para alinear con la clase Auditable que usa nombres en inglés

ALTER TABLE configuracion_avanzada RENAME COLUMN creado_por TO created_by;
ALTER TABLE configuracion_avanzada RENAME COLUMN fecha_creacion TO created_at;
ALTER TABLE configuracion_avanzada RENAME COLUMN modificado_por TO updated_by;
ALTER TABLE configuracion_avanzada RENAME COLUMN fecha_modificacion TO updated_at;
