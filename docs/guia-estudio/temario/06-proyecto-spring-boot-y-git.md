# 06 — Proyecto Spring Boot y Git

> **Fase del plan:** 6. Este tema se irá completando paso a paso: contiene Git (6.1), Spring Initializr (6.2), el plan de dependencias (6.3), la estructura Maven (6.4) y el ciclo de vida de Maven con el primer test (6.5). **Completo.**

## 1. Git: control de versiones

**Git** guarda la historia del proyecto: cada **commit** es una "foto" de los archivos en un momento, con un mensaje que explica qué cambió. Sirve para volver atrás, ver qué se hizo y cuándo, y trabajar con otros.

Estados de un archivo:

| Estado | En `git status --short` | Significado |
|---|---|---|
| Sin seguir (*untracked*) | `??` | Git lo ve, pero no forma parte del repositorio. |
| Preparado (*staged*) | `A` / `M` en verde | Irá en el próximo commit (`git add`). |
| Confirmado (*committed*) | (no aparece) | Guardado en la historia y sin cambios. |
| Ignorado | (no aparece) | Git hace como si no existiera, por una regla de `.gitignore`. |

## 2. `.gitignore`: lo que nunca debe entrar

Hay dos tipos de archivos que no se versionan:

1. **Secretos:** `.env` contiene la contraseña de MariaDB. Si se sube a un repositorio, hay que darla por comprometida aunque luego se borre, porque queda en la historia.
2. **Archivos generados o personales:** se pueden reconstruir o solo valen para un ordenador.

Nuestro `.gitignore`:

| Regla | Qué ignora | Por qué |
|---|---|---|
| `.env` | Credenciales reales | Secreto (fase 4). `.env.example` sí se versiona. |
| `target/` | Clases compiladas y el `.jar` | Maven los genera de nuevo con `mvn package`. |
| `.idea/`, `*.iml`, `.vscode/` | Configuración del IDE | Es personal; cada uno usa su editor. |
| `Thumbs.db`, `.DS_Store` | Archivos del sistema | Los crean Windows y macOS solos. |
| `*.log` | Logs | Se generan al ejecutar. |

Para saber **por qué** se ignora un archivo: `git check-ignore -v <archivo>`. Responde con el archivo, la línea y la regla. Si no imprime nada, el archivo no está ignorado.

## 3. Finales de línea: LF y CRLF

Cada sistema marca el final de una línea de texto de forma distinta:

- **LF** (`\n`): Linux y macOS.
- **CRLF** (`\r\n`): Windows.

Si no se controla, Git puede ver cambios "fantasma": un archivo que nadie ha tocado aparece modificado solo porque cambiaron sus finales de línea. Además, avisa con mensajes como `LF will be replaced by CRLF`.

Hay dos formas de controlarlo:

| Forma | Dónde vive | Problema |
|---|---|---|
| `git config core.autocrlf` | En cada ordenador | Depende de que cada persona lo configure bien. Git para Windows lo deja en `true` a nivel de sistema, y en Linux o macOS no suele estarlo. |
| `.gitattributes` ✅ | En el repositorio | Ninguno: viaja con el proyecto y vale para todos. |

Nuestro `.gitattributes`:

```
* text=auto eol=lf       # todo archivo de texto se guarda con LF
*.cmd text eol=crlf      # salvo los scripts de Windows (como mvnw.cmd, paso 6.2),
*.bat text eol=crlf      # que necesitan CRLF para ejecutarse
```

## 4. Repositorio remoto (GitHub)

El repositorio de tu ordenador es el **local**. Una copia en GitHub es un **remoto**: sirve de copia de seguridad y para compartir el código.

| Comando | Qué hace |
|---|---|
| `git remote add origin <url>` | Registra el remoto con el nombre `origin` (el nombre habitual). |
| `git push -u origin main` | Sube la rama `main`. `-u` la deja enlazada, así que después basta con `git push`. |
| `git pull` | Trae los cambios del remoto. |

