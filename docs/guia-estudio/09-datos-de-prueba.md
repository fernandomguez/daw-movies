# Fase 9 — Datos de prueba y comprobación de relaciones

> Estado: **completada** (2026-10-28). Conceptos: [temario/09-datos-de-prueba-y-consultas.md](temario/09-datos-de-prueba-y-consultas.md).

## En pocas palabras

> Este archivo es el **diario** de la fase. Para **entender** los conceptos, lee primero el [tema 09](temario/09-datos-de-prueba-y-consultas.md), que tiene también las preguntas de repaso.

El esquema ya existía, pero estaba vacío. En esta fase cargamos los datos ficticios de la [fase 2](02-alcance-del-dominio.md) —**6 directores, 10 películas, 14 actores y 24 participaciones**— con un script SQL aparte, comprobamos que las claves foráneas hacen su trabajo (provocando errores a propósito) y practicamos las consultas con `JOIN`, `LEFT JOIN` y agregados que la API necesitará en la fase 16.

## Objetivo

1. Decidir **dónde viven** los datos de prueba y por qué no deben ser una migración de esquema.
2. Cargarlos en el orden que exigen las claves foráneas.
3. Ver con errores reales qué protege la base de datos.
4. Saber escribir las consultas que recuperan película + director y película + actores.

## Pasos

| Paso | Estado |
|---|---|
| 9.1 Esquema vs. datos de prueba | ✅ Hecho (script separado, D-42) |
| 9.2 `INSERT` y orden por claves foráneas | ✅ Hecho (6 + 10 + 14 + 24 filas) |
| 9.3 Provocar un error de FK a propósito | ✅ Hecho (`1452`, `1451`, `4025`, `1062`) |
| 9.4 Consultas con `JOIN` | ✅ Hecho (dos y tres tablas) |
| 9.5 `LEFT JOIN` y agregados | ✅ Hecho (director sin películas, `COUNT`, `AVG`, `HAVING`) |

## 9.1 Esquema vs. datos de prueba

### La pregunta

El esquema (tablas, columnas, restricciones) es **estructura** y se versiona con Flyway. Los datos ficticios son **contenido de desarrollo**: se pueden borrar y volver a cargar sin consecuencias. Mezclarlos tiene trampas.

### Lo que probamos primero (y por qué se descartó)

La primera idea fue una migración `V100__insert_test_data.sql` en una carpeta aparte (`db/testdata`), activada solo con un perfil `dev` de Spring:

```yaml
spring:
  flyway:
    locations: classpath:db/migration,classpath:db/testdata
```

Funcionaba (`Successfully applied 1 migration ... now at version v100`), pero al ejecutar los tests **sin** el perfil apareció esto:

```
Current version of schema `movies`: 100
WARN : Schema `movies` has a version (100) that is newer than the latest available migration (4) !
```

El problema no es el aviso: es que el **historial de Flyway es uno solo** para el esquema y los datos. Si la base de desarrollo queda en la versión 100, la próxima migración de esquema (`V5`) sería una migración "fuera de orden" y Flyway no la aplicaría sin cambiar configuración (`outOfOrder`). Además, dos programadores tendrían historiales distintos según hayan cargado o no los datos.

Se deshizo el experimento: se borraron las filas insertadas, se eliminó la fila `version = 100` de `flyway_schema_history` y se reiniciaron los `AUTO_INCREMENT`. El esquema volvió a estar en la versión 4, como al terminar la fase 8.

### La decisión: un script normal, fuera de Flyway (D-42)

```
src/main/resources/
└── db/
    ├── migration/          ← esquema, versionado por Flyway (V1–V4)
    │   ├── V1__create_director_table.sql
    │   ├── V2__create_actor_table.sql
    │   ├── V3__create_movie_table.sql
    │   └── V4__create_movie_actor_table.sql
    └── testdata/           ← datos ficticios, NO los ejecuta Flyway
        └── seed.sql
```

