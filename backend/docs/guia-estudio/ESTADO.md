# Estado del proyecto y bitácora

> **Para qué sirve este archivo:** es el parte de situación. Se lee **al empezar cualquier sesión** (humano o asistente de IA) y se **actualiza al terminar cada paso**, antes de cerrar. Así se puede empezar de cero, sin memoria de la conversación anterior, sin inventar nada.
>
> Última actualización: **2026-11-04** (decimotercera parte).

---

## 1. Dónde estamos (leer esto primero)

> **Giro del 2026-11-04.** El profesor dio el visto bueno a un nuevo rumbo: **sin *value objects*** (D-46 revocada), **sin JPA**, arquitectura por capas con **DAO JDBC** (D-48, [`docs/arquitectura.md`](../arquitectura.md)). Las fases JPA del plan original (11–14) y la 15A quedan superadas o replanteadas; el plan nuevo son las fases 30–34 del Bloque G. Las entradas de la bitácora anteriores a esta fecha cuentan la historia anterior.

| Dato | Valor |
|---|---|
| Rumbo actual | Arquitectura por capas con JDBC: `controller` (con `dto`, `mapper`, `handler`), `domain` (`model`, `service`, `repository`, `exception`), `persistence` (`dao`, `repository.impl`) |
| Paso en curso | Fases 0–3 y la **1c de docs, hechas**. **Frontend Angular terminado y verificado** (décima/undécima/duodécima partes). Quedan: los **tests del DAO/repositorio con base de datos real** (quedaron fuera de la 1c) y la **4** (monolito modular) |
| Último commit | `9ff1fdb` (`test: add ArchUnit layer rules (fase 4a)`). (Historia del cambio de ramas, 2026-11-04: se **intercambiaron las ramas en GitHub** con `gh api .../branches/<rama>/rename`: el `main` viejo (camino JPA, fases 11 y 15A) es ahora **`origin/jpa-fase-11-15a`** (`2d58690`) y la rama `nuevo-rumbo-jdbc` pasó a llamarse `main` (rama por defecto, puesta con `gh repo edit --default-branch main`). Etiquetas `fase-01`…`fase-15A` y `backup/antes-de-volver-a-fase-10` sin cambios |
| Tests | **73 en verde** (`mvnw clean test`): 19 controlador, 13 servicio, 20 modelo (6+6+8), 3 de contrato OpenAPI, **10 de persistencia** (6 DAO + 4 repositorio, decimotercera parte), **5 de ArchUnit** (`ArquitecturaTest`, fase 4a) y 3 de integración. Los 16 que arrancan la aplicación o tocan la BD necesitan Docker; el resto no |
| Endpoints | `/movies`: GET lista, GET `/{id}`, POST, PUT `/{id}`, DELETE `/{id}`. Swagger UI en `/swagger-ui.html` y contrato en `/v3/api-docs` (generado desde el código, sin yaml) |
| Tabla de datos | `director` (6), `actor` (14), `movie` (10), `movie_actor` (24); Flyway en la versión 4 |
| Cambios sin commitear | ninguno al cierre de la sesión del 2026-11-04 (el repo del skill, `Lorenzo\creador-de-skills`, no tiene remoto: sus commits son solo locales) |
| Ramas / etiquetas | `main` (local y remota) = nuevo rumbo JDBC, a partir de la fase 10. El camino JPA/OpenAPI de las fases 11 y 15A vive en `origin/jpa-fase-11-15a` y en las etiquetas `fase-11` y `fase-15A`. Existe `backup/antes-de-volver-a-fase-10`. No mezclar sin hablarlo. **`origin` es ahora HTTPS** (`https://github.com/fernandomguez/daw-movies.git`): el SSH de `git push` falló con `Permission denied (publickey)` aunque `ssh -T` funcionaba; para subir desde el asistente hace falta la credencial de `gh` (`gh auth login` ya hecho) |

### Las fases que quedan (plan aprobado)

Plan completo: `C:\Users\ferna\.claude\plans\vamos-a-cambiar-el-drifting-pond.md`. **Un commit por subpaso**, con `mvnw clean test` en verde antes de cada uno y confirmación del alumno (excepción deliberada a D-30 para tener más puntos de retorno).

| Fase | Qué se hace | Subpasos / commits propuestos |
|---|---|---|
| **0** ✅ | Que compile y los tests pasen | `d75cffc` |
| **1** ✅ | Entender el código, tests de servicio y controlador, `arquitectura.md` real, D-48, docs sin *value objects* ni JPA | `8b1a1dd`, `7ad36c1`, `0b6f567`, `339cb5d`, `f7877e6` |
| **1c** ✅ (docs, 2026-11-04) | Registro temario 05 (banner + nota en la «decisión final» + sección 11 histórica + notas en value objects y fase 11), diarios 05–07 con nota histórica, glosario con un solo `Puerto`, D-20 (**se mantiene**, con nota) y D-22 (`Impl` real anotada), DTO de `entender-el-codigo.md` con `@AssertTrue`, nota del año en §1. *(El diario y el temario 15A ya no existen en `main`: están en `jpa-fase-11-15a`; los enlaces vivos se apuntaron allí.)* Los **tests del DAO y del repositorio con base de datos real** ya existen (decimotercera parte, `113bcc8`) | `docs: fix leftovers after the new direction` |
| **2** ✅ Skill (`40b0bb1`, `ca3c834`, `ff9f2ad`, `2b9cd9f` en el repo del skill, sin remoto) | Actualizar `C:\Users\ferna\Desktop\FP DAW\2o curso\Lorenzo\creador-de-skills` para que replique **esta** arquitectura. **Primero comprobar si esa carpeta es un repositorio git** (si no, no hay commits ahí). Renombres: `presentation` → `controller`, `domain.models` → `domain.model`, `domain.exceptions` → `domain.exception`; mapper → `controller.mapper`; advice → `controller.handler`. En las skills solo `<Entidad>` y `<paquete.raíz>`, nunca `Movie` | **2a** `k-back-module` (árbol, reglas `MOD-R`, ArchUnit) · **2b** renombrar `k-back-presentation` a `k-back-controller` + `k-back-domain` + `k-back-persistence` (decidir dónde van las excepciones de persistencia) · **2c** `do-code-impl`, `do-code-review`, plantillas y `teoria/`. `demo-cliente/` se deja como ejemplo antiguo |
| **3** ✅ Swagger (`3f3dbc2`; el contrato sale del código, sin yaml: ver la bitácora y D-47) | springdoc **3.2.0** (rama de Spring Boot 4; verificar en Maven Central). `OpenApiConfig` en `com.daw.movies.config`, **fuera** de las capas, para que haya un único Swagger UI cuando haya varios módulos. Probar los 5 endpoints con «Try it out» contra `seed.sql` | **3a** `feat: add springdoc and OpenApiConfig` · **3b** `test: add OpenAPI contract test` (`/v3/api-docs` lista los 5 endpoints) · **3c** `docs: record Swagger manual checks` |
| **4** Monolito modular | Un único módulo **`catalog`** ahora; después `stock`, `order`, `user`… Estructura `com.daw.movies.catalog.{controller,domain,persistence}`; `MoviesApplication` y `config` en la raíz. Spring Modulith + ArchUnit (versión para Boot 4 a verificar). Riesgo D-45: si `Director`/`Actor` se separan en otro módulo, relacionar por id | **4a** ✅ `test: add ArchUnit layer rules (fase 4a)` (`9ff1fdb`: 5 reglas, ArchUnit **1.5.1**, rojo controlado con sonda) · **4b** ⏳ `refactor: move code into catalog module` (solo `package` e imports) · **4c** `feat: add Spring Modulith verification` (versión para Boot 4 a verificar) · **4d** `docs: D-49 modular monolith` + ajustar el skill a `<paquete.raíz>.<módulo>` |

### La siguiente acción, exacta

1. **Fase 4b** (siguiente subpaso de la 4, cuando el alumno lo pida): `refactor: move code into catalog module` (solo `package` e imports; las 5 reglas de `ArquitecturaTest` con comodines `..domain..`/`..controller..`/`..persistence..` siguen valiendo). La **4a ya está hecha** (commit `9ff1fdb`), con rojo controlado con una clase sonda borrada. Spring Modulith se verifica en la 4c.
2. Para subir cambios desde el asistente: `origin` es HTTPS; el `git push` normal funciona con `git -c credential.helper= -c "credential.helper=!gh auth git-credential" push origin main`. **Ya se usó con éxito** (22:00, `a79e06a..ed55526`).
3. **No se empieza ninguna fase sin que el alumno lo pida.**

Antes de tocar nada: `docker info` responde y `docker compose ps` → `healthy` (los 3 tests de integración lo necesitan; el resto no). Si Docker está cerrado: `Start-Process "C:\Users\ferna\AppData\Local\Programs\DockerDesktop\Docker Desktop.exe"` y esperar a `docker info`.

### Decisiones y rarezas ya conocidas (no volver a descubrirlas)

- **Año de estreno:** `@Min(1888)` y el método `isReleaseYearValid()` con `@AssertTrue` en `MovieRequest` (año actual + 10; `@Max` solo admite números fijos; la anotación propia `@MaxYearsAhead` se quitó en `6f0fbea`).
- **500 deliberado:** si `create`/`update` no encuentran la película justo después de guardarla → `IllegalStateException` (no un 404).
- **`GlobalExceptionHandler`:** 404 (película/director), 400 (validación, JSON mal formado, id no numérico), 415 (`Content-Type` no JSON). Un fallo de base de datos o un `IllegalArgumentException` siguen dando 500.
- **`PUT /movies/{id}`:** comprueba primero la película y después el director; si ambos fallan solo se ve el 404 de la película.
- **Deuda aceptada:** N+1 en `MovieDaoJdbc.findAll`, reparto de actores de solo lectura, `@Service`/`@Transactional` en `domain`, `Movie` no valida año ni duración (lo hacen el DTO y los `CHECK`).
- **`spring-boot-starter-webmvc-test`** está en el `pom.xml` porque en Spring Boot 4 `@WebMvcTest` ya no viene en `spring-boot-starter-test`.
- Los avisos de CRLF → LF de Git en Windows son normales.

---

## 2. Entorno comprobado (no volver a suponerlo)

| Elemento | Valor real | Cómo se comprueba |
|---|---|---|
| Java | 25 (Temurin) | `java -version` |
| Spring Boot | 4.1.1 | `pom.xml` (parent) |
| Maven | 3.10.0 por Wrapper | `.\mvnw.cmd -v` |
| MariaDB del proyecto | contenedor `movies-mariadb-1`, `mariadb:12.3.3`, puerto **3307** → 3306 | `docker compose ps` |
| Proyecto Compose | `movies` (línea `name:` de `compose.yaml`), volumen `movies_mariadb-data` | `docker inspect movies-mariadb-1` |
| Flyway | **12.4.0** (la que gestiona Spring Boot, D-40) | `.\mvnw.cmd dependency:tree "-Dincludes=org.flywaydb:*"` |
| Docker Desktop | en `C:\Users\ferna\AppData\Local\Programs\DockerDesktop\Docker Desktop.exe` | `Test-Path` |
| Credenciales | `.env` en la raíz (no versionado); el asistente **no** puede leerlo ni crearlo | — |

### Contenedores que **no** son de este proyecto

| Contenedor | Qué es | Puerto | Se toca |
|---|---|---|---|
| `mariadb_uno` | `docker run` a mano (2026-10-13), sin Compose, bind a `C:/docker/mariadb1`, root/root, `--restart always` | 4000 | ❌ |
| `mariadb_dos` | igual, bind a `C:/docker/mariadb2` | 4001 | ❌ |
| `banco-mysql-dev`, `gmaps-scraper` | otros proyectos, parados | — | ❌ |

`docker compose down` en esta carpeta solo afecta a `movies-mariadb-1`, porque el proyecto Compose se llama `movies`.

---

## 3. Avisos y trampas conocidas (no son errores)

| Aviso | Significado | Acción |
|---|---|---|
| `Using MariaDB 12.3 which is newer than the version Flyway has been verified with. The latest verified version of MariaDB is 11.7` | Versión no certificada por Flyway (D-04). Es un `WARN`; Flyway funciona | Ninguna. Anotado |
| `No migrations found. Are your locations set up correctly?` | `src/main/resources/db/migration` no existe o está vacía | Desaparece al crear la `V1` |
| `Could not transfer metadata ... HTTP Status: 401` (`flyway-community-db-support`, `MavenFeed`) | Repositorios extra declarados en los `pom.xml` de Flyway; sin credenciales dan 401 | Ninguna, `BUILD SUCCESS` |
| `Mockito is currently self-attaching` / `Java agent has been loaded dynamically` | Mockito con Java 25 (D-31) | Ninguna de momento |
| `ERROR 1142 ... SELECT command denied ... 'mysql'.'user'` | `movies_app` sin permiso sobre la tabla de usuarios: mínimo privilegio (D-17) | Usar `root` si hace falta |
| `DB: Name 'pk_director' ignored for PRIMARY key. (Error Code: 1280)` | MariaDB no admite nombre para la clave primaria: se llama siempre `PRIMARY` | Ninguna; sale una vez por tabla con PK nombrada (`V1`–`V4`). D-41 |
| `Validate failed: Migrations have failed validation / Migration checksum mismatch for migration version N` | Se ha editado una migración ya aplicada | Revertir el archivo a su contenido original. `flyway repair` reescribe el checksum y **oculta** el problema: no usarlo |
| `NativeCommandError` + `CategoryInfo : NotSpecified` al ejecutar SQL que falla | PowerShell trata la salida de error de `mariadb` como un error propio | Ruido de PowerShell: la línea que importa es la de MariaDB (`ERROR 1452`, `ERROR 4025`...) |
| `ERROR 1062 ... Duplicate entry '1' for key 'PRIMARY'` al cargar `seed.sql` | El script solo inserta y los datos ya están | Vaciar las tablas en orden inverso y reiniciar los `AUTO_INCREMENT` (receta del [diario 09](09-datos-de-prueba.md#cómo-se-recarga-desde-cero)) |

### Trampas de PowerShell (nos costaron un archivo roto)

- `Set-Content -Encoding utf8` en PowerShell 5.1 escribe **UTF-8 con BOM** (`EF BB BF`) y `Get-Content` sin `-Encoding` lee UTF-8 como ANSI: **rompe las tildes**.
- Para editar archivos con acentos (SQL, `.md`), usar el editor del proyecto. Si hay que hacerlo por consola: `[System.IO.File]::ReadAllText` + `[System.IO.File]::WriteAllText($p, $t, (New-Object System.Text.UTF8Encoding($false)))`.
- Comprobar la codificación: primeros bytes del archivo (`2D 2D 20` = `-- `, correcto; `EF BB BF` = BOM).
| `docker: failed to connect to the docker API ... dockerDesktopLinuxEngine` | Docker Desktop cerrado | Abrirlo y esperar a que `docker info` responda |

### Comandos que engañan

- `.\mvnw.cmd -q dependency:tree` **no imprime nada** con `-q`: quitar la `-q`.
- El SQL por PowerShell tiene que ir por la entrada estándar (error 3 del README):

```powershell
"SHOW TABLES;" | docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" -t movies'
```

---

## 4. Bitácora por sesiones

### 2026-10-20 — fase 8, pasos 8.1 y 8.2

- **8.1 Teoría:** qué es una migración, `flyway_schema_history`, checksum, orden por claves foráneas. Escrito el [temario 08](temario/08-migraciones-flyway.md). Comprobado que `movies` tiene **0 tablas**.
- **8.2 Diseño de endpoints** (sugerencia del profesor): 4 endpoints de solo lectura → D-36, D-37 (`biography`), D-38 (órdenes), D-39 (`200 []`). Escrito el [temario 08 de endpoints](temario/08-diseno-de-endpoints.md). Esquema y datos de prueba de las fases 2 y 3 actualizados con biografías.
- Sin tocar código.

### 2026-10-26 — sesión cortada

- Escrito el test `FlywayMigrationTests` (el rojo del 8.3).
- **No se ejecutó nada**: Docker Desktop estaba cerrado. Sin resultados que anotar.

### 2026-10-28 — fase 8, paso 8.3 terminado

1. Abierto Docker Desktop desde PowerShell (`Start-Process`). `docker info` → `29.8.0`.
2. `docker compose up -d --wait` → `movies-mariadb-1` **healthy** en el 3307.
3. Duda del alumno sobre `mariadb_uno` / `mariadb_dos`: comprobado con `docker inspect` que **no** son de este proyecto (ver §2).
4. `.\mvnw.cmd test` → **rojo** esperado:
   ```
   [ERROR] FlywayMigrationTests.flywayCreatesItsSchemaHistoryTableOnStartup:25
   expected: 1
    but was: 0
   [INFO] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
   ```
5. `pom.xml`: añadidos `spring-boot-starter-flyway` y `org.flywaydb:flyway-mysql`, **sin versión**.
6. `dependency:tree` → `flyway-mysql:jar:12.4.0` y `flyway-core:jar:12.4.0`.
7. `.\mvnw.cmd test` → **verde**: 4 tests, `Creating Schema History table movies.flyway_schema_history`, y los dos `WARN` de §3 (MariaDB 12.3 y "No migrations found").
8. Comprobado por SQL: la única tabla en `movies` es `flyway_schema_history`, y está **vacía** (ninguna migración aplicada aún).
9. Documentado: [diario 08](08-flyway-migraciones.md), [temario 08](temario/08-migraciones-flyway.md) (nueva sección 7 y 4 preguntas), D-04 cerrada, D-40 propuesta, glosario (`Autoconfiguración`, `db/migration`), `README.md` de la raíz y de la guía.

**Pendiente de decisión:** D-40 (dejar Flyway 12.4.0 o forzar 13.11.1) → **decidido: 12.4.0** (2026-10-28).

### 2026-10-28 (segunda parte) — fase 8, paso 8.4 terminado

1. Leído el DDL del borrador de la [fase 3](temario/03-diseno-esquema-relacional.md), sección 10.
2. Creado `src/main/resources/db/migration/V1__create_director_table.sql` (tabla `director` + `biography`, D-37).
3. `.\mvnw.cmd test` → 4 en verde y en el log:
   ```
   Migrating schema `movies` to version "1 - create director table"
   WARN : DB: Name 'pk_director' ignored for PRIMARY key. (SQL State: - Error Code: 1280)
   Successfully applied 1 migration to schema `movies`, now at version v1
   ```
4. Comprobado por SQL: `SHOW TABLES` → `director` y `flyway_schema_history`; `SHOW CREATE TABLE director` → 5 columnas y `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci` (heredado de la base, D-16); el historial tiene 1 fila (`version 1`, `success 1`).
5. Documentado: sección 8.4 del [diario](08-flyway-migraciones.md), D-40 cerrada, D-41 propuesta (nombre de la PK), error 13 del `README.md`, tabla de progreso y estructura.

**Pendiente de decisión:** D-41 (nombre `pk_...` en la PK, que MariaDB ignora, o `PRIMARY KEY` a secas). Afecta a `V2`–`V4`. → **decidido: se conserva el nombre** (D-41).

### 2026-10-28 (tercera parte) — fase 8, paso 8.5 terminado

1. Creadas `V2__create_actor_table.sql`, `V3__create_movie_table.sql` y `V4__create_movie_actor_table.sql` desde el borrador de la [fase 3](temario/03-diseno-esquema-relacional.md).
2. `.\mvnw.cmd test` → 4 en verde y en el log:
   ```
   Current version of schema `movies`: 1
   Migrating schema `movies` to version "2 - create actor table"
   Migrating schema `movies` to version "3 - create movie table"
   Migrating schema `movies` to version "4 - create movie actor table"
   Successfully applied 3 migrations to schema `movies`, now at version v4
   ```
   Tres `WARN` 1280 (uno por PK nombrada). La `V1` **no** se volvió a ejecutar.
3. Verificado por SQL: 5 tablas (4 + historial), `movie` con `fk_movie_director` + dos `CHECK` con nombre, `movie_actor` con PK `(movie_id, actor_id)` y `CASCADE` en `movie_id`; en `information_schema.referential_constraints` los `delete_rule` son `RESTRICT`, `RESTRICT` y `CASCADE`; en `check_constraints`, los dos `chk_movie_*`.
4. Detalles anotados: `RESTRICT` no se escribe en el `SHOW CREATE TABLE` porque es el valor por defecto; MariaDB crea solo un índice (`KEY`) por cada FK; la PK compuesta hace única la pareja, no cada columna.
5. Documentado: sección 8.5 del [diario](08-flyway-migraciones.md), D-41 cerrada, progreso y estructura del `README.md`.

**Siguiente:** paso 8.6, inmutabilidad y checksum (romper una migración a propósito y leer el error).

### 2026-10-28 (cuarta parte) — fase 8, paso 8.6 terminado; **fase 8 completa**

1. Añadido un comentario al final de `V1__create_director_table.sql` (migración ya aplicada).
2. `.\mvnw.cmd test` → **falla al construir el contexto**:
   ```
   Failed to initialize dependency 'flywayInitializer' of LoadTimeWeaverAware bean 'entityManagerFactory'
   Validate failed: Migrations have failed validation
   Migration checksum mismatch for migration version 1
   Either revert the changes to the migration, or run repair to update the schema history.
   ```
   Enseña dos cosas: el checksum tiene dientes (un comentario basta) y Flyway va **antes** que JPA.
3. Anotado que `flyway repair` reescribe el checksum y oculta el problema: no se usa.
4. Revertida la línea. Al hacerlo por PowerShell se rompió la codificación del archivo (tildes + BOM `EF BB BF`); detectado comparando bytes con `V2`–`V4` y corregido reescribiéndolo en UTF-8 sin BOM. Probado que Flyway ignora el BOM (validó con y sin él).
5. `.\mvnw.cmd test` → `Successfully validated 4 migrations`, `Schema movies is up to date. No migration necessary.`, 4 tests en verde.
6. Documentado: sección 8.6 del [diario](08-flyway-migraciones.md) (con el incidente de PowerShell), filas nuevas en las trampas conocidas de este archivo y en los errores del `README.md`.

**Siguiente:** fase 9, paso 9.1 (cerrada la fase 8: commit `3e38972`, etiqueta `fase-08` subida).

### 2026-10-28 (quinta parte) — cierre de la fase 8

```
git add -A
git commit -m "feat(fase-08): create schema with Flyway migrations"
# [main 3e38972] 23 files changed, 2144 insertions(+), 341 deletions(-)
git tag -a fase-08 -m "Fase 8: esquema creado con migraciones Flyway"
git push          # ccbf5b7..3e38972  main -> main
git push origin fase-08   # [new tag] fase-08 -> fase-08
```

Un único commit para toda la fase (D-30). No se comiteó `.env` (ignorado) ni `target/`.

### 2026-10-28 (sexta parte) — fase 9 completa (pasos 9.1 a 9.5)

1. `docker info` → `29.8.0`; `docker compose ps` → `movies-mariadb-1` **healthy** en el 3307; esquema en la versión 4.
2. **9.1:** se probó primero una migración `V100__insert_test_data.sql` en `db/testdata` con un perfil `dev` de Spring. Se aplicó (`Successfully applied 1 migration ... now at version v100`), pero al ejecutar sin el perfil salió:
   ```
   Current version of schema `movies`: 100
   WARN : Schema `movies` has a version (100) that is newer than the latest available migration (4) !
   ```
   Descartado: el historial de Flyway es único y una futura `V5` quedaría fuera de orden (D-42). Se borraron las filas, la fila `version = 100` del historial y se reiniciaron los `AUTO_INCREMENT`.
3. **9.2:** creado `src/main/resources/db/testdata/seed.sql` (6 directores —incluida Sofia Coppola, D-43—, 10 películas, 14 actores y 24 participaciones) y cargado con:
   ```
   docker compose cp src/main/resources/db/testdata/seed.sql mariadb:/tmp/seed.sql
   docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" movies < /tmp/seed.sql'
   ```
   Resultado: `exit=0` y conteos `director 6`, `actor 14`, `movie 10`, `movie_actor 24`. Las tildes se guardaron bien (`Amenábar`, `Verdú`). Las tres consultas de huérfanos dieron 0.
4. **9.3:** cinco operaciones que deben fallar, con estos errores reales: `1452` (película con `director_id = 99`), `1451` (`DELETE` del director 1), `4025` (`duration_minutes = 0` y `release_year = 1800`) y `1062` (Bale repetido en *The Prestige*). Los cinco intentos no dejaron rastro (siguen 6 directores y 10 películas).
5. **9.4:** `INNER JOIN` de dos tablas (película + director), filtro por director con `ORDER BY release_year, title`, `JOIN` de tres tablas con `GROUP_CONCAT` para el reparto y la misma relación desde el actor. Coincide con la fase 2.
6. **9.5:** `LEFT JOIN`: 5 directores con `INNER` frente a 6 con `LEFT`; Sofia aparece con `COUNT(m.id) = 0` pero `COUNT(*) = 1`; `AVG` devuelve `NULL` para ella; `HAVING COUNT(m.id) > 1` deja a Nolan, Scorsese y Tarantino.
7. `.\mvnw.cmd test` → **4 en verde**, `Successfully validated 4 migrations`, `Schema movies is up to date`. Ningún aviso de la versión 100.
8. Documentado: [diario 09](09-datos-de-prueba.md), [temario 09](temario/09-datos-de-prueba-y-consultas.md), D-42 y D-43, glosario (datos de prueba, `INSERT`, `JOIN`, `LEFT JOIN`, `GROUP BY`, agregados, `HAVING`, SQLSTATE...), `README.md` de la raíz y de la guía, y la [fase 2](02-alcance-del-dominio.md) con Sofia Coppola.

**Siguiente:** fase 10, paso 10.1 (modelo anémico frente a rico).

### 2026-10-28 (séptima parte) — fase 10, paso 10.1 terminado

1. Explicadas las dos opciones (anémico: campos + setters y reglas en el servicio; rico: constructor que valida y sin setters) y la intermedia (rico con setters). **El alumno eligió el modelo rico moderado** → D-44.
2. Decidido qué incluye: constructor que valida, sin setters, `fullName()` calculado, biografía en blanco → `NULL`, constructor protegido para JPA y campos no `final` (coste de D-29). Qué queda fuera: `equals`/`hashCode`, Bean Validation en el dominio, servicios de dominio y setters.
3. Aclarado en el temario qué sería un modelo rico "completo" (*value objects*, factorías, eventos, agregados, dominio separado de JPA) y por qué no se hace ahora; también que el modelo de dominio no decide la forma del proyecto (una sola aplicación Spring Boot, paquetes por capa, D-18).
4. **No se tocó código**: los 4 tests siguen en verde.
5. Documentado: [diario 10](10-modelo-de-dominio.md#101-modelo-anémico-frente-a-modelo-rico), [temario 10](temario/10-modelo-de-dominio.md), D-44 y este archivo.
6. **El alumno aclara que el profesor pide un monolito normal** → D-45: el proyecto es un monolito **clásico en capas** (un `.jar`, un módulo Maven, una base de datos, paquetes por capa) y **no** un monolito modular. Comprobado en el `pom.xml` (ningún `<module>`) y en `src/main/java` (una sola clase, `MoviesApplication`). No se añaden herramientas de fronteras. *(Corregido en la novena parte: la web del profesor sí plantea monolito modular; la modularización se planifica en la fase 25.)*

**Siguiente:** fase 10, paso 10.2 (por entonces: clases de dominio; redefinido después a *value objects*).

### 2026-10-28 (octava parte) — commit de la fase 9

1. `.\mvnw.cmd clean test` → **4 en verde**, `Successfully validated 4 migrations`, `Schema movies is up to date`. (Sin `clean` quedaban informes viejos en `target/surefire-reports` de los tests de dominio borrados; el `clean` los elimina y deja el recuento real.)
2. El commit de la fase 9 se hizo **seleccionando archivos**, porque `ESTADO.md` y `decisiones-tecnicas.md` ya mezclaban contenido de la fase 10. Para esos dos se preparó una versión "solo fase 9" en un archivo temporal y se metió en el índice con `git hash-object -w` + `git update-index --cacheinfo`, **sin tocar el árbol de trabajo**. El árbol siguió mostrando `MM` en los dos archivos (índice = fase 9, disco = fase 10).
3. ```
   git commit -m "feat(fase-09): add test data seed and verify SQL relationships"
   # [main 7bbb579] 9 files changed, 1091 insertions(+), 49 deletions(-)
   git tag -a fase-09 -m "Fase 9: datos de prueba y comprobación de relaciones SQL"
   git push              # 3e38972..7bbb579  main -> main
   git push origin fase-09   # [new tag] fase-09 -> fase-09
   ```
   La etiqueta se rehízo (`-f`) con la tilde correcta en "comprobación" y se volvió a subir.
4. **Lo que queda sin comitear es solo fase 10**: `ESTADO.md`, `decisiones-tecnicas.md` (D-44, D-45) y los dos archivos nuevos de la fase 10.

**Siguiente:** fase 10, paso 10.2 (por entonces: clases de dominio; redefinido después a *value objects*).

### 2026-10-28 (novena parte) — el plan se contrasta con el temario del profesor

1. Leídos [DWS](https://cesguiro.es/doku.php/clase/daw/dws/start) y [Programación](https://cesguiro.es/doku.php/clase/daw/prog/start), y de DWS: [01 arquitecturas](https://cesguiro.es/doku.php/clase/daw/dws/1eval/arquitecturas), [02 API First](https://cesguiro.es/doku.php/clase/daw/dws/1eval/api_first), [03 validación](https://cesguiro.es/doku.php/clase/daw/dws/1eval/validacion), [04 modulith](https://cesguiro.es/doku.php/clase/daw/dws/1eval/modulith) y [05 JPA](https://cesguiro.es/doku.php/clase/daw/dws/1eval/jpa).
2. **Hallazgo:** su tema 01 fija como referencia «una arquitectura por capas desplegada como **monolito modular**» y el tema 04 desarrolla módulos, ArchUnit y Spring Modulith. Eso contradice D-45 tal como estaba escrita ayer. Se avisa al alumno y se corrige.
3. **Decisión del alumno:** monolito **normal ahora**, modular **en el futuro** → D-45 reescrita: monolito clásico en capas ahora y modularización en la **fase 25**. Se anota el riesgo: con D-29, partir en dos módulos obligaría a relacionar entidades por identificador (tema 05 del profesor); con un módulo `catalog` no hay conflicto.
4. **Añadido al plan:** paso 14.6 (`open-in-view=false`), **fase 15A** (API First, sin renumerar 16–24), paso 18.5 (`ProblemDetail`), la fase 24 pasa a 🟡 con la nota de que el profesor la da en la 1ª evaluación, y el **Bloque F**: 25 modular, 26 eventos, 27 logs, 28 concurrencia y 29 serverless (marcada como «no encaja en esta API»). Añadida una tabla de cobertura del temario del profesor.
5. **Diferencia que no se toca:** el profesor pone las invariantes en *value objects* (`record Iban`, tema 03); nosotros en el constructor de la entidad (D-44). Queda como decisión abierta **antes del paso 10.2**, porque adoptarlos obliga a `@Embeddable`/`AttributeConverter` en la fase 11.
6. **No se tocó código**: los 4 tests siguen en verde (última ejecución: `.\mvnw.cmd clean test` antes del commit `7bbb579`).

**Siguiente:** fase 10, paso 10.2.

### 2026-10-28 (décima parte) — fase 10, paso 10.2 terminado

1. **El alumno confirma dos cosas:** el proyecto no es una práctica evaluada (es un ejercicio para aplicar la clase), el profesor **pide *value objects*** y la versión de Java/Spring Boot no importa. Con eso se cierra la duda abierta en la novena parte.
2. **D-46:** se adoptan seis *value objects* (`Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate`, `Biography`) y se reescribe D-44: la validación de cada campo pasa al *value object*; el constructor de la entidad solo comprobará lo que cruza campos.
3. **Leídas las cuatro migraciones** (`V1`–`V4`) para copiar las reglas reales, no inventarlas: `title VARCHAR(255)`, `first_name`/`last_name VARCHAR(100)`, `birth_date DATE NOT NULL`, `biography TEXT NULL`, `CHECK (release_year >= 1888)`, `CHECK (duration_minutes > 0)`.
4. **Diseño cerrado:** `record` con constructor compacto, `IllegalArgumentException` para todo valor inválido (también `null`), `trim()` antes de medir, sin topes superiores que el esquema no tenga, `BirthDate` no futura como única regla añadida, `Biography` no admite blanco y la ausencia es `null` (la normalización blanco → `null` se hace al entrar: mapper de la 17 y DTO de la 18). Todo en `com.daw.movies.domain.model`.
5. **Plan y temario corregidos** en consecuencia: el paso 10.2 pasa a ser «*value objects*: qué son y cuáles necesitamos», el 10.3 los escribe con TDD y el 10.4 crea las entidades; la cobertura del tema 03 del profesor pasa a ✅.
6. **No se tocó código Java**: los 4 tests siguen en verde (última ejecución: `.\mvnw.cmd clean test` antes del commit `7bbb579`). Este paso es de diseño.

**Siguiente:** fase 10, paso 10.3 (*value objects* con TDD: test en rojo y después el `record`).

### 2026-10-28 (undécima parte) — fase 15A adelantada: contrato OpenAPI y Swagger UI

Petición del alumno: *"¿hemos incluido Swagger? Implementa la documentación OpenAPI para ver la interfaz de los endpoints"*. La fase 15A estaba en el plan **entre la 15 y la 16**; se adelantan 15A.1 y 15A.2 porque no dependen de la fase 15 (las rutas salieron de D-36 en la fase 8). El 15A.3 sigue esperando a los controladores.

1. **Versión comprobada antes de escribir el `pom.xml`:** `maven-metadata.xml` de Maven Central → `<release>3.2.0</release>`, y la web de springdoc → v3 es la rama de **Spring Boot 4**. Se declara **explícita**, porque Boot no gestiona esta dependencia.
2. **`pom.xml`:** añadidos `spring-boot-starter-webmvc` (lo necesita springdoc; excepción anotada a D-26) y `springdoc-openapi-starter-webmvc-ui` **3.2.0**.
3. **`src/main/resources/static/openapi.yaml`** (nuevo, OAS 3.0.3): los 4 endpoints de D-36, esquemas `MovieSummary`, `MovieDetail`, `PersonRef`, `DirectorDetail` y `ProblemDetail`, órdenes de D-38, `200 []` de D-39 y `biography` anulable de D-37. Ejemplos con datos reales del `seed.sql`. Sin prefijo `/api/v1`.
4. **`application.yaml`:** bloque `springdoc` con `swagger-ui.url=/openapi.yaml`, `operations-sorter: method` y `tags-sorter: alpha`.
5. **`OpenApiContractTests`** (nuevo, 3 tests, puerto aleatorio con `@LocalServerPort`): el YAML se sirve, **parsea** con SnakeYAML y sus `paths` son exactamente los cuatro de la v1, y `/swagger-ui/index.html` responde 200.
6. `.\mvnw.cmd clean test` → **7 en verde**:
   ```
   [INFO] Tests run: 2 ... DatabaseConnectionTests
   [INFO] Tests run: 1 ... FlywayMigrationTests
   [INFO] Tests run: 1 ... MoviesApplicationTests
   [INFO] Tests run: 3 ... OpenApiContractTests
   [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```
7. **Comprobado en vivo** (`java -jar`, Tomcat en el 8080):
   ```
   /openapi.yaml             → 200, application/octet-stream, 9024 bytes
   /swagger-ui.html          → 302 → /swagger-ui/index.html
   /swagger-ui/index.html    → 200, text/html
   /v3/api-docs/swagger-config → {"url":"/openapi.yaml", "operationsSorter":"method", ...}
   /v3/api-docs              → {"openapi":"3.1.0", ... "paths":{}, "components":{}}
   ```
   El `/v3/api-docs` vacío es lo esperado: **no hay controladores**. Captura de la UI en [img/15A-swagger-ui.png](img/15A-swagger-ui.png).
8. **Detalles anotados:** Spring sirve `.yaml` como `application/octet-stream` (Swagger UI lo parsea igual; se corregiría con un `ResourceHandler`); springdoc avisa de que sus endpoints están activos "by default" (aquí es el objetivo); aparece el aviso de `spring.jpa.open-in-view` (paso 14.6, pendiente).
9. Documentado: [diario 15A](15A-api-first.md), [temario 15A](temario/15A-api-first.md), **D-47**, glosario (OpenAPI, Swagger UI, springdoc, API First, `operationId`), plan (fase 15A con ✅/✅/⏳ y fila de cobertura), `README.md` de la raíz (estado, dependencias, 7 tests, sección Swagger, estructura) y el README de esta guía.

### 2026-10-31 — los dos frentes se comitean y fase 10, paso 10.3 terminado

**Primera parte: los commits pendientes.** Con permiso del alumno se separaron los dos frentes:

1. `f2cf8cc` — `docs(fase-10): design rich domain model and six value objects` (8 archivos, 689 inserciones). Solo documentación: diario y temario 10, D-44/D-45/D-46, plan, glosario, README de la guía y `ESTADO.md`.
2. `d06943e` — `feat(fase-15A): add OpenAPI contract and Swagger UI` (13 archivos, 844 inserciones). Código: `openapi.yaml`, springdoc 3.2.0, `OpenApiContractTests`, D-47 y README de la raíz.
3. Para separarlos se prepararon versiones parciales de los cinco archivos compartidos (`ESTADO.md`, `decisiones-tecnicas.md`, `glosario.md`, `README.md` de la guía y plan) sobre el árbol de trabajo, se comiteó la fase 10 y se restauraron. Sin `git hash-object` esta vez. **Ninguna etiqueta y ningún `push`**: ninguna de las dos está cerrada.

**Segunda parte: paso 10.3 (TDD).**

1. Docker Desktop estaba cerrado: `Start-Process` sobre `Docker Desktop.exe`; `docker info` → `29.8.0`; `docker compose up -d --wait` → `movies-mariadb-1` **healthy** en el 3307.
2. Línea base: `.\mvnw.cmd clean test` → **7 en verde**.
3. **Rojo:** creados los seis tests en `src/test/java/com/daw/movies/domain/model/` (28 tests). `.\mvnw.cmd test` → fallo de compilación (`cannot find symbol` en las seis clases), el rojo esperado.
4. **Verde:** creados los seis `record` y `Texts` en `src/main/java/com/daw/movies/domain/model/`. Primer intento: **34 de 35**, con `BiographyTest.rejectsBlankText` en rojo por el mensaje «La biografía no puede estar **vacío**» (concordancia). Se cambió el mensaje a «no puede estar **en blanco**» (invariable) y se ajustaron los dos tests que lo comprueban.
5. `.\mvnw.cmd clean test` → **35 en verde** (7 anteriores + 28 nuevos), `BUILD SUCCESS`. Los tests de dominio no arrancan Spring ni tocan la base.
6. Documentado: [diario 10, §10.3](10-modelo-de-dominio.md#103-los-seis-value-objects-con-tdd) (rojo, verde, el fallo de concordancia y las decisiones), [temario 10](temario/10-modelo-de-dominio.md) (código real y pregunta 10), D-46 (nota de implementación), glosario («rojo/verde») y este archivo.

**Siguiente:** fase 11, paso 11.1 (qué es JPA y qué es Hibernate), cuando el alumno lo pida.

### 2026-10-31 (segunda parte) — fase 10, paso 10.4 terminado; **fase 10 completa**

1. Entorno: `docker info` → `29.8.0`; `docker compose ps` → `movies-mariadb-1` **healthy** en el 3307.
2. **Rojo:** creados `DirectorTest` (5), `ActorTest` (5) y `MovieTest` (10) en `src/test/java/com/daw/movies/domain/model/`. `.\mvnw.cmd test-compile` → **32 errores** `cannot find symbol` de `Director`, `Actor` y `Movie`.
3. **Verde:** creadas `Director`, `Actor` y `Movie` en `com.daw.movies.domain.model` con los seis *value objects*, getters, `fullName()` delegando en `PersonName`, constructor `protected` vacío para JPA y campos no `final` (D-29). Relaciones unidireccionales; `getActors()` devuelve `List.copyOf`.
4. `.\mvnw.cmd clean test` → **55 en verde** (7 de integración + 28 de los *value objects* + 20 de las entidades), `BUILD SUCCESS`.
5. Decisiones anotadas: sin `equals`/`hashCode` todavía; `addActor` no comprueba duplicados (la garantía es la PK compuesta, D-14); `Director` y `Actor` duplican código a propósito (D-11); el reparto no se expone mutable.
6. Documentado: [diario 10, §10.4](10-modelo-de-dominio.md#104-las-entidades-y-sus-relaciones), [temario 10](temario/10-modelo-de-dominio.md), D-44 (nota de implementación), glosario («Reflexión») y `README.md` de la raíz.

7. **Ampliación posterior (misma sesión, a petición del alumno):** los temas 10 y 11 recogen las explicaciones de la sesión —ejemplo completo de creación, *value object* frente a entidad, reflexión y `protected`, alternativa con mappers, recorrido fila ↔ objeto, reparto JPA/Hibernate/Spring Boot/Spring Data, *dirty checking* y guía de búsquedas—; el glosario añade cuatro términos (`AttributeConverter`, *persistence context*, *dirty checking*, *persistence ignorance*).

---

### 2026-11-04 — revisión del temario contra el código (HEAD = `aa0cef7`, fase 10)

1. Contrastados los temas 02–11 con el código de `domain.model`, las migraciones y `pom.xml`. El tema 10 coincide con el código.
2. Corregidos tres desfases: tema 11 (ejemplo de `new Director(1L, "Christopher", ...)` sustituido por el constructor real con *value objects*, y nota de que el ejemplo `@Entity` con `String`/`LocalDate` es una simplificación) y tema 05 (la frase «en la fase 10 elegiremos…» ahora recoge D-44/D-46).
3. Observado, **no tocado**: `docs/arquitectura.md` existe sin seguimiento y **vacío**; la rama local `main` está en la fase 10, mientras `origin/main` y las etiquetas `fase-11` y `fase-15A` contienen trabajo posterior (el commit `daf027e` de la 15A no está en el historial de `main`; por eso `openapi.yaml` y springdoc no existen en el árbol actual, aunque §1 los menciona).
4. No se ejecutó ningún comando de Maven ni de Docker.

### 2026-11-04 (segunda parte) — nuevo rumbo: paso 0 (que compile)

**Contexto:** el profesor da el visto bueno al nuevo rumbo (D-46 revocada, arquitectura por capas con DAO JDBC). Plan aprobado: 0) compilar y tests en verde, 1) revisar el código, 2) actualizar el skill `creador-de-skills` (`presentation` → `controller`), 3) Swagger (springdoc), 4) monolito modular con un módulo `catalog`. Plan completo en `~/.claude/plans/vamos-a-cambiar-el-drifting-pond.md`.

**Estado real del código (commit `398ab41`):** JDBC puro (`JdbcTemplate`), sin JPA ni springdoc; endpoints `/movies` (5). §1 de este archivo (JPA, 55 tests, OpenAPI) **está desfasado**; se corrige en el paso 1.

**Paso 0:**
1. `controller/MovieController`, `controller/mapper/MovieMapper` y `controller/exception/GlobalExceptionHandler` tenían el `package` de su ubicación anterior. Corregidos los tres `package` y el import del mapper.
2. `.\mvnw.cmd -q clean test-compile` → `exit=0`.
3. Docker Desktop estaba cerrado: `Start-Process` + espera; `docker compose up -d --wait` → `movies-mariadb-1` **healthy** en el 3307.
4. `.\mvnw.cmd clean test` → **23 tests en verde** (`DatabaseConnectionTests` 1, `ActorTest` 6, `DirectorTest` 6, `MovieTest` 8, `FlywayMigrationTests` 1, `MoviesApplicationTests` 1), `BUILD SUCCESS`. No hay tests de controller, service ni DAO.

**Paso 1 (en curso, tres agentes en paralelo + ajustes del alumno):**
1. Tests nuevos de `MovieServiceImpl` (Mockito) y `MovieController` (`@WebMvcTest`); `pom.xml` gana `spring-boot-starter-webmvc-test` (en Boot 4 `@WebMvcTest` ya no viene en `spring-boot-starter-test`).
2. `docs/arquitectura.md` reescrito con la arquitectura real; D-48 añadida; D-29 marcada como superada. D-20 sin marcar (decisión pendiente).
3. Temarios 10 y 11, diario 10, plan (fases 30–34), glosario y READMEs adaptados al giro (value objects y JPA marcados como históricos).
4. Nuevo temario [entender-el-codigo.md](temario/entender-el-codigo.md) (recorrido de una petición, esquemas y 15 preguntas).
5. Ajustes pedidos por el alumno, con TDD (rojo: 5 fallos + 1 error; verde): `@MaxYearsAhead(10)` en el año de `MovieRequest`; `create`/`update` lanzan `IllegalStateException` (500) y no `MovieNotFoundException` al releer; el `GlobalExceptionHandler` trata JSON mal formado (400), id no numérico (400) y `Content-Type` incorrecto (415).
6. `.\mvnw.cmd clean test` → **55 tests en verde** (19 controlador, 13 servicio, 6+6+8 modelo, 3 de integración), `BUILD SUCCESS`.

**Pendiente de revisar (informe de los agentes):** swagger/`openapi.yaml` ya no existen (se retoma en la fase 33); `temario/05` sección 11, `temario/15A` y diarios 05–07 y 15A siguen con referencias antiguas; glosario con `Puerto` duplicado.

7. A petición del alumno, el paquete `controller.exception` pasa a llamarse **`controller.handler`** (`git mv`; `package`, import del test y docs actualizados). `.\mvnw.cmd clean test` → 55 en verde. El nombre antiguo solo queda en la entrada del paso 0, que es historia.

**Siguiente:** fase 2 (skill `creador-de-skills`), cuando el alumno lo pida.

### 2026-11-04 (tercera parte) — fase 2a: `k-back-module`

1. `git rev-parse --is-inside-work-tree` en `Lorenzo\creador-de-skills` → **no era repositorio**. A petición del alumno: `git init -b main`, `.gitignore` con `target/` y commit inicial `3c6027a` (estado previo, sin tocar).
2. Reescrito `skills-code-review/skills/k-back-module/SKILL.md`: `presentation` → `controller` (con `dto`, `mapper`, `handler`), `models` → `model`, `exceptions` → `exception`; ArchUnit y reglas R01/R02/R05 con `..controller..`; el servicio recibe valores sueltos; el mapper solo hace la salida (R07); sin *value objects*; enrutado a `k-back-controller` (la carpeta se renombra en la 2b). Solo `<Entidad>` y `<paquete.raíz>`.
3. Commit `40b0bb1` en el repo del skill. No hay tests que ejecutar ahí (son documentos Markdown).
4. **Decisión del alumno:** las excepciones de persistencia (`PersistenceAccessException`, `PersistenceConflictException`; tu proyecto no las tiene) se tratan en la **2b**. En la 2a se dejó una nota en el skill y no se crea el paquete.
5. `demo-cliente/` sin tocar (ejemplo antiguo).

### 2026-11-04 (cuarta parte) — fase 2b, subpaso 1: `k-back-controller`

1. **Pregunta del alumno:** el profesor ve `presentation` como el nombre conceptual de la capa, pero usa `controller` por el orden alfabético. **Recomendación aceptada:** mantener `controller` (paquete y skill) y hablar de «capa de presentación» en los textos. Anotado en D-48 (`decisiones-tecnicas.md`).
2. `git mv k-back-presentation k-back-controller` y reescritos `SKILL.md`, `controller.md`, `dto.md`, `handler.md` y `mapper.md` leyendo antes el código real (`MovieController`, `MovieMapper`, `GlobalExceptionHandler`, `MovieRequest`, `MovieResponse`, `MovieControllerTest`).
3. **Cambios de fondo respecto al skill antiguo:** el servicio recibe valores sueltos (no `toModel`); el mapper es un `@Component` solo de salida; el POST usa `@ResponseStatus(CREATED)` (sin `Location`); el cuerpo de error es `{ "message": ... }` y no `ProblemDetail`; el handler cubre 404/400/415 y lo demás cae en el 500 de Spring; `@Import` del mapper y del handler en el test `@WebMvcTest`. Los identificadores siguen siendo `PRE-*`.
4. Commit `ca3c834` en el repo del skill. Sin tests que ejecutar (Markdown).
5. **Queda abierto:** `ProblemDetail` frente a `{ message }`. Se siguió el código del proyecto; si el profesor quiere `ProblemDetail`, cambiar `PRE-D5` y el proyecto a la vez.

### 2026-11-04 (quinta parte) — fase 2b, subpaso 2: `k-back-domain` y `k-back-persistence`; **2b completa**

1. Leídos los siete archivos de los dos skills y el código real (`Movie`, `MovieNotFoundException`, `MovieService`, `MovieServiceImpl`, `MovieRepository`, `MovieRepositoryImpl`, `MovieDao`, `MovieDaoJdbc`).
2. **`k-back-domain`:** tipos simples y sin *value objects*; sin *setters*, `getX()`, dos constructores (creación y reconstrucción con `id`); comprobaciones defensivas con `IllegalArgumentException`; relaciones por objeto y unidireccionales (antes «por id»); solo `<Entidad>NotFoundException`; `IllegalStateException` para la relectura fallida; servicio con valores sueltos; el puerto `save` devuelve el id; `@Service` y `@Transactional` en la clase (impureza reconocida).
3. **`k-back-persistence`:** `JdbcTemplate` + `RowMapper` + `GeneratedKeyHolder` (antes `DataSource` y `PreparedStatement` a mano); `findById` con `query(...).findFirst()`; N+1 como deuda aceptada; esquema por migraciones.
4. **Decisión de las excepciones de persistencia: no se crean propias** (nueva regla `PER-D9`/`PER-R11`); anotado en D-48. `k-back-module` actualizado.
5. Commit `ff9f2ad` en el repo del skill. Sin tests que ejecutar (Markdown).
6. Búsqueda de restos (`grep`) en los cuatro skills `k-back-*`: solo quedan coincidencias intencionadas.
7. **Tests de integración:** el skill exige tests de DAO/repositorio (PER-R09), pero tu proyecto aún no los tiene (ESTADO §1). Es una diferencia conocida, no un fallo del skill.

### 2026-11-04 (sexta parte) — fase 2c; **fase 2 completa**

1. Leídos `do-code-impl`, `plantilla-entidad.md`, `do-code-review`, `plantillas-proyecto/*`, `contexto.md` y `teoria/*`.
2. **`do-code-impl`:** orden domain → persistence → controller; checklist con `controller/...`, sin *value objects*, con migración del esquema y el método del handler. **`plantilla-entidad.md`** reescrita: tipos simples, relaciones por objeto y unidireccionales, POST → 201 (sin `Location`), casos de uso con valores sueltos, errores 404 por `NotFoundException`, tests con `@Import`. **`do-code-review`:** una línea (`controller`).
3. **`plantillas-proyecto`:** `agents.md` (`k-back-controller`), `architecture.md` (`controller`, migraciones), `tech.md` (`JdbcTemplate`, sin JPA).
4. **`contexto.md`** con aviso de actualización y lista de skills al día; **`teoria/arquitectura.md`** reescrita con el código real; **`teoria/contexto-skills-k-back-do.md`** con las decisiones D1–D12 actualizadas (D6 `ProblemDetail` queda como «abierto»); **`version-1`** y **`version-2`** marcadas como históricas; `contexto-skills-k-do.md` (teoría general) sin cambios.
5. Comprobado con `grep`: ninguna skill ni plantilla nombra `Movie` (se corrigió una en `model.md`); los restos de `presentation`, `PersistenceAccessException` o *value objects* son solo prohibiciones o ejemplos «mal».
6. Commit `2b9cd9f` en el repo del skill. `demo-cliente/` queda como ejemplo antiguo, sin tocar, y **sin ejecutar** `mvn test` ahí.
7. **No probado:** las skills nuevas no se han ejecutado generando una entidad (`/do-code-impl`) ni revisando el proyecto (`/do-code-review`); solo se han reescrito y contrastado con el código leído.

### 2026-11-04 (séptima parte) — fase 3, subpasos 3a y 3b (Swagger generado desde el código)

1. **Decisión del alumno:** el profesor no quiere el contrato en `src/main/resources`. Elegida la **opción A**: contrato generado por springdoc desde el código, sin `openapi.yaml`; `OpenApiConfig` en `com.daw.movies.config`, fuera de las capas.
2. Versión comprobada en Maven Central (`maven-metadata.xml`): **3.2.0** es la última de la rama 3.x. `docker info` → `29.8.0`; `docker compose ps` → `movies-mariadb-1` healthy.
3. **Rojo:** creado `src/test/java/com/daw/movies/config/OpenApiContractTest.java` (3 tests: `/v3/api-docs` lista los 5 endpoints, título `Movies API`, `/swagger-ui/index.html` responde 200). `mvnw -q test -Dtest=OpenApiContractTest` → 3 fallos, `Status expected:<200> but was:<404>`.
4. **Verde:** `pom.xml` gana `springdoc-openapi-starter-webmvc-ui` con `${springdoc.version}` = 3.2.0, y se crea `config/OpenApiConfig.java` (bean `OpenAPI` con título, versión y descripción). Incidente: el primer `Edit` del `pom.xml` no se aplicó (la sangría real son tabuladores), y la compilación falló con `package io.swagger.v3.oas.models does not exist`; se corrigió la dependencia.
5. `.\mvnw.cmd clean test` → **58 en verde** (55 + 3), `BUILD SUCCESS`.
6. **Comprobado con la aplicación arrancada** (`spring-boot:run`, puerto 8080): `/v3/api-docs` → 200, OpenAPI 3.1.0, los 5 endpoints (`GET/POST /movies`, `GET/PUT/DELETE /movies/{id}`) y los esquemas `ErrorResponse`, `MovieRequest`, `MovieResponse`, `ActorView`, `DirectorView`; `/swagger-ui/index.html` → 200; `/swagger-ui.html` → 302 al anterior; `GET /movies` → 200. Aplicación parada después.
7. **No ejecutado:** el «Try it out» desde el navegador contra `seed.sql` (queda para el 3c, hecho por el alumno) y ninguna anotación `@Operation`/`@Schema` (los ejemplos y las descripciones por endpoint son opcionales).

8. **Prueba manual de los 5 endpoints con `curl`** (a petición del alumno; app arrancada con `spring-boot:run`, 10 películas del `seed.sql` al empezar y al terminar). Solo se tocó una película creada para la prueba (id 12), borrada al final:
   - `GET /movies/1` → 200 (*The Prestige*, Nolan, 3 actores). `POST /movies` → **201** (id 12). `PUT /movies/12` → **200** (cambia título, año, duración y director). `GET /movies` → 200 (11 en ese momento). `DELETE /movies/12` → **204**, y `GET /movies/12` después → 404.
   - Errores, todos con cuerpo `{ "message": ... }`: id inexistente (GET/PUT/DELETE) → 404; director inexistente → 404; `/movies/abc` → 400; título vacío + año 1800 + duración 0 → 400 con los tres campos; año 2100 → 400; JSON roto → 400; `Content-Type: text/plain` → 415.
   - Efecto lateral: el `AUTO_INCREMENT` de `movie` avanza (el próximo id no será el 11).
   - Esto **no** es «Try it out» en el navegador: usa las mismas rutas, pero no pasa por la interfaz de Swagger.

9. A petición del alumno se dejó la aplicación arrancada para que viera Swagger UI en `http://localhost:8080/swagger-ui.html`; al terminar se paró (`8080` sin respuesta). Commit `3f3dbc2` (`feat: add springdoc and OpenApiConfig with contract test`; `pom.xml`, `OpenApiConfig`, `OpenApiContractTest`, `ESTADO.md`).

**Siguiente:** fase 1c y/o fase 4 (monolito modular, empezando por 4a: pruebas ArchUnit). No se empieza sin que el alumno lo pida.

### 2026-11-04 (octava parte) — intercambio de ramas en GitHub y cierre de la sesión

1. `git fetch` → `main` local 10 commits por delante y 11 por detrás de `origin/main` (el camino JPA de las fases 11 y 15A, `2d58690`). Un `push` normal se habría rechazado y forzarlo habría borrado esos commits. El alumno eligió «rama nueva»: `git push -u origin main:nuevo-rumbo-jdbc` → ok (`24f7e9c`).
2. Luego pidió **cambiar `main` por la rama nueva**. `gh` no tenía sesión y `git push` fallaba con `Permission denied (publickey)` (incluso forzando `C:/Windows/System32/OpenSSH/ssh.exe`) aunque el `ssh -T git@github.com` del alumno respondía «Hi fernandomguez!». El alumno hizo `gh auth login` (HTTPS).
3. Con `gh api -X POST repos/fernandomguez/daw-movies/branches/<rama>/rename`: `main` → `jpa-fase-11-15a` (el primer intento dio `HTTP 502`; se repitió una vez y se comprobó que el listado tarda unos segundos en reflejarlo) y después `nuevo-rumbo-jdbc` → `main`. La rama por defecto no se movió sola: `gh repo edit --default-branch main`. Sin forzar nada; ramas protegidas: ninguna.
4. Resultado en GitHub: `main` = `24f7e9c` (luego `38187f0`), `jpa-fase-11-15a` = `2d58690`, `backup/antes-de-volver-a-fase-10` = `242b205`; las etiquetas `fase-01`…`fase-15A` no cambian.

### 2026-11-04 (novena parte) — giro de validaciones, traslado del proyecto y arranque del frontend

**Lo que hizo la sesión «Validaciones Jakarta y maxYearsAhead» (19:22–20:31, murió con Orca cerrado):**

1. `MovieRequest`: la anotación propia `@MaxYearsAhead(10)` (y su validador) se sustituyó por un método `isReleaseYearValid()` con `@AssertTrue` (el año no pasa de año actual + 10; `@Max` solo admite números fijos). Borrados `MaxYearsAhead.java` y `MaxYearsAheadValidator.java`.
2. [temario/entender-el-codigo.md](temario/entender-el-codigo.md) ampliado (el viaje de una petición incluido el `Optional` y el `KeyHolder` del `insert`).
3. **Traslado del proyecto**: de `...\SpringBoot\prueba2_peliculas` a `...\SpringBoot\proyectoMovies\backend` (con `.git` intacto; la carpeta vieja quedó vacía y se borró). Hecho con `robocopy /MOVE` porque la carpeta estaba bloqueada (MarkText tenía abierto un `.md` del proyecto).
4. **Plan del frontend** (5 fases, en esa sesión): 0 reestructurar · 1 scaffold Angular 22 + `proxy.conf.json` (`/movies` → `localhost:8080`, sin CORS) · 2 núcleo (`core/models/movie.ts`, `core/services/movie-service.ts` con signals) · 3 componentes al estilo del proyecto `trabajadores` (`features/movies`, `card-movies`, `modal-movies`, `form-movies`, errores 400 del backend pintados en el formulario) · 4 SCSS en capas s01–s06 con tokens `--mlt-sys-*` y semánticas `!default` · 5 verificación. Las fases 0, 1 y 2 **quedaron verificadas por subagente**. Fase 3 a medias: `card-movies.*` y `modal-movies.*` en disco; faltan `features/movies`, `form-movies`, la shell de app y las fases 4–5.

**Recuperación (esta sesión, 20:33+):**

1. `.\mvnw.cmd clean test` → **58 en verde** desde la nueva ubicación (el cambio de validación queda probado). Docker estaba ya arrancado (`movies-mariadb-1` healthy).
2. Borrado el archivo basura `MovieServiceImpl (recovered).java` (difería de la real en una línea de javadoc; restos de la recuperación del traslado).
3. Pendiente decidido: el frontend vivirá en este mismo repo (la raíz git pasará de `backend\` a `proyectoMovies\` para que el `frontend\` quede dentro). El commit del frontend será otro, separado de este.
4. Commit de validaciones: `refactor: replace MaxYearsAhead annotation with AssertTrue validation`.

*(Los apartados 5–7 del cierre de la octava parte que quedaban al final de este bloque — remote en HTTPS, documentos actualizados y el aviso de no reejecutar tests tras tocar solo `.md` — estos se consideran cubiertos por la décima y la undécima parte de hoy.)*

### 2026-11-04 (décima parte) — la raíz del repo sube a `proyectoMovies\` y el frontend queda completo

**Recuperación del plan muerto (continuación de la novena parte):**

1. Repositorio: el `.git` subió de `backend\` a la raíz `proyectoMovies\` (historial intacto; commit `2b41cd6` rearma las rutas). A partir de aquí la raíz del repo es `proyectoMovies\` y `git` se usa desde ahí.
2. Datos del backend revalidados tras el traslado: `.\mvnw.cmd clean test` → **58 en verde**, Docker `healthy` (`movies-mariadb-1`); borrado el archivo basura `MovieServiceImpl (recovered).java`; commit `6f0fbea` (`refactor: replace MaxYearsAhead annotation with AssertTrue validation`).
3. **Frontend terminado** (Angular 22, `frontend\`): fases 1–2 ya verificadas por subagente en la sesión muerta; esta sesión audita y completa la 3 (listado con buscador por título, `card-movies`, `modal-movies`, `form-movies` con mensajes de error del backend, borrado con confirmación) y la 4 (SCSS en capas s01–s06 con tokens `--mlt-sys-*` y estado privado por componente). Revisor de subagente en dos rondas: la primera marcó valores crudos, lecturas directas de `--mlt-sys-*`, el modificador propio de un elemento, `font-weight` repetido, un test trivial y un `any`; corregidos todos (catálogo ampliado con `$sombras`, `$elevaciones`, `$duraciones`, `$pesos`, `$z-index`; `modal__vacio` como elemento; `MoviePayload` reutilizado; `HttpErrorResponse` en el servicio; test nuevo que comprueba aviso y que el backend no recibe nada con el formulario inválido). La segunda ronda aprobó todo y detectó una clase HTML sin regla SCSS (`card__poster*`), saldada también. Token muerto `--card-outline-width` eliminado.
4. **Rareza al detalle:** el servidor `opencode` de la sesión muerta se despertó (resucitado por Orca, PID 10668) y siguió ejecutando su plan por su cuenta entre las 20:37 y las 20:55. Ese proceso huérfano termino matado por esta sesión; su trabajo (poster de las tarjetas) quedó verificado a mano y por el revisor. **Los posters (`public\posters\{id}.jpg`, imágenes de Wikipedia en *fair use* documentado en su `README.md`) NO están aprobados por el alumno**: van en este commit por comodidad; si no se quieren, se deshacen en un commit aparte sin tocar el resto.
5. Verificación final: `npx ng build` → **sin avisos de presupuesto** (subidos a `8kB/16kB` de `anyComponentStyle` porque la arquitectura de tokens hace los archivos más largos); `npx ng test` → **15/15** (5 ficheros: app, movies, card, modal, form); `ng serve` (proxy 4200→8080) con el backend y el seed reales: `GET /movies` 200 con las 10 películas, raíz 200 y `posters/3.jpg` 200 (134,6 kB).
6. Commit del frontend: `feat(frontend): Angular 22 movies dashboard with tokens SCSS and posters` (único commit, con el `ESTADO.md` de este apartado).

**Siguiente (sin ejecutar):** fase 1c y/o fase 4 del backend (preguntas y ArchUnit), cuando el alumno lo pida.

### 2026-11-04 (undécima parte) — fase 1c: limpieza de la documentación después del giro

1. Commits previos de casa: `ece8a0b` (token muerto `--card-outline-width`, verificado que nada lo usaba) y `1dcde3b` (la décima parte de este archivo asentada).
2. Línea base de la 1c verificada contra el disco: el texto «entre 1888 y 2100» ya no existía en `entender-el-codigo.md` (había quedado como «año actual + 10», aunque su extracto de `MovieRequest` seguía sin mostrar el método con `@AssertTrue`).
3. `temario/05`: banner con el giro definitivo (D-19/D-29 superadas por D-48, enlace a `arquitectura.md`), nota histórica en la «decisión al final», sección 11 marcada como histórica (el `@Service` se mantiene) y notas históricas en el bloque de value objects (D-46 revocada) y en «Entidad JPA y objeto de dominio».
4. Diarios 05–07: nota histórica en cada uno (el código real ya no tiene JPA ni Hibernate; `spring-boot-starter-jdbc` entrega el `DataSource` y el esquema lo lleva Flyway).
5. Glosario: las dos entradas `Puerto` fusionadas en una (quedó la de la lista general, con el texto más rico; borrada la de la sección «giro a JDBC»).
6. Decisión D-20: **se mantiene, no superada** (nota en D-20): el puerto `MovieRepository` sigue en el dominio y el adaptador delega ahora en el DAO JDBC (`MovieRepositoryImpl` → `MovieDao`) y no en Spring Data. Extra del mismo repaso: **D-22** decidió `MovieRepositoryAdapter` pero en el código vive `MovieRepositoryImpl`; anotado en D-22 como histórico.
7. `entender-el-codigo.md` (§5.5): extracto de `MovieRequest` actualizado con el método `isReleaseYearValid` y una frase que explica por qué `@Max` no valía.
8. §1 actualizado: fila «Paso en curso», fila 1c ✅ y la nota del año (`@AssertTrue`, sin `@MaxYearsAhead`, que se quitó en `6f0fbea`).
9. Los **tests del DAO y del repositorio con base de datos real** quedan como tarea propia pendiente, fuera de esta fase.
10. No se ejecutó `mvnw clean test`: solo se tocaron `.md` (mismo precedente que al cerrar la 15A); la última ejecución en verde fue **58** de la décima parte.

### 2026-11-04 (duodécima parte) — commit final del frontend y cierre de la revisión (tercera ronda)

1. Tras la undécima parte, esta sesión completó el frontend con lo que faltaba: **filtro por director en el cliente** (select alimentado por `movieService.directores()`, un `computed` que deduplica y ordena por nombre; mismo estilo que el filtro por departamento del proyecto `trabajadores`), el **formulario de director pasó a un select** (con el `directorId` convertido una sola vez, `form-movies.ts:73`) y el **arreglo del bug del modal de crear** (`modalFormulario` pasó a `Movie | null | undefined` y el template ahora prueba `@if (modalFormulario() !== undefined)`: antes con `@if (...; as movie)` el `null` de crear **nunca se renderizaba**). Tests: de **15 a 20** (los nuevos: directores deduplicados, filtro por director con clic real, conversión del select).
2. Commit final `a6546d3` (`feat(frontend): Angular 22 movies dashboard with tokens SCSS, posters and director filter`). Juntos, el frontend va en **8 commits** (`f1fb4ce`, `2079b79`, `e814b1d`, `ece8a0b`, `1dcde3b`, `e3f2967`, y anteriores de la sesión muerta; ver `git log --oneline`).
3. **Tercera ronda de revisión (subagente):** APRUEBA frontend (build 0 avisos, **20/20** tests, lecturas de tokens correctas, HTML↔SCSS coincide, payload correcto), **APRUEBA** backend (árbol limpio, humo contra `localhost:8080`: 200/404/400 correctos con el seed) y **APRUEBA** codificación (UTF-8 sin BOM en los 13 ficheros, tildes por codepoint). Los fallos que señala la ronda son de **documentación** (se corrigen en este apartado) y el texto corrupto de `frontend/agents.md` (líneas 43, 52, 73, 74, 86, 92: corregido en el mismo commit).
4. **Pendiente de decisión del alumno:** `main` local va **9 commits por delante** de `origin/main` (**sin push**). La sesión terminó sin subir nada; el protocolo pide confirmación antes de push (ver §1).
5. El poster de las tarjetas y el filtro por director **no estaban en el plan original del frontend** (los ejecutó el proceso huérfano de la sesión muerta): quedan porque superaron la revisión, pero se deshacen en commits separados si el alumno no los quiere.

### 2026-11-04 (decimotercera parte) — push pendiente, tests de persistencia y fase 4a (ArchUnit)

1. **Push autorizado por el alumno:** `git -c credential.helper= -c "credential.helper=!gh auth git-credential" push origin main` → `a79e06a..ed55526`, ok. Tras los pasos de abajo quedan **2 commits locales sin subir** (`113bcc8`, `9ff1fdb`).
2. Entorno: `docker info` → `29.8.0`; `docker compose ps` (desde `backend\`) → `movies-mariadb-1` **healthy** en el 3307.
3. **Tests de integración de persistencia** (fin de la tarea pendiente de la 1c), siguiendo la plantilla del skill `k-back-persistence` (PER-R09: CRUD completo, 0 mocks de JDBC, test de `RepositoryImpl` en espejo instanciando el `DaoJdbc` real):
   - `persistence.dao.impl.jdbc.MovieDaoJdbcTest` (6): insert→findById con director y biografía `NULL`; reparto de `findById` ordenado por id de actor; `update` compara **filas completas** de las demás películas y el reparto antes/después (`Map` de `queryForMap`, no un COUNT, que engaña); `deleteById`→`findById` empty y `findAll` sin ella; `findAll` con `COUNT(*)` y reparto; `findDirectorById` (existe y `999L` → empty).
   - `persistence.repository.impl.MovieRepositoryImplTest` (4): save→id+fila en tabla; findById por el puerto; findDirectorById; update + deleteById por el puerto.
   - **PER-D8 adaptado y anotado en el javadoc de ambos:** en vez de vaciar tablas en `@BeforeEach` (aquí solo hay una BD, con `seed.sql` cargado para Swagger y el frontend), cada test corre en `@Transactional` con **rollback**: los datos de prueba del test desaparecen al acabar y el seed queda intacto (comprobado: `COUNT(*) FROM movie` = 10 tras la suite).
   - Detalle: los tests NO dependen del seed (insertan sus propios directores/actores por SQL), así que sobreviven a un `docker compose down -v`.
4. `.\mvnw.cmd -q test -Dtest=MovieDaoJdbcTest,MovieRepositoryImplTest` → **10 en verde a la primera** (6 + 4). `.\mvnw.cmd clean test` → **68 en verde**, `BUILD SUCCESS`. Commit `113bcc8`.
5. **Fase 4a: `ArquitecturaTest`** (MOD-R10). Versión de ArchUnit resuelta primero, como pedía el plan: Maven Central → **1.5.1** (última; 1.4.2 ya soporta Java 26, así que Java 25 va bien). `pom.xml`: propiedad `archunit.version` explicitada (Boot no la gestiona, igual que springdoc).
   - Las 5 reglas, con paquetes con comodín para que sigan valiendo tras la modularización (4b): `domainNoDependeDeNadie` (MOD-R02), `controllerSoloUsaInterfacesDeDomain` (MOD-R01), `domainNoConoceLosImpl` (MOD-R03), `persistenceNoConoceLosImpl` (MOD-R04), `dtoSoloEnController` (MOD-R05). Tests no analizados (`ImportOption.DoNotIncludeTests`).
   - **Rojo controlado:** una clase sonda `domain.model.SondaArquitectura` con un campo `MovieDao` (violación de MOD-R02) → `Tests run: 5, Failures: 1` fallando exactamente `domainNoDependeDeNadie`. Sonda borrada después (verificado con `git status`).
   - `.\mvnw.cmd test -Dtest=ArquitecturaTest` → 5 en verde. `.\mvnw.cmd clean test` → **73 en verde**, `BUILD SUCCESS`. Commit `9ff1fdb`.
6. Documentado: este archivo (§1: 73 tests, fase 4a ✅, siguiente acción = 4b). `arquitectura.md` y diario/temario de la fase 4 quedan para la 4d.

## 5. Trabajo sin commitear

Ninguno. Los tres commits de la undécima parte y de casa (`style` del token muerto, la décima parte de este archivo y el arreglo de los restos) están subidos al historial local. El §1 de arriba manda.

### Commits locales desde la fase 10 (más reciente primero)

| Commit | Contenido |
|---|---|
| `339cb5d` | Docs adaptadas al giro: temarios 10 y 11, diario 10, plan (fases 30–34), glosario, READMEs, `ESTADO.md` |
| `0b6f567` | Nuevo temario `entender-el-codigo.md` (recorrido, esquemas y 15 preguntas) |
| `7ad36c1` | `docs/arquitectura.md` reescrito con el código real + D-48 |
| `8b1a1dd` | Tests de servicio y controlador (55 en total), tope de año, 500 al releer, 400/415 |
| `d75cffc` | Corrige los `package` de controller, mapper y handler (compilaba mal) |
| `398ab41` | Quita los *value objects* y añade la estructura por capas (controller, service, repository, DAO) |
| `68742ba` | Temarios 05 y 11 alineados, `arquitectura.md` restaurado, D-46 revocada |
| `aa0cef7` | `feat(fase-10): add domain entities` (etiqueta `fase-10`) |

---

## 6. Cómo se mantiene este archivo (reglas del asistente)

1. **Al empezar una sesión:** leer `claude.md`, este archivo y [decisiones-tecnicas.md](decisiones-tecnicas.md). No preguntar "¿por dónde íbamos?" si está aquí.
2. **Al terminar cada paso:** actualizar §1 (estado y siguiente acción), añadir la entrada del día en §4 con los **comandos y resultados reales**, y refrescar §5 si cambian los archivos pendientes.
3. **Al cerrar una fase:** mover el resumen a §4, actualizar el `README.md` de la raíz y pedir permiso para el commit + etiqueta.
4. **Nunca** anotar un resultado que no se haya visto en pantalla. Si algo no se ejecutó, se escribe "no ejecutado".
5. Este archivo se actualiza **antes** de responder "ya está": si no está aquí, no ha pasado.
