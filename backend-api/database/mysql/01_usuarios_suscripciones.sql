-- =====================================================================
-- BuildZone API - Script 01: modulo de usuarios, planes y suscripciones
-- Motor: MariaDB 10.4 (XAMPP) / MySQL 8+
--
-- Se ejecuta UNA SOLA VEZ sobre la base de datos "buildzone" existente
-- (phpMyAdmin > buildzone > SQL, o: mysql -u root buildzone < 01_...sql).
--
-- NO modifica ni borra ninguna de las 10 tablas previas (producto, marca,
-- categoria, usuario, ...). Crea tablas nuevas con nombres propios; la
-- tabla de cuentas se llama "usuario_cuenta" precisamente para no chocar
-- con la tabla "usuario" que ya existe.
--
-- El usuario ADMIN inicial y los planes Gratuito/Premium los crea la API
-- automaticamente al arrancar (DatosInicialesConfig), con la contrasena
-- ya cifrada en BCrypt; por eso no se insertan aqui.
-- =====================================================================

USE buildzone;

CREATE TABLE IF NOT EXISTS usuario_cuenta (
    id_usuario      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre          VARCHAR(60)  NOT NULL,
    apellido        VARCHAR(60)  NOT NULL,
    username        VARCHAR(40)  NOT NULL,
    email           VARCHAR(120) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL COMMENT 'Hash BCrypt, nunca texto plano',
    rol             VARCHAR(20)  NOT NULL DEFAULT 'USUARIO' COMMENT 'USUARIO | ADMIN',
    estado          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'  COMMENT 'ACTIVO | INACTIVO',
    fecha_registro  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_cuenta_username UNIQUE (username),
    CONSTRAINT uk_usuario_cuenta_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plan_suscripcion (
    id_plan        BIGINT        NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(100)  NOT NULL,
    descripcion    VARCHAR(500),
    precio         DECIMAL(12,2) NOT NULL COMMENT 'Pesos colombianos (COP)',
    duracion_dias  INT           NOT NULL,
    activo         BIT(1)        NOT NULL DEFAULT b'1',
    PRIMARY KEY (id_plan),
    CONSTRAINT uk_plan_suscripcion_nombre UNIQUE (nombre),
    CONSTRAINT ck_plan_precio CHECK (precio >= 0),
    CONSTRAINT ck_plan_duracion CHECK (duracion_dias > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS suscripcion (
    id_suscripcion  BIGINT      NOT NULL AUTO_INCREMENT,
    id_usuario      BIGINT      NOT NULL,
    id_plan         BIGINT      NOT NULL,
    fecha_inicio    DATE        NOT NULL,
    fecha_fin       DATE        NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVA' COMMENT 'ACTIVA | CANCELADA | VENCIDA',
    PRIMARY KEY (id_suscripcion),
    KEY idx_suscripcion_usuario_estado (id_usuario, estado),
    CONSTRAINT fk_suscripcion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario_cuenta (id_usuario),
    CONSTRAINT fk_suscripcion_plan FOREIGN KEY (id_plan) REFERENCES plan_suscripcion (id_plan)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
