-- ============================================================
-- SkyQ - Drop Tables Script v3.0
-- Elimina todas las tablas en orden inverso (FK primero)
-- ============================================================

USE skyq_db;
GO

-- Tablas con FK: eliminar primero
IF OBJECT_ID('dbo.equipaje', 'U') IS NOT NULL DROP TABLE dbo.equipaje;
IF OBJECT_ID('dbo.mantenimiento', 'U') IS NOT NULL DROP TABLE dbo.mantenimiento;
IF OBJECT_ID('dbo.pasajero', 'U') IS NOT NULL DROP TABLE dbo.pasajero;
IF OBJECT_ID('dbo.hospedaje_piloto', 'U') IS NOT NULL DROP TABLE dbo.hospedaje_piloto;
IF OBJECT_ID('dbo.configuracion_asientos', 'U') IS NOT NULL DROP TABLE dbo.configuracion_asientos;
IF OBJECT_ID('dbo.vuelos', 'U') IS NOT NULL DROP TABLE dbo.vuelos;
GO

-- Tablas base: al final
IF OBJECT_ID('dbo.aviones', 'U') IS NOT NULL DROP TABLE dbo.aviones;
IF OBJECT_ID('dbo.pilotos', 'U') IS NOT NULL DROP TABLE dbo.pilotos;
IF OBJECT_ID('dbo.usuarios', 'U') IS NOT NULL DROP TABLE dbo.usuarios;
IF OBJECT_ID('dbo.auditoria', 'U') IS NOT NULL DROP TABLE dbo.auditoria;
GO

PRINT 'Todas las tablas han sido eliminadas correctamente.';
GO
