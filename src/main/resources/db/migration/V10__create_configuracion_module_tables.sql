-- =====================================================
-- V10: Crear tablas para el módulo de Configuración del Sistema
-- Descripción: Este módulo implementa una gestión centralizada de la configuración
-- con soporte para información de la clínica, permisos dinámicos por rol,
-- servicios veterinarios, horarios de atención, auditoría contextual y respaldos.
-- 
-- Patrones implementados:
-- - Singleton: ConfigService gestiona configuración centralizada
-- - Command: Operaciones auditables con historial
-- - Memento: Respaldos con posibilidad de restauración
-- =====================================================

-- =====================================================
-- 1. INFORMACIÓN GENERAL DE LA CLÍNICA
-- Tabla para almacenar datos básicos de la clínica (nombre, NIT, contacto, etc.)
-- Sigue el patrón Singleton ya que solo debe haber UN registro activo
-- =====================================================
CREATE TABLE IF NOT EXISTS informacion_clinica (
    id BIGSERIAL PRIMARY KEY,
    nombre_clinica VARCHAR(255) NOT NULL,
    nit VARCHAR(50) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(255),
    direccion VARCHAR(500),
    idioma VARCHAR(10) DEFAULT 'es',
    moneda VARCHAR(10) DEFAULT 'COP',
    zona_horaria VARCHAR(50) DEFAULT 'America/Bogota',
    formato_fecha VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    logo_url VARCHAR(500),
    
    -- Auditoría
    creado_por VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(255),
    fecha_modificacion TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Constraint para asegurar solo UN registro activo (Singleton)
    CONSTRAINT uq_informacion_clinica_activo UNIQUE (activo)
);

-- Índices para optimizar consultas
CREATE INDEX idx_informacion_clinica_activo ON informacion_clinica(activo);

-- Insertar registro inicial con datos de ejemplo
INSERT INTO informacion_clinica (
    nombre_clinica, 
    nit, 
    telefono, 
    email, 
    direccion,
    idioma,
    moneda,
    zona_horaria,
    formato_fecha,
    creado_por,
    activo
) VALUES (
    'Clínica Veterinaria Universitaria Humboldt',
    '900.123.456-7',
    '+57 312 456 7890',
    'contacto@vetclinic.com',
    'Calle 123 #45-67, Armenia, Quindío',
    'es',
    'COP',
    'America/Bogota',
    'DD/MM/YYYY',
    'SYSTEM',
    TRUE
);

-- =====================================================
-- 2. PERMISOS POR ROL (Gestión Dinámica)
-- Tabla para gestionar qué rutas/permisos tiene cada rol del sistema
-- Esto permite configurar dinámicamente el acceso sin modificar código
-- =====================================================
CREATE TABLE IF NOT EXISTS permisos_rol (
    id BIGSERIAL PRIMARY KEY,
    rol_id BIGINT NOT NULL REFERENCES roles(id_rol) ON DELETE CASCADE,
    
    -- Información del permiso
    modulo VARCHAR(100) NOT NULL, -- Ej: 'usuarios', 'inventario', 'citas'
    accion VARCHAR(100) NOT NULL, -- Ej: 'ver', 'crear', 'editar', 'eliminar'
    ruta VARCHAR(255), -- Ruta frontend si aplica: '/admin/usuarios'
    descripcion TEXT,
    
    -- Control de acceso
    permitido BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    creado_por VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(255),
    fecha_modificacion TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Constraint para evitar duplicados
    CONSTRAINT uq_permisos_rol_modulo_accion UNIQUE (rol_id, modulo, accion)
);

-- Índices para optimizar consultas de permisos
CREATE INDEX idx_permisos_rol_rol_id ON permisos_rol(rol_id);
CREATE INDEX idx_permisos_rol_modulo ON permisos_rol(modulo);
CREATE INDEX idx_permisos_rol_activo ON permisos_rol(activo);

