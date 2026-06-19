-- ============================================================
-- SkyQ - Schema de Base de Datos v3.0
-- Creación de tablas (sin datos iniciales)
-- SQL Server 2025 en Docker (puerto 1433)
-- ============================================================

-- 1. Creación e inicialización segura de la Base de Datos
IF DB_ID('skyq_db') IS NULL
BEGIN
    CREATE DATABASE skyq_db;
END;
GO

USE skyq_db;
GO

-- 2. Eliminación ordenada respetando dependencias (baja en cascada correcta)
IF OBJECT_ID('dbo.hospedaje_piloto',     'U') IS NOT NULL DROP TABLE dbo.hospedaje_piloto;
IF OBJECT_ID('dbo.mantenimiento',        'U') IS NOT NULL DROP TABLE dbo.mantenimiento;
IF OBJECT_ID('dbo.vuelos',               'U') IS NOT NULL DROP TABLE dbo.vuelos;
IF OBJECT_ID('dbo.configuracion_asientos','U') IS NOT NULL DROP TABLE dbo.configuracion_asientos;
IF OBJECT_ID('dbo.equipaje',             'U') IS NOT NULL DROP TABLE dbo.equipaje;
IF OBJECT_ID('dbo.pasajero',             'U') IS NOT NULL DROP TABLE dbo.pasajero;
IF OBJECT_ID('dbo.pilotos',              'U') IS NOT NULL DROP TABLE dbo.pilotos;
IF OBJECT_ID('dbo.auditoria',            'U') IS NOT NULL DROP TABLE dbo.auditoria;
IF OBJECT_ID('dbo.usuarios',             'U') IS NOT NULL DROP TABLE dbo.usuarios;
IF OBJECT_ID('dbo.aviones',             'U') IS NOT NULL DROP TABLE dbo.aviones;
GO

-- ============================================================
-- TABLA -1: auditoria
-- Log centralizado de todas las acciones del sistema
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'auditoria')
BEGIN
    CREATE TABLE dbo.auditoria (
        idAuditoria INT           IDENTITY(1,1) NOT NULL,
        username    VARCHAR(50)   NOT NULL,
        accion      VARCHAR(50)   NOT NULL,
        detalle     VARCHAR(255)  NULL,
        fecha_hora  DATETIME2     DEFAULT GETDATE(),
        CONSTRAINT PK_auditoria PRIMARY KEY (idAuditoria)
    );
END;
GO

-- ============================================================
-- TABLA 0: usuarios
-- Usada por: App Manager (autenticación), App Usuario, App Piloto
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'usuarios')
BEGIN
    CREATE TABLE dbo.usuarios (
        idUsuario       INT           IDENTITY(1,1) NOT NULL,
        username        VARCHAR(50)   NOT NULL UNIQUE,
        password_hash   VARCHAR(255)  NOT NULL,
        rol             VARCHAR(30)   NOT NULL,    -- 'GERENTE' | 'OPERARIO'
        estado          VARCHAR(20)   NOT NULL,    -- 'Activo' | 'Inactivo'
        fechaCreacion   DATETIME2     DEFAULT GETDATE(),
        CONSTRAINT PK_usuarios PRIMARY KEY (idUsuario)
    );
END;
GO

-- ============================================================
-- TABLA 1: aviones
-- Usada por: App Manager (CRUD), App Usuario (ver avión del vuelo),
--            App Piloto (ver avión asignado)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'aviones')
BEGIN
    CREATE TABLE dbo.aviones (
        matricula VARCHAR(20)  NOT NULL,
        modelo    VARCHAR(100) NOT NULL,
        capacidad INT          NOT NULL,
        estado    VARCHAR(50)  NOT NULL,   -- 'EN_TERMINAL' | 'EN_VUELO' | 'EN_MANTENIMIENTO'
        CONSTRAINT PK_aviones PRIMARY KEY (matricula)
    );
END;
GO

-- ============================================================
-- TABLA 2: pilotos
-- Usada por: App Manager (CRUD + asignación), App Piloto (login/perfil)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'pilotos')
BEGIN
    CREATE TABLE dbo.pilotos (
        idPiloto INT          IDENTITY(1,1) NOT NULL,
        nombre   VARCHAR(100) NOT NULL,
        rango    VARCHAR(50)  NOT NULL,    -- 'Comandante' | 'Co-Piloto' | 'Piloto Instrucción'
        estado   VARCHAR(30)  NOT NULL,    -- 'Disponible' | 'En Vuelo' | 'Licencia' | 'Descanso'
        CONSTRAINT PK_pilotos PRIMARY KEY (idPiloto)
    );
END;
GO

