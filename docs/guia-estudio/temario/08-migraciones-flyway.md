# Tema 08 — Crear las tablas con Flyway, explicado desde cero

> Fase del plan: 8. Lo que hicimos, con los comandos reales: [08-flyway-migraciones.md](../08-flyway-migraciones.md).

**En una frase:** Flyway crea y modifica las tablas de la base de datos a partir de archivos SQL numerados que guardamos en Git, y apunta cuáles ha ejecutado ya.

**Para qué sirve:** para que la base de datos de cualquier ordenador tenga **exactamente** las mismas tablas, sin hacer nada a mano.

---

## 1. El problema

El código Java está en Git: quien clona el repositorio tiene las mismas clases que tú. Pero **las tablas de la base de datos no están en Git**: viven dentro de MariaDB.

Sin una herramienta:
- Cada persona tendría que crear las tablas a mano, copiando SQL. Con un despiste, su base de datos sería distinta a la tuya.
- Si mañana añades una columna, ¿cómo se entera el resto? ¿Cómo sabes si en tu ordenador ya la añadiste?

## 2. La idea: un libro de obras numerado

Piensa en las **obras de una casa**. Cada obra se apunta en un libro, en orden:

```
Obra 1: construir la habitación "director"
Obra 2: construir la habitación "actor"
Obra 3: construir la habitación "movie" (con una puerta a "director")
Obra 4: construir el pasillo "movie_actor" (une "movie" y "actor")
```

Si llega un obrero nuevo, lee el libro y sabe exactamente qué hay que hacer y en qué orden.

En Flyway, cada obra es una **migración**: un archivo `.sql` con un cambio de la base de datos y un número de versión.

```
src/main/resources/db/migration/
├── V1__create_director_table.sql
├── V2__create_actor_table.sql
├── V3__create_movie_table.sql
└── V4__create_movie_actor_table.sql
```

### El nombre del archivo importa

```
V1__create_director_table.sql
│ │ │                      │
│ │ │                      └─ .sql
│ │ └─ descripción (los _ se leen como espacios)
│ └─ dos guiones bajos: separan el número de la descripción
└─ V + número de versión: marca el orden
```

⚠️ Son **dos** guiones bajos (`__`). Con uno solo, Flyway ignora el archivo y no avisa.

## 3. Cómo sabe Flyway qué le falta: la tabla `flyway_schema_history`

Flyway lleva su propio **libro de registro** dentro de nuestra base de datos: una tabla llamada `flyway_schema_history`. Ahí apunta cada migración que ejecuta.

Cada vez que arranca la aplicación, Flyway hace esto:

1. Si no existe su tabla de registro, la crea.
2. Mira los archivos `V...sql` de la carpeta.
3. Los compara con lo apuntado: los que **no** están apuntados son **pendientes**.
4. Ejecuta los pendientes, **en orden de número**, y los apunta.

Por eso:
- En una base de datos vacía (como la nuestra ahora), ejecuta las 4.
- Si las 4 ya están apuntadas, no hace nada.
- Si alguien añade la `V5`, solo ejecuta la `V5`.

## 4. Regla de oro: una migración ejecutada no se toca

Flyway guarda una **huella** de cada archivo que ejecuta. Se llama **checksum**: un número calculado a partir del contenido del archivo. Si cambias una sola letra, el número cambia.

En cada arranque, Flyway vuelve a calcular la huella de cada archivo y la compara con la apuntada. Si alguien ha editado una migración ya ejecutada, **no coinciden y la aplicación no arranca**.

¿Por qué es tan estricto? Porque los ordenadores que ya ejecutaron la versión antigua **no se enterarían** del cambio, y cada base de datos acabaría siendo distinta sin que nadie lo notara.

**¿Y si me he equivocado?** No se edita la migración vieja: se añade una nueva que corrige (`V5__fix_...sql`). Como en el libro de obras: no se tachan obras hechas, se apunta una obra nueva.

(Excepción: mientras una migración solo esté en tu ordenador y no la hayas subido a GitHub, puedes rehacerla, borrando tu base de datos de desarrollo y empezando de cero.)

## 5. El orden importa

