-- Migración: Agregar columna foto_perfil a tabla pacientes
-- Descripción: Permite almacenar la URL o path de la foto de perfil del paciente
-- Autor: Sistema
-- Fecha: 2025-12-05

ALTER TABLE public.pacientes 
ADD COLUMN foto_perfil VARCHAR(500);

COMMENT ON COLUMN public.pacientes.foto_perfil IS 'URL o path relativo de la foto de perfil del paciente';
