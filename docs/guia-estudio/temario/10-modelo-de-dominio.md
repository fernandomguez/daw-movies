# 10 — Modelo de dominio: anémico o rico

> **Fase del plan:** 10 (modelo de dominio). Diario: [10-modelo-de-dominio.md](../10-modelo-de-dominio.md).
>
> **En construcción:** este tema se completa con los pasos 10.2 (*value objects*), 10.3 (TDD de los tipos) y 10.4 (entidades y relaciones). Ahora mismo cubre los pasos 10.1 y 10.2.

## 1. Qué es el modelo de dominio

El **modelo de dominio** son las clases que representan los conceptos del negocio y sus reglas. En este proyecto: `Movie`, `Director` y `Actor`.

No es lo mismo que:

| | Modelo de dominio | Tabla de la base | DTO |
|---|---|---|---|
| Representa | El concepto del negocio | Cómo se guarda | Cómo viaja por la API |
| Ejemplo | `Movie` con su `Director` | `movie` con `director_id` | `MovieResponse` con `director.fullName` |
| Cambia por | Una regla de negocio | Una migración | Un cambio del contrato |

El modelo de dominio no debe estar diseñado "a imagen" de la tabla: primero se piensa el negocio y después la persistencia se adapta. En este proyecto se decidió que las clases de dominio **sean también** las entidades JPA (D-29), así que hay una concesión: JPA impondrá un constructor sin argumentos y campos no `final`.

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

> En este proyecto los campos **no** son `final` porque JPA los rellena por reflexión (D-29). La idea es la misma: el único camino "normal" para crear el objeto es el constructor que valida.

### Comparación

| Criterio | Anémico | Rico |
|---|---|---|
| Dónde están las reglas | Repartidas por los servicios | En la clase |
| Objetos inválidos | Posibles | Imposibles por el constructor |
| Cuándo se descubre el error | Al guardar o al leer | Al crear el objeto |
| Código de la clase | Muy corto | Más largo |
| Encaja con frameworks | Muy bien | Bien (JPA pone condiciones) |
| Cuándo tiene sentido | Dominio trivial, solo CRUD, reglas que dependen de la base | Reglas estables que deben cumplirse siempre |

No es una guerra religiosa: un DTO o una clase de configuración son anémicos por naturaleza. La pregunta es si **el concepto del negocio** tiene reglas que merezca la pena proteger.

## 3. Invariantes: "válido por construcción"

Un **invariante** es una condición que se cumple durante toda la vida del objeto. Ejemplos de este proyecto:

| Invariante | Dónde se comprueba |
|---|---|
| Una película tiene título | Constructor de `Movie` + `title NOT NULL` |
| La duración es mayor que cero | Constructor + `chk_movie_duration_positive` |
| El año no es anterior a 1888 | Constructor + `chk_movie_release_year` |
| Toda película tiene director | Constructor + `director_id NOT NULL` + FK |
| Una persona tiene nombre y apellidos | Constructores + `NOT NULL` |
| La fecha de nacimiento no es futura | Solo el constructor (la base no puede con un `CHECK` estable) |

"Válido por construcción" significa que **no hay forma de obtener un objeto que viole el invariante** usando la API pública de la clase: el constructor lo comprueba y no hay setters que lo rompan después.

### No confundir validación de entrada con invariante

| | Validación de entrada | Invariante del dominio |
|---|---|---|
| Qué comprueba | Que el JSON tiene la forma esperada | Que el dato tiene sentido para el negocio |
| Dónde | DTO de entrada (`@NotBlank`, `@Positive`), fase 18 | Constructor de la clase |
| Mensaje | "El campo título es obligatorio" | "El título no puede estar vacío" |
| ¿Se puede saltar? | Sí: otro cliente de la API | No: no hay objeto sin pasar por el constructor |

Las dos existen y se complementan. Bean Validation no sustituye al constructor, y el constructor no sabe nada de JSON.

## 4. Lo que el modelo rico no resuelve

El modelo rico **no** lo puede todo:

- **Reglas que necesitan la base:** "el director con id 7 existe", "no hay dos películas con el mismo título". Necesitan el repositorio (fase 12) y el servicio (fase 16).
- **Reglas entre varios objetos:** "un actor no puede estar dos veces en la misma película" se puede evitar en `Movie.addActor`, pero la garantía definitiva es la PK compuesta de la base (D-14).
- **Identidad y `equals`/`hashCode`:** la identidad real es el `id` que asigna la base. Un objeto recién creado no lo tiene. Por eso no se implementan todavía.
- **Persistencia:** guardar y recuperar es trabajo de JPA (fase 11) y del repositorio (fase 12), no del dominio.
- **Concurrencia:** dos usuarios cambiando la misma fila a la vez. Se verá más adelante.

## 5. La decisión de este proyecto (D-44 y D-46)

**Modelo rico moderado con *value objects***, elegido por el alumno y confirmado con el profesor:

