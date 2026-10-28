# Tema 06 — El proyecto Spring Boot y Git, explicado desde cero

> Fase del plan: 6. Lo que hicimos, con los comandos reales: [06-creacion-proyecto-spring-boot.md](../06-creacion-proyecto-spring-boot.md).

**En una frase:** en la fase 6 creamos el "esqueleto" de la aplicación Java (con Spring Boot), aprendimos a construirla y probarla (con Maven) y empezamos a guardar su historia (con Git).

---

## 1. El mapa: qué piezas hay y cómo encajan

Antes de entrar en detalle, esta es la foto completa. Cada pieza tiene **un solo trabajo**:

```
   TÚ escribes código Java
          │
          ▼
 ┌─────────────────┐   guarda la historia de los archivos   ┌──────────┐
 │       Git       │ ─────────────────────────────────────► │  GitHub  │ (copia en internet)
 └─────────────────┘                                        └──────────┘
          │
          ▼
 ┌─────────────────┐   descarga librerías, compila,
 │      Maven      │   ejecuta los tests
 └─────────────────┘
          │
          ▼
 ┌─────────────────┐   arranca la aplicación y conecta
 │   Spring Boot   │   sus piezas automáticamente
 └─────────────────┘
          │  (fase 7)
          ▼
 ┌─────────────────┐
 │ MariaDB (Docker)│   guarda los datos
 └─────────────────┘
```

| Herramienta | Su trabajo, en pocas palabras |
|---|---|
| **Git** | Hacer "fotos" del proyecto para poder volver atrás |
| **Maven** | Construir el proyecto: conseguir las librerías, compilar y probar |
| **Spring Boot** | Arrancar la aplicación y montar sus piezas sin que tengamos que hacerlo a mano |

---

## 2. Git: guardar la historia del proyecto

### La idea

Imagina un videojuego con **puntos de guardado**. Si algo sale mal, vuelves al último punto. Git hace eso con los archivos del proyecto: cada punto de guardado se llama **commit**.

Cada commit guarda:
- cómo estaban **todos** los archivos en ese momento;
- un **mensaje** que explica qué cambió;
- quién y cuándo.

### Los pasos para guardar

Guardar en Git tiene dos pasos, como hacer una maleta:

1. **`git add`**: meter en la maleta lo que quieres llevarte (elegir qué cambios van en el commit).
2. **`git commit`**: cerrar la maleta y ponerle una etiqueta (crear el punto de guardado con un mensaje).

```powershell
git status         # ¿qué archivos han cambiado?
git add -A         # meter todos los cambios en la "maleta"
git commit -m "feat(fase-07): connect Spring Boot to MariaDB"   # guardar
```

En `git status --short`, `??` significa "archivo nuevo que Git todavía no sigue", `M` "modificado", `A` "añadido" y `D` "borrado".

### GitHub: la copia en internet

Tu carpeta es el repositorio **local**. GitHub guarda una copia **remota**, llamada `origin`. Sirve de copia de seguridad y para compartir.

```powershell
git push           # subir tus commits a GitHub
git pull           # traer los cambios de GitHub
```

### Etiquetas: un marcador por fase

Una **etiqueta** (*tag*) es un nombre fijo pegado a un commit, como un marcapáginas. En este proyecto, cada fase terminada tiene la suya: `fase-05`, `fase-06`, `fase-07`...

```powershell
git checkout fase-06   # ver el proyecto exactamente como estaba al acabar la fase 6
git checkout main      # volver al presente
```

Al hacer `checkout` de una etiqueta, Git dice `detached HEAD`. **No es un error**: significa "estás mirando el pasado". No hagas cambios ahí; vuelve con `git checkout main`.

### `.gitignore`: lo que nunca se sube

Hay archivos que **no** deben entrar en Git. Se apuntan en el archivo `.gitignore`:

