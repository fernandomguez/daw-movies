# 11 — Introducción a JPA

> **Fase del plan:** 11 (Fundamentos de JPA y entidades).
> **Estado:** introducción conceptual, explicada por adelantado en la fase 1. Actualizada el 2026-10-20: las anotaciones JPA van en las clases del dominio (`Director`, `Movie`, `Actor`), sin entidades separadas (D-29). El código de este tema era **ilustrativo**: **desde el 2026-11-01 (paso 11.3) el mapeo de `Director` y `Actor` ya existe en el proyecto** (sección 11); `Movie` llega en los pasos 11.4–11.5. **Ampliado el 2026-10-31** (sesión del paso 10.4) con el recorrido fila ↔ objeto, el reparto de responsabilidades JPA/Hibernate/Spring Boot/Spring Data, *dirty checking* y una guía de búsqueda.

## Objetivo

Entender qué problema resuelve JPA, qué diferencia hay entre JPA, Hibernate y Spring Data JPA, y por qué lo estudiamos después de SQL y del modelo de dominio.

## 1. El problema: Java y MariaDB guardan la información de forma distinta

**MariaDB** guarda los datos en **tablas** con **filas** y columnas:

```
Tabla director
| id | first_name  | last_name | birth_date |
|  1 | Christopher | Nolan     | 1970-07-30 |
|  2 | Quentin     | Tarantino | 1963-03-27 |
```

**Java** trabaja con **objetos**:

```java
Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30));
```

### Cómo sería sin JPA

Para leer un director tendríamos que hacerlo todo a mano:

1. Escribir el SQL: `SELECT id, first_name, last_name, birth_date FROM director WHERE id = 1`.
2. Ejecutarlo y recorrer el resultado columna por columna.
3. Crear el objeto con `new Director(...)` y pasarle cada valor.
4. Repetir lo mismo, al revés, para cada `INSERT` y cada `UPDATE`.

Con nuestras 4 tablas (`director`, `movie`, `actor`, `movie_actor`) ya sería mucho código repetitivo; en un proyecto con 40 tablas sería inmanejable.

## 2. La solución: un "traductor" automático (ORM)

**JPA traduce automáticamente entre filas y objetos.** Marcamos una clase Java con **anotaciones** que indican a qué tabla y columnas corresponde, y JPA genera el SQL por nosotros:

```java
@Entity                     // "esta clase representa una tabla"
@Table(name = "director")   // "en concreto, la tabla director"
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                            // la clave primaria, con AUTO_INCREMENT

    @Embedded
    private PersonName name;                    // name ↔ first_name + last_name (embeddable)

    private BirthDate birthDate;                // birth_date, vía su AttributeConverter
}
```

Al pedir "dame el director 1", JPA genera el `SELECT`, lee la fila y devuelve un objeto `Director` relleno con los datos de Nolan.<sup>*</sup>

<sup>*</sup> Pero no está completo: la clase real (sección 11) también lleva `biography` y, más abajo, la explicación de por qué `PersonName` no son dos campos `String`.

A este tipo de herramienta se le llama **ORM** (*Object-Relational Mapping*): traduce entre **objetos** y tablas **relacionales**.

## 3. JPA, Hibernate y Spring Data JPA

Son tres nombres que suelen confundirse:

| Nombre | Qué es | Analogía |
|---|---|---|
| **JPA** | Una **especificación**: reglas y anotaciones (`@Entity`, `@Id`...). Por sí sola no ejecuta nada. | Las normas de un examen |
| **Hibernate** | La **implementación**: el programa que cumple esas reglas y genera el SQL. Es el que usa Spring Boot. | El alumno que hace el examen |
| **Spring Data JPA** | Capa de Spring encima de JPA que crea automáticamente métodos habituales (`findById`, `findAll`, `save`...). | Una plantilla ya medio rellena |

En nuestro proyecto, Spring Boot 4.1.1 trae **Hibernate 7.4.5**, que implementa **JPA 3.2**.

## 4. Las relaciones de nuestro proyecto en SQL y en JPA

Las relaciones que diseñaremos en SQL (fase 3) tienen su equivalente en JPA (fase 11):

| En la base de datos | En JPA | Significado |
|---|---|---|
| `movie.director_id` es una clave foránea hacia `director.id` | `@ManyToOne` en `Movie` | Muchas películas → un director |
| Tabla intermedia `movie_actor (movie_id, actor_id)` | `@ManyToMany` + `@JoinTable` en `Movie` | Muchas películas ↔ muchos actores |