`db/testdata` **no está** en `spring.flyway.locations`, así que Flyway ni lo mira. Los tests siguen diciendo `Successfully validated 4 migrations` y no aparece ningún aviso de versión.

### Cómo se carga

Dos comandos. El primero copia el archivo **dentro** del contenedor y el segundo lo ejecuta allí:

```powershell
docker compose cp src/main/resources/db/testdata/seed.sql mariadb:/tmp/seed.sql
docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" movies < /tmp/seed.sql'
```

**Por qué no se le pasa el archivo por la entrada estándar de PowerShell** (`Get-Content seed.sql | docker compose exec ...`): PowerShell 5.1 lee el archivo como ANSI y lo reescribe al enviarlo, y los acentos y eñes se rompen (el mismo problema que estropeó la `V1` en el paso 8.6). Con `docker compose cp` el archivo viaja byte a byte y la redirección `<` la hace el shell de Linux, dentro del contenedor.

Resultado real:

```
 movies-mariadb-1 Copying src/main/resources/db/testdata/seed.sql to movies-mariadb-1:/tmp/seed.sql
 movies-mariadb-1 Copied src/main/resources/db/testdata/seed.sql to movies-mariadb-1:/tmp/seed.sql
exit=0
```

### Cómo se recarga desde cero

El script solo inserta; si se ejecuta dos veces, la primera fila ya existe y MariaDB responde `ERROR 1062`. Para empezar de nuevo, se vacían las tablas **en el orden inverso a las claves foráneas** (primero las participaciones, al final los directores) y se reinician los contadores:

```powershell
$sql = @'
DELETE FROM movie_actor;
DELETE FROM movie;
DELETE FROM actor;
DELETE FROM director;
ALTER TABLE director AUTO_INCREMENT = 1;
ALTER TABLE movie AUTO_INCREMENT = 1;
ALTER TABLE actor AUTO_INCREMENT = 1;
'@
$sql | docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" movies'
```

Si algún día se hace `docker compose down -v`, el volumen desaparece: hay que volver a arrancar (Flyway recrea el esquema) y volver a cargar la semilla.

## 9.2 `INSERT` y orden por claves foráneas

### El archivo

`src/main/resources/db/testdata/seed.sql`, en cuatro bloques:

| Bloque | Tabla | Filas |
|---|---|---|
| 1 | `director` | 6 (los 5 de la fase 2 + Sofia Coppola, D-43) |
| 2 | `movie` | 10 |
| 3 | `actor` | 14 |
| 4 | `movie_actor` | 24 |

El orden **no es capricho**: `movie.director_id` apunta a `director.id`, y `movie_actor` apunta a `movie` y a `actor`. Si se insertara una película antes que su director, MariaDB la rechazaría con `ERROR 1452`.

### Los ids

Las tablas están vacías, así que el primer `INSERT` recibe el `id` 1, el segundo el 2... En el script los escribimos **a mano** para que se lea quién es quién (`director_id = 1` es Nolan) y para poder escribir las 24 participaciones sin subconsultas. Es una decisión válida **solo** porque la tabla está vacía y el script se ejecuta una vez. En una base con datos habría que omitir `id` y dejar que `AUTO_INCREMENT` lo asigne.

### Comprobación

```
+-------------+-------+
| tabla       | filas |
+-------------+-------+
| director    |     6 |
| actor       |    14 |
| movie       |    10 |
| movie_actor |    24 |
+-------------+-------+
```

Y las relaciones, contadas desde los dos lados:

```
| director            | peliculas |
| Christopher Nolan   |         4 |
| Quentin Tarantino   |         2 |
| Martin Scorsese     |         2 |
| Guillermo del Toro  |         1 |
| Alejandro Amenábar  |         1 |
| Sofia Coppola       |         0 |

| actor               | peliculas |
| Leonardo DiCaprio   |         4 |
| Michael Caine       |         4 |
| Christian Bale      |         2 |
| Margot Robbie       |         2 |
| Matt Damon          |         2 |
| Matthew McConaughey |         2 |
| ...                 |         1 |
```