La tabla `movie` tiene una columna `director_id` que apunta a la tabla `director` (una **clave foránea**, fase 3). No puedes apuntar a algo que no existe, así que **`director` tiene que crearse antes que `movie`**. Igual con `movie_actor`, que necesita `movie` y `actor`. Por eso los números: `director` (1), `actor` (2), `movie` (3), `movie_actor` (4).

## 6. Quién hace qué al arrancar la aplicación

```
1. Conexión a MariaDB (HikariCP)          ← fase 7
2. Flyway ejecuta las migraciones pendientes  ← fase 8: CREA y CAMBIA las tablas
3. Hibernate comprueba que todo encaja        ← fase 11: solo MIRA (ddl-auto: validate)
```

Flyway es el **arquitecto** que hace las obras; Hibernate es el **inspector** que solo comprueba.

## 7. Cómo se activa Flyway en Spring Boot

No hay que escribir ni una línea de Java. Solo se añaden dos dependencias al `pom.xml`:

| Dependencia | Qué aporta |
|---|---|
| `spring-boot-starter-flyway` | La librería Flyway **y** su autoconfiguración de Spring Boot |
| `flyway-mysql` | El módulo con el dialecto de MySQL/MariaDB |

Al arrancar, Spring Boot ve que hay Flyway en el *classpath* y lo pone en marcha antes de crear la capa de JPA (por eso, en el log, Flyway sale antes que Hibernate). Busca los archivos `.sql` en **`src/main/resources/db/migration`** (en el `classpath`: `classpath:db/migration`). Si esa carpeta no existe, avisa con `No migrations found` y sigue.

**La versión la elige Spring Boot**, no nosotros: el `pom.xml` de *parent* fija las versiones de todo lo que trae, y son las que el equipo de Spring ha probado juntas. En este proyecto se resolvió la **12.4.0**. Si algún día hace falta otra, se cambia con una propiedad (`<flyway.version>`), pero sin un motivo concreto es arriesgar la integración.

**Aviso de versión de la base de datos.** Al conectarse, Flyway comprueba contra qué versiones de MariaDB ha sido probado. Con la 12.3 del proyecto salió:

```
WARN : Using MariaDB 12.3 which is newer than the version Flyway has been verified with.
       The latest verified version of MariaDB is 11.7.
```

Es un **aviso**, no un error: Flyway sigue, crea su tabla y ejecuta las migraciones. Solo significa que el fabricante no ha certificado esa combinación.

---

## Lo que tienes que recordar

- Una **migración** es un archivo SQL numerado con un cambio de la base de datos: `V1__descripcion.sql`, con **dos** guiones bajos.
- Flyway apunta en **`flyway_schema_history`** lo que ya ha ejecutado y, al arrancar, solo ejecuta lo pendiente.
- Una migración ya ejecutada **no se edita**: Flyway lo detecta por su huella (checksum). Los errores se corrigen con una migración nueva.
- El orden de los números lo marcan las claves foráneas.
- En Spring Boot basta con dos dependencias (`spring-boot-starter-flyway` y `flyway-mysql`): Flyway se ejecuta solo al arrancar y lee `src/main/resources/db/migration`.
- La versión de Flyway la decide Spring Boot, no nosotros.
- El aviso "Using MariaDB 12.3 ... verified with 11.7" es solo eso, un aviso.

## Preguntas de repaso

### La idea

**1. ¿Qué problema resuelve Flyway?**
Que las tablas no están en Git. Con Flyway, el SQL que crea y cambia las tablas va en archivos dentro del proyecto, y cualquier ordenador acaba con las mismas tablas.

**2. ¿Qué es una migración?**
Un archivo SQL con un cambio de la base de datos y un número de versión, por ejemplo `V1__create_director_table.sql`.

**3. ¿En qué carpeta van las migraciones?**
En `src/main/resources/db/migration/`.

**4. ¿Qué significa cada parte de `V3__create_movie_table.sql`?**
`V` = migración con versión; `3` = el orden; `__` (dos guiones bajos) = separador; `create_movie_table` = descripción; `.sql` = extensión.