Vista previa (ilustrativa):

```java
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    private Long id;

    // Muchas películas pueden tener el mismo director (columna director_id)
    @ManyToOne
    @JoinColumn(name = "director_id")
    private Director director;

    // Relación muchos a muchos a través de la tabla intermedia movie_actor
    @ManyToMany
    @JoinTable(
        name = "movie_actor",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private List<Actor> actors;
}
```

Por ejemplo, con estos datos, cargar *Inception* daría un `Movie` cuyo `director` es Nolan y cuya lista `actors` contiene a DiCaprio y a Michael Caine.

> **Paso 11.4 hecho:** la vista previa es ya casi el código real. Las diferencias: `id` lleva también `@GeneratedValue(strategy = IDENTITY)` (el `AUTO_INCREMENT` de V3); los tres *value objects* **no llevan anotación** porque los converters del 11.3 se aplican solos (`autoApply`); `duration` lleva `@Column(name = "duration_minutes")` porque la estrategia por defecto no adivina esa palabra; y `actors` lleva **todavía** `@Transient`, porque la relación con actores se mapea en el 11.5 (ver la sección 12).

## 5. Dos reglas de este proyecto

1. **JPA no crea nuestras tablas.** Las crea **Flyway** con SQL que escribimos nosotros. JPA solo lee y escribe datos. Por eso configuraremos Hibernate para que **no modifique el esquema** (`ddl-auto` en `none` o `validate`).
2. **Las anotaciones JPA van en las clases del dominio** (D-29). `Director` es a la vez el modelo de negocio y la entidad JPA: no hay una `Director` separada ni un mapper entre las dos. Se diseña primero pensando en el negocio (fase 10) y en la fase 11 se le añaden las anotaciones. Lo que **no** sale nunca de la API es la entidad: el JSON se construye con DTOs (fase 15).

## 6. ¿Por qué JPA va en la fase 11 y no antes?

- Primero entendemos las tablas y las relaciones con **SQL puro** (fases 3, 8 y 9). Así, JPA solo será aprender a *describir en Java* algo que ya conocemos, y no "magia".
- Primero diseñamos el **dominio** (fase 10), para no acabar copiando la estructura de las tablas en nuestro modelo de negocio. Después le añadimos las anotaciones JPA, adaptando lo mínimo.

## 7. El viaje de una fila a un objeto (con nuestro diseño)

Con `Movie` anotada (fase 11) y el repositorio (fase 12), esta llamada:

```java
Movie movie = movieRepository.findById(3L).orElseThrow();
```

pone en marcha esto:

1. **Hibernate consulta el mapa interno** que construyó al arrancar a partir de las anotaciones: `Movie` ↔ tabla `movie`, `title` ↔ `movie.title`, `director` ↔ `movie.director_id`...
2. **Genera el SQL**:

```sql
SELECT id, title, release_year, duration_minutes, director_id
FROM movie WHERE id = 3;
```

3. **Crea el objeto vacío** con el constructor `protected` y **rellena los campos por reflexión**.
4. **Cada campo pasa por su converter**:

```java
movie.title = titleConverter.convertToEntityAttribute("Inception");  // new Title("Inception")
```

5. **Carga las relaciones**: el director con otro `SELECT` (o un `JOIN`, según la configuración) y, si se piden, los actores a través de `movie_actor`.
6. **Devuelve un `Movie` del dominio**, con sus *value objects* reales. No hay `MovieEntity` ni copia intermedia.

Al guardar, el camino es el inverso: el converter traduce cada valor a columna y Hibernate genera el `INSERT`/`UPDATE` con parámetros.

**La consecuencia práctica:** el objeto que sale del repositorio ya es el del dominio. Puedes llamar a `movie.getDirector().fullName()` sin mapear nada, y Hibernate "vigila" los cambios del objeto (*dirty checking*): si cambias algo dentro de una transacción, genera el `UPDATE` al cerrar sin necesidad de `save`.

## 8. Qué escribes tú y qué no se ve

Son cuatro piezas, no una:

| Pieza | Papel |
|---|---|
| **JPA** | Las reglas y las anotaciones (`@Entity`, `@Id`...) |
| **Hibernate** | El motor: lee las anotaciones, genera SQL, hidrata objetos |
| **Spring Boot** | El cableado: DataSource, EntityManagerFactory, transacciones, Flyway, Tomcat |
| **Spring Data JPA** | Los repositorios: implementa las interfaces al arrancar |