Coincide exactamente con la tabla de la [fase 2](02-alcance-del-dominio.md). Además, las tres consultas de "huérfanos" dieron **0**:

```
| peliculas_sin_director |
|                      0 |

| participaciones_sin_pelicula | participaciones_sin_actor |
|                            0 |                         0 |
```

Los `LEFT JOIN` de esas comprobaciones son la forma estándar de buscar hijos sin padre: se conservan todas las filas de la tabla hija y se filtra por las que no encontraron pareja (`WHERE d.id IS NULL`).

## 9.3 Provocar errores a propósito

Cinco `INSERT`/`DELETE` que **deben** fallar. Se ejecutaron uno a uno y se copió el mensaje real:

| # | Operación | Error | Qué demuestra |
|---|---|---|---|
| 1 | Película con `director_id = 99` | `ERROR 1452 (23000): Cannot add or update a child row: a foreign key constraint fails (movies.movie, CONSTRAINT fk_movie_director ...)` | No se puede crear un hijo que apunte a un padre inexistente |
| 2 | `DELETE` del director 1 (tiene 4 películas) | `ERROR 1451 (23000): Cannot delete or update a parent row: a foreign key constraint fails (movies.movie, CONSTRAINT fk_movie_director ...)` | `ON DELETE RESTRICT` protege al padre (D-13) |
| 3 | Película con `duration_minutes = 0` | `ERROR 4025 (23000): CONSTRAINT chk_movie_duration_positive failed for movies.movie` | El `CHECK` se cumple siempre, venga el dato de donde venga |
| 4 | Participación repetida (Bale en *The Prestige*) | `ERROR 1062 (23000): Duplicate entry '1-3' for key 'PRIMARY'` | La PK compuesta impide repetir la pareja (D-14) |
| 5 | Película con `release_year = 1800` | `ERROR 4025 (23000): CONSTRAINT chk_movie_release_year failed for movies.movie` | El segundo `CHECK`, con su nombre en el mensaje |

### Cómo se leen

- **`1452`** = intento de insertar/actualizar un **hijo** con una FK que no existe. Es el error del `INSERT`.
- **`1451`** = intento de borrar/actualizar un **padre** que aún tiene hijos y la FK es `RESTRICT`. Es el error del `DELETE`.
- **`4025`** = un `CHECK` con nombre ha fallado. El nombre (`chk_movie_duration_positive`) sale en el mensaje: por eso vale la pena ponerle nombre a las restricciones.
- **`1062`** = clave duplicada. Con una PK compuesta, el "duplicado" es la **pareja** (`'1-3'`), no una columna suelta.
- **`(23000)`** es el **SQLSTATE**: un código estándar de SQL para "violación de restricción de integridad". Los cuatro errores comparten SQLSTATE porque los cuatro son eso: la base diciendo "esto rompe una regla".

Los cinco intentos **no dejaron rastro**: al fallar la sentencia, no se insertó ni se borró nada. Se comprobó que seguía habiendo 6 directores y 10 películas.

> Detalle de PowerShell: al ejecutar estos comandos, la consola añade un bloque `NativeCommandError` porque MariaDB escribe el error por la salida de error. Es ruido de PowerShell, no un error distinto: la línea que importa es la que empieza por `ERROR 14xx` / `ERROR 4025` / `ERROR 1062`.

## 9.4 Consultas con `JOIN`

### Película + director (dos tablas)

```sql
SELECT m.id, m.title, m.release_year, CONCAT(d.first_name, ' ', d.last_name) AS director
FROM movie m
INNER JOIN director d ON d.id = m.director_id
ORDER BY m.title;
```

