# 10 — Modelo de dominio: anémico o rico

> **Fase del plan:** 10 (modelo de dominio). Diario: [10-modelo-de-dominio.md](../10-modelo-de-dominio.md).
>
> **Actualizado el 2026-11-04:** el profesor no quiere *value objects* (D-46 revocada) y el proyecto pasó de JPA a JDBC con DAO (D-48). El dominio usa **tipos simples**; las reglas de entrada están en el DTO con Jakarta Validation. Lo antiguo queda marcado *(histórico)*.
>
> **Cubre:** anémico frente a rico, invariantes, entidades y relaciones, y entidad frente a DTO.

## 1. Qué es el modelo de dominio

El **modelo de dominio** son las clases que representan los conceptos del negocio y sus reglas. En este proyecto: `Movie`, `Director` y `Actor`.

No es lo mismo que:

| | Modelo de dominio | Tabla de la base | DTO |
|---|---|---|---|
| Representa | El concepto del negocio | Cómo se guarda | Cómo viaja por la API |
| Ejemplo | `Movie` con su `Director` | `movie` con `director_id` | `MovieResponse` con `director.fullName` |
| Cambia por | Una regla de negocio | Una migración | Un cambio del contrato |

El modelo de dominio no debe estar diseñado "a imagen" de la tabla: primero se piensa el negocio y después la persistencia se adapta. En este proyecto las clases de dominio son Java puro: no llevan anotaciones de persistencia. El SQL vive aparte, en la capa `persistence` (D-48). *(Histórico: D-29 las habría convertido en entidades JPA, con constructor vacío y campos no `final`; esa idea quedó superada.)*

> **El modelo de dominio no decide la forma del proyecto.** Son ejes distintos: cuántas aplicaciones se despliegan, cómo se organiza el código por dentro y qué dependencias se permiten entre capas.

### ¿Monolito normal o monolito modular?

Este proyecto es **hoy** un **monolito clásico en capas**, lo que se suele llamar «monolito normal». La modularización está planificada en la **fase 25** (D-45), no descartada.

| Rasgo | En este proyecto |
|---|---|
| Artefactos desplegables | **Uno**: el `.jar` de Spring Boot `movies` |
| Módulos Maven | **Uno**: el `pom.xml` de la raíz, sin `<module>` |
| Base de datos | **Una** instancia MariaDB con el esquema `movies` |
| Organización interna | Paquetes por **capa técnica** (`controller`, `domain`, `persistence`) con DIP (D-18) |
| Microservicios | No |
| Módulos de negocio | **En la fase 25** (paquetes `api`/`internal`, ArchUnit y Spring Modulith) |

**Todavía no es un monolito modular.** Un monolito modular también se despliega como una sola aplicación, pero se organiza por **módulos de negocio** (`catalog`, `billing`...) con fronteras explícitas: cada módulo tiene sus datos y habla con los demás por interfaces o eventos, y hay herramientas que impiden las dependencias cruzadas. Aquí las capas son **técnicas**, no de negocio, y el modelo de dominio lo comparte toda la aplicación. Organizar por capas **no** convierte el proyecto en modular.

