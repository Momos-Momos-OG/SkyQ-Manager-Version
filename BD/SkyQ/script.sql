IF DB_ID('skyq_db') IS NULL
BEGIN
    CREATE DATABASE skyq_db;
END;
GO

USE skyq_db;
GO

IF OBJECT_ID('dbo.equipaje', 'U') IS NOT NULL DROP TABLE dbo.equipaje;
IF OBJECT_ID('dbo.pasajero', 'U') IS NOT NULL DROP TABLE dbo.pasajero;
IF OBJECT_ID('dbo.aviones', 'U') IS NOT NULL DROP TABLE dbo.aviones;
GO

CREATE TABLE dbo.aviones (
    matricula VARCHAR(20) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    capacidad INT NOT NULL,
    estado VARCHAR(50) NOT NULL,
    CONSTRAINT PK_aviones PRIMARY KEY (matricula)
);
GO

CREATE TABLE dbo.pasajero (
    idPasajero INT IDENTITY(1,1) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    numAsiento VARCHAR(20) NOT NULL,
    nivelPrioridad INT NOT NULL,
    timestampLlegada DATETIME2 NULL,
    CONSTRAINT PK_pasajero PRIMARY KEY (idPasajero)
);
GO

CREATE TABLE dbo.equipaje (
    idMaleta INT IDENTITY(1,1) NOT NULL,
    idPasajero INT NOT NULL,
    peso DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT PK_equipaje PRIMARY KEY (idMaleta),
    CONSTRAINT FK_equipaje_pasajero
        FOREIGN KEY (idPasajero)
        REFERENCES dbo.pasajero (idPasajero)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
GO