```
| id | title                         | release_year | director            |
|  9 | El laberinto del fauno        |         2006 | Guillermo del Toro  |
|  3 | Inception                     |         2010 | Christopher Nolan   |
|  4 | Interstellar                  |         2014 | Christopher Nolan   |
| 10 | Mar adentro                   |         2004 | Alejandro Amenábar  |
|  6 | Once Upon a Time in Hollywood |         2019 | Quentin Tarantino   |
|  5 | Pulp Fiction                  |         1994 | Quentin Tarantino   |
|  2 | The Dark Knight               |         2008 | Christopher Nolan   |
|  7 | The Departed                  |         2006 | Martin Scorsese     |
|  1 | The Prestige                  |         2006 | Christopher Nolan   |
|  8 | The Wolf of Wall Street       |         2013 | Martin Scorsese     |
```

`ON d.id = m.director_id` es la condición que empareja cada película con su director. Sin `ON`, el resultado sería el **producto cartesiano**: 10 × 6 = 60 filas sin sentido.

El `ORDER BY m.title` reproduce el orden que decidió [D-38](decisiones-tecnicas.md) para `GET /movies`.

### Las películas de un director

```sql
SELECT m.id, m.title, m.release_year, m.duration_minutes
FROM movie m
INNER JOIN director d ON d.id = m.director_id
WHERE d.last_name = 'Nolan'
ORDER BY m.release_year, m.title;
```

```
| 1 | The Prestige    | 2006 | 130 |
| 2 | The Dark Knight | 2008 | 152 |
| 3 | Inception       | 2010 | 148 |
| 4 | Interstellar    | 2014 | 169 |
```

Es el orden de `GET /directors/{id}/movies` (año y, si coincide, título). Se filtra por `last_name` aquí solo para la prueba; en la API se filtrará por `director_id`.

### Película + reparto (tres tablas)

Para llegar de `movie` a `actor` hay que pasar por la tabla intermedia:

```sql
SELECT m.title,
       GROUP_CONCAT(CONCAT(a.first_name, ' ', a.last_name)
                    ORDER BY a.last_name, a.first_name SEPARATOR ', ') AS actores,
       COUNT(*) AS num_actores
FROM movie m
INNER JOIN movie_actor ma ON ma.movie_id = m.id
INNER JOIN actor a ON a.id = ma.actor_id
GROUP BY m.id, m.title
ORDER BY m.title;
```

```
| El laberinto del fauno        | Ivana Baquero, Maribel Verdú                                  | 2 |
| Inception                     | Michael Caine, Leonardo DiCaprio                              | 2 |
| Interstellar                  | Michael Caine, Matt Damon, Anne Hathaway, Matthew McConaughey | 4 |
| Mar adentro                   | Javier Bardem                                                 | 1 |
| Once Upon a Time in Hollywood | Leonardo DiCaprio, Brad Pitt, Margot Robbie                   | 3 |
| Pulp Fiction                  | Samuel L. Jackson, Uma Thurman                                | 2 |
| The Dark Knight               | Christian Bale, Michael Caine                                 | 2 |
| The Departed                  | Matt Damon, Leonardo DiCaprio                                 | 2 |
| The Prestige                  | Christian Bale, Michael Caine, Hugh Jackman                   | 3 |
| The Wolf of Wall Street       | Leonardo DiCaprio, Matthew McConaughey, Margot Robbie         | 3 |
```

- **`GROUP BY m.id, m.title`**: una fila por película, con todos sus actores juntos. Sin él, `GROUP_CONCAT` juntaría el reparto de **todas** las películas en una sola celda.
- **`GROUP_CONCAT(... ORDER BY a.last_name, a.first_name)`**: el orden de D-38 (apellido y nombre). `GROUP_CONCAT` no garantiza orden si no se le pide.
- **`COUNT(*)`** aquí cuenta participaciones, es decir, actores por película (todas las filas tienen actor porque es un `INNER JOIN`).

### La misma relación desde el actor

