-- ============================================================
-- SkyQ - Seed Data v3.0
-- Datos iniciales de prueba
-- ============================================================

USE skyq_db;
GO

-- Usuarios de prueba (contraseñas en texto plano)
-- GERENTE: user="gerente" pass="Gerente@123"
-- OPERARIO: user="operario" pass="Operario@123"
INSERT INTO dbo.usuarios (username, password_hash, rol, estado) VALUES
('gerente', 'Gerente@123', 'GERENTE', 'Activo'),
('operario', 'Operario@123', 'OPERARIO', 'Activo');
GO

-- Flota inicial
INSERT INTO dbo.aviones (matricula, modelo, capacidad, estado) VALUES
('HC-BXA', 'Boeing 737-800',  162, 'Disponible'),
('HC-CJP', 'Airbus A320',     150, 'Disponible'),
('HC-DMK', 'ATR 72-600',       68, 'En mantenimiento'),
('HC-EAQ', 'Embraer E190',     96, 'Disponible');
GO

-- Distribuciones de cabina pre-configuradas (nuevo formato)
-- Formato: "CLASE:DISTRIBUCION:FILAS|..."
-- HC-BXA (162 pax, doble pasillo): VIP 16pax (2-2-2x4 filas), EJEC 32pax (2-4-2x5 filas), ECON 114pax (3-4-3x19 filas)
-- HC-CJP (150 pax, doble pasillo): VIP 15pax (2-2-2x3 filas), EJEC 30pax (2-4-2x5 filas), ECON 105pax (3-4-3x18 filas)
-- HC-DMK (68 pax, un pasillo): VIP 7pax (2-2x2 filas), ECON 61pax (3-3x11 filas)
-- HC-EAQ (96 pax, un pasillo): VIP 10pax (2-2x3 filas), ECON 86pax (3-3x14 filas)
INSERT INTO dbo.configuracion_asientos (matricula, distribucion_clases) VALUES
('HC-BXA', 'VIP:2-2-2:4|EJEC:2-4-2:5|ECON:3-4-3:19'),
('HC-CJP', 'VIP:2-2-2:3|EJEC:2-4-2:5|ECON:3-4-3:18'),
('HC-DMK', 'VIP:2-2:2|ECON:3-3:11'),
('HC-EAQ', 'VIP:2-2:3|ECON:3-3:14');
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

-- Mantenimientos de flota
INSERT INTO dbo.mantenimiento (matricula, fechaInicio, fechaFin, descripcion, estado) VALUES
('HC-BXA', '2026-06-01', NULL, 'Revisión de motores y sistemas de combustible', 'Programado'),
('HC-CJP', '2026-06-08', '2026-06-12', 'Cambio de aceite y filtros', 'Completado'),
('HC-DMK', '2026-06-15', NULL, 'Inspección estructural y reparación de fuselaje', 'En Curso'),
('HC-BXA', '2026-05-20', '2026-05-25', 'Inspección de presurización', 'Completado');
GO
