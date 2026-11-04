# Preguntas para el profesor: monolito modular (fase 4 / fase 34)

> Preparado el 2026-11-04. Rellenar la columna «Respuesta» en clase. Las respuestas decidirán cómo se hace la fase 4 (ver [ESTADO.md](ESTADO.md), §1) y se anotarán en `decisiones-tecnicas.md` como **D-49**.

## Qué conviene tener claro antes

- **Qué es un módulo:** una parte del negocio (aquí `catalog`: películas, directores y actores) que es dueña de su código y de sus tablas, y que solo se deja usar a través de una «puerta» pública.
- **Qué cambia respecto a hoy:** las capas (`controller`, `domain`, `persistence`) pasan a ir **dentro** de cada módulo.
- **Qué se verifica:** que un módulo no use las clases internas de otro y que no haya dependencias circulares, con **ArchUnit** y **Spring Modulith**.
- **Con un solo módulo apenas se nota nada:** el valor aparece cuando hay un segundo (`stock`, `order`, `user`…). Riesgo anotado en D-45: si `Director` y `Actor` se separan en otro módulo, hay que relacionarlos por id.

## Preguntas

| # | Tema | Pregunta para el profesor | Mi propuesta | Respuesta |
|---|---|---|---|---|
| 1 | Estructura | ¿`com.daw.movies.catalog.{controller,domain,persistence}` o los módulos como paquetes de primer nivel con otra organización dentro? | `catalog.{controller,domain,persistence}` | |
| 2 | Estructura | ¿Dónde van `MoviesApplication` y `config` (Swagger)? | En la raíz, fuera de los módulos | |
| 3 | Estructura | ¿Basta un solo módulo `catalog` o espera ver al menos dos? | Uno ahora; `stock`, `order` o `user` después | |
| 4 | La «puerta» del módulo | ¿Qué puede usar otro módulo: la interfaz del servicio, una fachada o un paquete `api`? Spring Modulith toma como público el paquete raíz del módulo y las capas quedan internas | La interfaz del servicio (`MovieService`) | |
| 5 | La «puerta» del módulo | ¿Los otros módulos reciben objetos del dominio (`Movie`) o DTO propios del módulo? | DTO propios, para no exponer el dominio | |
| 6 | Relaciones | ¿`Director` y `Actor` van en `catalog` o en otro módulo (p. ej. `people`)? | En `catalog`; si se separan, relacionarlos solo por id | |
| 7 | Relaciones | ¿Cómo se comunican los módulos: llamando al servicio del otro o con eventos (fase 26)? | Llamada al servicio ahora; eventos más adelante | |
| 8 | Base de datos | ¿Una sola base de datos con tablas por módulo? ¿Se permiten claves foráneas entre módulos? | Una base de datos; sin claves foráneas entre módulos | |
| 9 | Base de datos | ¿Las migraciones de Flyway se organizan por módulo o siguen juntas en `db/migration`? | Siguen juntas por ahora | |
| 10 | Pruebas | ¿Qué espera ver: pruebas de ArchUnit, `ApplicationModules.verify()` de Spring Modulith o el diagrama que genera? | ArchUnit primero y Modulith después | |
| 11 | Herramientas | ¿Con qué versión de Spring Modulith trabaja, dado que usamos Spring Boot 4.1.1? | Comprobarla en Maven Central antes de añadirla | |
| 12 | Swagger | ¿Los endpoints se agrupan por módulo o sale un único Swagger UI? | Un único Swagger UI, con `OpenApiConfig` en la raíz | |
| 13 | Transacciones | ¿Una transacción puede cruzar módulos o cada módulo tiene las suyas? | Cada módulo con las suyas | |

Las más importantes son la **4**, la **6** y la **1**, porque condicionan todo lo demás.

## Orden de trabajo previsto (ya en el plan)

1. **4a** `test: add ArchUnit layer rules` — antes de mover nada.
2. **4b** `refactor: move code into catalog module` — solo cambian los `package` y los `import`.
3. **4c** `feat: add Spring Modulith verification`.
4. **4d** `docs: D-49 modular monolith` y ajustar el skill a `<paquete.raíz>.<módulo>`.
