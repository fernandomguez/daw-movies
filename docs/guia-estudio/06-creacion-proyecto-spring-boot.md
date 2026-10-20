# Fase 6 — Creación del proyecto Spring Boot

> Estado: **completada** (2026-10-20). Conceptos: [temario/06-proyecto-spring-boot-y-git.md](temario/06-proyecto-spring-boot-y-git.md).

## Objetivo

Tener un proyecto Spring Boot 4.1.1 con Java 25, bajo control de versiones con Git, que arranque y pase un primer test.

## Pasos

| Paso | Estado |
|---|---|
| 6.1 Git y `.gitignore` | ✅ Hecho |
| 6.2 Generar el proyecto | ✅ Hecho |
| 6.3 Dependencias mínimas | ✅ Hecho (solo `spring-boot-starter` y `spring-boot-starter-test`; plan del resto documentado) |
| 6.4 Estructura Maven | ✅ Hecho |
| 6.5 Primer arranque y primer test | ✅ Hecho |

## 6.1 Git y `.gitignore`

### Situación de partida (comprobada)

- Git 2.51.0 instalado, con `user.name`, `user.email` e `init.defaultBranch = main` ya configurados.
- `core.autocrlf = true` a nivel de **sistema** (`C:/Program Files/Git/etc/gitconfig`, lo configura el instalador de Git para Windows). *Corrección:* al principio se comprobó solo con `git config --global core.autocrlf`, que no devolvía nada, y se documentó por error como "sin configurar". El comando que muestra todos los niveles es `git config --show-origin --get-all core.autocrlf`.
- La carpeta no era un repositorio. Ya existía un `.gitignore` de la fase 4 que solo ignoraba `.env`.

### Archivos

| Archivo | Cambio | Para qué |
|---|---|---|
| `.gitignore` | Ampliado | Además de `.env`, ignora `target/` (compilación de Maven), la configuración del IDE (`.idea/`, `*.iml`, `.vscode/`), archivos del sistema operativo y logs. |
| `.gitattributes` | Nuevo | El repositorio guarda siempre finales de línea LF; los scripts `.cmd`/`.bat` de Windows, CRLF. |

### Comandos y resultados reales

```
> git init
Initialized empty Git repository in C:/Users/ferna/Desktop/FP DAW/2o curso/César/SpringBoot/prueba2_peliculas/.git/

> git status --short --untracked-files=all
?? .env.example
?? .gitattributes
?? .gitignore
?? base-standards.md
?? claude.md
?? compose.yaml
?? docs/guia-estudio/...        (15 archivos de documentación)

> git check-ignore -v .env .env.example
.gitignore:2:.env	.env
```

Cómo leerlo:

- `??` significa *untracked*: Git ve el archivo pero todavía no lo sigue.
- `.env` **no aparece** en `git status`: está ignorado. `check-ignore` dice qué regla lo ignora (línea 2 de `.gitignore`).
- Para `.env.example`, `check-ignore` no imprime nada: **no** está ignorado, así que se versionará (es la plantilla sin secretos).

### Decisiones

- [D-24](decisiones-tecnicas.md): finales de línea fijados con `.gitattributes` en el repositorio, no con la configuración personal de cada ordenador.

### Primer commit y subida a GitHub

```
> git add -A
> git commit -m "chore: initial project setup with docs, Docker Compose and git config"
warning: in the working copy of '.env.example', CRLF will be replaced by LF the next time Git touches it

> git log --oneline
f1d7a62 chore: initial project setup with docs, Docker Compose and git config

> git remote add origin git@github.com:fernandomguez/daw-movies.git
> git push -u origin main
branch 'main' set up to track 'origin/main'.
To github.com:fernandomguez/daw-movies.git
 * [new branch]      main -> main
```

- El commit incluye **22 archivos** (los 20 de antes más los dos documentos de esta fase).
- **El aviso de `.env.example`** demuestra que `.gitattributes` funciona: ese archivo se escribió en Windows con CRLF y Git lo guarda en el repositorio con LF. No es un error.
- Comprobado tras el push: en el commit está `.env.example` y **no** está `.env` (`git ls-tree -r --name-only HEAD`).
- `-u` deja `main` enlazada con `origin/main`: a partir de ahora basta con `git push` y `git pull`.

Repositorio remoto: `git@github.com:fernandomguez/daw-movies.git`.

### Reorganización del historial: un commit por fase (D-30)

Durante la fase 6 llegaron a hacerse 5 commits. El alumno decidió tener **un commit por fase**, así que el historial se reescribió:

