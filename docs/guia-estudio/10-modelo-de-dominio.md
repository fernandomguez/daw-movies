# Fase 10 — Modelo de dominio

> Estado: **en curso** (empezada el 2026-10-28). Conceptos: [temario/10-modelo-de-dominio.md](temario/10-modelo-de-dominio.md).

## En pocas palabras

> Este archivo es el **diario** de la fase. Para **entender** los conceptos, lee primero el [tema 10](temario/10-modelo-de-dominio.md), que tiene también las preguntas de repaso.

Hasta ahora todo era infraestructura: Docker, conexión, Flyway, datos. En esta fase aparece el **negocio** en Java: las clases `Movie`, `Director` y `Actor`, escritas **sin JPA todavía** (las anotaciones llegan en la fase 11, D-29). El primer paso es decidir si esas clases solo guardan datos o también hacen cumplir las reglas; el segundo, decidir **con qué tipos** se representan esos datos.

## Objetivo

1. Decidir si el modelo es **anémico** (solo datos) o **rico** (datos + reglas).
2. Representar los valores del negocio con **tipos propios** (*value objects*) que no puedan existir inválidos.
3. Escribir las tres clases pensando en el negocio, no en las tablas.
4. Decidir cómo se representan las relaciones en Java y en qué dirección.
5. Probar las reglas con tests unitarios.

## Pasos

| Paso | Estado |
|---|---|
| 10.1 Modelo anémico vs. rico | ✅ Hecho (D-44: modelo rico moderado) |
| 10.2 *Value objects*: qué son y cuáles necesitamos | ✅ Hecho (D-46: diseño de los seis tipos) |
| 10.3 *Value objects* con TDD | ⏳ Pendiente |
| 10.4 Entidades y relaciones | ⏳ Pendiente |

## 10.1 Modelo anémico frente a modelo rico

### Las dos opciones

| | Anémico | Rico |
|---|---|---|
| Qué contiene la clase | Campos + getters + setters | Campos + constructor con reglas + comportamiento |
| Dónde se valida | En el servicio o el controlador | En la propia clase |
| ¿Se puede crear un objeto inválido? | Sí: `new Movie()` y `setDurationMinutes(-5)` | No: el constructor lo impide |
| Ejemplo | `movie.setTitle("")` y alguien decide después si vale | `new Movie("", ...)` lanza una excepción |

### Por qué elegimos el rico (D-44)

- Las reglas ya están escritas en el esquema (`NOT NULL`, `CHECK`, FK). Ponerlas también en el constructor es la única forma de que un objeto Java **nazca válido**.
- Con setters, cualquier capa puede dejar la película en un estado imposible y el error aparece mucho después, al guardar.
- El modelo es pequeño (tres clases): no hace falta una arquitectura de servicios enorme para que las reglas estén en su sitio.
- La opción intermedia (constructor que valida **y** setters que vuelven a validar) multiplica los caminos para cambiar el estado. La escritura llega en la fase 17; hasta entonces no la necesitamos.

### Lo que NO incluye la decisión

Para no adelantar fases:

- **Sin setters.** Para cambiar un dato se creará otro objeto o se añadirá un método con nombre de negocio (fase 17). JPA podrá rellenar los campos por reflexión.
- **Sin `equals`/`hashCode` todavía.** La identidad real es el `id` de la base, que aún no existe al crear el objeto; añadirlos ahora con `id == null` es una trampa clásica. Se decidirá en la fase 11/14 si JPA lo exige.
- **Sin Bean Validation** (`@NotBlank`, `@Positive`): D-29 la deja para los DTOs de entrada (fase 18). En el dominio se valida en el constructor.
- **Sin servicios de dominio todavía:** las reglas que necesitan varios objetos o la base (por ejemplo, "el director existe") llegarán con el repositorio (fase 12) y el servicio (fase 16).

### Consecuencia para el paso 10.4

Las clases tendrán que ser compatibles con JPA sin tener aún anotaciones (D-29):