- **Value objects** en lugar de primitivos: `Title`, `ReleaseYear`, `DurationMinutes`, `PersonName`, `BirthDate` y `Biography` (D-46).
- Constructor que valida las reglas conocidas.
- **Sin setters.**
- `fullName()` calculado (no se guarda), delegando en `PersonName`.
- Biografía opcional: `null` cuando no hay, y el texto en blanco se normaliza a `null` al entrar en el sistema (mapper de la fase 17, DTO de la 18).
- **Sin** `equals`/`hashCode`, **sin** Bean Validation en el dominio, **sin** servicios de dominio todavía.

El "moderado" es importante: no se convierte el dominio en una jaula. Si en la fase 17 hace falta cambiar un dato, se añadirá un método con nombre de negocio (`changeDuration`, `assignDirector`...) que vuelva a comprobar lo que tenga que comprobar. No se abrirán setters genéricos.

### Los *value objects* (D-46)

Un **value object** es un tipo pequeño, inmutable y comparado por valor, que representa un concepto del negocio y protege sus invariantes al construirse. Sustituye la *primitive obsession*: en vez de un `String` que puede ser cualquier cosa, un `Title` que solo puede ser un título válido.

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

El cuerpo del constructor compacto se ejecuta en **toda** vía de creación: `new`, un mapper, JPA al hidratar, un test. Eso es lo que las anotaciones de Jakarta Validation **no** hacen fuera de una petición HTTP.

| Value object | Regla | Columna del esquema |
|---|---|---|
| `Title` | no vacío, ≤ 255, sin espacios sobrantes | `movie.title VARCHAR(255)` |
| `ReleaseYear` | ≥ 1888 | `movie.release_year` + `CHECK` |
| `DurationMinutes` | > 0 | `movie.duration_minutes` + `CHECK` |
| `PersonName` | nombre y apellidos no vacíos, ≤ 100 cada uno; `fullName()` | `first_name`/`last_name VARCHAR(100)` |
| `BirthDate` | no nula, no futura | `birth_date DATE` |
| `Biography` | no vacía si existe; sin biografía = `null` (no un objeto vacío) | `biography TEXT NULL` (D-37) |

Las reglas **no se inventan**: son las del esquema que ya existe (fase 3) más la fecha de nacimiento no futura. Si el esquema cambia, el value object tiene que cambiar con él.

Dos detalles del diseño que se deciden en el paso 10.2 y conviene tener presentes:

- **`IllegalArgumentException` para todo valor inválido**, también para `null`. Un único tipo de excepción que la fase 18 traducirá a `400`; un `NullPointerException` se confundiría con un fallo de programación.
- **`trim()` antes de medir**: `"  Alien  "` tiene 5 letras, no 9. `ReleaseYear` y `DurationMinutes` no tienen tope superior porque el esquema no lo pone; inventarlo rechazaría datos válidos.

