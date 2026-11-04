# Arquitectura del proyecto (ejemplo con `Movie`)

> Describe la arquitectura **real** del código en `src/main/java/com/daw/movies`, no un ideal. Paquete raíz: `com.daw.movies`.
> Decisión que la respalda: [D-48](guia-estudio/decisiones-tecnicas.md#d-48-arquitectura-por-capas-con-dao-jdbc-y-dominio-con-tipos-simples) (capas con DAO JDBC) y [D-49](guia-estudio/decisiones-tecnicas.md#d-49-monolito-modular-módulo-catalog-con-spring-modulith) (el módulo `catalog` con Spring Modulith). Código en inglés; documentación en español.
>
> La arquitectura objetivo de futuro (JPA, fachadas, casos de uso) está en [arquitectura-monolito.md](arquitectura-monolito.md); se adopta por temas del temario.
>
> Esta arquitectura no se debe tocar. Son apuntes del profesor y se debe mantener así. (Ya se ha tocado con la carpeta config, revisar si está bien)

---

## 1. Estructura de paquetes

```text
com.daw.movies
├── MoviesApplication                   ← arranque de Spring Boot (+ @Modulithic: `config` es módulo compartido)
│
├── config                              ← configuración de toda la aplicación (fuera de las capas; módulo compartido)
│   └── OpenApiConfig                   ← título y versión del contrato OpenAPI (springdoc lo genera del código)
│
└── catalog                             ← MÓDULO Spring Modulith «Catalog» (sellado en package-info con @ApplicationModule)
    ├── controller                      ← capa de entrada (HTTP)
    │   ├── MovieController             ← API REST en /movies
    │   ├── dto
    │   │   ├── request
    │   │   │   └── MovieRequest        ← datos que entran (con Jakarta Validation)
    │   │   └── response
    │   │       └── MovieResponse       ← datos que salen (con DirectorView y ActorView)
    │   ├── mapper
    │   │   └── MovieMapper             ← dominio → MovieResponse
    │   └── handler
    │       └── GlobalExceptionHandler   ← excepciones → respuestas HTTP
    │
    ├── domain                          ← núcleo de negocio
    │   ├── model
    │   │   ├── Movie
    │   │   ├── Director
    │   │   └── Actor
    │   ├── exception
    │   │   ├── MovieNotFoundException
    │   │   └── DirectorNotFoundException
    │   ├── service
    │   │   ├── MovieService            ← interfaz (casos de uso)
    │   │   └── impl
    │   │       └── MovieServiceImpl    ← lógica de negocio y transacciones
    │   └── repository
    │       └── MovieRepository         ← interfaz (puerto) del repositorio
    │
    └── persistence                     ← acceso a datos
        ├── repository
        │   └── impl
        │       └── MovieRepositoryImpl ← implementa MovieRepository, usa el DAO
        └── dao
            ├── MovieDao                ← interfaz del DAO
            └── impl
                └── jdbc
                    └── MovieDaoJdbc    ← JDBC puro con JdbcTemplate
```

No hay JPA, ni *value objects* (D-46 revocada), ni entidades de persistencia separadas: las clases de `domain.model` son las que construye el DAO y las que consume el mapper.

**Módulo Spring Modulith (D-49):** `catalog` está sellado en su `package-info.java`; `config` es *módulo compartido* (`@Modulithic`). La verificación vive en `ModulithTest` (`verify()` + `Documenter`) y las reglas de ArchUnit en `ArquitecturaTest`. Hoy no hay fachada: `catalog` no ofrece ni consume nada de otros módulos.

**Segundo módulo — `ratings` (fase 5, [D-50](guia-estudio/decisiones-tecnicas.md#d-50-segundo-módulo-ratings-conectado-por-fachada)):**

```text
com.daw.movies.ratings                     ← MÓDULO «Ratings» (solo puede depender de `catalog`)
├── package-info.java                      ← @ApplicationModule(allowedDependencies = "catalog")
├── controller                             ← (operaciones sobre las valoraciones)
│   ├── RatingController                   ← GET/POST /movies/{movieId}/ratings · DELETE /ratings/{id}
│   ├── dto.request.RatingRequest          ← @Min(1) @Max(10), @Size(max=500), comentario opcional
│   ├── dto.response.RatingResponse
│   ├── mapper.RatingMapper                ← dominio → RatingResponse
│   └── handler.RatingExceptionHandler     ← 404 (película/valoración), 400, 415
├── domain
│   ├── model.Rating                       ← id, movieId (¡por id!: D-45), score, comment
│   ├── exception.MovieNotFoundException · RatingNotFoundException
│   ├── repository.RatingRepository        ← puerto
│   └── service.RatingService · impl.RatingServiceImpl
└── persistence
    ├── dao.RatingDao · impl.jdbc.RatingDaoJdbc
    └── repository.impl.RatingRepositoryImpl
```

La **fachada** del módulo catalog es el único punto de contacto: `CatalogFacade` + `record MovieLookup(Long id, String title)` viven en la raíz de `com.daw.movies.catalog` (la única zona pública del módulo sellado). Si la película no existe, ratings lanza **su propia** `MovieNotFoundException`. Esquema nuevo: `V5__create_rating_table.sql` (`rating`, FK a `movie` con `CASCADE`, `CHECK` nota 1–10).

---

## 2. Qué va en cada capa

| Capa                      | Paquete                       | Clase                                                 | Responsabilidad real                                                                                                                                                                                                                                                                                                                                                                                   |
| ------------------------- | ----------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Controlador               | `controller`                  | `MovieController`                                     | Endpoints `GET /movies`, `GET /movies/{id}`, `POST /movies`, `PUT /movies/{id}` y `DELETE /movies/{id}`. Valida el cuerpo con `@Valid`, pasa **valores sueltos** al servicio y convierte el resultado con el mapper. Sin lógica de negocio.                                                                                                                                                            |
| DTO de entrada            | `controller.dto.request`      | `MovieRequest`                                        | `record` con `title`, `releaseYear`, `duration` y `directorId`. Sus anotaciones (`@NotBlank`, `@Size(max = 255)`, `@NotNull`, `@Min(1888)`, `@MaxYearsAhead(10)`, `@Positive`) son la validación de entrada.                                                                                                                                                                                           |
| DTO de salida             | `controller.dto.response`     | `MovieResponse`                                       | `record` con id, título, año, duración, `DirectorView` (id y nombre completo) y lista de `ActorView`. No expone fecha de nacimiento ni biografía.                                                                                                                                                                                                                                                      |
| Mapper                    | `controller.mapper`           | `MovieMapper`                                         | `@Component`. Solo la **salida**: `toResponse(Movie)`.                                                                                                                                                                                                                                                                                                                                                 |
| Manejador de errores      | `controller.handler`          | `GlobalExceptionHandler`                              | `@RestControllerAdvice`. Traduce `MovieNotFoundException` y `DirectorNotFoundException` a **404**; `MethodArgumentNotValidException`, `HttpMessageNotReadableException` (JSON mal formado) y `MethodArgumentTypeMismatchException` (p. ej. `/movies/abc`) a **400**; y `HttpMediaTypeNotSupportedException` a **415**, con cuerpo `{ "message": ... }`.                                                |
| Modelo                    | `domain.model`                | `Movie`, `Director`, `Actor`                          | Clases con tipos simples, sin setters y sin anotaciones de persistencia. Los constructores solo comprueban lo que no puede faltar (`IllegalArgumentException`). `Movie` conoce a su `Director` y a su reparto (`addActor`; `getActors` devuelve una copia inmutable); las relaciones son unidireccionales. Cada clase tiene un segundo constructor con `id` para reconstruirla desde la base de datos. |
| Excepciones de dominio    | `domain.exception`            | `MovieNotFoundException`, `DirectorNotFoundException` | `RuntimeException` sin conocimiento de HTTP.                                                                                                                                                                                                                                                                                                                                                           |
| Servicio (contrato)       | `domain.service`              | `MovieService`                                        | `findAll`, `findById`, `create`, `update`, `delete`. Recibe valores sueltos, no DTO.                                                                                                                                                                                                                                                                                                                   |
| Servicio (implementación) | `domain.service.impl`         | `MovieServiceImpl`                                    | Lógica de los casos de uso: busca el director, construye la `Movie`, guarda y vuelve a leer por id. Lanza las excepciones de «no existe». Depende de `MovieRepository`.                                                                                                                                                                                                                                |
| Repositorio (puerto)      | `domain.repository`           | `MovieRepository`                                     | `findAll`, `findById`, `findDirectorById`, `save` (**devuelve el id generado**), `update(id, movie)` y `deleteById`.                                                                                                                                                                                                                                                                                   |
| Repositorio (adaptador)   | `persistence.repository.impl` | `MovieRepositoryImpl`                                 | `@Repository`. Cumple el puerto delegando en `MovieDao` (`save` llama a `insert`).                                                                                                                                                                                                                                                                                                                     |
| DAO (contrato)            | `persistence.dao`             | `MovieDao`                                            | Operaciones sobre la tabla `movie` y lo necesario para hidratar una `Movie`.                                                                                                                                                                                                                                                                                                                           |
| DAO (implementación)      | `persistence.dao.impl.jdbc`   | `MovieDaoJdbc`                                        | `@Repository`. SQL con `JdbcTemplate` y `RowMapper`.                                                                                                                                                                                                                                                                                                                                                   |

### Persistencia: cómo lee y escribe `MovieDaoJdbc`

- **Lectura:** una consulta con `JOIN` de `movie` y `director` y, después, **por cada película**, una segunda consulta sobre `movie_actor` + `actor` para cargar el reparto. En `findAll` eso es el problema **N+1** (1 + N consultas). `findById` hace 2.
- **Escritura:** `insert` usa `GeneratedKeyHolder` y devuelve el id; `update` y `deleteById` son sentencias simples.
- **El reparto no se escribe:** ni `insert` ni `update` tocan `movie_actor`. Solo se lee; no hay endpoints que lo gestionen.
- El esquema lo crean las migraciones Flyway (`V1` a `V4`); los datos de prueba están fuera de Flyway (D-42).

---

## 3. Reglas de dependencia

```text
controller ──▶ domain.service (interfaz)
    │
    └──▶ domain.model (MovieMapper lee Movie)

domain.service.impl ──▶ domain.repository (interfaz)
                                ▲
                                │ implementa
persistence.repository.impl ──▶ persistence.dao (interfaz)
                                        ▲
                                        │ implementa
                          persistence.dao.impl.jdbc
```

1. `controller` usa la **interfaz** `MovieService`, nunca `MovieServiceImpl`. Conoce también el modelo de dominio, porque `MovieMapper` lo traduce a DTO.
2. `domain` **no importa** nada de `controller` ni de `persistence`.
3. `MovieServiceImpl` depende de `MovieRepository` (interfaz), nunca de `MovieRepositoryImpl`.
4. `MovieRepositoryImpl` depende de `MovieDao` (interfaz), nunca de `MovieDaoJdbc`.
5. Los DTO viven en `controller`; el dominio no los conoce. Por eso el servicio recibe valores sueltos.
6. Toda dependencia se inyecta por constructor.

### Impurezas reconocidas

- `MovieServiceImpl` lleva `@Service` y `@Transactional`: `domain` depende de anotaciones de Spring. Se acepta por simplicidad (D-48).
- `MovieDaoJdbc` y `MovieRepositoryImpl` llevan `@Repository`.

---

## 4. Convenciones de nombres

| Elemento               | Patrón                       | Ejemplo                  |
| ---------------------- | ---------------------------- | ------------------------ |
| Controlador            | `<Entidad>Controller`        | `MovieController`        |
| DTO de entrada         | `<Entidad>Request`           | `MovieRequest`           |
| DTO de salida          | `<Entidad>Response`          | `MovieResponse`          |
| Mapper                 | `<Entidad>Mapper`            | `MovieMapper`            |
| Modelo                 | `<Entidad>`                  | `Movie`                  |
| Excepción de dominio   | `<Entidad>NotFoundException` | `MovieNotFoundException` |
| Servicio (interfaz)    | `<Entidad>Service`           | `MovieService`           |
| Servicio (impl.)       | `<Entidad>ServiceImpl`       | `MovieServiceImpl`       |
| Repositorio (interfaz) | `<Entidad>Repository`        | `MovieRepository`        |
| Repositorio (impl.)    | `<Entidad>RepositoryImpl`    | `MovieRepositoryImpl`    |
| DAO (interfaz)         | `<Entidad>Dao`               | `MovieDao`               |
| DAO (impl. JDBC)       | `<Entidad>DaoJdbc`           | `MovieDaoJdbc`           |

---

## 5. Flujo de una petición (ejemplo: crear una película)

```text
POST /movies  (MovieRequest)
   │
   ▼
MovieController        → @Valid comprueba MovieRequest (si falla: 400)
   │                     pasa title, releaseYear, duration y directorId sueltos
   ▼
MovieService.create(...)                     [interfaz]
   │   MovieServiceImpl (@Transactional):
   │   1. findDirectorById → si no existe: DirectorNotFoundException (404)
   │   2. new Movie(title, releaseYear, duration, director)
   │   3. save(movie) → id generado
   │   4. findById(id) → Movie completa
   ▼
MovieRepository                              [interfaz]
   │   MovieRepositoryImpl → MovieDao.insert / findById
   ▼
MovieDaoJdbc (JdbcTemplate)  →  MariaDB
   │
   ▲  la Movie vuelve por el mismo camino
   │
MovieController        → MovieMapper.toResponse(movie) → 201 Created
```

`update` y `delete` comprueban primero que la película existe (`MovieNotFoundException`, 404). `update` además comprueba el director.

### Errores

| Situación                                   | Excepción                                 | Respuesta |
| ------------------------------------------- | ----------------------------------------- | --------- |
| Película no existe                          | `MovieNotFoundException`                  | 404       |
| Director no existe                          | `DirectorNotFoundException`               | 404       |
| Cuerpo no cumple la validación              | `MethodArgumentNotValidException`         | 400       |
| JSON mal formado o con un tipo imposible    | `HttpMessageNotReadableException`         | 400       |
| Id de la URL no numérico                    | `MethodArgumentTypeMismatchException`     | 400       |
| `Content-Type` distinto de JSON             | `HttpMediaTypeNotSupportedException`      | 415       |
| La película no aparece justo tras guardarla | `IllegalStateException`, sin manejador    | 500       |
| Constructor del dominio rechaza un valor    | `IllegalArgumentException`, sin manejador | 500       |
| Error de base de datos                      | sin manejador                             | 500       |

---

## 6. Checklist para añadir una entidad nueva

Sustituir `Movie` por la entidad:

- [ ] `domain/model/<Entidad>`
- [ ] `domain/exception/<Entidad>NotFoundException`
- [ ] `domain/repository/<Entidad>Repository` (interfaz)
- [ ] `domain/service/<Entidad>Service` (interfaz) e `impl/<Entidad>ServiceImpl`
- [ ] `persistence/dao/<Entidad>Dao` (interfaz) e `impl/jdbc/<Entidad>DaoJdbc`
- [ ] `persistence/repository/impl/<Entidad>RepositoryImpl`
- [ ] `controller/dto/request/<Entidad>Request` y `dto/response/<Entidad>Response`
- [ ] `controller/mapper/<Entidad>Mapper`
- [ ] `controller/<Entidad>Controller` y el manejador de su excepción en `GlobalExceptionHandler`
- [ ] Migración Flyway si hay tabla nueva
- [ ] Tests de cada capa (TDD: primero el test)

---

## 7. Limitaciones conocidas

- **N+1** en `MovieDaoJdbc.findAll` (una consulta de actores por película).
- **Reparto de solo lectura:** no se escribe `movie_actor`.
- **500 sin tratar** para `IllegalArgumentException` y errores de base de datos.
- **Impureza de Spring en el dominio** (`@Service`, `@Transactional`).
- **Un único agregado:** `Director` y `Actor` existen como modelo, pero solo se leen a través de `Movie` (`findDirectorById` es lo único propio de director).
- **Sin `equals`/`hashCode`** en las entidades.
