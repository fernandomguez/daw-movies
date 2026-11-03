# Arquitectura de referencia (ejemplo con `Movie`)

> Documento base para reorganizar la estructura del proyecto y, más adelante, convertirlo en una **skill** que se irá refinando.
> Origen: `docs/arquitectura.txt` (ejemplo original con `Book`). Aquí se usa `Movie`.
> Paquete raíz: `com.daw.movies`.

---

## 1. Estructura de paquetes

```text
com.daw.movies
├── controller
│   ├── controllers                     ← controladores REST
│   │   └── MovieController
│   └── dto
│       ├── request
│       │   └── MovieRequest            ← datos que entran
│       └── response
│           └── MovieResponse           ← datos que salen
│
├── domain                              ← núcleo: no depende de nadie
│   ├── models
│   │   └── Movie                       ← entidad de dominio
│   ├── service
│   │   ├── MovieService                ← interfaz del servicio
│   │   └── impl
│   │       └── MovieServiceImpl        ← implementación (lógica de negocio)
│   └── repository
│       └── MovieRepository             ← interfaz (puerto) del repositorio
│
└── persistence                         ← acceso a datos
    ├── repository
    │   └── impl
    │       └── MovieRepositoryImpl     ← implementa MovieRepository, usa el DAO
    └── dao
        ├── MovieDao                    ← interfaz del DAO
        └── impl
            └── jdbc
                └── MovieDaoJdbc        ← implementación con JDBC
```

---

## 2. Qué va en cada capa

| Capa | Paquete | Clase de ejemplo | Responsabilidad |
|---|---|---|---|
| Controlador | `controller.controllers` | `MovieController` | Recibe la petición HTTP, valida la entrada, llama al servicio y devuelve la respuesta. Sin lógica de negocio. |
| DTO de entrada | `controller.dto.request` | `MovieRequest` | Forma de los datos que envía el cliente. |
| DTO de salida | `controller.dto.response` | `MovieResponse` | Forma de los datos que se devuelven al cliente. |
| Modelo | `domain.models` | `Movie` | Entidad de dominio y sus reglas. |
| Servicio (contrato) | `domain.service` | `MovieService` | Casos de uso que ofrece el dominio. |
| Servicio (implementación) | `domain.service.impl` | `MovieServiceImpl` | Lógica de negocio. Usa `MovieRepository`. |
| Repositorio (contrato) | `domain.repository` | `MovieRepository` | Qué necesita el dominio para guardar/leer, sin saber cómo. |
| Repositorio (implementación) | `persistence.repository.impl` | `MovieRepositoryImpl` | Cumple el contrato y delega en el DAO. |
| DAO (contrato) | `persistence.dao` | `MovieDao` | Operaciones de acceso a datos de una tabla. |
| DAO (implementación) | `persistence.dao.impl.jdbc` | `MovieDaoJdbc` | SQL/JDBC concreto. Si cambia la tecnología, se añade otra implementación al lado. |

---

## 3. Reglas de dependencia

```text
controller ──▶ domain.service (interfaz)
                    │
domain.service.impl ──▶ domain.repository (interfaz)
                                ▲
                                │ implementa
persistence.repository.impl ──▶ persistence.dao (interfaz)
                                        ▲
                                        │ implementa
                          persistence.dao.impl.jdbc
```

1. `controller` solo conoce las **interfaces** de `domain.service`, nunca `MovieServiceImpl`.
2. `domain` **no importa** nada de `controller` ni de `persistence`. Es la capa más estable.
3. `MovieServiceImpl` depende de `MovieRepository` (interfaz), nunca de `MovieRepositoryImpl`.
4. `MovieRepositoryImpl` depende de `MovieDao` (interfaz), nunca de `MovieDaoJdbc`.
5. Los DTO viven en `controller`; el dominio no los conoce. La conversión `Request → Movie → Response` se hace en el controlador (o en un mapper dentro de `controller`).
6. Toda dependencia se inyecta por constructor.

---

## 4. Convenciones de nombres

| Elemento | Patrón | Ejemplo |
|---|---|---|
| Controlador | `<Entidad>Controller` | `MovieController` |
| DTO de entrada | `<Entidad>Request` | `MovieRequest` |
| DTO de salida | `<Entidad>Response` | `MovieResponse` |
| Modelo | `<Entidad>` | `Movie` |
| Servicio (interfaz) | `<Entidad>Service` | `MovieService` |
| Servicio (impl.) | `<Entidad>ServiceImpl` | `MovieServiceImpl` |
| Repositorio (interfaz) | `<Entidad>Repository` | `MovieRepository` |
| Repositorio (impl.) | `<Entidad>RepositoryImpl` | `MovieRepositoryImpl` |
| DAO (interfaz) | `<Entidad>Dao` | `MovieDao` |
| DAO (impl. JDBC) | `<Entidad>DaoJdbc` | `MovieDaoJdbc` |

Código en inglés; documentación en español.

---

## 5. Flujo de una petición (ejemplo: crear una película)

```text
POST /movies  (MovieRequest)
   │
   ▼
MovieController          → convierte MovieRequest en Movie
   │
   ▼
MovieService.create(movie)           [interfaz]
   │   (MovieServiceImpl: reglas de negocio)
   ▼
MovieRepository.save(movie)          [interfaz]
   │   (MovieRepositoryImpl)
   ▼
MovieDao.insert(movie)               [interfaz]
   │   (MovieDaoJdbc: SQL)
   ▼
Base de datos (MariaDB)
   │
   ▲  el resultado vuelve por el mismo camino
   │
MovieController          → convierte Movie en MovieResponse → 201 Created
```

---

## 6. Checklist para añadir una entidad nueva (plantilla para la skill)

Sustituir `Movie` por la entidad (por ejemplo `Actor`, `Director`):

- [ ] `domain/models/Movie`
- [ ] `domain/repository/MovieRepository` (interfaz)
- [ ] `domain/service/MovieService` (interfaz)
- [ ] `domain/service/impl/MovieServiceImpl`
- [ ] `persistence/dao/MovieDao` (interfaz)
- [ ] `persistence/dao/impl/jdbc/MovieDaoJdbc`
- [ ] `persistence/repository/impl/MovieRepositoryImpl`
- [ ] `controller/dto/request/MovieRequest`
- [ ] `controller/dto/response/MovieResponse`
- [ ] `controller/controllers/MovieController`
- [ ] Tests de cada capa (TDD: primero el test)

---

## 7. Puntos abiertos para refinar en la skill

Cosas que el esquema original no define y que habrá que decidir:

- **Mapeo DTO ↔ modelo:** ¿en el controlador, en una clase `MovieMapper`, o con una librería?
- **Excepciones:** dónde se declaran (`domain`) y dónde se traducen a HTTP (un `@ControllerAdvice` en `controller`).
- **Value objects** (`Title`, `ReleaseYear`, …): si viven en `domain.models` o en un subpaquete propio.
- **Persistencia:** el esquema usa DAO + JDBC; el proyecto actual usa entidades JPA con conversores. Decidir si se mantiene JPA como otra implementación del DAO/repositorio o se sustituye.
- **Varias entidades relacionadas** (`Movie` con `Director` y `Actor`): si cada una tiene su propio juego completo de clases o si hay agregados.
- **Transacciones:** en qué capa se declaran (normalmente `MovieServiceImpl`).
- **Tests:** estructura espejo de `src/main` y cuáles son unitarios (dominio) o de integración (persistencia).