La URL `git@github.com:...` usa **SSH**: GitHub te identifica con tu clave SSH, sin pedir contraseña.

### Un commit y una etiqueta por fase (D-30)

En este proyecto, cada fase termina con **un único commit** y una **etiqueta** (*tag*): un nombre fijo que apunta a ese commit.

| Comando | Qué hace |
|---|---|
| `git log --oneline --decorate` | Lista los commits con sus etiquetas: `d2a88c4 (tag: fase-05) docs(fases-01-05): ...` |
| `git tag` | Lista las etiquetas: `fase-05`, `fase-06`... |
| `git checkout fase-05` | Deja los archivos **exactamente** como estaban al terminar la fase 5 |
| `git checkout main` | Vuelve al presente |
| `git tag -a fase-06 -m "..."` | Crea una etiqueta anotada (con autor, fecha y mensaje) |
| `git push origin fase-06` | Sube la etiqueta: `git push` solo no la sube |

Al hacer `git checkout fase-05`, Git avisa con un mensaje como `You are in 'detached HEAD' state`. **No es un error.** Significa que estás mirando un commit concreto y no el final de una rama. Si haces cambios ahí, no se guardan en `main`. Para seguir trabajando, `git checkout main`.

### Reescribir el historial

Cambiar commits que ya están en GitHub obliga a sustituir el historial remoto con `git push --force-with-lease`. La opción `--with-lease` solo sustituye si GitHub sigue como lo dejamos. Así no se borra trabajo que otra persona haya subido entretanto. Solo es aceptable en un repositorio que nadie más usa, y conviene guardar antes una rama de copia: `git branch backup/historial-original`.

## 5. Spring Initializr: generar el esqueleto del proyecto

