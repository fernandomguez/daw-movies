# Decisiones técnicas

Formato: contexto → alternativas → decisión → motivo. Las decisiones "pendientes" esperan confirmación.

## D-01 Versiones base — ✅ Decidida (requisito del proyecto)
Java 25, Spring Boot 4.1.1 y Maven. Verificadas en fuentes oficiales (ver [01-inspeccion-inicial.md](01-inspeccion-inicial.md)). No se cambian sin consultarlo.

## D-02 Idioma del código — ✅ Decidida (impuesta por `base-standards.md`)
Código, nombres de tablas y columnas, tests y commits en inglés. Comentarios, documentación y mensajes en español.

## D-03 Pruebas desde el principio — ✅ Decidida
- **Contexto:** `base-standards.md` exige TDD; el plan original situaba las pruebas al final.
- **Decisión:** pruebas unitarias desde la fase 6 (test primero cuando haya lógica Java) y pruebas de integración en la fase 19. Lo que no es código Java (Docker, SQL) se comprueba con comandos y consultas documentados.

## D-04 Versión de MariaDB — ✅ Decidida: `mariadb:12.3`
- **Alternativas:** 11.4 LTS, 11.8 LTS, 12.3 LTS.
- **Decisión:** 12.3, la LTS más reciente (soporte comunitario hasta junio de 2029).
- **Riesgo conocido:** la documentación de Flyway verifica MariaDB hasta la 11.7. Es posible que Flyway muestre un aviso de versión no probada; si aparece, lo analizaremos en la fase 8 antes de decidir nada.

## D-05 Guía de estudio — ✅ Decidida
- Un archivo por fase (lo que hicimos), creado al empezar la fase.
- Carpeta `temario/` con los conceptos explicados mediante ejemplos de este proyecto. Los archivos se numeran con la fase del plan a la que pertenecen (`11-introduccion-jpa.md`), para que el temario se lea en orden de aprendizaje aunque un tema se explique por adelantado.
- `decisiones-tecnicas.md` y `glosario.md` (definiciones cortas que enlazan al temario).

## D-06 Orden de las fases — ✅ Aprobado
Diseño del esquema antes que Docker; dominio antes que JPA. Motivos en [00-plan-de-aprendizaje.md](00-plan-de-aprendizaje.md).
JPA se mantiene en la fase 11, con una introducción previa en [temario/11-introduccion-jpa.md](temario/11-introduccion-jpa.md).

## D-07 Alcance inicial del dominio — ✅ Aprobado
- **Tablas:** `director`, `movie`, `actor` y `movie_actor` (tabla intermedia, PK compuesta).
- **Datos:** 10 películas reales, 5 directores y 14 actores, con actores compartidos entre películas.
- **Excluidos a propósito:** *The Matrix* (dos directoras, no encaja con "un director principal") y entidades extra (géneros, productoras, etc.).

## D-08 Normas del proyecto — ✅ Confirmado
Solo existe `base-standards.md`. Las referencias a `backend-standards.md` y a la carpeta `skills/` no aplican.

## D-09 Atributos del modelo conceptual — ✅ Decidido (fase 2)
| Decisión | Alternativa descartada | Motivo |
|---|---|---|
| Año de estreno (entero) | Fecha completa | La fecha de estreno varía según el país; el año es estable. |
| Duración en minutos (entero) | "2 h 28 min" como texto | Un número se compara y ordena; el formato de horas se calcula al mostrarlo. |
| Nombre y apellidos separados | Nombre completo en un campo | Permite ordenar y buscar por apellido. Casos límite: "del Toro", "Samuel L.". |
| Fecha de nacimiento | Edad | La edad cambia; se calcula a partir de la fecha. |
| Identificador propio para cada entidad | Título como identificador | El título puede repetirse (remakes) o corregirse. |
| Director y actor separados (provisional) | Entidad "persona" | Más sencillo; se revisa en la fase 3 (caso Tarantino). |

## D-10 Fecha de nacimiento obligatoria — ✅ Decidido (fase 3)
- **Alternativas:** obligatoria (`NOT NULL`) u opcional (admite `NULL`).
- **Decisión:** obligatoria.
- **Motivo:** todos nuestros datos la tienen y evita tratar valores vacíos en Java mientras aprendemos lo básico. Si en el futuro hiciera falta, convertirla en opcional sería un buen ejercicio de migración con Flyway.

## D-11 Director y actor en tablas separadas — ✅ Decidido (fase 3)
- **Alternativas:** dos tablas; una tabla `person` con papeles.
- **Decisión:** dos tablas.
- **Motivo:** más fácil de entender y consultar. La duplicación es de estructura (4 columnas), no de datos: en nuestro conjunto nadie es director y actor a la vez. Se revisaría si fuera habitual.

## D-12 Tipos de datos — ✅ Decidido (fase 3)
| Dato | Tipo | Alternativa descartada | Motivo |
|---|---|---|---|
| Identificadores | `BIGINT` | `INT` | Se corresponde con `Long` en Java, el tipo habitual para identificadores. |
| Año de estreno | `INT` | `YEAR`, `SMALLINT` | `YEAR` tiene un rango limitado y no se corresponde bien con Java; `SMALLINT` obligaría a usar `Short`. `INT` → `Integer`. |
| Duración | `INT` | `SMALLINT` | Mismo motivo. |
| Fecha de nacimiento | `DATE` | `DATETIME` | No necesitamos la hora. `DATE` → `LocalDate`. |

