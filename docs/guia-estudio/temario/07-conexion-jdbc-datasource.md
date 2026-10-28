# Tema 07 — Cómo se conecta Java con MariaDB, explicado desde cero

> Fase del plan: 7. Lo que hicimos, con los comandos reales: [07-conexion-java-mariadb.md](../07-conexion-java-mariadb.md).

**En una frase:** en la fase 7 conseguimos que la aplicación Java hable con la base de datos MariaDB que corre en Docker, sin escribir la contraseña en el código.

**Para qué sirve:** sin esto, la aplicación no puede guardar ni leer películas. Es el "cable" entre Java y la base de datos.

---

## 1. El problema

Java y MariaDB son **dos programas distintos**:
- MariaDB está dentro de un contenedor Docker, esperando conexiones en el **puerto 3307** de tu ordenador.
- MariaDB solo entiende su propio "idioma" de red. Java, por sí solo, no lo habla.

Necesitamos una cadena de piezas que lleve las peticiones de Java hasta MariaDB.

## 2. La cadena completa

Piensa en una llamada a otro país:

```
 Nuestro código Java          "Quiero la película 3"
        │
        ▼
 Hibernate (JPA)              Traductor: convierte objetos Java en SQL
        │                     (SELECT * FROM movie WHERE id = 3)
        ▼
 DataSource (HikariCP)        Centralita: presta una línea (una conexión)
        │
        ▼
 Driver de MariaDB            Habla el "idioma" de MariaDB por la red
        │
        ▼
 MariaDB en Docker            Ejecuta el SQL y devuelve el resultado
 (localhost:3307)
```

Veamos cada pieza, de abajo arriba.

### El driver: el que habla el idioma de MariaDB

Java tiene un estándar para hablar con cualquier base de datos, llamado **JDBC**. JDBC es como un **enchufe estándar**: define la forma, pero no sabe nada de ninguna base de datos concreta.

Cada base de datos aporta su **driver**: el adaptador que encaja en ese enchufe y sabe hablar con ella. El de MariaDB es la librería `mariadb-java-client`.

¿Cómo sabe Java qué driver usar? Por la **URL de conexión**, la "dirección" de la base de datos:

```
jdbc:mariadb://localhost:3307/movies
     │         │         │    │
     │         │         │    └─ nombre de la base de datos
     │         │         └─ puerto (el que publica Docker)
     │         └─ ordenador (localhost = el mío)
     └─ "usa el driver de MariaDB"
```

### El DataSource y el pool: la centralita

Abrir una conexión con la base de datos es **lento**: hay que conectarse por la red, enviar el usuario y la contraseña, esperar la respuesta...

Por eso no se abre una conexión nueva para cada consulta. Se usa un **pool de conexiones**, que es como una **parada de taxis**: hay varias conexiones ya abiertas esperando. Cuando alguien necesita una, la coge; cuando termina, la **devuelve** a la parada para el siguiente.

- **`DataSource`** es el nombre estándar en Java de "algo que te da conexiones".
- **HikariCP** es el *pool* que usa Spring Boot. En los logs aparece como `HikariPool-1`.

### Hibernate y JPA: el traductor

Escribir SQL a mano para cada operación es pesado. **Hibernate** traduce automáticamente entre **objetos Java** y **filas de tablas**. **JPA** es el estándar de Java que dice cómo se hace esa traducción, e Hibernate es el programa que lo aplica. Lo usaremos de verdad en la fase 11; en esta fase solo lo hemos configurado.

### ¿Quién monta toda la cadena?

Nosotros no. Al añadir la dependencia `spring-boot-starter-data-jpa`, **Spring Boot lo monta solo** (autoconfiguración, tema 06). Solo necesita que le digamos tres datos: **la URL, el usuario y la contraseña**.

---

## 3. Dónde se escriben esos datos: `application.yaml`

`src/main/resources/application.yaml` es el **archivo de ajustes** de la aplicación. Spring Boot lo lee al arrancar.

### Cómo se lee un YAML

YAML organiza los ajustes por niveles usando la **sangría** (los espacios del principio de la línea):

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3307/movies
```

Esto significa: dentro de `spring`, dentro de `datasource`, el ajuste `url` vale `jdbc:mariadb://...`. Es lo mismo que escribir `spring.datasource.url=jdbc:mariadb://...` en un archivo `.properties` (el formato que traía el proyecto al principio).

⚠️ La sangría **son dos espacios**, nunca tabuladores. Un espacio de más o de menos cambia el significado.

### El problema de la contraseña

