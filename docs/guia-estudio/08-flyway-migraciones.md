# Fase 8 — Flyway: migraciones del esquema

> Estado: **completada** (empezada el 2026-10-20, terminada el 2026-10-28). Conceptos: [temario/08-migraciones-flyway.md](temario/08-migraciones-flyway.md).

## En pocas palabras

> Este archivo es el **diario** de la fase. Para **entender** los conceptos, lee primero el [tema 08](temario/08-migraciones-flyway.md), que tiene también las preguntas de repaso.

Hasta ahora la base de datos `movies` está **vacía**. En esta fase, Flyway creará las cuatro tablas (`director`, `actor`, `movie` y `movie_actor`) a partir de archivos SQL numerados que guardaremos en Git.

## Objetivo

Que las tablas diseñadas en la [fase 3](03-diseno-del-esquema.md) (`director`, `actor`, `movie`, `movie_actor`) las cree **Flyway** al arrancar la aplicación, a partir de archivos SQL versionados en Git. Hibernate solo las validará (D-34).

## Pasos

| Paso | Estado |
|---|---|
| 8.1 Qué es una migración | ✅ Hecho (teoría + estado de partida) |
| 8.2 Diseño de los endpoints y revisión del esquema | ✅ Hecho (paso añadido al plan: sugerencia del profesor) |
| 8.3 Configurar Flyway | ✅ Hecho (rojo → verde, D-04 cerrada) |
| 8.4 Primera migración (`director`) | ✅ Hecho (tabla creada y apuntada en el historial) |
| 8.5 Migraciones con claves foráneas | ✅ Hecho (`actor`, `movie`, `movie_actor`; esquema v4) |
| 8.6 Inmutabilidad de las migraciones (checksum) | ✅ Hecho (error provocado y corregido) |

## 8.1 Qué es una migración

Paso teórico: no cambia ningún archivo del proyecto. La explicación completa está en el [temario 08](temario/08-migraciones-flyway.md).

### Estado de partida (comprobado)

```
> "SELECT COUNT(*) AS tables_in_movies FROM information_schema.tables WHERE table_schema='movies';" | docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" -t movies'
+------------------+
| tables_in_movies |
+------------------+
|                0 |
+------------------+
```

La base de datos `movies` existe (la creó Docker con `MARIADB_DATABASE`, fase 4) pero está **vacía**. Cuando Flyway se ejecute por primera vez creará su tabla de control `flyway_schema_history` y, después, nuestras tablas.

### Plan de migraciones

Una migración por tabla, en el orden que exigen las claves foráneas (una tabla solo puede apuntar a otra que ya exista):

| Archivo | Crea | Depende de |
|---|---|---|
| `V1__create_director_table.sql` | `director` | — |
| `V2__create_actor_table.sql` | `actor` | — |
| `V3__create_movie_table.sql` | `movie` | `director` |
| `V4__create_movie_actor_table.sql` | `movie_actor` | `movie`, `actor` |

El SQL sale del borrador de la [fase 3](03-diseno-del-esquema.md), con la columna `biography` que se añadió en el paso 8.2. Los nombres exactos se confirmarán al crearlas (pasos 8.4 y 8.5).

### Pendiente de comprobar en el paso 8.3

[D-04](decisiones-tecnicas.md): en la fase 1 vimos que Flyway declaraba compatibilidad verificada hasta MariaDB 11.7, y usamos la 12.3. Lo comprobaremos con la ejecución real.

## 8.2 Diseño de los endpoints y revisión del esquema

Paso de diseño: no cambia código, solo documentación. Los conceptos están en el [temario 08, diseño de endpoints](temario/08-diseno-de-endpoints.md).

### Por qué este paso, y por qué ahora

El profesor recomendó pensar los endpoints **antes de empezar una aplicación**. En este proyecto no se había hecho: el plan solo tenía ejemplos sueltos (`GET /movies`, `GET /movies/{id}`).

No hizo falta empezar de cero: Docker, la conexión y la arquitectura no dependen de los endpoints. Lo único que sí depende de ellos es el **esquema**, y todavía no se había creado ninguna tabla. Es el momento más barato para revisarlo: cambiar el diseño cuesta editar un documento; después costaría una migración nueva.

