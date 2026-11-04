# 09 — Datos de prueba, `INSERT` y consultas con `JOIN`

> **Fase del plan:** 9 (datos de prueba y comprobación de relaciones). Diario: [09-datos-de-prueba.md](../09-datos-de-prueba.md).
>
> Esta fase no escribe Java: es SQL. Todo lo que aprendas aquí es el SQL que Hibernate generará por ti a partir de la fase 11, y el que necesitarás para comprobar a mano que los datos son los que crees.

## 1. De dónde venimos

Al terminar la fase 8 teníamos lo siguiente:

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
```

Cinco tablas y **ninguna fila** (salvo las 4 de control de Flyway). Un esquema sin datos sirve para comprobar que las tablas se crean, pero no para responder a la pregunta "¿funciona la relación entre películas y actores?".

## 2. Estructura y contenido son cosas distintas

| | Esquema | Datos |
|---|---|---|
| Qué es | Tablas, columnas, tipos, claves, restricciones | Filas concretas |
| Cambia | Con migraciones versionadas | Se insertan, se actualizan, se borran |
| ¿Se versiona? | Sí, con Flyway (`V1`–`V4`) | No: es contenido recargable |
| En producción | Es la verdad del sistema | Solo los datos reales de los usuarios |
| En desarrollo | Igual que en producción | Datos ficticios para probar |

La confusión típica es meter los datos ficticios en una migración. Funciona al principio, pero mezcla dos ciclos de vida distintos: el esquema debe ser idéntico en todos los entornos; los datos de prueba **no deben existir** fuera del desarrollo.

## 3. Dónde viven los datos de prueba

Se les llama **datos de prueba**, **datos de ejemplo** o ***seed data*** (semilla). Las opciones habituales son:

| Opción | Ventaja | Inconveniente |
|---|---|---|
| Migración de Flyway en otra carpeta, activada por perfil | Automático y versionado | Comparte el historial de Flyway con el esquema: la base queda en la versión de la semilla y las migraciones posteriores pueden quedar "fuera de orden" (lo probamos y salió un `WARN` real) |
| Script `data.sql` con la inicialización de Spring | Sencillo | Se ejecuta en cada arranque o no se ejecuta nunca; choca con el orden de Flyway; no es idempotente por defecto |
| **Script SQL suelto, cargado a mano o por un script** | Cero acoplamiento; se recarga cuando quieras; sirve igual para un contenedor de pruebas | Hay que acordarse de cargarlo |
| Docker: script en `/docker-entrypoint-initdb.d` | Automático al crear el volumen | Solo se ejecuta con la base **vacía**; si el volumen ya existe, no hace nada |

En este proyecto elegimos la tercera ([D-42](../decisiones-tecnicas.md)): `src/main/resources/db/testdata/seed.sql`, **fuera** de `db/migration`. Flyway no lo ve; los datos se cargan cuando se necesitan.

## 4. `INSERT`

### Forma básica

```sql
INSERT INTO director (first_name, last_name, birth_date)
VALUES ('Christopher', 'Nolan', '1970-07-30');
```

- **La lista de columnas es obligatoria en la práctica**: sin ella hay que dar valor a *todas* las columnas en el orden de la tabla, y un cambio en el esquema rompe la sentencia.
- Las columnas que no aparecen reciben su valor por defecto o `NULL`. `id` tiene `AUTO_INCREMENT`, así que se rellena solo.
- Las cadenas van entre **comillas simples**. Las dobles son para identificadores (tablas y columnas).
- Las fechas van como texto `'1970-07-30'` en formato `AAAA-MM-DD`; MariaDB las convierte a `DATE`.

### Varias filas de una vez

```sql
INSERT INTO actor (first_name, last_name, birth_date) VALUES
    ('Leonardo', 'DiCaprio', '1974-11-11'),
    ('Michael', 'Caine', '1933-03-14'),
    ('Christian', 'Bale', '1974-01-30');
