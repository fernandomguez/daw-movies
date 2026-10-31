# Decisiones técnicas

Formato: contexto → alternativas → decisión → motivo. Las decisiones "pendientes" esperan confirmación.

## D-01 Versiones base — ✅ Decidida (requisito del proyecto)
Java 25, Spring Boot 4.1.1 y Maven. Verificadas en fuentes oficiales (ver [01-inspeccion-inicial.md](01-inspeccion-inicial.md)). No se cambian sin consultarlo.

## D-02 Idioma del código — ✅ Decidida (impuesta por `base-standards.md`)
Código, nombres de tablas y columnas, tests y commits en inglés. Comentarios, documentación y mensajes en español.

## D-03 Pruebas desde el principio — ✅ Decidida
- **Contexto:** `base-standards.md` exige TDD; el plan original situaba las pruebas al final.
- **Decisión:** pruebas unitarias desde la fase 6 (test primero cuando haya lógica Java) y pruebas de integración en la fase 19. Lo que no es código Java (Docker, SQL) se comprueba con comandos y consultas documentados.

## D-04 Versión de MariaDB — ✅ Decidida: `mariadb:12.3` (riesgo cerrado en la fase 8)
- **Alternativas:** 11.4 LTS, 11.8 LTS, 12.3 LTS.
- **Decisión:** 12.3, la LTS más reciente (soporte comunitario hasta junio de 2029).
- **Riesgo conocido:** la documentación de Flyway verifica MariaDB hasta la 11.7. Es posible que Flyway muestre un aviso de versión no probada.
- **Comprobado en la fase 8 (paso 8.3), con la ejecución real:**
  ```
  WARN : Using MariaDB 12.3 which is newer than the version Flyway has been verified with.
         The latest verified version of MariaDB is 11.7.
  ```
  El aviso aparece, pero **no bloquea**: Flyway crea `flyway_schema_history` y los tests pasan. Se mantiene la 12.3. Si en el futuro una migración fallara de forma rara, este aviso es la primera pista.

## D-05 Guía de estudio — ✅ Decidida
- Un archivo por fase (lo que hicimos), creado al empezar la fase.
- Carpeta `temario/` con los conceptos explicados mediante ejemplos de este proyecto. Los archivos se numeran con la fase del plan a la que pertenecen (`11-introduccion-jpa.md`), para que el temario se lea en orden de aprendizaje aunque un tema se explique por adelantado.
- `decisiones-tecnicas.md` y `glosario.md` (definiciones cortas que enlazan al temario).

## D-06 Orden de las fases — ✅ Aprobado
Diseño del esquema antes que Docker; dominio antes que JPA. Motivos en [00-plan-de-aprendizaje.md](00-plan-de-aprendizaje.md).
JPA se mantiene en la fase 11, con una introducción previa en [temario/11-introduccion-jpa.md](temario/11-introduccion-jpa.md).

## D-07 Alcance inicial del dominio — ✅ Aprobado
- **Tablas:** `director`, `movie`, `actor` y `movie_actor` (tabla intermedia, PK compuesta).
- **Datos:** 10 películas reales, 5 directores y 14 actores, con actores compartidos entre películas.
- **Excluidos a propósito:** *The Matrix* (dos directoras, no encaja con "un director principal") y entidades extra (géneros, productoras, etc.).

## D-08 Normas del proyecto — ✅ Confirmado
Solo existe `base-standards.md`. Las referencias a `backend-standards.md` y a la carpeta `skills/` no aplican.

## D-09 Atributos del modelo conceptual — ✅ Decidido (fase 2)
| Decisión | Alternativa descartada | Motivo |
|---|---|---|
| Año de estreno (entero) | Fecha completa | La fecha de estreno varía según el país; el año es estable. |
| Duración en minutos (entero) | "2 h 28 min" como texto | Un número se compara y ordena; el formato de horas se calcula al mostrarlo. |
| Nombre y apellidos separados | Nombre completo en un campo | Permite ordenar y buscar por apellido. Casos límite: "del Toro", "Samuel L.". |
| Fecha de nacimiento | Edad | La edad cambia; se calcula a partir de la fecha. |
| Identificador propio para cada entidad | Título como identificador | El título puede repetirse (remakes) o corregirse. |
| Director y actor separados (provisional) | Entidad "persona" | Más sencillo; se revisa en la fase 3 (caso Tarantino). |

## D-10 Fecha de nacimiento obligatoria — ✅ Decidido (fase 3)
- **Alternativas:** obligatoria (`NOT NULL`) u opcional (admite `NULL`).
- **Decisión:** obligatoria.
- **Motivo:** todos nuestros datos la tienen y evita tratar valores vacíos en Java mientras aprendemos lo básico. Si en el futuro hiciera falta, convertirla en opcional sería un buen ejercicio de migración con Flyway.

## D-11 Director y actor en tablas separadas — ✅ Decidido (fase 3)
- **Alternativas:** dos tablas; una tabla `person` con papeles.
- **Decisión:** dos tablas.
- **Motivo:** más fácil de entender y consultar. La duplicación es de estructura (4 columnas), no de datos: en nuestro conjunto nadie es director y actor a la vez. Se revisaría si fuera habitual.

## D-12 Tipos de datos — ✅ Decidido (fase 3)
| Dato | Tipo | Alternativa descartada | Motivo |
|---|---|---|---|
| Identificadores | `BIGINT` | `INT` | Se corresponde con `Long` en Java, el tipo habitual para identificadores. |
| Año de estreno | `INT` | `YEAR`, `SMALLINT` | `YEAR` tiene un rango limitado y no se corresponde bien con Java; `SMALLINT` obligaría a usar `Short`. `INT` → `Integer`. |
| Duración | `INT` | `SMALLINT` | Mismo motivo. |
| Fecha de nacimiento | `DATE` | `DATETIME` | No necesitamos la hora. `DATE` → `LocalDate`. |

