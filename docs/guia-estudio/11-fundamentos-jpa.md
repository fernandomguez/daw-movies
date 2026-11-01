# 11 — Fundamentos de JPA y entidades

> **Fase del plan:** 11 (Fundamentos de JPA y entidades).
> **Estado:** cerrada (6 de 6 pasos; commit y etiqueta pendientes de confirmación).
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

## 11.3 Entidades simples y conversión de los *value objects*

### El test primero

`EntityMappingTests` (nuevo, 2 tests, en `com.daw`): arranca la aplicación y lee filas reales del seed con el `EntityManager` (JPA puro, sin repositorios, que llegan en la fase 12):

```java
@PersistenceContext private EntityManager em;

em.find(Director.class, 1L);                                  // Nolan, por id
em.createQuery("select d from Director d", Director.class);   // los 6
```

Las aserciones usan los datos reales del `seed.sql` (no inventados): Nolan con `name` "Christopher"/"Nolan", `birthDate` 1970-07-30 y biografía; DiCaprio con `biography == null` (los actores no tienen, D-37).

### Rojo (esto es lo que esperábamos)

```
Unknown entity type 'com.daw.movies.domain.model.Actor'            (em.find)
UnknownEntityException: Could not resolve root entity 'Director'   (JPQL)
```

Hibernate recién arrancado no tiene ninguna entidad en su mapa: la sesión existe, pero no sabe qué es un `Director`. Es el mismo "Found 0 JPA repository interfaces" de antes, ahora en forma de excepción.

### Los cambios para el verde

| Archivo | Qué se añade | Definición |
|---|---|---|
| `Director`, `Actor` | `@Entity`, `@Table(name = "director"/"actor")` | La clase es la entidad; su tabla |
| `Director`, `Actor` | `@Id` + `@GeneratedValue(strategy = IDENTITY)` en `id` | Clave primaria alimentada por el `AUTO_INCREMENT` de V1–V2 |
| `Director`, `Actor` | `@Embedded` en `name` | Ahí anida el *embeddable* `PersonName` |
| `PersonName` | `@Embeddable` + `@Column(name = "first_name"/"last_name", nullable = false, length = 100)` en los componentes del `record` | Dos columnas, un *value object* (D-48) |
| `TitleConverter`, `ReleaseYearConverter`, `DurationMinutesConverter`, `BirthDateConverter`, `BiographyConverter` | `AttributeConverter` con `@Converter(autoApply = true)` | Cada *value object* de una columna ↔ su columna |

Otras dos decisiones escondidas en la tabla:

- **`autoApply = true`:** aplica el conversor a todo atributo de su tipo en cualquier entidad. Si fuera `false`, habría que escribir `@Convert(converter = ...)` campo por campo —y en la fase 11.4, en `Movie`, tres veces más. Así, `Movie` heredará los cinco convertidores sin tocarlos.
- **`@Column` en los componentes del `record`**, no en los campos de la entidad: `Director.name` es un atributo sin columna propia; las columnas las declaran los componentes de `PersonName`. Como `director` y `actor` tienen exactamente los mismos nombres de columna, no hacen falta `@AttributeOverrides`: un solo `@Embeddable` sirve para las dos entidades.

### La deuda del 11.2, saldada con una prueba

La duda: ¿instancia Hibernate 7.4.5 el `record` `PersonName` como *embeddable* pasando por su **constructor canónico** (con validación) o rompería algo? Resultado de la prueba real: **verde a la primera**, incluido el `ddl-auto: validate` contra las tablas de Flyway. Si Hibernate no hallara caminos para construir el `record`, el arranque habría fallado; las aserciones sobre `nolan.getName().firstName()` confirman que el nombre cruzó de las dos columnas al *value object*. D-48 cerrada.

Dos observaciones del arranque:

1. El `validate` no imprimió nada en el log: valida en silencio y solo grita cuando algo no encaja. Pasar es no aparecer.
2. Temíamos un posible rechazo del `String` del `Biography` contra la columna `TEXT` (el validador compara tipos de columna): no ocurrió. Hibernate acepta la familia de tipos de la columna tal cual la declara MariaDB.

### Lo que este paso no hace

- `Movie` sigue sin anotar: sus dos relaciones (`@ManyToOne`, `@ManyToMany`) son los pasos 11.4 y 11.5.
- No existe ningún repositorio: `Found 0 JPA repository interfaces`.
- No se ha escrito ningún SQL: el esquema sigue siendo el de Flyway V1–V4.
- No hay commit: la fase 11 se cierra al terminar el 11.6 (D-30).

## 11.4 `@ManyToOne`: la clave foránea en JPA

### El test primero

Tercer test en `EntityMappingTests`: cuenta las 10 películas del seed y lee *Inception* por su id (`em.find(Movie.class, 3L)`), comprobando `title`, `releaseYear`, `duration` y —lo nuevo— que `inception.getDirector().getName().lastName()` devuelve `Nolan`: el campo relación se resuelve abajo, en la fila de `director`.

**Rojo:** `Could not resolve root entity 'Movie'` (JPQL). Para Hibernate, `Movie` todavía no existía.