| Tú escribes | Lo hace el framework |
|---|---|
| Anotaciones en las clases | Construir el mapa clase ↔ tabla |
| Los converters | Llamarlos en cada lectura/escritura |
| La interfaz del repositorio (fase 12) | Implementarla en tiempo de ejecución |
| Servicio y controladores (fase 16) | Abrir/cerrar conexión, sesión y transacción |
| Consultas cuando no basta (fase 14) | Convertir el resultado en objetos |
| Migraciones SQL (fase 8, ya hechas) | Crear tablas: eso es **Flyway**, no Hibernate |

**Desde el paso 11.3**: `Director` y `Actor` son entidades y Hibernate ya las valida contra el esquema al arrancar (sin imprimir nada: pasa en silencio). En los logs sigue `Found 0 JPA repository interfaces` hasta la fase 12, y sigue sin haber `Movie` anotada hasta el 11.4–11.5.

## 9. Hibernate no "inyecta SQL": lo genera

Es tentador decir que Hibernate mete SQL en Java, pero no es exacto:

- **No hay cadenas SQL en el código**: hay anotaciones. Hibernate construye la sentencia en tiempo de ejecución.
- **No concatena valores**: usa *prepared statements* con parámetros (`WHERE id = ?`). Concatenar es lo que provoca SQL injection.
- **No todo es SQL**: también gestiona el *persistence context* (caché de primer nivel), el *dirty checking*, la carga perezosa y las transacciones.

Definición corta: **Hibernate es un traductor entre objetos Java y filas, que genera el SQL a partir de las anotaciones.**

## 10. Cómo verlo con tus ojos

Para ver el SQL que se genera:

```yaml
spring:
  jpa:
    show-sql: true
```

Muestra en consola cada `SELECT`, `INSERT` y `UPDATE` con sus parámetros. Es el paso 14.1 y la mejor forma de comprobar que todo esto no es magia.

## 11. El código real del paso 11.3

Lo ilustrativo de las secciones anteriores ya existe. Así queda `Director` (2026-11-01); `Actor` es igual con su tabla:

```java
@Entity
@Table(name = "director")
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT de V1
    private Long id;

    @Embedded                       // anida el @Embeddable PersonName en first_name + last_name
    private PersonName name;

    private BirthDate birthDate;    // birth_date, vía BirthDateConverter (autoApply)

    private Biography biography;    // biography TEXT NULL: null cuando no hay (D-37)
```

Tres piezas nuevas, una por tipo de problema:

**1. Un `@Embeddable` para el *value object* de dos columnas.** `PersonName` ocupa `first_name` y `last_name`, y un `AttributeConverter` solo sabe de una columna y un tipo Java. La solución: anotar el propio `record` como integrable en la entidad:

```java
@Embeddable
public record PersonName(
        @Column(name = "first_name", nullable = false, length = 100) String firstName,
        @Column(name = "last_name", nullable = false, length = 100) String lastName) {

    public PersonName {
        firstName = Texts.requireText(firstName, "El nombre", 100);
        lastName = Texts.requireText(lastName, "El apellido", 100);
    }
```

Hibernate 7 instancia el `record` por su **constructor canónico**, así que la validación de la fase 10 se ejecuta también al leer de la base. Lo comprobamos con una prueba real en el 11.3 (D-48), no con la documentación.

**2. Un `AttributeConverter` por cada *value object* de una columna.** Son cinco clases pequeñas de una misma forma. La de `Title`, por ejemplo:

```java
@Converter(autoApply = true)
public class TitleConverter implements AttributeConverter<Title, String> {

    @Override
    public String convertToDatabaseColumn(Title attribute) {
        return attribute == null ? null : attribute.value();   // Title → columna
    }

    @Override
    public Title convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Title(dbData);      // columna → Title (valida)
    }
}
```

El `autoApply = true` declara "aplica este conversor a todos los atributos de tipo `Title` de cualquier entidad": no hace falta escribir `@Convert` en `Director`, en `Actor` ni en la futura `Movie`.

**3. `@Id` + `@GeneratedValue(strategy = IDENTITY)`.** El identificador lo asigna la base de datos al insertar (el `AUTO_INCREMENT` de V1). Antes de guardar, el campo está a `null`; las pruebas existentes lo usan así. La estrategia `IDENTITY` es la que corresponde a una columna `AUTO_INCREMENT`.

