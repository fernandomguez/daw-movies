# Glosario

Términos ordenados alfabéticamente. Se amplía en cada fase.

**Acoplamiento**: grado en que una clase depende de otras. Mucho acoplamiento = un cambio en una clase rompe muchas otras.

**Adaptador**: clase que implementa un puerto del dominio usando una tecnología concreta. Ejemplo: `MovieRepositoryAdapter` implementa `MovieRepository` con JPA. En otros proyectos y apuntes la misma clase se llama `MovieRepositoryImpl`: es el mismo papel con otro nombre (ver D-22). Ver [temario/05](temario/05-arquitectura-tres-capas-dip.md).

**Atributo**: característica de una entidad (título, año, duración). Ver [temario/02](temario/02-entidades-atributos-y-relaciones.md).

**Atributo calculado (derivado)**: dato que no se guarda porque se obtiene de otros. Ejemplos: la edad (a partir de la fecha de nacimiento) y el nombre completo (nombre + apellidos).

**Bean (Spring)**: objeto que Spring crea, guarda y conecta con otros automáticamente.

**BOM (*Bill of Materials*)**: lista de versiones de bibliotecas que se han probado juntas. Spring Boot publica una y, al heredarla, no tenemos que escribir las versiones de cada dependencia.

**`AUTO_INCREMENT`**: opción de MariaDB que asigna automáticamente el siguiente número (1, 2, 3...) a la clave primaria al insertar una fila.

**Capa**: grupo de clases con una misma responsabilidad. Usamos tres: controlador, dominio y persistencia.

**Cardinalidad**: cuántos elementos de una entidad pueden relacionarse con cuántos de otra. Se descubre leyendo la relación en los dos sentidos. Ejemplos: director–película 1:N; película–actor N:M.

**Clave foránea (FK, *foreign key*)**: columna cuyo valor debe existir como clave primaria en otra tabla. Ejemplo: `movie.director_id` → `director.id`. Ver [temario/03](temario/03-diseno-esquema-relacional.md).

**Clave primaria (PK, *primary key*)**: columna o columnas que identifican cada fila de forma única; no se repite ni puede ser `NULL`.

**Clave primaria compuesta**: clave primaria formada por varias columnas. Ejemplo: `(movie_id, actor_id)` en `movie_actor`.

**Clave sustituta (*surrogate key*)**: identificador sin significado (un número) creado solo para identificar filas. Se opone a la **clave natural** (un dato real, como el título).

**Diagrama entidad-relación (ER)**: dibujo de las tablas y sus relaciones con símbolos de cardinalidad.

**Contenedor**: ejecución aislada y desechable de una imagen, con su propio estado. Ejemplo: `movies-mariadb-1`. Ver [temario/04](temario/04-docker-compose-mariadb.md).

**DAO (*Data Access Object*)**: patrón de acceso a datos pensado desde la base de datos: normalmente una clase por tabla con métodos CRUD (`insert`, `update`, `delete`, `findById`). En este proyecto usamos **Repositorio** en su lugar. Ver [temario/05](temario/05-arquitectura-tres-capas-dip.md), sección 10.

**DIP (principio de inversión de dependencias)**: el dominio no depende de la persistencia; ambos dependen de una interfaz que define el dominio. La flecha de dependencia pasa a apuntar hacia el dominio.

**DTO (*Data Transfer Object*)**: objeto que define la forma de los datos que entran o salen de la API. De entrada: `CreateMovieRequest`; de salida: `MovieResponse`. Vive en el controlador. Ver [temario/05](temario/05-arquitectura-tres-capas-dip.md), sección 8.

**Docker Compose**: herramienta que describe uno o varios contenedores en un archivo (`compose.yaml`) y los arranca con `docker compose up -d`.

**Docker Desktop**: aplicación que ejecuta el motor de Docker en Windows. Si no está abierta, los comandos `docker` fallan con "failed to connect to the docker API".

**`.env` / `.env.example`**: archivo con los valores reales de las variables de entorno (secreto, no versionado) y su plantilla con valores falsos (versionada).

**Entidad (modelo conceptual)**: "cosa" del problema con identidad propia, de la que guardamos varias y que tiene datos propios: película, director, actor. No confundir con la *entidad JPA* (una clase Java anotada con `@Entity`).

