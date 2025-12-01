-- =====================================================
-- V28: Alinear servicios_configuracion con entidad ServicioConfiguracion
-- =====================================================
-- Problema: La entidad espera 'nombre_servicio' pero la tabla tiene 'nombre'
-- Solución: Renombrar columna si existe, o crear nueva desde nombre
-- =====================================================

DO $$
BEGIN
    -- Verificar si existe la columna 'nombre' y renombrarla a 'nombre_servicio'
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'servicios_configuracion' 
        AND column_name = 'nombre'
    ) THEN
        ALTER TABLE public.servicios_configuracion RENAME COLUMN nombre TO nombre_servicio;
        RAISE NOTICE 'Columna "nombre" renombrada a "nombre_servicio"';
    END IF;
    
    -- Si por alguna razón existe nombre_servicio con nulls, backfill con valor por defecto
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'servicios_configuracion' 
        AND column_name = 'nombre_servicio'
    ) THEN
        UPDATE public.servicios_configuracion 
        SET nombre_servicio = 'Servicio Sin Nombre - ID ' || id::TEXT
        WHERE nombre_servicio IS NULL;
        
        RAISE NOTICE 'Valores NULL en nombre_servicio actualizados';
    END IF;
END $$;

COMMENT ON COLUMN public.servicios_configuracion.nombre_servicio IS 'Nombre del servicio configurado (renombrado desde nombre)';