## 12. El código real del paso 11.4

Así queda `Movie` después de este paso (el reparto, en 11.5):

```java
@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT de V3
    private Long id;

    private Title title;            // movie.title, vía TitleConverter (autoApply)
    private ReleaseYear releaseYear;// movie.release_year, ídem
    @Column(name = "duration_minutes")
    private DurationMinutes duration;

    @ManyToOne                      // muchas películas → un director
    @JoinColumn(name = "director_id", nullable = false)  // la FK de V3
    private Director director;

    @Transient                      // fuera del mapeo hasta el 11.5
    private List<Actor> actors = new ArrayList<>();
```

Lo nuevo para entenderlo bien:

**1. `@ManyToOne` = la FK vista desde el lado "muchos".** En la fase 3 se diseñó como una columna (`movie.director_id`); en JPA se describe como un campo cuyo tipo es **la entidad apuntada**. Hibernate genera el `SELECT` por la cadena de ids: primero lee la fila de `movie`, ve `director_id = 1` y baja a `director` por su clave primaria.

**2. `@JoinColumn` es "cómo se llama la columna aquí".** Sin ella, la columna derivaría de `movie` + `_id` (`director_id` de casualidad) o del campo + `_id` (`director_id`). Como en `duration_minutes`, preferimos escribirla: una FK con nombre explícito no se deja a la adivinanza.

**3. El lado dueño de la relación (*owning side*).** La FK física está en `movie` (V3), de modo que la relación se implementa en la entidad de la tabla que posee la columna. En unidireccionales (como aquí) no hay ambigüedad; cuando aparecieran dos direcciones (fase 14) la pregunta es cuál de las dos "escribe" la FK.

**4. `@Transient` quita del mapa lo que aún no tiene mapeo propio.** Sin esta anotación, Hibernate tomaría `actors` por un atributo persistente y el arranque fallaría al no saber convertir una `List<Actor>` en un valor de columna. En el 11.5 esta anotación desaparece y entra `@ManyToMany + @JoinTable`; la decisión de **cuándo** se carga cada relación (`LAZY`/`EAGER`) se formaliza en el 11.6.

## 13. El código real del paso 11.5

El campo `actors` de `Movie` ya no es `@Transient`; es la relación:

```java
@ManyToMany                      // N:M: película ↔ actores
@JoinTable(
        name = "movie_actor",    // la tabla intermedia de V4, no una nueva
        joinColumns = @JoinColumn(name = "movie_id"),       // la columna de ESTA entidad
        inverseJoinColumns = @JoinColumn(name = "actor_id") // la columna de LA OTRA
)
private List<Actor> actors = new ArrayList<>();
```

**1. La tabla intermedia se "gasta" aquí, no se duplica.** En la fase 3 se decidió que película-actor era N:M con tabla `movie_actor` (V4); en JPA eso es exactamente `@ManyToMany` + `@JoinTable` apuntando a la tabla existente. Si la anotación no dijera el nombre, Hibernate crearía otra tabla (`movie_actors`) al escribir. Es el caso donde el esquema diseñado a mano y el mapeo se cubren palabra por palabra.

**2. `movie_id` es "la columna de esta entidad"; `actor_id`, "la de la otra".** En un `@ManyToMany` unidireccional, `joinColumns` siempre señala hacia la entidad donde está la anotación y `inverseJoinColumns` hacia la del otro lado. Ni `movie` ni `actor` tienen una columna propia nueva: las dos siguen intactas en V2 y V3.

**3. Qué genera Hibernate al leer el reparto.** Con el `LAZY` (por defecto en esta anotación), `getActors()` provoca un segundo `SELECT`:

```sql
select a.id, a.first_name, a.last_name, a.birth_date, a.biography
from actor a
inner join movie_actor ma on a.id = ma.actor_id
where ma.movie_id = ?
```

Es el patrón que la fase 14 traerá al proyecto: primero `movie`, después `director` (11.4) y después `actors` (aquí). Tres `SELECT` para una película completa, de los que se verá el coste real en el paso 14.2 (N+1).

**4. La escritura que falta.** Leer ya funciona; guardar relación (añadir un actor a `movie_actor`) llega en la fase 17 con `persist`. Será el momento de ver cómo Hibernate inserta filas individuales (una por cada pareja), nunca la lista entera.