```

Es una sola sentencia y una sola ida al servidor: mucho más rápido que tres `INSERT` separados. En el archivo de semilla se usa para las 24 participaciones.

### `id` automático o escrito a mano

| | `AUTO_INCREMENT` (sin `id`) | `id` explícito |
|---|---|---|
| Quién decide el número | La base de datos | Tú |
| Sirve con datos existentes | Sí | No (choca con los ids que ya hay) |
| Ventaja | No hay que saber los ids | Se lee mejor: `director_id = 1` es Nolan |

En el script de la fase 9 los ids van escritos **porque las tablas están vacías**: el orden de inserción es conocido y el resultado es predecible. Es una comodidad de un script de desarrollo, no una costumbre para la aplicación: cuando Java dé de alta una película, **no** elegirá el `id`.

### `NULL` no es lo mismo que cadena vacía

- `NULL` = "no hay valor". Se comprueba con `IS NULL` / `IS NOT NULL`, nunca con `= NULL` (eso siempre es falso, incluso para `NULL = NULL`).
- `''` = cadena vacía: sí es un valor.
- `biography` admite `NULL` (D-37). Los actores de la semilla no tienen biografía: se insertan sin esa columna y queda `NULL`.

## 5. Integridad referencial: la base como última barrera

La **integridad referencial** es la garantía de que toda clave foránea apunta a una fila que existe. La base de datos la comprueba en cada `INSERT`, `UPDATE` y `DELETE`, venga la orden de donde venga (SQL a mano, Hibernate, un script).

### El orden de inserción lo dictan las claves foráneas

```
director  →  movie  →  movie_actor  ←  actor
```

1. Primero `director` y `actor`: no dependen de nadie.
2. Después `movie`: necesita que su `director_id` exista.
3. Al final `movie_actor`: necesita la película y el actor.

Para borrar, el orden es el inverso: primero las participaciones, después las películas, y al final directores y actores. Es la misma razón por la que en un armario se saca primero lo de arriba.

### Los cuatro errores que vimos

| Código | Cuándo aparece | Ejemplo del proyecto |
|---|---|---|
| `1452` | `INSERT`/`UPDATE` de un **hijo** con un padre que no existe | Película con `director_id = 99` |
| `1451` | `DELETE`/`UPDATE` de un **padre** que tiene hijos y la FK es `RESTRICT` | Borrar a Nolan, que tiene 4 películas |
| `4025` | Un `CHECK` con nombre falla | `duration_minutes = 0`; `release_year = 1800` |
| `1062` | Clave duplicada (PK o `UNIQUE`) | Bale dos veces en *The Prestige* |

Todos llevan **SQLSTATE `23000`**, que significa "violación de restricción de integridad". El número que va después (`1452`, `1062`...) es el código concreto de MariaDB/MySQL; PostgreSQL, por ejemplo, usa otros. Por eso conviene leer el **mensaje**, no solo el número.

### `RESTRICT` y `CASCADE` se ven al borrar

- `movie.director_id` es `RESTRICT`: no se puede borrar un director con películas. Protege la historia: las películas no se quedan sin autor "por accidente".
- `movie_actor.movie_id` es `CASCADE`: al borrar una película, sus participaciones se borran solas. No tienen sentido sin ella.
- `movie_actor.actor_id` es `RESTRICT`: no se puede borrar un actor que participa en películas.

Estas reglas se decidieron en la fase 3 ([D-13](../decisiones-tecnicas.md)) y se comprobaron en `information_schema` en la fase 8.

### `CHECK`: reglas de negocio en la base

```sql
CONSTRAINT chk_movie_duration_positive CHECK (duration_minutes > 0),
CONSTRAINT chk_movie_release_year CHECK (release_year >= 1888)
```

Un `CHECK` no sustituye a la validación en Java: la complementa. Java puede dar un mensaje claro antes de llegar a la base; la base es la que garantiza que **nadie** meta un dato imposible, ni siquiera por SQL.

## 6. `SELECT` y `JOIN`

### Una tabla

```sql
SELECT id, title, release_year
FROM movie
WHERE release_year >= 2010
ORDER BY title;
```

`SELECT` elige columnas, `FROM` la tabla, `WHERE` filtra filas, `ORDER BY` ordena. Sin `ORDER BY`, **el orden no está garantizado** (por eso D-38 fija un orden explícito para cada lista de la API).

### `JOIN`: unir dos tablas por una condición

`movie` guarda `director_id`, un número. Para ver el **nombre** del director hay que ir a `director`:

```sql
SELECT m.title, CONCAT(d.first_name, ' ', d.last_name) AS director
FROM movie m
INNER JOIN director d ON d.id = m.director_id;
```

Cómo se lee:

1. `movie m` y `director d` son **alias**: nombres cortos para no repetir el nombre de la tabla. Sin alias habría que escribir `movie.title` y `director.first_name`.
2. `ON d.id = m.director_id` es la **condición de emparejamiento**: "junta cada fila de `movie` con la fila de `director` cuyo `id` coincide con su `director_id`".
3. `INNER JOIN` (o `JOIN`, que es lo mismo) descarta las filas que **no** encuentran pareja.

### Sin `ON` aparece el producto cartesiano

```sql
SELECT COUNT(*) FROM movie JOIN director;   -- 10 × 6 = 60 filas
```

Cada película se empareja con **cada** director. No es un error de SQL: es lo que se pidió. Casi siempre es un olvido del `ON`. Si alguna vez una consulta devuelve muchísimas más filas de las esperadas, mira ahí primero.

### Tres tablas: película → participaciones → actor

La relación N:M no se puede resolver con dos tablas: hace falta la intermedia.

```sql
SELECT m.title, a.first_name, a.last_name
FROM movie m
INNER JOIN movie_actor ma ON ma.movie_id = m.id
INNER JOIN actor a       ON a.id = ma.actor_id
ORDER BY m.title, a.last_name;
```

Se encadenan los `JOIN`: cada uno añade una tabla y su condición. El resultado es una fila por **participación**: una película con 4 actores aparece 4 veces. Para volver a tener una fila por película se agrupa (siguiente apartado).

### `CONCAT` y `AS`

`CONCAT(d.first_name, ' ', d.last_name)` pega nombre y apellidos con un espacio. `AS director` pone nombre a la columna del resultado. Es el mismo "nombre completo calculado" que la API devolverá como `fullName` (D-36): no se guarda en la base, se calcula.

## 7. Agregados y `GROUP BY`

Un **agregado** resume muchas filas en un valor:

| Función | Qué hace | Ejemplo |
|---|---|---|
| `COUNT(*)` | Cuenta filas | Cuántas películas hay |
| `COUNT(columna)` | Cuenta valores **no nulos** de esa columna | Cuántas películas tiene cada director |
| `SUM(columna)` | Suma | Minutos totales de un director |
| `AVG(columna)` | Media (ignora `NULL`) | Duración media de sus películas |
| `MIN` / `MAX` | Mínimo y máximo | Película más corta y más larga |

`GROUP BY` dice **cómo se agrupan** las filas antes de resumirlas:

```sql
SELECT d.last_name, COUNT(m.id) AS peliculas
FROM director d
INNER JOIN movie m ON m.director_id = d.id
GROUP BY d.id, d.last_name;
```

- Sin `GROUP BY`, `COUNT` contaría **todas** las filas y `d.last_name` no tendría un valor único que mostrar.
- Se agrupa por `d.id` (la clave) y por `d.last_name` (lo que se muestra). Dos directores pueden apellidarse igual.

### `COUNT(*)` no es `COUNT(columna)`

Con un `LEFT JOIN`, la fila del director sin películas existe, pero su parte de `movie` es `NULL`:

| Expresión | Qué cuenta | Sofia Coppola (0 películas) |
|---|---|---|
| `COUNT(*)` | Filas del resultado | **1** |
| `COUNT(m.id)` | Valores no nulos de `m.id` | **0** |

Por eso, para "cuántas películas tiene cada director" se usa `COUNT(m.id)`. Es el error clásico al pasar de `INNER` a `LEFT`.

### Los agregados ignoran los `NULL`

`AVG(m.duration_minutes)` de Sofia es `NULL`, no `0`: no hay ningún valor que promediar. `SUM` también devuelve `NULL`. Si se quiere mostrar 0:

```sql
IFNULL(SUM(m.duration_minutes), 0)
COALESCE(AVG(m.duration_minutes), 0)
```

`IFNULL` es de MySQL/MariaDB; `COALESCE` es estándar y acepta más de dos argumentos. En el proyecto se usa `IFNULL` por ser el más corto; ambos sirven.

### `WHERE` frente a `HAVING`

```sql
SELECT d.last_name, COUNT(m.id) AS peliculas
FROM director d
INNER JOIN movie m ON m.director_id = d.id
WHERE m.release_year >= 2000        -- filtra FILAS antes de agrupar
GROUP BY d.id, d.last_name
HAVING COUNT(m.id) > 1              -- filtra GRUPOS después de agrupar
ORDER BY peliculas DESC;
```

- `WHERE` no puede usar agregados: cuando se evalúa, todavía no hay grupos.
- `HAVING` sí, porque se evalúa después de `GROUP BY`.
- Regla práctica: todo lo que se pueda poner en `WHERE` va en `WHERE` (se descartan filas antes, menos trabajo); `HAVING` solo para condiciones sobre el resultado del agregado.

### `GROUP_CONCAT`: varias filas en una celda

SQL no devuelve listas dentro de una celda... salvo con `GROUP_CONCAT`, que une los valores de un grupo en un texto:

```sql
GROUP_CONCAT(CONCAT(a.first_name, ' ', a.last_name)
             ORDER BY a.last_name, a.first_name
             SEPARATOR ', ')