```sql
SELECT CONCAT(a.first_name, ' ', a.last_name) AS actor,
       GROUP_CONCAT(m.title ORDER BY m.release_year SEPARATOR ' | ') AS peliculas
FROM actor a
INNER JOIN movie_actor ma ON ma.actor_id = a.id
INNER JOIN movie m ON m.id = ma.movie_id
GROUP BY a.id, actor
ORDER BY actor;
```

```
| Leonardo DiCaprio   | The Departed | Inception | The Wolf of Wall Street | Once Upon a Time in Hollywood |
| Michael Caine       | The Prestige | The Dark Knight | Inception | Interstellar                 |
| Christian Bale      | The Prestige | The Dark Knight                                               |
...
```

La relación N:M se lee igual de bien desde los dos lados: esa es la prueba de que la tabla intermedia está bien planteada.

## 9.5 `LEFT JOIN` y agregados

### La diferencia entre `INNER` y `LEFT`

```sql
SELECT
  (SELECT COUNT(DISTINCT d.id) FROM director d INNER JOIN movie m ON m.director_id = d.id) AS con_inner_join,
  (SELECT COUNT(DISTINCT d.id) FROM director d LEFT JOIN movie m ON m.director_id = d.id) AS con_left_join;
```

```
| con_inner_join | con_left_join |
|              5 |             6 |
```

Sofia Coppola no tiene películas: el `INNER JOIN` la **pierde** porque no encuentra pareja. El `LEFT JOIN` conserva todas las filas de la izquierda (`director`) y rellena con `NULL` lo que no existe a la derecha.

### Películas por director, incluido el que no tiene

```sql
SELECT CONCAT(d.first_name, ' ', d.last_name) AS director,
       COUNT(m.id) AS peliculas,
       COUNT(*) AS filas_del_join,
       IFNULL(SUM(m.duration_minutes), 0) AS minutos_totales,
       ROUND(AVG(m.duration_minutes), 1) AS media_minutos
FROM director d
LEFT JOIN movie m ON m.director_id = d.id
GROUP BY d.id, director
ORDER BY peliculas DESC, director;
```

```
| director            | peliculas | filas_del_join | minutos_totales | media_minutos |
| Christopher Nolan   |         4 |              4 |             599 |         149.8 |
| Martin Scorsese     |         2 |              2 |             331 |         165.5 |
| Quentin Tarantino   |         2 |              2 |             315 |         157.5 |
| Alejandro Amenábar  |         1 |              1 |             125 |         125.0 |
| Guillermo del Toro  |         1 |              1 |             118 |         118.0 |
| Sofia Coppola       |         0 |              1 |               0 |          NULL |
```

Esta tabla es la lección más importante del paso:

1. **`COUNT(m.id)` = 0, `COUNT(*)` = 1** para Sofia. `COUNT(*)` cuenta **filas**; la fila de Sofia existe aunque su película sea `NULL`. `COUNT(columna)` cuenta valores **no nulos**. Con `LEFT JOIN`, para contar hijos se usa `COUNT(columna_del_hijo)`.
2. **`SUM` y `AVG` ignoran los `NULL`**. `AVG` de Sofia es `NULL` (no hay nada que promediar), no 0. Si se quiere 0, se envuelve con `IFNULL(...)`/`COALESCE(...)`; `SUM` ya devuelve `NULL` y aquí se convirtió a 0 con `IFNULL`.
3. **`GROUP BY d.id, director`**: se agrupa por la PK y por el alias. Agrupar solo por `director` funcionaría hoy, pero dos personas pueden llamarse igual.

### `HAVING` no es `WHERE`

```sql
SELECT CONCAT(d.first_name, ' ', d.last_name) AS director, COUNT(m.id) AS peliculas
FROM director d
INNER JOIN movie m ON m.director_id = d.id
GROUP BY d.id, director
HAVING COUNT(m.id) > 1
ORDER BY peliculas DESC, director;
```

