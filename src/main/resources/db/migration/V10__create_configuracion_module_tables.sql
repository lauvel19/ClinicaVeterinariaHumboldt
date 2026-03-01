-- =====================================================
-- V10: Crear tablas para el módulo de Configuración del Sistema
-- =====================================================
-- Este script procesa 7 tablas que extienden Auditable:
-- - informacion_clinica
-- - permisos_rol
-- - servicios_configuracion
-- - horarios_atencion
-- - auditoria_detallada
-- - respaldos_sistema
-- - configuracion_avanzada
-- =====================================================

-- =====================================================
-- 1. INFORMACIÓN GENERAL DE LA CLÍNICA
-- =====================================================
CREATE TABLE IF NOT EXISTS informacion_clinica (
    id_clinica BIGSERIAL PRIMARY KEY,
    nombre_clinica VARCHAR(200) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    sitio_web VARCHAR(200),
    logo_url VARCHAR(500),
    mision TEXT,
    vision TEXT,
    horario_atencion TEXT,
    redes_sociales JSONB,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_informacion_clinica_created_at ON informacion_clinica(created_at);

INSERT INTO informacion_clinica (
    nombre_clinica, telefono, email, direccion, sitio_web,
    created_by, created_at
) VALUES (
    'Clínica Veterinaria Universitaria Humboldt',
    '+57 312 456 7890', 
    'contacto@vetclinic.com',
    'Calle 123 #45-67, Armenia, Quindío',
    'https://vetclinic.com',
    'SYSTEM',
    CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. PERMISOS POR ROL
-- =====================================================
-- Campos: idPermiso, rolId, modulo, accion, descripcion, activo
-- Extends: Auditable
CREATE TABLE IF NOT EXISTS permisos_rol (
    id_permiso BIGSERIAL PRIMARY KEY,
    rol_id BIGINT NOT NULL REFERENCES roles(id_rol) ON DELETE CASCADE,
    modulo VARCHAR(50) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_permisos_rol_modulo_accion UNIQUE (rol_id, modulo, accion)
);

CREATE INDEX idx_permisos_rol_rol_id ON permisos_rol(rol_id);
CREATE INDEX idx_permisos_rol_modulo ON permisos_rol(modulo);
CREATE INDEX idx_permisos_rol_activo ON permisos_rol(activo);

INSERT INTO permisos_rol (rol_id, modulo, accion, descripcion, activo, created_by) VALUES
(1, 'usuarios', 'leer', 'Ver lista de usuarios', TRUE, 'SYSTEM'),
(1, 'usuarios', 'crear', 'Crear nuevos usuarios', TRUE, 'SYSTEM'),
(1, 'usuarios', 'editar', 'Editar usuarios', TRUE, 'SYSTEM'),
(1, 'usuarios', 'eliminar', 'Eliminar usuarios', TRUE, 'SYSTEM'),
(1, 'inventario', 'leer', 'Ver inventario', TRUE, 'SYSTEM'),
(1, 'inventario', 'gestionar', 'Gestionar inventario', TRUE, 'SYSTEM'),
(1, 'reportes', 'generar', 'Generar reportes', TRUE, 'SYSTEM'),
(1, 'configuracion', 'gestionar', 'Acceso total a configuración', TRUE, 'SYSTEM'),
(2, 'pacientes', 'leer', 'Ver pacientes', TRUE, 'SYSTEM'),
(2, 'pacientes', 'gestionar', 'Gestionar pacientes', TRUE, 'SYSTEM'),
(2, 'historias', 'leer', 'Ver historias clínicas', TRUE, 'SYSTEM'),
(2, 'historias', 'escribir', 'Editar historias clínicas', TRUE, 'SYSTEM'),
(2, 'consultas', 'realizar', 'Realizar consultas', TRUE, 'SYSTEM'),
(3, 'citas', 'leer', 'Ver citas', TRUE, 'SYSTEM'),
(3, 'citas', 'crear', 'Crear citas', TRUE, 'SYSTEM'),
(3, 'citas', 'editar', 'Editar citas', TRUE, 'SYSTEM'),
(3, 'clientes', 'leer', 'Ver clientes', TRUE, 'SYSTEM'),
(3, 'facturas', 'leer', 'Ver facturas', TRUE, 'SYSTEM'),
(4, 'citas', 'leer', 'Ver mis citas', TRUE, 'SYSTEM'),
(4, 'citas', 'solicitar', 'Solicitar citas', TRUE, 'SYSTEM'),
(4, 'facturas', 'leer', 'Ver mis facturas', TRUE, 'SYSTEM');

-- =====================================================
-- 3. SERVICIOS CONFIGURACIÓN
-- =====================================================
-- Campos: idServicioConfig, servicioId, nombreServicio, descripcion, precio, duracionMinutos, categoria, activo
-- Extends: Auditable
CREATE TABLE IF NOT EXISTS servicios_configuracion (
    id_servicio_config BIGSERIAL PRIMARY KEY,
    servicio_id BIGINT,
    nombre_servicio VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10, 2),
    duracion_minutos INTEGER,
    categoria VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_servicios_config_activo ON servicios_configuracion(activo);
CREATE INDEX idx_servicios_config_categoria ON servicios_configuracion(categoria);

INSERT INTO servicios_configuracion (nombre_servicio, descripcion, precio, duracion_minutos, categoria, activo, created_by) VALUES
('Consulta General', 'Examen médico completo y diagnóstico', 250000.00, 30, 'Consulta', TRUE, 'SYSTEM'),
('Vacunación', 'Aplicación de vacunas', 180000.00, 20, 'Prevención', TRUE, 'SYSTEM'),
('Cirugía', 'Procedimientos quirúrgicos', 450000.00, 120, 'Cirugía', TRUE, 'SYSTEM'),
('Control', 'Seguimiento post-tratamiento', 150000.00, 20, 'Consulta', TRUE, 'SYSTEM'),
('Desparasitación', 'Tratamiento antiparasitario', 80000.00, 15, 'Prevención', TRUE, 'SYSTEM');

-- =====================================================
-- 4. HORARIOS DE ATENCIÓN
-- =====================================================
-- Campos: idHorario, diaSemana (ENUM STRING), horaApertura, horaCierre, cerrado, notas
-- Extends: Auditable
CREATE TABLE IF NOT EXISTS horarios_atencion (
    id_horario BIGSERIAL PRIMARY KEY,
    dia_semana VARCHAR(20) NOT NULL,
    hora_apertura TIME,
    hora_cierre TIME,
    cerrado BOOLEAN NOT NULL DEFAULT FALSE,
    notas VARCHAR(200),
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_horarios_atencion_dia UNIQUE (dia_semana),
    CONSTRAINT chk_dia_semana CHECK (dia_semana IN ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO'))
);

CREATE INDEX idx_horarios_atencion_dia ON horarios_atencion(dia_semana);

INSERT INTO horarios_atencion (dia_semana, hora_apertura, hora_cierre, cerrado, notas, created_by) VALUES
('LUNES', '08:00:00', '18:00:00', FALSE, 'Horario normal', 'SYSTEM'),
('MARTES', '08:00:00', '18:00:00', FALSE, 'Horario normal', 'SYSTEM'),
('MIERCOLES', '08:00:00', '18:00:00', FALSE, 'Horario normal', 'SYSTEM'),
('JUEVES', '08:00:00', '18:00:00', FALSE, 'Horario normal', 'SYSTEM'),
('VIERNES', '08:00:00', '18:00:00', FALSE, 'Horario normal', 'SYSTEM'),
('SABADO', '09:00:00', '18:00:00', FALSE, 'Horario reducido', 'SYSTEM'),
('DOMINGO', NULL, NULL, TRUE, 'Cerrado', 'SYSTEM');

-- =====================================================
-- 5. AUDITORÍA DETALLADA
-- =====================================================
-- Campos: idAuditoria, usuarioId, tipoAccion, entidad, entidadId, 
--         datosAntes, datosDespues, ipOrigen, userAgent, descripcion
-- Extends: Auditable
CREATE TABLE IF NOT EXISTS auditoria_detallada (
    id_auditoria BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    tipo_accion VARCHAR(50) NOT NULL,
    entidad VARCHAR(100) NOT NULL,
    entidad_id BIGINT,
    datos_antes JSONB,
    datos_despues JSONB,
    ip_origen VARCHAR(45),
    user_agent VARCHAR(500),
    descripcion TEXT,
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auditoria_usuario ON auditoria_detallada(usuario_id);
CREATE INDEX idx_auditoria_tipo_accion ON auditoria_detallada(tipo_accion);
CREATE INDEX idx_auditoria_entidad ON auditoria_detallada(entidad, entidad_id);
CREATE INDEX idx_auditoria_created_at ON auditoria_detallada(created_at DESC);

-- =====================================================
-- 6. RESPALDOS DEL SISTEMA
-- =====================================================
-- Campos: idRespaldo, usuarioId, fechaRespaldo, tipoRespaldo, 
--         rutaArchivo, tamanoBytes, hashVerificacion, estado, descripcion, errorMensaje
-- Extends: Auditable
CREATE TABLE IF NOT EXISTS respaldos_sistema (
    id_respaldo BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    fecha_respaldo TIMESTAMP NOT NULL,
    tipo_respaldo VARCHAR(20) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    tamano_bytes BIGINT,
    hash_verificacion VARCHAR(64),
    estado VARCHAR(20) NOT NULL,
    descripcion TEXT,
    error_mensaje TEXT,
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_tipo_respaldo CHECK (tipo_respaldo IN ('COMPLETO', 'INCREMENTAL', 'DIFERENCIAL', 'CONFIGURACION')),
    CONSTRAINT chk_estado_respaldo CHECK (estado IN ('COMPLETADO', 'EN_PROCESO', 'FALLIDO', 'CORRUPTO', 'RESTAURADO'))
);

CREATE INDEX idx_respaldos_usuario ON respaldos_sistema(usuario_id);
CREATE INDEX idx_respaldos_tipo ON respaldos_sistema(tipo_respaldo);
CREATE INDEX idx_respaldos_fecha ON respaldos_sistema(fecha_respaldo DESC);
CREATE INDEX idx_respaldos_estado ON respaldos_sistema(estado);

-- =====================================================
-- 7. CONFIGURACIÓN AVANZADA
-- =====================================================
-- Campos: idConfiguracion, clave, valor, categoria, tipoDato, descripcion, editable, activo
-- Extends: Auditable
CREATE TABLE IF NOT EXISTS configuracion_avanzada (
    id_configuracion BIGSERIAL PRIMARY KEY,
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT NOT NULL,
    categoria VARCHAR(50),
    tipo_dato VARCHAR(20) NOT NULL,
    descripcion VARCHAR(500),
    editable BOOLEAN NOT NULL DEFAULT TRUE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Heredadas de Auditable
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_tipo_dato CHECK (tipo_dato IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON', 'DECIMAL', 'DATE', 'TIME'))
);

CREATE INDEX idx_config_categoria ON configuracion_avanzada(categoria);
CREATE INDEX idx_config_editable ON configuracion_avanzada(editable);
CREATE INDEX idx_config_activo ON configuracion_avanzada(activo);

INSERT INTO configuracion_avanzada (clave, valor, categoria, tipo_dato, descripcion, editable, activo, created_by) VALUES
('respaldos.automaticos.habilitado', 'true', 'RESPALDOS', 'BOOLEAN', 'Habilitar respaldos automáticos', TRUE, TRUE, 'SYSTEM'),
('respaldos.automaticos.hora', '02:00', 'RESPALDOS', 'STRING', 'Hora de ejecución de respaldos', TRUE, TRUE, 'SYSTEM'),
('respaldos.retencion.dias', '30', 'RESPALDOS', 'INTEGER', 'Días de retención', TRUE, TRUE, 'SYSTEM'),
('auditoria.retener.dias', '90', 'AUDITORIA', 'INTEGER', 'Días de retención de auditoría', TRUE, TRUE, 'SYSTEM'),
('citas.duracion.estandar', '30', 'CITAS', 'INTEGER', 'Duración estándar en minutos', TRUE, TRUE, 'SYSTEM'),
('citas.maximo.dia', '12', 'CITAS', 'INTEGER', 'Máximo de citas por día', TRUE, TRUE, 'SYSTEM'),
('citas.anticipacion.horas', '24', 'CITAS', 'INTEGER', 'Horas de anticipación para agendar', TRUE, TRUE, 'SYSTEM');