- **Secretos**: `.env` tiene la contraseña de la base de datos. Si se sube a GitHub, aunque luego lo borres, **sigue en la historia** y hay que cambiar la contraseña.
- **Cosas que se pueden volver a generar**: la carpeta `target/`, que crea Maven al compilar.
- **Cosas personales**: la configuración de tu editor (`.idea/`, `.vscode/`).

Para preguntar a Git si ignora un archivo, y por qué: `git check-ignore -v .env`.

### `.gitattributes`: los saltos de línea

Windows y Linux marcan el final de cada línea de texto con caracteres distintos (CRLF y LF). Si no se controla, Git cree que un archivo ha cambiado aunque nadie lo haya tocado. El archivo `.gitattributes` fija una regla para todos: en el repositorio, siempre LF. Por eso, al hacer commit, a veces verás el aviso `CRLF will be replaced by LF`. **Es normal**: es la regla funcionando.

---

## 3. Maven: el que construye el proyecto

### La idea

Una aplicación Java usa muchas **librerías**, es decir, código que han escrito otros: para conectarse a la base de datos, para los tests... Conseguirlas a mano, en la versión correcta, sería un lío.

**Maven** es como un **cocinero con su receta**:
- la **receta** es el archivo `pom.xml`: dice qué es el proyecto y qué "ingredientes" (librerías) necesita;
- Maven va a buscar esos ingredientes a internet, los guarda en tu ordenador (en la carpeta `~/.m2`) y "cocina" el proyecto: compila y ejecuta los tests.

### Dependencias

Cada librería que necesitamos se llama **dependencia** y se escribe así en el `pom.xml`:

```xml
<dependency>
    <groupId>org.mariadb.jdbc</groupId>          <!-- quién la hace -->
    <artifactId>mariadb-java-client</artifactId> <!-- cómo se llama -->
    <scope>runtime</scope>                        <!-- cuándo se necesita (ver abajo) -->
</dependency>
```

Fíjate en que **no pone versión**. La decide Spring Boot, que trae una lista de versiones que se han probado juntas y funcionan bien entre sí. (A esa lista se le llama **BOM**.) Así no mezclamos versiones que choquen.

El **scope** dice cuándo hace falta la librería:

| Scope | Cuándo se usa | Ejemplo |
|---|---|---|
| `compile` (si no se pone nada) | Siempre | Spring Boot |
| `runtime` | Solo al ejecutar. Nuestro código no la usa directamente | El driver de MariaDB (fase 7) |
| `test` | Solo al ejecutar los tests. No va en la aplicación final | JUnit |

### Starters: librerías "en pack"

Spring Boot ofrece **starters**: dependencias que traen de golpe todo lo necesario para una tarea. Por ejemplo, `spring-boot-starter-data-jpa` trae unas diez librerías para trabajar con bases de datos. Nosotros escribimos una línea y Maven trae el resto.

### Una dependencia en cada fase

No añadimos todas las dependencias al principio, sino **cada una en la fase que la necesita**. El motivo es que Spring Boot **reacciona a lo que encuentra** (sección 4): si añadimos la de base de datos sin haber configurado la conexión, la aplicación no arranca. Lo vimos en la fase 7.

### El Maven Wrapper (`mvnw`)

No hace falta instalar Maven. El proyecto incluye un pequeño programa, **`mvnw.cmd`** (en Windows), que descarga la versión correcta de Maven la primera vez y la usa. Por eso todos los comandos empiezan por `.\mvnw.cmd`.

---

## 4. Spring Boot: el que monta la aplicación

### La idea

Una aplicación tiene muchas piezas que dependen unas de otras: la conexión a la base de datos, los servicios, los controladores... Alguien tiene que **crearlas y conectarlas** en el orden correcto.

**Spring** es ese "alguien". Funciona como el **encargado de un almacén**:
- crea los objetos que la aplicación necesita;
- los guarda en un "almacén", que se llama **contexto de Spring**;
- cuando un objeto necesita otro, se lo entrega. A esto se le llama **inyección de dependencias**.

A cada objeto que Spring crea y guarda en el contexto se le llama **bean**.

