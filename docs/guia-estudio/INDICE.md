# Índice general de la guía de estudio

> **Qué es:** el mapa de toda la documentación. Cada fase del [plan](00-plan-de-aprendizaje.md) con sus subfases, su estado y el archivo donde se explica.
>
> **Cuándo se usa:** al empezar a estudiar (para elegir qué leer) y al cerrar una fase (para añadir el diario que falta).
>
> **Última actualización:** 2026-10-31 (fase 10 cerrada).

## Cómo leer este índice

Cada fase tiene dos documentos posibles:

| Icono | Documento | Qué cuenta |
|---|---|---|
| 📓 **Diario** | `NN-nombre-de-la-fase.md` | Lo que **hicimos**: pasos, comandos, errores reales y resultados |
| 📚 **Temario** | `temario/NN-...md` | Los **conceptos** desde cero, con ejemplos de este proyecto y preguntas de repaso |

Regla de la guía: el diario se crea **al empezar** la fase; el temario, cuando hace falta explicar la teoría. Por eso hay fases con diario y sin temario, y al revés.

**Leyenda de estado:**

| Símbolo | Significado |
|---|---|
| ✅ | Hecho y documentado |
| ⏳ | Siguiente paso (o en curso) |
| 🔒 | Pendiente |
| ➖ | Eliminado del plan |

Si un paso no indica otro archivo, lo documenta el diario de su fase.

---

## Resumen

Aquí se lleva el **progreso acumulado**: cuántas fases hay, cuántos pasos y en qué estado está cada uno. El parte del día —paso en curso, entorno, siguiente acción— está en [ESTADO.md](ESTADO.md).

| Métrica | Valor |
|---|---|
| Unidades de fase en el plan | **29** (1–20 sin la 13, más 15A y 21–29) |
| Fases completadas | **10** (1–10) |
| Fase adelantada | **15A** parcial: 15A.1 y 15A.2 hechos; falta 15A.3 |
| Pasos numerados | **92** |
| Pasos hechos | **52** (los 48 de las fases 1–10 + 15A.1, 15A.2, 11.1 y 11.2) |
| Pasos pendientes | **40** |
| Siguiente paso | **11.3** — entidades simples (`@Entity`, `@Table`, `@Id`, `@Column`) |
| Objetivo del alumno | **`POST /movies`** (fase 17) |
| Ruta mínima hasta el POST | **28 pasos**, con la fase 14 incluida |

---

## Bloque A — Preparación

### Fase 1 · Inspección inicial y herramientas · ✅

📓 Diario: [01-inspeccion-inicial.md](01-inspeccion-inicial.md) · 📚 Temario: — (es una fase de inspección, sin conceptos nuevos)

| Paso | Concepto | Estado |
|---|---|---|
| 1.1 | Leer `base-standards.md` | ✅ |
| 1.2 | Inspeccionar el repositorio | ✅ |
| 1.3 | Comprobar herramientas y versiones | ✅ |
| 1.4 | Proponer alcance, índice y guía | ✅ → [00-plan-de-aprendizaje.md](00-plan-de-aprendizaje.md) |

### Fase 2 · Alcance del dominio y datos de prueba · ✅

📓 Diario: [02-alcance-del-dominio.md](02-alcance-del-dominio.md) · 📚 Temario: [temario/02-entidades-atributos-y-relaciones.md](temario/02-entidades-atributos-y-relaciones.md)

| Paso | Concepto | Estado |
|---|---|---|
| 2.1 | Identificar entidades | ✅ |
| 2.2 | Identificar atributos | ✅ |
| 2.3 | Identificar relaciones en lenguaje natural | ✅ |
| 2.4 | Elegir el conjunto de datos de prueba | ✅ |

### Fase 3 · Diseño del esquema relacional · ✅

📓 Diario: [03-diseno-del-esquema.md](03-diseno-del-esquema.md) · 📚 Temario: [temario/03-diseno-esquema-relacional.md](temario/03-diseno-esquema-relacional.md)