### Punto 1: de las pantallas a los endpoints

El alumno describió la navegación que quería:

1. Al entrar, se ven todas las películas.
2. Al pulsar una, se abre su ficha, con sus actores como lista de nombres.
3. En la ficha, el nombre del director es un enlace a sus datos (con su biografía) y a la lista de sus películas.

Cada pantalla pide sus datos a la API. Resultado ([D-36](decisiones-tecnicas.md)):

| # | Método | URL | Pantalla | Devuelve | Errores |
|---|---|---|---|---|---|
| 1 | `GET` | `/movies` | Inicio | Lista: `id`, `title`, `releaseYear` | — |
| 2 | `GET` | `/movies/{id}` | Ficha de película | `id`, `title`, `releaseYear`, `durationMinutes`, `director` (`id` + `fullName`), `actors` (`id` + `fullName`) | 404 / 400 |
| 3 | `GET` | `/directors/{id}` | Ficha de director | `id`, `firstName`, `lastName`, `birthDate`, `biography` | 404 / 400 |
| 4 | `GET` | `/directors/{id}/movies` | Ficha de director | Lista: `id`, `title`, `releaseYear` (`[]` si no tiene) | 404 / 400 |

Decisiones tomadas por el alumno durante el punto 1:

| Pregunta | Decisión |
|---|---|
| Ficha del director: ¿una petición con todo (A) o director y películas por separado (B)? | **B**: cada endpoint hace una cosa. |
| ¿Actores en la ficha de la película? | Sí, solo con el nombre "de momento". Se devuelve también su `id` para no cambiar el contrato si más adelante son enlaces. |
| ¿Qué pasa si la película no existe? | `404` con un cuerpo *Problem Details*. Se programará en la fase 18 (*exception handler*). |
| Director sin películas: ¿una excepción con el mensaje "no tiene películas"? | **No**: `200` con `[]`. El mensaje lo escribe el front ([D-39](decisiones-tecnicas.md)). |
| ¿Nombre del director junto o separado? | Separado (`firstName`, `lastName`) en su ficha; `fullName` cuando aparece dentro de otra respuesta. |

### Punto 2: comprobación contra el esquema de la fase 3

Para cada dato de cada respuesta, ¿de qué tabla y columna sale?

| Endpoint | Dato | Sale de |
|---|---|---|
| 1 y 4 | `id`, `title`, `releaseYear` | `movie` (el 4, filtrando por `movie.director_id`) |
| 2 | `durationMinutes` | `movie.duration_minutes` |
| 2 | `director.id` | `movie.director_id` |
| 2 | `director.fullName` | `director.first_name` + `" "` + `director.last_name` (**calculado**) |
| 2 | `actors[]` | `movie` → `movie_actor` → `actor` (relación N:M); `fullName` **calculado** |
| 3 | `firstName`, `lastName`, `birthDate` | `director` |
| 3 | `biography` | ❌ **No existía** |

Resultado:

- **Faltaba una columna:** la biografía del director. Se añade `biography TEXT NULL` a `director` y, por decisión del alumno, también a `actor` (vacía por ahora), para no tener que cambiar la tabla más adelante ([D-37](decisiones-tecnicas.md)). El esquema actualizado está en la [fase 3](03-diseno-del-esquema.md) y las biografías de prueba en la [fase 2](02-alcance-del-dominio.md).
- **El resto ya estaba cubierto.** La clave foránea `director_id` y la tabla `movie_actor` dan todas las relaciones que piden las pantallas.
- **El esquema no guarda ningún orden.** Una base de datos no garantiza el orden de las filas si no se pide, así que se decidieron los órdenes por defecto ([D-38](decisiones-tecnicas.md)):

| Lista | Orden |
|---|---|
| `GET /movies` | Alfabético por título |
| `GET /directors/{id}/movies` | Por año, del más antiguo al más reciente; si coincide el año, por título |
| Actores de una película | Alfabético por apellido y nombre. El orden de reparto queda para más adelante (nueva columna en `movie_actor` con una migración `V5`). |

- **Detalle para la fase 16:** para distinguir `404` (el director no existe) de `[]` (existe pero no tiene películas), primero hay que comprobar que el director existe y después buscar sus películas.