El profesor lo plantea al revés: su [tema 01](https://cesguiro.es/doku.php/clase/daw/dws/1eval/arquitecturas) toma como referencia una arquitectura por capas desplegada como **monolito modular**, y su [tema 04](https://cesguiro.es/doku.php/clase/daw/dws/1eval/modulith) desarrolla los módulos y su verificación. Nuestro camino llega al mismo sitio en la fase 25; hasta entonces, la revisión de la fase 20.1 es manual (que el dominio no importe web ni persistencia), no automática.

## 2. Modelo anémico frente a modelo rico

### Anémico

La clase solo tiene datos y accesores:

```java
public class Movie {
    private String title;
    private int durationMinutes;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public void setDurationMinutes(int minutes) { this.durationMinutes = minutes; }
}
```

Cualquier código puede hacer:

```java
Movie movie = new Movie();
movie.setTitle("");
movie.setDurationMinutes(-5);   // la película "existe", pero es imposible
```

Las reglas se comprueban en otra capa: el servicio, el controlador o el formulario. Es el estilo más extendido en aplicaciones CRUD y el que suele salir de un tutorial.

### Rico

La clase guarda los datos **y** hace cumplir sus reglas:

```java
public class Movie {
    private final String title;
    private final int durationMinutes;

    public Movie(String title, int durationMinutes) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor que cero");
        }
        this.title = title.trim();
        this.durationMinutes = durationMinutes;
    }

    // Sin setters
}
```

Ahora `new Movie("", -5)` falla en el acto. No existe un objeto `Movie` inválido.

> En este proyecto los campos tampoco son `final` (hay dos constructores y el `id` se asigna en el segundo), pero no hay setters. La idea es la misma: el único camino normal para crear el objeto es el constructor.

### Comparación

| Criterio | Anémico | Rico |
|---|---|---|
| Dónde están las reglas | Repartidas por los servicios | En la clase |
| Objetos inválidos | Posibles | Imposibles por el constructor |
| Cuándo se descubre el error | Al guardar o al leer | Al crear el objeto |
| Código de la clase | Muy corto | Más largo |
| Encaja con frameworks | Muy bien | Bien |
| Cuándo tiene sentido | Dominio trivial, solo CRUD, reglas que dependen de la base | Reglas estables que deben cumplirse siempre |

No es una guerra religiosa: un DTO o una clase de configuración son anémicos por naturaleza. La pregunta es si **el concepto del negocio** tiene reglas que merezca la pena proteger.

## 3. Invariantes: "válido por construcción"

Un **invariante** es una condición que se cumple durante toda la vida del objeto. Ejemplos de este proyecto:

| Invariante | Dónde se comprueba |
|---|---|
| Una película tiene título | Constructor de `Movie` (no `null`) + `@NotBlank` en `MovieRequest` + `title NOT NULL` |
| La duración es mayor que cero | `@Positive` en `MovieRequest` + `chk_movie_duration_positive` |
| El año no es anterior a 1888 | `@Min(1888)` en `MovieRequest` + `chk_movie_release_year` |
| Toda película tiene director | Constructor + `director_id NOT NULL` + FK |
| Una persona tiene nombre, apellido y fecha | Constructores (no `null`) + `NOT NULL` |
| La fecha de nacimiento no es futura | No se comprueba hoy (pendiente; la base no puede con un `CHECK` estable) |

"Válido por construcción" significa que **no hay forma de obtener un objeto que viole el invariante** usando la API pública de la clase: el constructor lo comprueba y no hay setters que lo rompan después. Con tipos simples esto se cumple para lo que el constructor comprueba (que no falte nada); el resto de reglas se defienden en el DTO y en la base (sección 5).

### No confundir validación de entrada con invariante

| | Validación de entrada | Invariante del dominio |
|---|---|---|
| Qué comprueba | Que el JSON tiene la forma esperada | Que el dato tiene sentido para el negocio |
| Dónde | DTO de entrada (`@NotBlank`, `@Positive`) con Jakarta Validation | Constructor de la clase |
| Mensaje | "El campo título es obligatorio" | "El título no puede estar vacío" |
| ¿Se puede saltar? | Sí: otro cliente de la API | No: no hay objeto sin pasar por el constructor |

Las dos existen y se complementan. Jakarta Validation no sustituye al constructor, y el constructor no sabe nada de JSON.

## 4. Lo que el modelo rico no resuelve

El modelo rico **no** lo puede todo:

- **Reglas que necesitan la base:** "el director con id 7 existe", "no hay dos películas con el mismo título". Necesitan el repositorio y el servicio (`MovieServiceImpl`).
- **Reglas entre varios objetos:** "un actor no puede estar dos veces en la misma película" se puede evitar en `Movie.addActor`, pero la garantía definitiva es la PK compuesta de la base (D-14).
- **Identidad y `equals`/`hashCode`:** la identidad real es el `id` que asigna la base. Un objeto recién creado no lo tiene. Por eso no se implementan todavía.
- **Persistencia:** guardar y recuperar es trabajo del repositorio y el DAO JDBC, no del dominio.
- **Concurrencia:** dos usuarios cambiando la misma fila a la vez. Se verá más adelante.


## 5. La decisión de este proyecto (D-44, con D-46 revocada)

> **Cambio de rumbo (2026-11-04).** Este tema hablaba de *value objects* (D-46). El profesor **no los quiere** en este proyecto y la decisión se revocó. Lo que queda aquí es lo que **sigue siendo cierto**; lo antiguo está marcado como *(histórico)*.

**Modelo rico moderado con tipos simples**, elegido por el alumno (D-44):

- Campos con tipos de siempre: `String`, `int`, `LocalDate`, `Long`.
- **Constructor que comprueba lo que no puede faltar** (por ejemplo, que el título y el director no sean `null`).
- **Sin setters.**
- `fullName()` calculado (no se guarda).
- Biografía opcional: `null` cuando no hay (D-37).
- **Sin** `equals`/`hashCode` todavía y **sin** servicios de dominio.

El "moderado" es importante: no se convierte el dominio en una jaula. Si hace falta cambiar un dato, se añadirá un método con nombre de negocio (`changeDuration`, `assignDirector`...), no un setter genérico.

### Dónde viven ahora las reglas

Antes (histórico) cada regla vivía en un *value object*: `Title` comprobaba que el título no estuviera en blanco, `ReleaseYear` que el año fuera 1888 o más, etc. Ahora las reglas de **entrada** viven en el DTO de entrada, con **Jakarta Validation**:

```java
public record MovieRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull @Min(1888) Integer releaseYear,
        @NotNull @Positive Integer duration,
        @NotNull @Positive Long directorId) { }
```

Piensa en la **puerta de un edificio**: el DTO es el portero que revisa a cada visitante (la petición HTTP). Dentro, la entidad solo se asegura de lo mínimo: que nadie entre sin lo imprescindible.

| Regla | Dónde se comprueba | Última defensa |
|---|---|---|
| Título no en blanco, hasta 255 | `@NotBlank` + `@Size` en `MovieRequest` | `title VARCHAR(255) NOT NULL` |
| Año desde 1888 | `@Min(1888)` en `MovieRequest` | `chk_movie_release_year` |
| Duración positiva | `@Positive` en `MovieRequest` | `chk_movie_duration_positive` |
| El director no puede faltar | `@NotNull` en el DTO **y** constructor de `Movie` | `director_id NOT NULL` + FK |
| Nombre, apellido y fecha de una persona | Constructor de `Director` y `Actor` | `NOT NULL` |

> **Matiz honesto.** El constructor de `Movie` **no** comprueba el año ni la duración: son `int`, y esas reglas se comprueban en `MovieRequest` y, por detrás, en los `CHECK` de la base. Por tanto, `new Movie("Alien", -5, -5, director)` compila y se ejecuta. Es el precio de usar tipos simples: la protección está en la puerta (el DTO) y en el último muro (la base), no en cada objeto. Con *value objects* sí era imposible, y esa fue su ventaja *(histórico)*.

### Los *value objects* (histórico, D-46 revocada)

Un **value object** es un tipo pequeño, inmutable y comparado por valor que representa un concepto del negocio (`Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate`, `Biography`). Se diseñaron y se escribieron con TDD en los pasos 10.2 y 10.3, pero **se eliminaron** en el giro del 2026-11-04. Conviene conocer la idea (es un patrón habitual), pero **no forman parte del código actual**. Qué se hizo y por qué se quitó está en el [diario 10, sección «Giro del 2026-11-04»](../10-modelo-de-dominio.md#giro-del-2026-11-04).

### Cómo se comparan las dos opciones

| | Tipos simples (hoy) | *Value objects* (histórico) |
|---|---|---|
| Campo título | `String title` | `Title title` |
| Dónde se valida el formato | DTO (`MovieRequest`) | Constructor de cada tipo |
| Objeto inválido posible | Sí, si alguien salta el DTO | No |
| Código | Poco | Más clases y más tests |
| Persistencia | `JdbcTemplate` lee y escribe tipos normales | Habría hecho falta convertir cada tipo |
| Lo que pide el profesor | Sí | No |

### ¿Qué sería un modelo rico "completo"?

"Moderado" no es un término de manual: es la etiqueta de este proyecto para decir "rico en lo que el dominio necesita hoy, sin toda la infraestructura de DDD". Un modelo rico completo añadiría:

| Pieza | Qué es | ¿En el proyecto? |
|---|---|---|
| Factorías | `Movie.create(...)` en vez del constructor | No |
| Servicios de dominio | Reglas que cruzan varios objetos ("el director existe") | Lo hace `MovieServiceImpl` con el repositorio |
| Eventos de dominio | Avisos de "película creada" | No (fase 26 opcional) |
| Agregados | `Movie` como raíz y la participación como entidad con identidad | Fase opcional 23 |
| `equals`/`hashCode` por `id` | Igualdad de entidad | No todavía: el `id` es `null` hasta guardar |

La regla práctica: **cada pieza se añade cuando resuelve un problema real**, no porque suene a arquitectura avanzada.

### El dominio y la persistencia hoy (D-48)

El dominio ya no lleva anotaciones de persistencia: `Movie`, `Director` y `Actor` son Java puro. La persistencia queda fuera, detrás de un **puerto**:

```
MovieController  →  MovieService  →  MovieRepository (puerto, en domain)
                                            ↑ implementa
                                     MovieRepositoryImpl  →  MovieDao  ←  MovieDaoJdbc (JdbcTemplate)
```

- `MovieRepository` (interfaz) vive en `domain/repository`: el dominio dice qué necesita.
- `MovieRepositoryImpl` y `MovieDaoJdbc` viven en `persistence`: ejecutan el SQL con `JdbcTemplate`.
- Las clases del dominio **no** conocen ni la tabla ni el SQL.

(Antes de este giro, D-29 decidía que las clases del dominio fueran también entidades JPA; esa decisión quedó superada. Ver el tema 11 y el diario.)

## 6. Cómo sigue la fase

| Paso | Qué añadió | Estado actual |
|---|---|---|
| 10.2 | Diseño de seis *value objects* | Superado (histórico) |
| 10.3 | Los seis `record` con TDD | Superado: se eliminaron |
| 10.4 | `Director`, `Actor` y `Movie` | Vigente, **reescritas con tipos simples** |

### Las entidades (paso 10.4)

Las tres clases son **entidades**: su identidad es el `id` que asigna la base de datos, no sus campos. Por eso son clases normales (no `record`) y todavía no tienen `equals`/`hashCode`.

```java
public class Director {
	private Long id;
	private String firstName;
	private String lastName;
	private LocalDate birthDate;
	private String biography;

	public Director(String firstName, String lastName, LocalDate birthDate, String biography) {
		if (firstName == null) throw new IllegalArgumentException("El nombre del director es obligatorio");
		// ... apellido y fecha igual; biography puede ser null (D-37)
	}

	/** Reconstrucción desde la base de datos: con el id. */
	public Director(Long id, String firstName, String lastName, LocalDate birthDate, String biography) {
		this(firstName, lastName, birthDate, biography);
		this.id = id;
	}

	public String fullName() {
		return firstName + " " + lastName;   // calculado: no se guarda repetido
	}
}
```

Hay **dos constructores**: uno para crear un director nuevo (sin `id`, lo asignará la base) y otro para reconstruirlo desde una fila (con `id`). Piensa en un carné: al rellenarlo no tiene número; el número lo pone la oficina al registrarlo.

`Movie` añade las dos relaciones, **unidireccionales**:

- `Director director`: muchas películas pueden tener el mismo director; la película conoce al director, no al revés.
- `List<Actor> actors`: el reparto; `getActors()` devuelve una copia inmutable y solo se modifica con `addActor`.

Sin `equals` todavía, `addActor` no detecta duplicados: la garantía es la PK compuesta de `movie_actor` (D-14).

#### Un ejemplo completo: crear *Inception*

```java
Director nolan = new Director("Christopher", "Nolan",
        LocalDate.of(1970, 7, 30),
        "Director, guionista y productor británico-estadounidense.");

Actor dicaprio = new Actor("Leonardo", "DiCaprio",
        LocalDate.of(1974, 11, 11),
        null);                       // sin biografía: es válido

Movie inception = new Movie("Inception", 2010, 148, nolan);
inception.addActor(dicaprio);
```

- El **director es obligatorio**: el constructor de `Movie` lanza `IllegalArgumentException` si es `null`.
- Los **actores son opcionales**: una película puede empezar con el reparto vacío.
- "Creado antes" significa **existente en memoria**, no guardado. Que el director exista en la base lo comprueba el servicio (con `findDirectorById`) y lo garantiza la FK.

#### Entidad frente a DTO

| | Entidad (`Movie`) | DTO (`MovieRequest`, `MovieResponse`) |
|---|---|---|
| Representa | Una **cosa del negocio con identidad** | Un **mensaje** que viaja por la API |
| Dónde vive | `domain/model` | `controller/dto` |
| Reglas | Lo mínimo que no puede faltar | Validación de entrada (Jakarta Validation) en `MovieRequest` |
| Forma | Clase con getters | `record` |
| Referencias | `Movie` guarda un `Director` entero | `MovieRequest` solo trae `directorId` |
| Cambia por | Una regla de negocio | Un cambio del contrato de la API |

El `MovieMapper` (en `controller/mapper`) traduce entre ambos. La entidad no sale nunca por HTTP tal cual.

#### Reflexión y constructor vacío (histórico)

Con JPA, Hibernate creaba la entidad con un constructor vacío `protected` y escribía los campos por **reflexión** (la capacidad de Java de leer y escribir campos privados en tiempo de ejecución). Por eso la versión con JPA tenía `protected Director() { }` y campos no `final`.

**Ya no aplica.** Con JDBC, la fila se convierte en objeto con un `RowMapper` que llama al constructor normal (el que lleva `id`). No hay constructor vacío, ni reflexión, ni Hibernate. Esto es una ventaja: al leer de la base, **sí** se ejecutan las comprobaciones del constructor.

## Lo que tienes que recordar

1. **Modelo de dominio ≠ tabla ≠ DTO.** Cada uno responde a una pregunta distinta.
2. **Anémico** = datos + setters, reglas fuera. **Rico** = datos + reglas dentro.
3. Un **invariante** es una condición que siempre se cumple. Con tipos simples, el constructor garantiza lo mínimo (que no falte nada) y el resto se defiende en dos sitios: el DTO y la base.
4. **Validación de entrada** (DTO, Jakarta Validation) y **regla de dominio** (constructor) son cosas distintas y se complementan.
5. El modelo rico **no** sustituye a la base ni al repositorio: las reglas que necesitan datos externos van en el servicio.
6. En este proyecto: rico moderado, tipos simples, sin setters, sin `equals` todavía y **sin JPA** (JDBC con DAO, D-48).
7. **Los *value objects* se probaron y se retiraron** (D-46 revocada): es un patrón válido, pero no forma parte del proyecto.

## Preguntas de repaso

**1. ¿Qué diferencia hay entre un modelo anémico y uno rico?**

<details><summary>Respuesta</summary>

El anémico solo tiene campos, getters y setters: las reglas viven en los servicios. El rico tiene un constructor que comprueba lo que no puede faltar (y comportamiento), de modo que no se puede crear un objeto sin lo imprescindible. En el proyecto se eligió el rico moderado (D-44).
</details>

**2. ¿Qué es un invariante? Pon un ejemplo del proyecto.**

<details><summary>Respuesta</summary>

Una condición que se cumple durante toda la vida del objeto. Ejemplo: toda película tiene director; el constructor de `Movie` lanza `IllegalArgumentException` si es `null`, y la base lo respalda con `director_id NOT NULL` y la FK.
</details>

**3. ¿Por qué no hay setters en `Movie`?**

<details><summary>Respuesta</summary>

Porque un setter permitiría romper los invariantes después de crear el objeto (por ejemplo, poner el director a `null`). Para cambiar un dato se creará un método con nombre de negocio, o un objeto nuevo.
</details>

**4. Si el DTO ya valida, ¿para qué sirve el `CHECK` de la base?**

<details><summary>Respuesta</summary>

Porque la base es la última barrera: los datos pueden llegar por SQL, por un script o por otra aplicación. `@Positive` en `MovieRequest` protege la entrada de la API; `chk_movie_duration_positive` protege la tabla. Son dos defensas para la misma regla.
</details>

**5. ¿Dónde se comprueba que el director de una película existe?**

<details><summary>Respuesta</summary>

En el constructor no se puede: haría falta consultar la base. Lo comprueba el servicio, que llama a `MovieRepository.findDirectorById` y lanza `DirectorNotFoundException` si no existe; la FK `fk_movie_director` es la garantía final.
</details>

**6. ¿Por qué no se implementan `equals` y `hashCode` todavía?**

<details><summary>Respuesta</summary>

Porque la identidad de una entidad es su `id`, que asigna la base al guardar. Dos objetos recién creados tienen `id == null`; compararlos por `id` los haría "iguales" a todos, y compararlos por campos haría que dos personas distintas con el mismo nombre fueran la misma. Se decidirá cuando haga falta.
</details>

**7. ¿Dónde se valida la entrada: en el dominio o en el DTO?**

<details><summary>Respuesta</summary>

En el DTO de entrada (`MovieRequest`) con Jakarta Validation (`@NotBlank`, `@Min`, `@Positive`...): comprueba la forma de la petición antes de llegar al dominio y produce el error HTTP. El dominio solo conserva comprobaciones defensivas de lo que no puede faltar.
</details>

**8. Con tipos simples, ¿se puede crear `new Movie("Alien", -5, -5, director)`? ¿Qué lo impide en la práctica?**

<details><summary>Respuesta</summary>

Sí, el constructor no comprueba el año ni la duración. En la práctica lo impiden dos barreras: `MovieRequest` rechaza esos valores antes de que lleguen al dominio y los `CHECK` de la base rechazan la fila. Es el coste de no usar *value objects*.
</details>

**9. ¿Por qué `Movie` no se compara por sus campos?**

<details><summary>Respuesta</summary>

Porque es una **entidad**: su identidad es el `id` que asigna la base, no sus campos. Dos películas con el mismo título y director siguen siendo dos filas distintas.
</details>

**10. ¿Qué es un *value object* y por qué ya no está en el proyecto?**

<details><summary>Respuesta</summary>

Un tipo pequeño, inmutable y comparado por valor que protege su regla al construirse (por ejemplo, un `Title` que no admite texto en blanco). Se diseñó en la fase 10 (D-46), pero el profesor no lo quiere en este proyecto y la decisión se revocó el 2026-11-04: las reglas de entrada pasaron a Jakarta Validation en los DTO.
</details>

**11. ¿Por qué `Movie` tiene dos constructores?**

<details><summary>Respuesta</summary>

Uno sin `id`, para crear una película nueva (el `id` lo asignará la base), y otro con `id`, para reconstruirla desde una fila. El segundo delega en el primero, así las comprobaciones se escriben una sola vez.
</details>

**12. ¿Dónde acaba el SQL, y qué sabe el dominio de él?**

<details><summary>Respuesta</summary>

El SQL está en `MovieDaoJdbc` (capa `persistence`, con `JdbcTemplate`). El dominio solo conoce la interfaz `MovieRepository` (el puerto) y no sabe nada de tablas ni de SQL. Eso es la inversión de dependencias: el dominio define lo que necesita y la persistencia lo implementa.
</details>

**13. ¿Qué diferencia hay entre una entidad y un DTO?**

<details><summary>Respuesta</summary>

La entidad (`Movie`) representa una cosa del negocio con identidad y vive en el dominio; el DTO (`MovieRequest`/`MovieResponse`) es el mensaje que viaja por la API y vive en el controlador. `MovieMapper` traduce entre ambos. Por ejemplo, el `MovieRequest` trae solo `directorId`, mientras que `Movie` guarda el `Director` completo.
</details>