- un **constructor sin argumentos**, `protected`, para que solo lo use la persistencia;
- campos **no `final`**, porque JPA los asigna por reflexión.

Es el coste que ya se aceptó en D-29: el dominio se parece un poco más a la tabla. A cambio, no habrá una `MovieEntity` separada ni un mapper entre entidad y dominio.

## 10.2 *Value objects*: qué son y cuáles necesitamos

### El problema que resuelven: *primitive obsession*

Con la decisión del 10.1, el constructor de `Movie` tendría esta pinta:

```java
public Movie(String title, int releaseYear, int durationMinutes, Director director) {
    if (title == null || title.isBlank()) throw new IllegalArgumentException("...");
    if (title.length() > 255) throw new IllegalArgumentException("...");
    if (releaseYear < 1888) throw new IllegalArgumentException("...");
    if (durationMinutes <= 0) throw new IllegalArgumentException("...");
    if (director == null) throw new IllegalArgumentException("...");
    ...
}
```

Funciona, pero tiene tres problemas:

1. **La regla viaja con el programador, no con el dato.** Cualquier otro sitio que reciba un título (el DTO de entrada, un mapper, un test, el código de la fase 17) tiene que repetir las mismas comprobaciones.
2. **El compilador no ayuda.** `String`, `int` y `LocalDate` sirven para todo: nada impide pasar el apellido del director donde va el título. Los dos son `String`.
3. **El objeto válido depende de por dónde entres.** Si un día se construye un `Movie` por otra vía (un importador, un test, JPA), las comprobaciones se pueden olvidar.

Eso es la *primitive obsession*: usar tipos genéricos para conceptos concretos del negocio.

### La solución: un tipo por concepto

Un ***value object*** es un tipo pequeño, **inmutable**, que representa un concepto del negocio y **se compara por su valor**. Su constructor es la única puerta de entrada, así que la regla se cumple siempre, venga el dato de donde venga.

```java
public record Title(String value) {
    public Title {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        value = value.trim();
        if (value.length() > 255) {
            throw new IllegalArgumentException("El título no puede superar 255 caracteres");
        }
    }
}
```

Con eso, `new Title("")` lanza una excepción y **no existe** ningún `Title` vacío en el programa. La regla está escrita una vez y el parámetro del constructor ya dice qué es cada cosa:

```java
public Movie(Title title, ReleaseYear releaseYear, DurationMinutes duration, Director director)
```

Un `String` no puede colarse en el hueco de un `Title`: el compilador lo impide.

### Los seis tipos del proyecto

Las reglas **salen del esquema que ya existe** (fase 3, migraciones `V1`–`V4`), no se inventan:

| Tipo | Firma | Regla que garantiza | De dónde sale |
|---|---|---|---|
| `Title` | `record Title(String value)` | no `null`, no vacío, ≤ 255 tras `trim` | `movie.title VARCHAR(255) NOT NULL` |
| `ReleaseYear` | `record ReleaseYear(int value)` | ≥ 1888 | `chk_movie_release_year` |
| `DurationMinutes` | `record DurationMinutes(int value)` | > 0 | `chk_movie_duration_positive` |
| `PersonName` | `record PersonName(String firstName, String lastName)` | no `null`, no vacíos, ≤ 100 cada uno tras `trim`; `fullName()` | `first_name`/`last_name VARCHAR(100) NOT NULL` |
| `BirthDate` | `record BirthDate(LocalDate value)` | no `null`, no futura | `birth_date DATE NOT NULL` |
| `Biography` | `record Biography(String value)` | no `null`, no vacía tras `trim`; la ausencia es `null` | `biography TEXT NULL` (D-37) |

`PersonName` es el único con dos componentes, porque en la base son dos columnas. El nombre completo no se guarda: se calcula.

```java
public record PersonName(String firstName, String lastName) {
    public PersonName {
        firstName = requireText(firstName, "El nombre", 100);
        lastName = requireText(lastName, "Los apellidos", 100);
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
```

`requireText` es un método privado estático del propio `record` que recorta, comprueba que no quede vacío y mide; se escribe en el paso 10.3, junto con sus tests. Aquí solo se muestra la forma.

