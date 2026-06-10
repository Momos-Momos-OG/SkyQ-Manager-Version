-- ============================================================
-- SkyQ - Script de Base de Datos v2.0
-- Compatible con las 3 aplicaciones: Manager, Usuario, Piloto
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
IF OBJECT_ID('dbo.vuelos',               'U') IS NOT NULL DROP TABLE dbo.vuelos;
IF OBJECT_ID('dbo.configuracion_asientos','U') IS NOT NULL DROP TABLE dbo.configuracion_asientos;
IF OBJECT_ID('dbo.equipaje',             'U') IS NOT NULL DROP TABLE dbo.equipaje;
IF OBJECT_ID('dbo.pasajero',             'U') IS NOT NULL DROP TABLE dbo.pasajero;
IF OBJECT_ID('dbo.pilotos',              'U') IS NOT NULL DROP TABLE dbo.pilotos;
IF OBJECT_ID('dbo.aviones',             'U') IS NOT NULL DROP TABLE dbo.aviones;
GO
 
-- ============================================================
-- TABLA 1: aviones
-- Usada por: App Manager (CRUD), App Usuario (ver avión del vuelo),
--            App Piloto (ver avión asignado)
-- ============================================================
CREATE TABLE dbo.aviones (
    matricula VARCHAR(20)  NOT NULL,
    modelo    VARCHAR(100) NOT NULL,
    capacidad INT          NOT NULL,
    estado    VARCHAR(50)  NOT NULL,   -- 'Disponible' | 'En Vuelo' | 'En mantenimiento' | 'Fuera de servicio'
    CONSTRAINT PK_aviones PRIMARY KEY (matricula)
);
GO
 
-- ============================================================
-- TABLA 2: pilotos
-- Usada por: App Manager (CRUD + asignación), App Piloto (login/perfil)
-- ============================================================
CREATE TABLE dbo.pilotos (
    idPiloto INT          IDENTITY(1,1) NOT NULL,
    nombre   VARCHAR(100) NOT NULL,
    rango    VARCHAR(50)  NOT NULL,    -- 'Comandante' | 'Co-Piloto' | 'Piloto Instrucción'
    estado   VARCHAR(30)  NOT NULL,    -- 'Disponible' | 'En Vuelo' | 'Licencia' | 'Descanso'
    CONSTRAINT PK_pilotos PRIMARY KEY (idPiloto)
);
GO
 
-- ============================================================
-- TABLA 3: pasajero
-- Usada por: App Manager (check-in), App Usuario (ver su reserva)
-- ============================================================
CREATE TABLE dbo.pasajero (
    idPasajero       INT           IDENTITY(1,1) NOT NULL,
    nombre           VARCHAR(100)  NOT NULL,
    numAsiento       VARCHAR(20)   NOT NULL,
    nivelPrioridad   INT           NOT NULL,      -- 1=VIP, 2=Normal, 3=Básico
    timestampLlegada DATETIME2     NULL,
    matricula        VARCHAR(20)   NOT NULL DEFAULT 'HC-BXA',  -- FK lógica hacia aviones
    CONSTRAINT PK_pasajero PRIMARY KEY (idPasajero)
);
GO
 
-- ============================================================
-- TABLA 4: equipaje
-- Usada por: App Manager (registro en check-in)
-- ============================================================
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
GO
 
-- ============================================================
-- TABLA 5: configuracion_asientos
-- Usada por: App Manager (diseñador de cabina), App Usuario (mapa de asientos)
-- ============================================================
CREATE TABLE dbo.configuracion_asientos (
    matricula VARCHAR(20)  NOT NULL,
    filas     INT          NOT NULL,
    columnas  INT          NOT NULL,
    pasillos  VARCHAR(100) NOT NULL,   -- Índices de columnas pasillo separados por coma (ej: "3,7")
    CONSTRAINT PK_configuracion_asientos PRIMARY KEY (matricula),
    CONSTRAINT FK_configuracion_aviones
        FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
        ON UPDATE CASCADE ON DELETE CASCADE
);
GO
 
-- ============================================================
-- TABLA 6: vuelos  *** NUEVA ***
-- Tabla central compartida por las 3 aplicaciones.
-- Registra qué piloto opera qué avión y en qué periodo.
-- Al estar 'En Vuelo', bloquea al piloto y al avión automáticamente
-- a nivel de negocio (verificado en código antes de asignar).
-- ============================================================
CREATE TABLE dbo.vuelos (
    idVuelo      INT          IDENTITY(1,1) NOT NULL,
    matricula    VARCHAR(20)  NOT NULL,    -- FK → aviones
    idPiloto     INT          NOT NULL,    -- FK → pilotos
    fechaSalida  DATETIME2    NOT NULL,
    fechaRegreso DATETIME2    NOT NULL,
    estado       VARCHAR(30)  NOT NULL DEFAULT 'Programado',
                                          -- 'Programado' | 'En Vuelo' | 'Completado' | 'Cancelado'
    CONSTRAINT PK_vuelos PRIMARY KEY (idVuelo),
    CONSTRAINT FK_vuelos_avion
        FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_vuelos_piloto
        FOREIGN KEY (idPiloto) REFERENCES dbo.pilotos(idPiloto)
        ON UPDATE CASCADE ON DELETE CASCADE
);
GO
 
-- ============================================================
-- TABLA 7: hospedaje_piloto  *** NUEVA ***
-- Usada por: App Manager (asignar hospedaje), App Piloto (ver su hospedaje)
-- Versión básica demostrativa: hotel, ciudad, fechas de estadía.
-- ============================================================
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
GO
 
-- ============================================================
-- SEED DATA — Datos iniciales de prueba
-- ============================================================
 
-- Flota inicial
INSERT INTO dbo.aviones (matricula, modelo, capacidad, estado) VALUES
('HC-BXA', 'Boeing 737-800',  162, 'Disponible'),
('HC-CJP', 'Airbus A320',     150, 'Disponible'),
('HC-DMK', 'ATR 72-600',       68, 'En mantenimiento'),
('HC-EAQ', 'Embraer E190',     96, 'Disponible');
GO
 
-- Distribuciones de cabina pre-configuradas
INSERT INTO dbo.configuracion_asientos (matricula, filas, columnas, pasillos) VALUES
('HC-BXA', 14, 7, '3'),
('HC-CJP', 12, 7, '3');
GO
 
-- Pilotos base de la aerolínea
INSERT INTO dbo.pilotos (nombre, rango, estado) VALUES
('Cap. Carlos Mendoza', 'Comandante', 'Disponible'),
('Cap. Ana Guevara',    'Comandante', 'Disponible'),
('F.O. Luis Rojas',     'Co-Piloto',  'En Vuelo');
GO
 
-- Vuelo de ejemplo: Luis Rojas opera HC-DMK
INSERT INTO dbo.vuelos (matricula, idPiloto, fechaSalida, fechaRegreso, estado) VALUES
('HC-DMK', 3, '2026-06-15 08:00:00', '2026-06-15 11:30:00', 'En Vuelo');
GO
 
-- Hospedaje de ejemplo para Luis Rojas
INSERT INTO dbo.hospedaje_piloto (idPiloto, hotel, ciudad, fechaIngreso, fechaSalida) VALUES
(3, 'Hotel Marriott Quito', 'Quito', '2026-06-14 14:00:00', '2026-06-16 12:00:00');
GO