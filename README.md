# ✈️ SkyQ - Consola Aeroportuaria de Control Integrado

Bienvenido al repositorio oficial de **SkyQ**, un ecosistema de software de nivel empresarial diseñado para el control y la gestión operativa de aerolíneas y terminales aéreas en tiempo real. 

Este proyecto está concebido bajo una arquitectura robusta de tres aplicaciones integradas. Actualmente se encuentra completada la **Fase 1: App Manager**, la consola de escritorio centralizada para la administración operativa, gestión de personal, control de cabinas y autorizaciones de seguridad.

---

## 🛠️ Stack Tecnológico

La arquitectura de SkyQ prioriza el rendimiento determinista, el control absoluto de los recursos de hardware y la persistencia de datos de alta velocidad:

* **Lenguaje:** Java 17+ (Desarrollado y validado en JDK 26) para asegurar compatibilidad con sintaxis moderna y optimizaciones del runtime.
* **Interfaz Gráfica:** **Swing Puro** con un motor de renderizado optimizado por hilos y un sistema de diseño visual personalizado (`EstiloUI`) para una experiencia visual inmersiva.
* **Persistencia:** **JDBC Puro** (sin sobrecarga de frameworks u ORMs), garantizando llamadas SQL de microsegundos y transaccionalidad nativa estructurada.
* **Motor de Base de Datos:** SQL Server 2025 con soporte para procedimientos almacenados, desencadenadores transaccionales y auditorías concurrentes.
* **Manejo de Conexiones:** **Custom Connection Pool** implementado con un *Dynamic Proxy* ([ConexionBD.java](file:///c:/Users/PC/Downloads/4TO%20SEMESTRE/SkyQ/skyq/src/skyq/database/ConexionBD.java)) que intercepta llamadas a `close()` para devolver conexiones al pool de forma segura e instantánea.

---

## 💎 Módulos Core y Logros Implementados

1. **🔒 Control de Acceso basado en Roles (RBAC):** Login seguro con privilegios diferenciados para Gerentes y Operarios, protegiendo vistas y habilitando/deshabilitando pestañas de forma reactiva.
2. **📝 Registro y Auditoría Transaccional:** Cada evento crítico de modificación de vuelos, mantenimientos o ventas queda registrado con autoría de usuario y marca de tiempo en la tabla de auditoría.
3. **📐 Lógica Geométrica de Asientos:** Un plano interactivo en tiempo real ([MapaAsientosPanel.java](file:///c:/Users/PC/Downloads/4TO%20SEMESTRE/SkyQ/skyq/src/skyq/view/MapaAsientosPanel.java)) que parsea distribuciones complejas (ej. `3-3`, `2-4-2`) y calcula dinámicamente las coordenadas de botones de asientos con soporte para múltiples clases (VIP, Ejecutiva, Económica).
4. **🛑 Bloqueos Operativos de Mantenimiento:** Validación cruzada en tiempo real. El sistema impide vender boletos o realizar el Check-In si el vuelo coincide con periodos de mantenimiento programados para la aeronave.
5. **🎟️ Flujo de Ventas y Check-in con Generación de PNR:** Proceso completo desde la compra de boleto hasta la asignación de asiento y el despacho del equipaje por PNR único, registrando pesos y prioridades.
6. **🔑 Seguridad "Manager Override" (Acceso Operario con Firma Gerencial):** Modificaciones críticas en los vuelos programados requieren la contraseña en vivo de un Gerente para desbloquear y registrar la acción auditada de forma segura.

---

## 📈 Calidad de Código y Estabilidad (0 Warnings)

El código ha sido sometido a un riguroso proceso de saneamiento e inspección de deuda técnica para asegurar la máxima mantenibilidad:
* **Cero Advertencias de Compilación:** Compilación 100% limpia bajo la bandera `-Xlint:all` de JDK.
* **Eliminación de Operadores Ternarios:** Todos los bloques ternarios complejos (`? :`) se transicionaron a estructuras `if-else` tradicionales y legibles.
* **Garantía de Serialización Transparente:** Remoción de la supresión genérica `@SuppressWarnings("serial")` del IDE, sustituyéndola por una arquitectura correcta donde los elementos Swing implementan `serialVersionUID` y todos los objetos no serializables asociados (como DAOs, modelos o listeners) se marcan con la palabra clave **`transient`**.
* **Protección `[this-escape]`**: Todas las vistas de Swing se declararon como clases `final` para evitar la filtración de la referencia `this` a subclases durante su inicialización.

---

## 🗄️ Despliegue de Base de Datos

Los archivos de inicialización y semillas para la base de datos SQL Server están ubicados en el directorio [BD/SkyQ/](file:///c:/Users/PC/Downloads/4TO%20SEMESTRE/SkyQ/BD/SkyQ). Siga este orden estricto de ejecución para desplegar el entorno local:

1. **`00_drop.sql`:** Elimina cualquier versión previa de tablas, triggers y constraints existentes para asegurar un despliegue limpio.
2. **`01_schema.sql`:** Crea la estructura de tablas relacionales de la plataforma, relaciones de llaves foráneas, triggers de automatización y constraints operativas.
3. **`02_seed.sql`:** Carga los datos de inicialización esenciales para probar el sistema, incluyendo aviones, pilotos en ruta, usuarios de prueba y vuelos programados.

---

## 🔑 Credenciales de Acceso por Defecto

El script de semillas inyecta las siguientes credenciales preconfiguradas para pruebas y desarrollo de la plataforma:

| Rol | Usuario | Contraseña | Privilegios |
|---|---|---|---|
| **Gerente** | `gerente` | `Gerente@123` | Control total del Dashboard, radar interactivo, asignación de pilotos y aprobación de Manager Override. |
| **Operario** | `operario` | `Operario@123` | Gestión de ventas de boletos, cola de abordaje y flujo de Check-In del pasajero. |

---
*SkyQ © 2026 - Avanzando la eficiencia y seguridad en el transporte aéreo.*
