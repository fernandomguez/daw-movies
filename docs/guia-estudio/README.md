# Guía de estudio — Proyecto Películas (Java 25 + Spring Boot 4.1.1 + MariaDB + Flyway + Docker + springdoc)

Esta carpeta es la guía para repasar el proyecto más adelante. Explica **el proyecto real** que estamos construyendo, paso a paso, con los motivos de cada decisión.

> **Giro del 2026-11-04.** El profesor no quiere *value objects* (D-46 revocada) y el proyecto pasó de JPA a una arquitectura por capas con **JDBC** (DAO + `JdbcTemplate`, D-48). Hoy hay **58 tests en verde**, el skill `creador-de-skills` está actualizado y Swagger se genera desde el código (springdoc 3.2.0). Los documentos de las fases 10 a 14 conservan la historia y lo superado está marcado como *(histórico)*. El camino JPA vive en la rama `jpa-fase-11-15a`. Los pasos que quedan están en el bloque G del [plan](00-plan-de-aprendizaje.md).

## Cómo está organizada

| Archivo | Contenido |
|---|---|
| [00-plan-de-aprendizaje.md](00-plan-de-aprendizaje.md) | Hoja de ruta completa: fases, pasos, conceptos, resultados y dependencias. |
| `NN-nombre-de-la-fase.md` | Un archivo por fase, que se crea **cuando empezamos esa fase** (no antes). Cuenta **lo que hicimos**: pasos, comandos, errores y resultados. |
| `temario/` | Temas explicativos: **los conceptos** con ejemplos de este proyecto. Cada archivo lleva el número de la fase del plan a la que pertenece, para leerlos en orden de aprendizaje. |
| [decisiones-tecnicas.md](decisiones-tecnicas.md) | Registro de decisiones: alternativas, elección y motivo. |
| [glosario.md](glosario.md) | Términos nuevos explicados con lenguaje sencillo (definiciones cortas que enlazan al temario). |
| [ESTADO.md](ESTADO.md) | **Parte de situación y bitácora**: dónde estamos, qué comando toca ahora y qué se hizo en cada sesión. Se lee al empezar y se actualiza al terminar cada paso. |

## Cómo estudiar con esta guía

1. **Primero el tema** (`temario/NN-...`): explica las ideas desde cero, con ejemplos de este proyecto. Al final tiene *Lo que tienes que recordar* y **preguntas de repaso con respuesta**: intenta contestarlas antes de leer la respuesta.
2. **Después el diario de la fase** (`NN-...md`): lo que hicimos de verdad, con los comandos y lo que salió en pantalla. Empieza con un resumen *En pocas palabras*.
3. **Si te encuentras una palabra que no entiendes**, búscala en el [glosario](glosario.md).

## Temario

