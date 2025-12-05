-- =====================================================
-- V29: Crear usuarios demo con contraseñas BCrypt válidas
-- Script robusto de seeders para pruebas/producción
-- =====================================================
-- NOTA: Esta migración crea usuarios si no existen
-- y actualiza contraseñas si ya existen.
-- =====================================================

-- Asegurar que existan los roles básicos
INSERT INTO roles (nombre_rol, descripcion) VALUES
    ('ADMIN', 'Administrador del sistema con acceso completo'),
    ('VETERINARIO', 'Veterinario que atiende pacientes'),
    ('SECRETARIO', 'Secretario que gestiona citas e inventario'),
    ('CLIENTE', 'Cliente que posee mascotas')
ON CONFLICT (nombre_rol) DO NOTHING;

-- ========================================
-- 1. CREAR USUARIO ADMIN
-- ========================================
DO $$
DECLARE
    v_persona_id BIGINT;
    v_rol_id BIGINT;
BEGIN
    SELECT id_rol INTO v_rol_id FROM roles WHERE nombre_rol = 'ADMIN';
    
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin') THEN
        SELECT id_persona INTO v_persona_id FROM personas WHERE correo = 'admin@veterinaria.com';
        
        IF v_persona_id IS NULL THEN
            INSERT INTO personas (nombre, apellido, correo, telefono, direccion)
            VALUES ('Administrador', 'Sistema', 'admin@veterinaria.com', '3001234567', 'Oficina Principal')
            RETURNING id_persona INTO v_persona_id;
        END IF;
        
        -- Password: Admin123! (BCrypt hash)
        INSERT INTO usuarios (id_usuario, username, password_hash, activo, rol_id)
        VALUES (v_persona_id, 'admin', '$2a$10$vuN7SkhOYsXagxpX0GOqJe7/REAKgMBBqIGpgCDlQoaydoiYgNhmK', true, v_rol_id);
        
        RAISE NOTICE 'Usuario admin creado';
    ELSE
        UPDATE usuarios 
        SET password_hash = '$2a$10$vuN7SkhOYsXagxpX0GOqJe7/REAKgMBBqIGpgCDlQoaydoiYgNhmK', 
            activo = true 
        WHERE username = 'admin';
        RAISE NOTICE 'Password de admin actualizado';
    END IF;
END $$;

-- ========================================
-- 2. CREAR USUARIO SUPERADMIN
-- ========================================
DO $$
DECLARE
    v_persona_id BIGINT;
    v_rol_id BIGINT;
BEGIN
    SELECT id_rol INTO v_rol_id FROM roles WHERE nombre_rol = 'ADMIN';
    
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'superadmin') THEN
        SELECT id_persona INTO v_persona_id FROM personas WHERE correo = 'superadmin@veterinaria.com';
        
        IF v_persona_id IS NULL THEN
            INSERT INTO personas (nombre, apellido, correo, telefono, direccion)
            VALUES ('Super', 'Admin', 'superadmin@veterinaria.com', '3001111111', 'Sede Principal')
            RETURNING id_persona INTO v_persona_id;
        END IF;
        
        -- Password: password (BCrypt hash)
        INSERT INTO usuarios (id_usuario, username, password_hash, activo, rol_id)
        VALUES (v_persona_id, 'superadmin', '$2a$10$E90wKHHwjkV5ymQAJDpIau0n.MbEhyubBVANBeIJf0m2q75gNi3Iu', true, v_rol_id);
        
        RAISE NOTICE 'Usuario superadmin creado';
    ELSE
        UPDATE usuarios 
        SET password_hash = '$2a$10$E90wKHHwjkV5ymQAJDpIau0n.MbEhyubBVANBeIJf0m2q75gNi3Iu',
            activo = true 
        WHERE username = 'superadmin';
    END IF;
END $$;

-- ========================================
-- 3. CREAR USUARIO VETERINARIO
-- ========================================
DO $$
DECLARE
    v_persona_id BIGINT;
    v_rol_id BIGINT;
BEGIN
    SELECT id_rol INTO v_rol_id FROM roles WHERE nombre_rol = 'VETERINARIO';
    
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'vet_carlos') THEN
        SELECT id_persona INTO v_persona_id FROM personas WHERE correo = 'carlos.vet@veterinaria.com';
        
        IF v_persona_id IS NULL THEN
            INSERT INTO personas (nombre, apellido, correo, telefono, direccion)
            VALUES ('Carlos', 'Méndez', 'carlos.vet@veterinaria.com', '3002222222', 'Consultorio 1')
            RETURNING id_persona INTO v_persona_id;
        END IF;
        
        -- Password: Test1234! (BCrypt hash)
        INSERT INTO usuarios (id_usuario, username, password_hash, activo, rol_id)
        VALUES (v_persona_id, 'vet_carlos', '$2a$10$ZvMhy7/Ve9S2tlAPnLuknexiB418t3lx7r00Jj0a4SDU19yNeyWiO', true, v_rol_id);
        
        -- Crear registro en tabla veterinarios
        INSERT INTO usuarios_veterinarios (id_usuario, licencia_profesional, especialidad, disponibilidad)
        VALUES (v_persona_id, 'MVZ-12345', 'Medicina General', '{"lunes":["09:00-17:00"]}'::jsonb)
        ON CONFLICT (id_usuario) DO NOTHING;
        
        RAISE NOTICE 'Usuario veterinario creado';
    ELSE
        UPDATE usuarios 
        SET password_hash = '$2a$10$ZvMhy7/Ve9S2tlAPnLuknexiB418t3lx7r00Jj0a4SDU19yNeyWiO',
            activo = true 
        WHERE username = 'vet_carlos';
    END IF;