**Entidad JPA**: clase Java anotada con `@Entity` que refleja una tabla; cada objeto corresponde a una fila. En este proyecto las entidades son las propias clases del dominio (`Movie`, `Director`, `Actor`), sin una `MovieEntity` separada (D-29). En otros proyectos sí se separan.

**Git**: sistema de control de versiones: guarda la historia del proyecto en *commits*. Ver [temario/06](temario/06-proyecto-spring-boot-y-git.md).

**Commit**: "foto" de los archivos del proyecto en un momento, con un mensaje que explica qué cambió.

**`.gitignore`**: archivo con reglas de lo que Git no debe versionar (secretos como `.env`, archivos generados como `target/`). `git check-ignore -v <archivo>` dice qué regla ignora un archivo.

**`.gitattributes`**: archivo que indica a Git cómo tratar ciertos archivos; aquí, qué final de línea usar (LF o CRLF).

**LF / CRLF**: formas de marcar el final de línea. LF (`\n`) en Linux y macOS; CRLF (`\r\n`) en Windows.

**Healthcheck**: comprobación periódica que Docker ejecuta para saber si el servicio de un contenedor funciona (`healthy`) o no (`unhealthy`).

**Hibernate**: la implementación de JPA que usa Spring Boot. JPA son las reglas; Hibernate es el programa que hace el trabajo real y genera el SQL.

**Imagen (Docker)**: plantilla de solo lectura con un programa ya instalado, identificada por nombre y etiqueta. Ejemplo: `mariadb:12.3`.

**Índice**: estructura que permite encontrar filas sin recorrer toda la tabla. Las PK y FK tienen índice automáticamente en MariaDB (InnoDB).

**Inyección de dependencias**: en lugar de que una clase cree sus dependencias, alguien externo (Spring o un test) se las pasa, normalmente por el constructor.

**Integridad referencial**: garantía de que toda clave foránea apunta a una fila que existe.

**JDK (*Java Development Kit*)**: lo necesario para compilar y ejecutar Java. Usamos Temurin 25.

<a id="jpa"></a>**Intercalación (*collation*)**: reglas para comparar y ordenar texto. `utf8mb4_uca1400_ai_ci` ignora tildes y mayúsculas al comparar.

**Juego de caracteres (*charset*)**: conjunto de caracteres que se pueden guardar. `utf8mb4` admite cualquier carácter Unicode (tildes, eñes, emojis).

**JPA (*Jakarta Persistence API*)**: especificación (conjunto de reglas y anotaciones) para traducir automáticamente entre filas de una tabla y objetos Java. Ejemplo: una clase `DirectorEntity` marcada con `@Entity` y `@Table(name = "director")` permite leer la fila de Nolan y obtener un objeto ya relleno, sin escribir el `SELECT` a mano. En este proyecto JPA **no crea las tablas** (eso lo hace Flyway): solo lee y escribe datos. Se estudia en la fase 11. Explicación completa: [temario/11-introduccion-jpa.md](temario/11-introduccion-jpa.md).

**Java puro (dominio)**: clases que solo usan Java y su biblioteca estándar, sin anotaciones ni `import` de frameworks (Spring, JPA, Jackson, Bean Validation). Se prueban con un simple `new`. En este proyecto el dominio **no** es Java puro estricto: lleva anotaciones JPA y `@Service` (D-19, D-29). Ver [temario/05](temario/05-arquitectura-tres-capas-dip.md), secciones 3 y 11.

**JWT (*JSON Web Token*)**: "pase" firmado que el servidor entrega al usuario tras hacer login. El cliente lo envía en cada petición para demostrar quién es, sin repetir la contraseña. Se usa con Spring Security (fase opcional 24).

**LTS (*Long-Term Support*)**: versión con soporte durante varios años. Java 25 y MariaDB 12.3 son LTS.

**Mapper**: clase que convierte un objeto de una capa en el de otra al cruzar una frontera. En este proyecto hay uno: `MovieDtoMapper` (dominio ↔ DTO). El `MovieEntityMapper` (entidad ↔ dominio) del diseño inicial desapareció al poner JPA en el dominio (D-29). Ver [temario/05](temario/05-arquitectura-tres-capas-dip.md), secciones 8 y 11.

**Ciclo de vida (Maven)**: lista ordenada de fases (`validate`, `compile`, `test`, `package`, `verify`, `install`). Al pedir una fase, Maven ejecuta antes todas las anteriores.

**Surefire**: plugin de Maven que ejecuta los tests en la fase `test` y deja los informes en `target/surefire-reports`.

