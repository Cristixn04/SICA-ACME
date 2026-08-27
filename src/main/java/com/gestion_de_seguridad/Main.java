package com.gestion_de_seguridad;

public class Main {
    public static void main(String[] args) {
        
    }
}

/* # SICA — Sistema Integrado de Control de Acceso para Zona ACME

> Documento maestro de especificaciones técnicas. Este archivo está diseñado para ser
> entregado a una IA de generación de código (o a un desarrollador) como fuente única
> de verdad del proyecto. Contiene contexto de negocio, requisitos funcionales, reglas
> de arquitectura obligatorias y convenciones de trabajo. Antes de escribir código,
> léelo completo.

---

## 1. Contexto de Negocio

El Complejo Empresarial **"Zona ACME"** alberga a más de 30 empresas de alto perfil.
Actualmente su control de acceso es manual (libros de registro en papel y comunicación
por radio), lo que genera cuatro problemas críticos que el sistema debe resolver:

1. **Vulnerabilidad de seguridad**: no existe un registro fiable de quién está dentro
   del complejo en un momento dado. En una emergencia, la evacuación sería caótica.
2. **Experiencia ineficiente**: filas largas en horas pico, cuellos de botella con
   invitados no anunciados.
3. **Falta de trazabilidad**: investigar un incidente implica revisar libros en papel
   manualmente.
4. **Gestión de incidentes reactiva**: no hay forma de restringir el acceso a una
   persona de forma inmediata.

**SICA** debe resolver estos cuatro puntos de forma explícita y verificable.

---

## 2. Flujo de Acceso (Reglas de Negocio Clave)

### 2.1 Invitado Pre-registrado (flujo ideal)
1. Un **Funcionario de Empresa** registra previamente a un invitado, indicando sus
   datos y la fecha/hora de la visita. El estado de la visita queda como `APROBADO`.
2. Al llegar, el invitado presenta su documento. El **Guarda de Seguridad** lo busca
   en el sistema.
3. La pantalla del guarda muestra instantáneamente la información del invitado, su
   foto (URL), a quién visita y que su acceso está autorizado.
4. El guarda realiza el **check-in**.

### 2.2 Invitado No Anunciado (tiempo real)
1. Un invitado llega sin registro previo. El guarda toma sus datos, los ingresa y la
   visita se crea con estado `PENDIENTE_APROBACION`.
2. El sistema notifica automáticamente al **Funcionario de Empresa** correspondiente.
3. El funcionario ve la solicitud en su pantalla y la aprueba o rechaza.
4. **La pantalla del Guarda se actualiza en tiempo real** (mediante concurrencia)
   reflejando la decisión, sin necesidad de refrescar manualmente.
5. Si aprueba → estado pasa a `APROBADO` → el guarda hace check-in.
6. Si rechaza → estado pasa a `RECHAZADO`.

> **Nota de implementación**: el requisito de "tiempo real" implica que la UI JavaFX
> del Guarda debe reaccionar a cambios de estado ocurridos en otra sesión/pantalla
> (la del Funcionario). Esto se resuelve de forma natural con el mismo mecanismo de
> **eventos de dominio** (sección 5.2): un listener en la capa de infraestructura de
> `acceso` puede actualizar la UI del Guarda vía `Platform.runLater()` al recibir el
> evento `VisitaAprobadaEvent` / `VisitaRechazadaEvent`. Si el proyecto corre como
> aplicación de un solo proceso (un JavaFX Application con múltiples ventanas/vistas
> internas), esto es sencillo con el `EventPublisher` interno. Si en cambio Guarda y
> Funcionario corren como procesos/instancias separadas, se necesitaría un mecanismo
> adicional (polling, WebSocket, o notificaciones vía base de datos) — **a confirmar
> con el trainer si la app es multi-ventana en un solo proceso o multi-instancia**.

### 2.3 Trabajador con Carnet Olvidado (Pase Temporal)
1. Un trabajador llega sin su documento de identidad.
2. El Guarda lo busca en el sistema y marca un ingreso como
   `PENDIENTE_APROBACION_POR_OLVIDO`.
3. El flujo es idéntico al del invitado no anunciado (sección 2.2): el Funcionario de
   Empresa recibe la notificación y aprueba un **ingreso puntual válido solo para ese
   día**.

### 2.4 Salida Olvidada (Flujo de Regularización)
1. Una persona (trabajador o invitado) sale del complejo sin registrar su salida. Su
   última visita queda con estado `DENTRO` (abierta indefinidamente).
2. En su **próximo intento de ingreso**, el sistema detecta la inconsistencia
   (existe una visita previa de esa persona todavía en estado `DENTRO`).
3. El sistema **no bloquea el nuevo ingreso**, pero ejecuta dos acciones automáticas:
   - La visita anterior abierta se marca automáticamente con el estado
     `CERRADA_POR_SISTEMA` (motivo: "Salida Olvidada").
   - Se crea un **nuevo registro de visita** para el ingreso actual.
4. Esto garantiza que el acceso nunca se bloquee por un error operativo, pero que la
   inconsistencia quede registrada en la bitácora de auditoría (evento auditable).

### 2.5 Máquina de Estados de la Visita
```
PENDIENTE_APROBACION ────────┬─→ APROBADO → CHECK_IN → DENTRO → CHECK_OUT
                              └─→ RECHAZADO

PENDIENTE_APROBACION_POR_OLVIDO ─→ APROBADO (ingreso puntual del día) → CHECK_IN → DENTRO → CHECK_OUT

APROBADO (pre-registrado) → CHECK_IN → DENTRO → CHECK_OUT

DENTRO ─(detectado en próximo ingreso de la misma persona)─→ CERRADA_POR_SISTEMA
```
Cualquier transición inválida (ej. hacer check-out sin check-in previo) debe ser
rechazada por el dominio con una excepción clara. El cierre automático por
`CERRADA_POR_SISTEMA` es la única transición que el sistema ejecuta sin intervención
humana directa — debe quedar auditada explícitamente como tal.

---

## 3. Roles del Sistema

| Rol | Descripción | Acciones típicas |
|---|---|---|
| **Administrador** | Gestiona usuarios, roles y permisos del sistema | CRUD de usuarios, asignación de roles/permisos |
| **Guarda de Seguridad** | Opera en los puntos de entrada | Registrar invitados no anunciados, check-in, check-out, reportar incidentes |
| **Funcionario de Empresa** | Pertenece a una de las +30 empresas | Pre-registrar invitados, aprobar/rechazar visitas no anunciadas |

> Nota: esta tabla es una base inicial. Los roles concretos y su set de permisos deben
> confirmarse/ampliarse antes de implementar (ver sección 10, "Pendientes").

---

## 4. Módulos del Sistema

### 4.1 Gestión de Usuarios y Seguridad (RBAC)
- Control de Acceso Basado en Roles, **configurable y auditable**: los permisos **no**
  están definidos en el código, sino en la base de datos.
- El sistema define **Roles** y **Permisos** por separado. Cada acción crítica (ej.
  `crear_usuario`, `registrar_visita`, `generar_reporte`, `bloquear_persona`) es un
  permiso individual. Los roles son agrupaciones de permisos.
- **Lógica de autorización**: antes de ejecutar cualquier operación, el sistema debe
  verificar si el rol del usuario autenticado tiene el permiso específico requerido.
  Si no lo tiene, la operación se deniega con un mensaje claro.

### 4.2 Auditoría y Trazabilidad (Bitácora)
- Cada acción relevante del sistema debe registrarse en una bitácora de auditoría
  **inmutable** (tabla `bitacora_auditoria` — solo permite `INSERT`, nunca `UPDATE`
  ni `DELETE` desde la capa de aplicación).
- La lógica de inserción vive en la **capa de servicio de Java**, no en triggers de BD.
- **Acciones mínimas a registrar**:
  - Intentos de login (exitosos y fallidos).
  - Creación, actualización o eliminación de cualquier entidad principal (Usuarios,
    Personas, Empresas).
  - Cambios de estado de acceso de una persona.
  - Registro de un incidente.
  - Check-in y check-out de una visita.
  - Cierre automático de una visita por el sistema (`CERRADA_POR_SISTEMA` — salida
    olvidada), como evento auditable propio distinto de un check-out normal.

### 4.3 Gestión de Personas
- CRUD de personas (empleados, visitantes frecuentes, etc.), asociadas a una empresa.
- Opera bajo las reglas de permisos y auditoría del sistema.

### 4.4 Control de Acceso
- Gestión de visitas: creación, aprobación/rechazo, check-in, check-out.
- Implementa la máquina de estados descrita en la sección 2.3.

### 4.5 Gestión de Incidentes
- Registro de incidentes de seguridad asociados a una persona.
- Debe permitir marcar a una persona con una restricción de acceso de forma inmediata.

### 4.6 Reportes
- Generación de reportes usando fuertemente **lambdas y la API Stream** de Java (ej.
  personas actualmente dentro del complejo, visitas por estado, incidentes por
  severidad).

---

## 5. Arquitectura Obligatoria

### 5.1 Arquitectura Hexagonal con Vertical Slice
La arquitectura es **exclusivamente Hexagonal** (NO MVC). Se organiza por **vertical
slice**: cada módulo de negocio tiene su propio hexágono completo (dominio, puertos,
aplicación, adaptadores), en lugar de una única capa horizontal compartida por todos
los módulos.

```
com.acme.sica/
│
├── shared/                          # Kernel compartido entre slices
│   ├── domain/                      # Value Objects comunes, excepciones base
│   │   └── event/                   # EventPublisher, DomainEvent (interfaz base)
│   └── infrastructure/
│       ├── config/                  # Configuración, DI, conexión BD
│       └── persistence/             # DataSource PostgreSQL
│
├── usuarios/                        # SLICE: Gestión de Usuarios y Seguridad (RBAC)
│   ├── domain/
│   │   ├── model/                   # Usuario, Rol, Permiso
│   │   └── port/
│   │       ├── in/                  # AutenticarUsuarioUseCase, VerificarPermisoUseCase
│   │       └── out/                 # UsuarioRepositoryPort, RolRepositoryPort
│   ├── application/service/         # Implementación de casos de uso
│   └── infrastructure/
│       ├── adapter/in/              # Adaptador de entrada (JavaFX controller)
│       └── adapter/out/persistence/ # Adaptador JDBC/PostgreSQL
│
├── personas/                        # SLICE: Gestión de Personas
│   └── (misma estructura: domain / application / infrastructure)
│
├── acceso/                          # SLICE: Control de Acceso (Visitas)
│   ├── domain/
│   │   ├── model/                   # Visita, EstadoVisita (enum + transiciones = patrón State)
│   │   └── port/{in, out}
│   └── (application / infrastructure)
│
├── incidentes/                      # SLICE: Gestión de Incidentes
│   └── (misma estructura)
│
├── auditoria/                       # SLICE: Auditoría y Trazabilidad
│   ├── domain/
│   │   ├── model/                   # RegistroAuditoria (inmutable, sin setters)
│   │   └── port/
│   │       ├── in/                  # RegistrarAuditoriaUseCase
│   │       └── out/                 # AuditoriaRepositoryPort (solo método guardar)
│   └── (application / infrastructure)
│
└── reportes/                        # SLICE: Reportes (uso intensivo de Streams/lambdas)
    └── (domain/port/in, application, infrastructure)
```

**Regla de dependencia estricta**: el `domain` de cada slice no puede depender de
`application` ni de `infrastructure`. Los adaptadores de `infrastructure` son los
únicos que conocen JDBC/SQL/JavaFX.

### 5.2 Comunicación entre Slices: Eventos de Dominio
Los slices **no se inyectan directamente entre sí**. Se comunican mediante un
`EventPublisher` interno (patrón **Observer**), definido en `shared/domain/event/`.

- Ejemplo: cuando `acceso` completa un check-in, publica un `CheckInRealizadoEvent`.
  El listener de `auditoria` está suscrito a ese tipo de evento y registra la bitácora,
  sin que `acceso` conozca la existencia de `auditoria`.
- Esto aplica a **toda** acción auditable listada en la sección 4.2 y a las
  notificaciones (ej. `VisitaNoAnunciadaEvent` → notifica al Funcionario de Empresa).
- Implementación recomendada: un `EventPublisher` simple con
  `Map<Class<?>, List<Consumer<?>>>`, usando lambdas para los listeners — esto cubre
  también el requisito de "funciones lambda" del proyecto.

### 5.3 Principios y Patrones Obligatorios
- **SOLID** aplicado rigurosamente en todo el código.
- **Mínimo 2 patrones de diseño**, ya identificados por el diseño de este proyecto:
  - **Observer** → comunicación entre slices vía eventos de dominio (obligatorio,
    ver 5.2).
  - **State** → máquina de estados de `Visita` (`EstadoVisita`), ver sección 2.3.
  - (Opcional, si se quiere un tercero) **Strategy** para las reglas de verificación
    de permisos, o **Factory** para creación de entidades con distintos roles.
- **Funciones lambda y API Stream de Java**: usar de forma intensiva en el módulo de
  Reportes y en el `EventPublisher`.

### 5.4 Base de Datos
- Motor: **PostgreSQL**.
- Entregar `schema.sql` (creación de estructura) y `data.sql` (datos de poblado /
  seed, incluyendo credenciales de ejemplo para cada rol).
- Los permisos y roles deben poder modificarse **sin tocar código** (viven en tablas,
  no en enums de Java para su lógica de negocio — aunque el modelo de dominio los
  represente como objetos).

### 5.5 Interfaz Gráfica
- **JavaFX**.
- Estilo visual: tema corporativo "Zona ACME" con identidad de marca inspirada en
  caricaturas clásicas tipo Looney Tunes (logo circular, paleta roja/naranja/amarilla
  sobre fondo oscuro, contornos gruesos tipo cartoon, sombras "pop" tipo cómic),
  equilibrado aproximadamente 70% software serio / 30% personalidad ACME — es una
  herramienta real de seguridad corporativa, no debe verse infantil.
- Debe incluir microinteracciones/animaciones suaves reales usando las APIs de
  animación de JavaFX (`FadeTransition`, `Timeline`, efectos de glow en hover,
  transiciones de badges de estado, pulso en notificaciones).
- Pantallas mínimas a implementar:
  1. Login (con selector visual de rol)
  2. Olvidé mi contraseña
  3. Check-in de Visitante (pantalla principal del guarda)
  4. Registrar Nuevo Funcionario
  5. Gestión de Personas
  6. Gestión de Incidentes
  7. Reportes (dashboard con métricas)
  8. Gestión de Usuarios (RBAC: asignación de roles y permisos)

---

## 6. Convenciones de Trabajo (Git)

- **Git Flow** como flujo de ramas (`main`, `develop`, `feature/*`, `release/*`,
  `hotfix/*`).
- Mensajes de commit siguiendo la especificación **Conventional Commits**
  (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`, `test:`, etc.), con el scope
  idealmente indicando el slice afectado (ej. `feat(acceso): agregar transición de
  estado check-out`).

---

## 7. Entregables Finales

Repositorio privado de GitHub (con el trainer como colaborador) que contenga:

1. **Código fuente completo**, estructurado en paquetes según la arquitectura
   Hexagonal por vertical slice (sección 5.1).
2. **Documentación en `README.md`**:
   - Descripción del proyecto: resumen del problema y la solución.
   - Modelo de la base de datos: diagrama Entidad-Relación.
   - Decisiones de diseño: sección explicando dónde y por qué se aplicaron los
     principios SOLID y los patrones de diseño.
   - Instrucciones de instalación y ejecución.
   - Guía de uso, incluyendo credenciales de ejemplo para cada rol.
3. **Scripts de base de datos**: `schema.sql` (creación) y `data.sql` (poblado).

---

## 8. Instrucciones para la IA que va a codificar

- Trabaja **slice por slice**, completando el hexágono completo de un módulo (domain
  → port → application → infrastructure) antes de pasar al siguiente, en vez de
  construir todas las capas de todos los módulos a la vez.
- Orden sugerido de implementación: `shared` (EventPublisher + config BD) →
  `usuarios` (RBAC, es prerrequisito de autorización para todo lo demás) →
  `personas` → `acceso` → `auditoria` → `incidentes` → `reportes` → UI JavaFX.
- No acoplar slices entre sí mediante imports directos de clases de `application` o
  `infrastructure` de otro slice. La única comunicación permitida entre slices es a
  través de eventos de dominio publicados/escuchados vía `EventPublisher`.
- El dominio (`domain/`) de cada slice debe ser Java puro, sin anotaciones de
  frameworks ni dependencias de JDBC/JavaFX.
- Antes de escribir cada entidad, define primero su **puerto de entrada** (caso de
  uso) y su **puerto de salida** (repositorio), y solo después implementa el
  adaptador concreto.
- Justifica en comentarios o en el README cada aplicación de un principio SOLID o
  patrón de diseño cuando no sea evidente por sí sola.
- Sigue Conventional Commits en cada commit, con scope por slice.

---

## 9. Prompts de Referencia para Diseño Visual (JavaFX)

Estos prompts fueron usados para generar mockups de referencia del estilo visual y
sirven como guía de diseño para implementar las vistas JavaFX reales:

- Login, Check-in de Visitante, Registrar Funcionario, Olvidé mi Contraseña, Gestión
  de Personas, Gestión de Incidentes, Reportes, Gestión de Usuarios (RBAC).
- Lenguaje visual común: paleta corporativa ACME (rojo/naranja/amarillo sobre fondo
  oscuro carbón), logo circular con cohete/rayo estilizado, contornos gruesos tipo
  cartoon, sombras "pop" tipo cómic, glow suave en elementos activos/hover, señales
  de microinteracción (motion lines, pulso de notificación, halos de transición de
  estado), equilibrio 70% serio / 30% caricaturesco.

---

## 10. Pendientes / Decisiones Abiertas

Estos puntos deben confirmarse antes o durante la implementación:

- [ ] Lista completa y definitiva de roles y de permisos granulares (más allá de los
  ejemplos dados: `crear_usuario`, `registrar_visita`, `generar_reporte`,
  `bloquear_persona`).
- [ ] Atributos exactos de cada entidad de dominio (Usuario, Rol, Permiso, Persona,
  Empresa, Visita, Incidente, RegistroAuditoria).
- [ ] Diagrama Entidad-Relación completo de la base de datos.
- [ ] Si se necesita un tercer patrón de diseño además de Observer y State (ej.
  Strategy o Factory) y dónde aplicarlo exactamente.
- [ ] Detalles de despliegue/ejecución local (versión de Java, gestor de
  dependencias: Maven o Gradle).
- [ ] Confirmar con el trainer si la aplicación corre como un solo proceso JavaFX
  (multi-ventana interna) o como instancias separadas por rol, ya que esto determina
  el mecanismo técnico para la actualización en tiempo real de la pantalla del Guarda
  (ver nota en sección 2.2). */