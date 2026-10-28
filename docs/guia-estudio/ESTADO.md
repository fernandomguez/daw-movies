# Estado del proyecto y bitácora

> **Para qué sirve este archivo:** es el parte de situación. Se lee **al empezar cualquier sesión** (humano o asistente de IA) y se **actualiza al terminar cada paso**, antes de cerrar. Así se puede empezar de cero, sin memoria de la conversación anterior, sin inventar nada.
>
> Última actualización: **2026-10-28**.

---

## 1. Dónde estamos (leer esto primero)

| Dato | Valor |
|---|---|
| Fase del plan | **8 — Flyway: migraciones del esquema → COMPLETA** (6 de 6 pasos). Pendiente solo el commit de la fase |
| Paso en curso | **ninguno**: el siguiente es la **fase 9, paso 9.1** (datos de prueba), tras el commit de la fase 8 |
| Último paso terminado | 8.6 — checksum: se rompió a propósito una migración, se leyó el error y se revirtió |
| Último commit | `ccbf5b7` — `feat(fase-07): connect Spring Boot to MariaDB with JPA and connection tests` |
| Etiqueta más reciente | `fase-07` |
| Tests | **4 en verde** (2 conexión + 1 Flyway + 1 contexto) |
| Tablas en `movies` | `flyway_schema_history` (4 filas, versiones 1–4), `director`, `actor`, `movie`, `movie_actor` |
| Cambios sin commitear | sí: `pom.xml`, test de Flyway, `V1`–`V4` y documentación (ver §5) |

### La siguiente acción, exacta

La fase 8 está terminada: **lo que toca es el commit único de la fase**, con permiso del alumno (D-30):

```powershell
git add -A
git commit -m "feat(fase-08): create schema with Flyway migrations"
git tag -a fase-08 -m "Fase 8: esquema creado con migraciones Flyway"
git push
git push origin fase-08
```

Después, **fase 9, paso 9.1**: decidir dónde viven los datos de prueba (migraciones nuevas solo para desarrollo, o un archivo aparte) y cargar los 5 directores, 10 películas y 14 actores de la [fase 2](02-alcance-del-dominio.md).

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

**Siguiente:** commit único de la fase 8 + etiqueta `fase-08` (con permiso), y después fase 9, paso 9.1.

---

## 5. Trabajo sin commitear (a 2026-10-28)

```
 M README.md
 M claude.md
 M docs/guia-estudio/00-plan-de-aprendizaje.md
 M docs/guia-estudio/02-alcance-del-dominio.md
 M docs/guia-estudio/03-diseno-del-esquema.md
 M docs/guia-estudio/06-creacion-proyecto-spring-boot.md
 M docs/guia-estudio/07-conexion-java-mariadb.md
 M docs/guia-estudio/README.md
 M docs/guia-estudio/decisiones-tecnicas.md
 M docs/guia-estudio/glosario.md
 M docs/guia-estudio/temario/03-diseno-esquema-relacional.md
 M docs/guia-estudio/temario/06-proyecto-spring-boot-y-git.md
 M docs/guia-estudio/temario/07-conexion-jdbc-datasource.md
 M pom.xml
?? docs/guia-estudio/08-flyway-migraciones.md
?? docs/guia-estudio/ESTADO.md                    ← este archivo
?? docs/guia-estudio/temario/08-diseno-de-endpoints.md
?? docs/guia-estudio/temario/08-migraciones-flyway.md
?? src/main/resources/db/                          ← V1–V4 (las cuatro migraciones)
?? src/test/java/com/daw/movies/FlywayMigrationTests.java
```

Todo esto entra en **un único commit** al cerrar la fase 8 (D-30): `feat(fase-08): ...`, etiqueta `fase-08`, `git push` y `git push origin fase-08`. **Solo con confirmación del alumno.**

---

## 6. Cómo se mantiene este archivo (reglas del asistente)

1. **Al empezar una sesión:** leer `claude.md`, este archivo y [decisiones-tecnicas.md](decisiones-tecnicas.md). No preguntar "¿por dónde íbamos?" si está aquí.
2. **Al terminar cada paso:** actualizar §1 (estado y siguiente acción), añadir la entrada del día en §4 con los **comandos y resultados reales**, y refrescar §5 si cambian los archivos pendientes.
3. **Al cerrar una fase:** mover el resumen a §4, actualizar el `README.md` de la raíz y pedir permiso para el commit + etiqueta.
4. **Nunca** anotar un resultado que no se haya visto en pantalla. Si algo no se ejecutó, se escribe "no ejecutado".
5. Este archivo se actualiza **antes** de responder "ya está": si no está aquí, no ha pasado.
