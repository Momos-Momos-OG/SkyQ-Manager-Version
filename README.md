# ✈  SkyQ — Consola Aeroportuaria de Control Integrado

**SkyQ** es un sistema de escritorio premium desarrollado en Java Swing con una arquitectura basada en capas y orientado a la gestión de un **Aeropuerto Único (Local)**. El sistema integra el control del estado de la flota, el registro y venta de boletos, check-in de equipaje, y un simulador operativo para el pre-embarque y desembarque de pasajeros.

Esta versión cumple al 100% con los requisitos académicos y especificaciones del documento **SRS (Software Requirements Specification)** utilizando estructuras de datos y algoritmos manuales de ordenamiento.

---

## 🚀 Núcleo Algorítmico (Estructuras de Datos - SRS)

El sistema demuestra el dominio de las estructuras de datos y el flujo operativo a través de tres motores algorítmicos localizados en el paquete `skyq.services`:

### 1. 🛫 Cola de Prioridad (`PriorityQueue`)
*   **Clase:** `AbordajeService.java`
*   **Funcionamiento:** Organiza a los pasajeros listos para abordar en un `java.util.PriorityQueue` en base a un comparador estricto de 3 niveles:
    1.  **Silla de Ruedas (Prioridad Especial):** Pasajeros marcados con movilidad reducida (`sillaRuedas == true`) obtienen máxima prioridad absoluta.
    2.  **Jerarquía Comercial (Clase):** Se prioriza según el nivel del boleto: Nivel 1 (VIP / Upgrades) > Nivel 2 (Ejecutiva) > Nivel 3 (Económica).
    3.  **FIFO (Check-in temporal):** A igualdad de condiciones previas, se desempata por orden de llegada (quien realizó el check-in antes, aborda primero, basado en `timestampLlegada`).

### 2. 📥 Bodega de Carga (`Stack` / LIFO)
*   **Clase:** `EquipajeService.java`
*   **Funcionamiento:** Modela el cargue físico de maletas en la bodega del avión utilizando un `java.util.Stack`.
    *   `cargarBodega()`: Empuja (`push`) las maletas a la pila.
    *   `descargarBodega()`: Vacía (`pop`) la pila de equipaje. Al ser una estructura LIFO (Last-In, First-Out), el último equipaje ingresado al avión es el primero en ser descargado.

### 3. 🛬 Secuencia de Desembarque (`Bubble Sort` Manual)
*   **Clase:** `DesembarqueService.java`
*   **Funcionamiento:** Ordena a los pasajeros en base a su fila de asiento para simular el desembarque desde la parte frontal a la posterior del avión (ej. Fila 1A, 1B, 2A...).
*   **Restricción SRS:** Se prohíbe el uso de `Collections.sort()` o `List.sort()`. La ordenación se realiza mediante un algoritmo **Bubble Sort manual** con desempate alfabético de butacas.

> [!TIP]
> **Consola de Simulación:** Puede evaluar estas tres estructuras de forma interactiva y en tiempo real presionando el botón **"🚀 Abrir Simulador"** en la esquina superior del panel de **Check-In**.

---

## 🛠 Stack Tecnológico y Arquitectura

*   **Lenguaje:** Java 17+
*   **Biblioteca Gráfica:** Java Swing con estilos y colores personalizados (Tema oscuro `EstiloUI` y `PanelBarraTitulo` undecorated).
*   **Arquitectura:** MVC simplificado con capa de servicios pura (`skyq.services`) para aislar la lógica de negocio de los componentes visuales de Swing.
*   **Base de Datos:** SQL Server 2025.
*   **Acceso a Datos:** Conexión mediante JDBC Puro (sin ORMs pesados).

---

## 💾 Instalación y Configuración de la Base de Datos

Para inicializar de forma limpia la base de datos de SkyQ, ejecute los scripts ubicados en la carpeta `/BD/SkyQ/` en el siguiente orden secuencial respetando las claves foráneas:

1.  **`00_drop.sql`**: Limpia cualquier base de datos previa o tablas activas en cascada.
2.  **`01_schema.sql`**: Crea la base de datos `skyq_db` e inicializa las tablas con las columnas correctas (incluyendo `sillaRuedas`, `upgrade` y la configuración de cabinas).
3.  **`02_seed.sql`**: Carga datos iniciales de prueba (usuarios, flota de aviones mapeada con Enums, pilotos, vuelos del Aeropuerto Local y pasajeros con PNRs activos).

---

## 🔑 Credenciales de Acceso

Inicie la aplicación y autentíquese utilizando uno de los siguientes perfiles de prueba cargados por el script de semilla:

| Rol | Usuario | Contraseña | Permisos |
| :--- | :--- | :--- | :--- |
| **Gerente** | `gerente` | `Gerente@123` | CRUD de Flota de Aviones, Asignación de Cabina, Historial de Mantenimientos, Manager Override (Autorización). |
| **Operario** | `operario` | `Operario@123` | Reserva/Venta de Boletos, Check-In, Control de Equipaje, Visualización de Simulación. |
