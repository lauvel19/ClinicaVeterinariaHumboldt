-- =====================================================
-- V27: Alinear TODAS las tablas del módulo de configuración con sus entidades JPA
-- - parametros_sistema
-- - configuracion_avanzada
-- - auditoria_detallada
-- - respaldos_sistema
-- =====================================================

-- =====================================================
-- 1. PARAMETROS_SISTEMA
-- Agregar columnas faltantes y audit columns
-- =====================================================
ALTER TABLE public.parametros_sistema
    ADD COLUMN IF NOT EXISTS tipo_dato VARCHAR(50),
    ADD COLUMN IF NOT EXISTS categoria VARCHAR(50),
    ADD COLUMN IF NOT EXISTS editable BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Backfill defaults for parametros_sistema
UPDATE public.parametros_sistema
SET 
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 'SYSTEM'),
    activo = COALESCE(activo, TRUE),
    editable = COALESCE(editable, TRUE)
WHERE created_at IS NULL OR created_by IS NULL;

-- =====================================================
-- 2. CONFIGURACION_AVANZADA
-- Renombrar id si es necesario y agregar audit columns
-- =====================================================
DO $$
BEGIN
    -- Renombrar columna id -> id_configuracion si aplica
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'configuracion_avanzada' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'configuracion_avanzada' AND column_name = 'id_configuracion'
    ) THEN
        EXECUTE 'ALTER TABLE public.configuracion_avanzada RENAME COLUMN id TO id_configuracion';
    END IF;
END$$;

ALTER TABLE public.configuracion_avanzada
    ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Backfill for configuracion_avanzada
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'configuracion_avanzada' AND column_name = 'creado_por'
    ) THEN
        EXECUTE 'UPDATE public.configuracion_avanzada SET created_by = creado_por WHERE created_by IS NULL';
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'configuracion_avanzada' AND column_name = 'fecha_creacion'
    ) THEN
        EXECUTE 'UPDATE public.configuracion_avanzada SET created_at = fecha_creacion WHERE created_at IS NULL';
    END IF;
END$$;

UPDATE public.configuracion_avanzada
SET 
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 'SYSTEM'),
    activo = COALESCE(activo, TRUE)
WHERE created_at IS NULL OR created_by IS NULL;

-- =====================================================
-- 3. AUDITORIA_DETALLADA
-- Renombrar id si es necesario y agregar audit columns
-- =====================================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'auditoria_detallada' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'auditoria_detallada' AND column_name = 'id_auditoria'
    ) THEN
        EXECUTE 'ALTER TABLE public.auditoria_detallada RENAME COLUMN id TO id_auditoria';
    END IF;
END$$;

ALTER TABLE public.auditoria_detallada
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Backfill for auditoria_detallada
UPDATE public.auditoria_detallada
SET 
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 'SYSTEM')
WHERE created_at IS NULL OR created_by IS NULL;

-- =====================================================
-- 4. RESPALDOS_SISTEMA
-- Renombrar id si es necesario y agregar audit columns
-- =====================================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'respaldos_sistema' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'respaldos_sistema' AND column_name = 'id_respaldo'
    ) THEN
        EXECUTE 'ALTER TABLE public.respaldos_sistema RENAME COLUMN id TO id_respaldo';
    END IF;
END$$;

ALTER TABLE public.respaldos_sistema
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Backfill for respaldos_sistema
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'respaldos_sistema' AND column_name = 'creado_por'
    ) THEN
        EXECUTE 'UPDATE public.respaldos_sistema SET created_by = creado_por WHERE created_by IS NULL';
    END IF;
END$$;

UPDATE public.respaldos_sistema
SET 
    created_at = COALESCE(created_at, fecha_respaldo, CURRENT_TIMESTAMP),
    created_by = COALESCE(created_by, 'SYSTEM')
WHERE created_at IS NULL OR created_by IS NULL;

-- =====================================================
-- COMENTARIOS
-- =====================================================
COMMENT ON COLUMN public.parametros_sistema.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN public.parametros_sistema.created_at IS 'Fecha de creación del registro';
COMMENT ON COLUMN public.parametros_sistema.updated_by IS 'Usuario que modificó el registro';
COMMENT ON COLUMN public.parametros_sistema.updated_at IS 'Fecha de última modificación del registro';

COMMENT ON COLUMN public.configuracion_avanzada.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN public.configuracion_avanzada.created_at IS 'Fecha de creación del registro';

COMMENT ON COLUMN public.auditoria_detallada.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN public.auditoria_detallada.created_at IS 'Fecha de creación del registro';

COMMENT ON COLUMN public.respaldos_sistema.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN public.respaldos_sistema.created_at IS 'Fecha de creación del registro';
