-- =====================================================
-- V24: Alinear la tabla informacion_clinica con la entidad JPA
-- - Renombrar columna primaria a id_clinica (si aplica)
-- - Agregar todas las columnas faltantes esperadas por la entidad
-- - Agregar columnas de auditoría estándar (created_by, created_at, updated_by, updated_at)
-- - Migrar datos desde columnas legadas
-- - Mantener restricciones e índices existentes
-- =====================================================

DO $$
BEGIN
    -- Renombrar columna id -> id_clinica sólo si existe 'id' y no existe 'id_clinica'
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'informacion_clinica' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'informacion_clinica' AND column_name = 'id_clinica'
    ) THEN
        EXECUTE 'ALTER TABLE public.informacion_clinica RENAME COLUMN id TO id_clinica';
    END IF;
END$$;

-- Agregar columnas faltantes que espera la entidad JPA
ALTER TABLE public.informacion_clinica
    ADD COLUMN IF NOT EXISTS sitio_web VARCHAR(200),
    ADD COLUMN IF NOT EXISTS mision TEXT,
    ADD COLUMN IF NOT EXISTS vision TEXT,
    ADD COLUMN IF NOT EXISTS horario_atencion TEXT,
    ADD COLUMN IF NOT EXISTS redes_sociales JSONB;

-- Agregar columnas de auditoría estándar si no existen
ALTER TABLE public.informacion_clinica
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Migrar datos desde columnas legadas si existen
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'informacion_clinica' AND column_name = 'creado_por'
    ) THEN
        EXECUTE 'UPDATE public.informacion_clinica SET created_by = creado_por WHERE created_by IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'informacion_clinica' AND column_name = 'fecha_creacion'
    ) THEN
        EXECUTE 'UPDATE public.informacion_clinica SET created_at = fecha_creacion WHERE created_at IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'informacion_clinica' AND column_name = 'modificado_por'
    ) THEN
        EXECUTE 'UPDATE public.informacion_clinica SET updated_by = modificado_por WHERE updated_by IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'informacion_clinica' AND column_name = 'fecha_modificacion'
    ) THEN
        EXECUTE 'UPDATE public.informacion_clinica SET updated_at = fecha_modificacion WHERE updated_at IS NULL';
    END IF;
END$$;

-- Establecer defaults razonables si continúan nulos
UPDATE public.informacion_clinica
SET created_at = COALESCE(created_at, CURRENT_TIMESTAMP)
WHERE created_at IS NULL;

UPDATE public.informacion_clinica
SET created_by = COALESCE(created_by, 'SYSTEM')
WHERE created_by IS NULL;

-- Comentarios de columnas para documentación
COMMENT ON COLUMN public.informacion_clinica.id_clinica IS 'Identificador principal de la clínica';
COMMENT ON COLUMN public.informacion_clinica.horario_atencion IS 'Horario general de atención (texto descriptivo)';
COMMENT ON COLUMN public.informacion_clinica.redes_sociales IS 'Redes sociales en formato JSONB';
COMMENT ON COLUMN public.informacion_clinica.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN public.informacion_clinica.created_at IS 'Fecha de creación del registro';
COMMENT ON COLUMN public.informacion_clinica.updated_by IS 'Usuario que modificó el registro';
COMMENT ON COLUMN public.informacion_clinica.updated_at IS 'Fecha de última modificación del registro';
