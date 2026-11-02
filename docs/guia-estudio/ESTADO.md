# Estado del proyecto y bitácora

> **Para qué sirve este archivo:** es el parte de situación. Se lee **al empezar cualquier sesión** (humano o asistente de IA) y se **actualiza al terminar cada paso**, antes de cerrar. Así se puede empezar de cero, sin memoria de la conversación anterior, sin inventar nada.
>
> Última actualización: **2026-11-02**.

---

## 1. Dónde estamos (leer esto primero)

| Dato | Valor |
|---|---|
| Fase del plan | **11 — Fundamentos de JPA → CERRADA y publicada** (6 de 6 pasos) |
| Paso en curso | **12.1** — DAO frente a Repositorio (se crea el diario 12 al empezar) |
| Último paso terminado | 11.6 — `FetchType.LAZY` en `director` (D-49) |
| Último commit | `f3b9b13` — `feat(fase-11): map the domain with JPA` + etiqueta anotada `fase-11` |
| Etiqueta más reciente | **`fase-11`** (anotada y publicada) |
| Tests | **60 en verde** (7 de integración + 28 de los *value objects* + 20 de las entidades + 5 de mapeo JPA) |
| Tablas en `movies` | `flyway_schema_history` (4 filas, versiones 1–4), `director` (6), `actor` (14), `movie` (10), `movie_actor` (24) |
| Cambios sin commitear | ninguno (ver §5) |

### La siguiente acción, exacta

La **fase 11 está cerrada** (los 6 pasos hechos). Lo siguiente es el **12.1** (fase 12, Repositorios y adaptadores): la tabla pide crear el diario al empezar. El **commit único `feat(fase-11): ...` + etiqueta anotada `fase-11`** está pendiente de la confirmación del alumno (D-30).

El **11.6** ha cerrado el mapeo: `@ManyToOne(fetch = FetchType.LAZY)` en `Movie.director`, decidido con un test de comportamiento (el rojo mostró que con el default `EAGER` el `find` carga el objeto real; con `LAZY`, Hibernate entrega un *proxy*). La regla del proyecto queda en **D-49: todas las relaciones perezosas**, sin depender de defaults desiguales (*ManyToOne* ansioso, *ManyToMany* perezoso). Deuda anotada: los *proxies* afectarán a `equals`/`hashCode` (fase 14). Ver [diario 11, §11.6](11-fundamentos-jpa.md).

**Segundo frente abierto:** la **fase 15A** (API First) tiene hechos 15A.1 (contrato `openapi.yaml`) y 15A.2 (springdoc 3.2.0 + Swagger UI). El **15A.3** no se puede hacer hasta que existan los controladores (fase 16): compara el documento generado `/v3/api-docs` con el contrato. La UI está en `http://localhost:8080/swagger-ui.html` y **no ejecuta** nada todavía ("Try it out" → `404`).

**Mapa de la documentación:** [INDICE.md](INDICE.md) lista todas las fases y subfases con su estado y el archivo donde se explican. Es el sitio al que ir para saber *qué leer* de cada fase.

**Recorrido por la historia:** [RECORRIDO.md](RECORRIDO.md) explica qué etiqueta visitar en cada fase (`fase-01` a `fase-15A`), qué mirar y qué necesita cada parada para compilar o arrancar.

