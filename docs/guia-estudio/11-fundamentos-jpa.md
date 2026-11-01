# 11 — Fundamentos de JPA y entidades

> **Fase del plan:** 11 (Fundamentos de JPA y entidades).
> **Estado:** en curso (2 de 6 pasos).
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

## 11.2 JPA en las clases del dominio (D-29)

### La decisión, en una frase

Las anotaciones de JPA van **en `Movie`, `Director` y `Actor`**. No existe `MovieEntity` ni `MovieEntityMapper`.

### Lo que se descartó

El diseño de la fase 5 tenía tres clases por concepto:

```
Movie            dominio, Java puro, con sus reglas
MovieEntity      @Entity, espejo exacto de la tabla
MovieEntityMapper  copia campo a campo entre las dos
```

Funciona bien y es lo que hace la mayoría de proyectos grandes. Se descartó por una razón concreta: **cada campo nuevo hay que añadirlo tres veces y copiarlo dos**. En un proyecto de aprendizaje, ese ruido tapa lo que se quiere estudiar.

### Qué impone JPA (comprobado en el código de hoy)

| Qué exige JPA | Por qué | Estado actual |
|---|---|---|
| Constructor sin argumentos, público o **protegido** | Hibernate crea la instancia por reflexión y luego rellena los campos | ✅ `protected Movie() {}` |
| Campos **no `final`** | Tiene que asignarlos después de crear el objeto | ✅ |
| Clase **no `final`** | Para `LAZY` necesita generar una subclase (*proxy*) | ✅ `public class Movie` |
| Acceso por campo | Con `@Id` en el campo, escribe directamente y **no** llama a los *setters* | ✅ todos los campos son privados |
| Colecciones mutables y no `final` | Sustituye la lista por su propia implementación (`PersistentBag`) | ✅ `new ArrayList<>()` |
| Tipos que JPA entienda | No conoce `Title`, `BirthDate`, `PersonName`… | ❌ trabajo real del 11.3 |
| `id` `Long` con `@GeneratedValue` | El valor lo pone la base de datos al insertar | ✅ `Long id`, `null` antes de guardar |
| Mapeo exacto del esquema | `ddl-auto: validate` compara entidad y tabla al arrancar | ⏳ 11.3–11.5 |

La fase 10 dejó preparados sin saberlo los puntos 1, 2, 3, 5 y 7. El único trabajo de verdad son los **seis *value objects***, que JPA no sabe convertir solo.

### El coste que no conviene esconder: la hidratación salta el constructor

Al guardar, el constructor de la entidad hace su trabajo. Al **leer**, no:

```java
// lo que escribimos nosotros
Director d = new Director(null, null, null);
// IllegalArgumentException: "El nombre del director es obligatorio"

// lo que hace Hibernate, en esencia
Director d = new Director();          // constructor protegido
d.name      = <columnas first_name + last_name>;
d.birthDate = <columna birth_date>;
```

Consecuencias reales:

1. **Las comprobaciones de la entidad no se ejecutan al leer.** Si la tabla tuviera `first_name` a `NULL`, obtendrías un `Director` con `name` nulo y el fallo aparecería mucho después, en `fullName()`.
2. **Los *value objects* sí siguen protegidos**, porque para construirlos Hibernate pasa por su constructor (el del `record` o el del `AttributeConverter`). Ahí las reglas se cumplen.
3. **El guardián de verdad es la base de datos.** `NOT NULL`, `CHECK` y las claves foráneas de Flyway no son decoración: son la última línea que impide que entren filas que el dominio considera imposibles. D-34 (`validate`) y las migraciones trabajan juntas por esto.

### Lo que se gana

| Ventaja | Consecuencia práctica |
|---|---|
| Dos clases y un mapper menos | ~150 líneas que no existen ni hay que mantener |
| Un campo se declara **una vez** | Añadir `originalTitle` toca un solo archivo |
| Desaparece el *bug* del mapper incompleto | Copiar campo a campo siempre acaba olvidando uno |
| El dominio no depende de Spring Data | JPA es Jakarta EE; el dominio sigue sin saber que existe Spring |
| La clase **es** la documentación de la tabla | Leer `Movie` dice qué columnas tiene `movie` |

### Lo que se pierde

| Coste | Cuánto duele aquí |
|---|---|
| El dominio importa `jakarta.persistence` | Se rompe la "ignorancia de la persistencia"; es el precio de D-29 |
| La clase de negocio refleja la tabla | Renombrar una columna puede obligar a tocar el dominio |
| Sin frontera entidad ↔ dominio | No hay dónde "adaptar" un esquema feo; hay que convivir con él |
| Los *proxies* cambian la clase | Con `LAZY`, `movie.getClass()` puede no ser `Movie.class`; afecta a `equals`/`hashCode` (fase 14) |

### Cuándo **no** elegiría D-29

- **Esquema heredado** que no puedes cambiar y no encaja con el dominio.
- **Varias fuentes** de la misma información (varias tablas o servicios) que hay que unificar en un objeto.
- **Varios equipos** donde el dominio es un contrato estable y la base de datos evoluciona por su cuenta.
- **Tablas enormes** donde cargar la entidad completa es caro y necesitas proyecciones.

Ninguno es el caso de este proyecto: un módulo, un esquema que escribimos nosotros y 4 tablas.

### La deuda que abre: `PersonName` ocupa dos columnas

JPA mapea **un atributo a una columna**. `PersonName` son `first_name` y `last_name`, así que no cabe en un `AttributeConverter` (esos van de un tipo Java a una columna y vuelta).

Candidatos para el 11.3, sin decidir todavía:

1. **`@Embeddable` en el `record`** + `@AttributeOverrides` para fijar los nombres de columna. Es lo natural. **Dato verificado** (no supuestos): Hibernate lo soporta desde la **6.2** (notas de la versión, marzo 2023) instanciando el `record` por su **constructor canónico** —es decir, con validación— en vez de por reflexión sobre campos. Nuestra versión es 7.4.5, muy posterior. Aún así se confirmará con una prueba real en el 11.3, porque los ejemplos de la documentación suelen usar records sin reglas, no con comprobaciones dentro.
2. **Columnas planas** en la entidad (`firstName`, `lastName`) y reconstruir `PersonName` en el *getter*. Pragmático, pero la entidad deja de reflejar el dominio.
3. **Volver `PersonName` a clase mutable** con constructor vacío. Se descarta: rompe la inmutabilidad de la fase 10 para satisfacer a la herramienta.

Se decide en el 11.3 **con una prueba**, no por suposición: es exactamente el tipo de detalle donde la documentación y la versión real no siempre coinciden.

### Lo que este paso no hace

- No se ha tocado código: 55 tests en verde y ninguna entidad mapeada.
- No se ha añadido ninguna anotación: `Movie`, `Director` y `Actor` siguen siendo Java puro.
- No hay commit: la fase 11 se cierra al terminar el 11.6 (D-30).

## Siguiente paso

**11.3 — Entidades simples:** `@Entity`, `@Table`, `@Id`, `@Column` en `Director` y `Actor`, más los `AttributeConverter` de los cinco *value objects* de una columna y la prueba con `PersonName`. Es el primer paso con código y el primero con riesgo real: `ddl-auto: validate` hará fallar el arranque si el mapeo no coincide **exactamente** con las tablas de Flyway.