| Nº | Tema | Estado |
|---|---|---|
| 02 | [Entidades, atributos y relaciones](temario/02-entidades-atributos-y-relaciones.md) | Completo |
| 03 | [Diseño del esquema relacional](temario/03-diseno-esquema-relacional.md) | Completo |
| 04 | [Docker, Docker Compose y MariaDB](temario/04-docker-compose-mariadb.md) | Completo |
| 05 | [Arquitectura en tres capas e inversión de dependencias](temario/05-arquitectura-tres-capas-dip.md) | Completo (la sección 11, con JPA en el dominio, es histórica: ver D-48) |
| 06 | [Proyecto Spring Boot y Git](temario/06-proyecto-spring-boot-y-git.md) | Completo |
| 07 | [Conexión con la base de datos: JDBC, driver y `DataSource`](temario/07-conexion-jdbc-datasource.md) | Completo |
| 08 | [Diseño de los endpoints antes de crear las tablas](temario/08-diseno-de-endpoints.md) | Completo (paso 8.2) |
| 08 | [Migraciones del esquema con Flyway](temario/08-migraciones-flyway.md) | Completo (incluye cómo se activa en Spring Boot) |
| 09 | [Datos de prueba, `INSERT` y consultas con `JOIN`](temario/09-datos-de-prueba-y-consultas.md) | Completo |
| 10 | [Modelo de dominio: anémico o rico](temario/10-modelo-de-dominio.md) | Actualizado el 2026-11-04: tipos simples; los *value objects* son histórico |
| 11 | [Introducción a JPA](temario/11-introduccion-jpa.md) | Solo teoría: el proyecto usa JDBC (D-48) |
| 15A | [API First: el contrato antes del código](https://github.com/fernandomguez/daw-movies/blob/jpa-fase-11-15a/docs/guia-estudio/temario/15A-api-first.md) (rama `jpa-fase-11-15a`) | Histórico: el contrato a mano se sustituyó el 2026-11-04 por uno generado desde el código (D-47) |
| — | [Entender el código: de una petición a la base de datos](temario/entender-el-codigo.md) | Completo (recorrido, esquemas y 15 preguntas) |

Cada archivo de fase sigue, cuando corresponde, esta estructura: objetivo, conceptos, por qué los necesitamos, archivos implicados, código explicado, recorrido de los datos, comandos y comprobaciones, errores y soluciones, decisiones, resumen y preguntas de repaso.

## Progreso

| Fase | Estado | Documento |
|---|---|---|
| 1. Inspección inicial y herramientas | ✅ Completada | [01-inspeccion-inicial.md](01-inspeccion-inicial.md) |
| 2. Alcance del dominio y datos de prueba | ✅ Completada | [02-alcance-del-dominio.md](02-alcance-del-dominio.md) |
| 3. Diseño del esquema relacional | ✅ Completada | [03-diseno-del-esquema.md](03-diseno-del-esquema.md) |
| 4. Docker, Docker Compose y MariaDB | ✅ Completada | [04-docker-y-mariadb.md](04-docker-y-mariadb.md) |
| 5. Arquitectura en tres capas e inversión de dependencias | ✅ Completada (diseño revisado en la fase 6: D-19, D-29) | [05-arquitectura.md](05-arquitectura.md) |
| 6. Creación del proyecto Spring Boot | ✅ Completada | [06-creacion-proyecto-spring-boot.md](06-creacion-proyecto-spring-boot.md) |
| 7. Conexión entre Java y MariaDB | ✅ Completada | [07-conexion-java-mariadb.md](07-conexion-java-mariadb.md) |
| 8. Flyway: migraciones del esquema | ✅ Completada (6 de 6 pasos; commit `3e38972`) | [08-flyway-migraciones.md](08-flyway-migraciones.md) |
| 9. Datos de prueba y comprobación de relaciones | ✅ Completada (5 de 5 pasos; commit `7bbb579`) | [09-datos-de-prueba.md](09-datos-de-prueba.md) |
| 10. Modelo de dominio | ✅ Cerrada; **reescrita el 2026-11-04** (sin *value objects*, tipos simples) | [10-modelo-de-dominio.md](10-modelo-de-dominio.md) (ver «Giro del 2026-11-04») |
| 15A. API First: el contrato antes del código | 🟠 Replanteada: resuelta en la fase 33 con el contrato generado desde el código | [15A-api-first.md](https://github.com/fernandomguez/daw-movies/blob/jpa-fase-11-15a/docs/guia-estudio/15A-api-first.md) (rama `jpa-fase-11-15a`) |
| 11, 13 y 14 | Superadas por el giro a JDBC (D-48) | Ver el [plan](00-plan-de-aprendizaje.md) |
| 12 | Replanteada: puerto `MovieRepository` + DAO JDBC | Ver el [plan](00-plan-de-aprendizaje.md) |
| 15 a 20 | Pendientes de reordenar con el código ya existente | Ver el [plan](00-plan-de-aprendizaje.md) |
| 30 a 33 (bloque G) | ✅ Entender el código, tests (servicio y controlador), skill y Swagger | Ver el [plan](00-plan-de-aprendizaje.md) y [ESTADO.md](ESTADO.md) |
| 34 (bloque G) | ⏳ Monolito modular `catalog` | Ver el [plan](00-plan-de-aprendizaje.md) |
| *(antes)* 11 a 20 | Pendientes (la 13 se eliminó, D-29) | Ver el [plan](00-plan-de-aprendizaje.md) |

> Normas del proyecto: [base-standards.md](../../base-standards.md). Código en inglés; documentación y comentarios en español.
