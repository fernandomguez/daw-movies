# 03 — Diseño del esquema relacional

> **Conceptos y diseño completo:** [temario/03-diseno-esquema-relacional.md](temario/03-diseno-esquema-relacional.md).

## Objetivo

Convertir el modelo conceptual de la fase 2 en un esquema de tablas listo para crearse con Flyway en la fase 8.

## Qué hicimos

| Paso | Resultado |
|---|---|
| 3.1 De entidad a tabla | Tablas `director`, `actor`, `movie` (singular, `snake_case`, en inglés). Tipos: `BIGINT`, `VARCHAR`, `INT`, `DATE`. |
| 3.2 Claves primarias | Claves sustitutas `id BIGINT AUTO_INCREMENT`. |
| 3.3 Relación 1:N | `movie.director_id` (`NOT NULL`) → `director.id`. |
| 3.4 Relación N:M | Tabla intermedia `movie_actor` con PK compuesta `(movie_id, actor_id)`. |
| 3.5 Restricciones | `NOT NULL` en todas las columnas; `CHECK` de duración y año; `ON DELETE` según la tabla de abajo. |
| 3.6 Diagrama ER | Diagrama Mermaid y versión en texto en el temario. |

## Esquema resultante

| Tabla | Columnas | Claves y restricciones |
|---|---|---|
| `director` | `id`, `first_name`, `last_name`, `birth_date`, `biography` | PK `id`; `biography` opcional (`NULL`) |
| `actor` | `id`, `first_name`, `last_name`, `birth_date`, `biography` | PK `id`; `biography` opcional (`NULL`) |
| `movie` | `id`, `title`, `release_year`, `duration_minutes`, `director_id` | PK `id`; FK `director_id` (`RESTRICT`); `CHECK duration_minutes > 0`; `CHECK release_year >= 1888` |
| `movie_actor` | `movie_id`, `actor_id` | PK `(movie_id, actor_id)`; FK `movie_id` (`CASCADE`); FK `actor_id` (`RESTRICT`) |

Orden de creación e inserción: `director` → `actor` → `movie` → `movie_actor`.

> **Revisado en la fase 8 (paso 8.2, [D-37](decisiones-tecnicas.md)):** al diseñar los endpoints, la ficha del director pedía una biografía que no estaba en el esquema. Se añadió `biography TEXT NULL` a `director` y a `actor`. Es la **única columna opcional** del esquema: en esta fase todas eran `NOT NULL`. El cambio se hizo antes de crear ninguna tabla. Ver [08-flyway-migraciones.md, paso 8.2](08-flyway-migraciones.md#82-diseño-de-los-endpoints-y-revisión-del-esquema).

## Comandos y comprobaciones

Ninguno: es una fase de diseño. El SQL del temario es un borrador que se ejecutará en la fase 8.

## Decisiones de esta fase

En [decisiones-tecnicas.md](decisiones-tecnicas.md): D-10 (fecha de nacimiento obligatoria), D-11 (director y actor separados), D-12 (tipos de datos), D-13 (`ON DELETE`), D-14 (PK compuesta en la tabla intermedia). Revisión posterior: D-37 (biografía, fase 8).

## Pendiente para fases posteriores

- Juego de caracteres `utf8mb4` para las tildes (fase 4).
- Comprobar que las tablas usan el motor InnoDB (fase 8).
