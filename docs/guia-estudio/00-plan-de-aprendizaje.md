# 00 — Plan de aprendizaje (hoja de ruta)

> **Estado:** aprobado el 2026-10-19. Revisado el 2026-10-20: dominio con anotaciones JPA y `@Service` (D-19, D-29); la fase 13 se elimina.
> **Revisado el 2026-10-28** tras leer el temario del profesor ([DWS](https://cesguiro.es/doku.php/clase/daw/dws/start)): se añade la fase 15A (API First), el paso 18.5 (`ProblemDetail`), el 14.6 (`open-in-view`) y el Bloque F (fases 25–29). El proyecto es un **monolito clásico en capas** y pasará a modular en la **fase 25**: la modularización es requisito del curso, confirmado por el profesor el 2026-11-02 (D-45). La fase 10 adopta *value objects* (D-46).
> **Revisado el 2026-11-03:** el profesor da el visto bueno hasta la fase 10 y fija el orden de lo que sigue (ver «Orden de trabajo según el profesor», D-50). Se añaden las fases **11B** y **23A**; la 18 sube a 🔴; la 20 pasa después de la 25.
> **Regla de trabajo:** solo trabajamos en el siguiente paso pendiente. No se avanza sin confirmación explícita.

Leyenda: 🔴 imprescindible · 🟡 recomendable · ⚪ opcional

## Cambios respecto al orden propuesto inicialmente (y por qué)

1. **El diseño del esquema relacional (fase 3) va antes que Docker.** Diseñar tablas y relaciones es un ejercicio "de papel": no necesita ninguna herramienta. Si lo hacemos primero, cuando lleguemos a Docker y Flyway ya sabremos *qué* queremos crear y solo aprenderemos *cómo*.
2. **Docker y MariaDB (fase 4) van antes que el proyecto Java.** Así comprobamos que la base de datos funciona por sí sola, sin mezclar problemas de Java con problemas de base de datos.
3. **El modelo de dominio (fase 10) va antes que las entidades JPA (fase 11).** Si escribimos primero las entidades JPA, es fácil acabar diseñando el dominio "a imagen" de las tablas. Al revés, el dominio se diseña pensando en el negocio y después la persistencia se adapta a él (esa es la idea de la inversión de dependencias).
4. **Las pruebas no son solo una fase final.** `base-standards.md` exige TDD, así que las pruebas aparecen desde la fase 6 y en cada fase con lógica Java. La fase 19 se dedica a las pruebas de integración con base de datos real y a revisar la cobertura.

## Orden de trabajo según el profesor (2026-11-03)

El profesor ha dado el OK hasta la fase 10 y pide, **por este orden**, lo que viene después:

| Prioridad | Lo que pide | Fases del plan | Estado |
|---|---|---|---|
| 1 | Organizar el dominio (agregados), refinar los *value objects* y meterlos en carpetas distintas | **11B** (nueva) | ⏳ siguiente |
| 2 | Crear persistencia y controladores | 12, 14, 15, 15A.3, 16 | ⏳ |
| 3 | Validaciones con Jakarta (`@NotNull`…) | 18 (sube a 🔴 y va antes que la 17) | ⏳ |
| 4 | Usuarios y pedidos (de películas, no alquileres) | **23A** (nueva) | ⏳ |
| 5 | Migrar a monolito modular | 25.1–25.2 | ⏳ |
| 6 | Empezar con Spring Modulith | 25.3–25.4 | ⏳ |

**Qué cambia en la práctica**
- La fase 11 ya está hecha. Lo siguiente es la **11B**, no la 12.1.
- La 12 y la 16 (persistencia y controladores) no cambian de contenido, pero se escriben **después** de la 11B, ya sobre las carpetas nuevas.
- La Jakarta Validation se hace pronto (18.2, sobre las entidades y los parámetros de la ruta). La parte de `@Valid` en cuerpos de petición llegará con la 17 (escritura), que pasa a después.
- La 20 (revisión final) se hace **después de la 25**, para revisar ya la estructura modular.

**Fuera de su lista, y por tanto después** (siguen en el plan): 17, 19, 20, 21, 22, 23, 26–29 y la **24** (JWT). La 24 no aparece en la lista del profesor, pero sigue siendo requisito (D-45, confirmado el 2026-11-02) y necesita la tabla de usuarios de la 23A.

## Bloque A — Preparación

### Fase 1 🔴 Inspección inicial y herramientas
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 1.1 Leer `base-standards.md` | Normas del proyecto | Resumen de las normas |
| 1.2 Inspeccionar el repositorio | Estado de partida | Inventario de archivos |
| 1.3 Comprobar herramientas y versiones | Compatibilidad | Tabla de versiones verificadas |
| 1.4 Proponer alcance, índice y guía | Planificación | Este documento |

Depende de: nada.

### Fase 2 🔴 Alcance del dominio y datos de prueba
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 2.1 Identificar entidades | Entidad = "cosa" del negocio con identidad propia | Lista: película, director, actor |
| 2.2 Identificar atributos | Atributo, tipo de dato, obligatoriedad | Atributos mínimos de cada entidad |
| 2.3 Identificar relaciones en lenguaje natural | Cardinalidad (1:N, N:M) | Frases del tipo "un director dirige varias películas" |
| 2.4 Elegir el conjunto de datos de prueba | Datos coherentes y verificables | Lista aprobada de películas, directores y actores |

Depende de: 1.

### Fase 3 🔴 Diseño del esquema relacional
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 3.1 De entidad a tabla | Tabla, fila, columna, tipos SQL | Tablas `director`, `actor`, `movie` |
| 3.2 Claves primarias | PK, `AUTO_INCREMENT`, claves naturales vs. sustitutas | PK de cada tabla |
| 3.3 Relación uno a muchos | Clave foránea (FK) en el lado "muchos" | `movie.director_id` → `director.id` |
| 3.4 Relación muchos a muchos | Tabla intermedia, PK compuesta | Tabla `movie_actor` |
| 3.5 Restricciones | `NOT NULL`, `UNIQUE`, `CHECK`, `ON DELETE` | Reglas que garantiza la base de datos |
| 3.6 Diagrama entidad-relación | Representación visual | Diagrama en la guía |

Depende de: 2.

## Bloque B — Base de datos

### Fase 4 🔴 Docker, Docker Compose y MariaDB
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 4.1 Qué es Docker | Imagen, contenedor | Explicación + `docker run hello-world` |
| 4.2 Qué es Docker Compose | Servicios declarados en un archivo | `compose.yaml` con un servicio `mariadb` |
| 4.3 Credenciales fuera del repositorio | Variables de entorno, `.env`, `.env.example`, `.gitignore` | Credenciales no versionadas |
| 4.4 Volúmenes | Persistencia de datos | Los datos sobreviven a `docker compose down` |
| 4.5 Conectarse a MariaDB | Cliente `mariadb` dentro del contenedor | Consultas `SHOW DATABASES`, `SELECT VERSION()` |
| 4.6 Healthcheck | Saber cuándo la BD está lista | Contenedor `healthy` |

Depende de: 1 (Docker Desktop arrancado). Conceptualmente, de 3.

### Fase 5 🔴 Arquitectura en tres capas e inversión de dependencias
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 5.1 Las tres capas | Controlador, dominio, persistencia | Diagrama de responsabilidades |
| 5.2 Dirección de las dependencias | Acoplamiento | Por qué el dominio no debe conocer JPA |
| 5.3 Principio de inversión de dependencias (DIP) | Interfaces definidas por quien las usa | Ejemplo con el repositorio de películas |
| 5.4 Capas conceptuales vs. paquetes | Organización física | Estructura de paquetes propuesta |

Depende de: 3. (Teórica: no crea código todavía.)

### Fase 6 🔴 Creación del proyecto Spring Boot
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 6.1 Git y `.gitignore` | Control de versiones | Repositorio inicializado |
| 6.2 Generar el proyecto | Spring Initializr, `pom.xml`, groupId/artifactId | Proyecto Maven con Java 25 y Boot 4.1.1 |
| 6.3 Dependencias mínimas | Starters, BOM de Spring Boot | Solo lo que necesitamos ahora |
| 6.4 Estructura Maven | `src/main`, `src/test`, `resources` | Explicación de cada carpeta |
| 6.5 Primer arranque y primer test | Ciclo de vida, `mvn test` | Test que pasa |

Depende de: 5.

### Fase 7 🔴 Conexión entre Java y MariaDB
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 7.1 Driver JDBC y `DataSource` | Cómo Java habla con una BD | Dependencia `mariadb-java-client` |
| 7.2 `application.yaml` con variables de entorno | Configuración externa | URL, usuario y contraseña sin escribir secretos |
| 7.3 Hibernate no toca el esquema | `ddl-auto` | `none`/`validate`, nunca `update` |
| 7.4 Comprobar la conexión | Logs de arranque y un test | La aplicación arranca conectada (comprobado con `DatabaseConnectionTests`) |

Depende de: 4, 6.

### Fase 8 🔴 Flyway: migraciones del esquema
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 8.1 Qué es una migración | Versionar el esquema como el código | Explicación + tabla `flyway_schema_history` |
| 8.2 Diseño de los endpoints y revisión del esquema | Pensar la API desde las pantallas antes de crear las tablas | Lista de endpoints de la v1; esquema revisado (`biography`) (D-36 a D-39) |
| 8.3 Configurar Flyway | Dependencias Flyway + módulo MariaDB/MySQL | Flyway se ejecuta al arrancar |
| 8.4 Primera migración | `V1__create_director_table.sql` | Tabla creada |
| 8.5 Migraciones con FK | Orden de creación | `movie`, `actor`, `movie_actor` |
| 8.6 Inmutabilidad de migraciones | Checksum, por qué no se edita una migración aplicada | Ejemplo de error controlado |

Depende de: 3, 7.

### Fase 9 🔴 Datos de prueba y comprobación de relaciones
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 9.1 Esquema vs. datos de prueba | Dónde vive cada cosa | Carpeta separada para los datos ficticios |
| 9.2 `INSERT` y orden por claves foráneas | Integridad referencial | Directores → películas → actores → participaciones |
| 9.3 Provocar un error de FK a propósito | Qué protege la BD | Entender el mensaje de error |
| 9.4 Consultas con `JOIN` | Recuperar datos relacionados | Película + director; película + actores |
| 9.5 `LEFT JOIN` y agregados | Casos sin relación, `COUNT`, `GROUP BY` | Películas por director, actores por película |

Depende de: 8.

## Bloque C — Java y persistencia

### Fase 10 🔴 Modelo de dominio
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 10.1 Modelo anémico vs. rico | Datos frente a datos + comportamiento | Elección justificada para nuestro problema |
| 10.2 *Value objects*: qué son y cuáles necesitamos | Tipos propios frente a primitivos (*primitive obsession*); reglas alineadas con el esquema (D-46) | Diseño de `Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate` y `Biography` |
| 10.3 *Value objects* con TDD | Test en rojo primero; constructor compacto que valida | Los seis `record` con sus tests |
| 10.4 Entidades y relaciones | `Director`, `Actor` y `Movie` con los *value objects*, getters y constructor protegido para JPA (D-29); relaciones unidireccionales | Clases de dominio |

Depende de: 5, 3.

### Fase 11 🔴 Fundamentos de JPA y entidades
Punto de partida: [temario/11-introduccion-jpa.md](temario/11-introduccion-jpa.md).

| Paso | Concepto | Resultado esperado |
|---|---|---|
| 11.1 Qué es JPA y qué es Hibernate | ORM, especificación vs. implementación | Explicación |
| 11.2 JPA en las clases del dominio (D-29) | Qué impone JPA (constructor vacío, campos no `final`) y qué ganamos (sin entidad aparte ni mapper) | Justificación; comparación con la entidad separada |
| 11.3 Entidades simples | `@Entity`, `@Table`, `@Id`, `@Column` | `Director` y `Actor` anotadas |
| 11.4 `@ManyToOne` | Relación 1:N desde el lado "muchos" | `Movie.director` |
| 11.5 `@ManyToMany` + `@JoinTable` | Tabla intermedia en JPA | `Movie.actors` |
| 11.6 `FetchType` | Carga perezosa (`LAZY`) vs. ansiosa (`EAGER`) | Elección justificada |

Depende de: 8, 10.

### Fase 11B 🔴 Organizar el dominio: agregados, *value objects* y carpetas
**Prioridad 1 del profesor (2026-11-03).** Se inserta entre la 11 y la 12 sin renumerar. No añade tecnología nueva: ordena lo que ya hay antes de escribir repositorios y controladores.

| Paso | Concepto | Resultado esperado |
|---|---|---|
| 11B.1 Agregados | Qué es un agregado y su raíz; qué objetos viven juntos y cuáles se relacionan solo por identificador | Decisión documentada: a qué agregado pertenecen `Movie`, `Director` y `Actor`, y cómo encajarán `Usuario` y `Pedido` (23A) |
| 11B.2 Refinar los *value objects* | Revisar los seis de D-46: reglas, mensajes y cuáles son de verdad un concepto propio | Lista de cambios aprobada; tests primero (TDD) |
| 11B.3 Carpetas | Estructura de paquetes que refleje los agregados y las capas. Punto de partida: [docs/arquitectura.md](../arquitectura.md), el esquema aportado por el alumno | Estructura aprobada, **sin mover nada todavía** |
| 11B.4 Mover | Reorganizar los paquetes de una capa cada vez, con los tests en verde | Código en las carpetas nuevas |

Depende de: 11.

### Fase 12 🔴 Repositorios y adaptadores de persistencia
**Prioridad 2 del profesor** (persistencia y controladores), junto con 14, 15, 15A.3 y 16.
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 12.1 DAO frente a Repositorio | Acceso a datos pensado desde la tabla vs. desde el dominio | Elección justificada (avance en [temario/05](temario/05-arquitectura-tres-capas-dip.md), sección 10) |
| 12.2 Interfaz de repositorio en el dominio (puerto) | DIP aplicado | `MovieRepository` sin dependencias de Spring Data |
| 12.3 Repositorio Spring Data JPA | `JpaRepository` | `MovieJpaRepository extends JpaRepository<Movie, Long>` en persistencia |
| 12.4 Adaptador | Implementa el puerto delegando en Spring Data (sin mapper, D-29) | `MovieRepositoryAdapter` |
| 12.5 Inyección de dependencias | `@Service`, `@Component` e inyección por constructor (D-19) | El servicio recibe la implementación sin conocerla |

Depende de: 11B (antes, 11).

### ~~Fase 13 Mapeos entre persistencia y dominio~~ — Eliminada (D-29, 2026-10-20)
Con las anotaciones JPA en las clases del dominio ya no existen `MovieEntity` ni `MovieEntityMapper`, así que no hay nada que mapear entre persistencia y dominio. Los conceptos de mapeo (campos copiados, calculados y omitidos) se estudian en la fase 15 con el mapper dominio → DTO. La comparación con la entidad separada se ve en el paso 11.2.

### Fase 14 🔴 Consultas de información relacionada y problemas habituales
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 14.1 Ver el SQL que genera Hibernate | Logs de SQL | Consultas visibles |
| 14.2 Problema N+1 | Consultas excesivas | Detectarlo en los logs |
| 14.3 Soluciones | `JOIN FETCH`, `@EntityGraph` | Una sola consulta |
| 14.4 `LazyInitializationException` | Sesión cerrada | Entender el error |
| 14.5 Referencias circulares | Relaciones bidireccionales y serialización | Por qué evitamos la bidireccionalidad innecesaria |
| 14.6 `open-in-view` | Sesión abierta durante toda la petición (por defecto) | `spring.jpa.open-in-view=false` y por qué (recomendación del profesor, [tema 05](https://cesguiro.es/doku.php/clase/daw/dws/1eval/jpa)) |

Depende de: 12.

## Bloque D — API como herramienta de aprendizaje

### Fase 15 🔴 DTOs de salida y mapeo dominio → DTO
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 15.1 Qué es un DTO y cuándo compensa | Ventajas y coste | Decisión documentada |
| 15.2 DTO de salida con `record` | Inmutabilidad | `MovieResponse` |
| 15.3 Mapper dominio → DTO | Campos copiados, calculados (p. ej., nombre completo) y omitidos | Recorrido completo MariaDB → `Movie` (dominio con JPA) → DTO |

Depende de: 12.

### Fase 15A 🟡 API First: el contrato antes del código
Se inserta entre la 15 y la 16 sin renumerar el resto, para no invalidar las referencias a las fases 16–24.
**Adelantada el 2026-10-28** (15A.1 y 15A.2) a petición del alumno, para poder ver la interfaz de los endpoints: [diario 15A](15A-api-first.md). No depende de la fase 15: las rutas ya estaban decididas en D-36.

| Paso | Concepto | Resultado esperado |
|---|---|---|
| 15A.1 El contrato de la v1 ✅ | Rutas, DTO, códigos y errores escritos antes de implementar ([tema 02](https://cesguiro.es/doku.php/clase/daw/dws/1eval/api_first)) | `src/main/resources/static/openapi.yaml`, servido en `/openapi.yaml` |
| 15A.2 OpenAPI y Swagger UI ✅ | `springdoc-openapi` **3.2.0** + Swagger UI; la UI carga el contrato estático | `http://localhost:8080/swagger-ui.html` |
| 15A.3 Contrato frente a implementación ⏳ | Que el contrato no mienta: lo declarado es lo que responde | Comparar `/v3/api-docs` (generado) con el contrato, en la fase 16 |

Depende de: D-36 a D-39 (ya decididos en la fase 8) y, para 15A.3, de la fase 16. Decisión: D-47.

### Fase 16 🔴 Servicio y controladores REST de lectura
**Prioridad 2 del profesor** (controladores), tras la persistencia de la 12.

| Paso | Concepto | Resultado esperado |
|---|---|---|
| 16.1 Servicio (caso de uso) | Quién coordina dominio y repositorio | `MovieService` |
| 16.2 Controlador | `@RestController`, `@GetMapping`, códigos HTTP | Los 4 endpoints de D-36: `GET /movies`, `GET /movies/{id}`, `GET /directors/{id}`, `GET /directors/{id}/movies` |
| 16.3 Probar desde fuera | `curl` o cliente HTTP | Respuestas JSON |

Depende de: 15.

### Fase 17 🟡 Operaciones de escritura y transacciones
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 17.1 DTO de entrada | Diferencia con el de salida | `CreateMovieRequest` |
| 17.2 Recorrido inverso | DTO → dominio → entidad → fila | `POST /movies` |
| 17.3 Transacciones | Atomicidad: todo o nada | `@Transactional` en el servicio |

Depende de: 16. **Pasa a después de la 18** (orden del profesor, 2026-11-03).

### Fase 18 🔴 Validaciones y manejo de excepciones
**Prioridad 3 del profesor** (validaciones con Jakarta). Sube de 🟡 a 🔴 y se hace antes que la 17. El 18.2 se empieza sobre las entidades y los parámetros de la ruta (no necesita la 17); el `@Valid` sobre cuerpos de petición llega con la 17.

| Paso | Concepto | Resultado esperado |
|---|---|---|
| 18.1 Tipos de errores | Validación de entrada, regla de dominio, restricción de BD, error inesperado | Tabla comparativa con ejemplos del proyecto |
| 18.2 Bean Validation | `@NotBlank`, `@Positive`, `@Valid` | Peticiones inválidas rechazadas |
| 18.3 Excepciones propias (solo las necesarias) | `MovieNotFoundException` | 404 cuando no existe |
| 18.4 `@RestControllerAdvice` | Gestión centralizada | Respuestas de error coherentes |
| 18.5 `ProblemDetail` (RFC 9457) | Formato de error estándar, el mismo en todas las operaciones ([tema 03](https://cesguiro.es/doku.php/clase/daw/dws/1eval/validacion)) | Errores con `type`, `title`, `status` y `errors` |

Depende de: 16 (la 17 ya no es previa; ver la nota de arriba).

## Bloque E — Calidad y cierre

### Fase 19 🔴 Pruebas automatizadas de integración y revisión de cobertura
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 19.1 Pirámide de pruebas | Unitarias vs. integración | Repaso de lo que ya tenemos |
| 19.2 Pruebas de persistencia | MariaDB real en contenedor (Testcontainers) | Tests del adaptador y las consultas |
| 19.3 Pruebas del controlador | Petición HTTP simulada | Tests de la API |

Depende de: 12 (y 16 para 19.3). Las pruebas unitarias se hacen desde la fase 6.

### Fase 20 🔴 Revisión de arquitectura y documentación final
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 20.1 Comprobar dependencias entre paquetes | El dominio solo importa JPA (`jakarta.persistence`) y anotaciones de Spring permitidas (`@Service`, `@Transactional`); nunca web, Spring Data, Jackson ni `persistence`/`controller` | Revisión hecha |
| 20.2 Revisar la guía | Coherencia con el código real | Guía actualizada |
| 20.3 README del proyecto | Cómo arrancarlo desde cero | Instrucciones verificadas |

Depende de: todas las anteriores que hayamos hecho. **Se hace después de la 25** (orden del profesor, 2026-11-03), para revisar ya la estructura modular.

### Fases opcionales y recomendables ⚪/🟡
- **21. Contenerizar la aplicación** ⚪ (`Dockerfile` + servicio `app` en Compose). Depende de: 20.
- **22. MapStruct** ⚪, solo si los mapeos manuales se vuelven repetitivos y lo justificamos. El profesor lo usa en el [tema 05](https://cesguiro.es/doku.php/clase/daw/dws/1eval/jpa); nosotros no separamos entidad y modelo (D-29), así que hoy no hace falta. Depende de: 15.
- **23. Ampliar la relación película–actor con datos propios** ⚪ (por ejemplo, el nombre del personaje). Convierte la tabla intermedia en una entidad con identidad. Depende de: 14.
- **24. Seguridad con Spring Security y JWT** 🔴. Login, token JWT que el cliente envía en cada petición y rutas protegidas (por ejemplo, solo usuarios autenticados pueden crear películas). Necesita una tabla de usuarios (migración Flyway) y contraseñas cifradas. **El profesor lo imparte en la 1ª evaluación** ([tema 06](https://cesguiro.es/doku.php/clase/daw/dws/1eval/jwt) y [tema 07](https://cesguiro.es/doku.php/clase/daw/dws/1eval/spring_security)) y **confirma que entra en este proyecto** (2026-11-02): los hitos 08–09 de su práctica son obligatorios, no opcionales. Depende de: 18.

### Fase 23A 🔴 Usuarios y pedidos de películas
**Prioridad 4 del profesor (2026-11-03).** Se inserta entre la 23 y la 24 sin renumerar. Se había hablado de «alquileres»; el profesor recomienda simplificar a **pedidos de películas**, sin fechas de devolución ni estados de alquiler. Sirve para tener algo más que el catálogo y poder partir el proyecto en módulos (fase 25). Los pasos son una propuesta que se concreta en el 23A.1.

| Paso | Concepto | Resultado esperado |
|---|---|---|
| 23A.1 Diseño | Qué es un pedido de películas, qué datos lleva y quién lo hace; `Usuario` y `Pedido` como agregados que se relacionan con el catálogo solo por identificador | Diseño documentado (sin código) |
| 23A.2 Esquema | Migraciones Flyway nuevas para usuarios y pedidos; una migración aplicada nunca se edita | Tablas nuevas verificadas |
| 23A.3 Dominio y persistencia | Entidades y repositorios, como en las fases 10–12 | Clases y tests (TDD) |
| 23A.4 API | Servicios y controladores de usuarios y pedidos | Endpoints por decidir en el 23A.1 |

Depende de: 11B, 16 y 18. La tabla de usuarios la reutilizará la fase 24 (JWT).

## Bloque F — Temas del temario del profesor que faltaban

Añadido el 2026-10-28 tras revisar [DWS](https://cesguiro.es/doku.php/clase/daw/dws/start) y [Programación](https://cesguiro.es/doku.php/clase/daw/prog/start). La fase 25 llega a la arquitectura de referencia de su [tema 04](https://cesguiro.es/doku.php/clase/daw/dws/1eval/modulith); las demás cubren temas que no estaban en el plan. **El 2026-11-02 el profesor confirmó** que la modularidad es requisito del curso (fase 25 🔴) y que JWT/Spring Security entran en el proyecto (fase 24 🔴, hitos 08–09 de su práctica).

### Fase 25 🔴 Monolito modular: paquetes por módulo, ArchUnit y Spring Modulith
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 25.1 De capas a módulos | Paquete por módulo con `api` e `internal`; raíz de composición `app`; decidir uno o dos módulos | Reorganización de paquetes |
| 25.2 API pública del módulo | Interfaz y DTO que cruzan el límite; lo interno no se expone | Contrato interno documentado |
| 25.3 ArchUnit | Sin ciclos, solo por la API pública, dominio sin frameworks | Tests de arquitectura |
| 25.4 Spring Modulith | `ApplicationModules.of(...).verify()` y documentación generada | Verificación automática |

**Prioridades 5 y 6 del profesor (2026-11-03):** 25.1–25.2 son «migrar a monolito modular» (5) y 25.3–25.4 son «empezar con Spring Modulith» (6).

Depende de: 23A (ya no de la 20, que se hace después). **Decisión pendiente en 25.1:** con D-29 las entidades se referencian entre sí. Con **un** módulo (`catalog` con películas, directores y actores) no hay conflicto; con **dos** (`movies` y `people`) las relaciones tendrían que pasar a identificadores, como indica su tema 05.

### Fase 26 ⚪ Eventos de aplicación y mensajería
Publicar un evento al crear una película y escucharlo con `@TransactionalEventListener`; después, la diferencia con un intermediario real (RabbitMQ/Kafka). Depende de: 17.

### Fase 27 ⚪ Logs e instrumentación
Logs estructurados con SLF4J, niveles, correlación de peticiones y métricas con Actuator. Depende de: 16.

### Fase 28 ⚪ Concurrencia: hilos de Tomcat y virtual threads
Hilos del contenedor, peticiones bloqueantes y virtual threads (Loom). Depende de: 16.

### Fase 29 ⚪ Serverless: lambda local
**No encaja en este proyecto**: nuestra API es un servidor HTTP permanente, no una función por evento. Se haría como ejercicio aparte con SAM o Docker, sin tocar el repositorio. Se anota para que la cobertura del temario quede completa, no para hacerlo aquí.

## Mapa de dependencias (resumen)

```
1 → 2 → 3 ─┬→ 4 ─┐
           └→ 5 → 6 → 7 → 8 → 9
                           │
               10 (necesita 3 y 5)
                           ↓
                 11 → 11B → 12 → 14
                                  ↓
                          15 → 15A → 16 → 18 (Jakarta) → 23A (usuarios y pedidos)
                                  │                              ↓
                                  │                       25 (modular + Modulith)
                                  │                              ↓
                                  │                       20 (revisión) · 24 (JWT)
                                  ├→ 17 (escritura), 19 (pruebas)
                                  └→ 26 (eventos), 27 (logs), 28 (concurrencia)
```

Las cajas de arriba siguen el **orden del profesor** (2026-11-03); lo que cuelga de la 16 sin estar en su lista (17, 19, 26–28) se hace después.

## Cobertura del temario del profesor (2026-10-28)

| Tema del profesor | Fase nuestra | Estado |
|---|---|---|
| DWS 01 Arquitecturas | 5, 25 | 🟡 capas hechas; modular en 25 |
| DWS 02 API First | 15A | 🟡 contrato y Swagger UI hechos (D-47); falta implementarlo (fase 16) y 15A.3 |
| DWS 03 Validación e invariancias | 10.2–10.3, 11B.2, 18.2, 18.5 | 🟡 *value objects* adoptados (D-46); se refinan en 11B.2 y se añade Jakarta en la 18 |
| DWS 04 Monolito modular y Modulith | 25 | 🔴 requisito confirmado por el profesor (2026-11-02) |
| DWS 05 Persistencia JPA | 11–14, 19 | 🟡 entidad = dominio (D-29), sin MapStruct |
| DWS 06 JWT y 07 Spring Security | 24 | 🔴 imprescindible: entra en el proyecto (confirmado 2026-11-02) |
| DWS 08 Eventos y mensajería | 26 | ⏳ añadida |
| DWS 09 Logs e instrumentación | 27 | ⏳ añadida |
| DWS 10 Serverless | 29 | ⚪ no encaja en esta API |
| DWS 11 Concurrencia | 28 | ⏳ añadida |
| Prog 08 Arquitectura por capas | 5 | ✅ |
| Prog 09 APIs REST y HTTP | 16 | ⏳ |
| Prog 10 Spring Boot | 6, 7 | ✅ |
| Prog 11 Hilos en Tomcat | 28 | ⏳ |
| Prog 12 Login con tokens | 24 | 🔴 |

**Nota sobre *value objects*:** el profesor los pide y se adoptan en la fase 10 (D-46): `Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate` y `Biography` son `record` con constructor compacto. El coste llega en la fase 11 (`AttributeConverter` y `@Embeddable`). La fase 11B los **refina** (no los quita): prioridad 1 del profesor, 2026-11-03.