1. Copia de seguridad local: `git branch backup/historial-original` (apunta al último commit antiguo, `b166f79`).
2. A partir del primer commit (`f1d7a62`) se reconstruyó el estado del final de la fase 5. Para ello se quitó lo que había añadido el paso 6.1: `.gitattributes`, los documentos de la fase 6, las reglas nuevas de `.gitignore`, la D-24, las entradas de Git del glosario y la fila de la fase 6 del README de la guía. El resultado es un commit raíz nuevo:
   ```
   d2a88c4 (tag: fase-05) docs(fases-01-05): domain analysis, schema design, Docker MariaDB and architecture
   ```
3. `git reset --mixed d2a88c4`: `main` apunta a ese commit y **todo el trabajo de la fase 6 queda en los archivos, sin commit**. Se comprobó que, comparado con el último commit antiguo, solo cambiaban las correcciones de documentación hechas después y el permiso de ejecución de `mvnw`.
4. Etiqueta `fase-05` y `git push --force-with-lease` para sustituir el historial de GitHub.

Desde ahora, la fase 6 se confirmará en **un solo commit** (con la etiqueta `fase-06`) al terminar el paso 6.5.

### Corrección: `core.autocrlf`

Al reconstruir la fase 5, Git mostró avisos `LF will be replaced by CRLF` en un directorio sin `.gitattributes`. Al investigarlo:

```
> git config --show-origin --get-all core.autocrlf
file:C:/Program Files/Git/etc/gitconfig	true
```

`autocrlf` **sí** estaba configurado, a nivel de sistema. En el paso 6.1 solo se había mirado `--global`. La decisión D-24 sigue siendo válida: `.gitattributes` tiene prioridad sobre `autocrlf`.

### Paquete base

El paquete base será **`com.daw.movies`** ([D-25](decisiones-tecnicas.md)). Se usará en el paso 6.2.

## 6.2 Generar el proyecto con Spring Initializr

### Comprobación previa

La API de Initializr (`https://start.spring.io`, consultada el 2026-10-20) ofrece:

- Spring Boot: `4.2.0-SNAPSHOT`, `4.2.0-M2`, `4.1.2-SNAPSHOT`, **`4.1.1`** (versión por defecto, la estable), `4.0.9-SNAPSHOT`, `4.0.8`.
- Java: 27, **25**, 21, 17.

### Generación

Se descargó el proyecto con la API de Initializr (equivale a rellenar el formulario web), **en una carpeta temporal**, para revisarlo antes de copiarlo:

```
https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=4.1.1
  &javaVersion=25&groupId=com.daw&artifactId=movies&name=movies
  &packageName=com.daw.movies&packaging=jar
  &description=Learning project: movies REST API
```

Contenido del `.zip`:

```
.gitattributes  .gitignore  HELP.md  mvnw  mvnw.cmd  pom.xml
.mvn/wrapper/maven-wrapper.properties        (Maven 3.10.0, wrapper 3.3.4)
src/main/java/com/daw/movies/MoviesApplication.java
src/main/resources/application.properties    (spring.application.name=movies)
src/test/java/com/daw/movies/MoviesApplicationTests.java
```

### Qué se copió al proyecto y qué no

| Archivo | Decisión |
|---|---|
| `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `src/` | Copiados |
| `pom.xml` | Se borraron las etiquetas vacías que pone Initializr (`<url/>`, `<licenses>`, `<developers>`, `<scm>`): no contenían nada |
| `.gitignore` | No se sustituyó: se añadieron al nuestro sus líneas útiles (`.mvn/wrapper/maven-wrapper.jar`, Eclipse/STS, NetBeans, excepciones `!**/src/main/**/target/`) |
| `.gitattributes` | No se copió: el nuestro (`* text=auto eol=lf` + `*.cmd` CRLF) ya cubre `mvnw` y `mvnw.cmd` |
| `HELP.md` | No se copió: solo son enlaces de ayuda |

## 6.3 Dependencias mínimas

Solo las dos que trae Initializr por defecto:

| Dependencia | Scope |
|---|---|
| `spring-boot-starter` | compile |
| `spring-boot-starter-test` | test |

El resto se añadirá en la fase que la necesite ([D-26](decisiones-tecnicas.md)). La lista completa, con los nombres reales de Boot 4.1.1 y las dependencias descartadas, está en el [temario, sección 6](temario/06-proyecto-spring-boot-y-git.md#6-dependencias-cuáles-y-por-qué). Los nombres se comprobaron generando un `pom.xml` de prueba con Initializr que incluía todas las dependencias del plan.

## 6.4 Estructura Maven

Paso explicativo: no cambia ningún archivo. Estructura real del proyecto (sin `docs/` ni `.git/`):

```
./.env  ./.env.example  ./.gitattributes  ./.gitignore  ./README.md  ./base-standards.md
./claude.md  ./compose.yaml  ./mvnw  ./mvnw.cmd  ./pom.xml
./.mvn/wrapper/maven-wrapper.properties
./src/main/java/com/daw/movies/MoviesApplication.java
./src/main/resources/application.properties
./src/test/java/com/daw/movies/MoviesApplicationTests.java
```

`target/` todavía **no existe** (`ls target` → `No such file or directory`): aparecerá al compilar por primera vez en el paso 6.5.

Explicación de cada carpeta, de `@SpringBootApplication` y del test `contextLoads`: [temario/06, sección 7](temario/06-proyecto-spring-boot-y-git.md#7-estructura-de-un-proyecto-maven).

## 6.5 Primer arranque y primer test

### Entorno

```
> java -version
openjdk version "25.0.4.1" 2026-08-18 LTS
OpenJDK Runtime Environment Temurin-25.0.4.1+1 (build 25.0.4.1+1-LTS)

JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot
```

### `.\mvnw.cmd test`

Tardó 14 segundos y **no descargó nada** (0 líneas `Downloading`): Maven 3.10.0 (`~/.m2/wrapper/dists/apache-maven-3.10.0`) y las dependencias ya estaban en el ordenador, de otros proyectos. En un equipo nuevo, la primera ejecución tardaría más.

Extracto de la salida real:

```
[INFO] Building movies 0.0.1-SNAPSHOT
[INFO] --- resources:3.5.0:resources (default-resources) @ movies ---
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] --- compiler:3.15.0:compile (default-compile) @ movies ---
[INFO] Compiling 1 source file with javac [debug parameters release 25] to target\classes
[INFO] --- resources:3.5.0:testResources (default-testResources) @ movies ---
[INFO] skip non existing resourceDirectory ...\src\test\resources
[INFO] --- compiler:3.15.0:testCompile (default-testCompile) @ movies ---
[INFO] Compiling 1 source file with javac [debug parameters release 25] to target\test-classes
[INFO] --- surefire:3.5.6:test (default-test) @ movies ---
[INFO] Running com.daw.movies.MoviesApplicationTests
... Found @SpringBootConfiguration com.daw.movies.MoviesApplication for test class com.daw.movies.MoviesApplicationTests
 :: Spring Boot ::                (v4.1.1)
... Starting MoviesApplicationTests using Java 25.0.4.1 with PID 19920
... No active profile set, falling back to 1 default profile: "default"
... Started MoviesApplicationTests in 0.965 seconds (process running for 2.673)
Mockito is currently self-attaching to enable the inline-mock-maker. ...
WARNING: A Java agent has been loaded dynamically (...byte-buddy-agent-1.18.11.jar)
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  10.602 s
```

- `release 25`: compila para Java 25, la versión del `pom.xml` (`<java.version>25</java.version>`).
- `skip non existing resourceDirectory ...\src\test\resources`: esa carpeta no existe todavía. No es un error.
- Los avisos de Mockito están explicados en el [temario, sección 8](temario/06-proyecto-spring-boot-y-git.md#8-el-ciclo-de-vida-de-maven-y-el-primer-test) (D-31).

### `.\mvnw.cmd spring-boot:run`

```
[INFO] --- spring-boot:4.1.1:run (default-cli) @ movies ---
... Starting MoviesApplication using Java 25.0.4.1 with PID 9236
... No active profile set, falling back to 1 default profile: "default"
... Started MoviesApplication in 0.978 seconds (process running for 1.495)
[INFO] BUILD SUCCESS
```

La aplicación arranca y **termina sola**, porque todavía no hay servidor web que la mantenga viva.

En la consola, la ruta aparecía como `CÃ©sar` en vez de `César`. Es solo un problema al **mostrar** la salida: el log se leyó con una codificación distinta a la que escribió Java. No afecta al programa.

### `target/`

Se crearon `target/classes`, `target/test-classes`, `target/surefire-reports` (informe del test en `.txt` y `.xml`) y otras carpetas auxiliares. Git lo ignora:

```
> git check-ignore -v target/
.gitignore:5:target/	target/
```

## Resumen de la fase 6

- Repositorio Git con un commit por fase (D-30) y subido a GitHub.
- Proyecto Spring Boot 4.1.1 / Java 25 generado con Initializr, con Maven Wrapper y solo las dependencias mínimas (D-26).
- Plan de dependencias para el resto del proyecto, con los nombres reales de Boot 4 (D-26, D-27).
- Diseño revisado: dominio con JPA y `@Service` (D-19, D-29).
- Primer test (`contextLoads`) en verde: **`Tests run: 1, Failures: 0, Errors: 0`**.