END $$;

-- ========================================
-- 4. CREAR USUARIO SECRETARIO
-- ========================================
DO $$
DECLARE
    v_persona_id BIGINT;
    v_rol_id BIGINT;
BEGIN
    SELECT id_rol INTO v_rol_id FROM roles WHERE nombre_rol = 'SECRETARIO';
    
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'sec_laura') THEN
        SELECT id_persona INTO v_persona_id FROM personas WHERE correo = 'laura.sec@veterinaria.com';
        
        IF v_persona_id IS NULL THEN
            INSERT INTO personas (nombre, apellido, correo, telefono, direccion)
            VALUES ('Laura', 'Gómez', 'laura.sec@veterinaria.com', '3003333333', 'Recepción')
            RETURNING id_persona INTO v_persona_id;
        END IF;
        
        -- Password: Test1234! (BCrypt hash)
        INSERT INTO usuarios (id_usuario, username, password_hash, activo, rol_id)
        VALUES (v_persona_id, 'sec_laura', '$2a$10$ZvMhy7/Ve9S2tlAPnLuknexiB418t3lx7r00Jj0a4SDU19yNeyWiO', true, v_rol_id);
        
        -- Crear registro en tabla secretarios
        INSERT INTO secretarios (id_usuario, extension)
        VALUES (v_persona_id, '101')
        ON CONFLICT (id_usuario) DO NOTHING;
        
        RAISE NOTICE 'Usuario secretario creado';
    ELSE
        UPDATE usuarios 
        SET password_hash = '$2a$10$ZvMhy7/Ve9S2tlAPnLuknexiB418t3lx7r00Jj0a4SDU19yNeyWiO',
            activo = true 
        WHERE username = 'sec_laura';
    END IF;
END $$;

-- ========================================
-- 5. CREAR USUARIO CLIENTE
-- ========================================
DO $$
DECLARE
    v_persona_id BIGINT;
    v_rol_id BIGINT;
BEGIN
    SELECT id_rol INTO v_rol_id FROM roles WHERE nombre_rol = 'CLIENTE';
    
    IF NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'cliente_diego') THEN
        SELECT id_persona INTO v_persona_id FROM personas WHERE correo = 'diego.cliente@email.com';
        
        IF v_persona_id IS NULL THEN
            INSERT INTO personas (nombre, apellido, correo, telefono, direccion)
            VALUES ('Diego', 'López', 'diego.cliente@email.com', '3004444444', 'Calle 10 #5-20')
            RETURNING id_persona INTO v_persona_id;
        END IF;
        
        -- Password: Test1234! (BCrypt hash)
        INSERT INTO usuarios (id_usuario, username, password_hash, activo, rol_id)
        VALUES (v_persona_id, 'cliente_diego', '$2a$10$ZvMhy7/Ve9S2tlAPnLuknexiB418t3lx7r00Jj0a4SDU19yNeyWiO', true, v_rol_id);
        
        -- Crear registro en tabla clientes
        INSERT INTO clientes (id_usuario, fecha_registro, documento_identidad)
        VALUES (v_persona_id, NOW(), 'CC-123456789')
        ON CONFLICT (id_usuario) DO NOTHING;
        
        RAISE NOTICE 'Usuario cliente creado';
    ELSE
        UPDATE usuarios 
        SET password_hash = '$2a$10$ZvMhy7/Ve9S2tlAPnLuknexiB418t3lx7r00Jj0a4SDU19yNeyWiO',
            activo = true 
        WHERE username = 'cliente_diego';
    END IF;
END $$;

-- =====================================================
-- CREDENCIALES DE ACCESO:
-- =====================================================
-- | Usuario       | Password   | Rol         |
-- |---------------|------------|-------------|
-- | admin         | Admin123!  | ADMIN       |
-- | superadmin    | password   | ADMIN       |
-- | vet_carlos    | Test1234!  | VETERINARIO |
-- | sec_laura     | Test1234!  | SECRETARIO  |
-- | cliente_diego | Test1234!  | CLIENTE     |
-- =====================================================
