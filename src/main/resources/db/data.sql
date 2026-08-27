-- ============================================================
-- SICA - Sistema Integrado de Control de Acceso (Zona ACME)
-- Datos de poblado / seed
--
-- Credenciales de ejemplo (en desarrollo):
--   admin     / 1234   -> Administrador
--   guarda    / 1234   -> Guarda de Seguridad
--   funcionario / 1234 -> Funcionario de Empresa
-- ============================================================

-- ------------------------------------------------------------
-- Empresas
-- ------------------------------------------------------------
INSERT INTO empresa (id, nombre, es_acme) VALUES
    (1, 'ACME (Complejo)', TRUE),
    (2, 'Tech Solutions Corp.', FALSE),
    (3, 'Marketing Nova', FALSE);

SELECT setval('empresa_id_seq', 3);

-- ------------------------------------------------------------
-- Personas (empleados y visitantes de ejemplo)
-- ------------------------------------------------------------
INSERT INTO persona (id, dni, nombre_completo, puesto, departamento, email_corporativo, estado, empresa_id) VALUES
    (1,  '30000001', 'Javier Rodríguez',   'Coordinador de Seguridad', 'Seguridad', 'javier.rodriguez@acme.com',  'ACTIVO',   1),
    (2,  '30000002', 'María González',     'Ingeniera de Software',    'TI',        'maria.gonzalez@techsolutions.com', 'ACTIVO', 2),
    (3,  '30000003', 'Carlos Ruiz',        'Gerente de Marketing',     'Marketing', 'carlos.ruiz@marketingnova.com',  'ACTIVO',  3),
    (4,  '30000004', 'Ana Silvia',         'Analista de Datos',        'TI',        'ana.silvia@techsolutions.com',  'LICENCIA', 2),
    (5,  '30000005', 'Juan Pérez Morales', 'Recursos Humanos',         'RRHH',      'juan.perez@marketingnova.com',  'ACTIVO',  3),
    (6,  '30000006', 'Sonia Diaz López',   'Contadora',                'Finanzas',  'sonia.diaz@techsolutions.com',  'ACTIVO',  2),
    (7,  '30123456', 'Martín Gómez Martínez', 'Consultor externo',     NULL,        NULL,                            'ACTIVO',   NULL),
    (8,  '30123457', 'Elena Ruiz',         'Directora de Proyectos',   'TI',        'elena.ruiz@techsolutions.com',  'ACTIVO',  2),
    (9,  '30123458', 'Pedro Martínez',     'Analista de Seguridad',    'Seguridad', 'pedro.martinez@acme.com',       'ACTIVO',   1),
    (10, '30123459', 'Eunita García',      'Recepcionista',            'Operaciones', 'eunita.garcia@acme.com',       'ACTIVO',   1);

SELECT setval('persona_id_seq', 10);

-- ------------------------------------------------------------
-- Roles
-- ------------------------------------------------------------
INSERT INTO rol (id, nombre) VALUES
    (1, 'ADMINISTRADOR'),
    (2, 'GUARDA'),
    (3, 'FUNCIONARIO');

SELECT setval('rol_id_seq', 3);

