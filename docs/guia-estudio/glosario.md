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

**Restricción (*constraint*)**: regla que la base de datos comprueba siempre: `PRIMARY KEY`, `NOT NULL`, `FOREIGN KEY`, `CHECK`, `UNIQUE`.

**Servicio (caso de uso)**: clase del dominio que coordina una operación de la aplicación y aplica sus reglas. Ejemplo: `MovieService.findById`.

**`@SpringBootApplication`**: anotación de la clase de arranque. Agrupa `@SpringBootConfiguration`, `@EnableAutoConfiguration` y `@ComponentScan`. Ver [temario/06](temario/06-proyecto-spring-boot-y-git.md), sección 7.

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

**Uno a muchos (1:N)**: un elemento de un lado se relaciona con varios del otro, pero cada uno de esos solo con uno. Director–película.