### Qué queda fuera de la v1 (a propósito)

Escritura (`POST`, `PUT`, `DELETE`), fichas de actor, filtros y ordenaciones elegidas por el usuario (`?sort=...`) y paginación. Se diseñarán cuando lleguen sus fases.

## 8.3 Configurar Flyway

> Empezado el 2026-10-26 y terminado el 2026-10-28: la primera sesión se cortó antes de empezar porque Docker Desktop estaba cerrado (`failed to connect to the docker API at npipe:////./pipe/dockerDesktopLinuxEngine`). Se retomó abriéndolo.

### Paso 1: el test del rojo (TDD)

El test va primero: [`FlywayMigrationTests`](../../src/test/java/com/daw/movies/FlywayMigrationTests.java) comprueba con `JdbcTemplate` que existe la tabla `flyway_schema_history` en la base de datos `movies`. Si Flyway se ha ejecutado, esa tabla existe; si no, no.

Con MariaDB arrancado (`docker compose up -d --wait`, estado `healthy`) y **sin** Flyway todavía en el `pom.xml`:

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0 -- in com.daw.movies.FlywayMigrationTests
[ERROR]   FlywayMigrationTests.flywayCreatesItsSchemaHistoryTableOnStartup:25
expected: 1
 but was: 0
[INFO] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
[ERROR] BUILD FAILURE
```

Es el rojo correcto: la base `movies` está vacía, nadie ha creado la tabla de control. Los otros 3 tests siguen en verde.

### Paso 2: las dependencias

Dos artefactos, **sin versión** (la elige el *parent* de Spring Boot, D-26):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

| Artefacto | Para qué |
|---|---|
| `spring-boot-starter-flyway` | Trae Flyway y la **autoconfiguración**: al arrancar el contexto, Spring busca las migraciones y las ejecuta. No hay que escribir código. |
| `flyway-mysql` | Módulo de Flyway con el dialecto de MySQL/MariaDB. Sin él, Flyway no sabe hablar con MariaDB. |

La versión que resuelve el *parent*, comprobada con `.\mvnw.cmd dependency:tree "-Dincludes=org.flywaydb:*"`:

```
[INFO] \- org.flywaydb:flyway-mysql:jar:12.4.0:compile
[INFO]    \- org.flywaydb:flyway-core:jar:12.4.0:compile
[INFO] BUILD SUCCESS
```

**Sobre la última versión (13.11.1):** se puede forzar con la propiedad `flyway.version`, pero Spring Boot 4.1.1 está construido y probado con la 12.4.0, y el *starter* es código suyo. Se deja la gestionada hasta que haya un motivo (ver D-40).

### Paso 3: el verde, y el aviso de D-04

```
Database: jdbc:mariadb://localhost:3307/movies?user=movies_app&password=******** (MariaDB 12.3)
WARN  o.f.c.internal.database.base.Database : Using MariaDB 12.3 which is newer than the version
      Flyway has been verified with. The latest verified version of MariaDB is 11.7.
Schema history table `movies`.`flyway_schema_history` does not exist yet
Successfully validated 0 migrations
WARN  o.f.core.internal.command.DbValidate : No migrations found. Are your locations set up correctly?
Creating Schema History table `movies`.`flyway_schema_history` ...
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Qué significa cada línea:

1. **`Using MariaDB 12.3 ... latest verified version ... 11.7`** es el riesgo de [D-04](decisiones-tecnicas.md) confirmado, y **no bloquea nada**: es un `WARN`, Flyway sigue, crea la tabla y no falla. La base de datos no está "rota": solo no es una versión contra la que el fabricante haya hecho pruebas.
2. **`No migrations found`** también es un `WARN` esperado: la carpeta `db/migration` aún no existe. Se resuelve en el paso 8.4.
3. **`Creating Schema History table`** es el momento exacto en que la tabla del test aparece.

### Paso 4: comprobación por SQL (fuera de Java)

```
+-----------------------+
| Tables_in_movies      |
+-----------------------+
| flyway_schema_history |
+-----------------------+
```

La tabla existe y está **vacía**: Flyway ha creado su libro de registro, todavía sin obras apuntadas.

### Errores de esta sesión

