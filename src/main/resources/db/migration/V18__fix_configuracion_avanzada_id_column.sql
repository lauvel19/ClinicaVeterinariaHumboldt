-- Renombrar columna id a id_configuracion en tabla configuracion_avanzada
-- para alinear con la entidad ConfiguracionAvanzada.java

ALTER TABLE configuracion_avanzada RENAME COLUMN id TO id_configuracion;