**5. Llamo a mi archivo `V1_create_director_table.sql`, con un solo guion bajo. ¿Qué pasa?**
Flyway no lo reconoce como migración y no lo ejecuta.

### `flyway_schema_history`

**6. ¿Qué es `flyway_schema_history` y dónde está?**
El libro de registro de Flyway: una tabla que crea dentro de nuestra base de datos `movies`, donde apunta cada migración ejecutada.

**7. ¿Qué es una migración pendiente?**
Una que está en la carpeta pero todavía no aparece apuntada en `flyway_schema_history`.

**8. ¿En qué orden ejecuta Flyway las migraciones pendientes?**
Por número de versión, de menor a mayor.

**9. Arranco la aplicación dos veces seguidas. ¿Qué hace Flyway la segunda vez?**
Nada: ve en `flyway_schema_history` que todas las migraciones ya están apuntadas.

**10. Un compañero clona el proyecto con su base de datos vacía. ¿Qué pasa al arrancar?**
Flyway crea su tabla de registro y ejecuta las 4 migraciones en orden. Acaba con las mismas tablas que tú.

**11. Tengo las 4 migraciones aplicadas y alguien sube la `V5`. ¿Qué se ejecuta?**
Solo la `V5`.

### El checksum

**12. ¿Qué es el checksum?**
Una "huella" numérica calculada a partir del contenido del archivo. Si cambia una sola letra, cambia el número.

**13. ¿Para qué guarda Flyway el checksum de cada migración?**
Para detectar si alguien ha editado una migración que ya se ejecutó.

**14. ¿Qué pasa si edito una migración ya ejecutada?**
La huella no coincide con la apuntada y la aplicación no arranca.

**15. ¿Por qué Flyway es tan estricto con eso?**
Porque los ordenadores que ya ejecutaron la versión antigua no se enterarían del cambio, y cada base de datos acabaría siendo distinta sin que nadie lo notara.

**16. Me equivoqué en una columna de la `V1` y ya está en GitHub. ¿Qué hago?**
No tocar la `V1`. Crear una `V5__fix_...sql` que corrija el error (con `ALTER TABLE`).

**17. ¿Y si la migración equivocada solo está en mi ordenador y no la he subido?**
Puedes rehacerla, pero tienes que borrar tu base de datos de desarrollo y empezar de cero, para que Flyway la vuelva a ejecutar.

### El orden y el papel de cada uno

**18. ¿Por qué `movie` es la versión 3 y no la 1?**
Porque apunta a `director` con una clave foránea, y `director` tiene que existir antes.

**19. ¿Por qué `movie_actor` es la última?**
Porque apunta a `movie` y a `actor`, y las dos tienen que existir antes.

**20. Al arrancar la aplicación, ¿qué va antes: Flyway o Hibernate? ¿Por qué?**
Flyway. Primero se crean o actualizan las tablas, y después Hibernate comprueba que encajan con las clases Java (`validate`).

**21. ¿Qué diferencia hay entre el papel de Flyway y el de Hibernate con las tablas?**
Flyway las crea y las cambia (el arquitecto). Hibernate solo comprueba que todo encaja (el inspector).

### Activarlo en Spring Boot

**22. ¿Qué dos dependencias se añaden al `pom.xml` y qué aporta cada una?**
`spring-boot-starter-flyway`, que trae Flyway y su autoconfiguración, y `flyway-mysql`, con el dialecto de MySQL/MariaDB.

**23. ¿Por qué no se escribe la versión de esas dependencias?**
Porque la fija el *parent* de Spring Boot (`spring-boot-starter-parent`), con las versiones que el equipo de Spring ha probado juntas. Escribir otra a mano puede romper la integración.

**24. ¿Dónde busca Flyway los archivos `.sql` por defecto?**
En `classpath:db/migration`, que en el proyecto es `src/main/resources/db/migration`. Si la carpeta no existe, avisa con `No migrations found`.

**25. Salió `Using MariaDB 12.3 which is newer than the version Flyway has been verified with`. ¿Hay que hacer algo?**
No por ahora: es un `WARN`, Flyway funciona igual. Solo dice que la combinación no está certificada por el fabricante; queda anotado en D-04 por si algún día da problemas.