El diseño completo, con la justificación de cada decisión, está en el [diario 10, paso 10.2](../10-modelo-de-dominio.md#102-value-objects-qué-son-y-cuáles-necesitamos).

**Coste en la fase 11:** JPA no guarda un `Title` tal cual. Habrá que mapearlo con un `AttributeConverter` (un valor → una columna) o, en `PersonName`, con `@Embeddable` (dos columnas). Ese es el precio de tener tipos propios; se paga una vez, en la capa de persistencia.

### ¿Qué sería un modelo rico "completo"?

"Moderado" no es un término de manual: es la etiqueta de este proyecto para decir "rico en lo que el dominio necesita hoy, sin la infraestructura de DDD". Un modelo rico completo añadiría:

| Pieza | Qué es | ¿En el proyecto? |
|---|---|---|
| Factorías | `Movie.create(...)` en vez del constructor | No: el constructor ya garantiza los invariantes |
| Servicios de dominio | Reglas que cruzan varios objetos ("el director existe") | Llegan con el repositorio (12) y el servicio (16) |
| Eventos de dominio | Avisos de "película creada" para otros módulos | No hay otros módulos que escuchar (fase 26 si se hace) |
| Agregados | `Movie` como raíz y la participación como entidad con identidad | La fase opcional 23 añade el personaje a `movie_actor` |
| `equals`/`hashCode` por `id` | Igualdad de entidad | El `id` es `null` hasta guardar (fase 11/14) |
| Dominio separado de JPA | `MovieEntity` + mapper | Descartado en D-29 |

La regla práctica: **cada pieza se añade cuando resuelve un problema real**, no porque suene a arquitectura avanzada. Los *value objects* entran ahora porque el profesor los pide y porque resuelven un problema concreto: que un título inválido no pueda existir en ningún sitio.

## 6. Cómo sigue la fase

| Paso | Qué añade |
|---|---|
| 10.2 | Qué es un *value object* y diseño de los seis del dominio (D-46) |
| 10.3 | Los seis `record` con TDD: primero el test en rojo, después el constructor compacto |
| 10.4 | `Director`, `Actor` y `Movie` con los *value objects*, getters, `fullName()` y constructor protegido para JPA; relaciones unidireccionales |

Después, la fase 11 añade JPA a estas mismas clases (D-29) y mapea los *value objects*; la fase 17 usará el constructor para crear películas desde la API.

## Lo que tienes que recordar

1. **Modelo de dominio ≠ tabla ≠ DTO.** Cada uno responde a una pregunta distinta.
2. **Anémico** = datos + setters, reglas fuera. **Rico** = datos + reglas dentro.
3. Un **invariante** es una condición que siempre se cumple; "válido por construcción" significa que el constructor la garantiza y no hay setters que la rompan.
4. **Validación de entrada** (DTO, Bean Validation) y **regla de dominio** (constructor) son cosas distintas y se complementan.
5. El modelo rico **no** sustituye a la base ni al repositorio: las reglas que necesitan datos externos van en el servicio.
6. En este proyecto: rico moderado, sin setters, sin `equals` todavía y con JPA en la fase 11.
7. **Un *value object* no es un DTO ni una entidad**: representa un valor del negocio, es inmutable, se compara por valor y no tiene identidad propia.

## Preguntas de repaso

**1. ¿Qué diferencia hay entre un modelo anémico y uno rico?**

<details><summary>Respuesta</summary>

El anémico solo tiene campos, getters y setters: las reglas viven en los servicios. El rico tiene un constructor que valida (y comportamiento), de modo que no se puede crear un objeto inválido. En el proyecto se eligió el rico moderado (D-44).
</details>

**2. ¿Qué es un invariante? Pon un ejemplo del proyecto.**

<details><summary>Respuesta</summary>

Una condición que se cumple durante toda la vida del objeto. Ejemplo: `duration_minutes > 0`; el constructor de `DurationMinutes` lanza `IllegalArgumentException` si la duración es 0 o negativa, así que `Movie` nunca recibe un valor imposible, y el `CHECK` de la tabla lo impide aunque se inserte por SQL.
</details>

**3. ¿Por qué no hay setters en `Movie`?**

<details><summary>Respuesta</summary>

Porque un setter permitiría romper los invariantes después de crear el objeto (`setDurationMinutes(-5)`). Para cambiar un dato se creará un método con nombre de negocio que valide lo necesario, o un objeto nuevo (fase 17).
</details>

**4. Si el constructor ya valida, ¿para qué sirve el `CHECK` de la base?**

<details><summary>Respuesta</summary>

Porque la base es la última barrera: los datos pueden llegar por SQL, por un script o por otra aplicación. El constructor protege a los objetos Java; el `CHECK` protege a la tabla. Son dos defensas para el mismo invariante.
</details>

**5. ¿Dónde se comprueba que el director de una película existe?**

<details><summary>Respuesta</summary>

En el constructor no se puede: haría falta consultar la base. Se comprueba en el servicio (fase 16) con el repositorio (fase 12); la FK `fk_movie_director` es la garantía final. El constructor solo exige que el director no sea `null`.
</details>

**6. ¿Por qué no se implementan `equals` y `hashCode` todavía?**

<details><summary>Respuesta</summary>

Porque la identidad de una entidad es su `id`, que asigna la base al guardar. Dos objetos recién creados tienen `id == null`; compararlos por `id` los haría "iguales" a todos, y compararlos por campos haría que dos personas distintas con el mismo nombre fueran la misma. Se decidirá cuando JPA lo exija (fase 11/14).
</details>

**7. ¿Bean Validation en el dominio o en el DTO?**

<details><summary>Respuesta</summary>

En el DTO de entrada (fase 18): valida la forma de la petición antes de llegar al dominio y produce mensajes de error HTTP. El dominio valida sus invariantes en el constructor. D-29 descarta Bean Validation en el dominio.
</details>

**8. ¿Por qué `new Title("")` no se puede ni escribir, y qué gana el proyecto con eso?**

<details><summary>Respuesta</summary>

Porque el constructor compacto del `record` lanza `IllegalArgumentException` si el valor está vacío: no existe ningún `Title` inválido, venga el dato de un DTO, de un mapper, de un test o de JPA. Se gana que la regla esté escrita una vez y que el compilador distinga un título de un nombre, aunque los dos sean texto en la base (D-46).
</details>

**9. ¿Por qué `Title` se compara por su texto y `Movie` no?**

<details><summary>Respuesta</summary>

Porque el `Title` es un **valor**: dos títulos con el mismo texto son intercambiables, y `equals` lo refleja. La `Movie` es una **entidad**: su identidad es el `id` que asigna la base, no sus campos; dos películas con el mismo título y el mismo director siguen siendo dos filas distintas. Por eso `equals`/`hashCode` de la entidad se deciden en la fase 11/14 y no ahora.
</details>