### El validate dientes por primera vez: el campo `duration`

Anotada `Movie`, el arranque falló con el primer error real del `ddl-auto: validate` en el proyecto:

```
org.hibernate.tool.schema.spi.SchemaManagementException: Schema validation:
missing column [duration] in table [movie]
```

La causa: Hibernate deduce el nombre de la columna a partir del nombre del campo, y Spring Boot aplica la estrategia *camelCase → snake_case*. `birthDate` → `birth_date` (encaja con V1–V3), `releaseYear` → `release_year` (encaja)... pero el campo `duration` se queda en `duration` y la columna de V3 es `duration_minutes`. Ninguna estrategia adivina la palabra `-minutes` que falta. La corrección: declararlo con `@Column(name = "duration_minutes")`.

Con esto el validate pasa. Es exactamente la promesa que venía de D-34: el mapeo debe coincidir con el esquema, y si no, el arranque no arranca.

### Lo que se escribió

| Anotación | Dónde | Qué dice |
|---|---|---|
| `@Entity`, `@Table(name = "movie")` | la clase | Ahora `Movie` también es entidad |
| `@Id` + `@GeneratedValue(IDENTITY)` | `id` | El `AUTO_INCREMENT` de V3 |
| `@Column(name = "duration_minutes")` | `duration` | El nombre que la estrategia por defecto no adivina |
| `@ManyToOne` + `@JoinColumn(name = "director_id", nullable = false)` | `director` | La clave foránea `movie.director_id → director.id` |
| `@Transient` | `actors` | El reparto queda fuera del mapeo **de momento** (11.5) |

Cuatro observaciones:

1. **Ni un solo `@Convert`:** los tres *value objects* de `Movie` entran por los converters del 11.3 gracias al `autoApply`. El esfuerzo del 11.3 se paga aquí: tres campos sin anotar de ese lado.
2. **La lectura del `@ManyToOne`:** anotación de la cardinalidad ("muchas películas → un director") y `@JoinColumn` declara la columna de la FK. `Movie` es el **lado dueño** (*owning side*) de la relación: la columna física está en su tabla, igual que en el esquema. `Director` no sabe nada: la relación es unidireccional (fase 14).
3. **`@Transient`:** una provisión honesta, no un truco permanente. Si Hibernate viera el campo `List<Actor> actors` sin anotar, intentaría guardarlo como un tipo básico y el arranque fallaría. En el 11.5 se sustituye por `@ManyToMany + @JoinTable` y la anotación desaparece.
4. **`EAGER` por defecto:** `@ManyToOne` carga la relación ansiosa (primera consulta que la toca). Está bien por ahora; en el 11.6 se decide `LAZY` frente a `EAGER` con justificación propia.

### Lo que este paso no hace

- El reparto (`actors`) sigue anotado `@Transient`: se mapea en el 11.5.
- No se guarda nada: solo se lee (`em.find`). El `INSERT` llega en la fase 17.
- No hay commit: la fase 11 se cierra al terminar el 11.6 (D-30).

## 11.5 `@ManyToMany` + `@JoinTable`: la tabla intermedia como relación

### El test primero

Cuarto test en `EntityMappingTests`: leer *Inception* (`em.find(Movie.class, 3L)`) y comprobar que su reparto son dos actores, DiCaprio y Michael Caine (filas `(3,1)` y `(3,2)` del seed).

**Rojo distinto:** un simple `expected: <2> but was: <0>`. Sin *error* de Hibernate: `Movie` se cargó bien; el reparto vino vacío porque el 11.4 tiene el campo marcado `@Transient`. Es la forma que tiene el test de decir "esto existe en la base (V4), pero JPA no lo ve". Vale la pena fijarse: un fallo de aserción y un error de arranque enseían cosas distintas; aquí el fallo señala el hueco del mapeo, no una entidad rota.

### Lo que se escribió

El campo `actors` cambia de anotación y desaparece el import `jakarta.persistence.Transient`:

```java
@ManyToMany
@JoinTable(
        name = "movie_actor",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "actor_id")
)
private List<Actor> actors = new ArrayList<>();
```

| Pieza | Qué dice |
|---|---|
| `@ManyToMany` | La cardinalidad: una película tiene varios actores y un actor actúa en varias películas |
| `@JoinTable(name = "movie_actor")` | "La relación vive en esta tabla intermedia" (la de V4, no una nueva) |
| `joinColumns` | Qué columna apunta a **esta** entidad: `movie_id` |
| `inverseJoinColumns` | Qué columna apunta a la **otra** entidad: `actor_id` |

Tres matices anotados:

1. **El validate también comprueba la tabla intermedia.** Si `movie_actor` no existiera o cambiara de columnas, el arranque fallaría igual que en el 11.4. Es más mapeo, no una tabla nueva: Flyway sigue siendo el dueño del esquema.
2. **`LAZY` por defecto aquí.** A diferencia del `@ManyToOne` (ansioso), una `@ManyToMany` se carga perezosa: en el test el reparto se trae con un `SELECT` aparte al llamar `getActors()`, dentro de la transacción del test. El paso 11.6 volverá sobre esto.
3. **Sin `cascade`:** guardar `Movie` no guarda sus actores: sin `cascade`, escribir la relación solo toca la tabla intermedia. Encaja con D-14 (la garantía del reparto es la PK compuesta) y con la fase 17, donde se persiste de forma explícita.