| Paso | Concepto | Estado |
|---|---|---|
| 3.1 | De entidad a tabla | ✅ |
| 3.2 | Claves primarias | ✅ |
| 3.3 | Relación uno a muchos | ✅ |
| 3.4 | Relación muchos a muchos | ✅ |
| 3.5 | Restricciones | ✅ |
| 3.6 | Diagrama entidad-relación | ✅ |

---

## Bloque B — Base de datos

### Fase 4 · Docker, Docker Compose y MariaDB · ✅

📓 Diario: [04-docker-y-mariadb.md](04-docker-y-mariadb.md) · 📚 Temario: [temario/04-docker-compose-mariadb.md](temario/04-docker-compose-mariadb.md)

| Paso | Concepto | Estado |
|---|---|---|
| 4.1 | Qué es Docker | ✅ |
| 4.2 | Qué es Docker Compose | ✅ |
| 4.3 | Credenciales fuera del repositorio | ✅ |
| 4.4 | Volúmenes | ✅ |
| 4.5 | Conectarse a MariaDB | ✅ |
| 4.6 | Healthcheck | ✅ |

### Fase 5 · Arquitectura en tres capas e inversión de dependencias · ✅

📓 Diario: [05-arquitectura.md](05-arquitectura.md) · 📚 Temario: [temario/05-arquitectura-tres-capas-dip.md](temario/05-arquitectura-tres-capas-dip.md)

| Paso | Concepto | Estado |
|---|---|---|
| 5.1 | Las tres capas | ✅ |
| 5.2 | Dirección de las dependencias | ✅ |
| 5.3 | Principio de inversión de dependencias (DIP) | ✅ |
| 5.4 | Capas conceptuales vs. paquetes | ✅ |

### Fase 6 · Creación del proyecto Spring Boot · ✅

📓 Diario: [06-creacion-proyecto-spring-boot.md](06-creacion-proyecto-spring-boot.md) · 📚 Temario: [temario/06-proyecto-spring-boot-y-git.md](temario/06-proyecto-spring-boot-y-git.md)

| Paso | Concepto | Estado |
|---|---|---|
| 6.1 | Git y `.gitignore` | ✅ |
| 6.2 | Generar el proyecto | ✅ |
| 6.3 | Dependencias mínimas | ✅ |
| 6.4 | Estructura Maven | ✅ |
| 6.5 | Primer arranque y primer test | ✅ |

### Fase 7 · Conexión entre Java y MariaDB · ✅

📓 Diario: [07-conexion-java-mariadb.md](07-conexion-java-mariadb.md) · 📚 Temario: [temario/07-conexion-jdbc-datasource.md](temario/07-conexion-jdbc-datasource.md)

| Paso | Concepto | Estado |
|---|---|---|
| 7.1 | Driver JDBC y `DataSource` | ✅ |
| 7.2 | `application.yaml` con variables de entorno | ✅ |
| 7.3 | Hibernate no toca el esquema (`ddl-auto`) | ✅ |
| 7.4 | Comprobar la conexión | ✅ |

### Fase 8 · Flyway: migraciones del esquema · ✅

📓 Diario: [08-flyway-migraciones.md](08-flyway-migraciones.md)
📚 Temario: [temario/08-migraciones-flyway.md](temario/08-migraciones-flyway.md) · [temario/08-diseno-de-endpoints.md](temario/08-diseno-de-endpoints.md) (paso 8.2)

| Paso | Concepto | Estado | Documento específico |
|---|---|---|---|
| 8.1 | Qué es una migración | ✅ | [temario 08 (Flyway)](temario/08-migraciones-flyway.md) |
| 8.2 | Diseño de los endpoints y revisión del esquema | ✅ | [temario 08 (endpoints)](temario/08-diseno-de-endpoints.md) |
| 8.3 | Configurar Flyway | ✅ | [temario 08 (Flyway)](temario/08-migraciones-flyway.md) |
| 8.4 | Primera migración | ✅ | — |
| 8.5 | Migraciones con FK | ✅ | — |
| 8.6 | Inmutabilidad de migraciones | ✅ | — |

