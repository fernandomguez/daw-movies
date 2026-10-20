# 05 — Arquitectura en tres capas e inversión de dependencias

> **Fase del plan:** 5. Tema **teórico**: el código que aparece es **ilustrativo** (todavía no existe en el proyecto). Las clases reales se crearán a partir de la fase 6, una a una.

## Objetivo

Entender:

1. Qué responsabilidad tiene cada una de las tres capas.
2. En qué **dirección** deben ir las dependencias entre ellas.
3. Qué es el **principio de inversión de dependencias (DIP)** y cómo lo aplicaremos.
4. Cómo se traducen las capas a **paquetes** de Java.

## 1. Qué significa "depender de"

En Java, la clase A **depende de** la clase B cuando A **usa** B: la importa, crea objetos suyos, llama a sus métodos o la recibe como parámetro.

```java
import com.example.persistence.MovieJpaRepository;   // ← MovieService depende de MovieJpaRepository

public class MovieService {
    private MovieJpaRepository repository;
}
```

**Consecuencia:** si B cambia, A puede romperse. Cuantas más dependencias, más cosas se rompen con cada cambio. A esto se le llama **acoplamiento**.

## 2. Las tres capas

Cada capa responde a una pregunta distinta:

| Capa | Pregunta que responde | Qué contiene en nuestro proyecto | Qué NO debe hacer | Conceptos asociados (fase) |
|---|---|---|---|---|
| **Controlador** | ¿Cómo llega una petición desde fuera y cómo se responde? | Clases que reciben peticiones HTTP (`GET /movies/3`) y devuelven JSON. DTOs. | Contener reglas de negocio o SQL. | DTO de salida (15) y de entrada (17); mapper dominio ↔ DTO (15). |
| **Dominio** | ¿Qué es una película y qué reglas cumple? ¿Qué operaciones ofrece la aplicación? | `Movie`, `Director`, `Actor`, sus reglas, los servicios (casos de uso) y las **interfaces** de repositorio. | Saber que existen HTTP, JSON, JPA, MariaDB o Spring. | Modelo anémico vs. rico (10); repositorio como puerto y diferencia con DAO (12); servicio o caso de uso (16). |
| **Persistencia** | ¿Cómo se guarda y recupera la información? | Entidades JPA, repositorios de Spring Data, adaptadores y mappers entidad ↔ dominio. | Decidir reglas de negocio. | Entidad JPA ≠ objeto de dominio (11); Spring Data y adaptador (12); mappers entidad ↔ dominio (13). |

