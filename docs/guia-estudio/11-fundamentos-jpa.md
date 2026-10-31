# 11 — Fundamentos de JPA y entidades

> **Fase del plan:** 11 (Fundamentos de JPA y entidades).
> **Estado:** en curso (1 de 6 pasos).
> **Qué cuenta este archivo:** lo que hacemos de verdad en la fase 11: anotaciones, errores, comprobaciones y resultados. Los conceptos están en el [temario 11](temario/11-introduccion-jpa.md).

## En pocas palabras

La fase 10 terminó con un dominio en Java puro: `Movie`, `Director`, `Actor` y seis *value objects* que se validan solos, pero que **no saben guardarse**. La fase 11 les enseña a persistir: qué tablas y columnas representan, cómo se relacionan y cuándo se cargan. No se escribe SQL: se describen las clases y Hibernate genera las consultas.

## 11.1 Qué es JPA y qué es Hibernate

### La pregunta

Tres nombres aparecen siempre juntos —**JPA**, **Hibernate** y **Spring Data JPA**— y no son lo mismo. Confundirlos hace que no se entienda qué parte del código es nuestra y qué parte la pone el framework.

### Qué es un ORM

**ORM** (*Object-Relational Mapping*, mapeo objeto-relacional) es la técnica de traducir entre dos formas de guardar información que no encajan:

| | Java | MariaDB |
|---|---|---|
| Unidad | Objeto (`Director`) | Fila |
| Agrupación | Clase | Tabla |
| Referencia | Campo con otro objeto (`movie.director`) | Clave foránea (`movie.director_id`) |
| Identidad | Referencia en memoria | Clave primaria |

Un ORM hace la traducción en los dos sentidos: fila → objeto al leer y objeto → fila al guardar. En este proyecto, la traducción la hace **Hibernate**.

**Lo que un ORM no es:** no es una base de datos, no sustituye a SQL (lo genera) y no decide el esquema (eso es Flyway, D-34).

### Las cuatro piezas

| Pieza | Qué es | Dónde se nota en el proyecto |
|---|---|---|
| **JPA** | Una **especificación**: el conjunto de reglas y anotaciones (`@Entity`, `@Id`, `@ManyToOne`). No ejecuta nada por sí sola. | Los imports `jakarta.persistence.*` que tendrán las entidades |
| **Hibernate** | La **implementación** de JPA: lee las anotaciones, genera el SQL y rellena los objetos. | `hibernate-core` en el `pom.xml` |
| **Spring Data JPA** | Una capa de Spring **encima** de JPA que implementa repositorios (`findById`, `save`...) al arrancar. | Llegará en la fase 12 |
| **Spring Boot** | El **cableado**: crea el `DataSource`, el `EntityManagerFactory`, el gestor de transacciones y arranca Flyway y Tomcat. | `@SpringBootApplication`, `application.yaml` |

La frase que lo resume: **JPA dice qué se puede escribir; Hibernate hace el trabajo; Spring Data JPA ahorra los métodos repetidos; Spring Boot monta las piezas.**

### Comprobado en el proyecto (no supuesto)

`.\mvnw.cmd dependency:tree "-Dincludes=org.hibernate.orm:*,jakarta.persistence:*"`:

```
org.springframework.boot:spring-boot-hibernate:jar:4.1.1:compile
   \- jakarta.persistence:jakarta.persistence-api:jar:3.2.0:compile
   \- org.hibernate.orm:hibernate-core:jar:7.4.5.Final:compile
```

- El `pom.xml` no declara Hibernate: lo trae el starter `spring-boot-starter-data-jpa`.
- La API que usaremos es **`jakarta.persistence` 3.2**, no `javax.persistence`. Si un tutorial usa `javax`, es anterior a 2022 y no encaja con Spring Boot 4.
- En el `application.yaml` ya está `spring.jpa.hibernate.ddl-auto: validate`: Hibernate **comprueba** que las entidades encajan con las tablas de Flyway, pero no las crea ni las modifica (D-34).

### Especificación frente a implementación

Una especificación es un documento de reglas; una implementación es un programa que las cumple. La analogía del temario: JPA son las normas del examen e Hibernate el alumno que lo hace.

Consecuencias prácticas:

1. **Nuestro código importa `jakarta.persistence`, nunca `org.hibernate`.** Así el dominio no queda atado a un proveedor concreto.
2. **En teoría se puede cambiar de implementación** (EclipseLink, por ejemplo) sin tocar las entidades. En la práctica, cuando se usan características propias de Hibernate, el cambio deja de ser trivial.
3. **JPA no incluye repositorios ni consultas por nombre de método.** Eso es Spring Data JPA (fase 12), que es otro estándar distinto, de Spring.

### Qué se escribirá en los siguientes pasos

| Paso | Qué se añade a las clases |
|---|---|
| 11.2 | La decisión D-29: JPA en el dominio y qué implica (constructor vacío, campos no `final`) |
| 11.3 | `@Entity`, `@Table`, `@Id`, `@Column` en `Director` y `Actor` |
| 11.4 | `@ManyToOne` + `@JoinColumn` en `Movie.director` |
| 11.5 | `@ManyToMany` + `@JoinTable` en `Movie.actors` |
| 11.6 | `FetchType.LAZY` frente a `EAGER` y su justificación |

Ninguno de esos pasos escribe SQL: son anotaciones y, para los *value objects*, `AttributeConverter`.

### Lo que este paso no hace

- No se ha tocado código: sigue habiendo 55 tests en verde y ninguna entidad mapeada.
- No se ha creado ninguna tabla: el esquema es el de Flyway (`V1`–`V4`).
- No se ha creado ningún repositorio: `Found 0 JPA repository interfaces` seguirá apareciendo hasta la fase 12.
- No hay commit: la fase 11 se cierra al terminar el 11.6 (D-30).

## Siguiente paso

**11.2 — JPA en las clases del dominio (D-29):** qué impone JPA a `Movie`, `Director` y `Actor`, qué se gana al no tener una entidad separada y qué se pierde. Sin código todavía: es una justificación escrita de la decisión.