### Fase 9 · Datos de prueba y comprobación de relaciones · ✅

📓 Diario: [09-datos-de-prueba.md](09-datos-de-prueba.md) · 📚 Temario: [temario/09-datos-de-prueba-y-consultas.md](temario/09-datos-de-prueba-y-consultas.md)

| Paso | Concepto | Estado |
|---|---|---|
| 9.1 | Esquema vs. datos de prueba | ✅ |
| 9.2 | `INSERT` y orden por claves foráneas | ✅ |
| 9.3 | Provocar un error de FK a propósito | ✅ |
| 9.4 | Consultas con `JOIN` | ✅ |
| 9.5 | `LEFT JOIN` y agregados | ✅ |

---

## Bloque C — Java y persistencia

### Fase 10 · Modelo de dominio · ✅

📓 Diario: [10-modelo-de-dominio.md](10-modelo-de-dominio.md) · 📚 Temario: [temario/10-modelo-de-dominio.md](temario/10-modelo-de-dominio.md)

| Paso | Concepto | Estado |
|---|---|---|
| 10.1 | Modelo anémico vs. rico | ✅ |
| 10.2 | *Value objects*: qué son y cuáles necesitamos | ✅ |
| 10.3 | *Value objects* con TDD | ✅ |
| 10.4 | Entidades y relaciones | ✅ |

### Fase 11 · Fundamentos de JPA y entidades · ⏳ en curso

📓 Diario: [11-fundamentos-jpa.md](11-fundamentos-jpa.md) · 📚 Temario: [temario/11-introduccion-jpa.md](temario/11-introduccion-jpa.md)

| Paso | Concepto | Estado |
|---|---|---|
| 11.1 | Qué es JPA y qué es Hibernate | ✅ |
| 11.2 | JPA en las clases del dominio (D-29) | ✅ |
| 11.3 | Entidades simples (`@Entity`, `@Table`, `@Id`, `@Column`) | ⏳ |
| 11.4 | `@ManyToOne` | 🔒 |
| 11.5 | `@ManyToMany` + `@JoinTable` | 🔒 |
| 11.6 | `FetchType` (`LAZY` vs. `EAGER`) | 🔒 |

### Fase 12 · Repositorios y adaptadores de persistencia · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: — (el paso 12.1 se apoya en [temario/05, sección 10](temario/05-arquitectura-tres-capas-dip.md))

| Paso | Concepto | Estado |
|---|---|---|
| 12.1 | DAO frente a Repositorio | 🔒 |
| 12.2 | Interfaz de repositorio en el dominio (puerto) | 🔒 |
| 12.3 | Repositorio Spring Data JPA | 🔒 |
| 12.4 | Adaptador | 🔒 |
| 12.5 | Inyección de dependencias | 🔒 |

### ~~Fase 13 · Mapeos entre persistencia y dominio~~ · ➖ eliminada

No tiene pasos: al ser la entidad el propio dominio (D-29) no hay nada que mapear. Los conceptos de mapeo pasan a la fase 15. Explicación en el [plan](00-plan-de-aprendizaje.md) y en [decisiones-técnicas, D-29](decisiones-tecnicas.md).

### Fase 14 · Consultas de información relacionada y problemas habituales · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 14.1 | Ver el SQL que genera Hibernate | 🔒 |
| 14.2 | Problema N+1 | 🔒 |
| 14.3 | Soluciones (`JOIN FETCH`, `@EntityGraph`) | 🔒 |
| 14.4 | `LazyInitializationException` | 🔒 |
| 14.5 | Referencias circulares | 🔒 |
| 14.6 | `open-in-view` | 🔒 |

---

## Bloque D — API como herramienta de aprendizaje

### Fase 15 · DTOs de salida y mapeo dominio → DTO · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 15.1 | Qué es un DTO y cuándo compensa | 🔒 |
| 15.2 | DTO de salida con `record` | 🔒 |
| 15.3 | Mapper dominio → DTO | 🔒 |