`application.yaml` **se sube a GitHub**, así que **no puede contener la contraseña**. Ya la teníamos guardada en `.env` (fase 4), que no se sube. Pero hay un detalle: Docker sabe leer `.env` por su cuenta y Spring Boot no.

La solución tiene dos partes.

**1. Decirle a Spring que lea también `.env`:**

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

- `file:.env` → lee el archivo `.env` de la carpeta del proyecto.
- `optional:` → si no existe, no falla al leerlo.
- `[.properties]` → "léelo como líneas `NOMBRE=valor`".

**2. Usar huecos en vez de valores:**

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:${MARIADB_PORT:3307}/${MARIADB_DATABASE:movies}
    username: ${MARIADB_USER}
    password: ${MARIADB_PASSWORD}
```

`${MARIADB_USER}` es un **hueco** que Spring rellena al arrancar con el valor de esa variable. Así en el archivo nunca aparece la contraseña real.

| Hueco | Significado |
|---|---|
| `${MARIADB_USER}` | Obligatorio: si no encuentra la variable, la aplicación no arranca |
| `${MARIADB_PORT:3307}` | Con valor por defecto: si no la encuentra, usa `3307` |

Los nombres (`MARIADB_USER`...) son **los mismos que usa `compose.yaml`**. Así un único `.env` sirve para Docker y para Spring.

**¿De dónde saca Spring el valor?** Primero mira si hay una **variable de entorno** del sistema con ese nombre; si no la hay, la busca en `.env`. Lo comprobamos poniendo una contraseña falsa como variable de entorno: ganó a la de `.env` y MariaDB contestó `Access denied`.

---

## 4. Lección importante: los secretos también se escapan por los logs

El **log** es el registro de mensajes que escribe la aplicación mientras funciona (lo que ves en la consola).

Al conectar por primera vez, Hibernate escribió en el log la URL de conexión... **con el usuario y la contraseña en claro**. No estaban en ningún archivo de Git, pero salían por pantalla. Si copias ese log en un foro o en un chat, la contraseña se va con él.

La solución: decirle a Spring que, de esa parte concreta de Hibernate, solo escriba avisos y errores, no la información normal.

```yaml
logging:
  level:
    org.hibernate.orm.connections.pooling: WARN
```

Los **niveles de log**, de más detalle a menos: `TRACE` → `DEBUG` → `INFO` → `WARN` → `ERROR`. Con `WARN` solo salen avisos (`WARN`) y errores (`ERROR`); los mensajes `INFO`, como el de la contraseña, dejan de salir.

**Moraleja:** no basta con no escribir secretos en el código; también hay que mirar qué escribe el programa.

---

## 5. Hibernate no toca las tablas: `ddl-auto: validate`

Hibernate es capaz de **crear o cambiar tablas** él solo, a partir de las clases Java. Parece cómodo, pero es peligroso: hace cambios en la base de datos sin que nadie los revise y sin dejar rastro.

Piensa en una casa:
- **Flyway** (fase 8) es el **arquitecto**: cada obra va en un plano numerado, revisado y guardado en Git.
- **Hibernate** será solo el **inspector**: mira que todo esté como dicen los planos, pero no toca nada.

Eso se configura así:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

| Valor de `ddl-auto` | Qué hace Hibernate al arrancar |
|---|---|
| `create` | Borra las tablas y las crea de nuevo: **pierdes los datos** |
| `update` | Cambia las tablas a su manera, sin revisión |
| `none` | Nada |
| `validate` ✅ | Nada, pero **comprueba** que las clases Java y las tablas encajan. Si no encajan, la aplicación no arranca y te dice por qué |

Si no se configura, Spring Boot usa `none` sin avisar (con MariaDB). Ahora mismo `validate` no tiene nada que comprobar, porque todavía no hay clases que representen tablas; empezará a trabajar en la fase 11.

---

## 6. Los tests de la fase 7, línea a línea

Creamos `DatabaseConnectionTests` con dos tests. Los dos usan `@SpringBootTest`, que arranca la aplicación completa, conexión incluida.

`@Autowired` significa "Spring, dame este objeto de tu almacén". Así el test puede usar las piezas que montó Spring.

### Test 1: ¿conecta con la base de datos correcta y con el usuario correcto?

```java
@Autowired
private DataSource dataSource;              // pido a Spring la "centralita" de conexiones