[Spring Initializr](https://start.spring.io) es el generador oficial de proyectos Spring Boot. Le dices qué quieres y te devuelve un `.zip` con el proyecto listo para compilar.

| Opción | Valor | Significado |
|---|---|---|
| Project | Maven | Herramienta de construcción (la alternativa es Gradle) |
| Language | Java | |
| Spring Boot | 4.1.1 | Versión estable (las `-SNAPSHOT` y `-M2` no lo son) |
| Group | `com.daw` | **groupId**: quién hace el proyecto (dominio al revés) |
| Artifact | `movies` | **artifactId**: nombre del proyecto |
| Package name | `com.daw.movies` | Paquete raíz de las clases = groupId + artifactId |
| Packaging | Jar | Un único `.jar` ejecutable, con el servidor dentro |
| Java | 25 | |
| Dependencies | (ninguna extra) | Ver sección 6 |

Lo que genera:

| Archivo | Para qué |
|---|---|
| `pom.xml` | La "ficha" del proyecto para Maven: versión de Java, de Spring Boot y dependencias |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | **Maven Wrapper**: descarga y usa la versión correcta de Maven (3.10.0). No hace falta instalar Maven: se ejecuta `.\mvnw.cmd` en Windows o `./mvnw` en Linux/macOS |
| `src/main/java/com/daw/movies/MoviesApplication.java` | Clase de arranque: `main` + `@SpringBootApplication` |
| `src/main/resources/application.properties` | Configuración de la aplicación (de momento, solo su nombre) |
| `src/test/java/com/daw/movies/MoviesApplicationTests.java` | Primer test: comprueba que Spring arranca (`contextLoads`) |
| `HELP.md` | Enlaces de ayuda. **No lo copiamos**: no aporta nada al proyecto |
| `.gitignore`, `.gitattributes` | **No los copiamos tal cual**: ya teníamos los nuestros; añadimos a nuestro `.gitignore` las líneas útiles del suyo |

### El padre del `pom.xml`: `spring-boot-starter-parent`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
</parent>
```

Nuestro `pom.xml` **hereda** de este. El padre trae una lista de versiones probadas juntas (el **BOM**, *Bill of Materials*): Hibernate 7.4.5, Flyway 12.4.0, driver de MariaDB 3.5.10... Por eso, al añadir una dependencia, **no escribimos su versión**: la pone Spring Boot. Así no mezclamos versiones incompatibles.

## 6. Dependencias: cuáles y por qué

### Principio: cada dependencia entra en la fase que la necesita

Podríamos marcar todas las dependencias en Initializr desde el principio, pero no lo hacemos:

1. **Spring Boot reacciona a lo que hay en el proyecto** (autoconfiguración). Si añadimos JPA sin haber configurado la base de datos, la aplicación **no arranca**. Si añadimos Spring Web, arranca un servidor que todavía no usamos. Cada dependencia de más es un error que aún no sabemos explicar.
2. **Así se ve qué aporta cada una**: la añadimos, vemos qué cambia y lo documentamos.

Por eso el proyecto empieza solo con lo mínimo, y en cada fase se añade lo que toca.

### Lo que hay ahora (fase 6)

| Dependencia | Scope | Qué trae | Por qué |
|---|---|---|---|
| `spring-boot-starter` | compile | El núcleo de Spring Boot: arranque, autoconfiguración, inyección de dependencias y logs | Sin esto no hay Spring Boot |
| `spring-boot-starter-test` | test | JUnit 5, AssertJ, Mockito y las utilidades de test de Spring | TDD desde la fase 6 (D-03). `scope test`: solo existe al ejecutar tests, no va en el `.jar` final |

### Lo que se añadirá (nombres reales en Spring Boot 4.1.1)

Los nombres se han comprobado generando un `pom.xml` con Initializr para Boot 4.1.1. En Spring Boot 4 **cambiaron varios nombres** respecto a Boot 3: por ejemplo, el antiguo `spring-boot-starter-web` ahora es `spring-boot-starter-webmvc`, y Flyway tiene su propio starter. Si en apuntes o tutoriales ves los nombres antiguos, es por eso.

| Fase | Dependencia | Scope | Para qué |
|---|---|---|---|
| 7 | `org.mariadb.jdbc:mariadb-java-client` | runtime | **Driver JDBC**: sabe hablar el protocolo de MariaDB. `runtime`: el código no lo usa directamente, solo hace falta al ejecutar |
| 7 | `spring-boot-starter-data-jpa` | compile | JPA + Hibernate + Spring Data JPA + `DataSource` con *pool* de conexiones (HikariCP). Lo necesitamos desde la fase 7 para conectar y configurar `ddl-auto` |
| 7 | `spring-boot-starter-data-jpa-test` | test | Herramientas de test para JPA (por ejemplo, `@DataJpaTest`) |
| 8 | `spring-boot-starter-flyway` | compile | Ejecuta las migraciones SQL al arrancar |
| 8 | `org.flywaydb:flyway-mysql` | compile | Módulo de Flyway para MySQL **y MariaDB**. Sin él, Flyway no reconoce MariaDB |
| 8 | `spring-boot-starter-flyway-test` | test | Lo añade Initializr junto a Flyway; en la fase 8 veremos si lo necesitamos |
| 16 | `spring-boot-starter-webmvc` | compile | Spring MVC + servidor Tomcat embebido + conversión a JSON: `@RestController`, `@GetMapping`... |
| 16 | `spring-boot-starter-webmvc-test` | test | Probar controladores simulando peticiones HTTP (`MockMvc`, `@WebMvcTest`) |
| 18 | `spring-boot-starter-validation` | compile | Bean Validation (Hibernate Validator): `@NotBlank`, `@Positive`, `@Valid` |
| 18 | `spring-boot-starter-validation-test` | test | Lo añade Initializr junto a Validation; se valorará en la fase 18 |
| 19 | `spring-boot-testcontainers` | test | Integración de Spring Boot con Testcontainers |
| 19 | `org.testcontainers:testcontainers-junit-jupiter` | test | Arranca y para contenedores desde los tests de JUnit |
| 19 | `org.testcontainers:testcontainers-mariadb` | test | Un MariaDB real y desechable para cada ejecución de los tests de integración |
| 22 (opcional) | MapStruct | compile | Generar mappers automáticamente, solo si los manuales se vuelven repetitivos |
| 24 (opcional) | `spring-boot-starter-security` (+ `-test`) y una librería JWT | compile | Login y protección de rutas. La librería JWT concreta se decidirá en esa fase |

### Las que **no** vamos a usar, y por qué

| Dependencia | Qué hace | Por qué no |
|---|---|---|
| **Lombok** | Genera getters, setters y constructores con anotaciones (`@Data`, `@Getter`...) | Oculta el código que queremos aprender a escribir y entender. Para los DTOs usaremos `record`, que ya es Java estándar y conciso |
| **Spring Boot DevTools** | Reinicia la aplicación al guardar cambios | Comodidad, no aprendizaje. Además, los reinicios automáticos dificultan ver qué hace cada cambio. Se puede añadir más adelante si molesta reiniciar a mano |
| **Docker Compose Support** | Spring Boot arranca `compose.yaml` solo y configura la conexión sin escribirla | Haría "magia" justo con lo que queremos aprender en la fase 7: configurar la conexión a mano |
| **H2** (base de datos en memoria) | Base de datos de juguete para tests | Su SQL no es exactamente el de MariaDB: un test podría pasar en H2 y fallar en MariaDB. Para los tests de integración usaremos MariaDB real (Testcontainers) |
| **Spring Boot Actuator** | Endpoints de monitorización (`/actuator/health`...) | No hay nada que monitorizar en un proyecto de aprendizaje local |
| **Spring REST Docs / OpenAPI** | Documentación automática de la API | Fuera del alcance actual |

## 7. Estructura de un proyecto Maven

Maven sigue la regla de **convención sobre configuración**: espera cada cosa en una carpeta fija, así que el `pom.xml` no tiene que decir dónde está el código.

```
movies/
├── pom.xml                          descripción del proyecto
├── mvnw, mvnw.cmd, .mvn/wrapper/    Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/daw/movies/     código de la aplicación
│   │   └── resources/               configuración y archivos no Java (migraciones SQL en la fase 8)
│   └── test/
│       └── java/com/daw/movies/     tests (mismo paquete que el código)
└── target/                          lo que genera Maven al compilar (no se versiona)
```

| Ruta | Qué va ahí | ¿Va en el `.jar`? |
|---|---|---|
| `src/main/java/` | Código de la aplicación | Sí, compilado (`.class`) |
| `src/main/resources/` | `application.properties` (o `.yaml`), migraciones de Flyway... | Sí, tal cual |
| `src/test/java/` | Tests | No |
| `src/test/resources/` | Configuración solo para tests (se crea si hace falta) | No |
| `target/` | `.class`, informes de tests, el `.jar` | Es el resultado de compilar |

**Los paquetes son carpetas:** `package com.daw.movies;` obliga a que el archivo esté en `.../com/daw/movies/`.

### La clase de arranque: `MoviesApplication`

```java
@SpringBootApplication
public class MoviesApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoviesApplication.class, args);   // crea el contexto de Spring y arranca
    }
}
```

`@SpringBootApplication` agrupa tres anotaciones:

| Anotación | Qué hace |
|---|---|
| `@SpringBootConfiguration` | Marca la clase como la configuración principal |
| `@EnableAutoConfiguration` | Spring Boot se configura solo según las dependencias presentes (por eso se añaden una a una, D-26) |
| `@ComponentScan` | Busca clases anotadas (`@Service`, `@RestController`, `@Component`...) en **`com.daw.movies` y sus subpaquetes** |

**Error típico:** una clase en un paquete fuera de `com.daw.movies` (por ejemplo, `com.daw.otracosa`) **no se encuentra**, y Spring falla al inyectarla. Por eso `controller`, `domain` y `persistence` irán dentro de `com.daw.movies`.

### El primer test: `MoviesApplicationTests`

```java
@SpringBootTest
class MoviesApplicationTests {
    @Test
    void contextLoads() { }
}
```

- `@SpringBootTest` arranca el **contexto completo de Spring**, igual que la aplicación real.
- El método está vacío **a propósito**: el test pasa si Spring arranca y falla si algo lo impide. Será nuestra alarma cuando añadamos dependencias. Con JPA y una conexión mal configurada (fase 7), fallará.
- Está en el **mismo paquete** (`com.daw.movies`) que la clase de arranque, aunque en otra carpeta. `@SpringBootTest` busca la clase con `@SpringBootApplication` en su paquete y en los superiores.
- En JUnit 5 la clase y el método de test no necesitan ser `public`.

### La configuración: `application.properties`

Ahora solo contiene `spring.application.name=movies`. En el paso 7.2 se cambiará a `application.yaml`, que se lee mejor cuando hay muchas propiedades anidadas (`spring.datasource.url`, `spring.datasource.username`...).

## 8. El ciclo de vida de Maven y el primer test

### Fases del ciclo de vida

Maven no ejecuta "comandos sueltos": recorre un **ciclo de vida**, una lista ordenada de fases. Al pedir una fase, Maven ejecuta **todas las anteriores** primero.

| Fase | Qué hace | Plugin que lo hace (visto en nuestra salida) |
|---|---|---|
| `validate` | Comprueba que el `pom.xml` es correcto | — |
| `compile` | Copia `src/main/resources` y compila `src/main/java` en `target/classes` | `resources:3.5.0`, `compiler:3.15.0` |
| `test` | Compila `src/test/java` en `target/test-classes` y ejecuta los tests | `compiler:3.15.0:testCompile`, `surefire:3.5.6` |
| `package` | Empaqueta todo en `target/movies-0.0.1-SNAPSHOT.jar` | `jar`, `spring-boot` |
| `verify`, `install` | Comprobaciones extra; copiar el `.jar` al repositorio local `~/.m2` | — |

Comandos que usaremos:

| Comando | Para qué |
|---|---|
| `.\mvnw.cmd test` | Compilar y ejecutar los tests (lo más habitual) |
| `.\mvnw.cmd spring-boot:run` | Compilar y arrancar la aplicación |
| `.\mvnw.cmd package` | Generar el `.jar` (pasa los tests antes) |
| `.\mvnw.cmd clean` | Borrar `target/` para compilar desde cero |

### Cómo leer la salida de `mvnw test`

1. **`Building movies 0.0.1-SNAPSHOT`**: el proyecto del `pom.xml` (`com.daw:movies`).
2. **`--- compiler:3.15.0:compile ...`** y siguientes: cada línea `---` es un plugin ejecutando una fase.
3. **`Running com.daw.movies.MoviesApplicationTests`**: Surefire, el plugin de tests, ejecuta nuestra clase.
4. **`Found @SpringBootConfiguration com.daw.movies.MoviesApplication`**: `@SpringBootTest` encontró la clase de arranque buscando en su paquete (sección 7).
5. **El dibujo de `Spring`** y `:: Spring Boot :: (v4.1.1)`: Spring empieza a arrancar.
6. **`Started MoviesApplicationTests in 0.965 seconds`**: el contexto de Spring arrancó, y por tanto `contextLoads()` pasa.
7. **`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`** y **`BUILD SUCCESS`**: todo correcto.

`Failures` y `Errors` no son lo mismo: un *failure* es una comprobación que no se cumple (por ejemplo, un `assertEquals` falso), mientras que un *error* es una excepción inesperada (por ejemplo, Spring no puede arrancar).

### Los avisos (`WARNING`) de Mockito

```
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK...
WARNING: A Java agent has been loaded dynamically (...byte-buddy-agent-1.18.11.jar)
WARNING: Dynamic loading of agents will be disallowed by default in a future release
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
```

- **Qué significan:** Mockito (incluido en `spring-boot-starter-test`) se "engancha" a la JVM durante los tests para poder crear objetos falsos. Java 25 avisa de que en el futuro no permitirá engancharse así, en tiempo de ejecución. La última línea es una consecuencia del enganche y tampoco es un error.
- **¿Es un problema?** Hoy no: el test pasa. Pero **en una versión futura de Java dejará de funcionar** si no se configura Mockito como agente en el `pom.xml`.
- **Decisión:** se deja así por ahora y se resolverá cuando usemos Mockito (D-31).

### ¿Por qué la aplicación arranca y termina sola?

Con `.\mvnw.cmd spring-boot:run` aparece `Started MoviesApplication in 0.978 seconds`... y el programa termina con `BUILD SUCCESS`. **No es un fallo:** la aplicación no tiene nada que la mantenga viva. Cuando añadamos `spring-boot-starter-webmvc` (fase 16), arrancará un servidor Tomcat que se queda esperando peticiones, y entonces sí seguirá en marcha hasta que la paremos con `Ctrl+C`.

## Preguntas de repaso

**¿Por qué no marcamos todas las dependencias desde el principio en Initializr?**
Porque Spring Boot se configura solo según las dependencias que encuentra. Con JPA sin base de datos configurada, la aplicación no arrancaría, y tendríamos errores de cosas que todavía no hemos visto. Cada dependencia entra en su fase.

**¿Por qué no escribimos la versión de `mariadb-java-client` en el `pom.xml`?**
Porque la decide el padre, `spring-boot-starter-parent` (su BOM), que trae versiones probadas juntas. En Boot 4.1.1 es la 3.5.10.

**¿Qué diferencia hay entre los scopes `compile`, `runtime` y `test`?**
`compile`: se usa en el código y va en la aplicación. `runtime`: no se usa en el código, pero hace falta al ejecutar (el driver). `test`: solo existe al ejecutar los tests.


**Al hacer el primer commit salió `CRLF will be replaced by LF` para `.env.example`. ¿Es un error?**
No. Es `.gitattributes` haciendo su trabajo: el archivo se creó en Windows con CRLF, y Git lo guarda en el repositorio con LF (`eol=lf`).


**¿Por qué `.env` no aparece en `git status` y `.env.example` sí?**
Porque `.gitignore` tiene la regla `.env`, que solo coincide con ese nombre exacto. `.env.example` es otro nombre, así que no está ignorado. `git check-ignore -v .env` lo confirma: `.gitignore:2:.env  .env`.

**Si se sube `.env` por error y se borra en el commit siguiente, ¿está a salvo la contraseña?**
No. Sigue en la historia de Git: cualquiera puede ver el commit anterior. Habría que cambiar la contraseña.

**¿Por qué no versionamos `target/`?**
Porque son archivos generados: se reconstruyen con Maven a partir del código. Versionarlos llenaría el repositorio de cambios inútiles en cada compilación.

**¿Por qué el `pom.xml` no dice dónde está el código fuente?**
Por convención sobre configuración: Maven ya espera el código en `src/main/java` y los tests en `src/test/java`.

**Si creo `com.daw.services.MovieService` con `@Service`, ¿lo encontrará Spring?**
No. `@ComponentScan` (dentro de `@SpringBootApplication`) solo busca en `com.daw.movies` y sus subpaquetes. Debería ser `com.daw.movies.domain.service.MovieService`.

**¿Qué comprueba `contextLoads()` si está vacío?**
Que el contexto de Spring arranca. Si cualquier configuración o dependencia lo impide, el test falla antes de llegar al método.

**Si ejecuto `.\mvnw.cmd package`, ¿se ejecutan los tests?**
Sí. `package` va después de `test` en el ciclo de vida, y Maven ejecuta todas las fases anteriores. Si un test falla, no se genera el `.jar`.

**¿Por qué `spring-boot:run` arranca la aplicación y termina enseguida?**
Porque no hay nada que la mantenga viva: todavía no hay servidor web. Con Spring Web (fase 16), Tomcat se queda esperando peticiones.

**¿Qué diferencia hay entre un *failure* y un *error* en el resumen de Surefire?**
*Failure*: una comprobación del test no se cumple. *Error*: se lanza una excepción inesperada (por ejemplo, el contexto de Spring no arranca).