**Spring Boot** añade una cosa más: la **autoconfiguración**. Mira qué librerías hay en el proyecto y prepara él solo lo necesario. Ejemplo real de la fase 7: al ver la librería de base de datos, intentó crear la conexión él solo, y como no tenía la dirección de la base de datos, falló.

### Spring Initializr

Para no empezar de cero, usamos [Spring Initializr](https://start.spring.io), una web oficial que genera el esqueleto de un proyecto. Eliges Maven, Java 25, Spring Boot 4.1.1 y el nombre del paquete (`com.daw.movies`), y te da un `.zip` listo para usar.

---

## 5. Las carpetas del proyecto

Maven espera encontrar cada cosa en una carpeta fija. Así no hay que explicarle dónde está nada:

```
prueba2_peliculas/
├── pom.xml                    ← la "receta" de Maven
├── mvnw.cmd                   ← Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/daw/movies/     ← NUESTRO CÓDIGO
│   │   └── resources/               ← configuración (application.yaml) y, desde la fase 8, el SQL
│   └── test/
│       └── java/com/daw/movies/     ← LOS TESTS
└── target/                    ← lo que genera Maven al compilar (no se sube a Git)
```

- Un **paquete** de Java es una carpeta: `package com.daw.movies;` significa que el archivo está en `com/daw/movies/`.
- Los tests están en **el mismo paquete** que el código, pero en otra carpeta (`test` en vez de `main`).

---

## 6. La clase de arranque y el contexto de Spring

Toda aplicación Spring Boot tiene una clase con el método `main`, que es por donde empieza Java:

```java
@SpringBootApplication                       // "esta es la aplicación Spring Boot"
public class MoviesApplication {

    public static void main(String[] args) {
        // Arranca Spring: crea el contexto, busca nuestras clases
        // y aplica la autoconfiguración
        SpringApplication.run(MoviesApplication.class, args);
    }
}
```

La anotación `@SpringBootApplication` hace tres cosas:
1. Marca esta clase como la principal.
2. Activa la **autoconfiguración** (sección 4).
3. **Busca nuestras clases** (las que lleven `@Service`, `@RestController`...) en el paquete `com.daw.movies` **y en los que estén dentro de él**.

**Error típico:** si creas una clase en `com.daw.otracosa`, Spring **no la encuentra**, porque está fuera de `com.daw.movies`.

### ¿Por qué la aplicación arranca y se cierra sola?

Al ejecutarla, sale `Started MoviesApplication` y termina. No es un fallo: todavía no hay nada que la mantenga encendida. En la fase 16 añadiremos un servidor web, que se queda esperando peticiones, y entonces seguirá en marcha hasta que la paremos.

---

## 7. Los tests

### Qué es un test

Un **test** es un trozo de código que **comprueba que otro código funciona**. Se ejecuta solo, tantas veces como quieras, y responde "bien" (verde) o "mal" (rojo).

Usamos **JUnit** (la librería de tests de Java) y **AssertJ** (para escribir comprobaciones que se leen casi como frases):

```java
@Test                                              // "esto es un test"
void sumaDosNumeros() {
    int resultado = 2 + 3;
    assertThat(resultado).isEqualTo(5);            // "comprueba que el resultado es 5"
}
```

Si la comprobación no se cumple, el test se pone en **rojo** y te dice qué esperaba y qué encontró.

### Nuestro primer test: `contextLoads`

```java
@SpringBootTest              // arranca la aplicación entera, como si fuera de verdad
class MoviesApplicationTests {

    @Test
    void contextLoads() {    // vacío a propósito
    }
}
```

Está vacío **a propósito**. `@SpringBootTest` arranca toda la aplicación antes de ejecutar el método. Si la aplicación no consigue arrancar (por una dependencia mal configurada, por ejemplo), el test falla. Es una **alarma**: "la aplicación ya no arranca".

### Failure frente a error

Cuando un test falla, Maven distingue dos casos:

| | Qué ha pasado | Ejemplo |
|---|---|---|
| **Failure** | El test se ejecutó, pero una comprobación no se cumplió | `assertThat(5).isEqualTo(6)` |
| **Error** | Algo explotó antes de poder comprobar nada | La aplicación no pudo arrancar |

### TDD: primero el test

Las normas del proyecto piden **TDD** (*Test-Driven Development*, desarrollo guiado por tests). Es un ciclo de tres pasos:

1. 🔴 **Rojo**: escribes un test de algo que todavía no existe. Falla, claro.
2. 🟢 **Verde**: escribes el código **mínimo** para que pase.
3. 🔵 **Mejorar**: ordenas el código sin romper el test.

Así cada línea de código tiene un test que demuestra que hace falta y que funciona.

---

## 8. Comandos y cómo leer lo que sale

| Comando | Qué hace |
|---|---|
| `.\mvnw.cmd test` | Compila y ejecuta todos los tests. **El que más usaremos** |
| `.\mvnw.cmd spring-boot:run` | Compila y arranca la aplicación |
| `.\mvnw.cmd clean` | Borra `target/` para empezar de cero |

`test` hace primero todo lo anterior (comprobar el `pom.xml` y compilar). Maven siempre sigue la misma lista de pasos en orden: comprobar → compilar → probar → empaquetar.

De toda la salida de `mvnw test`, lo importante está **al final**:

```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0     ← 3 tests, ninguno falla
[INFO] BUILD SUCCESS                                         ← todo bien
```

Si algo falla verás `BUILD FAILURE`. Entonces hay que buscar **hacia arriba** el primer `[ERROR]` o la última línea que empieza por `Caused by:`, que es la causa real.

**Avisos que puedes ignorar por ahora:** salen unas líneas `WARNING` que hablan de *Mockito* y *agent*. Mockito es una librería de tests que todavía no usamos. Java avisa de que en el futuro habrá que configurarla de otra forma. Hoy no rompe nada (decisión D-31).

---

## Lo que tienes que recordar

- **Git** guarda "fotos" (commits) del proyecto; **GitHub** es la copia en internet. Los secretos (`.env`) nunca se suben.
- **Maven** consigue las librerías (dependencias) que pide el `pom.xml`, compila y ejecuta los tests. Se usa con `.\mvnw.cmd`.
- **Spring Boot** crea y conecta los objetos de la aplicación (beans) y se configura solo según las librerías que encuentra.
- Nuestro código va en `src/main/java/com/daw/movies/` y los tests en `src/test/java/com/daw/movies/`.
- Un **test** comprueba que el código funciona: verde = bien, rojo = mal. Con TDD, primero el test.

## Preguntas de repaso

### Git

**1. ¿Qué es un commit?**
Un punto de guardado del proyecto: cómo estaban todos los archivos en ese momento, con un mensaje que explica qué cambió.

**2. ¿Para qué sirve `git add` si luego hago `git commit`?**
`git add` elige qué cambios entran en el próximo commit (meter cosas en la maleta). `git commit` crea el punto de guardado con esos cambios (cerrar la maleta).

**3. En `git status --short`, ¿qué significan `??`, `M`, `A` y `D`?**
`??`: archivo nuevo que Git no sigue todavía. `M`: modificado. `A`: añadido al próximo commit. `D`: borrado.

**4. ¿Qué diferencia hay entre el repositorio local y `origin`?**
El local es la carpeta de tu ordenador. `origin` es la copia en GitHub. `git push` sube tus commits; `git pull` trae los de GitHub.

**5. ¿Qué es una etiqueta y para qué las usamos?**
Un nombre fijo pegado a un commit, como un marcapáginas. Ponemos una al terminar cada fase (`fase-06`, `fase-07`) para poder ver el proyecto tal como estaba en ese momento.

**6. Hago `git checkout fase-06` y Git dice `detached HEAD`. ¿He roto algo?**
No. Significa que estás mirando un punto del pasado. Para volver al presente: `git checkout main`.

**7. ¿Qué tipo de archivos van en `.gitignore`? Pon un ejemplo de cada.**
Secretos (`.env`), archivos que se pueden volver a generar (`target/`) y archivos personales (`.idea/`).

**8. Si subo `.env` a GitHub y lo borro en el commit siguiente, ¿está a salvo la contraseña?**
No. El commit anterior sigue en la historia y cualquiera puede verlo. Habría que cambiar la contraseña.

**9. ¿Por qué `.env` está ignorado y `.env.example` no?**
Porque la regla de `.gitignore` es `.env` exactamente. `.env.example` es la plantilla sin secretos, y sí queremos subirla.

**10. Al hacer commit sale `CRLF will be replaced by LF`. ¿Es un error?**
No. Es `.gitattributes` aplicando su regla: el archivo se escribió en Windows (CRLF) y Git lo guarda con LF.

### Maven

**11. ¿Qué es Maven y qué es el `pom.xml`?**
Maven es la herramienta que construye el proyecto: consigue las librerías, compila y ejecuta los tests. El `pom.xml` es su "receta": dice qué es el proyecto y qué librerías necesita.

**12. ¿Qué es una dependencia?**
Una librería (código escrito por otros) que nuestro proyecto necesita. Se apunta en el `pom.xml` y Maven la descarga.

**13. ¿Dónde guarda Maven las librerías que descarga?**
En la carpeta `.m2` de tu usuario. Por eso la segunda vez no vuelve a descargarlas.

**14. ¿Por qué en el `pom.xml` las dependencias no llevan versión?**
Porque las elige Spring Boot, que trae una lista de versiones que funcionan bien juntas.

**15. ¿Qué diferencia hay entre los scopes `compile`, `runtime` y `test`?**
`compile`: se usa siempre. `runtime`: hace falta para ejecutar la aplicación, aunque el código no la nombre (el driver de MariaDB). `test`: solo existe al ejecutar los tests.

**16. ¿Qué es un starter?**
Una dependencia "en pack" que trae de golpe todo lo necesario para una tarea. Ejemplo: `spring-boot-starter-data-jpa`.

**17. ¿Por qué añadimos las dependencias de una en una, en su fase?**
Porque Spring Boot reacciona a lo que encuentra. Una dependencia sin configurar puede impedir que la aplicación arranque, con errores que todavía no sabríamos entender.

**18. ¿Por qué no hace falta instalar Maven?**
Porque el proyecto trae el Maven Wrapper (`mvnw.cmd`), que descarga la versión correcta y la usa.

### Spring Boot

**19. ¿Qué es el contexto de Spring?**
El "almacén" donde Spring guarda los objetos que crea para la aplicación.

**20. ¿Qué es un bean?**
Un objeto que crea Spring y guarda en el contexto para entregarlo a quien lo necesite.

**21. ¿Qué es la inyección de dependencias?**
Que Spring entrega a cada objeto los otros objetos que necesita, en vez de que los cree él mismo.

**22. ¿Qué es la autoconfiguración? Pon el ejemplo que vimos.**
Que Spring Boot prepara cosas él solo según las librerías que encuentra. En la fase 7, al ver la librería de base de datos, intentó crear la conexión; como no tenía la URL, falló.

**23. ¿Para qué sirve Spring Initializr?**
Para generar el esqueleto de un proyecto Spring Boot con las opciones que elijas, sin empezar de cero.

**24. ¿Qué tres cosas hace `@SpringBootApplication`?**
Marca la clase principal, activa la autoconfiguración y busca nuestras clases en `com.daw.movies` y sus subpaquetes.

**25. Creo la clase `com.daw.services.MovieService` con `@Service`. ¿La encontrará Spring?**
No. Spring solo busca dentro de `com.daw.movies` y sus subpaquetes.

**26. ¿Por qué la aplicación arranca y se cierra sola?**
Porque todavía no hay nada que la mantenga encendida. Cuando haya servidor web (fase 16), se quedará esperando peticiones.

### Carpetas

**27. ¿Dónde va nuestro código, dónde los tests y dónde la configuración?**
Código: `src/main/java/com/daw/movies/`. Tests: `src/test/java/com/daw/movies/`. Configuración: `src/main/resources/`.

**28. ¿Qué es la carpeta `target/` y por qué no se sube a Git?**
Lo que genera Maven al compilar. Se puede volver a generar en cualquier momento, así que subirla solo ensuciaría el repositorio.

### Tests

**29. ¿Qué es un test?**
Código que comprueba que otro código funciona. Se ejecuta solo y da verde (bien) o rojo (mal).

**30. ¿Qué hace `assertThat(resultado).isEqualTo(5)`?**
Comprueba que `resultado` vale 5. Si no, el test se pone en rojo diciendo qué esperaba y qué encontró.

**31. ¿Qué hace `@SpringBootTest`?**
Arranca la aplicación completa antes de ejecutar los tests de esa clase.

**32. ¿Qué comprueba `contextLoads()` si está vacío?**
Que la aplicación consigue arrancar. Si no arranca, el test falla antes de llegar al método.

**33. ¿Qué diferencia hay entre un *failure* y un *error*?**
*Failure*: el test se ejecutó, pero una comprobación no se cumplió. *Error*: algo explotó antes de poder comprobar (por ejemplo, la aplicación no arrancó).

**34. ¿Cuáles son los tres pasos de TDD?**
Rojo (escribir un test que falla), verde (el código mínimo para que pase) y mejorar (ordenar el código sin romper el test).

### Comandos

**35. ¿Qué comando ejecuta los tests? ¿Y cuál arranca la aplicación?**
`.\mvnw.cmd test` y `.\mvnw.cmd spring-boot:run`.

**36. ¿Dónde miro primero en la salida de `mvnw test`?**
Al final: `Tests run: ..., Failures: ..., Errors: ...` y `BUILD SUCCESS` o `BUILD FAILURE`.

**37. Sale `BUILD FAILURE`. ¿Dónde busco la causa?**
Hacia arriba: el primer `[ERROR]` y, en los errores largos, la última línea `Caused by:`.

**38. ¿Son graves los avisos `WARNING` de Mockito?**
No, de momento. Avisan de que en futuras versiones de Java habrá que configurar Mockito de otra forma. Hoy no rompen nada (D-31).

---

## Referencia: plan de dependencias

*Para consultar; no hace falta memorizarlo.* Cada dependencia entra en su fase (D-26). Los nombres son los de Spring Boot 4.1.1; algunos cambiaron respecto a la versión 3, que sale en muchos tutoriales (por ejemplo, `spring-boot-starter-web` ahora se llama `spring-boot-starter-webmvc`).

| Fase | Dependencia | Para qué |
|---|---|---|
| 6 | `spring-boot-starter` | El núcleo de Spring Boot |
| 6 | `spring-boot-starter-test` (test) | JUnit, AssertJ, Mockito |
| 7 | `spring-boot-starter-data-jpa` | Trabajar con la base de datos desde Java (JPA, Hibernate, conexiones) |
| 7 | `mariadb-java-client` (runtime) | El driver que habla con MariaDB |
| 7 | `spring-boot-starter-data-jpa-test` (test) | Herramientas para probar la parte de base de datos |
| 8 | `spring-boot-starter-flyway` + `flyway-mysql` | Crear las tablas con migraciones (`flyway-mysql` sirve también para MariaDB) |
| 16 | `spring-boot-starter-webmvc` (+ `-test`) | Servidor web y controladores REST |
| 18 | `spring-boot-starter-validation` | Validar datos (`@NotBlank`, `@Valid`) |
| 19 | `spring-boot-testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-mariadb` (test) | Un MariaDB desechable para los tests |
| 22, 24 (opcionales) | MapStruct; `spring-boot-starter-security` + JWT | Mappers automáticos; seguridad |

**No las usamos** (D-27): Lombok (oculta código que queremos aprender), DevTools, Docker Compose Support (haría sola la conexión que aprendemos en la fase 7), H2 (no es MariaDB), Actuator.