### Fase 15A · API First: el contrato antes del código · 🟡 parcial

📓 Diario: [15A-api-first.md](15A-api-first.md) · 📚 Temario: [temario/15A-api-first.md](temario/15A-api-first.md)
📎 Artefactos: [openapi.yaml](../../src/main/resources/static/openapi.yaml) · [captura de Swagger UI](img/15A-swagger-ui.png)

| Paso | Concepto | Estado |
|---|---|---|
| 15A.1 | El contrato de la v1 | ✅ |
| 15A.2 | OpenAPI y Swagger UI | ✅ |
| 15A.3 | Contrato frente a implementación | 🔒 (necesita la fase 16) |

### Fase 16 · Servicio y controladores REST de lectura · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 16.1 | Servicio (caso de uso) | 🔒 |
| 16.2 | Controlador (`@RestController`, `@GetMapping`) | 🔒 |
| 16.3 | Probar desde fuera (`curl` o cliente HTTP) | 🔒 |

### Fase 17 · Operaciones de escritura y transacciones · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 17.1 | DTO de entrada (`CreateMovieRequest`) | 🔒 |
| 17.2 | Recorrido inverso: DTO → dominio → fila (`POST /movies`) | 🔒 |
| 17.3 | Transacciones (`@Transactional`) | 🔒 |

> **Antes del 17.2 hay un paso fuera del plan:** ampliar [openapi.yaml](../../src/main/resources/static/openapi.yaml) con `POST /movies`, su cuerpo, el `201` con `Location` y los errores `400`/`404` (API First, D-47).

### Fase 18 · Validaciones y manejo de excepciones · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 18.1 | Tipos de errores | 🔒 |
| 18.2 | Bean Validation | 🔒 |
| 18.3 | Excepciones propias | 🔒 |
| 18.4 | `@RestControllerAdvice` | 🔒 |
| 18.5 | `ProblemDetail` (RFC 9457) | 🔒 |

---

## Bloque E — Calidad y cierre

### Fase 19 · Pruebas de integración y revisión de cobertura · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 19.1 | Pirámide de pruebas | 🔒 |
| 19.2 | Pruebas de persistencia (Testcontainers) | 🔒 |
| 19.3 | Pruebas del controlador | 🔒 |

### Fase 20 · Revisión de arquitectura y documentación final · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 20.1 | Comprobar dependencias entre paquetes | 🔒 |
| 20.2 | Revisar la guía | 🔒 |
| 20.3 | README del proyecto | 🔒 |

---

## Fases sin subfases (21–24 y 26–29)

El plan no las divide en pasos. Se documentarán, si se hacen, con un diario propio.

| Fase | Tema | Prioridad | Estado |
|---|---|---|---|
| 21 | Contenerizar la aplicación (`Dockerfile` + Compose) | ⚪ opcional | 🔒 |
| 22 | MapStruct | ⚪ opcional | 🔒 |
| 23 | Ampliar la relación película–actor con datos propios | ⚪ opcional | 🔒 |
| 24 | Seguridad con Spring Security y JWT | 🟡 recomendable | 🔒 |
| 26 | Eventos de aplicación y mensajería | ⚪ opcional | 🔒 |
| 27 | Logs e instrumentación | ⚪ opcional | 🔒 |
| 28 | Concurrencia: hilos de Tomcat y virtual threads | ⚪ opcional | 🔒 |
| 29 | Serverless: lambda local | ⚪ opcional | ➖ no encaja en el proyecto |

---

## Bloque F — Temas del temario del profesor

### Fase 25 · Monolito modular: paquetes por módulo, ArchUnit y Spring Modulith · 🔒

📓 Diario: — (se crea al empezar) · 📚 Temario: —

| Paso | Concepto | Estado |
|---|---|---|
| 25.1 | De capas a módulos | 🔒 |
| 25.2 | API pública del módulo | 🔒 |
| 25.3 | ArchUnit | 🔒 |
| 25.4 | Spring Modulith | 🔒 |

---

## Ruta hasta `POST /movies`

