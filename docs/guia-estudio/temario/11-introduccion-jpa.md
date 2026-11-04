# 11 — Introducción a JPA

> **Fase del plan:** 11 (Fundamentos de JPA y entidades).
> **Estado:** introducción conceptual, explicada por adelantado en la fase 1. Actualizada el 2026-10-20: las anotaciones JPA van en las clases del dominio (`Director`, `Movie`, `Actor`), sin entidades separadas (D-29). El código de este tema es **ilustrativo**: todavía no existe en el proyecto. **Ampliado el 2026-10-31** (sesión del paso 10.4) con el recorrido fila ↔ objeto, el reparto de responsabilidades JPA/Hibernate/Spring Boot/Spring Data, *dirty checking* y una guía de búsqueda. Se completará con el código real en la fase 11.

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
Director nolan = new Director(
        new PersonName("Christopher", "Nolan"),
        new BirthDate(LocalDate.of(1970, 7, 30)),
        null);                      // sin biografía
```

El `id` no se pasa al constructor: lo asigna la base al guardar (por eso `getId()` es `null` hasta entonces). Los datos no son `String` sueltos sino *value objects* (fase 10, D-46): ahí está el reto que resolverá la fase 11.

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

    @Id                     // "este campo es la clave primaria"
    private Long id;

    @Column(name = "first_name")   // "este campo es la columna first_name"
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;
}
```

Al pedir "dame el director 1", JPA genera el `SELECT`, lee la fila y devuelve un objeto `Director` relleno con los datos de Nolan.

> **Simplificación didáctica.** Este ejemplo usa `String` y `LocalDate` para ver solo la idea de las anotaciones. El `Director` real (paso 10.4) guarda `PersonName name`, `BirthDate birthDate` y `Biography biography`, y JPA no sabe guardar esos tipos por sí solo: hace falta un `AttributeConverter` (un valor → una columna: `Title`, `BirthDate`…) o `@Embeddable` (un valor → varias columnas: `PersonName`). Es el coste de D-46, y se paga en la fase 11.

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

**Hoy** Hibernate arranca pero no mapea nada: en los logs de los tests aparece `Initialized JPA EntityManagerFactory` y `Found 0 JPA repository interfaces`. En cuanto existan entidades y repositorios, empezará a generar SQL.

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