**Mockito**: librería para crear objetos falsos (*mocks*) en los tests. Viene en `spring-boot-starter-test`.

**Maven Wrapper (`mvnw` / `mvnw.cmd`)**: script que descarga y usa la versión de Maven fijada en `.mvn/wrapper/maven-wrapper.properties` (3.10.0). Así no hace falta instalar Maven. En Windows: `.\mvnw.cmd test`.

**Maven**: herramienta que descarga dependencias, compila, ejecuta tests y empaqueta el proyecto a partir del `pom.xml`.

**Mínimo privilegio**: dar a cada usuario solo los permisos que necesita. La aplicación usa `movies_app`, que solo accede a la base de datos `movies`.

**Migración (Flyway)**: archivo SQL versionado que cambia el esquema de la base de datos de forma ordenada y repetible.

**Modelo de dominio**: clases que representan el negocio y sus reglas (`Movie`, `Director`, `Actor`). En este proyecto llevan anotaciones JPA (D-29), pero no Spring Web, Spring Data ni Jackson.

**Muchos a muchos (N:M)**: cada elemento de un lado puede relacionarse con varios del otro, y viceversa. Película–actor. En una base de datos relacional necesita una tabla intermedia.

**`NULL`**: ausencia de valor en una columna. `NOT NULL` lo prohíbe.

**`ON DELETE`**: qué hace la base de datos con las filas que referencian a una fila que se borra: `RESTRICT` (prohíbe el borrado), `CASCADE` (las borra también) o `SET NULL` (deja la FK vacía).

**ORM (*Object-Relational Mapping*)**: técnica o herramienta que traduce entre objetos (Java) y tablas relacionales (SQL). JPA/Hibernate es un ORM.

**Puerto (arquitectura)**: interfaz definida por el dominio que expresa lo que necesita del exterior. Ejemplo: `MovieRepository`. No confundir con un puerto de red.

**Publicar un puerto**: conectar un puerto de tu ordenador con uno del contenedor. `"3307:3306"` = el 3307 de Windows lleva al 3306 del contenedor.

**Primera forma normal (1FN)**: regla de diseño según la cual cada celda contiene un solo valor (nada de listas como "1, 2, 3, 4").

**Repositorio (patrón)**: interfaz pensada desde el dominio que se comporta como una colección de objetos de negocio (`save(Movie)`, `findByDirector`). La define el dominio (es un puerto) y la implementa la persistencia. No confundir con un repositorio Git. Ver [temario/05](temario/05-arquitectura-tres-capas-dip.md), sección 10.

**Restricción (*constraint*)**: regla que la base de datos comprueba siempre: `PRIMARY KEY`, `NOT NULL`, `FOREIGN KEY`, `CHECK`, `UNIQUE`. En MySQL/MariaDB, la clave primaria se llama siempre `PRIMARY` (su nombre propio se ignora, aviso 1280); las `FOREIGN KEY` y los `CHECK` sí conservan el nombre y lo muestran en los errores.

**Servicio (caso de uso)**: clase del dominio que coordina una operación de la aplicación y aplica sus reglas. Ejemplo: `MovieService.findById`.

**`@SpringBootApplication`**: anotación de la clase de arranque. Agrupa `@SpringBootConfiguration`, `@EnableAutoConfiguration` y `@ComponentScan`. Ver [temario/06](temario/06-proyecto-spring-boot-y-git.md), sección 6.

**Autoconfiguración**: Spring Boot se configura solo según las dependencias que encuentra (por ejemplo, con el driver de MariaDB y JPA intenta crear la conexión).

**Component scan (escaneo de componentes)**: búsqueda de clases anotadas (`@Service`, `@RestController`...) que hace Spring en el paquete de la clase de arranque (`com.daw.movies`) y sus subpaquetes.

**Contexto de Spring (*ApplicationContext*)**: el "contenedor" donde Spring crea y guarda los objetos de la aplicación (*beans*) y los conecta entre sí.

**Convención sobre configuración**: principio de Maven y Spring Boot: si sigues la estructura esperada (`src/main/java`...), no hace falta configurarla.