-- Insertar permisos base para ROL ADMIN (id=1)
INSERT INTO permisos_rol (rol_id, modulo, accion, ruta, descripcion, permitido, creado_por, activo) VALUES
(1, 'usuarios', 'ver', '/usuarios', 'Ver lista de usuarios', TRUE, 'SYSTEM', TRUE),
(1, 'usuarios', 'crear', '/usuarios', 'Crear nuevos usuarios', TRUE, 'SYSTEM', TRUE),
(1, 'usuarios', 'editar', '/usuarios', 'Editar usuarios existentes', TRUE, 'SYSTEM', TRUE),
(1, 'usuarios', 'eliminar', '/usuarios', 'Eliminar usuarios', TRUE, 'SYSTEM', TRUE),
(1, 'inventario', 'ver', '/admin/inventario', 'Ver inventario', TRUE, 'SYSTEM', TRUE),
(1, 'inventario', 'gestionar', '/admin/inventario', 'Gestionar inventario completo', TRUE, 'SYSTEM', TRUE),
(1, 'finanzas', 'ver', '/admin/finanzas', 'Ver finanzas', TRUE, 'SYSTEM', TRUE),
(1, 'reportes', 'generar', '/reportes', 'Generar reportes', TRUE, 'SYSTEM', TRUE),
(1, 'configuracion', 'gestionar', '/configuracion', 'Acceso total a configuración', TRUE, 'SYSTEM', TRUE);

-- Insertar permisos base para ROL VETERINARIO (id=2)
INSERT INTO permisos_rol (rol_id, modulo, accion, ruta, descripcion, permitido, creado_por, activo) VALUES
(2, 'pacientes', 'ver', '/veterinario/pacientes', 'Ver pacientes', TRUE, 'SYSTEM', TRUE),
(2, 'pacientes', 'gestionar', '/veterinario/pacientes', 'Gestionar pacientes', TRUE, 'SYSTEM', TRUE),
(2, 'historias', 'ver', '/veterinario/historias', 'Ver historias clínicas', TRUE, 'SYSTEM', TRUE),
(2, 'historias', 'editar', '/veterinario/historias', 'Editar historias clínicas', TRUE, 'SYSTEM', TRUE),
(2, 'consultas', 'realizar', '/veterinario/consultas', 'Realizar consultas', TRUE, 'SYSTEM', TRUE),
(2, 'citas', 'gestionar', '/veterinario/agenda', 'Gestionar agenda de citas', TRUE, 'SYSTEM', TRUE),
(2, 'inventario', 'ver', '/veterinario/inventario', 'Ver inventario disponible', TRUE, 'SYSTEM', TRUE);

-- Insertar permisos base para ROL SECRETARIO (id=3)
INSERT INTO permisos_rol (rol_id, modulo, accion, ruta, descripcion, permitido, creado_por, activo) VALUES
(3, 'citas', 'ver', '/secretario/citas', 'Ver citas', TRUE, 'SYSTEM', TRUE),
(3, 'citas', 'crear', '/secretario/citas', 'Crear citas', TRUE, 'SYSTEM', TRUE),
(3, 'citas', 'editar', '/secretario/citas', 'Editar citas', TRUE, 'SYSTEM', TRUE),
(3, 'clientes', 'ver', '/clientes', 'Ver clientes', TRUE, 'SYSTEM', TRUE),
(3, 'clientes', 'gestionar', '/clientes', 'Gestionar clientes', TRUE, 'SYSTEM', TRUE),
(3, 'facturas', 'ver', '/secretario/facturas', 'Ver facturas', TRUE, 'SYSTEM', TRUE),
(3, 'facturas', 'crear', '/secretario/facturas', 'Crear facturas', TRUE, 'SYSTEM', TRUE),
(3, 'inventario', 'ver', '/secretario/inventario', 'Ver inventario', TRUE, 'SYSTEM', TRUE),
(3, 'notificaciones', 'enviar', '/notificaciones', 'Enviar notificaciones', TRUE, 'SYSTEM', TRUE);

-- Insertar permisos base para ROL CLIENTE (id=4)
INSERT INTO permisos_rol (rol_id, modulo, accion, ruta, descripcion, permitido, creado_por, activo) VALUES
(4, 'mascotas', 'ver', '/cliente/mascotas', 'Ver mis mascotas', TRUE, 'SYSTEM', TRUE),
(4, 'citas', 'ver', '/cliente/citas', 'Ver mis citas', TRUE, 'SYSTEM', TRUE),
(4, 'citas', 'solicitar', '/cliente/citas', 'Solicitar citas', TRUE, 'SYSTEM', TRUE),
(4, 'historial', 'ver', '/cliente/historial', 'Ver historial médico', TRUE, 'SYSTEM', TRUE),
(4, 'facturas', 'ver', '/cliente/facturas', 'Ver mis facturas', TRUE, 'SYSTEM', TRUE);