@Test
void connectsToMoviesDatabaseAsApplicationUser() throws SQLException {
    // Pido una conexión. El try (...) la devuelve a la parada de taxis al terminar,
    // aunque falle una comprobación.
    try (Connection connection = dataSource.getConnection()) {
        DatabaseMetaData metaData = connection.getMetaData();   // datos sobre la conexión

        assertThat(metaData.getDatabaseProductName()).isEqualTo("MariaDB");  // ¿es MariaDB?
        assertThat(connection.getCatalog()).isEqualTo("movies");             // ¿la base de datos movies?
        assertThat(metaData.getUserName()).isEqualTo("movies_app");          // ¿el usuario de la aplicación, no root?
    }
}
```

Comprobamos el usuario porque la aplicación **nunca debe conectarse como `root`** (el administrador). Si alguien encontrara un fallo en la aplicación, con `movies_app` solo podría tocar la base de datos `movies`. A esto se le llama **mínimo privilegio**: cada uno, solo los permisos que necesita.

### Test 2: ¿Hibernate tiene `validate`?

```java
@Autowired
private EntityManagerFactory entityManagerFactory;   // la pieza principal de Hibernate

@Test
void hibernateOnlyValidatesTheSchema() {
    // Pregunto a Hibernate qué configuración ha recibido de verdad
    assertThat(entityManagerFactory.getProperties())
            .containsEntry("hibernate.hbm2ddl.auto", "validate");
}
```

`hibernate.hbm2ddl.auto` es el nombre que Hibernate da internamente a nuestro `ddl-auto`. Comprobamos lo que **ha recibido Hibernate**, no lo que hay escrito en el archivo: así sabemos que el ajuste ha llegado a su destino.

### Un test tiene que poder fallar

Un test que nunca se ha visto en rojo **no demuestra nada**: a lo mejor no comprueba lo que crees. Por eso:
- El test 2 lo escribimos **antes** de configurar `validate` y lo vimos en rojo (TDD).
- El test 1 lo hicimos fallar a propósito con una contraseña incorrecta.

### La segunda clase de test va mucho más rápida

`MoviesApplicationTests` tardó 0,027 segundos, y `DatabaseConnectionTests` unos 5. Arrancar la aplicación es lo lento, y Spring la **reutiliza** entre clases de test que tienen la misma configuración: solo la arranca una vez.

### Consecuencia: para pasar los tests, MariaDB tiene que estar encendido

Desde esta fase, antes de `.\mvnw.cmd test` hay que arrancar la base de datos con `docker compose up -d`. Si no, todos los tests fallan con `Connection refused` ("nadie contesta en ese puerto"). En la fase 19 lo mejoraremos con Testcontainers, que arranca una base de datos solo para los tests.

---

## 7. Cómo leer los errores que vimos

Cuando algo falla, Java escribe una cadena de errores enlazados con `Caused by:` ("causado por"). Van del síntoma a la causa, así que **la causa real es la última línea `Caused by:`**.

| Mensaje | Qué quiere decir en cristiano | Solución |
|---|---|---|
| `Failed to configure a DataSource: 'url' attribute is not specified` | "Me has pedido conectar con una base de datos, pero no me has dicho cuál" | Escribir la URL en `application.yaml` |
| `Connection refused` en `localhost:3307` | "He llamado y nadie contesta": MariaDB no está encendido | Abrir Docker Desktop y `docker compose up -d` |
| `Unable to determine Dialect` | Hibernate no ha podido preguntar a la base de datos qué tipo es (porque no hay conexión) | La misma: arrancar MariaDB |
| `Access denied for user 'movies_app'` | "Contesta alguien, pero no te deja entrar": usuario o contraseña incorrectos | Revisar `.env` |

El **dialecto** es la "variante de SQL" de cada base de datos (cada una tiene pequeñas diferencias). Hibernate lo averigua solo al conectar; aquí eligió `MariaDBDialect`.

---

## Lo que tienes que recordar

- Java habla con MariaDB a través de una cadena: **Hibernate** (traduce a SQL) → **pool HikariCP** (presta conexiones) → **driver** (habla con MariaDB) → **MariaDB**.
- Spring Boot monta esa cadena solo; necesita **URL, usuario y contraseña**, que van en `application.yaml`.
- La contraseña **no** se escribe en `application.yaml`: se pone un hueco `${MARIADB_PASSWORD}` y el valor sale de `.env`.
- Los secretos también pueden salir **por los logs**.
- `ddl-auto: validate`: Hibernate **no toca** las tablas; las creará Flyway.
- Para ejecutar los tests, **MariaDB tiene que estar encendido**.

---

## Preguntas de repaso

### La conexión

**1. ¿Qué es JDBC?**
El estándar de Java para hablar con bases de datos. Es como un enchufe estándar: define la forma, pero no sabe hablar con ninguna base de datos concreta.

**2. ¿Qué es el driver y por qué hace falta?**
La librería que sabe hablar con una base de datos concreta (para MariaDB, `mariadb-java-client`). Sin él, JDBC no sabría cómo comunicarse con MariaDB.

**3. ¿Por qué el driver tiene el scope `runtime`?**
Porque nuestro código nunca lo nombra directamente: solo usa el estándar JDBC. El driver solo hace falta cuando la aplicación se ejecuta.

**4. En `jdbc:mariadb://localhost:3307/movies`, ¿qué significa cada parte?**
`jdbc:mariadb:` = usa el driver de MariaDB; `localhost` = en mi ordenador; `3307` = el puerto; `movies` = la base de datos.