**Spring Initializr**: generador oficial de proyectos Spring Boot (https://start.spring.io). Devuelve un `.zip` con `pom.xml`, Maven Wrapper y la clase de arranque.

**Starter (Spring Boot)**: dependencia "paquete" que trae todo lo necesario para una tecnología, con versiones compatibles. Ejemplo: `spring-boot-starter-data-jpa` trae JPA, Hibernate y Spring Data. En Boot 4 cada starter tiene su pareja de test (`...-test`).

**BOM (*Bill of Materials*)**: lista de versiones probadas juntas. La trae `spring-boot-starter-parent`; por eso las dependencias del `pom.xml` no llevan versión.

**`pom.xml`**: archivo que describe el proyecto para Maven: groupId, artifactId, versión de Java, padre de Spring Boot, dependencias y plugins.

**groupId / artifactId**: identificadores de un proyecto Maven. groupId = quién lo hace (`com.daw`); artifactId = nombre del proyecto (`movies`).

**Scope (Maven)**: cuándo está disponible una dependencia. `compile`: siempre; `runtime`: solo al ejecutar (el driver JDBC); `test`: solo en los tests.

**Spring Security**: módulo de Spring que controla quién puede entrar a la aplicación (autenticación) y qué puede hacer (autorización). Fase opcional 24.

**Spring Data JPA**: capa de Spring sobre JPA que genera automáticamente métodos habituales (`findById`, `findAll`, `save`...) a partir de una interfaz.

**Relación**: conexión entre entidades ("un director dirige películas"). Si llega a tener datos propios (p. ej., el nombre del personaje), pasa a tratarse como entidad.

**Tabla intermedia (de unión)**: tabla que representa una relación N:M guardando parejas de claves foráneas. Ejemplo: `movie_actor`.

**TDD (*Test-Driven Development*)**: escribir primero un test que falla, después el código mínimo que lo hace pasar y, por último, mejorar el código.

**Volumen (Docker)**: almacenamiento gestionado por Docker fuera del contenedor. Los datos sobreviven aunque el contenedor se borre (salvo con `docker compose down -v`).

**JDBC (*Java Database Connectivity*)**: API estándar de Java para hablar con bases de datos relacionales. Define interfaces (`Connection`, `ResultSet`...); cada base de datos aporta su implementación (el driver). Ver [temario/07](temario/07-conexion-jdbc-datasource.md).

**Driver JDBC**: librería que implementa JDBC para una base de datos concreta. Para MariaDB, `mariadb-java-client`. Spring elige cuál usar a partir de la URL (`jdbc:mariadb://...`).

**URL JDBC**: cadena que dice a qué base de datos conectarse: `jdbc:mariadb://localhost:3307/movies` (driver, máquina, puerto y base de datos).

**`DataSource`**: interfaz estándar de Java para "algo que entrega conexiones a la base de datos". Spring Boot la crea sola si tiene la URL, el usuario y la contraseña.

**Pool de conexiones / HikariCP**: grupo de conexiones abiertas que se reutilizan, porque abrir una conexión nueva es lento. HikariCP es la implementación que usa Spring Boot por defecto.

**Error frente a fallo (JUnit)**: un *failure* es una aserción que no se cumple; un *error* es una excepción inesperada (por ejemplo, que el contexto de Spring no pueda arrancar).

**Dialecto (Hibernate)**: clase que conoce las particularidades del SQL de una base de datos concreta. Hibernate lo elige solo al conectar (`MariaDBDialect`). Ver [temario/07](temario/07-conexion-jdbc-datasource.md), sección 7.

**`spring.config.import`**: propiedad que añade otros archivos de configuración. La usamos para leer `.env`: `optional:file:.env[.properties]` (D-32).

**Marcador (*placeholder*) `${...}`**: referencia a otra propiedad o variable de entorno dentro de la configuración. `${NOMBRE:valor}` incluye un valor por defecto.

**YAML**: formato de configuración basado en la sangría (dos espacios, nunca tabuladores). `application.yaml` equivale a `application.properties` escrito de forma jerárquica.

**Nivel de log (`logging.level`)**: cuánto detalle escribe un logger: `TRACE` < `DEBUG` < `INFO` < `WARN` < `ERROR`. Se configura por paquete en `application.yaml`.

**`ddl-auto`**: propiedad (`spring.jpa.hibernate.ddl-auto`) que decide si Hibernate crea, cambia, valida o ignora las tablas al arrancar. Usamos `validate`: el esquema es de Flyway (D-34).

**`@SpringBootTest`**: anotación de test que arranca la aplicación completa (con la conexión a la base de datos) para probar las piezas juntas.

**Caché de contextos (tests)**: Spring reutiliza el contexto arrancado para otras clases de test con la misma configuración, así solo se arranca una vez.

**try-with-resources**: `try (Recurso r = ...) { }`. Java cierra el recurso al salir del bloque, aunque haya una excepción. Con un pool, cerrar una conexión significa devolverla.

**`flyway_schema_history`**: tabla de control que crea Flyway en nuestra base de datos. Apunta qué migraciones se han aplicado, cuándo y con qué checksum. Ver [temario/08](temario/08-migraciones-flyway.md).

**Checksum**: número calculado a partir del contenido de un archivo. Si el archivo cambia, el número cambia. Flyway lo usa para detectar migraciones editadas después de aplicarse.

**Migración pendiente**: migración que está en la carpeta `db/migration` pero todavía no aparece en `flyway_schema_history`. Flyway la ejecuta al arrancar.

**Autoconfiguración (*auto-configuration*)**: mecanismo de Spring Boot por el que, al encontrar una librería en el *classpath*, la configura solo. Es lo que hace que baste con añadir `spring-boot-starter-flyway` para que Flyway se ejecute sin escribir código.

**`db/migration` (locations)**: carpeta por defecto donde Flyway busca los `.sql` (`src/main/resources/db/migration`, en el `classpath`). Se cambia con `spring.flyway.locations`.

**Uno a muchos (1:N)**: un elemento de un lado se relaciona con varios del otro, pero cada uno de esos solo con uno. Director–película.

**API REST**: forma de ofrecer los datos de una aplicación a otros programas por HTTP. Cada cosa (película, director) es un **recurso** con su URL, y lo que se hace con ella lo dice el método (`GET`, `POST`, `PUT`, `DELETE`). Ver [temario/08-diseno-de-endpoints](temario/08-diseno-de-endpoints.md).

**Endpoint**: cada operación que ofrece la API: un método HTTP más una URL. Ejemplo: `GET /movies/3` = leer la película 3.

**Recurso (REST)**: cosa a la que se accede con una URL, nombrada con un sustantivo en plural: `/movies`, `/directors/1`.

**Frontend / backend**: el frontend es lo que ve el usuario (una web o una app) y pinta las pantallas; el backend es este proyecto: guarda los datos y los entrega por la API. El back devuelve datos y códigos; el front decide cómo mostrarlos.

**JSON**: formato de texto para enviar datos entre programas: `{ "id": 1, "title": "The Prestige" }`. Es lo que devolverá nuestra API.

**Código de estado HTTP**: número que acompaña a cada respuesta y dice cómo ha ido. `200` = bien; `400` = petición mal hecha; `404` = no existe. Los 4xx son errores de quien pide; los 5xx, del servidor.

**Problem Details**: formato estándar (RFC 9457) para el cuerpo de una respuesta de error, con campos como `status`, `title` y `detail`. Spring lo trae incorporado.

**Contrato de la API**: la forma acordada de peticiones y respuestas. Quien usa la API depende de ella, así que no se cambia a la ligera. Por eso los actores se devuelven con su `id` aunque hoy solo se muestre el nombre (D-36).

**`TEXT` (MariaDB)**: tipo de columna para texto largo, sin el límite pequeño de `VARCHAR(n)`. Lo usa `biography` (D-37).

**Datos de prueba (semilla, *seed data*)**: filas ficticias para desarrollar y probar. No son esquema: se borran y se recargan cuando hace falta. En el proyecto viven en `db/testdata/seed.sql`, fuera de Flyway (D-42).

**`INSERT`**: sentencia SQL que añade filas a una tabla. `INSERT INTO tabla (columnas) VALUES (...)`.

**`SELECT`**: sentencia SQL que lee datos. `SELECT columnas FROM tabla WHERE condición ORDER BY ...`.

**`JOIN` / `INNER JOIN`**: une filas de dos tablas que cumplen una condición (`ON`). Si una fila no encuentra pareja, **desaparece** del resultado. Ejemplo: película + su director.

**`LEFT JOIN`**: como `INNER JOIN`, pero conserva **todas** las filas de la tabla de la izquierda y rellena con `NULL` la parte derecha cuando no hay pareja. Ejemplo: directores + sus películas, incluido el director que no tiene ninguna.

**`ON`**: condición que empareja las filas de un `JOIN`: `ON d.id = m.director_id`. Sin `ON` se obtiene el producto cartesiano.

**Producto cartesiano**: resultado de unir dos tablas sin condición: cada fila con todas las de la otra (10 × 6 = 60). Casi siempre es un `ON` olvidado.

**`GROUP BY`**: agrupa filas que comparten un valor para resumirlas con agregados (`COUNT`, `SUM`, `AVG`...). Una fila de resultado por grupo.

**Agregado**: función que resume muchas filas en un valor: `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`. Ignoran los `NULL` (salvo `COUNT(*)`, que cuenta filas).

**`COUNT(*)` frente a `COUNT(columna)`**: `COUNT(*)` cuenta filas; `COUNT(columna)` cuenta valores no nulos. Con `LEFT JOIN`, el director sin películas da 1 con `COUNT(*)` y 0 con `COUNT(m.id)`.

**`HAVING`**: filtra **grupos** después de `GROUP BY`; `WHERE` filtra **filas** antes. `HAVING COUNT(*) > 1` es válido; `WHERE COUNT(*) > 1`, no.

**`GROUP_CONCAT`**: une en un texto los valores de un grupo. Útil para comprobar datos; no para el contrato de la API, que devuelve listas.

**`IFNULL` / `COALESCE`**: devuelven un valor alternativo cuando el primero es `NULL`. `IFNULL` es de MySQL/MariaDB; `COALESCE` es estándar y admite varios argumentos.

**SQLSTATE**: código estándar de error de SQL. `23000` significa "violación de restricción de integridad" (FK, PK, `CHECK`). El número concreto que va después (`1452`, `1062`...) es del motor.

**`ERROR 1452` / `ERROR 1451`**: `1452` = se intenta insertar un hijo con una FK que no existe; `1451` = se intenta borrar un padre que tiene hijos con FK `RESTRICT`.

**`ERROR 4025`**: un `CHECK` con nombre ha fallado. El mensaje incluye el nombre (`chk_movie_duration_positive`).

**Idempotente**: operación que se puede repetir sin cambiar el resultado. El script de datos **no** lo es (la segunda ejecución da `ERROR 1062`); por eso hay que vaciar antes las tablas.

**Invariante**: condición que un objeto debe cumplir **siempre**, desde que se crea hasta que se destruye. Ejemplo: una duración de película siempre es mayor que cero. Si el objeto se puede construir violándola, no es una invariante, es una recomendación.

**`Value object` (objeto de valor)**: tipo pequeño e **inmutable** que representa un concepto del negocio y se compara **por su valor**, no por identidad. Protege su invariante en el constructor, así que un `Title` vacío no puede existir. En este proyecto: `Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate` y `Biography` (D-46).

**`Primitive obsession`**: usar tipos genéricos (`String`, `int`, `LocalDate`) para conceptos del dominio. El compilador no distingue un título de un nombre, y la regla ("no vacío", "mayor que 0") queda suelta en cada sitio que use el dato. Se corrige con *value objects*.

**`record`**: clase inmutable de Java que genera constructor, accesores (`title.value()`), `equals`, `hashCode` y `toString`. No tiene setters. Su **constructor compacto** permite validar sin repetir la asignación de campos.

**Constructor compacto**: forma de un `record` para validar o normalizar sus componentes sin escribir el constructor completo: `public Title { if (...) throw ...; value = value.trim(); }`. Se ejecuta en **toda** creación del objeto.

**OpenAPI**: estándar para describir una API HTTP en YAML o JSON (rutas, parámetros, cuerpos, respuestas y esquemas). Antes se llamaba *Swagger Specification*. En este proyecto, `src/main/resources/static/openapi.yaml` (OAS 3.0.3) es el contrato de la v1 (D-47).

**Swagger UI**: aplicación web que lee un documento OpenAPI y lo pinta como página navegable, con "Try it out". No es lo mismo que OpenAPI: la UI **necesita** un documento; el documento no necesita la UI.

**springdoc-openapi**: librería que genera un documento OpenAPI a partir de los `@RestController` de Spring y sirve Swagger UI. En este proyecto se usa la **3.2.0** (rama para Spring Boot 4), apuntando a nuestro contrato con `springdoc.swagger-ui.url=/openapi.yaml`.

**API First (*contract first*)**: escribir el contrato de la API **antes** que el código. El contrato manda: si el código no coincide, se corrige el código. Lo contrario es *code first*, donde el documento se genera después y puede no reflejar lo acordado.

**`operationId`**: nombre único de una operación dentro del documento OpenAPI (`listMovies`). Lo usan Swagger UI y los generadores de clientes.
