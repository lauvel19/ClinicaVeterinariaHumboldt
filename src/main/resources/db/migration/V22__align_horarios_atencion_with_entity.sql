-- Alinear la tabla horarios_atencion con la entidad HorarioAtencion
-- Corrige error: missing column [id_horario] en [public.horarios_atencion]

-- 1) Renombrar PK 'id' -> 'id_horario' si existe
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'horarios_atencion' AND column_name = 'id'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'horarios_atencion' AND column_name = 'id_horario'
    ) THEN
        ALTER TABLE public.horarios_atencion RENAME COLUMN id TO id_horario;
    END IF;
END $$;

-- 2) Convertir dia_semana INTEGER (1..7) a VARCHAR(20) con nombres
--    Primero eliminar CHECK constraints que referencian dia_semana como entero
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN 
        SELECT conname 
        FROM pg_constraint 
        WHERE conrelid = 'public.horarios_atencion'::regclass 
          AND contype = 'c'
    LOOP
        EXECUTE 'ALTER TABLE public.horarios_atencion DROP CONSTRAINT ' || quote_ident(rec.conname);
    END LOOP;
END $$;

ALTER TABLE public.horarios_atencion
    ALTER COLUMN dia_semana TYPE VARCHAR(20)
    USING CASE CAST(dia_semana AS INTEGER)
        WHEN 1 THEN 'LUNES'
        WHEN 2 THEN 'MARTES'
        WHEN 3 THEN 'MIERCOLES'
        WHEN 4 THEN 'JUEVES'
        WHEN 5 THEN 'VIERNES'
        WHEN 6 THEN 'SABADO'
        WHEN 7 THEN 'DOMINGO'
        ELSE 'LUNES'
    END;

-- 3) Agregar columna 'cerrado' y poblarla como NOT abierto, luego eliminar 'abierto'
ALTER TABLE public.horarios_atencion
    ADD COLUMN IF NOT EXISTS cerrado BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE public.horarios_atencion SET cerrado = NOT COALESCE(abierto, TRUE)
WHERE EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'horarios_atencion' AND column_name = 'abierto'
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'horarios_atencion' AND column_name = 'abierto'
    ) THEN
        ALTER TABLE public.horarios_atencion DROP COLUMN abierto;
    END IF;
END $$;

-- 4) Renombrar 'descripcion' -> 'notas'
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'horarios_atencion' AND column_name = 'descripcion'
    ) THEN
        ALTER TABLE public.horarios_atencion RENAME COLUMN descripcion TO notas;
    END IF;
END $$;

-- 5) Ajustar constraints e índices
-- Eliminar UNIQUE previo si existe y crear uno nuevo sobre dia_semana
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_horarios_atencion_dia'
    ) THEN
        ALTER TABLE public.horarios_atencion DROP CONSTRAINT uq_horarios_atencion_dia;
    END IF;
END $$;

ALTER TABLE public.horarios_atencion
    ADD CONSTRAINT uq_horarios_atencion_dia UNIQUE (dia_semana);

-- Restaurar constraint de horarios válidos (hora_cierre > hora_apertura)
ALTER TABLE public.horarios_atencion
    ADD CONSTRAINT chk_horarios_validos CHECK (hora_cierre > hora_apertura);

-- Índices sobre dia_semana y activo (recrear si faltan)
CREATE INDEX IF NOT EXISTS idx_horarios_atencion_dia ON public.horarios_atencion(dia_semana);
CREATE INDEX IF NOT EXISTS idx_horarios_atencion_activo ON public.horarios_atencion(activo);

COMMENT ON COLUMN public.horarios_atencion.id_horario IS 'PK de horarios_atencion, coincide con entidad JPA';
COMMENT ON COLUMN public.horarios_atencion.dia_semana IS 'Nombre del día (LUNES..DOMINGO)';
COMMENT ON COLUMN public.horarios_atencion.cerrado IS 'TRUE si la clínica está cerrada ese día';
COMMENT ON COLUMN public.horarios_atencion.notas IS 'Notas adicionales del horario';