```

- `ORDER BY` dentro del `GROUP_CONCAT` es imprescindible si el orden importa: sin él, MariaDB no promete ninguno.
- Es cómodo para **leer** y para comprobar datos, pero no es la forma de devolver el reparto en una API: el front necesitará una lista de verdad (un `JOIN` y un `List<Actor>`, fase 16). Además, el resultado se trunca a `group_concat_max_len`.
- En este proyecto se usa solo en consultas de comprobación.

## 8. `LEFT JOIN`: conservar las filas sin pareja

```sql
SELECT CONCAT(d.first_name, ' ', d.last_name) AS director, COUNT(m.id) AS peliculas
FROM director d
LEFT JOIN movie m ON m.director_id = d.id
GROUP BY d.id, director
ORDER BY peliculas DESC;
```

```
| Christopher Nolan   |         4 |
| ...
| Sofia Coppola       |         0 |
```

- `LEFT JOIN` conserva **todas** las filas de la tabla de la izquierda (`director`) y rellena con `NULL` las columnas de la derecha cuando no hay pareja.
- `RIGHT JOIN` es lo mismo al revés; casi siempre se reescribe como `LEFT` cambiando el orden de las tablas, porque se lee mejor.
- `FULL OUTER JOIN` (todas las filas de ambos lados) **no existe en MySQL/MariaDB**; se simula con `UNION` de un `LEFT` y un `RIGHT`.

### Para qué sirve en este proyecto

| Pregunta | JOIN |
|---|---|
| Ficha de una película con su director | `INNER` (toda película tiene director, es `NOT NULL`) |
| Reparto de una película | `LEFT` desde `movie`: una película sin actores debe aparecer con lista vacía (D-36, D-39) |
| Películas de un director | `INNER` para la lista; `LEFT` si se quiere incluir "0 películas" |
| Directores sin películas | `LEFT` + `WHERE m.id IS NULL` |

### Buscar huérfanos

La misma idea sirve para auditar los datos:

```sql
SELECT COUNT(*)
FROM movie m
LEFT JOIN director d ON d.id = m.director_id
WHERE d.id IS NULL;          -- 0: no hay películas sin director
```

Con las FK activas esto siempre dará 0. Se usa al importar datos de otro sistema, donde las FK pueden estar desactivadas o los datos venir incompletos.

## 9. El orden no está garantizado (D-38)

Una consulta sin `ORDER BY` puede devolver las filas en cualquier orden y cambiarlo cuando le apetezca al motor (por ejemplo, al añadir un índice). No es un fallo: el estándar SQL no promete orden.

Por eso [D-38](../decisiones-tecnicas.md) fija el orden **en la consulta**:

| Lista | `ORDER BY` |
|---|---|
| `GET /movies` | `title` |
| `GET /directors/{id}/movies` | `release_year, title` |
| Actores de una película | apellido, nombre |

Ordenar en SQL (y no en Java) aprovecha los índices y evita traer filas que luego se descartan.

## 10. Qué tiene que ver esto con Java

Aunque esta fase no tenga código Java, todo lo anterior vuelve en las siguientes:

| Fase | Qué relación tiene |
|---|---|
| 11 | JPA mapea `Movie.director` a la FK `director_id`; `Movie.actors` a la tabla `movie_actor` |
| 12 | El repositorio pedirá "películas de un director" y Hibernate escribirá el `JOIN` |
| 14 | Hibernate puede lanzar **muchas** consultas para cargar los actores de cada película: el problema **N+1**. Se arregla con `JOIN FETCH` o `@EntityGraph`, que es exactamente un `JOIN` |
| 15 | El DTO `MovieResponse` con `director.fullName` y `actors[]` sale de estas consultas |
| 16 | `GET /movies/{id}` necesitará `LEFT JOIN` para devolver una película sin reparto con `actors: []` |
| 18 | Los errores `1452`, `1451`, `4025` y `1062` son los que la API no debería provocar; si llegan, es un fallo del dominio |
| 19 | Los tests de integración cargarán este mismo `seed.sql` en un MariaDB desechable |

## Lo que tienes que recordar

1. **Esquema y datos son cosas distintas**: el esquema se versiona (Flyway); los datos de prueba se recargan (script).
2. **El orden de `INSERT` lo dictan las claves foráneas**: primero los padres, después los hijos. Para borrar, al revés.
3. **La base es la última barrera**: FK (`1452`/`1451`), `CHECK` (`4025`) y PK (`1062`) protegen los datos aunque el programa tenga un fallo.
4. **`INNER JOIN` descarta lo que no tiene pareja; `LEFT JOIN` lo conserva** con `NULL`.
5. **`COUNT(*)` cuenta filas; `COUNT(columna)` cuenta valores no nulos**. Con `LEFT JOIN`, casi siempre quieres lo segundo.
6. **`WHERE` filtra filas; `HAVING` filtra grupos.** Los agregados ignoran `NULL`.
7. **Sin `ORDER BY` no hay orden garantizado.** La API lo necesita explícito (D-38).
8. **`GROUP_CONCAT` sirve para leer y comprobar**, no para el contrato de la API.

## Preguntas de repaso

**1. ¿Por qué los datos de prueba no van en una migración de Flyway?**

<details><summary>Respuesta</summary>

Porque el historial de Flyway es único: una migración de datos deja la base en una versión que mezcla esquema y contenido. En el proyecto se comprobó que al ejecutar sin el perfil de desarrollo aparecía `Schema movies has a version (100) that is newer than the latest available migration (4)`, y la siguiente migración de esquema (`V5`) habría quedado fuera de orden. Además, el esquema debe ser igual en todos los entornos, pero los datos ficticios solo deben existir en desarrollo. Decisión: script aparte (D-42).
</details>

**2. ¿Qué diferencia hay entre `ERROR 1452` y `ERROR 1451`?**

<details><summary>Respuesta</summary>

`1452` es un fallo al insertar o actualizar un **hijo**: su clave foránea apunta a un padre que no existe (`Cannot add or update a child row`). `1451` es un fallo al borrar o actualizar un **padre** que todavía tiene hijos y la FK es `RESTRICT` (`Cannot delete or update a parent row`). Los dos son SQLSTATE `23000`.
</details>

**3. Con `LEFT JOIN`, ¿por qué `COUNT(*)` da 1 y `COUNT(m.id)` da 0 para un director sin películas?**

<details><summary>Respuesta</summary>

Porque el `LEFT JOIN` conserva la fila del director y rellena las columnas de `movie` con `NULL`. `COUNT(*)` cuenta esa fila (existe); `COUNT(m.id)` no cuenta el `NULL`. Para contar hijos se usa `COUNT(columna_del_hijo)`.
</details>

**4. ¿Qué pasa si se ejecuta un `JOIN` sin `ON`?**

<details><summary>Respuesta</summary>

Se obtiene el producto cartesiano: cada fila de una tabla se empareja con todas las de la otra. Con 10 películas y 6 directores salen 60 filas. No es un error de sintaxis, pero casi siempre es un olvido.
</details>

**5. ¿Por qué `WHERE COUNT(*) > 1` es un error y `HAVING COUNT(*) > 1` no?**

<details><summary>Respuesta</summary>

Porque `WHERE` se evalúa **antes** de agrupar, cuando todavía no existen los grupos ni sus agregados. `HAVING` se evalúa después de `GROUP BY`, así que puede usar `COUNT`, `SUM`, etc.
</details>

**6. ¿Para qué sirve `GROUP_CONCAT` y por qué no se usa para devolver el reparto de una película en la API?**

<details><summary>Respuesta</summary>

Une en un solo texto los valores de un grupo (por ejemplo, los nombres de los actores de una película). Sirve para consultas de comprobación. En la API no se usa porque el contrato de D-36 devuelve una **lista** de objetos `{id, fullName}`, no una cadena; además `GROUP_CONCAT` se trunca si el texto crece.
</details>

**7. ¿Por qué las películas de la semilla llevan `id` escrito a mano y la aplicación no debería hacerlo?**

<details><summary>Respuesta</summary>

En la semilla las tablas están vacías y el orden de inserción es conocido, así que los ids son predecibles y el archivo se lee mejor (`director_id = 1` es Nolan). En la aplicación no: la base puede tener datos, así que se omite `id` y `AUTO_INCREMENT` asigna el siguiente. Escribir ids a mano en producción provoca colisiones (`ERROR 1062`).
</details>

**8. ¿Qué consulta devuelve los directores que no han dirigido ninguna película?**

<details><summary>Respuesta</summary>

```sql
SELECT d.first_name, d.last_name
FROM director d
LEFT JOIN movie m ON m.director_id = d.id
WHERE m.id IS NULL;
```

El `LEFT JOIN` conserva a todos los directores; el `WHERE ... IS NULL` deja solo los que no encontraron película. En nuestros datos devuelve a Sofia Coppola.
</details>