## D-13 Comportamiento al borrar (`ON DELETE`) — ✅ Decidido (fase 3)
| FK | Opción | Motivo |
|---|---|---|
| `movie.director_id` | `RESTRICT` | No borrar películas a escondidas; `SET NULL` imposible porque la columna es obligatoria. |
| `movie_actor.movie_id` | `CASCADE` | Las participaciones no tienen sentido sin su película. |
| `movie_actor.actor_id` | `RESTRICT` | No sacar a un actor de sus películas en silencio. |

## D-14 Clave primaria de `movie_actor` — ✅ Decidido (fase 3)
- **Alternativas:** PK compuesta `(movie_id, actor_id)`; columna `id` propia + `UNIQUE (movie_id, actor_id)`.
- **Decisión:** PK compuesta.
- **Motivo:** la tabla no tiene datos propios y la PK compuesta ya impide repetir la misma pareja. La alternativa tendría sentido si la participación pasara a tener datos (fase opcional 23).

## D-15 Puerto 3307 para MariaDB en Docker — ✅ Decidido (fase 4)
- **Contexto:** el 3306 lo ocupa un MariaDB instalado como servicio de Windows.
- **Alternativas:** parar o desinstalar el servicio de Windows; publicar el contenedor en otro puerto.
- **Decisión:** `"3307:3306"`, configurable con `MARIADB_PORT` en `.env`.
- **Motivo:** no tocar software del sistema que puede usarse en otras asignaturas. La aplicación deberá conectarse a `localhost:3307`.

## D-16 Juego de caracteres — ✅ Decidido (fase 4)
`utf8mb4` con `utf8mb4_uca1400_ai_ci`, escrito explícitamente en `compose.yaml` aunque coincide con el valor por defecto de MariaDB 12.3, para no depender de él. Comprobado: las tildes se guardan correctamente.

## D-17 Credenciales y usuario de la aplicación — ✅ Decidido (fase 4)
- Contraseñas en `.env` (excluido por `.gitignore`); `.env.example` con `change_me` como plantilla versionada.
- La aplicación usará el usuario `movies_app`, con permisos solo sobre la base de datos `movies` (mínimo privilegio). `root` queda solo para administración.

## D-18 Paquetes organizados por capa — ✅ Decidido (fase 5)
- **Alternativas:** por capa (`controller`, `domain`, `persistence`); por funcionalidad (`movie`, `director`, `actor`).
- **Decisión:** por capa, más `config/` para el ensamblaje. (Revisado con D-19: `config/` solo se creará si hace falta otra configuración.)
- **Motivo:** el objetivo es aprender las capas, y con tres entidades la organización por capa es clara. Los paquetes se crean cuando se necesitan.

## D-19 Servicios del dominio con `@Service` — ✅ Decidido por el alumno (2026-10-20, se aplica en la fase 12)
- **Alternativas:** (A) dominio puro y una clase `@Configuration` en `config/` que crea sus objetos con `@Bean`; (B) permitir `@Service` en los servicios del dominio.
- **Recomendación inicial:** A (dominio sin Spring, inyección visible).
- **Decisión:** B. Es la forma habitual, la que se usa en clase, y más sencilla: Spring detecta el servicio solo. `config/` deja de ser necesario para los servicios (se creará solo si hace falta otra configuración).
- **Consecuencia:** el dominio depende de las anotaciones de Spring `@Service` (y en la fase 17, probablemente `@Transactional`). Sigue sin depender de Spring Web, Spring Data ni Jackson.

## D-20 Repositorio (puerto + adaptador) en lugar de DAO — ✅ Decidido (consecuencia de D-18 y de la DIP, fase 5)
- **Alternativas:** (A) Repositorio: interfaz definida por el dominio (`MovieRepository`) e implementada por un adaptador con Spring Data; (B) DAO: interfaz en la persistencia (`MovieDao` + `MovieDaoImpl`) que usa directamente el servicio.
- **Decisión:** A. Con DAO, el servicio dependería de la persistencia, que es justo lo que la DIP evita. Se justificará con código en el paso 12.1.
- **Explicación:** [temario/05](temario/05-arquitectura-tres-capas-dip.md), secciones 8 y 10.

## D-21 Servicio como interfaz + implementación (`MovieService` + `MovieServiceImpl`) — ⏳ Pendiente (se decide en la fase 16)
- **Alternativas:** (A) una sola clase `MovieService`; (B) interfaz + `Impl`, muy habitual en clase.
- **A valorar:** con un único servicio y tests que usan repositorios falsos, la interfaz puede no aportar nada; pero conviene conocer el patrón.

## D-22 Nombre del adaptador: `MovieRepositoryAdapter` (no `MovieRepositoryImpl`) — ✅ Decidido
- **Alternativas:** `MovieRepositoryAdapter` o `MovieRepositoryImpl`. Las dos cumplen el mismo papel: implementar el puerto `MovieRepository`.
- **Decisión:** `Adapter`. Coincide con el vocabulario de puertos y adaptadores del tema 5 y dice más que `Impl`: traduce entre el dominio y Spring Data/JPA. Además, Spring Data da un significado especial al sufijo `Impl` (implementaciones de métodos personalizados de repositorios), así que evitarlo previene confusiones.

## D-23 Seguridad con JWT — ✅ Añadida al plan como fase opcional 24
- **Motivo:** el alumno lo verá en clase. Va al final porque necesita la API terminada (depende de la fase 18).