La última columna se desarrolla en la [sección 10](#10-conceptos-relacionados-que-veremos-más-adelante).

Ejemplo con una regla real: "la duración de una película debe ser positiva".

- La **base de datos** la protege con `CHECK (duration_minutes > 0)` (fase 3).
- El **dominio** la expresa en Java, para que un objeto `Movie` nunca pueda existir con duración negativa (fase 10).
- El **controlador** solo traduce: recibe el JSON, lo pasa al dominio y devuelve un error HTTP si algo falla (fase 18).

## 3. ¿Por qué el dominio es el centro?

El **dominio** es lo más valioso y lo que menos debería cambiar: una película tiene título, año y director tanto si los datos están en MariaDB, en PostgreSQL o en un archivo; y tanto si se piden por HTTP como desde un test.

En cambio, los **detalles técnicos** cambian con frecuencia: versión de Spring, de Hibernate, forma de la API... Por eso queremos que el dominio **no dependa** de ellos.

**Regla de oro:** las dependencias apuntan **hacia el dominio**, nunca desde el dominio hacia fuera.

```
Controlador ──► Dominio ◄── Persistencia
```

## 4. El problema: la forma "ingenua"

Lo más intuitivo sería que el servicio del dominio usara directamente el repositorio de Spring Data:

```java
// ❌ Dominio dependiendo de la persistencia
package com.example.domain;

import com.example.persistence.MovieEntity;          // clase JPA
import com.example.persistence.MovieJpaRepository;   // interfaz de Spring Data

public class MovieService {
    private final MovieJpaRepository repository;

    public MovieEntity findMovie(Long id) { ... }     // devuelve una entidad JPA
}
```

```
Controlador ──► Dominio ──► Persistencia ──► JPA / MariaDB
```

Problemas concretos en **nuestro** proyecto:

1. **Los tests del dominio necesitarían base de datos.** Para probar `MovieService` habría que arrancar Spring y MariaDB. Con TDD (que exige `base-standards.md`) escribiremos muchos tests: queremos que los del dominio tarden milisegundos, no segundos.
2. **JPA se "cuela" en el dominio.** `MovieEntity` tiene anotaciones `@Entity`, carga perezosa (`LAZY`) y otras particularidades. Un error típico, `LazyInitializationException` (fase 14), aparecería en el corazón del negocio.
3. **Cualquier cambio en la base de datos obliga a cambiar el dominio.** Renombrar una columna o cambiar de JPA a JDBC afectaría a clases que no deberían enterarse.

## 5. La solución: inversión de dependencias (DIP)

El principio dice:

> Los módulos de alto nivel (el dominio) **no deben depender** de los de bajo nivel (la persistencia). **Ambos deben depender de abstracciones** (interfaces). Y la abstracción la define **quien la necesita**.

Aplicado a nuestro caso, en tres pasos:

**Paso 1. El dominio define lo que necesita, con sus propias palabras (una interfaz):**

```java
// ✅ En el dominio. Solo usa clases del dominio y de Java estándar.
package com.example.domain.repository;

import com.example.domain.model.Movie;
import java.util.List;
import java.util.Optional;

public interface MovieRepository {
    Optional<Movie> findById(Long id);
    List<Movie> findAll();
}
```

Fíjate: habla de `Movie` (dominio), no de `MovieEntity` (JPA). No sabe nada de SQL.

**Paso 2. El servicio del dominio usa esa interfaz:**

```java
package com.example.domain.service;

public class MovieService {
    private final MovieRepository movieRepository;   // la interfaz del dominio

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }
}
```

**Paso 3. La persistencia implementa la interfaz (un "adaptador"):**

```java
// En persistencia. Depende del dominio (implementa su interfaz y devuelve sus objetos).
package com.example.persistence.adapter;

public class MovieRepositoryAdapter implements MovieRepository {
    private final MovieJpaRepository jpaRepository;   // Spring Data
    private final MovieEntityMapper mapper;           // MovieEntity → Movie

    @Override
    public Optional<Movie> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
```

### ¿Qué se ha "invertido"?

La **flecha de dependencia** entre dominio y persistencia:

```
ANTES (sin DIP):   Dominio ──────────────► Persistencia
                   (MovieService importa MovieJpaRepository)

DESPUÉS (con DIP): Dominio ◄────────────── Persistencia
                   (MovieRepositoryAdapter importa e implementa MovieRepository)
```

En tiempo de ejecución, los datos siguen yendo del dominio a la base de datos y vuelta. Lo que cambia es **quién conoce a quién en el código**: ahora la persistencia conoce al dominio, y el dominio no conoce a la persistencia.

### ¿Cómo recibe `MovieService` el adaptador si no lo conoce?

Mediante **inyección de dependencias**: alguien externo crea el adaptador y se lo pasa al servicio por el constructor. Ese "alguien" es **Spring**: al arrancar, crea los objetos (llamados *beans*) y los conecta entre sí. Lo veremos en la práctica en la fase 12.

En un **test** del dominio, en cambio, podemos pasarle una implementación falsa en memoria, sin base de datos:

```java
MovieRepository fakeRepository = new InMemoryMovieRepository();   // lista en memoria
MovieService service = new MovieService(fakeRepository);
```

Ese es el beneficio más inmediato para nosotros: **tests del dominio rápidos y sin infraestructura**.

### Vocabulario: puerto y adaptador

- **Puerto:** la interfaz que define el dominio (`MovieRepository`). Es el "enchufe".
- **Adaptador:** la clase que la implementa con una tecnología concreta (`MovieRepositoryAdapter`, con JPA). Es "lo que se enchufa".

## 6. El coste (para ser honestos)

DIP no es gratis. Para una sola consulta de películas tendremos:

| Clase | Capa | Para qué |
|---|---|---|
| `Movie` | Dominio | Modelo de negocio |
| `MovieRepository` | Dominio | Puerto (interfaz) |
| `MovieEntity` | Persistencia | Representación de la tabla |
| `MovieJpaRepository` | Persistencia | Consultas con Spring Data |
| `MovieEntityMapper` | Persistencia | `MovieEntity` ↔ `Movie` |
| `MovieRepositoryAdapter` | Persistencia | Implementa el puerto |

En un proyecto pequeño "de verdad" podría ser excesivo, y muchos equipos usan directamente las entidades JPA. **Aquí lo hacemos porque aprender esta separación y los mapeos es el objetivo del proyecto**, y porque nos da tests del dominio sin base de datos. Cada clase se justificará cuando la creemos; no se crearán "por si acaso".

## 7. Reglas de dependencia (qué puede importar cada capa)

| Desde \ Hacia | Controlador | Dominio | Persistencia | Spring / JPA |
|---|---|---|---|---|
| **Controlador** | — | ✅ | ❌ | ✅ (Spring Web) |
| **Dominio** | ❌ | — | ❌ | ❌ (solo Java estándar) |
| **Persistencia** | ❌ | ✅ | — | ✅ (JPA, Spring Data) |

Forma fácil de comprobarlo: **ningún archivo del paquete `domain` puede tener un `import` que empiece por `org.springframework`, `jakarta.persistence` ni por los paquetes `controller` o `persistence`**. En la fase 20 podremos comprobarlo automáticamente con un test.

**¿Entonces no usamos Spring?** Sí, en todo el proyecto. Solo las clases del dominio quedan sin Spring, a propósito, para poder probarlas sin arrancar nada:

| Capa | ¿Usa Spring? | Qué parte de Spring |
|---|---|---|
| Controlador | ✅ Sí | Spring Web: `@RestController`, `@GetMapping`, conversión a JSON |
| Dominio | ❌ No (a propósito) | Ninguna: Java normal |
| Persistencia | ✅ Sí | Spring Data JPA: `JpaRepository`, `@Entity` |
| `config/` | ✅ Sí | `@Configuration` y `@Bean`: Spring crea las clases del dominio y las conecta |
| Todo el proyecto | ✅ Sí | Spring Boot lo arranca, configura la conexión a MariaDB, ejecuta Flyway... |

## 8. Recorrido completo de una petición (vista previa)

Así viajará `GET /movies/3` cuando todo esté construido (fase 16).

**Cómo leer el diagrama:** cada bloque es una clase. Debajo de ella, **"hace"** es su trabajo y **"llama a"** es el método **de la clase siguiente** que invoca. El `findById` que aparece bajo `MovieController` **no es un método del controlador**: es la llamada que hace al servicio.

### Ida: la petición baja hasta la base de datos

```
Cliente HTTP (navegador, Postman...)
│  envía:    GET /movies/3
▼
┌─ CONTROLADOR ───────────────────────────────────────────
│  MovieController
│    hace:     saca el 3 de la URL /movies/3
│    llama a:  movieService.findById(3)
▼
┌─ DOMINIO ───────────────────────────────────────────────
│  MovieService
│    hace:     aplica las reglas del caso de uso
│              (si la película no existe → error "película no encontrada")
│    llama a:  movieRepository.findById(3)
│              (solo conoce la interfaz MovieRepository, no sabe que hay JPA)
▼
┌─ PERSISTENCIA ──────────────────────────────────────────
│  MovieRepositoryAdapter   (implementa el puerto MovieRepository)
│    llama a:  jpaRepository.findById(3)
│
│  MovieJpaRepository (Spring Data) + Hibernate
│    hace:     genera y lanza  SELECT ... FROM movie WHERE id = 3
▼
MariaDB  (tabla movie, fila con id = 3)
```

### Vuelta: el resultado sube y cambia de forma en cada capa

| Paso | Dónde está | Qué objeto hay | Quién lo convierte en el siguiente |
|---|---|---|---|
| 1 | MariaDB | Una **fila** de la tabla `movie` | Hibernate la lee... |
| 2 | Persistencia | ...y la convierte en un **`MovieEntity`** | El adaptador, con su mapper entidad → dominio |
| 3 | Dominio | Un **`Movie`** (el objeto de negocio) | El servicio se lo devuelve al controlador |
| 4 | Controlador | Un **`MovieResponse`** (DTO) | El controlador, con su mapper dominio → DTO |
| 5 | Cliente | Texto **JSON** | Spring lo escribe en la respuesta HTTP (200 OK) |

En resumen: **fila → `MovieEntity` → `Movie` → `MovieResponse` → JSON**. Cada capa solo trabaja con su propio objeto.

### ¿Y si la película 3 no existe?

El trabajo se reparte:

1. El **adaptador** devuelve "vacío" (`Optional.empty()`): no ha encontrado la fila.
2. El **dominio** decide qué significa eso: es un error de *película no encontrada*.
3. El **controlador** lo traduce al lenguaje de HTTP: responde **404 Not Found** (fase 18).

### Lógica de negocio frente a traducción

El controlador no tiene lógica de negocio: **traduce** entre HTTP y Java (lee la URL, llama al dominio y da forma a la respuesta). Para saber en qué capa va algo, usa esta prueba:

> Si mañana la aplicación dejara de ser una API REST y pasara a ser un programa de consola, **¿esto seguiría siendo verdad?** Si la respuesta es sí, es del **dominio**. Si es no, es del **controlador**.

| Afirmación | ¿Sigue valiendo en consola? | Capa |
|---|---|---|
| "Buscar una película por su id; si no existe, es un error" | Sí | Dominio |
| "La duración debe ser positiva" | Sí | Dominio |
| "No se puede crear una película con un director que no existe" | Sí | Dominio |
| "El id viene en la URL, después de `/movies/`" | No (en consola no hay URL) | Controlador |
| "Si no existe, responder con un 404" | No (en consola no hay códigos HTTP) | Controlador |

**El mismo nombre no implica el mismo trabajo.** `findById` aparece en tres clases, pero cada una hace una cosa distinta:

| Clase | Su `findById` significa... |
|---|---|
| `MovieService` (dominio) | "El caso de uso *consultar una película*, con sus reglas." |
| `MovieRepositoryAdapter` (persistencia) | "Tráemela de donde esté guardada y conviértela en `Movie`." |
| `MovieJpaRepository` (Spring Data) | "Haz el `SELECT ... WHERE id = ?`." |

En una consulta tan sencilla, el servicio apenas añade nada y casi solo pasa la llamada. Es normal. Su papel se nota cuando hay reglas, por ejemplo al crear películas (fase 17).

### Mapa completo: todas las piezas

El diagrama anterior responde a **quién llama a quién**. Este apartado añade **qué objeto viaja en cada tramo** y **quién lo transforma**: DTO, mappers, modelo de dominio, entidad JPA, etc.

Cada recorrido aparece **de dos formas que cuentan lo mismo**:

1. **Paso a paso (tablas):** como una carrera de relevos. Cada fila es una pieza que recibe algo, hace una cosa y se lo pasa a la siguiente. Se lee de arriba abajo. **Empieza por aquí.**
2. **De un vistazo (diagrama):** todo el recorrido en un solo dibujo, con las fronteras entre capas. Sirve para repasar cuando ya entiendes las tablas.

**Cómo leer los diagramas:**

- **Columna izquierda (▼):** la ida. Se lee de arriba abajo.
- **Columna derecha (▲):** la vuelta. Se lee **de abajo arriba**, empezando por la fila de MariaDB.
- Las líneas `══╪══` son las fronteras entre capas. Cada objeto solo existe dentro de su capa: para cruzar una frontera, un mapper lo convierte en el objeto de la capa siguiente.
- `◄─` señala qué es cada pieza.

#### Consulta: `GET /movies/3`

**Paso a paso. Ida (la petición baja hasta la base de datos):**

| Paso | Quién (capa) | Qué recibe | Qué hace |
|---|---|---|---|
| 1 | Cliente (navegador o Postman) | — | Envía `GET /movies/3` |
| 2 | `MovieController` (controlador) | La URL | Saca el número 3 y se lo pasa al servicio |
| 3 | `MovieService` (dominio) | El 3 | Pide la película 3 al repositorio (`MovieRepository`, el puerto) |
| 4 | `MovieRepositoryAdapter` (persistencia) | El 3 | Se lo pasa a Spring Data |
| 5 | `MovieJpaRepository` + Hibernate (persistencia) | El 3 | Escribe y lanza `SELECT ... WHERE id = 3` |
| 6 | MariaDB | El SQL | Encuentra la fila de *Inception* |

**Paso a paso. Vuelta (la respuesta sube, y en cada paso cambia de forma):**

| Paso | Quién (capa) | Qué recibe | Lo convierte en... |
|---|---|---|---|
| 7 | Hibernate (persistencia) | La fila | `MovieEntity`: la película "con forma de tabla" |
| 8 | `MovieEntityMapper` (persistencia) | `MovieEntity` | `Movie`: la película "con forma de negocio" |
| 9 | `MovieService` (dominio) | `Movie` (o nada) | Si no hay película, lanza "no encontrada". Si la hay, la devuelve |
| 10 | `MovieDtoMapper` (controlador) | `Movie` | `MovieResponse`: la película "con forma de lo que ve el cliente" |
| 11 | Spring (controlador) | `MovieResponse` | Texto JSON (con 200 OK) |
| 12 | Cliente | El JSON | Lo muestra |

**La idea principal:** es **la misma película con tres formas**, y un mapper la convierte de una forma a la siguiente:

| Forma | Se parece a... | Capa |
|---|---|---|
| `MovieEntity` | La tabla de MariaDB | Persistencia |
| `Movie` | El negocio (con sus reglas) | Dominio |
| `MovieResponse` | Lo que ve el cliente (el JSON) | Controlador |

**De un vistazo (el mismo recorrido en diagrama):**

```
  IDA: la petición baja                       VUELTA: la respuesta sube

  Cliente: GET /movies/3                      Cliente recibe: 200 OK + JSON
  │                                           {"id": 3, "title": "Inception",
  │                                            "releaseYear": 2010, "durationMinutes": 148,
  │                                            "directorName": "Christopher Nolan"}
  │                                           ▲  Spring convierte el DTO en JSON
══╪══ CONTROLADOR ════════════════════════════╪═════════════════════════════════════════════════
  ▼                                           │
  MovieController.getMovie(3)                 MovieResponse      ◄─ DTO de salida (record)
  │  lee el 3 de la URL                       ▲
  │                                           MovieDtoMapper     ◄─ mapper dominio → DTO
  │                                           ▲  (puede calcular campos: nombre +
  │                                           │   apellidos del director = directorName)
══╪══ DOMINIO ════════════════════════════════╪═════════════════════════════════════════════════
  ▼                                           │
  MovieService.findById(3)                    Movie              ◄─ modelo de dominio
  │  caso de uso; si no hay película          ▲    (Java puro, protege sus reglas)
  │  → MovieNotFoundException                 │
  ▼                                           │
  MovieRepository  ◄─ PUERTO (interfaz)       │
══╪══ PERSISTENCIA ═══════════════════════════╪═════════════════════════════════════════════════
  ▼  lo implementa...                         │
  MovieRepositoryAdapter ◄─ ADAPTADOR         MovieEntityMapper  ◄─ mapper entidad → dominio
  │                                           ▲
  ▼                                           │
  MovieJpaRepository ◄─ Spring Data           MovieEntity        ◄─ entidad JPA (@Entity)
  │                                           ▲
  ▼                                           │
  Hibernate (JPA) genera el SQL               Hibernate copia la fila en la entidad
  │  y lo envía con el driver JDBC            ▲
══╪══ BASE DE DATOS (MariaDB) ════════════════╪═════════════════════════════════════════════════
  ▼                                           │
  SELECT ... FROM movie WHERE id = 3          fila: (3, 'Inception', 2010, 148, 1)
  │                                           ▲
  └────────── MariaDB busca la fila ──────────┘
```

#### Creación: `POST /movies` (fase 17)

En la creación los objetos viajan al revés: el JSON entra, se convierte en DTO de entrada, después en objeto de dominio y después en entidad. Aquí se ve bien el trabajo del dominio, que comprueba las reglas antes de guardar nada.

**Paso a paso. Ida (los datos bajan para guardarse):**

| Paso | Quién (capa) | Qué recibe | Qué hace |
|---|---|---|---|
| 1 | Cliente | — | Envía `POST /movies` con el JSON `{"title": "Oppenheimer", "releaseYear": 2023, "durationMinutes": 180, "directorId": 1}` |
| 2 | Spring (controlador) | El JSON | Lo convierte en `CreateMovieRequest` (DTO de entrada) |
| 3 | `@Valid` (controlador) | `CreateMovieRequest` | Comprueba el **formato**: título no vacío, números donde toca... Si falla, responde 400 Bad Request y no sigue |
| 4 | `MovieController` + `MovieDtoMapper` (controlador) | `CreateMovieRequest` | Lo convierte en datos del dominio y llama al servicio |
| 5 | `MovieService` (dominio) | Los datos | Comprueba las **reglas**: ¿existe el director 1? Crea `new Movie(...)`, cuyo constructor comprueba que la duración sea positiva. Llama a `movieRepository.save(movie)` |
| 6 | `MovieRepositoryAdapter` + `MovieEntityMapper` (persistencia) | `Movie` | Lo convierte en `MovieEntity` (todavía sin id) |
| 7 | `MovieJpaRepository` + Hibernate (persistencia) | `MovieEntity` | Escribe y lanza el `INSERT INTO movie ...` |
| 8 | MariaDB | El SQL | Guarda la fila y genera el id nuevo (`AUTO_INCREMENT`) |

**Paso a paso. Vuelta (se confirma lo guardado):**

| Paso | Quién (capa) | Qué recibe | Lo convierte en... |
|---|---|---|---|
| 9 | Hibernate (persistencia) | El id nuevo | Lo guarda dentro de la `MovieEntity` |
| 10 | `MovieEntityMapper` (persistencia) | `MovieEntity` | `Movie`, ya con su id |
| 11 | `MovieService` (dominio) | `Movie` | Se la devuelve al controlador |
| 12 | `MovieDtoMapper` (controlador) | `Movie` | `MovieResponse` |
| 13 | Spring (controlador) | `MovieResponse` | Texto JSON (con 201 Created) |
| 14 | Cliente | El JSON | Ve la película creada, con su id |

**De un vistazo (el mismo recorrido en diagrama):**

```
  IDA: los datos bajan para guardarse         VUELTA: se confirma lo guardado

  Cliente: POST /movies + JSON                Cliente recibe: 201 Created + JSON
  {"title": "Oppenheimer",                    {"id": <nuevo>, "title": "Oppenheimer",
   "releaseYear": 2023,                        "directorName": "Christopher Nolan", ...}
   "durationMinutes": 180, "directorId": 1}   ▲
══╪══ CONTROLADOR ════════════════════════════╪═════════════════════════════════════════════════
  ▼  Spring convierte el JSON en...           │
  CreateMovieRequest ◄─ DTO de entrada        MovieResponse      ◄─ DTO de salida
  │  @Valid: formato (título no vacío...)     ▲
  ▼                                           │
  MovieController.create(request)             MovieDtoMapper     ◄─ dominio → DTO
  │  MovieDtoMapper: DTO → datos del dominio  ▲
══╪══ DOMINIO ════════════════════════════════╪═════════════════════════════════════════════════
  ▼                                           │
  MovieService.create(...)                    Movie (ya con su id)
  │  regla: ¿existe el director 1?            ▲
  │  new Movie(...): ¿duración > 0?           │
  ▼                                           │
  MovieRepository.save(movie) ◄─ PUERTO       │
══╪══ PERSISTENCIA ═══════════════════════════╪═════════════════════════════════════════════════
  ▼                                           │
  MovieRepositoryAdapter ◄─ ADAPTADOR         MovieEntityMapper  ◄─ entidad → dominio
  │  MovieEntityMapper: dominio → entidad     ▲
  ▼                                           │
  MovieEntity (sin id todavía)                MovieEntity (con el id generado)
  │                                           ▲
  ▼                                           │
  MovieJpaRepository.save(entity)             Hibernate guarda el id en la entidad
  │  Hibernate genera el INSERT               ▲
══╪══ BASE DE DATOS (MariaDB) ════════════════╪═════════════════════════════════════════════════
  ▼                                           │
  INSERT INTO movie (title, ...) VALUES       id generado por AUTO_INCREMENT
  │                                           ▲
  └───────── MariaDB guarda la fila ──────────┘
```

Fíjate en las dos comprobaciones:

- `@Valid` (controlador) solo comprueba el **formato** de lo que llega: que el título no esté vacío o que el año sea un número.
- El dominio comprueba las **reglas de negocio**: que el director exista y que la duración sea positiva. Si mañana los datos llegaran desde un programa de consola, estas reglas seguirían haciendo falta (la prueba de la consola de arriba).

#### Qué es cada pieza

| Pieza | Capa | Paquete | Qué es | Fase |
|---|---|---|---|---|
| `CreateMovieRequest` | Controlador | `controller/dto` | **DTO de entrada**: la forma del JSON que envía el cliente. | 17 |
| `MovieResponse` | Controlador | `controller/dto` | **DTO de salida**: la forma del JSON que devuelve la API. Decide qué campos se enseñan. | 15 |
| `MovieDtoMapper` | Controlador | `controller/mapper` | **Mapper** dominio ↔ DTO. Copia campos, calcula otros (`directorName`) y omite los que no deben salir. | 15 |
| `MovieController` | Controlador | `controller` | Recibe la petición HTTP, llama al servicio y devuelve la respuesta. Solo traduce. | 16 |
| `MovieService` | Dominio | `domain/service` | **Servicio** o caso de uso: coordina la operación y aplica las reglas. | 16 |
| `Movie` | Dominio | `domain/model` | **Modelo de dominio**: la película del negocio, en Java puro. | 10 |
| `MovieNotFoundException` | Dominio | `domain/exception` (provisional) | Error de negocio: "esa película no existe". | 18 |
| `MovieRepository` | Dominio | `domain/repository` | **Puerto**: interfaz con lo que el dominio necesita para guardar y leer. | 12 |
| `MovieRepositoryAdapter` | Persistencia | `persistence/adapter` | **Adaptador**: implementa el puerto usando Spring Data. En otros proyectos se llama `MovieRepositoryImpl` (mismo papel, otro nombre). | 12 |
| `MovieEntityMapper` | Persistencia | `persistence/mapper` | **Mapper** entidad ↔ dominio. | 13 |
| `MovieEntity` | Persistencia | `persistence/entity` | **Entidad JPA** (`@Entity`): un objeto que refleja una fila de la tabla `movie`. | 11 |
| `MovieJpaRepository` | Persistencia | `persistence/repository` | **Repositorio de Spring Data**: interfaz que extiende `JpaRepository`; Spring escribe sus métodos. | 12 |
| Hibernate (JPA) | Persistencia | (librería) | **ORM**: traduce entre objetos y filas y genera el SQL. | 11 |
| Driver JDBC | Persistencia | (librería) | Envía el SQL a MariaDB por la red (puerto 3307). | 7 |

Tres nombres que se confunden a menudo:

- **`Movie`, `MovieEntity` y `MovieResponse` representan la misma película, pero cada uno tiene un propósito distinto.** `MovieEntity` se parece a la tabla, `Movie` al negocio y `MovieResponse` a lo que ve el cliente. Si la tabla cambia, solo cambia `MovieEntity` (y su mapper); el JSON de la API no se entera.
- **Hay dos mappers porque hay dos fronteras:** persistencia ↔ dominio y dominio ↔ controlador.
- **"Repositorio" aparece dos veces:** `MovieRepository` es el puerto del dominio y `MovieJpaRepository` es la herramienta de Spring Data que usa el adaptador.

#### ¿Y dónde estaría el DAO?

En este proyecto **no hay DAO**: su papel lo cumplen el puerto `MovieRepository` y su adaptador. Así quedaría la misma consulta con el enfoque DAO, que es muy habitual en proyectos con JDBC y en muchos apuntes:

```
  CON REPOSITORIO (este proyecto)             CON DAO (enfoque clásico)

  MovieService            [dominio]           MovieServiceImpl        [servicio]
  │ usa la interfaz                           │ usa la interfaz
  ▼                                           ▼
  MovieRepository         [dominio]           MovieDao                [persistencia]
  │ save(Movie), findById...                  │ insert, update, delete, findById
  │ (habla de objetos de negocio)             │ (habla de filas de una tabla)
  ▼ la implementa                             ▼ la implementa
  MovieRepositoryAdapter  [persistencia]      MovieDaoImpl            [persistencia]
  │                                           │ escribe el SQL con JDBC o usa
  ▼                                           │ el EntityManager de JPA
  MovieJpaRepository (Spring Data)            ▼
  │ genera el SQL solo                        MariaDB
  ▼
  MariaDB
```

**La misma consulta con DAO, paso a paso:**

| Paso | Quién (capa) | Qué recibe | Qué hace |
|---|---|---|---|
| 1 | `MovieController` (controlador) | La URL | Saca el 3 y llama a `MovieServiceImpl` |
| 2 | `MovieServiceImpl` (servicio) | El 3 | Llama a `movieDao.findById(3)`. Ojo: `MovieDao` es una interfaz **de la persistencia** |
| 3 | `MovieDaoImpl` (persistencia) | El 3 | Escribe el `SELECT` a mano (JDBC) o lo pide al `EntityManager` de JPA |
| 4 | MariaDB | El SQL | Encuentra la fila |
| 5 | `MovieDaoImpl` (persistencia) | La fila | La convierte en `MovieEntity` y la devuelve |
| 6 | `MovieServiceImpl` (servicio) | `MovieEntity` | Suele trabajar directamente con la entidad. El servicio **conoce la persistencia** |

Compárala con la tabla de `GET /movies/3` de arriba: con Repositorio, el servicio nunca ve una `MovieEntity` ni una interfaz de la persistencia.

Las diferencias clave:

1. **Dónde está la interfaz:** el Repositorio lo define el **dominio**; el DAO, la **persistencia**. Con DAO, el servicio depende de la persistencia (justo lo que la DIP quiere evitar, sección 5).
2. **De qué habla:** el Repositorio, de objetos de negocio (`Movie`); el DAO, normalmente de filas o de entidades (`MovieEntity`).
3. **Quién escribe el SQL:** con Spring Data se genera solo; con un DAO clásico se escribe a mano (JDBC) o con el `EntityManager` de JPA.

Otra variante muy habitual en clase es dividir el servicio en **interfaz + implementación** (`MovieService` + `MovieServiceImpl`). Es compatible con los dos enfoques. Lo valoraremos al crear el servicio (fase 16).

## 9. Capas conceptuales vs. paquetes físicos

Las **capas** son una idea (responsabilidades). Los **paquetes** son carpetas de Java donde ponemos las clases. No tienen por qué coincidir uno a uno, así que hay que decidir cómo organizarlos.

| Opción | Estructura | Ventaja | Inconveniente |
|---|---|---|---|
| **Por capa** ✅ | `controller/`, `domain/`, `persistence/` | Las capas se ven a simple vista; ideal para aprenderlas | Con muchas funcionalidades, cada paquete crece mucho |
| Por funcionalidad | `movie/`, `director/`, `actor/` (cada uno con sus capas dentro) | Todo lo de "películas" junto | Las capas quedan menos visibles; repetimos la estructura tres veces |

**Elegimos por capa**: el objetivo es aprender las capas y solo tenemos tres entidades.

### Estructura objetivo

`com.example.movies` es provisional: el nombre base definitivo se decide en la fase 6.

```
com.example.movies
├── MoviesApplication.java              ← clase de arranque de Spring Boot (fase 6)
├── controller/                         ← CAPA CONTROLADOR (fases 15-18)
│   ├── dto/                              DTOs de entrada y salida
│   └── mapper/                           dominio ↔ DTO
├── domain/                             ← CAPA DOMINIO (fases 10, 12, 16) — sin Spring ni JPA
│   ├── model/                            Movie, Director, Actor
│   ├── repository/                       puertos: MovieRepository...
│   └── service/                          casos de uso: MovieService...
├── persistence/                        ← CAPA PERSISTENCIA (fases 11-13)
│   ├── entity/                           MovieEntity, DirectorEntity, ActorEntity
│   ├── repository/                       MovieJpaRepository (Spring Data)
│   ├── mapper/                           entidad ↔ dominio
│   └── adapter/                          MovieRepositoryAdapter (implementa el puerto)
└── config/                             ← ENSAMBLAJE (no es una capa, ver abajo)
```

**Importante:** esta es la estructura **a la que llegaremos**. Cada paquete se creará cuando metamos en él la primera clase que lo necesite, no antes.

### El caso especial: `config/` y las anotaciones de Spring

Spring necesita saber qué clases debe crear y conectar. Lo habitual es anotarlas (`@Service`, `@Repository`...). Pero si ponemos `@Service` en `MovieService`, el dominio **importaría Spring** y rompería nuestra regla.

| Opción | Cómo | Valoración |
|---|---|---|
| **A. Dominio puro + `config/`** (recomendada) | El dominio no lleva anotaciones. Una clase `@Configuration` en `config/` crea los objetos del dominio con `@Bean` y les pasa sus dependencias. | Cumple la regla al 100 %. Una clase más, pero hace **visible** cómo se conectan las piezas (muy útil para aprender inyección de dependencias). |
| B. Permitir `@Service` en el dominio | Anotar los servicios del dominio. | Más rápido y muy común, pero el dominio depende de Spring. |

`config/` **no es una cuarta capa**: no tiene lógica. Solo "monta" las piezas, como quien conecta los cables. Es el único paquete que conoce a todos los demás.

Las **transacciones** (`@Transactional`) plantean el mismo dilema; lo resolveremos en la fase 17, cuando haya operaciones de escritura que las necesiten.

## 10. Conceptos relacionados que veremos más adelante

Este tema explica **cómo se organizan las capas**. Los conceptos de esta sección explican **qué hay dentro de cada capa**, y se estudian a fondo en la fase en la que escribimos ese código. Aquí solo va una primera idea.

### DAO frente a Repositorio (fase 12)

Los dos patrones sirven para "acceder a los datos", pero se piensan desde lados distintos:

| | DAO (*Data Access Object*) | Repositorio |
|---|---|---|
| Se piensa desde... | La **base de datos**: normalmente un DAO por tabla. | El **dominio**: se comporta como una "colección de películas". |
| Métodos típicos | `insert`, `update`, `delete`, `findById` (CRUD sobre filas). | `save(Movie)`, `findById`, `findByDirector` (lenguaje del negocio). |
| Dónde vive la interfaz | En la persistencia. | En el **dominio** (es un puerto, ver sección 5). |

En este proyecto usamos **Repositorio**: `MovieRepository` es la interfaz que define el dominio con lo que necesita. Un DAO no es incorrecto (es muy habitual en proyectos con JDBC), pero obligaría al dominio a pensar en tablas.

### Modelo anémico frente a rico (fase 10)

- **Anémico:** las clases del dominio solo tienen datos (atributos, getters y setters). Las reglas están en los servicios. Ejemplo: `movie.setDurationMinutes(-5)` se acepta, y es el servicio quien tiene que acordarse de comprobarlo.
- **Rico:** las clases tienen datos **y** comportamiento: protegen sus propias reglas. Ejemplo: el constructor de `Movie` lanza una excepción si la duración no es positiva, así que no puede existir una película inválida.

En la fase 10 elegiremos y justificaremos el modelo para nuestro problema.

### Otros conceptos

- **Entidad JPA ≠ objeto de dominio (fase 11):** `MovieEntity` refleja la tabla; `Movie`, el negocio.
- **Mappers (fases 13 y 15):** clases que copian datos entre entidad ↔ dominio y dominio ↔ DTO.
- **DTO (fases 15 y 17):** objeto que define la forma del JSON que entra o sale de la API.

## Resumen

- **Controlador**: entrada y salida (HTTP/JSON). **Dominio**: modelo, reglas y casos de uso. **Persistencia**: guardar y recuperar.
- Las dependencias apuntan **hacia el dominio**; el dominio solo usa Java estándar.
- **DIP**: el dominio define una **interfaz** (puerto) con lo que necesita; la persistencia la **implementa** (adaptador). Así se invierte la flecha de dependencia.
- **Inyección de dependencias**: Spring crea los objetos y se los pasa por constructor; en los tests podemos pasar implementaciones falsas.
- Beneficio principal aquí: tests del dominio **sin base de datos**. Coste: más clases y mappers.
- Paquetes **por capa**: `controller`, `domain`, `persistence`, más `config` para el ensamblaje.

## Preguntas de repaso

**¿Por qué no queremos que `MovieService` importe `MovieJpaRepository`?**
Porque el dominio pasaría a depender de Spring Data y JPA: sus tests necesitarían base de datos, los problemas de JPA aparecerían en el negocio y cualquier cambio en la persistencia obligaría a tocar el dominio.

**¿Dónde se define la interfaz `MovieRepository` y quién la implementa?**
Se define en el dominio (`domain/repository`), porque es el dominio quien la necesita. La implementa `MovieRepositoryAdapter`, en la persistencia.

**¿Qué se invierte exactamente en la "inversión de dependencias"?**
La dirección de la dependencia en el código: antes el dominio importaba la persistencia; ahora la persistencia importa el dominio (implementa su interfaz). Los datos, en ejecución, siguen yendo y viniendo igual.

**¿Puede una clase de `controller` usar `MovieEntity`?**
No. El controlador solo conoce el dominio (y sus DTOs). Si usara entidades JPA, la API dependería de la estructura de la base de datos.

**¿Tiene `MovieController` un método `findById`? ¿Leer el id de la URL es lógica de negocio?**
No a las dos. El controlador **llama** a `movieService.findById(3)`, que es un método del dominio. Leer el `3` de `/movies/3` es traducción de HTTP a Java: en un programa de consola no habría URL. Prueba: si algo seguiría siendo verdad sin HTTP, va en el dominio; si no, va en el controlador.

**Si la película no existe, ¿quién decide que es un error y quién decide que es un 404?**
El dominio decide que es un error (*película no encontrada*). El controlador lo traduce a HTTP como 404 Not Found.

**¿Es `config/` una capa?**
No: no contiene lógica. Solo crea y conecta los objetos de las capas.