-- ------------------------------------------------------------
-- Permisos
-- ------------------------------------------------------------
INSERT INTO permiso (id, codigo, modulo, descripcion) VALUES
    -- Modulo Usuarios
    (1,  'crear_usuario',       'Usuarios', 'Crear nuevos usuarios del sistema'),
    (2,  'modificar_permisos',  'Usuarios', 'Asignar y modificar roles y permisos'),
    (3,  'bloquear_usuario',    'Usuarios', 'Bloquear el acceso de un usuario'),
    -- Modulo Personas
    (4,  'registrar_persona',   'Personas', 'Registrar nuevas personas'),
    (5,  'editar_persona',      'Personas', 'Editar datos de una persona'),
    (6,  'eliminar_persona',    'Personas', 'Eliminar una persona del sistema'),
    (7,  'bloquear_persona',    'Personas', 'Bloquear el acceso de una persona'),
    -- Modulo Visitas
    (8,  'crear_visita',        'Visitas',  'Crear / pre-registrar una visita'),
    (9,  'aprobar_visita',      'Visitas',  'Aprobar una visita'),
    (10, 'rechazar_visita',     'Visitas',  'Rechazar una visita'),
    (11, 'registrar_checkin',   'Visitas',  'Realizar el check-in de una visita'),
    (12, 'registrar_checkout',  'Visitas',  'Realizar el check-out de una visita'),
    -- Modulo Incidentes
    (13, 'reportar_incidente',  'Incidentes','Reportar un incidente de seguridad'),
    (14, 'gestionar_incidente', 'Incidentes','Gestionar estados de incidentes'),
    -- Modulo Reportes
    (15, 'generar_reporte_mensual', 'Reportes', 'Generar reportes mensuales'),
    (16, 'exportar_datos',      'Reportes', 'Exportar datos (Excel, PDF)');

SELECT setval('permiso_id_seq', 16);

-- ------------------------------------------------------------
-- Matriz rol - permiso
-- ------------------------------------------------------------
-- ADMINISTRADOR: todos los permisos (1..16)
INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT 1, id FROM permiso;

-- GUARDA: registrar_persona, bloquear_persona, crear_visita,
--         registrar_checkin, registrar_checkout, reportar_incidente
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
    (2, 4), (2, 7), (2, 8), (2, 11), (2, 12), (2, 13);

-- FUNCIONARIO: crear_visita, aprobar_visita, rechazar_visita, reportar_incidente
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
    (3, 8), (3, 9), (3, 10), (3, 13);

-- ------------------------------------------------------------
-- Usuarios (contraseña en texto de ejemplo: 1234)
-- ------------------------------------------------------------
INSERT INTO usuario (id, nombre_usuario, password_hash, rol_id, persona_id, activo) VALUES
    (1, 'admin',        '$2a$10$c6GZARr01JIHNGmIVv7xROt3vRcjEI3WSmoCusso6mPunif/YCnnq', 1, 1,  TRUE),
    (2, 'guarda',       '$2a$10$c6GZARr01JIHNGmIVv7xROt3vRcjEI3WSmoCusso6mPunif/YCnnq', 2, 9,  TRUE),
    (3, 'funcionario',  '$2a$10$c6GZARr01JIHNGmIVv7xROt3vRcjEI3WSmoCusso6mPunif/YCnnq', 3, 2,  TRUE);

SELECT setval('usuario_id_seq', 3);

-- ------------------------------------------------------------
-- Visitas de ejemplo
-- ------------------------------------------------------------
-- Visita pre-registrada APROBADA (Martín Gómez visita a Elena Ruiz)
INSERT INTO visita (persona_id, persona_visitada_id, motivo, fecha_hora_visita, estado) VALUES
    (7, 8, 'Reunion de Proyecto', CURRENT_TIMESTAMP + INTERVAL '1 hour', 'APROBADO');

-- Persona DENTRO en el complejo (Juan Pérez Morales)
INSERT INTO visita (persona_id, persona_visitada_id, motivo, fecha_hora_checkin, estado, guarda_id) VALUES
    (5, 3, 'Coordinacion de campaña', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'DENTRO', 2);

-- ------------------------------------------------------------
-- Incidentes de ejemplo
-- ------------------------------------------------------------
INSERT INTO incidente (persona_id, tipo, descripcion, severidad, estado, fecha_registro) VALUES
    (5,  'Acceso Fallido',        'Intento de acceso con credenciales invalidas', 'ALTO',   'ABIERTO',   CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (2,  'Dispositivo Desconectado', 'Cerradura de la puerta principal sin comunicacion', 'MEDIO', 'EN_PROGRESO', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (7,  'Alarma de Puerta',      'Alarma activada al abrirse la puerta de acceso', 'BAJO',  'CERRADO',   CURRENT_TIMESTAMP - INTERVAL '5 days');
