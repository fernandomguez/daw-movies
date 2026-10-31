# Recorrido guiado por la historia del proyecto

> **Para qué sirve:** poder empezar en el primer commit y avanzar parada a parada viendo cómo se construyó el proyecto: qué se decidió, qué archivos aparecieron y qué código funcionaba en cada momento.
>
> **Requisito:** este documento se escribió al rehacer la historia el **2026-10-31**. Cada parada es un commit real y compilable, no una reconstrucción inventada del código.

## Cómo viajar

```powershell
# Ir a una parada
git switch --detach fase-06

# Saber dónde estás y qué acabas de ver
git log --oneline --decorate -5
git show --stat

# Ver qué cambió respecto a la parada anterior
git diff fase-05..fase-06

# Volver al presente
git switch main
```

- En una parada estás en **detached HEAD**: puedes mirar, compilar y ejecutar, pero **no commitear** ahí. Si quieres experimentar, crea una rama: `git switch -c probando-fase-06`.
- `git switch -` vuelve a donde estabas antes de viajar.
- Las etiquetas son **anotadas**: `git show fase-04` muestra el mensaje de cierre de esa fase.

### Viajar sin perder el presente (worktrees)

```powershell
git worktree add ..\parada-04 fase-04   # abre la parada 4 en otra carpeta
git worktree remove ..\parada-04        # cuando termines
```

Así puedes tener el proyecto actual abierto en un editor y una parada antigua en otro, a la vez.

## Las paradas

| # | Etiqueta | Qué aparece | ¿MariaDB para tests/arranque? |
|---|---|---|---|
| 1 | `fase-01` | Normas, inspección del repositorio, plan de aprendizaje, glosario y registro de decisiones | No |
| 2 | `fase-02` | Alcance del dominio: entidades, atributos, relaciones y datos de prueba | No |
| 3 | `fase-03` | Diseño del esquema relacional: tablas, PK, FK y tabla intermedia | No |
| 4 | `fase-04` | `compose.yaml`, MariaDB en contenedor, volumen y healthcheck | Sí, para probarlo |
| 5 | `fase-05` | Arquitectura en tres capas, DIP y estructura de paquetes | No |
| 6 | `fase-06` | Proyecto Maven real: wrapper, `pom.xml`, `MoviesApplication` y primer test | No |
| 7 | `fase-07` | Driver JDBC, `DataSource`, `application.yaml` y test de conexión | **Sí** |
| 8 | `fase-08` | Flyway y las migraciones `V1`–`V4` | **Sí** |
| 9 | `fase-09` | `seed.sql` con los datos de prueba y consultas `JOIN` verificadas | **Sí** |
| 10 | — | Documentación de la fase 10: modelo rico y diseño de los *value objects* | Solo para los tests de integración |
| 11 | — | Los seis *value objects* con sus tests (35 tests) | Solo para los tests de integración |
| 12 | `fase-10` | `Director`, `Actor` y `Movie` con TDD (55 tests en total) | Solo para los tests de integración |
| 13 | `fase-15A` | Contrato `openapi.yaml`, springdoc y Swagger UI | Sí, para arrancar la app y para los tests |
| 14 | `main` | Índice general, este recorrido y las notas de la reescritura | Igual que la 13 |

> **Interludio en la parada 13:** el 15A se adelantó el 2026-10-28. En el plan de estudio aparece entre las fases 15 y 16; en la historia está aquí porque se hizo antes. Este commit **no se moverá** cuando lleguemos a la fase 15: lo que faltará es el paso 15A.3, que se añadirá más adelante con su propio commit.

### Qué mirar en cada parada

| Parada | Archivos que merecen la pena |
|---|---|
| 1 | `base-standards.md`, `docs/guia-estudio/00-plan-de-aprendizaje.md`, `decisiones-tecnicas.md` |
| 2 | `docs/guia-estudio/02-alcance-del-dominio.md` (los datos de prueba que se usan hoy) |
| 3 | `docs/guia-estudio/03-diseno-del-esquema.md` (el diagrama entidad-relación) |
| 4 | `compose.yaml`, `docs/guia-estudio/04-docker-y-mariadb.md` (errores reales de puertos y comillas) |
| 5 | `docs/guia-estudio/temario/05-arquitectura-tres-capas-dip.md` (por qué el dominio manda) |
| 6 | `pom.xml`, `src/main/java/.../MoviesApplication.java`, primer test |
| 7 | `application.yaml`, `DatabaseConnectionTests` |
| 8 | `src/main/resources/db/migration/V1`–`V4` |
| 9 | `src/main/resources/db/testdata/seed.sql` |
| 10 | `docs/guia-estudio/temario/10-modelo-de-dominio.md` (modelo rico y D-29) |
| 11 | `domain/model/Title.java` y los otros cinco *value objects* |
| 12 | `domain/model/Movie.java` (relaciones y constructor `protected`) |
| 13 | `static/openapi.yaml`, `OpenApiContractTests` |
| 14 | `INDICE.md` y este archivo |

## Qué necesita cada parada para funcionar

- **Compilar siempre funciona** desde la parada 6: `.\mvnw.cmd -q -DskipTests compile`.
- **Los tests unitarios** (dominio y *value objects*) no necesitan nada: `.\mvnw.cmd -Dtest=MovieTest test`.
- **Los tests de integración** (a partir de la 7) necesitan MariaDB:

```powershell
docker compose up -d
docker compose ps          # esperar a "healthy"
```

- **La parada 9 necesita los datos**: si la base se recreó, hay que cargar `seed.sql` con la receta del [diario 09](09-datos-de-prueba.md).
- **La parada 13 arranca la aplicación** (`.\mvnw.cmd spring-boot:run`), pero necesita MariaDB porque Flyway se ejecuta al inicio.

## Aviso honesto sobre las paradas 1–5

El código es real, pero la documentación de esas fases se escribió y revisó **con la mirada puesta en las fases 6–10**. Por eso, en las primeras paradas verás decisiones (por ejemplo D-29, JPA en el dominio) que todavía no se habían tomado en ese momento del relato. Es una reconstrucción del orden, no un diario escrito a mano día a día.

A partir de la parada 6 el orden sí es el original: cada commit se hizo al cerrar su fase.

## Cómo se mantiene

- Cada fase nueva se cierra con **un commit que compila** y una **etiqueta anotada** `fase-NN`.
- Si una fase se parte en varios commits (como la 10), la etiqueta va en el último.
- Al añadir una parada, se actualiza la tabla de arriba y el [índice general](INDICE.md).