-- =====================================================
-- 3. SERVICIOS VETERINARIOS
-- Tabla para gestionar los servicios que ofrece la clínica
-- Se sincroniza con la tabla 'servicio' existente pero con gestión adicional
-- =====================================================
CREATE TABLE IF NOT EXISTS servicios_configuracion (
    id BIGSERIAL PRIMARY KEY,
    servicio_id BIGINT REFERENCES servicios(id_servicio) ON DELETE SET NULL,
    
    -- Información del servicio
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio_base DECIMAL(10,2) NOT NULL,
    duracion_estimada_minutos INTEGER DEFAULT 30,
    
    -- Configuración
    disponible BOOLEAN DEFAULT TRUE,
    requiere_cita BOOLEAN DEFAULT TRUE,
    color_hex VARCHAR(7) DEFAULT '#3B82F6', -- Para UI
    icono VARCHAR(50), -- Nombre del ícono para frontend
    
    -- Auditoría
    creado_por VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(255),
    fecha_modificacion TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- Índices
CREATE INDEX idx_servicios_config_disponible ON servicios_configuracion(disponible);
CREATE INDEX idx_servicios_config_activo ON servicios_configuracion(activo);
CREATE INDEX idx_servicios_config_servicio_id ON servicios_configuracion(servicio_id);

-- Insertar servicios iniciales
INSERT INTO servicios_configuracion (nombre, descripcion, precio_base, duracion_estimada_minutos, disponible, requiere_cita, color_hex, icono, creado_por, activo) VALUES
('Consulta General', 'Examen médico completo y diagnóstico', 250000.00, 30, TRUE, TRUE, '#3B82F6', 'stethoscope', 'SYSTEM', TRUE),
('Vacunación', 'Aplicación de vacunas y esquema de inmunización', 180000.00, 20, TRUE, TRUE, '#10B981', 'syringe', 'SYSTEM', TRUE),
('Cirugía', 'Procedimientos quirúrgicos y atención especializada', 450000.00, 120, TRUE, TRUE, '#EF4444', 'scissors', 'SYSTEM', TRUE),
('Control', 'Seguimiento y control post-tratamiento', 150000.00, 20, TRUE, TRUE, '#8B5CF6', 'clipboard', 'SYSTEM', TRUE),
('Desparasitación', 'Tratamiento antiparasitario interno y externo', 80000.00, 15, TRUE, TRUE, '#F59E0B', 'bug', 'SYSTEM', TRUE);

-- =====================================================
-- 4. HORARIOS DE ATENCIÓN
-- Tabla para gestionar los horarios de atención de la clínica
-- Permite configurar horarios diferentes para cada día de la semana
-- =====================================================
CREATE TABLE IF NOT EXISTS horarios_atencion (
    id BIGSERIAL PRIMARY KEY,
    
    -- Día de la semana (1=Lunes, 7=Domingo)
    dia_semana INTEGER NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    
    -- Horarios
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,
    
    -- Control
    abierto BOOLEAN DEFAULT TRUE,
    descripcion VARCHAR(255), -- Ej: "Horario de atención regular", "Cerrado"
    
    -- Auditoría
    creado_por VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(255),
    fecha_modificacion TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Constraint para evitar duplicados
    CONSTRAINT uq_horarios_atencion_dia UNIQUE (dia_semana, activo),
    
    -- Constraint para validar horarios
    CONSTRAINT chk_horarios_validos CHECK (hora_cierre > hora_apertura)
);

-- Índices
CREATE INDEX idx_horarios_atencion_dia ON horarios_atencion(dia_semana);
CREATE INDEX idx_horarios_atencion_activo ON horarios_atencion(activo);

-- Insertar horarios iniciales (Lunes a Viernes 8:00 AM - 6:00 PM, Sábados 9:00 AM - 6:00 PM)
INSERT INTO horarios_atencion (dia_semana, hora_apertura, hora_cierre, abierto, descripcion, creado_por, activo) VALUES
(1, '08:00:00', '18:00:00', TRUE, 'Lunes a Viernes - Horario de atención regular', 'SYSTEM', TRUE),
(2, '08:00:00', '18:00:00', TRUE, 'Lunes a Viernes - Horario de atención regular', 'SYSTEM', TRUE),
(3, '08:00:00', '18:00:00', TRUE, 'Lunes a Viernes - Horario de atención regular', 'SYSTEM', TRUE),
(4, '08:00:00', '18:00:00', TRUE, 'Lunes a Viernes - Horario de atención regular', 'SYSTEM', TRUE),
(5, '08:00:00', '18:00:00', TRUE, 'Lunes a Viernes - Horario de atención regular', 'SYSTEM', TRUE),
(6, '09:00:00', '18:00:00', TRUE, 'Sábados - Horario de atención fin de semana', 'SYSTEM', TRUE),
(7, '08:00:00', '12:00:00', FALSE, 'Domingos y Festivos - Cerrado', 'SYSTEM', TRUE);

-- =====================================================
-- 5. AUDITORÍA CONTEXTUAL POR ROL
-- Ampliar la tabla historial_acciones existente con más contexto
-- Esto complementa la tabla existente sin modificarla
-- =====================================================
CREATE TABLE IF NOT EXISTS auditoria_detallada (
    id BIGSERIAL PRIMARY KEY,
    historial_accion_id BIGINT REFERENCES historial_acciones(id_accion) ON DELETE CASCADE,
    
    -- Contexto adicional
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    rol_nombre VARCHAR(50) NOT NULL, -- Nombre del rol para búsqueda rápida
    
    -- Detalles de la acción
    modulo VARCHAR(100) NOT NULL, -- Módulo afectado
    entidad VARCHAR(100), -- Entidad afectada (Usuario, Cita, Paciente, etc.)
    entidad_id BIGINT, -- ID de la entidad afectada
    
    -- Datos antes/después (Patrón Memento)
    datos_anteriores JSONB, -- Estado anterior en formato JSON
    datos_nuevos JSONB, -- Estado nuevo en formato JSON
    
    -- Metadata
    relevancia VARCHAR(20) DEFAULT 'NORMAL', -- ALTA, NORMAL, BAJA
    requiere_revision BOOLEAN DEFAULT FALSE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    -- Auditoría
    fecha_accion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índice para búsquedas
    CONSTRAINT chk_relevancia CHECK (relevancia IN ('ALTA', 'NORMAL', 'BAJA'))
);

-- Índices para optimizar consultas de auditoría
CREATE INDEX idx_auditoria_usuario ON auditoria_detallada(usuario_id);
CREATE INDEX idx_auditoria_rol ON auditoria_detallada(rol_nombre);
CREATE INDEX idx_auditoria_modulo ON auditoria_detallada(modulo);
CREATE INDEX idx_auditoria_entidad ON auditoria_detallada(entidad, entidad_id);
CREATE INDEX idx_auditoria_fecha ON auditoria_detallada(fecha_accion DESC);
CREATE INDEX idx_auditoria_relevancia ON auditoria_detallada(relevancia);

-- =====================================================
-- 6. RESPALDOS DEL SISTEMA
-- Tabla para gestionar respaldos automáticos y manuales
-- Implementa el patrón Memento para restauración
-- =====================================================
CREATE TABLE IF NOT EXISTS respaldos_sistema (
    id BIGSERIAL PRIMARY KEY,
    
    -- Información del respaldo
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(20) NOT NULL, -- 'AUTOMATICO' o 'MANUAL'
    
    -- Datos del respaldo
    ruta_archivo VARCHAR(500) NOT NULL, -- Ruta donde se guardó el backup
    tamano_bytes BIGINT, -- Tamaño del archivo
    hash_verificacion VARCHAR(64), -- SHA-256 para verificar integridad
    
    -- Metadata
    fecha_respaldo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP, -- Cuándo se debe eliminar automáticamente
    
    -- Estado
    estado VARCHAR(20) DEFAULT 'EXITOSO', -- EXITOSO, FALLIDO, EN_PROGRESO
    puede_restaurar BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    creado_por VARCHAR(255),
    restaurado_por VARCHAR(255),
    fecha_restauracion TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_tipo_respaldo CHECK (tipo IN ('AUTOMATICO', 'MANUAL')),
    CONSTRAINT chk_estado_respaldo CHECK (estado IN ('EXITOSO', 'FALLIDO', 'EN_PROGRESO'))
);

-- Índices
CREATE INDEX idx_respaldos_tipo ON respaldos_sistema(tipo);
CREATE INDEX idx_respaldos_fecha ON respaldos_sistema(fecha_respaldo DESC);
CREATE INDEX idx_respaldos_estado ON respaldos_sistema(estado);
CREATE INDEX idx_respaldos_puede_restaurar ON respaldos_sistema(puede_restaurar);

-- =====================================================
-- 7. PARÁMETROS DE CONFIGURACIÓN (Mejorar tabla existente)
-- Esta tabla complementa parameter_sistema para configuraciones específicas
-- =====================================================
CREATE TABLE IF NOT EXISTS configuracion_avanzada (
    id BIGSERIAL PRIMARY KEY,
    
    -- Clave-valor
    clave VARCHAR(100) NOT NULL UNIQUE,
    valor TEXT NOT NULL,
    
    -- Metadata
    categoria VARCHAR(50) NOT NULL, -- 'SISTEMA', 'HORARIOS', 'NOTIFICACIONES', etc.
    tipo_dato VARCHAR(20) NOT NULL, -- 'STRING', 'INTEGER', 'BOOLEAN', 'JSON'
    descripcion TEXT,
    
    -- Validación
    valor_por_defecto TEXT,
    requerido BOOLEAN DEFAULT FALSE,
    editable BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    creado_por VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modificado_por VARCHAR(255),
    fecha_modificacion TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_tipo_dato CHECK (tipo_dato IN ('STRING', 'INTEGER', 'BOOLEAN', 'JSON', 'DECIMAL'))
);

-- Índices
CREATE INDEX idx_config_avanzada_categoria ON configuracion_avanzada(categoria);
CREATE INDEX idx_config_avanzada_editable ON configuracion_avanzada(editable);

-- Insertar configuraciones iniciales
INSERT INTO configuracion_avanzada (clave, valor, categoria, tipo_dato, descripcion, valor_por_defecto, requerido, editable, creado_por) VALUES
-- Configuración de respaldos
('respaldos.automaticos.habilitado', 'true', 'RESPALDOS', 'BOOLEAN', 'Habilitar respaldos automáticos diarios', 'true', TRUE, TRUE, 'SYSTEM'),
('respaldos.automaticos.hora', '02:00', 'RESPALDOS', 'STRING', 'Hora de ejecución de respaldos automáticos (HH:MM)', '02:00', TRUE, TRUE, 'SYSTEM'),
('respaldos.retencion.dias', '30', 'RESPALDOS', 'INTEGER', 'Días de retención de respaldos antiguos', '30', TRUE, TRUE, 'SYSTEM'),
('respaldos.ubicacion', '/var/backups/veterinaria', 'RESPALDOS', 'STRING', 'Ubicación de almacenamiento de respaldos', '/var/backups/veterinaria', TRUE, TRUE, 'SYSTEM'),

-- Configuración de auditoría
('auditoria.retener.dias', '90', 'AUDITORIA', 'INTEGER', 'Días de retención del historial de auditoría', '90', TRUE, TRUE, 'SYSTEM'),
('auditoria.nivel.detalle', 'NORMAL', 'AUDITORIA', 'STRING', 'Nivel de detalle de auditoría (MINIMO, NORMAL, COMPLETO)', 'NORMAL', TRUE, TRUE, 'SYSTEM'),

-- Configuración de citas
('citas.duracion.estandar', '30', 'CITAS', 'INTEGER', 'Duración estándar de citas en minutos', '30', TRUE, TRUE, 'SYSTEM'),
('citas.maximo.dia', '12', 'CITAS', 'INTEGER', 'Máximo de citas por día', '12', TRUE, TRUE, 'SYSTEM'),
('citas.anticipacion.horas', '24', 'CITAS', 'INTEGER', 'Horas de anticipación para agendar citas', '24', TRUE, TRUE, 'SYSTEM'),
('citas.cancelacion.horas', '12', 'CITAS', 'INTEGER', 'Horas máximas para cancelar citas', '12', TRUE, TRUE, 'SYSTEM');

-- =====================================================
-- COMENTARIOS FINALES
-- =====================================================
-- Esta migración crea la estructura base para el módulo de configuración
-- siguiendo principios SOLID y patrones de diseño.
-- 
-- Próximos pasos:
-- 1. Implementar entidades JPA correspondientes
-- 2. Crear servicios con patrones Singleton, Command y Memento
-- 3. Desarrollar controllers REST
-- 4. Implementar frontend con consumo dinámico
-- =====================================================
