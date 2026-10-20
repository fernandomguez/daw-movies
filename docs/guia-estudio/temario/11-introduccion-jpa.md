# 11 — Introducción a JPA

> **Fase del plan:** 11 (Fundamentos de JPA y entidades).
> **Estado:** introducción conceptual, explicada por adelantado en la fase 1. El código de este tema es **ilustrativo**: todavía no existe en el proyecto. Se ampliará con el código real cuando lleguemos a la fase 11.

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
public class DirectorEntity {

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

Al pedir "dame el director 1", JPA genera el `SELECT`, lee la fila y devuelve un objeto `DirectorEntity` relleno con los datos de Nolan.

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
| `movie.director_id` es una clave foránea hacia `director.id` | `@ManyToOne` en `MovieEntity` | Muchas películas → un director |
| Tabla intermedia `movie_actor (movie_id, actor_id)` | `@ManyToMany` + `@JoinTable` en `MovieEntity` | Muchas películas ↔ muchos actores |

Vista previa (ilustrativa):

```java
@Entity
@Table(name = "movie")
public class MovieEntity {

    @Id
    private Long id;

    // Muchas películas pueden tener el mismo director (columna director_id)
    @ManyToOne
    @JoinColumn(name = "director_id")
    private DirectorEntity director;

    // Relación muchos a muchos a través de la tabla intermedia movie_actor
    @ManyToMany
    @JoinTable(
        name = "movie_actor",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private List<ActorEntity> actors;
}
```

Por ejemplo, con estos datos, cargar *Inception* daría un `MovieEntity` cuyo `director` es Nolan y cuya lista `actors` contiene a DiCaprio y a Michael Caine.

## 5. Dos reglas de este proyecto

1. **JPA no crea nuestras tablas.** Las crea **Flyway** con SQL que escribimos nosotros. JPA solo lee y escribe datos. Por eso configuraremos Hibernate para que **no modifique el esquema** (`ddl-auto` en `none` o `validate`).
2. **La entidad JPA no es el objeto de dominio.** `DirectorEntity` vive en la capa de persistencia y lleva anotaciones de base de datos. `Director` vive en el dominio y es Java puro, sin dependencias de JPA. Pasaremos de uno a otro con **mappers** (fase 13).

## 6. ¿Por qué JPA va en la fase 11 y no antes?

- Primero entendemos las tablas y las relaciones con **SQL puro** (fases 3, 8 y 9). Así, JPA solo será aprender a *describir en Java* algo que ya conocemos, y no "magia".
- Primero diseñamos el **dominio** (fase 10), para no acabar copiando la estructura de las tablas en nuestro modelo de negocio.

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