## 14. El código real del paso 11.6: `FetchType`

La última anotación de la fase decide **cuándo** va a la base cada relación:

```java
@ManyToOne(fetch = FetchType.LAZY)   // en Movie.director
@JoinColumn(name = "director_id", nullable = false)
private Director director;

@ManyToMany                          // en Movie.actors: LAZY implícito (es su default)
```

**1. Los dos defaults eran distintos, y por accidente.** `@ManyToOne` carga `EAGER` (ansioso: al pedir la película, Hibernate va a buscar también al director) y `@ManyToMany` carga `LAZY` (perezoso: solo si tocas el reparto). Saber de memoria los defaults de cada anotación es peor que declarar la regla una vez: D-49 dice "todas las relaciones perezosas".

**2. Qué es un *proxy*.** Con `LAZY`, `movie.getDirector()` no devuelve un `Director`: devuelve un sustituto (un "stand-in" sin datos todavía), una subclase generada en caliente por Hibernate que lleva el id guardado. La primera vez que llamas a algo suyo (por ejemplo `getName()`), el proxy consulta la base con `SELECT ... WHERE id = director_id` y pide los datos completos. A partir de ahí, delega como un `Director` normal.

**3. Por qué no `EAGER` "para más comodidad".** Listar 10 películas con `EAGER` trae el director de cada una aunque el caso de uso (la lista de la API, D-36) no lo pida; además genera N+1: una consulta por película. Con `LAZY`, la factura se paga solo cuando se usa, y cuando se usa mucho existe el `JOIN FETCH` de la fase 14 para traer todo en una sola consulta.

**4. El precio de `LAZY` se paga fuera de la transacción.** Si tocas `movie.getDirector()` cuando la sesión ya está cerrada, el proxy no puede ir a la base y lanza `LazyInitializationException`. La solución no es abrir la sesión de par en par (`open-in-view`, el aviso del log que aún no desactivamos): es controlar el alcance de la transacción (fase 12) y pedir las relaciones de forma explícita cuando hagan falta (fase 14).

## Para ampliar: qué buscar y qué evitar

El tema se llama **ORM** (*Object-Relational Mapping*) o **mapeo objeto-relacional**. Búsquedas útiles, en orden:

| Qué quieres entender | Búsqueda |
|---|---|
| El concepto | `que es un ORM explicado` |
| Las tres piezas | `JPA vs Hibernate diferencia` |
| El mapeo | `Spring Boot JPA entidades @Entity @Id @Column` |
| El repositorio | `Spring Data JPA repositorio findById` |
| Los converters | `JPA AttributeConverter ejemplo` |
| Las relaciones | `JPA @ManyToOne @ManyToMany JoinTable` |
| Lo que no se ve | `Hibernate entity lifecycle explained` / `ciclo de vida de una entidad JPA` |

Tres avisos:

1. **Casi todos los tutoriales usan entidad separada + mapper.** Verás `MovieEntity`, Lombok y clases `Mapper`. Es el diseño alternativo a D-29, no el nuestro.
2. **Mira la fecha y los imports.** Si usan `javax.persistence`, son anteriores a 2022; el proyecto usa `jakarta.persistence` (Spring Boot 3+).
3. **No busques "inyectar SQL en Java"**: el término correcto es ORM o mapeo objeto-relacional.

## Resumen

- JPA traduce automáticamente entre filas de tablas y objetos Java (es un ORM).
- JPA son las reglas; Hibernate, el programa que las aplica; Spring Data JPA, una capa que nos ahorra código.
- `@ManyToOne` representa la clave foránea `director_id`; `@ManyToMany` + `@JoinTable` representa la tabla intermedia `movie_actor`.
- En este proyecto, Flyway crea las tablas y JPA solo trabaja con los datos.

## Preguntas de repaso

**¿Qué problema resuelve JPA?**
Evita escribir a mano el SQL y la conversión entre filas y objetos para cada operación.

**¿Cuál es la diferencia entre JPA y Hibernate?**
JPA es la especificación (las reglas y anotaciones); Hibernate es la implementación que realmente ejecuta el trabajo.

**¿Qué anotación JPA representa la columna `director_id` de la tabla `movie`?**
`@ManyToOne` (con `@JoinColumn(name = "director_id")`).

