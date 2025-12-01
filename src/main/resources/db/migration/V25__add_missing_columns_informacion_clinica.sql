-- =====================================================
-- V25: Agregar columnas faltantes a informacion_clinica
-- - Agregar sitio_web, mision, vision, horario_atencion, redes_sociales
-- =====================================================

-- Agregar columnas faltantes que espera la entidad JPA
ALTER TABLE public.informacion_clinica
    ADD COLUMN IF NOT EXISTS sitio_web VARCHAR(200),
    ADD COLUMN IF NOT EXISTS mision TEXT,
    ADD COLUMN IF NOT EXISTS vision TEXT,
    ADD COLUMN IF NOT EXISTS horario_atencion TEXT,
    ADD COLUMN IF NOT EXISTS redes_sociales JSONB;

-- Comentarios de columnas para documentación
COMMENT ON COLUMN public.informacion_clinica.sitio_web IS 'Sitio web de la clínica';
COMMENT ON COLUMN public.informacion_clinica.mision IS 'Misión de la clínica';
COMMENT ON COLUMN public.informacion_clinica.vision IS 'Visión de la clínica';
COMMENT ON COLUMN public.informacion_clinica.horario_atencion IS 'Horario general de atención (texto descriptivo)';
COMMENT ON COLUMN public.informacion_clinica.redes_sociales IS 'Redes sociales en formato JSONB';