```
| Christopher Nolan |         4 |
| Martin Scorsese   |         2 |
| Quentin Tarantino |         2 |
```

`WHERE` filtra **filas antes** de agrupar; `HAVING` filtra **grupos después** de agrupar. `WHERE COUNT(...) > 1` sería un error.

### Actores por película con `LEFT JOIN`

```sql
SELECT m.title, COUNT(ma.actor_id) AS actores
FROM movie m
LEFT JOIN movie_actor ma ON ma.movie_id = m.id
GROUP BY m.id, m.title
ORDER BY actores DESC, m.title;
```

```
| Interstellar                  |       4 |
| Once Upon a Time in Hollywood |       3 |
| The Prestige                  |       3 |
| The Wolf of Wall Street       |       3 |
| El laberinto del fauno        |       2 |
| Inception                     |       2 |
| Pulp Fiction                  |       2 |
| The Dark Knight               |       2 |
| The Departed                  |       2 |
| Mar adentro                   |       1 |
```

Hoy todas las películas tienen reparto. Si alguna no lo tuviera, este `LEFT JOIN` la mostraría con `0`; un `INNER JOIN` la haría desaparecer. Para `GET /movies/{id}` interesa que la película aparezca aunque no tenga actores.

### Agrupar por año

```sql
SELECT release_year, COUNT(*) AS peliculas, GROUP_CONCAT(title ORDER BY title SEPARATOR ' + ') AS titulos
FROM movie
GROUP BY release_year
HAVING COUNT(*) > 1
ORDER BY release_year;
```

```
| 2006 | 3 | El laberinto del fauno + The Departed + The Prestige |
```

`GROUP BY` no exige agrupar por una relación: vale cualquier columna. Aquí se agrupa por año para contar cuántas películas hay en cada uno.

## Errores de esta sesión

| Síntoma | Causa | Solución |
|---|---|---|
| `Schema movies has a version (100) that is newer than the latest available migration (4) !` | El primer diseño cargaba los datos como migración `V100` en una carpeta aparte con perfil `dev`, y el historial de Flyway es único | Descartar la migración: los datos van en un script fuera de Flyway (D-42). Se borró la fila `100` del historial y se vaciaron las tablas |
| La consola muestra `NativeCommandError` al provocar los errores | PowerShell trata la salida de error de `mariadb` como un error de PowerShell | Es ruido: leer la línea `ERROR 14xx` / `4025` / `1062`, que es la de MariaDB |
| Si el script se ejecuta dos veces: `ERROR 1062 ... Duplicate entry '1' for key 'PRIMARY'` | El script solo inserta y los ids ya existen | Vaciar las tablas en orden inverso y reiniciar los `AUTO_INCREMENT` (receta arriba) |

## Decisiones de esta fase

- [D-42](decisiones-tecnicas.md): los datos de prueba viven en `db/testdata/seed.sql`, fuera de Flyway.
- [D-43](decisiones-tecnicas.md): sexto director (Sofia Coppola) sin películas para practicar `LEFT JOIN`.

## Resumen

| Antes | Después |
|---|---|
| Esquema creado y vacío | Esquema + 6 directores, 10 películas, 14 actores y 24 participaciones |
| Flyway era lo único que tocaba la base | Flyway crea el esquema; un script carga los datos |
| No sabíamos qué protege la base | Vistos `1452`, `1451`, `4025` y `1062` con su SQLSTATE |
| Consultas de una sola tabla | `JOIN` de dos y tres tablas, `LEFT JOIN`, `GROUP BY`, `HAVING`, `COUNT`/`SUM`/`AVG` |

## Siguiente paso

**Fase 10, paso 10.1**: modelo de dominio (anémico frente a rico). Las clases `Movie`, `Director` y `Actor` se escribirán en Java puro en el paso 10.2; las anotaciones JPA llegan en la fase 11 (D-29).