### Lo que este paso no hace

- No se escribe ni se borra en `movie_actor`: solo se lee. El `INSERT` del reparto llega en la fase 17 (recorrido inverso DTO → dominio → fila).
- La relación sigue unidireccional: `Actor` tampoco ahora conoce sus películas (fase 14 muestra el coste de cruzarse).
- No hay commit: la fase 11 se cierra al terminar el 11.6 (D-30).

## 11.6 `FetchType`: todas las relaciones perezosas (D-49)

### El problema

Las dos relaciones de `Movie` cargaban con valores distintos por accidente: `director` era `EAGER` (el default de `@ManyToOne`) y `actors` era `LAZY` (el de `@ManyToMany`). Eso obliga a saber los defaults de memoria y, peor, con `EAGER` una lista de películas trae el director de cada una aunque nadie lo haya pedido.

### El test primero

Quinto test de `EntityMappingTests`, que comprueba el **comportamiento** y no la anotación: con `EAGER`, `movie.getDirector()` ya es un objeto real de clase `Director`; con `LAZY`, Hibernate entrega un *proxy* —una subclase generada en caliente que consulta la base solo la primera vez que se toca—, así que **su clase no puede ser `Director`**:

```java
assertNotEquals(Director.class, inception.getDirector().getClass());
```

**Rojo real:**

```
expected: not equal but was: <class com.daw.movies.domain.model.Director>
```

En un `@ManyToOne` sin `fetch`, el `em.find` carga el objeto entero. Comprobado, no supuesto.

### El cambio

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "director_id", nullable = false)
private Director director;
```

Una línea y un import (`jakarta.persistence.FetchType`). Para `actors` no se escribe nada más: `@ManyToMany` ya es `LAZY` por defecto; anotarlo sería redundante, pero queda anotado en el javadoc y en D-49 que **la regla del proyecto es: todas las relaciones perezosas, sin depender de defaults desiguales**.

### La decisión (D-49), en una frase

**Todas las relaciones de todas las entidades son `LAZY`; nada se carga por accidente.** Los motivos:

1. Una consulta que pide películas no debe traer de regalo director ni reparto: con la API de la v1, listar películas (`MovieSummary`) y pedir cargas que no se usan son transacciones de red gratis que nadie pidió.
2. `EAGER` también provoca N+1 (la fase 14 lo contará en los logs, con `JOIN FETCH` como antídoto 14.3).
3. El coste de `LAZY` se conoce y se planifica: el `LazyInitializationException` aparece si se toca la relación fuera de la transacción. Eso se maneja en la fase 12 (operaciones dentro de la transacción del servicio/adaptador) y con `open-in-view: false` en el 14.6 (petición del profesor): mejor que la excepción salte pronto y clara que que se cure con una sesión abierta toda la petición.

**Nota para el futuro (fase 14):** los *proxies* alteran la clase del objeto (`movie.getDirector()` no es `instanceof` un `Director` "normal"), lo que afectará a `equals`/`hashCode` cuando lleguen.

### Lo que este paso no hace

- No añade consultas ni cargas deliberadas: la fase 14 estudia `JOIN FETCH` y `@EntityGraph`.
- No toca los tests de dominio (28 + 20): son unitarios y no entra JPA por ahí.
- No hay commit hasta cerrar la fase (D-30): se pide permiso por confirmar.

## Cierre de la fase 11

| Paso | Qué quedó | Test/A/test |
|---|---|---|
| 11.1 | JPA vs. Hibernate vs. Spring Data, comprobado con `dependency:tree` | 0 |
| 11.2 | D-29 escrita: qué impone JPA y qué deja de correr (el constructor de la entidad) | 0 |
| 11.3 | `Director` y `Actor` entidades; 5 converters; `PersonName` `@Embeddable` (D-48) | 2 |
| 11.4 | `Movie` entidad; `@ManyToOne` + `@JoinColumn`; error real del validate con `duration_minutes` | 1 |
| 11.5 | `@ManyToMany` + `@JoinTable` sobre `movie_actor`; rojo de aserción, no de arranque | 1 |
| 11.6 | `FetchType.LAZY` en `director` (D-49); proxy comprobado con test | 1 |

| 11.5 | `@ManyToMany` + `@JoinTable` sobre `movie_actor`; rojo de aserción, no de arranque | 1 |
| 11.6 | `FetchType.LAZY` en `director` (D-49); proxy comprobado con test | 1 |

Total: **60 tests en verde** (55 de la fase 10 + 5 de `EntityMappingTests`). **Siguiente:** fase 12.1 — DAO frente a Repositorio.

## Siguiente paso

**12.1 — DAO frente a Repositorio** (fase 12): primer paso de la siguiente fase, se crea el diario al empezar. Cerrada la fase 11 con commit único + etiqueta `fase-11` (D-30), cuando el alumno lo confirme.