## D-24 Finales de línea con `.gitattributes` — ✅ Decidido (fase 6)
- **Alternativas:** (A) `.gitattributes` en el repositorio; (B) `core.autocrlf` en la configuración de cada ordenador.
- **Decisión:** A, con `* text=auto eol=lf` y CRLF solo para `*.cmd` y `*.bat`. Viaja con el proyecto y no depende de cómo tenga configurado Git cada persona (en este equipo, `core.autocrlf = true` venía del instalador de Git para Windows, a nivel de sistema; `.gitattributes` tiene prioridad sobre ese ajuste).

## D-25 Paquete base `com.daw.movies` — ✅ Decidido por el alumno (fase 6)
- Se usará como `groupId` (`com.daw`) + `artifactId` (`movies`) en el paso 6.2, y como raíz de las carpetas de `src/main/java`.
- Repositorio remoto: `git@github.com:fernandomguez/daw-movies.git`.

## D-26 Cada dependencia se añade en la fase que la necesita — ✅ Decidido (fase 6)
- **Alternativas:** (A) marcar todas las dependencias en Initializr desde el principio; (B) empezar con `spring-boot-starter` + `spring-boot-starter-test` y añadir cada una en su fase.
- **Decisión:** B.
- **Motivo:** Spring Boot se autoconfigura según las dependencias presentes: JPA sin base de datos configurada impide arrancar, y Spring Web levanta un servidor que aún no usamos. Añadirlas una a una permite ver qué aporta cada una. Lista completa y nombres reales de Boot 4.1.1: [temario 06, «Referencia: plan de dependencias»](temario/06-proyecto-spring-boot-y-git.md#referencia-plan-de-dependencias).

## D-27 Dependencias descartadas — ✅ Decidido (fase 6, revisable)
| Dependencia | Motivo |
|---|---|
| Lombok | Oculta el código que queremos aprender; para DTOs usaremos `record` |
| DevTools | Comodidad, no aprendizaje; se puede añadir si molesta reiniciar a mano |
| Docker Compose Support | Configuraría la conexión sola, justo lo que se aprende en la fase 7 |
| H2 | Su SQL no es el de MariaDB; los tests de integración usarán MariaDB real (Testcontainers, fase 19) |
| Actuator, REST Docs / OpenAPI | Fuera del alcance actual |

## D-28 Proyecto generado con Initializr y revisado antes de copiarlo — ✅ Decidido (fase 6)
- Se generó en una carpeta temporal y se copiaron `pom.xml`, Maven Wrapper y `src/`. No se copiaron `HELP.md`, `.gitignore` ni `.gitattributes` (se fusionaron con los nuestros). Se quitaron del `pom.xml` las etiquetas vacías (`<url/>`, `<licenses>`, `<developers>`, `<scm>`).
- Se usa el **Maven Wrapper** (`mvnw.cmd`, Maven 3.10.0): no hace falta instalar Maven y todos usan la misma versión.

## D-29 Anotaciones JPA en las clases del dominio (sin entidad separada) — ✅ Decidido por el alumno (2026-10-20)
- **Contexto:** el diseño de la fase 5 separaba `Movie` (dominio en Java puro), `MovieEntity` (JPA) y `MovieResponse` (DTO), con dos mappers.
- **Alternativas:** (A) dominio en Java puro + entidad JPA separada; (B) anotaciones `jakarta.persistence` (`@Entity`, `@Id`, `@ManyToOne`...) en `Movie`, `Director` y `Actor`; (C) además, Bean Validation (`jakarta.validation`) en el dominio.
- **Decisión:** B. Bean Validation queda en los DTOs de entrada (fase 18): en el dominio no valida nada por sí sola, y las reglas se comprueban en el constructor.
- **Motivo:** menos código (desaparecen `MovieEntity` y `MovieEntityMapper`), estructura parecida a la de clase y el dominio sigue sin depender de Spring Data. JPA es una especificación estándar (Jakarta EE), no Spring.
- **Coste aceptado:** JPA impone condiciones a las clases del dominio (constructor sin argumentos, campos no `final`) y la clase de negocio refleja la tabla; un cambio en la tabla puede obligar a tocarla. Ya no hay frontera "entidad ↔ dominio".
- **Se mantiene:** el puerto `MovieRepository` y su adaptador (D-20), que ahora solo delega en Spring Data sin mapear; los DTOs y el mapper dominio ↔ DTO (la API no expone las entidades).
- **Impacto en el plan:** fase 10 diseña las clases pensando en el negocio; fase 11 les añade las anotaciones; **la fase 13 (mapeos persistencia ↔ dominio) se elimina**.
- **Documentación:** [temario/05](temario/05-arquitectura-tres-capas-dip.md) describe el diseño estricto de la fase 5 (sigue siendo válido como referencia) y, en la sección "Cambio de diseño", lo que cambia con esta decisión.

## D-30 Un commit y una etiqueta por fase — ✅ Decidido por el alumno (2026-10-20)
- **Objetivo:** poder recorrer el proyecto fase a fase (`git checkout fase-07`) y ver cómo crece.
- **Forma de trabajo:** sin commits intermedios. Al cerrar cada fase se actualizan los documentos, se pregunta al alumno y se hace **un único commit** con el mensaje `tipo(fase-NN): ...`, una **etiqueta anotada** `fase-NN` y el push (`git push` + `git push origin fase-NN`).
- **Historial anterior:** los 5 commits hechos durante la fase 6 se **reescribieron**. Las fases 1–5 se hicieron antes de que existiera Git, así que quedan en un único commit (etiqueta `fase-05`), reconstruido a partir del primer commit sin lo que añadió el paso 6.1. La fase 6 irá en un solo commit al terminar el paso 6.5. Hizo falta `git push --force-with-lease`. Antes se guardó una copia en la rama local `backup/historial-original`.
- **Alternativa descartada:** rama por fase con commits pequeños y `git merge --squash` al cerrarla. Más segura en fases largas, pero innecesaria con fases cortas.


## D-31 Aviso de Mockito "self-attaching" con Java 25 — ⏳ Pendiente (cuando usemos Mockito)
- **Contexto:** al ejecutar los tests aparece `Mockito is currently self-attaching ... This will no longer work in future releases of the JDK` y `Dynamic loading of agents will be disallowed by default in a future release`.
- **Alternativas:** (A) dejarlo por ahora: el test pasa y no usamos Mockito todavía; (B) configurar Mockito como agente Java en el `pom.xml` (plugin de Surefire con `-javaagent`), como indica la documentación de Mockito.
- **Decisión provisional:** A. Se revisará en la primera fase que use Mockito, o antes si una versión de Java lo convierte en error.


## D-32 Spring lee las credenciales de `.env` con `spring.config.import` — ✅ Decidido (fase 7)
- **Contexto:** las credenciales de MariaDB están en `.env` (no versionado, D-17). Docker Compose lo lee solo; Spring Boot no.
- **Alternativas:** (A) `spring.config.import: optional:file:.env[.properties]` en `application.yaml`; (B) exportar las variables a mano en cada terminal y en la configuración del IDE; (C) una librería *dotenv*.
- **Elección:** A. Un único archivo de secretos para Docker y para Spring; funciona igual con `mvnw`, con el IDE y en los tests; no añade dependencias. Una variable de entorno real del sistema tiene prioridad sobre `.env`, así que en otro entorno basta con definir las variables.
- **Cuidado:** `.env` se lee con formato *properties*. Si un valor se escribe entre comillas, las comillas formarán parte del valor. La ruta es relativa a la carpeta desde la que se arranca la aplicación (la raíz del proyecto).

## D-33 Hibernate escribe la contraseña en el log de arranque — ✅ Decidido por el alumno: A (fase 7)
- **Contexto:** al conectar, Hibernate escribe con nivel `INFO` (logger `org.hibernate.orm.connections.pooling`, mensaje `HHH10001005: Database info`) la URL que le devuelve el driver de MariaDB, y esa URL incluye `user=...&password=...` en claro. También queda en `target/surefire-reports/*.xml` (ignorado por Git).
- **Alternativas:** (A) subir ese logger a `WARN` en `application.yaml`: desaparece todo el bloque "Database info" (también la versión y el dialecto, que son útiles); (B) dejarlo: es una contraseña local de desarrollo, pero se filtraría al copiar logs en un foro, un chat o un CI.
- **Decisión:** A. En `application.yaml`: `logging.level.org.hibernate.orm.connections.pooling: WARN`. Comprobado: 0 líneas con `password=` en la salida y en `target/surefire-reports`; la conexión se sigue viendo en `HikariPool-1 - Added connection`. Se pierde el bloque `Database info` (versión y dialecto); si hace falta verlo, se baja el nivel temporalmente y no se comparte ese log.


## D-34 `spring.jpa.hibernate.ddl-auto: validate` — ✅ Decidido (fase 7)
- **Contexto:** Hibernate puede crear o cambiar tablas a partir de las entidades. El esquema lo gestionará Flyway (fase 8), con migraciones SQL revisadas y versionadas.
- **Alternativas:** `create`/`create-drop` (borran los datos al arrancar); `update` (cambia tablas sin que nadie lo revise y nunca borra columnas); `none` (no hace nada; es lo que Spring Boot aplica sin decirlo para bases de datos que no están en memoria); `validate`.
- **Elección:** `validate`. No modifica nada y, desde la fase 11, la aplicación no arrancará si una entidad no encaja con su tabla (columna o tabla que falta, tipo incompatible). Así un error de mapeo aparece al arrancar y no en mitad de una petición.
- **Comprobado con test:** `DatabaseConnectionTests.hibernateOnlyValidatesTheSchema` (rojo antes de configurarlo: la propiedad `hibernate.hbm2ddl.auto` no existía).

## D-35 Los tests necesitan MariaDB arrancado — ✅ Aceptado (fase 7, provisional hasta la fase 19)
- **Contexto:** desde la fase 7, `@SpringBootTest` se conecta a la base de datos al arrancar. Si Docker o el contenedor están apagados, todos los tests fallan (`Connection refused`).
- **Elección provisional:** ejecutar `docker compose up -d` antes de `.\mvnw.cmd test`. Está anotado en `DatabaseConnectionTests` y en el README.
- **Más adelante:** en la fase 19, Testcontainers arrancará un MariaDB desechable para los tests, sin depender del contenedor de desarrollo.

## D-36 Endpoints de la versión 1 y forma de las respuestas — ✅ Decidido por el alumno (fase 8, paso 8.2)
- **Contexto:** el profesor recomendó pensar los endpoints antes de empezar la aplicación. Se diseñaron desde las pantallas: inicio con todas las películas → ficha de la película → ficha del director con sus películas.
- **Endpoints:** `GET /movies`, `GET /movies/{id}`, `GET /directors/{id}` y `GET /directors/{id}/movies`. Solo lectura; la escritura se diseñará en la fase 17.
- **Ficha del director, alternativas:** (A) un solo endpoint con las películas dentro; (B) director y películas en endpoints separados. **Elección: B.** Cada endpoint hace una cosa, y la lista de películas tiene la misma forma que `GET /movies`.
- **Forma de las respuestas:** las listas devuelven `id`, `title` y `releaseYear`; la ficha de la película añade `durationMinutes`, `director` y `actors`. El director y los actores dentro de otra respuesta van como `{ id, fullName }`; en su propia ficha, el director lleva `firstName` y `lastName` por separado.
- **Por qué los actores llevan `id` aunque solo se muestre el nombre:** la respuesta es un contrato. Si más adelante se quieren enlaces a los actores, la forma no cambia y no se rompe ningún frontend.
- **Errores:** `404` si el recurso no existe y `400` si el `id` no es un número, con cuerpo *Problem Details*. Se programará en la fase 18.
- Detalle en [08-flyway-migraciones.md, paso 8.2](08-flyway-migraciones.md#82-diseño-de-los-endpoints-y-revisión-del-esquema) y [temario/08-diseno-de-endpoints.md](temario/08-diseno-de-endpoints.md).

## D-37 Biografía opcional (`TEXT NULL`) en `director` y `actor` — ✅ Decidido por el alumno (fase 8, paso 8.2)
- **Contexto:** al comprobar los endpoints contra el esquema de la fase 3, la ficha del director pedía una biografía que no tenía columna.
- **Tipo, alternativas:** `VARCHAR(n)` (con un máximo de caracteres) o `TEXT` (texto largo). **Elección: `TEXT`**: una biografía puede ocupar varios párrafos.
- **Obligatoriedad:** **opcional** (`NULL`). Se puede dar de alta un director sin tener aún su biografía. Es la primera excepción a "todas las columnas `NOT NULL`" de la fase 3.
- **También en `actor`:** el alumno quiere biografías de actores en el futuro, pero no ahora. Se añade ya la columna (vacía) porque las tablas aún no existen: añadirla después costaría una migración nueva.
- **Solo se devuelve en la ficha** (`GET /directors/{id}`), no en las listas.
- **Datos de prueba:** biografías cortas de los 5 directores en [02-alcance-del-dominio.md](02-alcance-del-dominio.md).

## D-38 Orden por defecto de las listas — ✅ Decidido por el alumno (fase 8, paso 8.2)
- **Contexto:** una base de datos no garantiza el orden de las filas si no se le pide (`ORDER BY`). Sin decidirlo, la misma petición podría devolver otro orden cada vez.
- **Decisión:**

| Lista | Orden |
|---|---|
| `GET /movies` | Alfabético por título |
| `GET /directors/{id}/movies` | Por año, del más antiguo al más reciente; si coincide el año, por título |
| Actores de una película | Alfabético por apellido y, después, por nombre |

- **Orden de reparto (aplazado):** el alumno quiere más adelante ordenar los actores por su orden en los créditos. Hoy no se tiene el dato. Cambiarlo después no rompe el contrato de la API (la forma de la respuesta es la misma). Hará falta una columna nueva en `movie_actor`, con una migración `V5`; encaja con la fase opcional 23 (personaje en `movie_actor`).
- **Ordenaciones elegidas por el usuario** (`?sort=...`): las pedirá el frontend y las hará el back. Se dejan para más adelante.

## D-39 Una lista vacía es `200 []`, no un error — ✅ Decidido (fase 8, paso 8.2)
- **Contexto:** el alumno preguntó si, cuando un director no tiene películas, se podía lanzar una excepción con el mensaje "este director no tiene películas".
- **Alternativas:** (A) `404` o una excepción propia con el mensaje; (B) `200` con `[]`, y que el frontend muestre el aviso.
- **Elección: B.** La petición no ha fallado: la respuesta correcta es "ninguna". Con `404` en los dos casos, el frontend no podría distinguir "el director no existe" (`404`) de "existe, pero no tiene películas" (`200 []`).
- **Regla:** el back devuelve datos y códigos; el front decide cómo contárselo al usuario (texto, idioma, diseño).
- **Consecuencia en la fase 16:** `GET /directors/{id}/movies` primero comprueba que el director existe y después busca sus películas.

## D-40 Versión de Flyway: la que gestiona Spring Boot (12.4.0), no la última (13.11.1) — ✅ Decidida (fase 8, paso 8.3)
- **Contexto:** el alumno preguntó por la última versión de Flyway (13.11.1). Al añadir las dependencias sin versión, Spring Boot 4.1.1 resolvió `flyway-core` y `flyway-mysql` **12.4.0**.
- **Alternativas:** (A) dejar la versión gestionada por el *parent*; (B) forzar la última con `<flyway.version>13.11.1</flyway.version>`.
- **Elección: A**, confirmada por el alumno. El `spring-boot-starter-flyway` es código del propio Spring Boot: está compilado y probado contra la versión que su *parent* declara. Forzar una mayor puede romper la autoconfiguración sin avisar, y no aporta nada en este proyecto (no usamos funciones nuevas de Flyway).
- **Cuándo se revisaría:** si una versión nueva corrige un fallo que nos afecte, o si Spring Boot actualiza su versión gestionada (`.\mvnw.cmd dependency:tree "-Dincludes=org.flywaydb:*"` lo dice en un segundo).
- **Dato real:** `[INFO] \- org.flywaydb:flyway-mysql:jar:12.4.0:compile` (2026-10-28).

## D-41 Nombre de la clave primaria: `CONSTRAINT pk_... PRIMARY KEY` (se conserva aunque MariaDB lo ignore) — ✅ Decidida (fase 8, paso 8.4)
- **Contexto:** al aplicar la `V1`, MariaDB avisó: `Name 'pk_director' ignored for PRIMARY key. (SQL State: - Error Code: 1280)`. En MySQL/MariaDB la clave primaria se llama siempre `PRIMARY`: no admite nombre propio. Las claves foráneas y los `CHECK` **sí** admiten nombre y ese nombre sale en los mensajes de error (temario 03, sección 12).
- **Alternativas:** (A) conservar `CONSTRAINT pk_director PRIMARY KEY (id)` en las cuatro migraciones; (B) escribir `PRIMARY KEY (id)` sin nombre.
- **Elección: A**, confirmada por el alumno. El aviso sale **una sola vez** (al aplicar la migración) y no rompe nada; a cambio, el SQL queda autodocumentado y es portable (PostgreSQL sí respeta el nombre).
- **Si se elige B:** hay que corregir el borrador de la [fase 3](temario/03-diseno-esquema-relacional.md), la convención de nombres del `README.md` (que hoy dice `pk_movie`, `fk_movie_director`...) y explicar que en MariaDB la PK se llama `PRIMARY`.
- **Alcance:** afecta a `V1` (ya aplicada) y a las futuras `V2`, `V3` y `V4`. **Ventaja de decidirlo ahora:** la `V1` todavía **no está en Git**, así que se puede rehacer borrando la base de desarrollo y volviendo a arrancar (`docker compose down -v` + `up -d`), como permite el [temario 08, sección 4](temario/08-migraciones-flyway.md). Una vez comiteada, cambiarla exigiría una migración nueva.

## D-42 Los datos de prueba viven en `db/testdata/seed.sql`, fuera de Flyway — ✅ Decidida (fase 9, paso 9.1)
- **Contexto:** el esquema lo crea Flyway (fase 8), pero en la fase 9 hay que cargar 6 directores, 10 películas, 14 actores y 24 participaciones. ¿Dónde viven esos datos?
- **Alternativas:**
  - **(A)** Una migración `V100__insert_test_data.sql` en `db/testdata`, añadida a `spring.flyway.locations` solo con un perfil `dev`. Automático y versionado.
  - **(B)** Un `data.sql` con la inicialización de Spring (`spring.sql.init`). Sencillo, pero se ejecuta en cada arranque (o nunca), choca con el orden de Flyway y no es idempotente por defecto.
  - **(C)** Un script SQL normal en `db/testdata`, cargado a mano con un comando documentado.
- **Elección: C**, confirmada con la ejecución real. Los datos ficticios son **contenido recargable**, no estructura.
- **Por qué se descartó A (probado antes de decidir):** el historial de `flyway_schema_history` es único. Al aplicar la semilla como migración, la base quedó en la versión 100 y, al ejecutar los tests sin el perfil `dev`, salió:
  ```
  Current version of schema `movies`: 100
  WARN : Schema `movies` has a version (100) that is newer than the latest available migration (4) !
  ```
  Peor que el aviso: la siguiente migración de esquema (`V5`) habría quedado **fuera de orden** y Flyway no la aplicaría sin activar `outOfOrder`. Además, dos programadores tendrían historiales distintos según hubieran cargado o no los datos. Se deshizo el experimento (filas borradas, fila `version = 100` eliminada del historial y `AUTO_INCREMENT` reiniciados).
- **Cómo se carga:**
  ```powershell
  docker compose cp src/main/resources/db/testdata/seed.sql mariadb:/tmp/seed.sql
  docker compose exec -T mariadb sh -c 'mariadb -u"$MARIADB_USER" -p"$MARIADB_PASSWORD" movies < /tmp/seed.sql'
  ```
  `docker compose cp` evita que PowerShell reescriba el archivo y rompa las tildes (el problema del paso 8.6).
- **Consecuencia:** no es automático; hay que cargarlo tras un `docker compose down -v`. La receta de recarga (borrar en orden inverso y reiniciar los `AUTO_INCREMENT`) está en el [diario 09](09-datos-de-prueba.md#cómo-se-recarga-desde-cero).
- **Por qué no molesta a Flyway:** `db/testdata` no está en `spring.flyway.locations`, así que Flyway no lo ve. Los tests siguen validando 4 migraciones.
- **Ventaja para la fase 19:** el mismo archivo podrá cargarse en el MariaDB desechable de Testcontainers (`withInitScript`).

## D-43 Sexto director sin películas (Sofia Coppola) para practicar `LEFT JOIN` — ✅ Decidida (fase 9)
- **Contexto:** la [fase 2](02-alcance-del-dominio.md) dejó anotado que no había ningún director sin películas y que se añadiría en la fase 9 para practicar `LEFT JOIN`. Sin ese caso, un `INNER` y un `LEFT` devuelven lo mismo y la diferencia no se ve.
- **Decisión:** añadir a **Sofia Coppola** (1971-05-14) a la semilla, con biografía, y **sin** películas. Los datos de la fase 2 (5 directores con películas) no cambian: es un caso didáctico adicional.
- **Motivo:** permite comprobar en la misma consulta la diferencia entre `COUNT(*)` y `COUNT(m.id)` y el comportamiento de `AVG` con `NULL`. Es un dato real y verificable, como el resto.
- **Consecuencia:** los recuentos de la semilla son **6 directores**, 10 películas, 14 actores y 24 participaciones. El director 6 aparece en las consultas con `LEFT JOIN` y desaparece con `INNER JOIN`.

## D-44 Modelo de dominio rico (moderado), sin setters — ✅ Decidida por el alumno (fase 10, paso 10.1) · **ampliada por D-46**
- **Contexto:** hay que decidir dónde viven las reglas de `Movie`, `Director` y `Actor` antes de escribirlas (fase 10). El esquema ya las impone en la base (`NOT NULL`, `CHECK`, FK), pero un objeto Java puede violarlas si la clase lo permite.
- **Alternativas:**
  - **(A) Anémico:** campos + getters + setters; la validación se hace en el servicio o el controlador. Es lo más habitual en muchos proyectos.
  - **(B) Rico:** constructor que valida, **sin setters** y con lo derivado calculado (`fullName()`). Un objeto no puede existir inválido.
  - **(C) Rico con setters:** el constructor valida y cada setter vuelve a validar. Permite cambiar el estado sin crear otro objeto.
- **Elección: B**, confirmada por el alumno.
- **Motivos:** el dominio es pequeño (tres clases) y las reglas ya están claras; concentrarlas en el constructor evita que cada capa las repita y que un objeto inválido circule hasta la base. Sin setters no hay forma de romper un invariante después de crear el objeto. La opción C multiplica los caminos de mutación para un beneficio que aún no necesitamos (la escritura llega en la fase 17).
- **Qué incluye:**
  - Constructor público que valida. **Desde D-46, la validación de cada campo vive en su *value object*** (`Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate`, `Biography`); el constructor de la entidad recibe tipos ya válidos y solo comprueba lo que cruza campos (por ejemplo, que el director no sea `null`).
  - Biografía opcional: `null` cuando no hay. `Biography` no admite texto en blanco; convertir un texto en blanco a `null` es normalización de entrada y se hace en el mapper (fase 17) y en el DTO (fase 18).
  - `fullName()` calculado (nombre + apellidos), delegando en `PersonName`; no es una columna.
  - Constructor sin argumentos **protegido** para JPA y campos **no `final`** (coste aceptado en D-29). Se añaden en el paso 10.4.
- **Qué NO incluye (a propósito):**
  - **Setters:** para cambiar un dato se crea otro objeto o se añade un método con nombre de negocio (fase 17).
  - **`equals`/`hashCode`:** la identidad real es el `id` de la base, que aún no existe al crear el objeto. Se decidirá en la fase 11/14 si JPA lo exige; añadirlos con `id == null` es la trampa clásica.
  - **Bean Validation** (`@NotBlank`, `@Positive`): D-29 la deja en los DTOs de entrada (fase 18).
  - **Servicios de dominio:** las reglas que necesitan varios objetos o la base llegan con el repositorio (fase 12) y el servicio (fase 16).
- **Consecuencia para la fase 11:** JPA exige un constructor sin argumentos y campos no `final`; por eso el constructor protegido se crea ya en el paso 10.4 aunque todavía no haya anotaciones.
- **Consecuencia para la fase 17:** el mapper del DTO de entrada llamará al constructor; no habrá setters que rellenar.
- **Documentación:** [diario 10](10-modelo-de-dominio.md#101-modelo-anémico-frente-a-modelo-rico) y [temario 10](temario/10-modelo-de-dominio.md).

## D-45 Monolito clásico en capas ahora; modularización planificada en la fase 25 — ✅ Decidida por el alumno (fase 10)
- **Contexto:** el profesor pide un **monolito** (no microservicios). Su web va más allá: el [tema 01](https://cesguiro.es/doku.php/clase/daw/dws/1eval/arquitecturas) toma como referencia «una arquitectura por capas desplegada como **monolito modular**» y el [tema 04](https://cesguiro.es/doku.php/clase/daw/dws/1eval/modulith) dedica un tema a módulos, ArchUnit y Spring Modulith.
- **Decisión:** ahora el proyecto es un **monolito clásico en capas**: un `.jar` de Spring Boot, un módulo Maven, una base de datos y paquetes por **capa técnica** con DIP (D-18). La **modularización no es un objetivo**: se hará **solo si la aplicación lo requiere** (cuando exista un segundo módulo de negocio real). La **fase 25** documenta el camino y las condiciones.
- **Por qué no modular ya:** con tres entidades y una relación principal, partir en módulos ahora añade estructura sin nada que aislar; primero se consolidan capas, dominio y persistencia, que es donde está el aprendizaje pendiente.
- **Por qué queda escrita aunque no se haga:** la web del profesor toma el monolito modular como referencia y conviene tener el camino preparado y las condiciones escritas. Si al final la aplicación no lo requiere, no se hace: **no es un objetivo del proyecto**.
- **Coste:** mover paquetes e imports (mecánico con el IDE) y añadir ArchUnit/Spring Modulith. Es barato **si no se rompen las reglas por el camino**: el dominio no importa web ni persistencia, el controlador no usa repositorios, los DTO no exponen entidades (D-18, D-20, D-29).
- **Riesgo identificado:** con D-29, `Movie` referencia a `Director`. Su tema 05 dice que las entidades de un módulo no referencian entidades de otro. Con **un** módulo (`catalog`) no hay conflicto; con **dos** (`movies` y `people`) habría que pasar a relaciones por identificador. Se decide en el paso 25.1.
- **Consecuencia:** no se añade Spring Modulith ni ArchUnit al `pom.xml` hasta la fase 25. La revisión de la fase 20.1 sigue siendo manual.
- **Documentación:** [temario 10](temario/10-modelo-de-dominio.md#monolito-normal-o-monolito-modular) y [plan, Bloque F](00-plan-de-aprendizaje.md#bloque-f--temas-del-temario-del-profesor-que-faltaban).

## D-46 *Value objects* en el dominio — ✅ Decidida por el alumno (fase 10, 2026-10-28)
- **Contexto:** D-44 dejó fuera los *value objects*. El profesor los pide expresamente ([tema 03](https://cesguiro.es/doku.php/clase/daw/dws/1eval/validacion)) y el proyecto no es una práctica evaluada, sino un ejercicio para aplicar lo que se da en clase.
- **Decisión:** el dominio usa **tipos propios** en lugar de primitivos: `Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate` y `Biography`. Son `record` inmutables, comparados por valor, con **constructor compacto** que valida.
- **Por qué:** *primitive obsession*: un `String` puede ser cualquier cosa y la regla queda dispersa. Con un tipo propio, el objeto inválido **no existe** por ninguna vía de creación (`new`, mapper, JPA, test). Las anotaciones de Jakarta Validation no dan esa garantía fuera de una petición HTTP.
- **Reglas (tomadas del esquema, no inventadas):**

  | *Value object* | Regla | Columna |
  |---|---|---|
  | `Title` | no vacío, ≤ 255, `trim` | `movie.title VARCHAR(255)` |
  | `ReleaseYear` | ≥ 1888 | `movie.release_year` + `CHECK` |
  | `DurationMinutes` | > 0 | `movie.duration_minutes` + `CHECK` |
  | `PersonName` | nombre y apellidos no vacíos, ≤ 100; `fullName()` | `first_name`/`last_name VARCHAR(100)` |
  | `BirthDate` | no nula, no futura | `birth_date DATE` |
  | `Biography` | no vacía si existe; sin biografía = `null` | `biography TEXT NULL` (D-37) |

- **Diseño cerrado en el paso 10.2:**
  - `IllegalArgumentException` para todo valor inválido, también para `null`: un solo tipo predecible que la fase 18 traduce a `400`.
  - `trim()` antes de medir la longitud.
  - `ReleaseYear` y `DurationMinutes` sin tope superior: el esquema no lo tiene e inventarlo rechazaría datos válidos.
  - `BirthDate` no futura es la única regla que no está en el esquema; usa `LocalDate.now()`, así que los tests trabajan con fechas relativas.
  - Todos los tipos viven en `com.daw.movies.domain.model`, junto a las entidades.

- **Alternativa descartada:** anotar los campos del DTO y validar en el constructor de la entidad (lo que decía D-44). Se descarta porque el profesor pide *value objects* y porque el tipo propio protege también las vías que no pasan por HTTP.
- **Consecuencia para la fase 10:** el orden cambia. Primero los *value objects* (10.2, diseño, y 10.3, TDD) y después las entidades (10.4), que reciben tipos ya válidos.
- **Consecuencia para la fase 11:** JPA no guarda un `Title` tal cual. `PersonName` se mapeará con `@Embeddable` (dos columnas) y los de un solo valor con `AttributeConverter`; se decide y se prueba allí, con el soporte real de Hibernate 7.
- **Consecuencia para la fase 18:** la validación del DTO sigue existiendo, pero solo para dar un **400 con el campo culpable**; la garantía del dominio es el *value object*.
- **Documentación:** [temario 10](temario/10-modelo-de-dominio.md#los-value-objects-d-46) y [plan, fase 10](00-plan-de-aprendizaje.md#fase-10--modelo-de-dominio).

## D-47 API First: contrato OpenAPI versionado y Swagger UI con springdoc 3.2.0 — ✅ Decidida por el alumno (fase 15A adelantada, 2026-10-28)
- **Contexto:** el alumno pide ver la **interfaz de los endpoints** antes de la fase 16. El plan ya tenía la fase 15A (API First) entre la 15 y la 16, pero no había ni contrato en OpenAPI ni capa web. Las rutas y las respuestas ya estaban decididas desde la fase 8 (D-36 a D-39).
- **Decisión:**
  - El **contrato de la v1** se escribe a mano en `src/main/resources/static/openapi.yaml` (OpenAPI 3.0.3). Spring Boot sirve `static/` en la raíz, así que queda en `/openapi.yaml` sin código Java y viaja dentro del `.jar`.
  - **Swagger UI** lo muestra en `/swagger-ui.html`, con `springdoc-openapi-starter-webmvc-ui` **3.2.0** y `springdoc.swagger-ui.url=/openapi.yaml`.
  - Se añade `spring-boot-starter-webmvc` **antes** de la fase 16 porque springdoc no arranca sin Spring MVC.
- **Por qué el contrato es estático y no generado:** no hay controladores, así que `/v3/api-docs` estaría vacío. Además, en API First el contrato es la fuente de verdad, no el reflejo del código. En la fase 16 se activará el documento generado y el paso **15A.3** comprobará que no contradice el contrato.
- **Versión comprobada:** springdoc **3.2.0** es la rama para Spring Boot 4 (`<release>3.2.0</release>` en Maven Central, `lastUpdated 2026-10-24`; la web de springdoc indica soporte de Boot 4). Spring Boot **no** gestiona esta versión: se declara explícita.
- **Alternativa descartada:** servir Swagger UI desde un *webjar* con `swagger-ui-dist` y un `ResourceHandler` propio. Funciona, pero añade configuración manual y no deja el camino preparado para generar el documento desde el código en la fase 16.
- **Coste:** Tomcat arranca en el 8080 (antes la aplicación terminaba sola); el YAML hay que mantenerlo a mano hasta que el código lo genere; el contrato puede mentir si no se compara (15A.3).
- **Excepción a D-26:** la dependencia web se adelanta a la fase 16. Se anota como tal; la fase 16 sigue pendiente (controladores, DTO y servicio).
- **Consecuencia:** `/v3/api-docs` existe pero devuelve `paths: {}` hasta la fase 16. "Try it out" responde `404`: la UI documenta, no ejecuta.
- **Documentación:** [diario 15A](15A-api-first.md) y [temario 15A](temario/15A-api-first.md).