**¿Por qué no dejamos que Hibernate cree las tablas?**
Porque el esquema lo gestionamos con migraciones de Flyway versionadas; si Hibernate también lo modificara, habría dos responsables del mismo esquema y perderíamos el control de los cambios.

**¿Cómo se mapea `PersonName`, que ocupa dos columnas?**
No cabe en un `AttributeConverter` (una columna por conversor): se anota el `record` como `@Embeddable` y cada componente lleva su `@Column` (`first_name`, `last_name`). La entidad lo anida con `@Embedded` (D-48).

**¿Qué papel hace el `AttributeConverter`?**
Tiene dos flechas: al escribir, entrega el valor interno del *value object* a la columna; al leer, el valor de la columna pasa por el constructor del `record`, que valida. Por eso `Title` vacío no puede existir aunque venga de la base.

**¿Cómo se marca en JPA la clave foránea `movie.director_id`?**
Con `@ManyToOne` en `Movie.director` (la cardinalidad, vista desde el lado "muchos") + `@JoinColumn(name = "director_id")`. `Movie` es el *lado dueño*: la columna física vive en su tabla.

**¿Qué es el *lado dueño* (*owning side*) de una relación?**
El lado cuya tabla tiene físicamente la columna de la FK. Es el punto de verdad de la relación: lo que Hibernate guarda y actualiza cuando se toca la relación.

**¿Para qué sirve `@Transient`?**
Saca un campo del mapeo de JPA: Hibernate lo ignora (no genera columna ni consulta por él). Se usa para campos que solo viven en memoria. En `Movie` quedó como apoyo del 11.4 y el 11.5 lo retiró: el reparto ya es relación.

**¿Cómo se mapea la tabla intermedia `movie_actor`?**
Con `@ManyToMany` + `@JoinTable(name = "movie_actor")` en `Movie.actors`: `joinColumns` apunta a `movie_id` (la columna de la entidad que declara la relación) e `inverseJoinColumns` a `actor_id` (la de la otra). La tabla ya existía (V4): el mapeo la usa, no la crea.

**¿Qué `SELECT` provoca `movie.getActors()`?**
Uno aparte, porque la `@ManyToMany` es `LAZY` por defecto: un `INNER JOIN` entre `actor` y `movie_actor` filtrado por `movie_id`. Igual que pasa con `director` en el 11.4: es el patrón de consultas encadenadas que la fase 14 medirá (N+1).

**¿Cómo se detecta un campo cuyo nombre no deduce su columna?**
Con `ddl-auto: validate`: el arranque falla con `Schema validation: missing column [duration] in table [movie]` (el caso real del 11.4): la estrategia *camelCase → snake_case* convierte `releaseYear` → `release_year`, pero de `duration` no puede derivar la palabra `-minutes`. La solución es declarar la columna con `@Column(name = ...)`.

**¿Qué quiere decir `autoApply = true` en un `@Converter`?**
Que JPA aplica ese conversor automáticamente a todos los atributos de ese tipo Java en cualquier entidad, sin anotarlos uno a uno.

**¿Por qué las aserciones del test de mapeo usan `nolan.getName().firstName()` y no un campo `firstName` suelto?**
Porque el dominio no tiene campos sueltos: la entidad guarda el *value object*. La aserción comprueba además que Hibernate reconstruyó el `record` (un `record` solo se puede crear por su constructor), y no escribió campos por reflexión.

**¿Cuál es el default de carga de cada relación y por qué no lo dejamos así?**
`@ManyToOne` es `EAGER` y `@ManyToMany` es `LAZY`. Depender de defaults desiguales obliga a saberlos de memoria y trae datos que nadie pidió, así que la regla del proyecto (D-49) es: todas las relaciones `LAZY`, declarado con `fetch = FetchType.LAZY` donde el default no manda.

**¿Qué es el *proxy* que entrega Hibernate con `LAZY`?**
Un sustituto: una subclase generada en tiempo de ejecución que guarda el id y consulta la base la primera vez que se toca. Comprobado con `assertNotEquals(Director.class, movie.getDirector().getClass())`: con `EAGER` el objeto real cargaba y con `LAZY` la clase ya no puede ser la original.

**¿Qué peligro trae el `LAZY`?**
El `LazyInitializationException` al tocar una relación con la sesión ya cerrada. El antídoto no es `open-in-view`: es mantener las operaciones dentro de la transacción (fase 12) y pedir las relaciones explícitas cuando hagan falta (`JOIN FETCH`, fase 14).
