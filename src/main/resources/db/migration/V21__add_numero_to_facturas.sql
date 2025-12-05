-- Agregar columna 'numero' a la tabla facturas y rellenar valores
-- Esta migración corrige el error de validación de esquema:
-- "Schema-validation: missing column [numero] in table [public.facturas]"

-- 1) Agregar columna si no existe
ALTER TABLE facturas
    ADD COLUMN IF NOT EXISTS numero VARCHAR(50);

-- 2) Rellenar 'numero' para filas existentes que estén NULL
-- Formato: FACT-YYYYMMDD-<secuencia de 4 dígitos>
DO $$
DECLARE
    r RECORD;
    seq INTEGER := 0;
BEGIN
    FOR r IN SELECT id_factura, fecha_emision FROM facturas WHERE numero IS NULL ORDER BY fecha_emision, id_factura LOOP
        seq := seq + 1;
        UPDATE facturas
        SET numero = 'FACT-' || to_char(r.fecha_emision, 'YYYYMMDD') || '-' || lpad(seq::text, 4, '0')
        WHERE id_factura = r.id_factura;
    END LOOP;
END $$;

-- 3) Hacer la columna NOT NULL y UNIQUE como exige el modelo
ALTER TABLE facturas
    ALTER COLUMN numero SET NOT NULL;

ALTER TABLE facturas
    ADD CONSTRAINT facturas_numero_uk UNIQUE (numero);

COMMENT ON COLUMN facturas.numero IS 'Número único de la factura (ej. FACT-20250101-0001)';