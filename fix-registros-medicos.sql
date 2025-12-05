-- Script para corregir el formato de insumos_usados y archivos en registros_medicos
-- Convierte objetos JSON a arrays vacíos para evitar errores de deserialización

-- Corregir insumos_usados que son objetos en lugar de arrays
UPDATE registros_medicos
SET insumos_usados = '[]'::jsonb
WHERE insumos_usados IS NOT NULL 
  AND jsonb_typeof(insumos_usados) = 'object';

-- Corregir archivos que son objetos en lugar de arrays  
UPDATE registros_medicos
SET archivos = '[]'::jsonb
WHERE archivos IS NOT NULL 
  AND jsonb_typeof(archivos) = 'object';

-- Asegurar que todos los registros tengan al menos arrays vacíos
UPDATE registros_medicos
SET insumos_usados = '[]'::jsonb
WHERE insumos_usados IS NULL;

UPDATE registros_medicos
SET archivos = '[]'::jsonb
WHERE archivos IS NULL;

-- Verificar los cambios
SELECT 
    COUNT(*) as total_registros,
    COUNT(CASE WHEN jsonb_typeof(insumos_usados) = 'array' THEN 1 END) as insumos_array,
    COUNT(CASE WHEN jsonb_typeof(archivos) = 'array' THEN 1 END) as archivos_array
FROM registros_medicos;