| Síntoma | Causa | Solución |
|---|---|---|
| `failed to connect to the docker API ... dockerDesktopLinuxEngine` (otra vez) | Docker Desktop cerrado | Abrirlo y esperar: `docker info` responde en segundos |
| `Could not transfer metadata /.meta/prefixes.txt from/to flyway-community-db-support ... HTTP Status: 401` | Los `pom.xml` de Flyway declaran repositorios extra (GitHub Packages, Azure) que Maven consulta al buscar metadatos; sin credenciales, responden 401 | Ninguna: es un `WARNING` de Maven, no de Flyway; la compilación sigue (`BUILD SUCCESS`) |
| `SELECT command denied ... mysql.user` | `movies_app` no puede leer la tabla de usuarios de MariaDB | Es lo correcto: mínimo privilegio (D-17). Para ver usuarios, usar `root` |

## 8.4 Primera migración: la tabla `director`

### Archivo

`src/main/resources/db/migration/V1__create_director_table.sql`:

```sql
CREATE TABLE director (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    birth_date DATE         NOT NULL,
    biography  TEXT         NULL,
    CONSTRAINT pk_director PRIMARY KEY (id)
);
```

Es el borrador de la [fase 3](03-diseno-del-esquema.md) tal cual, con la columna `biography` que pidió el paso 8.2 (D-37). El nombre `V1` y los **dos** guiones bajos son lo que hace que Flyway lo reconozca.

### Ejecución

```
Migrating schema `movies` to version "1 - create director table"
WARN  o.f.c.i.s.DefaultSqlScriptExecutor : DB: Name 'pk_director' ignored for PRIMARY key.
      (SQL State:  - Error Code: 1280)
Successfully applied 1 migration to schema `movies`, now at version v1 (execution time 00:00.032s)
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **`Migrating ... to version "1"`**: Flyway ha encontrado el archivo, no estaba en su libro y lo ha ejecutado.
- **`now at version v1`**: el libro queda actualizado; en el siguiente arranque no hace nada.
- **`Name 'pk_director' ignored for PRIMARY key`**: MariaDB **ignora el nombre de una clave primaria**. En MySQL/MariaDB la PK se llama siempre `PRIMARY`; ponerle nombre no es un error, pero no sirve de nada y Flyway avisa. Ver D-41.

### Comprobación por SQL

```
+-----------------------+
| Tables_in_movies      |
+-----------------------+
| director              |
| flyway_schema_history |
+-----------------------+

CREATE TABLE `director` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `birth_date` date NOT NULL,
  `biography` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci

+----------------+---------+-----------------------+------+---------+
| installed_rank | version | description           | type | success |
+----------------+---------+-----------------------+------+---------+
|              1 | 1       | create director table | SQL  |       1 |
+----------------+---------+-----------------------+------+---------+
```

Tres cosas que confirma esta salida:

1. La tabla existe con las 5 columnas y `AUTO_INCREMENT` en `id`.
2. `DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci`: la tabla **hereda** el juego de caracteres de la base de datos, que se fijó en `compose.yaml` (D-16). No hace falta repetirlo en cada `CREATE TABLE`, y eso es lo que comprobamos aquí.
3. `flyway_schema_history` tiene **una fila** con `version = 1`, `description = create director table` y `success = 1`: el libro apunta la obra.

### Aviso 1280: nombres de restricción

En `director` solo hay una restricción, la clave primaria, y MariaDB la ignora por su nombre. Las que **sí** conservan el nombre son las claves foráneas y los `CHECK` (`fk_movie_director`, `chk_movie_duration_positive`), y ahí el nombre vale oro: sale en los mensajes de error (temario 03, sección 12).

## 8.5 Migraciones con claves foráneas: `actor`, `movie` y `movie_actor`

Tres archivos nuevos, en el orden que imponen las dependencias (una FK solo puede apuntar a una tabla que ya exista):

| Archivo | Tabla | Depende de |
|---|---|---|
| `V2__create_actor_table.sql` | `actor` | — |
| `V3__create_movie_table.sql` | `movie` | `director` (`V1`) |
| `V4__create_movie_actor_table.sql` | `movie_actor` | `movie` (`V3`) y `actor` (`V2`) |

### Ejecución

```
Current version of schema `movies`: 1
Migrating schema `movies` to version "2 - create actor table"
WARN : DB: Name 'pk_actor' ignored for PRIMARY key. (Error Code: 1280)
Migrating schema `movies` to version "3 - create movie table"
WARN : DB: Name 'pk_movie' ignored for PRIMARY key. (Error Code: 1280)
Migrating schema `movies` to version "4 - create movie actor table"
WARN : DB: Name 'pk_movie_actor' ignored for PRIMARY KEY. (Error Code: 1280)
Successfully applied 3 migrations to schema `movies`, now at version v4 (execution time 00:00.112s)
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Fíjate en **`Current version of schema movies: 1`**: Flyway no vuelve a ejecutar la `V1`. Lee su libro, ve que ya está, y solo aplica de la 2 a la 4. Eso es exactamente lo que promete una migración.

