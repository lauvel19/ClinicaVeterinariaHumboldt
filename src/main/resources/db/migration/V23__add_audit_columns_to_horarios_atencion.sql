-- Agregar columnas de auditoría requeridas por Auditable en horarios_atencion

-- created_by
ALTER TABLE public.horarios_atencion
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

-- created_at
ALTER TABLE public.horarios_atencion
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

-- updated_by
ALTER TABLE public.horarios_atencion
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

-- updated_at
ALTER TABLE public.horarios_atencion
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Backfill desde columnas antiguas si existen
UPDATE public.horarios_atencion h SET
    created_by = COALESCE(h.created_by, h.creado_por, 'SYSTEM'),
    created_at = COALESCE(h.created_at, h.fecha_creacion, CURRENT_TIMESTAMP),
    updated_by = COALESCE(h.updated_by, h.modificado_por),
    updated_at = COALESCE(h.updated_at, h.fecha_modificacion)
WHERE TRUE;

-- Opcional: eliminar columnas antiguas si ya no se usan
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='horarios_atencion' AND column_name='creado_por') THEN
        ALTER TABLE public.horarios_atencion DROP COLUMN creado_por;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='horarios_atencion' AND column_name='fecha_creacion') THEN
        ALTER TABLE public.horarios_atencion DROP COLUMN fecha_creacion;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='horarios_atencion' AND column_name='modificado_por') THEN
        ALTER TABLE public.horarios_atencion DROP COLUMN modificado_por;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='horarios_atencion' AND column_name='fecha_modificacion') THEN
        ALTER TABLE public.horarios_atencion DROP COLUMN fecha_modificacion;
    END IF;
END $$;

COMMENT ON COLUMN public.horarios_atencion.created_by IS 'Usuario que creó el registro';
COMMENT ON COLUMN public.horarios_atencion.created_at IS 'Fecha/hora de creación';
COMMENT ON COLUMN public.horarios_atencion.updated_by IS 'Usuario que modificó el registro';
COMMENT ON COLUMN public.horarios_atencion.updated_at IS 'Fecha/hora de última modificación';