## D-13 Comportamiento al borrar (`ON DELETE`) — ✅ Decidido (fase 3)
| FK | Opción | Motivo |
|---|---|---|
| `movie.director_id` | `RESTRICT` | No borrar películas a escondidas; `SET NULL` imposible porque la columna es obligatoria. |
| `movie_actor.movie_id` | `CASCADE` | Las participaciones no tienen sentido sin su película. |
| `movie_actor.actor_id` | `RESTRICT` | No sacar a un actor de sus películas en silencio. |

## D-14 Clave primaria de `movie_actor` — ✅ Decidido (fase 3)
- **Alternativas:** PK compuesta `(movie_id, actor_id)`; columna `id` propia + `UNIQUE (movie_id, actor_id)`.
- **Decisión:** PK compuesta.
- **Motivo:** la tabla no tiene datos propios y la PK compuesta ya impide repetir la misma pareja. La alternativa tendría sentido si la participación pasara a tener datos (fase opcional 23).

## D-15 Puerto 3307 para MariaDB en Docker — ✅ Decidido (fase 4)
- **Contexto:** el 3306 lo ocupa un MariaDB instalado como servicio de Windows.
- **Alternativas:** parar o desinstalar el servicio de Windows; publicar el contenedor en otro puerto.
- **Decisión:** `"3307:3306"`, configurable con `MARIADB_PORT` en `.env`.
- **Motivo:** no tocar software del sistema que puede usarse en otras asignaturas. La aplicación deberá conectarse a `localhost:3307`.

## D-16 Juego de caracteres — ✅ Decidido (fase 4)
`utf8mb4` con `utf8mb4_uca1400_ai_ci`, escrito explícitamente en `compose.yaml` aunque coincide con el valor por defecto de MariaDB 12.3, para no depender de él. Comprobado: las tildes se guardan correctamente.

## D-17 Credenciales y usuario de la aplicación — ✅ Decidido (fase 4)
- Contraseñas en `.env` (excluido por `.gitignore`); `.env.example` con `change_me` como plantilla versionada.
- La aplicación usará el usuario `movies_app`, con permisos solo sobre la base de datos `movies` (mínimo privilegio). `root` queda solo para administración.

## D-18 Paquetes organizados por capa — ✅ Decidido (fase 5)
- **Alternativas:** por capa (`controller`, `domain`, `persistence`); por funcionalidad (`movie`, `director`, `actor`).
- **Decisión:** por capa, más `config/` para el ensamblaje.
- **Motivo:** el objetivo es aprender las capas, y con tres entidades la organización por capa es clara. Los paquetes se crean cuando se necesitan.

## D-19 Dominio sin anotaciones de Spring — ⏳ Pendiente de confirmar (se aplica en la fase 12)
- **Alternativas:** (A) dominio puro y una clase `@Configuration` en `config/` que crea sus objetos con `@Bean`; (B) permitir `@Service` en los servicios del dominio.
- **Recomendación:** A. Cumple la regla "el dominio no depende de Spring" y hace visible la inyección de dependencias.
- **Relacionado:** dónde colocar `@Transactional` se decide en la fase 17.

## D-20 Repositorio (puerto + adaptador) en lugar de DAO — ✅ Decidido (consecuencia de D-18 y de la DIP, fase 5)
- **Alternativas:** (A) Repositorio: interfaz definida por el dominio (`MovieRepository`) e implementada por un adaptador con Spring Data; (B) DAO: interfaz en la persistencia (`MovieDao` + `MovieDaoImpl`) que usa directamente el servicio.
- **Decisión:** A. Con DAO, el servicio dependería de la persistencia, que es justo lo que la DIP evita. Se justificará con código en el paso 12.1.
- **Explicación:** [temario/05](temario/05-arquitectura-tres-capas-dip.md), secciones 8 y 10.

## D-21 Servicio como interfaz + implementación (`MovieService` + `MovieServiceImpl`) — ⏳ Pendiente (se decide en la fase 16)
- **Alternativas:** (A) una sola clase `MovieService`; (B) interfaz + `Impl`, muy habitual en clase.
- **A valorar:** con un único servicio y tests que usan repositorios falsos, la interfaz puede no aportar nada; pero conviene conocer el patrón.

## D-22 Nombre del adaptador: `MovieRepositoryAdapter` (no `MovieRepositoryImpl`) — ✅ Decidido
- **Alternativas:** `MovieRepositoryAdapter` o `MovieRepositoryImpl`. Las dos cumplen el mismo papel: implementar el puerto `MovieRepository`.
- **Decisión:** `Adapter`. Coincide con el vocabulario de puertos y adaptadores del tema 5 y dice más que `Impl`: traduce entre el dominio y Spring Data/JPA. Además, Spring Data da un significado especial al sufijo `Impl` (implementaciones de métodos personalizados de repositorios), así que evitarlo previene confusiones.

## D-23 Seguridad con JWT — ✅ Añadida al plan como fase opcional 24
- **Motivo:** el alumno lo verá en clase. Va al final porque necesita la API terminada (depende de la fase 18).
