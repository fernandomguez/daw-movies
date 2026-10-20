# 01 — Inspección inicial y herramientas

## Objetivo

Saber de dónde partimos antes de escribir una sola línea de código: qué normas tiene el proyecto, qué hay en el repositorio, qué herramientas están instaladas y si las versiones que queremos usar existen y son compatibles.

## 1. Normas del proyecto (`base-standards.md`)

| Norma | Qué significa para nosotros |
|---|---|
| Pasos pequeños y cambios incrementales | Un paso cada vez; cada cambio pequeño y explicado. |
| Nombres claros y descriptivos | `releaseYear` mejor que `ry`; `MovieRepository` mejor que `Repo`. |
| Cuestionar suposiciones | Ante dudas, preguntar antes de decidir. |
| Detectar código duplicado | Ejemplo previsto: `Director` y `Actor` comparten atributos (lo analizaremos en la fase 3). |
| **Código en inglés** | Clases, métodos, variables, tablas, columnas, tests y commits en inglés: `Movie`, `movie_actor`, `release_year`. |
| **Todo lo demás en español** | Comentarios, documentación, mensajes de error y logs. |
| **TDD** | Escribir el test antes que la funcionalidad. Afecta al plan: las pruebas aparecen desde la fase 6, no solo al final. |
| Estándares de backend | Referencia a `backend-standards.md`. **No existe** (confirmado: no aplica). |
| Skills del proyecto | Referencia a una carpeta `skills`. **No existe** (confirmado: no aplica). |

### Cómo afectan a la arquitectura, la documentación y el desarrollo

- **Arquitectura:** no imponen ninguna estructura concreta; las tres capas y la inversión de dependencias las decidimos nosotros (fase 5).
- **Documentación:** en español. No hay una estructura documental impuesta, así que usamos la de esta guía.
- **Desarrollo:** TDD y nombres en inglés en todo el código, incluido el SQL.

## 2. Estado del repositorio

| Elemento | Estado |
|---|---|
| `base-standards.md` | Existe (normas generales). |
| `claude.md` | Existe pero está **vacío** (0 bytes). |
| `backend-standards.md` | No existe. |
| `skills/` | No existe. |
| Git | La carpeta **no** es un repositorio Git todavía. |
| Código, `pom.xml`, Docker | Nada todavía: partimos de cero. |

## 3. Herramientas instaladas

Comandos ejecutados (PowerShell):

```powershell
java -version
javac -version
mvn -v
docker --version
docker compose version
docker info
git --version
```

| Herramienta | Versión detectada | ¿Sirve? |
|---|---|---|
| Java (Temurin) | 25.0.4.1 LTS | ✅ |
| `JAVA_HOME` | `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot` | ✅ |
| Maven | 3.9.16 (usa Java 25) | ✅ (Spring Boot pide 3.6.3 o superior) |
| Docker | 29.8.0 | ✅ instalado |
| Docker Compose | v5.5.1 | ✅ |
| Motor de Docker | **No estaba arrancado** (`failed to connect to the docker API ... dockerDesktopLinuxEngine`) | ⚠️ Hay que abrir Docker Desktop antes de la fase 4 |
| Git | 2.51.0 | ✅ |

## 4. Versiones verificadas en fuentes oficiales

| Componente | Versión | Fuente | Comentario |
|---|---|---|---|
| Spring Boot | 4.1.1 | Maven Central (`spring-boot-dependencies`) | Existe y es la última versión estable de la rama 4.1 (hay una 4.2 en fase *milestone*, no estable). |
| Java | 25 | Documentación de Spring Boot 4.1.1 (requisitos del sistema) | Boot 4.1.1 admite de Java 17 a Java 26. ✅ |
| Spring Framework | 7.0.9 | BOM de Boot 4.1.1 | Lo gestiona Spring Boot. |
| Hibernate | 7.4.5.Final | BOM de Boot 4.1.1 | Implementación de JPA 3.2. |
| Flyway | 12.4.0 | BOM de Boot 4.1.1 | Para MariaDB necesita además el módulo `flyway-mysql`. |
| Driver MariaDB | 3.5.10 | BOM de Boot 4.1.1 | `org.mariadb.jdbc:mariadb-java-client`. |
| JUnit | 6.0.3 | BOM de Boot 4.1.1 | |
| Testcontainers | 2.0.5 | BOM de Boot 4.1.1 | Para la fase 19. |
| MariaDB (imagen Docker) | **12.3** (decidido) | mariadb.org | LTS mantenidas: 10.11, 11.4, 11.8, 12.3. Elegimos la más reciente. |

**Idea clave — el BOM:** Spring Boot publica una lista de versiones probadas juntas (*Bill of Materials*). Si usamos el padre de Spring Boot en el `pom.xml`, no escribimos las versiones de Hibernate, Flyway o el driver: las hereda. Así evitamos incompatibilidades.

## 5. Posibles incompatibilidades y puntos de atención

1. **Flyway + MariaDB:** desde Flyway 10, cada base de datos va en un módulo aparte. Para MariaDB hace falta `flyway-mysql` además del núcleo. Si se olvida, Flyway falla al arrancar con un error de base de datos no soportada.
2. **Spring Boot 4 es modular:** respecto a Spring Boot 3, varios *starters* se reorganizaron (por ejemplo, hay *starter* específico para Flyway). Lo comprobaremos en la documentación oficial al crear el `pom.xml` (fase 6).
3. **Versión de MariaDB y Flyway:** la documentación de Flyway lista como verificadas versiones de MariaDB hasta la 11.7. MariaDB 11.8 está muy próxima y es LTS; 12.3 es más nueva y está menos probada con Flyway.
4. **Docker Desktop** debe estar en ejecución para cualquier comando de contenedores.

## Resumen

- Las versiones pedidas (Java 25, Spring Boot 4.1.1, Maven) existen y son compatibles con lo instalado.
- Usaremos MariaDB 12.3. Falta arrancar Docker Desktop antes de la fase 4.
- `base-standards.md` hace referencia a archivos que no existen (`backend-standards.md`, `skills/`); confirmado que no aplican.
- La norma de TDD cambia el plan: las pruebas acompañan cada fase.

## Preguntas de repaso

**¿Por qué no escribimos la versión de Hibernate en el `pom.xml`?**
Porque el BOM de Spring Boot ya fija una versión probada con el resto de componentes. Escribirla a mano podría romper esa compatibilidad.

**¿Qué ocurre si usamos Flyway con MariaDB sin el módulo `flyway-mysql`?**
Flyway no reconoce la base de datos y la aplicación no arranca.
