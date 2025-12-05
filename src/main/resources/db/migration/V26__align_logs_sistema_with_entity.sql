-- =====================================================
-- V26: Alinear tabla logs_sistema con la entidad LogSistema
-- - Agregar columnas faltantes: usuario, ip_address
-- - Renombrar columna: metadata -> detalles
-- - Hacer columna componente NOT NULL
-- =====================================================

-- Agregar columnas faltantes si no existen
ALTER TABLE public.logs_sistema
    ADD COLUMN IF NOT EXISTS usuario VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);

-- Renombrar metadata -> detalles solo si existe metadata y no existe detalles
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'logs_sistema' AND column_name = 'metadata'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'logs_sistema' AND column_name = 'detalles'
    ) THEN
        EXECUTE 'ALTER TABLE public.logs_sistema RENAME COLUMN metadata TO detalles';
    END IF;
END$$;

-- Cambiar tipo de columna detalles de JSONB a TEXT si es necesario
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'logs_sistema' AND column_name = 'detalles'
        AND data_type = 'jsonb'
    ) THEN
        -- Convertir JSONB a TEXT preservando el contenido
        EXECUTE 'ALTER TABLE public.logs_sistema ALTER COLUMN detalles TYPE TEXT USING detalles::text';
    END IF;
END$$;

-- Asegurar que componente sea NOT NULL (si tiene valores nulos, establecer un valor por defecto)
UPDATE public.logs_sistema SET componente = 'SISTEMA' WHERE componente IS NULL;
ALTER TABLE public.logs_sistema ALTER COLUMN componente SET NOT NULL;

-- Comentarios de columnas para documentación
COMMENT ON COLUMN public.logs_sistema.usuario IS 'Usuario relacionado con el evento del log';
COMMENT ON COLUMN public.logs_sistema.ip_address IS 'Dirección IP desde donde se generó el evento';
COMMENT ON COLUMN public.logs_sistema.detalles IS 'Información adicional del evento en formato TEXT';
COMMENT ON COLUMN public.logs_sistema.componente IS 'Componente o módulo del sistema que generó el log';