Los tres avisos 1280 son el mismo de la `V1` (D-41: se conserva el nombre en el SQL y MariaDB lo ignora en la clave primaria).

### Comprobación por SQL

```
+-----------------------+
| Tables_in_movies      |
+-----------------------+
| actor                 |
| director              |
| flyway_schema_history |
| movie                 |
| movie_actor           |
+-----------------------+

CREATE TABLE `movie` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `release_year` int(11) NOT NULL,
  `duration_minutes` int(11) NOT NULL,
  `director_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_movie_director` (`director_id`),
  CONSTRAINT `fk_movie_director` FOREIGN KEY (`director_id`) REFERENCES `director` (`id`),
  CONSTRAINT `chk_movie_duration_positive` CHECK (`duration_minutes` > 0),
  CONSTRAINT `chk_movie_release_year` CHECK (`release_year` >= 1888)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci

CREATE TABLE `movie_actor` (
  `movie_id` bigint(20) NOT NULL,
  `actor_id` bigint(20) NOT NULL,
  PRIMARY KEY (`movie_id`,`actor_id`),
  KEY `fk_movie_actor_actor` (`actor_id`),
  CONSTRAINT `fk_movie_actor_actor` FOREIGN KEY (`actor_id`) REFERENCES `actor` (`id`),
  CONSTRAINT `fk_movie_actor_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci
```

Y las reglas de borrado, consultadas en `information_schema` (que es donde están de verdad, no solo en el texto del `.sql`):

```
| constraint_name      | table_name  | referenced_table_name | delete_rule |
| fk_movie_director    | movie       | director              | RESTRICT    |
| fk_movie_actor_actor | movie_actor | actor                 | RESTRICT    |
| fk_movie_actor_movie | movie_actor | movie                 | CASCADE     |

| constraint_name             | check_clause           |
| chk_movie_duration_positive | `duration_minutes` > 0 |
| chk_movie_release_year      | `release_year` >= 1888 |
```

### Tres detalles que sorprenden al mirar el `SHOW CREATE TABLE`

1. **`ON DELETE RESTRICT` no aparece en `movie`.** MariaDB no lo escribe porque `RESTRICT` es el comportamiento **por defecto**: si no dices nada, ya es restrictivo. La prueba de que está activo es la tabla de `information_schema`: `delete_rule = RESTRICT`. Y que no aparezca no significa que no exista.
2. **Aparece un `KEY fk_movie_director (director_id)` que no escribimos.** Al crear una FK, MariaDB crea sola un índice sobre la columna; sin él, comprobar la relación fila a fila sería lentísimo. No es una tabla ni una restricción de más: es el índice que hace falta para que la FK funcione rápido.
3. **La PK compuesta es `PRIMARY KEY (movie_id, actor_id)`**: la **pareja** es única, no cada columna por separado. La misma película puede tener muchos actores, y el mismo actor muchas películas, pero "Keanu en Matrix" solo una vez (D-14).

### Lo que este paso **no** hace (a propósito)

No inserta datos ni prueba los errores de FK (`ERROR 1452`, `ERROR 4025`): eso es la fase 9, pasos 9.2 y 9.3, con los datos de prueba reales. Aquí solo se comprueba que el esquema se crea como se diseñó.

## 8.6 Inmutabilidad de las migraciones: el checksum

### El experimento

Se añadió **una línea de comentario** al final de `V1__create_director_table.sql`, que ya estaba aplicada:

```sql
-- Prueba de inmutabilidad del paso 8.6
```

Solo un comentario. Nada que cambie el esquema. Y `.\mvnw.cmd test`:

```
ERROR ... o.s.boot.SpringApplication : Application run failed
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory' ...
Failed to initialize dependency 'flywayInitializer' ... :
Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 1
Either revert the changes to the migration, or run repair to update the schema history.
```

La aplicación **no arranca**. Ni siquiera se ejecutan los tests: el fallo ocurre al construir el contexto.

### Cómo se lee ese error

```
BeanCreationException (entityManagerFactory)
  └─ depende de 'flywayInitializer'      ← Flyway se ejecuta ANTES que JPA
       └─ Validate failed
            └─ Migration checksum mismatch for migration version 1
