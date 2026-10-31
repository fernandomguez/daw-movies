# Guía de estudio — Proyecto Películas (Java 25 + Spring Boot 4.1.1 + MariaDB + Flyway + Docker + springdoc)

Esta carpeta es la guía para repasar el proyecto más adelante. Explica **el proyecto real** que estamos construyendo, paso a paso, con los motivos de cada decisión.

## Cómo está organizada

| Archivo | Contenido |
|---|---|
| [00-plan-de-aprendizaje.md](00-plan-de-aprendizaje.md) | Hoja de ruta completa: fases, pasos, conceptos, resultados y dependencias. |
| [INDICE.md](INDICE.md) | **Mapa de toda la documentación**: cada fase y subfase con su estado y el archivo donde se explica. |
| [RECORRIDO.md](RECORRIDO.md) | **Viaje por la historia**: qué commit visitar en cada fase, qué mirar y qué necesita para funcionar. |
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
| 05 | [Arquitectura en tres capas e inversión de dependencias](temario/05-arquitectura-tres-capas-dip.md) | Completo (sección 11: diseño real con JPA en el dominio) |
| 06 | [Proyecto Spring Boot y Git](temario/06-proyecto-spring-boot-y-git.md) | Completo |
| 07 | [Conexión con la base de datos: JDBC, driver y `DataSource`](temario/07-conexion-jdbc-datasource.md) | Completo |
| 08 | [Diseño de los endpoints antes de crear las tablas](temario/08-diseno-de-endpoints.md) | Completo (paso 8.2) |
| 08 | [Migraciones del esquema con Flyway](temario/08-migraciones-flyway.md) | Completo (incluye cómo se activa en Spring Boot) |
| 09 | [Datos de prueba, `INSERT` y consultas con `JOIN`](temario/09-datos-de-prueba-y-consultas.md) | Completo |
| 10 | [Modelo de dominio y *value objects*](temario/10-modelo-de-dominio.md) | Completo (pasos 10.1 a 10.4) |
| 11 | [Introducción a JPA](temario/11-introduccion-jpa.md) | Completo como introducción; se pondrá a prueba con el código real en la fase 11 |
| 15A | [API First: el contrato antes del código](temario/15A-api-first.md) | Completo (fase 15A adelantada; falta 15A.3 en la fase 16) |

Cada archivo de fase sigue, cuando corresponde, esta estructura: objetivo, conceptos, por qué los necesitamos, archivos implicados, código explicado, recorrido de los datos, comandos y comprobaciones, errores y soluciones, decisiones, resumen y preguntas de repaso.

## Estado y progreso

| Para saber… | Abres |
|---|---|
| **Dónde estamos ahora** y qué toca después | [ESTADO.md](ESTADO.md) |
| El progreso **detallado, paso a paso**: cada fase, cada subfase y su estado | [INDICE.md](INDICE.md) |
| Qué commit visitar para ver una fase terminada | [RECORRIDO.md](RECORRIDO.md) |

El README **no guarda el progreso a propósito**: se quedaría desfasado cada sesión. La única fuente del estado es [ESTADO.md](ESTADO.md); el detalle acumulado de fases y subfases, el [índice](INDICE.md).

> Normas del proyecto: [base-standards.md](../../base-standards.md). Código en inglés; documentación y comentarios en español.
