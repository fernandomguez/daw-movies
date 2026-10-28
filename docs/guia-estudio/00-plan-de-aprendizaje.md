# 00 — Plan de aprendizaje (hoja de ruta)

> **Estado:** aprobado el 2026-10-19. Revisado el 2026-10-20: dominio con anotaciones JPA y `@Service` (D-19, D-29); la fase 13 se elimina.
> **Regla de trabajo:** solo trabajamos en el siguiente paso pendiente. No se avanza sin confirmación explícita.

Leyenda: 🔴 imprescindible · 🟡 recomendable · ⚪ opcional

## Cambios respecto al orden propuesto inicialmente (y por qué)

1. **El diseño del esquema relacional (fase 3) va antes que Docker.** Diseñar tablas y relaciones es un ejercicio "de papel": no necesita ninguna herramienta. Si lo hacemos primero, cuando lleguemos a Docker y Flyway ya sabremos *qué* queremos crear y solo aprenderemos *cómo*.
2. **Docker y MariaDB (fase 4) van antes que el proyecto Java.** Así comprobamos que la base de datos funciona por sí sola, sin mezclar problemas de Java con problemas de base de datos.
3. **El modelo de dominio (fase 10) va antes que las entidades JPA (fase 11).** Si escribimos primero las entidades JPA, es fácil acabar diseñando el dominio "a imagen" de las tablas. Al revés, el dominio se diseña pensando en el negocio y después la persistencia se adapta a él (esa es la idea de la inversión de dependencias).
4. **Las pruebas no son solo una fase final.** `base-standards.md` exige TDD, así que las pruebas aparecen desde la fase 6 y en cada fase con lógica Java. La fase 19 se dedica a las pruebas de integración con base de datos real y a revisar la cobertura.

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
| 10.2 Clases de dominio en Java | Diseñarlas pensando en el negocio (las anotaciones JPA se añaden en la fase 11, D-29) | `Movie`, `Director`, `Actor` |
| 10.3 Representar relaciones en Java | Referencias y colecciones, dirección de la relación | `Movie` conoce a su `Director` y sus `Actor` |
| 10.4 Reglas del dominio con TDD | Test primero | Tests unitarios de las reglas que existan |

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

### Fase 12 🔴 Repositorios y adaptadores de persistencia
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 12.1 DAO frente a Repositorio | Acceso a datos pensado desde la tabla vs. desde el dominio | Elección justificada (avance en [temario/05](temario/05-arquitectura-tres-capas-dip.md), sección 10) |
| 12.2 Interfaz de repositorio en el dominio (puerto) | DIP aplicado | `MovieRepository` sin dependencias de Spring Data |
| 12.3 Repositorio Spring Data JPA | `JpaRepository` | `MovieJpaRepository extends JpaRepository<Movie, Long>` en persistencia |
| 12.4 Adaptador | Implementa el puerto delegando en Spring Data (sin mapper, D-29) | `MovieRepositoryAdapter` |
| 12.5 Inyección de dependencias | `@Service`, `@Component` e inyección por constructor (D-19) | El servicio recibe la implementación sin conocerla |

Depende de: 11.

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

Depende de: 12.

## Bloque D — API como herramienta de aprendizaje

### Fase 15 🔴 DTOs de salida y mapeo dominio → DTO
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 15.1 Qué es un DTO y cuándo compensa | Ventajas y coste | Decisión documentada |
| 15.2 DTO de salida con `record` | Inmutabilidad | `MovieResponse` |
| 15.3 Mapper dominio → DTO | Campos copiados, calculados (p. ej., nombre completo) y omitidos | Recorrido completo MariaDB → `Movie` (dominio con JPA) → DTO |

Depende de: 12.

### Fase 16 🔴 Servicio y controladores REST de lectura
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

Depende de: 16.

### Fase 18 🟡 Validaciones y manejo de excepciones
| Paso | Concepto | Resultado esperado |
|---|---|---|
| 18.1 Tipos de errores | Validación de entrada, regla de dominio, restricción de BD, error inesperado | Tabla comparativa con ejemplos del proyecto |
| 18.2 Bean Validation | `@NotBlank`, `@Positive`, `@Valid` | Peticiones inválidas rechazadas |
| 18.3 Excepciones propias (solo las necesarias) | `MovieNotFoundException` | 404 cuando no existe |
| 18.4 `@RestControllerAdvice` | Gestión centralizada | Respuestas de error coherentes |

Depende de: 16 (17 recomendable).

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

Depende de: todas las anteriores que hayamos hecho.

### Fases opcionales ⚪
- **21. Contenerizar la aplicación** (`Dockerfile` + servicio `app` en Compose). Depende de: 20.
- **22. MapStruct**, solo si los mapeos manuales se vuelven repetitivos y lo justificamos. Depende de: 15.
- **23. Ampliar la relación película–actor con datos propios** (por ejemplo, el nombre del personaje). Convierte la tabla intermedia en una entidad con identidad. Depende de: 14.
- **24. Seguridad con Spring Security y JWT.** Login, token JWT que el cliente envía en cada petición y rutas protegidas (por ejemplo, solo usuarios autenticados pueden crear películas). Necesita una tabla de usuarios (migración Flyway) y contraseñas cifradas. Depende de: 18.

## Mapa de dependencias (resumen)

```
1 → 2 → 3 ─┬→ 4 ─┐
           └→ 5 → 6 → 7 → 8 → 9
                           │
               10 (necesita 3 y 5)
                           ↓
                  11 → 12 → 14
                              ↓
                       15 → 16 → 17 → 18
                              ↓
                             19 → 20 → (21, 22, 23, 24 opcionales)
```