```

- **`checksum mismatch for migration version 1`**: el contenido del archivo ya no coincide con la huella que se guardó en `flyway_schema_history` cuando se aplicó. Da igual que sea un comentario: el checksum se calcula sobre el archivo, no sobre el efecto en la base de datos.
- **`entityManagerFactory` → `flywayInitializer`**: confirma con un error real lo que decía el temario. Hibernate (JPA) no arranca hasta que Flyway termina, porque el inspector necesita que el arquitecto haya acabado.
- **`Either revert the changes to the migration, or run repair`**: las dos salidas que ofrece Flyway.

### Sobre `flyway repair` (no se usa aquí)

`repair` **reescribe el checksum guardado** para que coincida con el archivo actual. Es cómodo... y peligroso: oculta el problema en tu base de datos, pero los demás (y producción) siguen con la versión anterior, y su historial y el tuyo ya no cuentan la misma historia. Solo tiene sentido cuando el cambio fue inofensivo **y** se coordina con todo el mundo. Aquí, lo correcto es deshacer.

### La solución: revertir

Se quitó la línea añadida y volvió a ejecutarse:

```
Successfully validated 4 migrations
Current version of schema `movies`: 4
Schema `movies` is up to date. No migration necessary.
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **`Successfully validated 4 migrations`**: el checksum vuelve a coincidir.
- **`Schema movies is up to date. No migration necessary.`**: no se ejecutó nada; las tablas no se recrean.

### Incidente real de este paso: PowerShell estropeó el archivo

Al revertir con PowerShell (`Get-Content` + `Set-Content`) en lugar de con el editor:

1. `Get-Content` leyó el archivo UTF-8 como si fuera ANSI → las tildes se convirtieron en caracteres rotos y se guardaron así.
2. Además, `Set-Content -Encoding utf8` en PowerShell 5.1 escribe **UTF-8 con BOM** (los bytes `EF BB BF` al principio).

Se detectó comparando los primeros bytes del archivo con los de `V2`–`V4`, y se corrigió reescribiéndolo con la codificación correcta y sin BOM.

**Lecciones:**

- Los archivos que llevan texto con tildes se editan con el editor, no con tuberías de PowerShell.
- Un BOM no rompe Flyway (el checksum siguió validando con y sin él), pero ensucia el archivo y algunas herramientas lo muestran como `?` o `\ufeff`.
- Aun así, el experimento valió: el error del checksum fue el de verdad, no una simulación.

## Cierre de la fase 8

Los seis pasos están hechos. Queda lo administrativo: actualizar el `README.md` de la raíz (hecho), revisar el diff, **pedir permiso** y hacer el commit único de la fase con su etiqueta (D-30):

```
feat(fase-08): create schema with Flyway migrations
git tag -a fase-08 -m "..."
git push && git push origin fase-08
```

### Cambios sin commit al cerrar esta sesión (2026-10-28)

- **Código:** test `FlywayMigrationTests`, las dos dependencias del `pom.xml` y las cuatro migraciones `V1`–`V4`. `application.yaml` **no** se ha tocado (Flyway usa sus valores por defecto: `classpath:db/migration`).
- **Documentación:** diario de la fase 8 (pasos 8.1 a 8.6), temarios 08 (los dos), D-36 a D-41, biografía en el esquema (fases 2 y 3), glosario, `ESTADO.md` y la reescritura de los temarios 06 y 07.