El objetivo del alumno es crear películas por HTTP. Este es el camino mínimo, con la fase 14 incluida (no se salta):

| Orden | Fase | Qué aporta | Pasos | Acumulado |
|---|---|---|---|---|
| 1 | **11** — Fundamentos de JPA | Anotar entidades y mapear los *value objects* | 6 | 6 |
| 2 | **12** — Repositorios | `save`, `findById`, `findAll` | 5 | 11 |
| 3 | **14** — Consultas y problemas habituales | Que las consultas y el `LAZY` funcionen bien desde el principio | 6 | 17 |
| 4 | **15** — DTOs de salida | `MovieResponse` | 3 | 20 |
| 5 | **15A** — API First (solo 15A.3) | Comprobar que el contrato no miente | 1 | 21 |
| 6 | **17** (paso previo) | Ampliar `openapi.yaml` con el `POST` | 1 | 22 |
| 7 | **16** — Servicio y controlador | Servicio + controlador | 3 | 25 |
| 8 | **17** — Escritura | **`POST /movies`** | 3 | **28** |

Después, muy recomendable: **fase 18** (+5 → 33), para que un título vacío devuelva `400` y no `500`. Los detalles de cada fase están más arriba, en su bloque.

---

## Índice por archivo

### Diarios de fase

| Archivo | Contenido | Fases |
|---|---|---|
| [01-inspeccion-inicial.md](01-inspeccion-inicial.md) | Normas, estado del repositorio, herramientas y versiones | 1 |
| [02-alcance-del-dominio.md](02-alcance-del-dominio.md) | Entidades, atributos, relaciones y datos de prueba elegidos | 2 |
| [03-diseno-del-esquema.md](03-diseno-del-esquema.md) | Tablas, claves y diagrama entidad-relación | 3 |
| [04-docker-y-mariadb.md](04-docker-y-mariadb.md) | Contenedor MariaDB, volumen, healthcheck y errores reales | 4 |
| [05-arquitectura.md](05-arquitectura.md) | Tres capas, DIP y estructura de paquetes | 5 |
| [06-creacion-proyecto-spring-boot.md](06-creacion-proyecto-spring-boot.md) | Git, Initializr, Maven y primer test | 6 |
| [07-conexion-java-mariadb.md](07-conexion-java-mariadb.md) | Driver, `DataSource`, `application.yaml` y `ddl-auto` | 7 |
| [08-flyway-migraciones.md](08-flyway-migraciones.md) | Migraciones `V1`–`V4`, checksum y endpoints de la v1 | 8 |
| [09-datos-de-prueba.md](09-datos-de-prueba.md) | `seed.sql`, errores de FK y consultas `JOIN` | 9 |
| [10-modelo-de-dominio.md](10-modelo-de-dominio.md) | *Value objects* y entidades con TDD | 10 |
| [11-fundamentos-jpa.md](11-fundamentos-jpa.md) | JPA, Hibernate y el camino hacia las entidades anotadas | 11 |
| [15A-api-first.md](15A-api-first.md) | Contrato OpenAPI, springdoc y Swagger UI | 15A.1–15A.2 |

### Temario