**5. ¿Por qué el puerto es 3307 y no el 3306 habitual?**
Porque en este ordenador el 3306 lo usa un MariaDB instalado en Windows. Docker publica nuestro contenedor en el 3307 (fase 4).

**6. ¿Qué es un pool de conexiones y para qué sirve?**
Un grupo de conexiones ya abiertas que se prestan y se devuelven, como una parada de taxis. Sirve porque abrir una conexión nueva cada vez es lento.

**7. ¿Qué es un `DataSource`? ¿Quién lo crea en nuestro proyecto?**
"Algo que te da conexiones". Lo crea Spring Boot solo (con HikariCP) cuando tiene la URL, el usuario y la contraseña.

**8. ¿Qué diferencia hay entre JPA e Hibernate?**
JPA es el estándar (las reglas de cómo traducir objetos a tablas). Hibernate es el programa que cumple esas reglas y hace el trabajo.

**9. En el paso 7.1 el driver ya estaba en el proyecto. ¿Por qué Spring decía "Failed to determine a suitable driver class"?**
Porque Spring elige el driver mirando la URL, y la URL todavía no estaba escrita.

### La configuración

**10. ¿Qué es `application.yaml` y cuándo se lee?**
El archivo de ajustes de la aplicación. Spring Boot lo lee al arrancar.

**11. Escribe en formato `.properties` este YAML: `spring:` → `jpa:` → `hibernate:` → `ddl-auto: validate`.**
`spring.jpa.hibernate.ddl-auto=validate`.

**12. ¿Qué pasa si en el YAML uso tabuladores o me equivoco con la sangría?**
YAML no admite tabuladores y la sangría decide qué ajuste está dentro de cuál. Si te equivocas, el ajuste queda en otro sitio (y no hace nada) o la aplicación no arranca por error de formato.

**13. ¿Por qué no escribimos la contraseña en `application.yaml`?**
Porque ese archivo se sube a GitHub y cualquiera podría verla.

**14. ¿Qué hace `optional:file:.env[.properties]`?**
Le dice a Spring que lea también el archivo `.env` (`file:.env`), sin fallar si no existe (`optional:`), interpretando cada línea como `NOMBRE=valor` (`[.properties]`).

**15. ¿Qué diferencia hay entre `${MARIADB_USER}` y `${MARIADB_PORT:3307}`?**
El primero es obligatorio: si no existe la variable, la aplicación no arranca. El segundo tiene un valor por defecto: si no existe, usa `3307`.

**16. ¿Por qué el usuario y la contraseña no tienen valor por defecto?**
Porque son secretos: un valor por defecto estaría escrito en un archivo de Git. Es mejor que la aplicación no arranque a que use una contraseña publicada.

**17. Si `MARIADB_PASSWORD` está en `.env` y también como variable de entorno, ¿cuál se usa? ¿Cómo lo comprobamos?**
La variable de entorno. Lo comprobamos ejecutando los tests con `MARIADB_PASSWORD=wrong-password`: MariaDB contestó `Access denied`, aunque `.env` tenía la contraseña buena.

**18. ¿Por qué Docker y Spring pueden usar el mismo `.env`?**
Porque `application.yaml` usa los mismos nombres de variable que `compose.yaml` (`MARIADB_USER`, `MARIADB_PASSWORD`...).

### Los logs

**19. Si la contraseña no estaba en ningún archivo de Git, ¿cómo llegó a verse?**
Hibernate la escribió en el log al conectar, dentro de la URL que le devolvió el driver.

**20. ¿Por qué es un problema que la contraseña salga en el log?**
Porque los logs se copian: en un foro, en un chat, en un informe... y la contraseña se va con ellos.

**21. ¿Qué niveles de log hay y qué consigue poner `WARN`?**
De más a menos detalle: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`. Con `WARN` solo salen avisos y errores; los mensajes `INFO` desaparecen.

**22. ¿Qué perdimos al poner ese logger en `WARN`?**
El bloque "Database info", que también mostraba la versión de MariaDB y el dialecto. La conexión se sigue viendo en las líneas `HikariPool-1 - Added connection`.

### `ddl-auto`

**23. ¿Qué hace cada valor de `ddl-auto`?**
`create`: borra y crea las tablas. `update`: las cambia sin revisión. `none`: nada. `validate`: nada, pero comprueba que encajan con las clases Java.

**24. ¿Por qué `validate` y no `update`?**
Porque `update` cambia la base de datos sin revisión ni historial. Las tablas las creará Flyway con archivos SQL guardados en Git; Hibernate solo comprueba.

**25. ¿Qué valor tenía `ddl-auto` antes de configurarlo?**
`none`: Spring Boot lo pone así, sin avisar, cuando la base de datos no es de memoria. Por eso el test vio que Hibernate no tenía ninguna propiedad `hibernate.hbm2ddl.auto`.

**26. Ahora mismo, ¿qué comprueba `validate`?**
Nada todavía: no hay clases Java que representen tablas. Empezará a comprobar en la fase 11.

### Los tests

**27. ¿Qué hace `@SpringBootTest`?**
Arranca la aplicación completa (conexión incluida) antes de ejecutar los tests de esa clase.

**28. ¿Qué significa `@Autowired` en un test?**
"Spring, dame este objeto de tu almacén." Así el test puede usar el `DataSource` o el `EntityManagerFactory` que creó Spring.

**29. ¿Por qué `try (Connection connection = ...)`?**
Para que la conexión se devuelva siempre al pool al terminar, aunque falle una comprobación. Si no, el pool se iría quedando sin conexiones.

**30. ¿Por qué comprobamos que el usuario es `movies_app` y no `root`?**
Por mínimo privilegio: `movies_app` solo puede tocar la base de datos `movies`. `root` puede hacer cualquier cosa en todo el servidor.

**31. ¿Por qué un test debe poder fallar? ¿Cómo hicimos fallar el test de conexión?**
Porque si nunca falla, no sabes si comprueba algo. Lo hicimos fallar con una contraseña incorrecta en una variable de entorno.

**32. En el 7.1 salió `Errors: 1` y en el 7.3 `Failures: 1`. ¿Por qué?**
En el 7.1 la aplicación no pudo ni arrancar (excepción): *error*. En el 7.3 arrancó, pero la comprobación no se cumplió: *failure*.

**33. ¿Por qué `MoviesApplicationTests` tardó solo 0,027 segundos?**
Porque Spring reutilizó la aplicación ya arrancada para la otra clase de test, que tiene la misma configuración.

**34. Ejecuto los tests con Docker apagado. ¿Qué pasa?**
Fallan los 3, con `Connection refused`: no hay nadie escuchando en el puerto 3307.

### Los errores

**35. En un error muy largo, ¿dónde está la causa real?**
En la última línea que empieza por `Caused by:`.

**36. ¿Qué diferencia hay entre `Connection refused` y `Access denied`?**
`Connection refused`: nadie contesta (MariaDB apagado o puerto equivocado). `Access denied`: MariaDB contesta, pero el usuario o la contraseña están mal.

**37. ¿Qué es el dialecto y por qué sin conexión falla `Unable to determine Dialect`?**
La variante de SQL de cada base de datos. Hibernate lo averigua preguntando a la base de datos; sin conexión no puede preguntar.

**38. En el error ponía `'movies_app'@'172.18.0.1'`. ¿Qué es esa IP?**
La dirección con la que el contenedor de MariaDB ve a tu ordenador a través de la red interna de Docker.

---

## Referencia: piezas y versiones

*Para consultar.*

| Pieza | Qué es | Versión |
|---|---|---|
| Spring Data JPA | Crea consultas a partir de interfaces (fase 12) | 4.1.1 |
| Jakarta Persistence (JPA) | El estándar: anotaciones como `@Entity` (fase 11) | 3.2.0 |
| Hibernate | El programa que aplica JPA | 7.4.5.Final |
| HikariCP | Pool de conexiones (`DataSource`) | 7.0.2 |
| `mariadb-java-client` | Driver JDBC de MariaDB | 3.5.10 |

Decisiones de esta fase: D-32 (leer `.env`), D-33 (contraseña fuera del log), D-34 (`validate`), D-35 (los tests necesitan MariaDB). Ver [decisiones-tecnicas.md](../decisiones-tecnicas.md).
