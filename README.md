# DAW Movies — API de películas con Spring Boot (proyecto de aprendizaje)

Proyecto educativo de 2.º de DAW: una API REST de películas, directores y actores construida **paso a paso** con Java 25, Spring Boot 4.1.1, MariaDB 12.3 (en Docker) y Flyway.

El objetivo no es solo que funcione, sino **entender cada pieza y cada decisión**. Por eso el repositorio incluye una guía de estudio completa ([`docs/guia-estudio/`](docs/guia-estudio/README.md)) que cuenta qué se hizo, por qué, qué errores aparecieron y cómo se resolvieron. Si estás aprendiendo Spring Boot, puedes seguirla en orden.

> **Estado actual:** fases 1–7 completadas y **fase 8 completada** (pendiente solo el commit). El proyecto Spring Boot 4.1.1 arranca **conectado a MariaDB** (credenciales leídas de `.env`) y tiene 4 tests en verde. Flyway crea **las cuatro tablas** (`director`, `actor`, `movie`, `movie_actor`) desde `V1`–`V4`, con claves foráneas y `CHECK` comprobados. Siguiente: fase 9, datos de prueba.

---

## Índice

1. [Tecnologías y versiones](#1-tecnologías-y-versiones)
2. [Progreso por fases](#2-progreso-por-fases)
3. [Cómo arrancar el proyecto](#3-cómo-arrancar-el-proyecto)
4. [El dominio: películas, directores y actores](#4-el-dominio-películas-directores-y-actores)
5. [Arquitectura](#5-arquitectura)
6. [Paso a paso: lo que se ha hecho](#6-paso-a-paso-lo-que-se-ha-hecho)
7. [Errores encontrados y soluciones](#7-errores-encontrados-y-soluciones)
8. [Manejo de errores y excepciones (previsto)](#8-manejo-de-errores-y-excepciones-previsto)
9. [Convenciones](#9-convenciones)
10. [Decisiones técnicas](#10-decisiones-técnicas)
11. [Guía de estudio](#11-guía-de-estudio)
12. [Estructura del repositorio](#12-estructura-del-repositorio)
13. [Cómo se trabaja en este proyecto](#13-cómo-se-trabaja-en-este-proyecto)

---

## 1. Tecnologías y versiones

Versiones verificadas en fuentes oficiales en la fase 1 ([detalle](docs/guia-estudio/01-inspeccion-inicial.md)).

| Componente | Versión | Para qué |
|---|---|---|
| Java (Temurin) | 25 (LTS) | Lenguaje |
| Spring Boot | 4.1.1 | Framework: arranque, configuración, web, datos |
| Spring Framework | 7.0.9 | Lo gestiona Spring Boot |
| Hibernate | 7.4.5.Final | Implementación de JPA (traduce objetos ↔ filas) |
| Flyway | 12.4.0 (+ `flyway-mysql`) | Migraciones versionadas del esquema |
| MariaDB | 12.3 (LTS), imagen Docker `mariadb:12.3` | Base de datos |
| Driver MariaDB | 3.5.10 (`mariadb-java-client`) | Conexión JDBC |
| HikariCP | 7.0.2 | *Pool* de conexiones (`DataSource`) |
| Maven (vía Maven Wrapper) | 3.10.0 | Compilación, dependencias y tests (no hace falta instalarlo) |
| Docker + Docker Compose | — | Ejecutar MariaDB sin instalarla |
| Git | 2.51 | Control de versiones |

### Dependencias de Spring Boot

**Cada dependencia se añade en la fase que la necesita** (D-26): Spring Boot se configura solo según lo que encuentra en el proyecto, y una dependencia antes de tiempo provoca errores que aún no se pueden explicar. Nombres comprobados para Boot 4.1.1 (algunos cambiaron respecto a Boot 3; por ejemplo, `spring-boot-starter-web` ahora es `spring-boot-starter-webmvc`).

| Fase | Dependencias | Para qué | Estado |
|---|---|---|---|
| 6 | `spring-boot-starter`, `spring-boot-starter-test` | Núcleo de Spring Boot; JUnit 5, AssertJ y Mockito | ✅ |
| 7 | `mariadb-java-client`, `spring-boot-starter-data-jpa` (+ `-test`) | Driver JDBC; JPA, Hibernate, Spring Data y pool de conexiones | ✅ |
| 8 | `spring-boot-starter-flyway`, `flyway-mysql` (12.4.0, versión gestionada por Boot) | Migraciones; módulo de Flyway para MySQL/MariaDB | ✅ |
| 16 | `spring-boot-starter-webmvc` (+ `-test`) | Controladores REST, Tomcat embebido, JSON | ⏳ |
| 18 | `spring-boot-starter-validation` | Bean Validation (`@NotBlank`, `@Valid`) | ⏳ |
| 19 | `spring-boot-testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-mariadb` | Tests de integración con un MariaDB real y desechable | ⏳ |
| 24 (opc.) | `spring-boot-starter-security` + librería JWT | Seguridad | ⚪ |

**Descartadas** (D-27): Lombok (oculta el código que queremos aprender; usaremos `record`), DevTools, Docker Compose Support (haría sola la configuración que se aprende en la fase 7), H2 (su SQL no es el de MariaDB), Actuator. Explicación completa: [temario 06, «Referencia: plan de dependencias»](docs/guia-estudio/temario/06-proyecto-spring-boot-y-git.md#referencia-plan-de-dependencias).

---

## 2. Progreso por fases

El plan completo, con pasos, conceptos y dependencias, está en [00-plan-de-aprendizaje.md](docs/guia-estudio/00-plan-de-aprendizaje.md).

| Bloque | Fase | Estado |
|---|---|---|
| A. Preparación | 1. Inspección inicial y herramientas | ✅ |
| | 2. Alcance del dominio y datos de prueba | ✅ |
| | 3. Diseño del esquema relacional | ✅ |
| B. Base de datos | 4. Docker, Docker Compose y MariaDB | ✅ |
| | 5. Arquitectura en tres capas e inversión de dependencias | ✅ |
| | 6. Creación del proyecto Spring Boot | ✅ |
| | 7. Conexión entre Java y MariaDB | ✅ |
| | 8. Flyway: migraciones del esquema | ✅ Completada (pendiente el commit de la fase) |
| | 9. Datos de prueba y comprobación de relaciones | ⏳ |
| C. Java y persistencia | 10. Modelo de dominio | ⏳ |
| | 11. Fundamentos de JPA y entidades | ⏳ |
| | 12. Repositorios y adaptadores de persistencia | ⏳ |
| | ~~13. Mapeos entre persistencia y dominio~~ | Eliminada (D-29) |
| | 14. Consultas de información relacionada y problemas habituales | ⏳ |
| D. API | 15. DTOs de salida y mapeo dominio → DTO | ⏳ |
| | 16. Servicio y controladores REST de lectura | ⏳ |
| | 17. Operaciones de escritura y transacciones | ⏳ |
| | 18. Validaciones y manejo de excepciones | ⏳ |
| E. Calidad y cierre | 19. Pruebas de integración y revisión de cobertura | ⏳ |
| | 20. Revisión de arquitectura y documentación final | ⏳ |
| Opcionales | 21. Contenerizar la aplicación · 22. MapStruct · 23. Personaje en `movie_actor` · 24. Spring Security + JWT | ⚪ |

---

## 3. Cómo arrancar el proyecto

La base de datos se arranca con Docker. La aplicación se conecta a ella leyendo las credenciales del mismo `.env` ([D-32](docs/guia-estudio/decisiones-tecnicas.md)), así que **MariaDB tiene que estar arrancado** antes de ejecutar los tests o la aplicación.

### Requisitos

- Docker Desktop **abierto** (si no, `docker` falla con `failed to connect to the docker API`).
- Git.
- JDK 25. Maven no hace falta: el proyecto incluye el Maven Wrapper (`mvnw.cmd`).

### Pasos

```powershell
# 1. Clonar
git clone git@github.com:fernandomguez/daw-movies.git
cd daw-movies

# 2. Crear el archivo de credenciales a partir de la plantilla y cambiar los "change_me"
copy .env.example .env

# 3. Arrancar MariaDB
docker compose up -d

# 4. Comprobar que está "healthy"
docker compose ps
```

Resultado esperado:

```
NAME               IMAGE          STATUS                    PORTS
movies-mariadb-1   mariadb:12.3   Up 10 seconds (healthy)   0.0.0.0:3307->3306/tcp
```

### Variables de `.env`

| Variable | Para qué |
|---|---|
| `MARIADB_ROOT_PASSWORD` | Contraseña de `root` (solo administración) |
| `MARIADB_DATABASE` | Base de datos de la aplicación (`movies`) |
| `MARIADB_USER` / `MARIADB_PASSWORD` | Usuario de la aplicación (`movies_app`), con permisos solo sobre `movies` |
| `MARIADB_PORT` | Puerto en tu ordenador (por defecto `3307`) |

> **¿Por qué 3307 y no 3306?** En el equipo de desarrollo, el 3306 lo ocupa un MariaDB instalado en Windows. Si tu 3306 está libre, puedes cambiar `MARIADB_PORT`.

### Compilar, ejecutar los tests y arrancar la aplicación

```powershell
docker compose up -d          # los tests se conectan a MariaDB: tiene que estar arrancado (D-35)
.\mvnw.cmd test               # compila y ejecuta los tests
.\mvnw.cmd spring-boot:run    # arranca la aplicación (se conecta y termina: aún no hay servidor web)
.\mvnw.cmd clean              # borra target/
```

Resultado esperado de `test`:

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.daw.movies.DatabaseConnectionTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in com.daw.movies.FlywayMigrationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in com.daw.movies.MoviesApplicationTests
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Si MariaDB no está arrancado, los 4 tests fallan con `Connection refused` en `localhost:3307`.

Dos `WARN` de Flyway son esperados (fase 8): `Using MariaDB 12.3 which is newer than the version Flyway has been verified with. The latest verified version of MariaDB is 11.7` ([D-04](docs/guia-estudio/decisiones-tecnicas.md)) y `No migrations found` mientras no existan archivos en `src/main/resources/db/migration`.

Los avisos `WARNING` de Mockito ("self-attaching", "Dynamic loading of agents") son conocidos y no rompen nada (D-31).

### Comandos útiles

```powershell
docker compose down        # para y borra el contenedor (los datos se conservan en el volumen)
docker compose down -v     # ⚠️ borra también el volumen: se pierden los datos

# Ejecutar SQL: en PowerShell, pásalo por la entrada estándar (ver error 3 en la sección 7)
"SELECT VERSION();" | docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" movies'
```

---

## 4. El dominio: películas, directores y actores

| Entidad | Atributos | Relación |
|---|---|---|
| Director | nombre, apellidos, fecha de nacimiento | Dirige **varias** películas (1:N) |
| Película | título, año de estreno, duración en minutos | Tiene **un** director y **varios** actores |
| Actor | nombre, apellidos, fecha de nacimiento | Participa en **varias** películas (N:M) |

Datos de prueba: 10 películas reales, 5 directores y 14 actores, con actores compartidos entre películas para que las relaciones N:M se vean de verdad.

### Esquema relacional (diseñado en la fase 3; se creará con Flyway en la fase 8)

```
director (id PK, first_name, last_name, birth_date)
    1
    │
    N
movie (id PK, title, release_year, duration_minutes, director_id FK → director.id)
    1
    │
    N
movie_actor (movie_id PK/FK → movie.id, actor_id PK/FK → actor.id)
    N
    │
    1
actor (id PK, first_name, last_name, birth_date)
```

Reglas que protege la base de datos:

- Claves primarias `BIGINT AUTO_INCREMENT` (claves sustitutas).
- Todas las columnas obligatorias (`NOT NULL`).
- `CHECK (duration_minutes > 0)` y `CHECK (release_year >= 1888)`.
- No se puede borrar un director con películas (`ON DELETE RESTRICT`); al borrar una película se borran sus participaciones (`CASCADE`).
- `utf8mb4` con `utf8mb4_uca1400_ai_ci` (tildes y eñes: *Amenábar*, *Verdú*).

Explicación completa: [temario/02](docs/guia-estudio/temario/02-entidades-atributos-y-relaciones.md) y [temario/03](docs/guia-estudio/temario/03-diseno-esquema-relacional.md).

---

## 5. Arquitectura

Tres capas, con las dependencias apuntando **hacia el dominio** (principio de inversión de dependencias, DIP):

| Capa | Responsabilidad | Qué contiene | Qué usa |
|---|---|---|---|
| **Controlador** | Entrada y salida HTTP/JSON. Solo traduce. | `MovieController`, DTOs (`CreateMovieRequest`, `MovieResponse`), `MovieDtoMapper` | Spring Web, Bean Validation (en los DTOs) |
| **Dominio** | Modelo, reglas de negocio y casos de uso | `Movie`, `Director`, `Actor` (con anotaciones JPA), `MovieService` (`@Service`), puerto `MovieRepository` | JPA (`jakarta.persistence`) y `@Service` (*) |
| **Persistencia** | Guardar y recuperar | `MovieJpaRepository` (Spring Data), `MovieRepositoryAdapter` (implementa el puerto) | Spring Data JPA |

(*) **Dominio con anotaciones JPA y `@Service`** (D-19, D-29). El diseño inicial de la fase 5 dejaba el dominio en Java puro, con una `MovieEntity` separada y un mapper entre ambas. En la fase 6 se eligió una versión más práctica: `Movie` es a la vez modelo de negocio y entidad JPA. JPA es un **estándar de Jakarta EE, no Spring**. El dominio sigue **sin** depender de Spring Web, Spring Data ni Jackson, y la API sigue **sin** devolver entidades (usa DTOs). Ojo: **Spring Web y Jackson sí se usan en el proyecto**, pero en el controlador. Jackson convierte los DTOs en JSON y viceversa. Como nunca se devuelven entidades, las clases del dominio no necesitan anotaciones de Jackson (`@JsonIgnore`, `@JsonManagedReference`...) para evitar referencias circulares (paso 14.5). Comparación de las dos versiones: [temario/05, secciones 3 y 11](docs/guia-estudio/temario/05-arquitectura-tres-capas-dip.md#11-cambio-de-diseño-dominio-con-jpa-y-service-d-19-y-d-29).

**La película tiene dos formas**, y un mapper la convierte al llegar al controlador:

```
fila de MariaDB ──Hibernate──► Movie ──MovieDtoMapper──► MovieResponse ──Spring──► JSON
                               (dominio + entidad JPA)   (controlador)
```

Estructura de paquetes prevista (cada paquete se crea cuando hace falta):

```
com.daw.movies
├── MoviesApplication.java
├── controller/        (dto/, mapper/)
├── domain/            (model/ = clases con JPA, repository/ = puertos, service/ = @Service)
└── persistence/       (repository/ = Spring Data, adapter/)
```

Explicación completa, con el recorrido de `GET /movies/3` y `POST /movies` paso a paso: [temario/05](docs/guia-estudio/temario/05-arquitectura-tres-capas-dip.md).

---

## 6. Paso a paso: lo que se ha hecho

Cada fase tiene su documento con comandos y resultados reales.

| Fase | Qué se hizo | Documento |
|---|---|---|
| 1 | Leer las normas, inventariar el repositorio, comprobar herramientas y verificar versiones en fuentes oficiales | [01](docs/guia-estudio/01-inspeccion-inicial.md) |
| 2 | Identificar entidades, atributos y relaciones; preparar datos de prueba reales | [02](docs/guia-estudio/02-alcance-del-dominio.md) |
| 3 | Diseñar tablas, claves, tipos, restricciones y comportamiento al borrar; borrador del SQL | [03](docs/guia-estudio/03-diseno-del-esquema.md) |
| 4 | `compose.yaml` con MariaDB 12.3, credenciales en `.env`, volumen persistente, healthcheck; comprobar usuario, charset, motor InnoDB y que los datos sobreviven a `docker compose down` | [04](docs/guia-estudio/04-docker-y-mariadb.md) |
| 5 | Diseñar la arquitectura en tres capas con DIP, reglas de dependencia y paquetes | [05](docs/guia-estudio/05-arquitectura.md) |
| 6.1 | `git init`, `.gitignore` (secretos, `target/`, IDE), `.gitattributes` (finales de línea LF), subida a GitHub y reorganización del historial en un commit por fase | [06](docs/guia-estudio/06-creacion-proyecto-spring-boot.md) |
| 6.2–6.3 | Generar el proyecto con Spring Initializr (Boot 4.1.1, Java 25, `com.daw.movies`) en una carpeta temporal, revisarlo y copiar solo lo útil; dependencias mínimas y plan del resto | [06](docs/guia-estudio/06-creacion-proyecto-spring-boot.md) |
| 6.4–6.5 | Estructura Maven, ciclo de vida, primer `.\mvnw.cmd test` en verde y primer arranque con `spring-boot:run` | [06](docs/guia-estudio/06-creacion-proyecto-spring-boot.md) |
| 7.1 | Añadir el driver de MariaDB y Spring Data JPA; el test se pone en rojo a propósito (`'url' attribute is not specified`) | [07](docs/guia-estudio/07-conexion-java-mariadb.md) |
| 7.2 | `application.yaml` con la URL y las credenciales leídas de `.env` (`spring.config.import`); contraseña fuera del log | [07](docs/guia-estudio/07-conexion-java-mariadb.md) |
| 7.3 | `ddl-auto: validate` con TDD (rojo → verde): Hibernate no toca el esquema | [07](docs/guia-estudio/07-conexion-java-mariadb.md) |
| 7.4 | Test de conexión (MariaDB, base `movies`, usuario `movies_app`) y comprobación de que puede fallar | [07](docs/guia-estudio/07-conexion-java-mariadb.md) |
| 8.1–8.2 | Teoría de migraciones; diseño de los 4 endpoints de la v1 y revisión del esquema (nace la columna `biography`, D-37) | [08](docs/guia-estudio/08-flyway-migraciones.md) |
| 8.3 | Test del rojo, dependencias `spring-boot-starter-flyway` + `flyway-mysql` (resuelven 12.4.0) y verde: Flyway crea `flyway_schema_history`; se cierra el riesgo de D-04 | [08](docs/guia-estudio/08-flyway-migraciones.md) |
| 8.4 | Primera migración `V1__create_director_table.sql`: tabla `director` creada, apuntada en el historial y con `utf8mb4_uca1400_ai_ci` heredado de la base | [08](docs/guia-estudio/08-flyway-migraciones.md) |
| 8.5 | `V2`–`V4`: `actor`, `movie` (FK RESTRICT + 2 `CHECK`) y `movie_actor` (PK compuesta, CASCADE/RESTRICT); verificado en `information_schema` con esquema en v4 | [08](docs/guia-estudio/08-flyway-migraciones.md) |
| 8.6 | Inmutabilidad: se editó a propósito una migración aplicada, se leyó el `checksum mismatch`, se revirtió y volvió el verde | [08](docs/guia-estudio/08-flyway-migraciones.md) |

---

## 7. Errores encontrados y soluciones

| # | Fase | Síntoma | Causa | Solución |
|---|---|---|---|---|
| 1 | 1 y 4 | `failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine` | El comando `docker` existe, pero el motor (Docker Desktop) no estaba arrancado | Abrir Docker Desktop y esperar a que `docker info` responda |
| 2 | 4 | Puerto 3306 ocupado por `mysqld` | MariaDB instalado como servicio de Windows | Publicar el contenedor en el 3307 (`"3307:3306"`), sin tocar el servicio ([D-15](docs/guia-estudio/decisiones-tecnicas.md)) |
| 3 | 4 | `ERROR 1064 (42000) ... near ''`: MariaDB solo recibía `SELECT` | El SQL iba entre comillas dentro de otro comando entre comillas; al pasar por PowerShell → Docker → `sh` se perdían | Enviar el SQL por la entrada estándar: `$sql \| docker compose exec -T mariadb ...` |
| 4 | 4 | El asistente de IA no puede crear ni leer `.env*` | Regla de permisos que protege archivos con secretos | El alumno crea `.env` y `.env.example` a mano |
| 5 | 6 | Aviso `CRLF will be replaced by LF` en `.env.example` al hacer commit | No es un error: `.gitattributes` normaliza a LF un archivo creado en Windows | Ninguna; es el comportamiento buscado ([D-24](docs/guia-estudio/decisiones-tecnicas.md)) |
| 6 | 6 | Se documentó que `core.autocrlf` "no estaba configurado", pero Git avisaba de conversiones LF/CRLF | Solo se había consultado `git config --global`; el valor `true` venía del nivel de sistema (instalador de Git para Windows) | Consultar todos los niveles con `git config --show-origin --get-all core.autocrlf`. `.gitattributes` tiene prioridad, así que D-24 sigue siendo válida |
| 7 | 7 | `Failed to configure a DataSource: 'url' attribute is not specified` | Spring Data JPA en el proyecto sin URL de conexión | Configurar `spring.datasource.url` en `application.yaml`. Se provocó a propósito para verlo |
| 8 | 7 | `Socket fail to connect to localhost:3307. Connection refused` y `Unable to determine Dialect` | Docker Desktop apagado: nadie escucha en el 3307 | Abrir Docker Desktop y `docker compose up -d --wait` |
| 9 | 7 | La contraseña de la base de datos aparecía en claro en el log de arranque | Hibernate (`HHH10001005: Database info`) escribe la URL que devuelve el driver, con las credenciales | Subir el logger `org.hibernate.orm.connections.pooling` a `WARN` ([D-33](docs/guia-estudio/decisiones-tecnicas.md)) |
| 10 | 8 | `failed to connect to the docker API` otra vez, y el test de Flyway sin ejecutar | Docker Desktop cerrado (la sesión anterior se cortó ahí) | Abrirlo; `docker info` responde en segundos y `docker compose up -d --wait` deja MariaDB `healthy` |
| 11 | 8 | `Could not transfer metadata ... HTTP Status: 401` de `flyway-community-db-support` y `MavenFeed` | Los `pom.xml` de Flyway declaran repositorios extra (GitHub Packages, Azure) que Maven consulta; sin credenciales responden 401 | Ninguna: es un `WARNING` de Maven, `BUILD SUCCESS` |
| 12 | 8 | `ERROR 1142 ... SELECT command denied ... 'mysql'.'user'` | `movies_app` no puede leer la tabla de usuarios | Es correcto (mínimo privilegio, D-17): para ver usuarios, usar `root` |
| 13 | 8 | `DB: Name 'pk_director' ignored for PRIMARY key. (Error Code: 1280)` al aplicar la `V1` | MariaDB no admite nombre para la clave primaria: siempre se llama `PRIMARY` | Ninguna: aviso único, la tabla se crea. Decisión en D-41 |
| 14 | 8 | `Validate failed: Migrations have failed validation` / `Migration checksum mismatch for migration version 1` | Se añadió un comentario a una migración ya aplicada: el checksum cambia aunque el esquema no | Revertir el archivo. `flyway repair` lo taparía; solo para cambios inofensivos y coordinados |
| 15 | 8 | Un `.sql` con tildes quedó con caracteres rotos y BOM (`EF BB BF`) al editarlo | `Get-Content`/`Set-Content` de PowerShell 5.1 leen UTF-8 como ANSI y escriben con BOM | Editar con el editor. Por consola: `[System.IO.File]::ReadAllText` + `WriteAllText(..., UTF8Encoding($false))` |

---

## 8. Manejo de errores y excepciones (previsto)

Se implementará en la fase 18. El diseño ya está decidido en la arquitectura:

| Situación | Quién la detecta | Qué pasa | Respuesta HTTP |
|---|---|---|---|
| JSON con formato incorrecto (título vacío...) | Controlador (`@Valid` sobre el DTO de entrada) | No llega al dominio | 400 Bad Request |
| La película pedida no existe | Dominio (`MovieNotFoundException`) | El adaptador devuelve `Optional.empty()`; el dominio decide que es un error | 404 Not Found |
| Regla de negocio incumplida (duración ≤ 0, director inexistente) | Dominio (constructor de `Movie`, `MovieService`) | Se lanza una excepción de dominio | Por decidir en la fase 18 |
| Restricción de la base de datos (FK, `CHECK`) | MariaDB (`ERROR 1452`, `ERROR 4025`...) | Última barrera: el dominio debería haberlo evitado antes | Por decidir en la fase 18 |

**Regla de reparto:** el dominio decide *qué* es un error; el controlador decide *cómo* se cuenta en HTTP. Los mensajes de error irán en español.

---

## 9. Convenciones

Definidas en [base-standards.md](base-standards.md) y en las decisiones del proyecto.

### Idioma

| En inglés | En español |
|---|---|
| Clases, métodos, variables, paquetes | Comentarios explicativos del código |
| Tablas y columnas SQL (`movie`, `release_year`) | Documentación y README |
| Nombres de los tests | Mensajes de error y logs |
| Mensajes de commit | |

### Nombres

| Tipo | Convención | Ejemplo |
|---|---|---|
| Tablas | Singular, `snake_case` | `movie`, `movie_actor` |
| Columnas | `snake_case`, con unidad si aplica | `duration_minutes`, `director_id` |
| Restricciones | Prefijo + tabla + detalle | `pk_movie`, `fk_movie_director`, `chk_movie_duration_positive` |
| Modelo de dominio / entidad JPA | Nombre del concepto, sin sufijo (D-29) | `Movie` |
| DTOs | `Request` / `Response` | `CreateMovieRequest`, `MovieResponse` |
| Puerto del dominio | Sufijo `Repository` | `MovieRepository` |
| Repositorio de Spring Data | Sufijo `JpaRepository` | `MovieJpaRepository` |
| Implementación del puerto | Sufijo `Adapter` (no `Impl`, ver D-22) | `MovieRepositoryAdapter` |
| Mappers | Sufijo `Mapper` | `MovieDtoMapper` (dominio ↔ DTO) |
| Paquete base | — | `com.daw.movies` |

### Git

- **Un commit por fase**, con etiqueta anotada `fase-NN` (D-30).
- Mensajes con [Conventional Commits](https://www.conventionalcommits.org/) y la fase como ámbito: `feat(fase-07): connect Spring Boot to MariaDB`.
- Nunca se versionan secretos (`.env`) ni archivos generados (`target/`).
- Finales de línea LF en el repositorio (`.gitattributes`).

### Desarrollo

- **Pasos pequeños:** un paso cada vez, comprobado antes de seguir.
- **TDD:** cuando haya lógica Java, primero el test.
- **Sin clases "por si acaso":** cada clase y cada paquete se crean cuando hacen falta.

---

## 10. Decisiones técnicas

Resumen. Cada decisión, con sus alternativas y su motivo, está en [decisiones-tecnicas.md](docs/guia-estudio/decisiones-tecnicas.md).

| ID | Decisión | Estado |
|---|---|---|
| D-01 | Java 25, Spring Boot 4.1.1 y Maven | ✅ |
| D-02 | Código en inglés; todo lo demás en español | ✅ |
| D-03 | Tests desde la fase 6 (TDD); integración en la fase 19 | ✅ |
| D-04 | MariaDB 12.3 (riesgo: Flyway verifica hasta la 11.7; se revisa en la fase 8) | ✅ |
| D-05 | Guía de estudio: un archivo por fase + temario + decisiones + glosario | ✅ |
| D-06 | Esquema antes que Docker; dominio antes que JPA | ✅ |
| D-07 | Alcance: `director`, `movie`, `actor`, `movie_actor` | ✅ |
| D-08 | Solo aplica `base-standards.md` | ✅ |
| D-09 | Año (no fecha) de estreno, duración en minutos, nombre y apellidos separados, fecha de nacimiento (no edad), id propio | ✅ |
| D-10 | Fecha de nacimiento obligatoria | ✅ |
| D-11 | Director y actor en tablas separadas (no `person`) | ✅ |
| D-12 | `BIGINT`, `INT`, `DATE` (correspondencia con `Long`, `Integer`, `LocalDate`) | ✅ |
| D-13 | `ON DELETE`: `RESTRICT` para directores y actores; `CASCADE` para participaciones | ✅ |
| D-14 | PK compuesta en `movie_actor` | ✅ |
| D-15 | MariaDB en el puerto 3307 | ✅ |
| D-16 | `utf8mb4` + `utf8mb4_uca1400_ai_ci` explícitos | ✅ |
| D-17 | Credenciales en `.env`; usuario `movies_app` con mínimo privilegio | ✅ |
| D-18 | Paquetes organizados por capa | ✅ |
| D-19 | Servicios con `@Service` | ✅ |
| D-20 | Repositorio (puerto + adaptador) en lugar de DAO | ✅ |
| D-21 | ¿Servicio como interfaz + `Impl`? | ⏳ Fase 16 |
| D-22 | `MovieRepositoryAdapter` en lugar de `MovieRepositoryImpl` | ✅ |
| D-23 | Spring Security + JWT como fase opcional 24 | ✅ |
| D-24 | Finales de línea con `.gitattributes` | ✅ |
| D-25 | Paquete base `com.daw.movies` | ✅ |
| D-26 | Cada dependencia se añade en la fase que la necesita | ✅ |
| D-27 | Sin Lombok, DevTools, Docker Compose Support, H2 ni Actuator | ✅ (revisable) |
| D-28 | Proyecto generado con Initializr y revisado antes de copiarlo; Maven Wrapper | ✅ |
| D-29 | Anotaciones JPA en las clases del dominio (sin `MovieEntity` ni su mapper); la fase 13 se elimina | ✅ |
| D-30 | Un commit y una etiqueta por fase; historial reescrito (fases 1–5 en un commit) | ✅ |
| D-31 | Aviso de Mockito "self-attaching" con Java 25: se deja de momento | ⏳ Cuando usemos Mockito |
| D-32 | Spring lee las credenciales de `.env` con `spring.config.import`; una variable de entorno real tiene prioridad | ✅ |
| D-33 | Logger `org.hibernate.orm.connections.pooling` a `WARN` para que la contraseña no salga en el log | ✅ |
| D-34 | `ddl-auto: validate`: el esquema es de Flyway; Hibernate solo lo comprueba | ✅ |
| D-35 | Los tests necesitan MariaDB arrancado (hasta Testcontainers, fase 19) | ✅ Provisional |
| D-36 | Endpoints de la v1: `GET /movies`, `/movies/{id}`, `/directors/{id}`, `/directors/{id}/movies` | ✅ |
| D-37 | `biography TEXT NULL` en `director` y `actor` | ✅ |
| D-38 | Orden por defecto de las listas (título; año y título; apellido y nombre) | ✅ |
| D-39 | Una lista vacía es `200 []`, no un error | ✅ |
| D-40 | Flyway con la versión que gestiona Spring Boot (12.4.0), no la última | ✅ |
| D-41 | Nombre `pk_...` en la clave primaria: MariaDB lo ignora (aviso 1280), se conserva en el SQL | ✅ |

---

## 11. Guía de estudio

Todo está en [`docs/guia-estudio/`](docs/guia-estudio/README.md):

| Documento | Contenido |
|---|---|
| [00-plan-de-aprendizaje.md](docs/guia-estudio/00-plan-de-aprendizaje.md) | Hoja de ruta: fases, pasos, conceptos y dependencias |
| `NN-*.md` | Lo que se hizo en cada fase: comandos, resultados reales y errores |
| [temario/](docs/guia-estudio/temario/) | Los conceptos explicados con ejemplos de este proyecto, con preguntas de repaso |
| [decisiones-tecnicas.md](docs/guia-estudio/decisiones-tecnicas.md) | Cada decisión con sus alternativas y su motivo |
| [glosario.md](docs/guia-estudio/glosario.md) | Términos explicados con lenguaje sencillo |
| [ESTADO.md](docs/guia-estudio/ESTADO.md) | Parte de situación y bitácora: dónde estamos, qué toca ahora, entorno real y qué se hizo en cada sesión |

Temas disponibles:

| Nº | Tema |
|---|---|
| 02 | [Entidades, atributos y relaciones](docs/guia-estudio/temario/02-entidades-atributos-y-relaciones.md) |
| 03 | [Diseño del esquema relacional](docs/guia-estudio/temario/03-diseno-esquema-relacional.md) |
| 04 | [Docker, Docker Compose y MariaDB](docs/guia-estudio/temario/04-docker-compose-mariadb.md) |
| 05 | [Arquitectura en tres capas e inversión de dependencias](docs/guia-estudio/temario/05-arquitectura-tres-capas-dip.md) (incluye DAO vs. Repositorio, DTOs y mappers) |
| 06 | [Proyecto Spring Boot y Git](docs/guia-estudio/temario/06-proyecto-spring-boot-y-git.md) |
| 07 | [Conexión con la base de datos: JDBC, driver y `DataSource`](docs/guia-estudio/temario/07-conexion-jdbc-datasource.md) |
| 11 | [Introducción a JPA](docs/guia-estudio/temario/11-introduccion-jpa.md) (adelanto) |

---

## 12. Estructura del repositorio

```
daw-movies/
├── README.md              ← este archivo
├── base-standards.md      ← normas del proyecto
├── claude.md              ← instrucciones para el asistente de IA que guía el proyecto
├── compose.yaml           ← MariaDB 12.3 en Docker
├── .env.example           ← plantilla de credenciales (el .env real no se versiona)
├── .gitignore
├── .gitattributes
├── pom.xml                ← proyecto Maven: Java 25, Spring Boot 4.1.1, dependencias
├── mvnw, mvnw.cmd, .mvn/  ← Maven Wrapper (Maven 3.10.0)
├── src/
│   ├── main/java/com/daw/movies/MoviesApplication.java   ← clase de arranque
│   ├── main/resources/application.yaml                    ← configuración (sin secretos: los lee de .env)
│   ├── main/resources/db/migration/                       ← migraciones Flyway (V1–V4: las cuatro tablas)
│   ├── test/java/com/daw/movies/MoviesApplicationTests.java
│   ├── test/java/com/daw/movies/DatabaseConnectionTests.java ← conexión a MariaDB y ddl-auto
│   └── test/java/com/daw/movies/FlywayMigrationTests.java   ← Flyway crea flyway_schema_history
└── docs/
    └── guia-estudio/      ← plan, fases, temario, decisiones y glosario
```

---

## 13. Cómo se trabaja en este proyecto

Es un proyecto **guiado**: el alumno lo construye con la ayuda de un asistente de IA (Claude Code), con estas reglas ([claude.md](claude.md)):

1. Un paso cada vez, y solo con permiso explícito del alumno.
2. Antes de tocar archivos, se explica el objetivo y los cambios.
3. Nunca se inventan resultados: los documentos recogen la salida real de los comandos, también los errores.
4. Cada fase se documenta mientras se hace (fase, temario, decisiones, glosario y este README).
5. **Un commit por fase** (D-30), solo cuando el alumno lo confirma, con una etiqueta `fase-NN`.

### Ver cómo crece el proyecto, fase a fase

Cada fase terminada tiene su etiqueta. Para ver el proyecto tal como estaba al final de una fase:

```powershell
git tag                      # fase-05, fase-06...
git checkout fase-05         # el proyecto al final de la fase 5 (aviso "detached HEAD": es normal)
git checkout main            # volver al presente
```

En GitHub se puede hacer lo mismo eligiendo la etiqueta en el selector de ramas. Las fases 1–5 están juntas en un commit, porque se hicieron antes de crear el repositorio.