### Decisiones de diseño (y por qué)

| Decisión | Motivo |
|---|---|
| `record`, no clase con getters | Inmutable por construcción, `equals`/`hashCode` por valor gratis y menos código que mantener. Sin setters no hay forma de romper la regla después. |
| Validar en el **constructor compacto** | Se ejecuta en toda creación: `new`, un mapper, un test, y JPA al hidratar (fase 11). Es la única forma de que la regla no tenga puertas traseras. |
| `IllegalArgumentException` para todo valor inválido, incluido `null` | Un solo tipo de excepción, predecible, que la fase 18 traducirá a un `400`. Un `NullPointerException` se confundiría con un fallo de programación. |
| `trim()` **antes** de medir | `"  Alien  "` es un título válido de 5 letras, no de 9. Además evita que un título de solo espacios pase el `isBlank`. |
| `ReleaseYear` sin tope superior | El esquema solo pone el mínimo. Una película anunciada para el año que viene es un dato legítimo. |
| `DurationMinutes` sin tope superior | El esquema no lo tiene. Inventar un máximo (por ejemplo 600) rechazaría datos válidos sin respaldo. |
| `BirthDate` no futura | Única regla que **no** está en el esquema, y se añade porque una fecha de nacimiento futura es imposible. Usa `LocalDate.now()`, así que los tests trabajan con fechas relativas (`now().plusDays(1)`), no fijas. |
| `Biography` rechaza el texto en blanco; la ausencia es `null` | En la columna, "sin biografía" es `NULL` (D-37). Un `Biography` con valor `null` sería un objeto que representa la nada: mejor `null` en el campo. Convertir un texto en blanco a `null` es **normalización de entrada** y se hace en el mapper (fase 17) y en el DTO (fase 18). |
| Un paquete, `com.daw.movies.domain.model` | Nueve tipos pequeños no justifican subpaquetes. Si el dominio crece, se separan con criterio. |

### Lo que estos tipos **no** hacen

- **No consultan la base.** "El director 7 existe" o "no hay dos películas con el mismo título" necesitan el repositorio (fase 12) y el servicio (fase 16).
- **No conocen JPA.** Son clases Java normales; las anotaciones llegan en la fase 11.
- **No implementan identidad.** Un `Title` se compara por su texto (correcto para un valor); una `Movie` se comparará por su `id` cuando exista (fase 11/14).
- **No sustituyen a Bean Validation.** El DTO de entrada seguirá validándose en la fase 18 para devolver un `400` con el campo culpable; el *value object* es la garantía del dominio.

### El coste: la fase 11 tendrá trabajo extra

JPA no guarda un `Title` tal cual. Habrá que decirle cómo convertirlo:

| Tipo | Mapeo probable | Coste |
|---|---|---|
| `Title`, `ReleaseYear`, `DurationMinutes`, `BirthDate` | `AttributeConverter<Tipo, String>` (o `<Tipo, Integer>`, `<Tipo, LocalDate>`): un valor → una columna | Una clase pequeña por tipo (o converters reutilizables) |
| `PersonName` | `@Embeddable` con dos columnas, o dos campos `String` en la entidad con el `PersonName` calculado | Hay que comprobar qué admite Hibernate 7 con `record`; se decide en la fase 11 |
| `Biography` | `AttributeConverter<Biography, String>`; el `NULL` de la columna sigue siendo `null` | Una clase pequeña |

Es el precio de tener tipos propios y se paga **una vez**, en la capa de persistencia. La alternativa (volver a `String`/`int` en la entidad) dejaría el dominio protegido solo a medias.

## Siguiente paso

**10.3 *Value objects* con TDD**: escribir primero los tests de los seis tipos (en rojo) y después los `record` con su constructor compacto hasta ponerlos en verde. Los tests van en inglés; los mensajes de error, en español. Después, el 10.4 crea `Director`, `Actor` y `Movie` con estos tipos, getters, `fullName()` y constructor protegido para JPA, y decide las relaciones.