-- ============================================================
-- TABLA 3: pasajero
-- Usada por: App Manager (check-in), App Usuario (ver su reserva)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'pasajero')
BEGIN
    CREATE TABLE dbo.pasajero (
        idPasajero       INT           IDENTITY(1,1) NOT NULL,
        nombre           VARCHAR(100)  NOT NULL,
        numAsiento       VARCHAR(20)   NOT NULL,
        nivelPrioridad   INT           NOT NULL,      -- 1=VIP, 2=Normal, 3=Básico
        timestampLlegada DATETIME2     NULL,
        matricula        VARCHAR(20)   NOT NULL DEFAULT 'HC-BXA',  -- FK lógica hacia aviones
        pnr              VARCHAR(20)   NULL,
        sillaRuedas      BIT           NOT NULL DEFAULT 0,
        upgrade          BIT           NOT NULL DEFAULT 0,
        CONSTRAINT PK_pasajero PRIMARY KEY (idPasajero)
    );
END;
GO

-- ============================================================
-- TABLA 4: equipaje
-- Usada por: App Manager (registro en check-in)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'equipaje')
BEGIN
    CREATE TABLE dbo.equipaje (
        idMaleta   INT            IDENTITY(1,1) NOT NULL,
        idPasajero INT            NOT NULL,
        peso       DECIMAL(10,2)  NOT NULL,
        estado     VARCHAR(20)    NOT NULL,    -- 'Registrado' | 'Embarcado' | 'Entregado'
        CONSTRAINT PK_equipaje PRIMARY KEY (idMaleta),
        CONSTRAINT FK_equipaje_pasajero
            FOREIGN KEY (idPasajero) REFERENCES dbo.pasajero(idPasajero)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 5: configuracion_asientos (Reestructurada)
-- Usada por: App Manager (diseñador de cabina), App Usuario (mapa de asientos)
-- Nueva columna: distribucion_clases (formato: "CLASE:DIST:FILAS|...")
-- Ejemplo: "VIP:2-2:4|EJEC:2-4-2:5|ECON:3-4-3:20"
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'configuracion_asientos')
BEGIN
    CREATE TABLE dbo.configuracion_asientos (
        matricula            VARCHAR(20)  NOT NULL,
        distribucion_clases  VARCHAR(255) NOT NULL,   -- Formato: "VIP:2-2:4|EJEC:3-3:5|ECON:3-3:20"
        CONSTRAINT PK_configuracion_asientos PRIMARY KEY (matricula),
        CONSTRAINT FK_configuracion_aviones
            FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 6.5: mantenimiento
-- Historial de mantenimientos de la flota
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'mantenimiento')
BEGIN
    CREATE TABLE dbo.mantenimiento (
        idMantenimiento INT           IDENTITY(1,1) NOT NULL,
        matricula       VARCHAR(20)   NOT NULL,    -- FK → aviones
        fechaInicio     DATE          NOT NULL,
        fechaFin        DATE          NULL,
        descripcion     VARCHAR(255)  NOT NULL,
        estado          VARCHAR(30)   NOT NULL,    -- 'Programado' | 'En Curso' | 'Completado'
        CONSTRAINT PK_mantenimiento PRIMARY KEY (idMantenimiento),
        CONSTRAINT FK_mantenimiento_avion
            FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 7: vuelos
-- Tabla central compartida por las 3 aplicaciones.
-- Registra qué piloto opera qué avión y en qué periodo.
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'vuelos')
BEGIN
    CREATE TABLE dbo.vuelos (
        idVuelo      INT          IDENTITY(1,1) NOT NULL,
        matricula    VARCHAR(20)  NOT NULL,    -- FK → aviones
        idPiloto     INT          NOT NULL,    -- FK → pilotos
        fechaSalida  DATETIME2    NOT NULL,
        fechaRegreso DATETIME2    NOT NULL,
        estado       VARCHAR(30)  NOT NULL DEFAULT 'Programado',
        origen       VARCHAR(100) NULL,
        destino      VARCHAR(100) NULL,
                                              -- 'Programado' | 'En Vuelo' | 'Completado' | 'Cancelado'
        CONSTRAINT PK_vuelos PRIMARY KEY (idVuelo),
        CONSTRAINT FK_vuelos_avion
            FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
            ON UPDATE CASCADE ON DELETE CASCADE,
        CONSTRAINT FK_vuelos_piloto
            FOREIGN KEY (idPiloto) REFERENCES dbo.pilotos(idPiloto)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 8: hospedaje_piloto
-- Usada por: App Manager (asignar hospedaje), App Piloto (ver su hospedaje)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'hospedaje_piloto')
BEGIN
    CREATE TABLE dbo.hospedaje_piloto (
        idHospedaje  INT          IDENTITY(1,1) NOT NULL,
        idPiloto     INT          NOT NULL,    -- FK → pilotos
        hotel        VARCHAR(150) NOT NULL,
        ciudad       VARCHAR(100) NOT NULL,
        fechaIngreso DATETIME2    NOT NULL,
        fechaSalida  DATETIME2    NOT NULL,
        CONSTRAINT PK_hospedaje_piloto PRIMARY KEY (idHospedaje),
        CONSTRAINT FK_hospedaje_piloto
            FOREIGN KEY (idPiloto) REFERENCES dbo.pilotos(idPiloto)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO
