# Fase 7 — Conexión entre Java y MariaDB

> Estado: **completada** (2026-10-20). Conceptos: [temario/07-conexion-jdbc-datasource.md](temario/07-conexion-jdbc-datasource.md).

## Objetivo

Que la aplicación Spring Boot arranque **conectada** a la base de datos MariaDB que corre en Docker (`localhost:3307`), sin escribir secretos en el código y sin que Hibernate modifique el esquema.

## Pasos

| Paso | Estado |
|---|---|
| 7.1 Driver JDBC y `DataSource` | ✅ Hecho (test en rojo, a propósito) |
| 7.2 `application.yaml` con variables de entorno | ✅ Hecho (test en verde; contraseña fuera del log, D-33) |
| 7.3 Hibernate no toca el esquema (`ddl-auto`) | ✅ Hecho (TDD: rojo → verde) |
| 7.4 Comprobar la conexión | ✅ Hecho (con un test, no solo mirando los logs) |

## 7.1 Driver JDBC y `DataSource`

### Qué se ha cambiado

`pom.xml`: tres dependencias nuevas, las previstas para la fase 7 en el [temario 06, sección 6](temario/06-proyecto-spring-boot-y-git.md#6-dependencias-cuáles-y-por-qué) (D-26).

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ninguna lleva `<version>`: la fija el BOM de Spring Boot 4.1.1.

**¿Por qué el driver y JPA a la vez?** El driver, solo, no hace nada: es una librería que sabe hablar con MariaDB, pero nadie la usaría. Quien crea el `DataSource` (el objeto que entrega conexiones) es la autoconfiguración de `spring-boot-starter-jdbc`, que viene dentro de `spring-boot-starter-data-jpa`.

### Versiones reales que ha resuelto Maven

```
> .\mvnw.cmd dependency:tree     (extracto)
+- org.springframework.boot:spring-boot-starter-data-jpa:jar:4.1.1:compile
|  +- org.springframework.boot:spring-boot-starter-jdbc:jar:4.1.1:compile
|  |  \- com.zaxxer:HikariCP:jar:7.0.2:compile
|  |  |  |  \- jakarta.persistence:jakarta.persistence-api:jar:3.2.0:compile
|  |  |  +- org.hibernate.orm:hibernate-core:jar:7.4.5.Final:compile
|  |  +- org.springframework.data:spring-data-jpa:jar:4.1.1:compile
+- org.mariadb.jdbc:mariadb-java-client:jar:3.5.10:runtime
+- org.springframework.boot:spring-boot-starter-data-jpa-test:jar:4.1.1:test
|  +- org.springframework.boot:spring-boot-starter-jdbc-test:jar:4.1.1:test
```

| Librería | Versión | Papel |
|---|---|---|
| `mariadb-java-client` | 3.5.10 | Driver JDBC de MariaDB |
| HikariCP | 7.0.2 | *Pool* de conexiones: la implementación de `DataSource` |
| Hibernate | 7.4.5.Final | Implementación de JPA (el ORM) |
| Jakarta Persistence | 3.2.0 | La especificación JPA (las anotaciones `@Entity`, `@Id`...) |
| Spring Data JPA | 4.1.1 | Repositorios generados a partir de interfaces |

### Predicción (TDD) y resultado real

**Predicción:** el test `contextLoads` (verde al acabar la fase 6) se pondrá en **rojo**, porque Spring Boot verá JPA en el proyecto, intentará crear un `DataSource` y no sabrá a qué base de datos conectarse.

**Resultado de `.\mvnw.cmd test`** (no se descargó nada: las librerías ya estaban en `~/.m2`):

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.

Reason: Failed to determine a suitable driver class

Action:

Consider the following:
	If you want an embedded database (H2, HSQL or Derby), please put it on the classpath.
	If you have database settings to be loaded from a particular profile you may need to activate it (no profiles are currently active).

[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
[ERROR] BUILD FAILURE
```

### Cómo leer el error

- **`'url' attribute is not specified`**: falta `spring.datasource.url`. Es exactamente lo que esperábamos.
- **`Failed to determine a suitable driver class`**: Spring deduce qué driver usar **a partir de la URL** (`jdbc:mariadb://...` → driver de MariaDB). Sin URL no puede deducirlo, aunque el driver esté en el proyecto.
- **`no embedded datasource`** y la sugerencia de H2: si hubiera una base de datos en memoria, Spring la usaría sin configuración. No la queremos (D-27): probamos contra MariaDB real.
- **`Errors: 1`, no `Failures: 1`**: un *failure* es una aserción que no se cumple; un *error* es una excepción inesperada. Aquí ni siquiera se pudo crear el contexto de Spring.
- La cadena de `Caused by` del log va de fuera hacia dentro: `entityManagerFactory` (JPA) necesita `dataSourceScriptDatabaseInitializer`, que necesita `dataSource` (Hikari), que falla. **La causa real es siempre el último `Caused by`.**

El rojo es **temporal y buscado**: el paso 7.2 lo arreglará dando la URL, el usuario y la contraseña.

## 7.2 `application.yaml` con variables de entorno

### El problema

Las credenciales de MariaDB están en `.env` (no se sube a Git, D-17). Docker Compose lee ese archivo solo; **Spring Boot no**. Se eligió importarlo con `spring.config.import` (D-32).

### Archivos

| Archivo | Cambio |
|---|---|
| `src/main/resources/application.properties` | Borrado (`git rm`). Su única línea pasa al YAML |
| `src/main/resources/application.yaml` | Nuevo |

```yaml
spring:
  application:
    name: movies

  config:
    import: optional:file:.env[.properties]

  datasource:
    url: jdbc:mariadb://localhost:${MARIADB_PORT:3307}/${MARIADB_DATABASE:movies}
    username: ${MARIADB_USER}
    password: ${MARIADB_PASSWORD}
```

- `optional:file:.env[.properties]`: lee `.env` desde la carpeta de arranque (la raíz del proyecto). `optional:` evita un error si no existe; `[.properties]` le dice a Spring que lo interprete como `CLAVE=valor`, porque la extensión `.env` no la reconoce.
- `${NOMBRE:valor}`: usa la variable `NOMBRE` y, si no existe, el valor por defecto. El puerto y el nombre de la base de datos tienen valor por defecto porque no son secretos. El usuario y la contraseña **no**: sin ellos la aplicación no debe arrancar.
- Se usan **los mismos nombres de variable que `compose.yaml`**: un único `.env` para Docker y para Spring.
- `properties` y `yaml` son dos formatos para lo mismo. `spring.datasource.url=...` en `.properties` equivale a la jerarquía `spring:` → `datasource:` → `url:` en YAML. En YAML **la sangría es la sintaxis**: dos espacios, nunca tabuladores.

### Primer intento: con Docker apagado

`docker compose ps` → `failed to connect to the docker API ... The system cannot find the file specified`: Docker Desktop no estaba arrancado. Se ejecutó el test igualmente para ver cómo cambiaba el error:

```
HikariPool-1 - Starting...
Socket fail to connect to localhost:3307. Connection refused: getsockopt
...
Caused by: org.hibernate.HibernateException: Unable to determine Dialect without JDBC metadata
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0
[ERROR] BUILD FAILURE
```

Sigue en rojo, pero **por otro motivo**, y eso es avanzar:

- `HikariPool-1 - Starting...`: Spring ya tiene la URL, ha elegido el driver y crea el *pool*.
- `Connection refused`: al otro lado del puerto 3307 no escucha nadie.
- `Unable to determine Dialect`: Hibernate pregunta a la base de datos qué versión es para elegir su **dialecto** (su variante de SQL). Sin conexión no puede.
- Si `${MARIADB_USER}` no se hubiera podido resolver, Spring habría fallado **antes** de intentar conectar. Llegar a conectar demuestra que la importación de `.env` funciona.

### Segundo intento: con MariaDB arrancado

```
> docker compose up -d --wait
 Container movies-mariadb-1 Healthy
```

`--wait` espera a que el *healthcheck* de `compose.yaml` diga que MariaDB está listo.

```
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.mariadb.jdbc.Connection@4451f60c
HikariPool-1 - Start completed.
HHH10001005: Database info:
	Database JDBC URL [jdbc:mariadb://localhost:3307/movies?user=movies_app&password=*****]
	Database driver: MariaDB Connector/J
	Database dialect: MariaDBDialect
	Database version: 12.3.3
	Default catalog/schema: movies/undefined
	Isolation level: REPEATABLE_READ [default REPEATABLE_READ]
	Pool: DataSourceConnectionProvider
Started MoviesApplicationTests in 4.374 seconds
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Verde.** El test `contextLoads` ahora comprueba algo más que antes: que la aplicación **arranca conectada** a MariaDB.

- `Added connection`: Hikari ha abierto la primera conexión real.
- `Database version: 12.3.3`: la versión exacta del contenedor (la imagen `mariadb:12.3` apunta a la última 12.3.x).
- `Database dialect: MariaDBDialect`: Hibernate lo ha deducido solo; no hace falta configurarlo.

### Problema encontrado: la contraseña aparece en el log

*(En este documento se ha sustituido por `*****`.)* La línea `Database JDBC URL` muestra el usuario y **la contraseña en claro**: Hibernate escribe la URL que le devuelve el driver, y el driver de MariaDB incluye ahí las credenciales. También queda copiada en `target/surefire-reports/TEST-com.daw.movies.MoviesApplicationTests.xml` (ignorado por Git). El log temporal de esta prueba se borró.

**Solución (D-33, opción A):** subir a `WARN` el logger que escribe ese bloque:

```yaml
logging:
  level:
    # Hibernate escribe en INFO la URL de conexión con el usuario y la contraseña en claro (D-33).
    org.hibernate.orm.connections.pooling: WARN
```

Resultado real de `.\mvnw.cmd test` después del cambio:

```
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.mariadb.jdbc.Connection@52c9d3d0
HikariPool-1 - Start completed.
Started MoviesApplicationTests in 3.131 seconds
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- Líneas con `password=` en la salida: **0**. En `target/surefire-reports/*.xml`: **0**.
- El bloque `HHH10001005: Database info` ya no aparece. La conexión se sigue viendo en `Added connection`.
- `logging.level.<paquete o clase>` controla qué se escribe en el log. Niveles, de más a menos detalle: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`. Con `WARN` solo salen avisos y errores.

**Lección:** no escribir secretos en el código no basta. Hay que mirar también **qué escriben los logs**.

## 7.3 Hibernate no toca el esquema: `ddl-auto`

### Opciones

| Valor | Qué hace al arrancar |
|---|---|
| `create` / `create-drop` | Borra y crea las tablas: **se pierden los datos** |
| `update` | Cambia las tablas para que encajen con las entidades, sin revisión y sin borrar columnas |
| `none` | Nada |
| `validate` | Nada, pero **comprueba** que tablas y entidades encajan; si no, la aplicación no arranca |

Elegido: **`validate`** ([D-34](decisiones-tecnicas.md)). El esquema es cosa de Flyway (fase 8).

### Rojo: el test primero

Nuevo archivo `src/test/java/com/daw/movies/DatabaseConnectionTests.java`:

```java
@SpringBootTest
class DatabaseConnectionTests {

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Test
	void hibernateOnlyValidatesTheSchema() {
		// El esquema lo crea Flyway: Hibernate solo debe comprobarlo, nunca modificarlo (D-34)
		assertThat(entityManagerFactory.getProperties())
				.containsEntry("hibernate.hbm2ddl.auto", "validate");
	}
}
```

No comprueba lo que hay escrito en `application.yaml`, sino lo que **Hibernate ha recibido**. `spring.jpa.hibernate.ddl-auto` es el nombre de Spring; Hibernate lo recibe como `hibernate.hbm2ddl.auto`.

```
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE! -- in com.daw.movies.DatabaseConnectionTests
Expecting map:
  {"APPLICATION_NAME"="movies", ...}
to contain entries:
  ["hibernate.hbm2ddl.auto"="validate"]
but could not find the following map entries:
  ["hibernate.hbm2ddl.auto"="validate"]
[ERROR] Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
[ERROR] BUILD FAILURE
```

- Es un **failure** (la aserción no se cumple), no un *error*: el contexto sí arrancó.
- En el mapa no había **ninguna** propiedad `hbm2ddl`: para una base de datos que no está en memoria, Spring Boot aplica `none` en silencio y no le pasa nada a Hibernate.

### Verde: el código mínimo

En `application.yaml`:

```yaml
spring:
  # El esquema lo crea y lo cambia Flyway (fase 8). Hibernate solo comprueba que
  # las tablas encajan con las entidades; si no encajan, la aplicación no arranca (D-34).
  jpa:
    hibernate:
      ddl-auto: validate
```

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.025 s -- in com.daw.movies.DatabaseConnectionTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.027 s -- in com.daw.movies.MoviesApplicationTests
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

`MoviesApplicationTests` tarda **0,027 s**: Spring guarda en caché el contexto de la primera clase de test y lo reutiliza en la segunda, porque tienen la misma configuración.

Ahora mismo `validate` no tiene nada que comprobar: todavía no hay entidades. Empezará a trabajar en la fase 11.

## 7.4 Comprobar la conexión

El plan decía "mirar los logs de arranque". Los logs sirven (`HikariPool-1 - Added connection`), pero hay que leerlos a mano cada vez. Un **test** lo comprueba solo en cada ejecución:

```java
	@Autowired
	private DataSource dataSource;

	@Test
	void connectsToMoviesDatabaseAsApplicationUser() throws SQLException {
		// try-with-resources: devuelve la conexión al pool al terminar
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();

			assertThat(metaData.getDatabaseProductName()).isEqualTo("MariaDB");
			assertThat(connection.getCatalog()).isEqualTo("movies");
			// Mínimo privilegio (D-17): la aplicación nunca se conecta como root
			assertThat(metaData.getUserName()).isEqualTo("movies_app");
		}
	}
```

- `dataSource.getConnection()` pide una conexión al *pool* de Hikari. Es JDBC puro: lo que Hibernate hará por nosotros a partir de la fase 11.
- `try (...)` (*try-with-resources*) cierra la conexión al terminar, aunque falle una aserción. En un *pool*, "cerrar" significa **devolverla** para que otro la use.
- `DatabaseMetaData`: información que da el driver sobre la base de datos y la conexión.

```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.daw.movies.DatabaseConnectionTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in com.daw.movies.MoviesApplicationTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Este test pasó **a la primera**, porque la conexión ya funcionaba desde el 7.2. No es un ciclo TDD que obligue a escribir código: es un test de verificación que deja la comprobación fijada.

### Comprobar que el test puede fallar

Un test que nunca falla no demuestra nada. Se ejecutó con una contraseña incorrecta **solo para ese comando** (en Git Bash, `VARIABLE=valor comando`; en PowerShell sería `$env:MARIADB_PASSWORD='...'`, que dura toda la sesión de la terminal):

```
> MARIADB_PASSWORD=wrong-password ./mvnw test
WARN  o.m.jdbc.message.server.ErrorPacket : Error: 1045-28000: Access denied for user 'movies_app'@'172.18.0.1' (using password: YES)
[ERROR] Tests run: 2, Failures: 0, Errors: 2, Skipped: 0 -- in com.daw.movies.DatabaseConnectionTests
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0 -- in com.daw.movies.MoviesApplicationTests
[ERROR] Tests run: 3, Failures: 0, Errors: 3, Skipped: 0
[ERROR] BUILD FAILURE
```

- **Demuestra la precedencia de D-32**: la variable de entorno real ganó a `.env`.
- `1045-28000`: código de error de MariaDB para credenciales incorrectas. No muestra la contraseña, solo `using password: YES`.
- `172.18.0.1`: la IP con la que el contenedor ve a Windows (la puerta de enlace de la red de Docker). Por eso el usuario de la aplicación tiene que poder conectarse desde fuera del contenedor.

### Consecuencia: los tests necesitan MariaDB arrancado

Desde esta fase, si Docker está apagado, **todos** los tests fallan (`Connection refused`, como en el primer intento del 7.2). Antes de `.\mvnw.cmd test` hay que hacer `docker compose up -d` ([D-35](decisiones-tecnicas.md)). En la fase 19, Testcontainers lo resolverá.

## Resumen de la fase 7

- `pom.xml`: `spring-boot-starter-data-jpa`, `mariadb-java-client` (`runtime`) y `spring-boot-starter-data-jpa-test`.
- `application.properties` sustituido por `application.yaml`, que lee `.env` con `spring.config.import` (D-32). Sin secretos en Git.
- La contraseña ya no se escribe en el log (D-33).
- `ddl-auto: validate`: Hibernate no toca el esquema (D-34).
- `DatabaseConnectionTests`: la conexión y la configuración de Hibernate quedan comprobadas por tests. **3 tests en verde**, con MariaDB arrancado (D-35).

| Archivo | Cambio |
|---|---|
| `pom.xml` | 3 dependencias nuevas |
| `src/main/resources/application.properties` | Borrado |
| `src/main/resources/application.yaml` | Nuevo |
| `src/test/java/com/daw/movies/DatabaseConnectionTests.java` | Nuevo (2 tests) |

## Errores de esta fase

| Síntoma | Causa | Solución |
|---|---|---|
| `Failed to configure a DataSource: 'url' attribute is not specified` | JPA en el proyecto sin URL de conexión | Configurar `spring.datasource.url` (7.2). Buscado a propósito en el 7.1 |
| `failed to connect to the docker API` y luego `Connection refused` en `localhost:3307` | Docker Desktop apagado | Abrir Docker Desktop y `docker compose up -d --wait` |
| La contraseña aparecía en el log (`HHH10001005: Database info`) | Hibernate escribe la URL del driver, que incluye las credenciales | Logger `org.hibernate.orm.connections.pooling` a `WARN` (D-33) |

## Preguntas de repaso

**¿Por qué no `ddl-auto: update`, si es tan cómodo?**
Porque hace cambios en la base de datos que nadie revisa ni versiona, no sabe borrar ni renombrar columnas, y el esquema de cada ordenador puede acabar siendo distinto. Con Flyway cada cambio es un archivo SQL revisado, en Git.

**El primer test del 7.3 dio *failure* y el del 7.1 dio *error*. ¿Por qué?**
En el 7.1 ni siquiera arrancó el contexto de Spring (excepción): *error*. En el 7.3 el contexto arrancó y lo que no se cumplió fue la aserción: *failure*.

**¿Cómo sabemos que una variable de entorno gana a `.env`?**
Porque con `MARIADB_PASSWORD=wrong-password` MariaDB rechazó la conexión (`Access denied ... using password: YES`), aunque `.env` tenga la contraseña correcta.

**¿Por qué `try (Connection connection = ...)`?**
Para devolver siempre la conexión al *pool*, aunque falle una aserción. Si no, el *pool* se quedaría sin conexiones.