Antes de tocar nada: `docker info` responde y `docker compose ps` → `healthy` (los tests de integración lo necesitan; los unitarios de dominio, no). Los datos de prueba ya están cargados; si la base se recrea (`docker compose down -v`), hay que volver a cargarlos con la receta del [diario 09](09-datos-de-prueba.md#cómo-se-recarga-desde-cero).

**Decidido:** se adoptan *value objects* como pide el profesor (D-46). El coste se paga en la fase 11, al mapearlos con `AttributeConverter`/`@Embeddable`.

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

> Los hashes de commit anteriores al **2026-10-31** son los que se usaron de verdad en su momento. Al rehacer la historia para el recorrido guiado, esos commits ya no están en `main`. Se conservaron en la rama `historial-antes-de-recorrido` y la etiqueta `pre-recorrido` hasta el **2026-11-01**, cuando se borraron (ver bitácora de ese día). Los hashes se dejan como registro de lo ejecutado, aunque ya no se puedan visitar.

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

### 2026-10-31 (tercera parte) — historia rehecha para poder recorrerla

A petición del alumno: poder empezar en el primer commit e ir viajando fase a fase. Se reescribió la historia (**sin tocar el contenido**: el árbol final es idéntico, `git diff` vacío contra el anterior).

1. **Red de seguridad:** rama `historial-antes-de-recorrido` y etiqueta anotada `pre-recorrido` en `a2e44e3`.
2. **Fases 1–5 separadas:** el commit raíz `d2a88c4` se dividió en cinco commits por fase, repartiendo sus archivos (`docs(fase-01)`…`docs(fase-05)`). Los transversales (plan, decisiones, glosario, README) quedan en la parada 1.
3. **15A movido detrás de la fase 10:** en el commit de 10.3 y 10.4 se quitaron los artefactos del 15A (`openapi.yaml`, `OpenApiContractTests`, `pom.xml` y `application.yaml` con springdoc, diario/temario/captura), que se añaden en su propia parada. El código de las paradas 10.3 y 10.4 es el mismo de antes, sin Swagger.
4. **Etiquetas:** borradas las antiguas `fase-05`…`fase-10` y creadas `fase-01`…`fase-09`, `fase-10` y `fase-15A`, todas anotadas. La numeración con dos cifras ordena bien.
5. **Comprobado:** los 9 commits con `pom.xml` (fase 6 a índice) compilan con `.\mvnw.cmd -q -DskipTests compile`.
6. **Documentado:** [RECORRIDO.md](RECORRIDO.md) (paradas, qué mirar, requisitos), [INDICE.md](INDICE.md), README de la guía y D-30.
7. **Publicado** el mismo día: rama `historial-antes-de-recorrido`, etiqueta `pre-recorrido`, `main` con `--force-with-lease` y las once etiquetas `fase-01`…`fase-15A` (las cinco que ya existían, con `--force`). Comprobado con `git ls-remote` que cada etiqueta remota apunta al mismo commit que la local.

### 2026-10-31 (cuarta parte) — fase 11, paso 11.1

1. **Teoría, sin código:** qué es un ORM, qué problema resuelve y por qué hay cuatro piezas distintas (JPA, Hibernate, Spring Data JPA y Spring Boot). Explicado en la conversación y escrito en el [diario 11](11-fundamentos-jpa.md).
2. **Comprobado en el proyecto**, no supuesto: `.\mvnw.cmd dependency:tree "-Dincludes=org.hibernate.orm:*,jakarta.persistence:*"` → `spring-boot-hibernate 4.1.1` trae `hibernate-core 7.4.5.Final` y `jakarta.persistence-api 3.2.0`. El `pom.xml` solo declara `spring-boot-starter-data-jpa`.
3. **Decisiones que se recalcan:** nuestro código importará `jakarta.persistence`, nunca `org.hibernate`; Hibernate no crea tablas (`ddl-auto: validate`, D-34); los repositorios no son JPA, son Spring Data (fase 12).
4. **Sin tocar código:** 55 tests siguen en verde y no hay ninguna entidad mapeada todavía.
5. **Documentado:** [diario 11](11-fundamentos-jpa.md) (nuevo), [INDICE.md](INDICE.md) (11.1 ✅, 11.2 ⏳, contadores) y este archivo.
6. **Sin commit:** la fase 11 se cierra al terminar el 11.6 (D-30).

**Siguiente:** paso 11.2 (JPA en las clases del dominio, D-29).

### 2026-11-01 — limpieza de las ramas de seguridad

1. **Comprobado** que `main` contiene todo: `src/` idéntico al de la historia antigua.
2. **Borradas** la rama `historial-antes-de-recorrido`, la etiqueta `pre-recorrido` y la rama local `backup/historial-original` (apuntaba a `b166f79`), en local y en GitHub.
3. **Resultado:** `git branch -a` → solo `main` y `origin/main`; `git ls-remote --heads --tags origin` → `main` y las once etiquetas `fase-01`…`fase-15A`. `git lg --all` muestra una sola línea recta de 15 commits.
4. **Documentos corregidos** para no hablar de ramas que ya no existen: nota del [diario 06](06-creacion-proyecto-spring-boot.md), D-30 en [decisiones-tecnicas.md](decisiones-tecnicas.md) y este archivo (aviso de la bitácora y §5). Las entradas del 2026-10-31 se dejan como estaban: son historia.
5. **Commit suelto** `docs: remove references to deleted backup branches` (corrección de documentación, no pertenece a ninguna fase).

**Siguiente:** paso 11.2 (sin cambios).

### 2026-11-01 (segunda parte) — fase 11, paso 11.2

1. **Justificación escrita, sin código:** D-29 al detalle en el [diario 11, §11.2](11-fundamentos-jpa.md#112-jpa-en-las-clases-del-dominio-d-29).
2. **Qué impone JPA**, comprobado contra el código real de la fase 10: constructor sin argumentos (`protected`), campos no `final`, clase no `final`, acceso por campo (`@Id` en el campo → Hibernate no llama a los *setters*), colección mutable y no `final`, `id` `Long` generado, mapeo exacto del esquema (`validate`). Cinco de los ocho ya estaban resueltos sin querer.
3. **Coste explicado sin adornos:** al leer, Hibernate crea la instancia por el constructor vacío y escribe los campos por reflexión, así que **las comprobaciones del constructor de la entidad no se ejecutan**. Los *value objects* sí se validan, porque se construyen por su constructor. La garantía real pasa a ser el esquema (`NOT NULL`, `CHECK`, claves foráneas).
4. **Deuda detectada para el 11.3:** `PersonName` ocupa dos columnas y no cabe en un `AttributeConverter`. Se barajan `@Embeddable` en el `record`, columnas planas o volver a clase mutable. Se decide **con una prueba** sobre Hibernate 7.4.5, no por suposición.
5. **Sin tocar código:** 55 tests en verde, ninguna entidad mapeada, ninguna anotación añadida.
6. **Documentado:** [diario 11](11-fundamentos-jpa.md), [INDICE.md](INDICE.md) (11.2 ✅, 11.3 ⏳, contadores 52/40) y este archivo.
7. **Sin commit:** la fase 11 se cierra al terminar el 11.6 (D-30).

**Siguiente:** paso 11.3 (anotar `Director` y `Actor` + conversión de los *value objects*).

### 2026-11-01 (tercera parte) — fase 11, paso 11.3

1. Entorno: `docker info` → `29.8.0`; `docker compose ps` → `movies-mariadb-1` **healthy** en el 3307. Línea base: `.\mvnw.cmd test` → **55 en verde**.
2. **Rojo:** creado `EntityMappingTests` (2 tests: los 6 directores del seed con `name` reconstruido y DiCaprio con `biography == null`, leyendo con `EntityManager`). `.\mvnw.cmd test "-Dtest=EntityMappingTests"` → **rojo por el motivo correcto**: `Unknown entity type 'com.daw.movies.domain.model.Actor'` y `Could not resolve root entity 'Director'`.
3. **Verde:** anotadas `Director` y `Actor` (`@Entity`, `@Table`, `@Id` + `@GeneratedValue(IDENTITY)`, `@Embedded` en `name`); `PersonName` como `@Embeddable` con `@Column` en los componentes del `record`; cinco `AttributeConverter` (`Title`, `ReleaseYear`, `DurationMinutes`, `BirthDate`, `Biography`) con `@Converter(autoApply = true)`. Primera ejecución → **verde a la primera**, lo que salda con una prueba la deuda del 11.2 (D-48).
4. Observaciones anotadas: el `validate` pasa en silencio (solo habla cuando algo no encaja); no hubo problema mapeando `String` sobre la columna `TEXT` de `biography`.
5. `.\mvnw.cmd clean test` → **57 en verde** (7 de integración + 28 de los *value objects* + 20 de las entidades + 2 de mapeo), `BUILD SUCCESS`. Los nuevos no tocan las tablas: solo leen filas del seed.
6. Documentado: [diario 11, §11.3](11-fundamentos-jpa.md), [temario 11](temario/11-introduccion-jpa.md) (sección nueva con el código real, preguntas nuevas, avisos actualizados), **D-48**, glosario (`@Embeddable`, `EntityManager`, `JPQL`), `INDICE.md` (11.3 ✅, 11.4 ⏳, contadores 53/39) y este archivo.
7. **Sin commit:** la fase 11 se cierra al terminar el 11.6 (D-30).

**Siguiente:** paso 11.4 (`@ManyToOne` + `@JoinColumn` en `Movie.director`).

### 2026-11-01 (cuarta parte) — fase 11, paso 11.4

1. **Rojo:** cuarto test en `EntityMappingTests` (`mapsTheSeedMoviesAndTheirDirector`: 10 películas + *Inception* con su director). Error real: `Could not resolve root entity 'Movie'` (JPQL).
2. Rojo técnico (el primero del validate en el proyecto): anotada la entidad, el arranque rompió con `Schema validation: missing column [duration] in table [movie]`. Causa: el campo `duration` y el nombre de la columna `duration_minutes`; la conversión *camelCase → snake_case* (estrategia de Spring Boot) no puede deducir la palabra que falta. Corregido con `@Column(name = "duration_minutes")`.
3. **Verde:** `Movie` anotada: `@Entity`, `@Table`, `@Id` + `@GeneratedValue(IDENTITY)`, tres *value objects* sin `@Convert` (los converters del 11.3 se aplican con `autoApply`), `@ManyToOne` + `@JoinColumn(name = "director_id", nullable = false)` en `director` (lado dueño de la FK, unidireccional) y `@Transient` en `actors` hasta el 11.5.
4. `.\mvnw.cmd clean test` → **58 en verde**, `BUILD SUCCESS`.
5. Documentado: [diario 11, §11.4](11-fundamentos-jpa.md), [temario 11](temario/11-introduccion-jpa.md) (sección 12 con el código real y 4 preguntas), glosario (`@ManyToOne`/`@JoinColumn`, *lado dueño*, `@Transient`), `INDICE.md` (11.4 ✅, 11.5 ⏳, contadores 54/38) y este archivo.
6. **Sin commit:** la fase 11 se cierra al terminar el 11.6 (D-30).

**Siguiente:** paso 11.5 (`@ManyToMany` + `@JoinTable` en `Movie.actors`).

### 2026-11-01 (quinta parte) — fase 11, paso 11.5

1. **Rojo distinto:** quinto test en `EntityMappingTests` (`mapsTheCastOfInceptionThroughTheJoinTable`). Error real: `expected: <2> but was: <0>` — un **fallo de aserción**, no un error de arranque: la película se cargaba y el reparto venía vacío por el `@Transient` del 11.4. El test señalaba el hueco del mapeo, no una entidad rota.
2. **Verde:** el import `jakarta.persistence.Transient` desaparece y `actors` pasa a ser relación: `@ManyToMany` + `@JoinTable(name = "movie_actor")`, `joinColumns = movie_id` (columna propia) e `inverseJoinColumns = actor_id` (columna ajena). Primera ejecución → verde.
3. Matices anotados: el `validate` también comprueba la tabla intermedia (sigue sin crearse nada); la `@ManyToMany` es `LAZY` por defecto, de modo que `getActors()` genera un `SELECT` aparte en el test; sin `cascade` guardar `Movie` no guarda actores (D-14).
4. `.\mvnw.cmd clean test` → **59 en verde**, `BUILD SUCCESS`.
5. Documentado: [diario 11, §11.5](11-fundamentos-jpa.md), [temario 11](temario/11-introduccion-jpa.md) (sección 13 con el código real y 2 preguntas nuevas), glosario (`@ManyToMany`/`@JoinTable`), `INDICE.md` (11.5 ✅, 11.6 ⏳, contadores 55/37) y este archivo. En el glosario se detectó que un cambio sobrescribía la entrada `@Embeddable` de 11.3 y se restauró.
6. **Sin commit:** la fase 11 se cierra al terminar el 11.6 (D-30), con permiso del alumno.

**Siguiente:** paso 11.6 (`FetchType`: `LAZY` vs. `EAGER`), el último de la fase.

### 2026-11-01 (sexta parte) — fase 11, paso 11.6 y cierre de la fase

1. **Decisión propuesta y aplicada (D-49):** todas las relaciones perezosas. El contexto: `director` cargaba `EAGER` por defecto (`@ManyToOne`) y `actors` `LAZY` por defecto (`@ManyToMany`) — defaults desiguales que conviene no memorizar sino declarar.
2. **Rojo** (test de comportamiento, no de anotación): `loadsTheDirectorLazily` con `assertNotEquals(Director.class, movie.getDirector().getClass())` → `expected: not equal but was: <class com.daw.movies.domain.model.Director>`. Con el default `EAGER`, el `find` carga el objeto real.
3. **Verde:** `@ManyToOne(fetch = FetchType.LAZY)` en `Movie.director`. Con `LAZY`, Hibernate entrega un *proxy* (subclase generada en caliente) y el test pasa. `actors` queda sin `fetch` explícito: el default de `@ManyToMany` ya es perezoso, y la regla queda en D-49 y en el javadoc.
4. `.\mvnw.cmd clean test` → **60 en verde** (7 de integración + 28 de los *value objects* + 20 de las entidades + 5 de mapeo), `BUILD SUCCESS`.
5. Deuda anotada para la fase 14: los *proxies* alteran la clase devuelta por los getters de relación; afectará a `equals`/`hashCode`.
6. Documentado: [diario 11, §11.6 y cierre](11-fundamentos-jpa.md), **D-49**, [temario 11](temario/11-introduccion-jpa.md) (sección 14 y 3 preguntas), glosario (`LAZY`/`EAGER`, *proxy*), `INDICE.md` (fase 11 ✅, contadores 56/36), [RECORRIDO.md](RECORRIDO.md) (parada 15 con `fase-11`), `README.md` de la raíz (estado 1–11, 60 tests, D-44–D-49, errores reales del validate) y este archivo.
7. **Fase 11 cerrada y publicada:** commit `f3b9b13` (`feat(fase-11): map the domain with JPA`, 18 archivos, +927/−52), etiqueta anotada `fase-11` y push de ambos a `origin` (`8602541..f3b9b13  main -> main`, `[new tag] fase-11`).

**Siguiente:** fase 12, paso 12.1 (DAO frente a Repositorio), con su diario nuevo.

### 2026-11-01 (séptima parte) — repaso general y sesión de dudas (sin código)

- **Repaso pedido** de la fase 11 (y de todo lo anterior) hecho en la conversación: qué se hizo fase a fase, cómo funcionan JPA/Hibernate/Spring, entidades, anotaciones y qué se hace delante (anotaciones, tests) frente a detrás (SQL generado, proxies, hidratación). No se añadieron archivos: el contenido permanente ya está en [temario/11](temario/11-introduccion-jpa.md) y en los temarios de fases previas.
- **Dudas resueltas en la conversación** (todas apoyadas en lo ya documentado, sin tocar código):
  1. **Volumen de Docker (fase 4):** los datos viven en el volumen con nombre `movies_mariadb-data`, gestionado por Docker en el disco del host, fuera del contenedor; sobrevive a `docker compose down`, se pierde solo con `down -v`. Coherente con [compose.yaml](../../compose.yaml).
  2. **Fase 5:** recordatorio de las tres capas y el DIP (el dominio no depende de nadie; su puerto, `MovieRepository`, lo implementa la persistencia). Documento: [temario/05](temario/05-arquitectura-tres-capas-dip.md).
  3. **`DataSource` y HikariCP** (fase 7): interfaz que entrega conexiones + pool reutilizable. **`ddl-auto` (D-34):** por qué `validate` (Hibernate comprueba, no crea; el dueño del esquema es Flyway) y cómo fue de útil en el 11.4 (error real con `duration_minutes`).
  4. **`record` y su `value` (fase 10):** el componente se define en la declaración (`record Title(String value)`), genera campo `final` + accesor `value()` + `equals`/`hashCode` por valor; el constructor compacto valida en toda creación.
  5. **`LazyInitializationException` y `equals`:** el precio del `LAZY` (11.6/D-49) y por qué las entidades todavía no implementan igualdad (el `id` llega tras insertar; los proxies de fase 14 lo dificultan).
  6. **Consulta abierta del alumno:** conectarse a datos de IMDb. Conclusión anotada (no hay BD de IMDb conectable): cargar datasets oficiales en MariaDB sería bajo coste; una API externa encaja como adaptador del puerto (fase 12 y DIP); una BD ajena requeriría un segundo DataSource y revisar D-34. Nada anotado en el plan: es una idea no comprometida.
- **Tests en verde al cerrar:** 60. **Siguiente sesión:** fase 12, paso 12.1.

**Siguiente:** fase 12, paso 12.1 (DAO frente a Repositorio), con su diario nuevo.

---

### 2026-11-02 — documento de comparación con la práctica P1 del profesor

1. Recuperadas del historial las conclusiones de la comparación contra el temario del 2026-10-28; la página de la **práctica** ([P1](https://cesguiro.es/doku.php/clase/daw/dws/practicas/p1), publicada el 2026-10-31) no estaba cubierta: la comparación anterior se hizo contra el temario (la página `practicas` aún no existía).
2. Creado un `comparacion-practica-p1.md` (descartado más tarde el mismo día): mapa hitos ↔ fases con el estado real (etiquetas, tests, commits), las seis divergencias deliberadas razonadas (D-29, D-34, D-42/43, paginación, recorrido JDBC→JPA) y las tres preguntas pendientes para el profesor.
3. Añadido a [INDICE.md](INDICE.md) (documentos transversales).
4. **Solo documentación**: no se tocó código ni tests; siguen los 60 en verde de la última ejecución (no se han vuelto a ejecutar).

**Siguiente:** fase 12, paso 12.1 (sin cambios por esta sesión).

### 2026-11-02 (segunda parte) — el profesor responde y el repo se deja a punto para su revisión

Se comparte el repositorio con el profesor para que dé el visto bueno. Con ello se resuelven las tres preguntas:

1. **Modularidad:** es **requisito del curso** — se empieza como monolito normal y se pasa a modular (confirma D-45).
2. **Dominio:** el dominio propio vale — lo que se cubren son **los mismos hitos**; la API de películas sirve.
3. **JWT/Spring Security:** **entran en este proyecto** (hitos 08–09 de su práctica).

Cambios, todo documentación:

1. **Plan:** fase 24 🟡→🔴 y fase 25 🟡→🔴; encabezado corregido (ya no dice «modular solo si la aplicación lo requiere»); tabla de cobertura (DWS 04, DWS 06/07, Prog 12) e intro del Bloque F anotan la confirmación con fecha.
2. **D-45 corregida:** la modularización deja de ser condicional («solo si la aplicación lo requiere»); es requisito confirmado y la fase 25 fija el momento.
3. **README de la raíz:** nota de contexto para la revisión del profesor (itinerario de la práctica con dominio propio, 11 fases etiquetadas, 60 tests, OpenAPI + Swagger), fase 24 pasa a 🔴 y sale de la fila «Opcionales» (que queda con 21–23 ⚪), fila 25 anotada como requisito.
4. **[comparacion-practica-p1.md]** creado y **descartado el mismo día** por petición del alumno: no llegó a comitearse, así que no queda huella en git; el contenido útil (estado, divergencias, confirmaciones) vive en el README, el plan, D-45 y este archivo.
5. **[INDICE.md](INDICE.md):** fila 24 → 🔴; fila 25 igual; entrada transversal del documento descartado retirada.
6. **Verificación con tests (petición del alumno, antes de comitear):** Docker Desktop estaba cerrado → abierto con `Start-Process`, `docker info` → `29.8.0`; `docker compose up -d --wait` → `movies-mariadb-1` **healthy** en el 3307; `.\mvnw.cmd clean test` → **60 en verde**, `BUILD SUCCESS`, `Successfully validated 4 migrations` (2 conexión + 1 Flyway + 1 contexto + 3 de contrato + 5 de mapeo + 28 de *value objects* + 20 de entidades).
7. **Commit y push** del aparte de documentación (sin etiqueta; no es una fase): ver §5.
8. **Solo documentación:** no se tocó código Java; la única ejecución fue la suite de tests, en verde.

**Siguiente:** fase 12, paso 12.1 (sin cambios).

---

## 5. Trabajo sin commitear

```
nada: el árbol está limpio.
```

El **2026-11-02 se comiteó y subió** el aparte de documentación de las confirmaciones del profesor:

```
git add -A
git commit -m "docs: record the professor's confirmations and prepare the repo for review"
# [main 509fa07] 5 files changed, 68 insertions(+), 17 deletions(-)
git push          # ede9bec..509fa07  main -> main
```

Este apunte del hash entra en un `docs` suelto posterior (el commit anterior no puede contener su propio hash). Verificación posterior: `git status` → árbol limpio y sincronizado con `origin/main`.

| Archivo | Cambio |
|---|---|
| `README.md` | nota para la revisión del profesor; 24/25 → 🔴 (dependencias y progreso) |
| `00-plan-de-aprendizaje.md` | 24 y 25 → 🔴; encabezado y cobertura del temario |
| `decisiones-tecnicas.md` | D-45 corregida (confirmación 2026-11-02) |
| `INDICE.md` | fila 24 → 🔴; fecha de actualización |
| `ESTADO.md` | esta bitácora y este §5 |

**El 2026-11-01 se cerró la fase 11** con el commit único `f3b9b13` (`feat(fase-11): map the domain with JPA`, 18 archivos: 5 converters y `EntityMappingTests` nuevos, `Director`/`Actor`/`Movie`/`PersonName` anotados y toda la documentación) y la etiqueta anotada `fase-11`, ambas publicadas. Este pequeño aparte del commit no recogía su propio hash (imposible de conocer antes de comitear); se apunta con un commit docs cuyo contenido describe el anterior.

| Commit | Frente | Contenido |
|---|---|---|
| `f3b9b13` | **Fase 11** | `feat(fase-11): map the domain with JPA` — D-48, D-49, 60 tests |

Los apuntes del 11.2 (y el dato verificado sobre `record` como `@Embeddable`) entraron en `docs(fase-11): add 11.2 notes on the D-29 trade-off`. La fase 11 sigue abierta (D-30): su commit de cierre llegará al terminar el 11.6.

**Cerrada la fase 10 el 2026-10-31.** Su commit es `feat(fase-10): add domain entities` (incluye la ampliación de los temas 10 y 11 hecha al final de la sesión) y la etiqueta `fase-10`, **publicada**.

**El 2026-10-31 se rehízo la historia** para poder recorrerla fase a fase (D-30): las fases 1–5 se separaron en cinco commits, el 15A se movió detrás de la fase 10 y se renombraron las etiquetas a `fase-01`…`fase-09`. El árbol final es **idéntico** al anterior salvo las notas de la reescritura (mismo contenido, hashes nuevos). La historia antigua se conservó en la rama `historial-antes-de-recorrido` y en la etiqueta `pre-recorrido` hasta el **2026-11-01**, cuando se borraron de local y de GitHub tras comprobar que `main` lo contiene todo. Todo quedó publicado ese mismo día: `main` con `--force-with-lease` y las etiquetas con `--force`.

| Commit | Frente | Contenido |
|---|---|---|
| `f2ffe69` | **Fase 8** | `feat(fase-08): create schema with Flyway migrations` |
| `9973cf8` | **Fase 9** | `feat(fase-09): add test data seed and verify SQL relationships` |
| `e82b78f` | **Fase 10** (documentación) | `docs(fase-10): design rich domain model and six value objects` — D-44, D-45, D-46 |
| `450961f` | **Fase 10, paso 10.3** | `feat(fase-10): add six value objects with TDD` — seis `record` + `Texts` (35 tests) |
| `aa0cef7` | **Fase 10, paso 10.4** | `feat(fase-10): add domain entities` — `Director`, `Actor`, `Movie` y 20 tests (55 en verde) |
| `daf027e` | **Fase 15A** | `feat(fase-15A): add OpenAPI contract and Swagger UI` — D-47 |
| `HEAD` | **Guía** | `docs: add study guide index, guided tour and history notes` — `INDICE.md`, `RECORRIDO.md` y notas de la reescritura |

**Etiquetas:** `fase-01` a `fase-10` y `fase-15A`. La 15A sigue abierta (falta 15A.3, que necesita los controladores de la fase 16).

---

## 6. Cómo se mantiene este archivo (reglas del asistente)

1. **Al empezar una sesión:** leer `claude.md`, este archivo y [decisiones-tecnicas.md](decisiones-tecnicas.md). No preguntar "¿por dónde íbamos?" si está aquí.
2. **Al terminar cada paso:** actualizar §1 (estado y siguiente acción), añadir la entrada del día en §4 con los **comandos y resultados reales**, y refrescar §5 si cambian los archivos pendientes.
3. **Al cerrar una fase:** mover el resumen a §4, actualizar el `README.md` de la raíz, marcar la fase como ✅ en [INDICE.md](INDICE.md) (y sumar los pasos hechos) y pedir permiso para el commit + etiqueta.
4. **Nunca** anotar un resultado que no se haya visto en pantalla. Si algo no se ejecutó, se escribe "no ejecutado".
5. Este archivo se actualiza **antes** de responder "ya está": si no está aquí, no ha pasado.
