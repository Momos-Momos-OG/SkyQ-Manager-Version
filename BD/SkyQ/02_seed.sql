-- ============================================================
-- SkyQ - Seed Data v3.1
-- Datos iniciales de prueba para el nuevo esquema
-- ============================================================

USE skyq_db;
GO

-- 1. Usuarios de prueba (contraseñas en texto plano)
-- GERENTE: user="gerente" pass="Gerente@123"
-- OPERARIO: user="operario" pass="Operario@123"
INSERT INTO dbo.usuarios (username, password_hash, rol, estado) VALUES
('gerente', 'Gerente@123', 'GERENTE', 'Activo'),
('operario', 'Operario@123', 'OPERARIO', 'Activo');
GO

-- 2. Flota inicial (2 disponibles, 1 en mantenimiento, 1 en vuelo)
INSERT INTO dbo.aviones (matricula, modelo, capacidad, estado) VALUES
('HC-BXA', 'Boeing 737-800',  136, 'EN_TERMINAL'),
('HC-CJP', 'Airbus A320',     152, 'EN_TERMINAL'),
('HC-DMK', 'ATR 72-600',       68, 'EN_MANTENIMIENTO'),
('HC-EAQ', 'Embraer E190',     96, 'EN_VUELO');
GO

-- 3. Distribuciones de cabina pre-configuradas (formato exacto)
INSERT INTO dbo.configuracion_asientos (matricula, distribucion_clases) VALUES
('HC-BXA', 'VIP:2-2:4|ECON:3-3:20'),
('HC-CJP', 'VIP:2-2:5|ECON:3-3:22'),
('HC-DMK', 'VIP:2-2:2|ECON:3-3:10'),
('HC-EAQ', 'VIP:2-2:3|ECON:3-3:14');
GO

-- 4. Pilotos de la aerolínea (2 disponibles, 1 en vuelo)
INSERT INTO dbo.pilotos (nombre, rango, estado) VALUES
('Cap. Carlos Mendoza', 'Comandante', 'Disponible'),
('Cap. Ana Guevara',    'Comandante', 'Disponible'),
('F.O. Luis Rojas',     'Co-Piloto',  'En Vuelo');
GO

-- 5. Mantenimientos de flota activos
INSERT INTO dbo.mantenimiento (matricula, fechaInicio, fechaFin, descripcion, estado) VALUES
('HC-DMK', '2026-06-15', NULL, 'Inspección estructural activa de motores y fuselaje', 'En Curso');
GO

-- 6. Vuelos programados y activos
-- Vuelo 1: Quito - Miami (HC-EAQ operado por Luis Rojas) en estado 'En Vuelo'
-- Vuelo 2: Guayaquil - Madrid (HC-BXA operado por Ana Guevara) en estado 'Programado'
INSERT INTO dbo.vuelos (matricula, idPiloto, fechaSalida, fechaRegreso, estado, origen, destino) VALUES
('HC-EAQ', 3, '2026-06-18 10:00:00', '2026-06-18 14:00:00', 'En Vuelo', 'Quito', 'Aeropuerto Local'),
('HC-BXA', 2, '2026-06-19 18:00:00', '2026-06-20 08:00:00', 'Programado', 'Aeropuerto Local', 'Madrid');
GO

-- 7. Grupo de pasajeros con el mismo PNR (SQ-77X9), diferentes tipos y asientos ya asignados
-- Mapeo prioridad: 1=VIP, 2=Ejecutiva (Normal), 3=Económica (Básico)
-- Asignados al vuelo Guayaquil-Madrid (HC-BXA)
INSERT INTO dbo.pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada, matricula, pnr) VALUES
('Juan Perez (Adulto - VIP)', '1A', 1, NULL, 'HC-BXA', 'SQ-77X9'),
('Maria Perez (Adulto - Ejecutiva)', '2B', 2, NULL, 'HC-BXA', 'SQ-77X9'),
('Pedrito Perez (Niño - Económica)', '5C', 3, NULL, 'HC-BXA', 'SQ-77X9'),
('Sonia Perez (Adulto - Económica)', '5D', 3, NULL, 'HC-BXA', 'SQ-77X9');
GO