| Archivo | Contenido | Fases |
|---|---|---|
| [temario/02-entidades-atributos-y-relaciones.md](temario/02-entidades-atributos-y-relaciones.md) | Qué es una entidad, un atributo y una relación | 2 |
| [temario/03-diseno-esquema-relacional.md](temario/03-diseno-esquema-relacional.md) | De entidad a tabla: PK, FK, tabla intermedia, restricciones | 3 |
| [temario/04-docker-compose-mariadb.md](temario/04-docker-compose-mariadb.md) | Imagen, contenedor, Compose, volúmenes | 4 |
| [temario/05-arquitectura-tres-capas-dip.md](temario/05-arquitectura-tres-capas-dip.md) | Capas, acoplamiento, DIP, DAO vs. repositorio | 5, 12.1 |
| [temario/06-proyecto-spring-boot-y-git.md](temario/06-proyecto-spring-boot-y-git.md) | Spring Initializr, starters, Maven, Git | 6 |
| [temario/07-conexion-jdbc-datasource.md](temario/07-conexion-jdbc-datasource.md) | JDBC, driver, pool de conexiones, `ddl-auto` | 7 |
| [temario/08-diseno-de-endpoints.md](temario/08-diseno-de-endpoints.md) | Cómo se piensan los endpoints antes de las tablas | 8.2 |
| [temario/08-migraciones-flyway.md](temario/08-migraciones-flyway.md) | Migraciones, versionado del esquema, checksum | 8 |
| [temario/09-datos-de-prueba-y-consultas.md](temario/09-datos-de-prueba-y-consultas.md) | `INSERT`, integridad referencial, `JOIN`, agregados | 9 |
| [temario/10-modelo-de-dominio.md](temario/10-modelo-de-dominio.md) | Modelo rico, *value objects*, entidades, reflexión, mappers vs. D-29 | 10 |
| [temario/11-introduccion-jpa.md](temario/11-introduccion-jpa.md) | ORM, JPA/Hibernate/Spring Data, recorrido fila ↔ objeto, *dirty checking* | 11 (y 12–14) |
| [temario/15A-api-first.md](temario/15A-api-first.md) | API First, OpenAPI, Swagger UI | 15A |

### Documentos transversales

| Archivo | Para qué |
|---|---|
| [00-plan-de-aprendizaje.md](00-plan-de-aprendizaje.md) | Hoja de ruta: fases, pasos, dependencias y cobertura del temario del profesor |
| [ESTADO.md](ESTADO.md) | Parte de situación y bitácora: dónde estamos y qué se hizo cada sesión |
| [decisiones-tecnicas.md](decisiones-tecnicas.md) | Registro de decisiones (D-01 a D-47): alternativas, elección y motivo |
| [glosario.md](glosario.md) | Términos nuevos explicados en lenguaje sencillo |
| [README.md](README.md) | Puerta de entrada a la guía y normas de estudio |
| [RECORRIDO.md](RECORRIDO.md) | Guía para viajar por los commits: una parada por fase, con etiqueta, qué mirar y qué necesita |

### Fuera de la guía

| Archivo | Para qué |
|---|---|
| [base-standards.md](../../base-standards.md) | Normas del proyecto (TDD, idioma, commits, documentación) |
| [README.md](../../README.md) | Presentación del repositorio y cómo arrancarlo |
| [openapi.yaml](../../src/main/resources/static/openapi.yaml) | Contrato de la API v1 (hoy solo los 4 `GET`) |
| [db/migration](../../src/main/resources/db/migration) | Migraciones `V1`–`V4` (esquema real) |
| [db/testdata/seed.sql](../../src/main/resources/db/testdata/seed.sql) | Datos de prueba: 6 directores, 10 películas, 14 actores, 24 participaciones |
| [domain/model](../../src/main/java/com/daw/movies/domain/model) | `Movie`, `Director`, `Actor` y los seis *value objects* |

---

## Huecos documentales

La guía crea los archivos **cuando empieza la fase**, así que esto no es un olvido:

| Falta | Cuándo se crea |
|---|---|
| Diarios y temarios de 12, 14, 15, 16, 17, 18, 19 y 20 | Al empezar cada fase |
| Diarios/temarios de 21–29 | Solo si se hacen |
| Temario de la fase 1 | No hace falta: la fase es de inspección |

## Cómo se mantiene este índice

1. Al **cerrar una fase**: actualizar su estado aquí (✅), añadir el enlace al diario nuevo, sumar los pasos hechos en el resumen y añadir su parada en [RECORRIDO.md](RECORRIDO.md).
2. Al **empezar una fase**: cambiar su estado a ⏳ y crear la fila del diario.
3. Si se **añade o elimina una fase**: actualizar el plan, este índice y la tabla de progreso del [README](README.md).
4. Las decisiones nuevas se anotan en [decisiones-tecnicas.md](decisiones-tecnicas.md), no aquí.
