# Arquitectura del proyecto (módulos `catalog` y `ratings`)

> Describe la arquitectura **real** del código en `src/main/java/com/daw/movies`, no un ideal. Paquete raíz: `com.daw.movies`.
> Decisiones que la respaldan: [D-48](guia-estudio/decisiones-tecnicas.md#d-48-arquitectura-por-capas-con-dao-jdbc-y-dominio-con-tipos-simples) (capas con DAO JDBC), [D-49](guia-estudio/decisiones-tecnicas.md#d-49-monolito-modular-módulo-catalog-con-spring-modulith) (monolito modular), [D-50](guia-estudio/decisiones-tecnicas.md#d-50-segundo-módulo-ratings-conectado-por-fachada) (segundo módulo por fachada) y [D-51](guia-estudio/decisiones-tecnicas.md#d-51-estructura-objetivo-en-los-dos-módulos-sync-con-el-§2-del-skill) (estructura `api` + `internal` del §2 del skill). Código en inglés; documentación en español.
>
> La arquitectura objetivo completa (JPA, MapStruct, modelos enriquecidos) está en [arquitectura-monolito.md](arquitectura-monolito.md); la estructura de paquetes ya es la del skill, lo que queda encolado son piezas internas.
>
> Esta arquitectura no se debe tocar. Son apuntes del profesor y se debe mantener así.

---

## 1. Estructura de paquetes

```text
com.daw.movies
├── MoviesApplication                      ← @SpringBootApplication, nada más (§3.1)
├── app                                    ← raíz de composición (NO es módulo)
│   └── OpenApiConfig                      ← título y versión del contrato OpenAPI (springdoc del código)
│
├── catalog                                ← MÓDULO «Catalog» (@ApplicationModule, sin dependencias)
│   ├── package-info.java
│   ├── api
│   │   ├── facade                         ← @NamedInterface("facade"): lo ÚNICO expuesto
│   │   │   ├── CatalogFacade              ← interfaz para los demás módulos
│   │   │   ├── MovieLookup                ← record DTO del contrato (no domina el módulo)
│   │   │   └── impl                       ← no forma parte de la interfaz con nombre
│   │   │       ├── CatalogFacadeImpl      ← delega en el servicio de dominio y mapea
│   │   │       └── CatalogFacadeMapper    ← a mano hasta que entre MapStruct
│   │   └── http                           ← contrato HTTP del módulo
│   │       ├── MovieController            ← GET/POST /movies · GET/PUT/DELETE /movies/{id}
│   │       ├── dto.request.MovieRequest   ← Jakarta Validation (@NotBlank, @Min(1888)…)
│   │       ├── dto.response.MovieResponse ← DirectorView y ActorView
│   │       ├── mapper.MovieMapper         ← dominio → MovieResponse
│   │       └── handler.CatalogExceptionHandler  ← 404/400/415 con { "message": ... }
│   └── internal
│       ├── application                    ← casos de uso (§3.3)
│       │   ├── FindMoviesUseCase · FindMovieByIdUseCase
│       │   └── CreateMovieUseCase · UpdateMovieUseCase · DeleteMovieUseCase
│       │        @Named; @jakarta.transaction.Transactional los que escriben
│       ├── domain                         ← núcleo (Java puro salvo @Named)
│       │   ├── model  Movie · Director · Actor
│       │   ├── exception  MovieNotFoundException · DirectorNotFoundException
│       │   ├── service  MovieService (+Impl)  ← consultas; las usa la fachada y los casos de uso
│       │   └── repository  MovieRepository ← puerto del repositorio
│       └── persistence                    ← adaptadores
│           ├── dao  MovieDao (+ MovieDaoJdbc: JDBC puro)
│           └── repository.impl  MovieRepositoryImpl
│
└── ratings                                ← MÓDULO «Ratings» (@ApplicationModule; "catalog :: facade")
    ├── package-info.java
    ├── api/http
    │   ├── RatingController               ← GET/POST /movies/{movieId}/ratings · DELETE /ratings/{id}
    │   ├── dto.request.RatingResponse     ← @Min(1) @Max(10), comentario ≤ 500 opcional
    │   ├── mapper.RatingMapper
    │   └── handler.RatingExceptionHandler
    └── internal
        ├── application                    ← CreateRating · FindRatingsByMovie · DeleteRating
        ├── domain
        │   ├── model.Rating               ← movieId POR ID (D-45): la peli vive en otro módulo
        │   ├── exception  MovieNotFoundException · RatingNotFoundException (propias del módulo)
        │   ├── port.AvailableMoviesPort   ← contrato con catalog declarado por el consumidor
        │   │       (record propio AvailableMovie: nada de DTO ajeno en el dominio)
        │   └── repository.RatingRepository
        └── persistence
            ├── dao  RatingDao (+ RatingDaoJdbc)
            ├── repository.impl  RatingRepositoryImpl
            └── adapter.CatalogMoviesAdapter ← ÚNICO import de la fachada ajena (§3.3)
```

No hay JPA, ni *value objects* (D-46 revocada), ni entidades de persistencia separadas: las clases de `internal.domain.model` son las que construye el DAO y las que consumen mapper y servicio de dominio.

**Módulos Spring Modulith (D-49/D-50/D-51):** `catalog` y `ratings` van sellados en su `package-info.java`; `app` no es módulo por la estrategia `spring.modulith.detection-strategy = explicitly-annotated` (solo los paquetes anotados cuentan). La verificación vive en `ModulithTest` (`verify()` + `Documenter` de Modulith) y en `ArquitecturaTest` (las reglas del §3.7 del skill). El único contacto entre módulos es `catalog :: facade`: si la película no existe, ratings lanza **su propia** `MovieNotFoundException` a través de su puerto `AvailableMoviesPort`. Esquema: migraciones `V1` a `V5` (`V5` crea `rating` con FK a `movie` en `CASCADE` y `CHECK` 1–10).

---

## 2. Qué va en cada paquete

Paquetes **relativos al módulo** (muestran el patrón; el árbol de §1 tiene las rutas completas).

| Elemento | Paquete | Clases | Responsabilidad real |
|---|---|---|---|
| Contrato HTTP | `api.http` | `MovieController`, `RatingController` | Llama a los **casos de uso** del propio módulo; valida con `@Valid`; nunca repositorios, ni `internal.persistence`, ni fachadas ajenas. |
| DTO web de entrada | `api.http.dto.request` | `MovieRequest`, `RatingRequest` | `record` con Jakarta Validation. Distintos de los records de la fachada aunque coincidan campo a campo (§3.3). |
| DTO web de salida | `api.http.dto.response` | `MovieResponse` (con `DirectorView`/`ActorView`), `RatingResponse` | Lo que sale; sin fechas de nacimiento ni biografías. |
| Mapper web | `api.http.mapper` | `MovieMapper`, `RatingMapper` | Solo la **salida**: dominio → DTO web. |
| Manejador de errores | `api.http.handler` | `CatalogExceptionHandler`, `RatingExceptionHandler` | `@RestControllerAdvice` **por módulo**: 404 (entidades), 400 (validación, JSON, id), 415, `{ "message": ... }`. |
| Casos de uso | `internal.application` | `FindMovies`, `FindMovieById`, `CreateMovie`, `UpdateMovie`, `DeleteMovie`, `CreateRating`, `FindRatingsByMovie`, `DeleteRating` | Un `execute()` cada uno (§3.3). Orquestan: servicio de dominio y/o puerto del repositorio, guardan y releen. `@Named`; transacción (`@jakarta.transaction.Transactional`) solo si escriben. No invocan otros casos de uso. |
| Servicio de dominio | `internal.domain.service` | `MovieService` + `MovieServiceImpl` | Consultas del módulo (`findMovie`, `findDirector` sobre el puerto) que usan la fachada y los casos de uso. Sin Spring. |
| Modelo | `internal.domain.model` | `Movie`, `Director`, `Actor`, `Rating` | Tipos simples, sin setters, sin anotaciones; constructores defensivos con `IllegalArgumentException`. Relaciones unidireccionales; entre módulos **por id** (D-45). |
| Excepciones de dominio | `internal.domain.exception` | `MovieNotFoundException`, `DirectorNotFoundException`, `RatingNotFoundException` | `RuntimeException` sin conocimiento de HTTP; propias de cada módulo. |
| Puerto del repositorio | `internal.domain.repository` | `MovieRepository`, `RatingRepository` | Interfaces (DIP): `findById`, listas, `save` (**devuelve el id generado**), `update`, `deleteById`. |
| Puerto de otro módulo | `internal.domain.port` | `AvailableMoviesPort` (record `AvailableMovie`) | Contrato con `catalog` **declarado por el consumidor** (§3.3): nunca DTO ajeno en el dominio. |
| Adaptador de repositorio | `internal.persistence.repository.impl` | `MovieRepositoryImpl`, `RatingRepositoryImpl` | `@Repository`; implementa el puerto delegando en el DAO. |
| DAO | `internal.persistence.dao(.impl.jdbc)` | `MovieDao`+`MovieDaoJdbc`, `RatingDao`+`RatingDaoJdbc` | JDBC puro: SQL con `JdbcTemplate` y `RowMapper`. |
| Adaptador de módulo ajeno | `internal.persistence.adapter` | `CatalogMoviesAdapter` | Implementa `AvailableMoviesPort` llamando a `CatalogFacade`: **el único sitio que importa una fachada ajena**. |
| Fachada (contrato) | `api.facade` | `CatalogFacade` + `MovieLookup` | `@NamedInterface("facade")`: solo interfaz + records; lo que los consumidores necesitan, no lo interno. |
| Fachada (impl.) | `api.facade.impl` | `CatalogFacadeImpl` + `CatalogFacadeMapper` | Delega en el servicio de dominio y mapea; sin reglas, sin repositorios, sin invocar casos de uso. Fuera de la interfaz con nombre. |

### Por qué `ratings` no tiene `api/facade` ni `domain.service` (todavía)

Los dos módulos comparten la **estructura** del §2, pero cada uno instancia solo las piezas que necesita. El skill marca cuándo nace cada una:

| Pieza | Qué la trae a la vida |
|---|---|
| `api/facade` + `@NamedInterface` + `impl` | Un **consumidor real** de otro módulo. §3.1: «un módulo que no ofrece nada a otros no necesita operaciones en su fachada… no inventes». `catalog` la estrenó al nacer `ratings` (D-50); `ratings` la estrenará cuando exista un tercer módulo o un módulo existente la necesite |
| `internal/domain.service` | La fachada que debe alimentar (§3.3: `FacadeImpl` «solo delega en servicios de dominio»), y/o reglas que cruzan modelos. En `ratings` los casos de uso llaman directo a sus puertos (`RatingRepository` + `AvailableMoviesPort`); el servicio aparecería con la fachada |
| `internal/persistence/adapter` | Tener un módulo del que **depender**. Solo lo tiene `ratings` (consume catalog). El grafo es dirigido: catalog no puede conocer a ratings (verificado por `ArquitecturaTest`) |

Si mañana ratings necesitara fachada, la receta es la del §6: interfaz + records en `api/facade`, `impl` delegando en el `RatingService` que renacería, y el consumidor declara `allowedDependencies = "ratings :: facade"`.

### Persistencia: cómo lee y escribe JDBC

- **Lectura:** `MovieDaoJdbc` hace una consulta con `JOIN` de `movie` y `director` y, después, **por cada película**, una segunda sobre `movie_actor` + `actor` para el reparto. En `findAll` eso es el **N+1** (1 + N consultas); `findById` hace 2. `RatingDaoJdbc` no necesita JOINs.
- **Escritura:** `insert` usa `GeneratedKeyHolder` y devuelve el id; `update` y `deleteById` son sentencias simples. El reparto no se escribe (no hay endpoints que lo gestionen).
- El esquema lo crean las migraciones Flyway (`V1` a `V5`); los datos de prueba están fuera de Flyway (D-42).

---

## 3. Reglas de dependencia

```text
api/http ──────► internal.application ──► internal.domain ◄── internal.persistence ◄──┐
   (controllers llaman         (casos de uso:          (modelo, servicio y puertos)   │
    casos de uso)               orquestan; tx aquí)                                    │
                                                                                       │
api/facade/impl ──► internal.domain.service ──► internal.domain.repository (puerto)
   (CatalogFacadeMapper, a mano)                                                  │
                                                                                       │
ratings.internal.persistence.adapter ──► catalog.api.facade   (la única fachada ajena)─┘
app ──► todo
```

1. `api.http` llama a los **casos de uso** de su módulo; nunca `internal.persistence`, ni `api.facade.impl`, ni módulos ajenos.
2. `internal.application` no toca `internal.persistence` ni `api`.
3. `internal.domain` no importa nada de `api`, de `internal.application`, de `internal.persistence` ni de Spring (solo se tolera `jakarta.inject.Named`).
4. Los casos de uso, el servicio de dominio y los adaptadores dependen de **interfaces** (`MovieRepository`, `MovieDao`, `AvailableMoviesPort`), nunca de los `*Impl`.
5. Los DTO web viven en `api.http` y los de fachada en `api.facade`; el dominio no conoce ninguno. Por eso los casos de uso reciben valores sueltos.
6. Un módulo consume a otro solo por su fachada con nombre (`catalog :: facade`); el adaptador ajeno vive en **su** `internal.persistence`. El grafo no tiene ciclos.
7. Toda dependencia se inyecta por constructor.

### Impurezas toleradas

- `@Named` (jakarta, no Spring) en casos de uso, fachada impl y el servicio de dominio.
- `MovieDaoJdbc`, `RatingDaoJdbc`, `MovieRepositoryImpl` y `RatingRepositoryImpl` llevan `@Repository` (Spring) en `internal.persistence`.

---

## 4. Convenciones de nombres

| Elemento               | Patrón                       | Ejemplo                  |
| ---------------------- | ---------------------------- | ------------------------ |
| Controlador            | `<Entidad>Controller`        | `MovieController`        |
| DTO de entrada         | `<Entidad>Request`           | `MovieRequest`           |
| DTO de salida          | `<Entidad>Response`          | `MovieResponse`          |
| Mapper web             | `<Entidad>Mapper`            | `MovieMapper`            |
| Caso de uso            | `<Acción><Entidad>UseCase`   | `CreateMovieUseCase`     |
| Modelo                 | `<Entidad>`                  | `Movie`                  |
| Excepción de dominio   | `<Entidad>NotFoundException` | `MovieNotFoundException` |
| Servicio de dominio    | `<Entidad>Service(+Impl)`    | `MovieService`           |
| Repositorio (interfaz) | `<Entidad>Repository`        | `MovieRepository`        |
| Repositorio (impl.)    | `<Entidad>RepositoryImpl`    | `MovieRepositoryImpl`    |
| DAO (interfaz/impl.)   | `<Entidad>Dao`, `<Entidad>DaoJdbc` | `MovieDaoJdbc`     |
| Fachada                | `<Modulo>Facade` (+ `Impl`, mapper) | `CatalogFacade`   |
| Handler del módulo     | `<Modulo>ExceptionHandler`   | `CatalogExceptionHandler` |
| Puerto de módulo ajeno | `<Capacidad>Port` (+ `Adapter`) | `AvailableMoviesPort` |
| Módulo                 | minúsculas, plural de negocio | `catalog`, `ratings`    |

---

## 5. Flujo de una petición (ejemplo: crear una película)

```text
POST /movies  (MovieRequest)
   │
   ▼
MovieController        → @Valid comprueba MovieRequest (si falla: 400)
   │                     pasa title, releaseYear, duration y directorId sueltos
   ▼
CreateMovieUseCase.execute(...)               [internal.application]
   │   1. MovieService.findDirector(id) → si no existe: DirectorNotFoundException (404)
   │   2. new Movie(title, releaseYear, duration, director)
   │   3. MovieRepository.save(movie) → id generado
   │   4. MovieRepository.findById(id) → Movie completa
   │      (@jakarta.transaction.Transactional cubre todo el caso de uso)
   ▼
MovieRepositoryImpl          [internal.persistence, implementa el puerto]
   │   → MovieDao.insert / findById
   ▼
MovieDaoJdbc (JdbcTemplate)  →  MariaDB
   │
   ▲  la Movie vuelve por el mismo camino
   │
MovieController        → MovieMapper.toResponse(movie) → 201 Created
```

En `ratings`, el caso de uso `CreateRatingUseCase` comprueba primero la película por `AvailableMoviesPort` (que el adapter `CatalogMoviesAdapter` responde con la fachada de `catalog`); si no existe, 404 con la excepción propia del módulo.

`update` y `delete` comprueban primero que la película existe (`MovieNotFoundException`, 404). `update` además comprueba el director.

### Errores

| Situación                                   | Excepción                                 | Respuesta |
| ------------------------------------------- | ----------------------------------------- | --------- |
| Película/valoración no existe               | `MovieNotFoundException`, `RatingNotFoundException` | 404 |
| Director no existe                          | `DirectorNotFoundException`               | 404       |
| Cuerpo no cumple la validación              | `MethodArgumentNotValidException`         | 400       |
| JSON mal formado o con un tipo imposible    | `HttpMessageNotReadableException`         | 400       |
| Id de la URL no numérico                    | `MethodArgumentTypeMismatchException`     | 400       |
| `Content-Type` distinto de JSON             | `HttpMediaTypeNotSupportedException`      | 415       |
| La entidad no aparece justo tras guardarla  | `IllegalStateException`, sin manejador    | 500       |
| Constructor del dominio rechaza un valor    | `IllegalArgumentException`, sin manejador | 500       |
| Error de base de datos                      | sin manejador                             | 500       |

---

## 6. Checklist para añadir una entidad nueva (dentro de un módulo)

Sustituir `<Entidad>` por la entidad y seguir el orden del §3.8:

- [ ] `internal/domain/model/<Entidad>` (+ excepciones propias si las hay)
- [ ] `internal/domain/repository/<Entidad>Repository` (puerto)
- [ ] `internal/persistence/dao/<Entidad>Dao` e `impl/jdbc/<Entidad>DaoJdbc`
- [ ] `internal/persistence/repository/impl/<Entidad>RepositoryImpl`
- [ ] `internal/application/<Acción><Entidad>UseCase` (uno por operación con `execute`)
- [ ] `api/http/dto/request/<Entidad>Request` y `dto/response/<Entidad>Response`
- [ ] `api/http/mapper/<Entidad>Mapper`
- [ ] `api/http/<Entidad>Controller` + método en el `<Modulo>ExceptionHandler`
- [ ] Operación consumible por otros módulos: método en `<Modulo>Facade` + record DTO, delegación y mapeo en `facade/impl`, `allowedDependencies` del consumidor, puerto + adapter en el consumidor
- [ ] Migración Flyway si hay tabla nueva
- [ ] Tests de cada capa (TDD: primero el test)

---

## 7. Limitaciones conocidas

- **N+1** en `MovieDaoJdbc.findAll` (una consulta de actores por película).
- **Reparto de solo lectura:** no se escribe `movie_actor`.
- **500 sin tratar** para `IllegalArgumentException` y errores de base de datos.
- **Un único agregado en `catalog`:** `Director` y `Actor` existen como modelo, pero solo se leen a través de `Movie` (`MovieService.findDirector` es lo único propio de director).
- **Sin `equals`/`hashCode`** en las entidades (identidad = `id` de la base).
- **Sin `@ApplicationModuleTest` por módulo** ni MapStruct ni JPA: en esa cola está su pieza ([arquitectura-monolito.md](arquitectura-monolito.md), §2).
