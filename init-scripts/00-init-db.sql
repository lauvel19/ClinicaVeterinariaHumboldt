-- ============================================
-- SCRIPT DE INICIALIZACIÓN DE BASE DE DATOS
-- Clínica Veterinaria Humboldt
-- ============================================
-- Este script solo crea extensiones necesarias.
-- Las tablas y datos son manejados por Flyway.
-- ============================================

-- Crear extensiones útiles (deben existir antes de Flyway)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Configurar timezone
SET timezone = 'America/Bogota';

-- Log de inicialización
DO $$
BEGIN
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Base de datos veterinaria_db inicializada';
    RAISE NOTICE 'Extensiones creadas correctamente';
    RAISE NOTICE 'Flyway manejará las tablas y datos';
    RAISE NOTICE 'Fecha: %', NOW();
    RAISE NOTICE '============================================';
END $$;
