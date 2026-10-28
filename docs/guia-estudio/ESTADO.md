# Estado del proyecto y bitácora

> **Para qué sirve este archivo:** es el parte de situación. Se lee **al empezar cualquier sesión** (humano o asistente de IA) y se **actualiza al terminar cada paso**, antes de cerrar. Así se puede empezar de cero, sin memoria de la conversación anterior, sin inventar nada.
>
> Última actualización: **2026-10-28**.

---

## 1. Dónde estamos (leer esto primero)

| Dato | Valor |
|---|---|
| Fase del plan | **9 — Datos de prueba y comprobación de relaciones → COMPLETA** (5 de 5 pasos). Pendiente solo el commit de la fase |
| Paso en curso | **ninguno**: el siguiente es la **fase 10, paso 10.1** (modelo de dominio: anémico frente a rico), tras el commit de la fase 9 |
| Último paso terminado | 9.5 — `LEFT JOIN` y agregados (Sofia Coppola sin películas, `COUNT`/`AVG`, `HAVING`) |
| Último commit | **`3e38972`** — `feat(fase-08): create schema with Flyway migrations` |
| Etiqueta más reciente | **`fase-08`** (anotada y subida a `origin`) |
| Tests | **4 en verde** (2 conexión + 1 Flyway + 1 contexto); la fase 9 es SQL y no añadió tests |
| Tablas en `movies` | `flyway_schema_history` (4 filas, versiones 1–4), `director` (6), `actor` (14), `movie` (10), `movie_actor` (24) |
| Cambios sin commitear | los de la fase 9 (ver §5) |

### La siguiente acción, exacta

La **fase 9 está terminada** (pasos 9.1 a 9.5). Lo que toca es su **commit único** (D-30), con permiso del alumno, y después la **fase 10, paso 10.1**: decidir si el modelo de dominio es **anémico** (solo datos, la lógica fuera) o **rico** (datos + reglas) y justificarlo para este problema. En el 10.2 se escribirán `Movie`, `Director` y `Actor` **en Java puro**; las anotaciones JPA llegan en la fase 11 (D-29).

Antes de tocar nada: `docker info` responde y `docker compose ps` → `healthy` (los tests de integración lo necesitan; los unitarios de dominio, no). Los datos de prueba ya están cargados; si la base se recrea (`docker compose down -v`), hay que volver a cargarlos con la receta del [diario 09](09-datos-de-prueba.md#cómo-se-recarga-desde-cero).

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

---

## 5. Trabajo sin commitear

```
 M README.md
 M docs/guia-estudio/ESTADO.md
 M docs/guia-estudio/README.md
 M docs/guia-estudio/02-alcance-del-dominio.md
 M docs/guia-estudio/decisiones-tecnicas.md
 M docs/guia-estudio/glosario.md
?? docs/guia-estudio/09-datos-de-prueba.md
?? docs/guia-estudio/temario/09-datos-de-prueba-y-consultas.md
?? src/main/resources/db/testdata/seed.sql
```

Todo esto entra en el **único commit de la fase 9** (D-30): `feat(fase-09): ...`, etiqueta `fase-09`, `git push` y `git push origin fase-09`. **Solo con confirmación del alumno.** No hay cambios de código Java: la fase 9 es SQL.

---

## 6. Cómo se mantiene este archivo (reglas del asistente)

1. **Al empezar una sesión:** leer `claude.md`, este archivo y [decisiones-tecnicas.md](decisiones-tecnicas.md). No preguntar "¿por dónde íbamos?" si está aquí.
2. **Al terminar cada paso:** actualizar §1 (estado y siguiente acción), añadir la entrada del día en §4 con los **comandos y resultados reales**, y refrescar §5 si cambian los archivos pendientes.
3. **Al cerrar una fase:** mover el resumen a §4, actualizar el `README.md` de la raíz y pedir permiso para el commit + etiqueta.
4. **Nunca** anotar un resultado que no se haya visto en pantalla. Si algo no se ejecutó, se escribe "no ejecutado".
5. Este archivo se actualiza **antes** de responder "ya está": si no está aquí, no ha pasado.
