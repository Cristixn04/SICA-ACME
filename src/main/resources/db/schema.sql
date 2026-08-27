-- ============================================================
-- SICA - Sistema Integrado de Control de Acceso (Zona ACME)
-- Esquema de base de datos (PostgreSQL)
-- Modelo normalizado (3FN)
-- ============================================================

-- Los tipos enum permiten restringir los valores de EstadoVisita y
-- EstadoPersona en la base de datos (integridad a nivel de BD).

CREATE TYPE estado_visita AS ENUM (
    'PENDIENTE_APROBACION',
    'PENDIENTE_APROBACION_POR_OLVIDO',
    'APROBADO',
    'RECHAZADO',
    'CHECK_IN',
    'DENTRO',
    'CHECK_OUT',
    'CERRADA_POR_SISTEMA'
);

CREATE TYPE estado_persona AS ENUM ('ACTIVO', 'LICENCIA', 'INACTIVO');

CREATE TYPE severidad_incidente AS ENUM ('BAJO', 'MEDIO', 'ALTO');

CREATE TYPE estado_incidente AS ENUM ('ABIERTO', 'EN_PROGRESO', 'CERRADO');

-- ------------------------------------------------------------
-- Tabla: empresa
-- ------------------------------------------------------------
CREATE TABLE empresa (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL UNIQUE,
    es_acme   BOOLEAN NOT NULL DEFAULT FALSE
);

-- ------------------------------------------------------------
-- Tabla: persona
-- ------------------------------------------------------------
CREATE TABLE persona (
    id                 SERIAL PRIMARY KEY,
    dni                VARCHAR(8) NOT NULL UNIQUE,
    nombre_completo    VARCHAR(120) NOT NULL,
    puesto             VARCHAR(80),
    departamento       VARCHAR(80),
    email_corporativo  VARCHAR(120),
    foto_url           VARCHAR(255),
    estado             estado_persona NOT NULL DEFAULT 'ACTIVO',
    empresa_id         INTEGER REFERENCES empresa (id)
);

CREATE INDEX idx_persona_nombre ON persona (nombre_completo);
CREATE INDEX idx_persona_empresa ON persona (empresa_id);

-- ------------------------------------------------------------
-- Tabla: rol
-- ------------------------------------------------------------
CREATE TABLE rol (
    id       SERIAL PRIMARY KEY,
    nombre   VARCHAR(50) NOT NULL UNIQUE
);

-- ------------------------------------------------------------
-- Tabla: permiso
-- ------------------------------------------------------------
CREATE TABLE permiso (
    id           SERIAL PRIMARY KEY,
    codigo       VARCHAR(60) NOT NULL UNIQUE,
    modulo       VARCHAR(30) NOT NULL,
    descripcion  VARCHAR(200)
);

-- ------------------------------------------------------------
-- Tabla puente: rol_permiso
-- ------------------------------------------------------------
CREATE TABLE rol_permiso (
    rol_id     INTEGER NOT NULL REFERENCES rol (id),
    permiso_id INTEGER NOT NULL REFERENCES permiso (id),
    PRIMARY KEY (rol_id, permiso_id)
);

-- ------------------------------------------------------------
-- Tabla: usuario (RBAC)
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id               SERIAL PRIMARY KEY,
    nombre_usuario   VARCHAR(50) NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    rol_id           INTEGER NOT NULL REFERENCES rol (id),
    persona_id       INTEGER REFERENCES persona (id),
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Tabla: visita (Control de Acceso)
-- ------------------------------------------------------------
CREATE TABLE visita (
    id                     SERIAL PRIMARY KEY,
    persona_id             INTEGER NOT NULL REFERENCES persona (id),
    empresa_propietaria_id INTEGER REFERENCES empresa (id),
    persona_visitada_id    INTEGER REFERENCES persona (id),
    motivo                 VARCHAR(200),
    fecha_hora_visita      TIMESTAMP,
    estado                 estado_visita NOT NULL,
    fecha_hora_checkin     TIMESTAMP,
    fecha_hora_checkout    TIMESTAMP,
    guarda_id              INTEGER REFERENCES usuario (id),
    motivo_cierre          VARCHAR(200),
    creada_en              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_visita_persona ON visita (persona_id);
CREATE INDEX idx_visita_estado ON visita (estado);

-- ------------------------------------------------------------
-- Tabla: incidente
-- ------------------------------------------------------------
CREATE TABLE incidente (
    id              SERIAL PRIMARY KEY,
    persona_id      INTEGER REFERENCES persona (id),
    tipo            VARCHAR(80) NOT NULL,
    descripcion     VARCHAR(500),
    severidad       severidad_incidente NOT NULL DEFAULT 'MEDIO',
    estado          estado_incidente NOT NULL DEFAULT 'ABIERTO',
    fecha_registro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_incidente_estado ON incidente (estado);
CREATE INDEX idx_incidente_severidad ON incidente (severidad);

-- ------------------------------------------------------------
-- Tabla: bitacora_auditoria (inmutable: solo INSERT)
-- ------------------------------------------------------------
CREATE TABLE bitacora_auditoria (
    id          SERIAL PRIMARY KEY,
    fecha_hora  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario     VARCHAR(50),
    accion      VARCHAR(80) NOT NULL,
    entidad     VARCHAR(80),
    entidad_id  BIGINT,
    detalles    TEXT
);
