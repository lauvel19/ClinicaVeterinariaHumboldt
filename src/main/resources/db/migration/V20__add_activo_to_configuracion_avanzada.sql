-- Agregar columna activo a la tabla configuracion_avanzada
ALTER TABLE configuracion_avanzada ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
