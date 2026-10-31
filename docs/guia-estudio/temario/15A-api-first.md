# Tema 15A — API First: el contrato antes del código

> Fase del plan: 15A. Lo que hicimos: [15A-api-first.md](../15A-api-first.md). Decisión: [D-47](../decisiones-tecnicas.md).

**En una frase:** antes de escribir el controlador, se escribe **qué** promete la API (rutas, datos, errores), y ese documento es el contrato que el código debe cumplir.

---

## 1. OpenAPI y Swagger no son lo mismo

Se usan como sinónimos y no lo son:

| Nombre | Qué es |
|---|---|
| **OpenAPI** | El **formato** (un estándar) para describir una API HTTP en JSON o YAML: rutas, parámetros, cuerpos, respuestas y esquemas. Antes se llamaba *Swagger Specification* |
| **Swagger UI** | Una **herramienta** que lee un documento OpenAPI y lo pinta como una página navegable, con "Try it out" para lanzar peticiones |
| **Swagger** | La empresa que creó la especificación y las herramientas. Hoy "Swagger" suele referirse a su familia de utilidades (UI, Editor, Codegen) |

Un documento OpenAPI **no necesita** Swagger UI: es un archivo de texto. Swagger UI **sí necesita** un documento OpenAPI. Y una API puede tener OpenAPI sin Swagger, o Swagger UI apuntando a un documento escrito a mano.

Versiones que conviven hoy: **OAS 3.0** y **OAS 3.1**. Nosotros escribimos `3.0.3` porque es la que entienden sin sorpresas todas las herramientas; springdoc genera `3.1.0` por defecto. La diferencia principal es que 3.1 es compatible con JSON Schema completo (`nullable` desaparece y se usan tipos con `null`). Para este proyecto no cambia nada.

## 2. API First (contract first) frente a code first

| | API First | Code first |
|---|---|---|
| Qué se escribe primero | El contrato OpenAPI | Los controladores y sus anotaciones |
| Quién decide la forma de la API | El contrato (y quien lo revisa: frontend, profesor, cliente) | El código, y el documento se genera después |
| Riesgo | Que el código se desvíe del contrato si nadie lo comprueba | Que el documento mienta: describe lo que el código hace hoy, no lo acordado |
| Cuándo brilla | Varios equipos, API pública, cliente que empieza antes | Proyecto pequeño y de una sola persona |

En este proyecto el contrato se decidió **en la fase 8**, antes de las tablas: las rutas y las respuestas salieron de las pantallas (D-36). Escribirlo ahora en OpenAPI es poner por escrito algo que ya estaba decidido, y así el frontend puede empezar sin esperar al back.

**El contrato manda.** Si el código y el contrato no coinciden, el error está en el código. Por eso el paso 15A.3 compara los dos.

## 3. Qué hace springdoc

`springdoc-openapi` es una librería que **genera** un documento OpenAPI a partir de una aplicación Spring MVC (o WebFlux):

1. Al arrancar, busca los `@RestController` y sus métodos.
2. Lee las anotaciones (`@GetMapping`, `@PathVariable`, `@RequestBody`...) y los tipos de los DTO.
3. Construye el documento y lo sirve en `/v3/api-docs` (JSON) y `/v3/api-docs.yaml`.
4. Registra **Swagger UI** en `/swagger-ui.html`, que carga ese documento.

Se le puede dar más información con `@Operation`, `@Schema`, `@ApiResponse`... pero **no es obligatorio**: con los tipos y las rutas ya deduce bastante.

En nuestro caso hay una particularidad: **todavía no hay controladores**, así que el documento generado está vacío. Por eso Swagger UI no apunta a `/v3/api-docs`, sino a nuestro YAML:

```yaml
springdoc:
  swagger-ui:
    url: /openapi.yaml
```

Swagger UI pide `/v3/api-docs/swagger-config`, springdoc le responde con `"url": "/openapi.yaml"` y la UI carga ese archivo. Es la misma UI; solo cambia de dónde lee el contrato.

## 4. Anatomía del contrato

```yaml
openapi: 3.0.3            # versión del formato
info:                     # título, versión de la API, descripción
  title: Movies API
  version: 1.0.0
servers:                  # dónde está desplegada
  - url: http://localhost:8080
tags:                     # agrupaciones de la UI
  - name: Películas
paths:                    # las rutas y sus operaciones
  /movies:
    get:
      operationId: listMovies
      responses:
        '200':
          description: Lista de películas; puede estar vacía
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/MovieSummary'
components:               # piezas reutilizables
  schemas:
    MovieSummary: { ... }
```

| Pieza | Para qué |
|---|---|
| `paths` | Cada URL y, dentro, cada método HTTP |
| `parameters` | Lo que va en la ruta, la query o las cabeceras |
| `requestBody` | Lo que se envía (aquí todavía no hay escritura) |
| `responses` | Código HTTP → descripción + esquema del cuerpo |
| `components` | Esquemas, parámetros y respuestas con nombre, para no repetirlos |
| `$ref` | Referencia a una pieza de `components` |

**`operationId`** es el nombre único de la operación. Swagger UI lo usa para identificarla y las herramientas de generación de clientes lo usan para nombrar el método. Vale la pena escribirlo bien (`listMovies`, no `get1`).

**Reutilizar con `$ref`** no es cosmética: si el `ProblemDetail` se define una vez, los cuatro endpoints comparten la misma forma de error y no hay copias que se desincronicen.

## 5. Qué se puede y qué no se puede comprobar con Swagger UI

Swagger UI sirve para:

- Ver de un vistazo **qué endpoints hay** y qué devuelve cada uno.
- Ver los **esquemas** (campos, obligatorios, tipos) sin leer código.
- Lanzar peticiones reales con "Try it out" **si el servidor las implementa**.
- Detectar contradicciones: un `404` documentado que el código nunca devuelve.

No sirve para:

- Sustituir los tests. Que la UI pinte bonito no significa que el endpoint funcione.
- Garantizar que el contrato se cumple. Eso se comprueba con tests (15A.3).

Hoy la UI **documenta** pero no **ejecuta**: "Try it out" devuelve `404` porque no hay controladores. No es un fallo: es el estado normal de un contrato escrito antes que el código.

## 6. El coste

| Coste | Cuándo se nota |
|---|---|
| Mantener el YAML a mano | Cada cambio de contrato hay que escribirlo dos veces: en el YAML y en el código |
| Dependencia extra (`springdoc` + `webmvc`) | Añade Tomcat y arranca un servidor web |
| El documento puede mentir | Si nadie compara contrato y código (15A.3) |

El beneficio compensa cuando la API la consume alguien más (un frontend, otro equipo, el profesor). En un proyecto de una persona y tres endpoints, el beneficio real es **pensar la API antes de programarla** y tener una página que enseñar.

## 7. En este proyecto

| Pieza | Dónde |
|---|---|
| Contrato | `src/main/resources/static/openapi.yaml` |
| Se sirve en | `/openapi.yaml` (Spring Boot sirve `static/` en la raíz) |
| UI | `/swagger-ui.html` → `/swagger-ui/index.html` |
| Versión de springdoc | 3.2.0 (la rama para Spring Boot 4; Boot no la gestiona) |
| Test | `OpenApiContractTests` (3 tests) |
| Decisión | [D-47](../decisiones-tecnicas.md) |

---

## Lo que tienes que recordar

1. **OpenAPI** es el formato; **Swagger UI** es una herramienta que lo pinta. No son lo mismo.
2. **API First** = contrato antes que código; **code first** = documento generado después. El contrato manda.
3. `springdoc` genera el documento a partir de los controladores y sirve Swagger UI.
4. `springdoc.swagger-ui.url` permite que la UI cargue **otro** documento (el nuestro, escrito a mano).
5. `components` + `$ref` evitan repetir esquemas; `operationId` da nombre a cada operación.
6. La UI **no** es un test: que pinte bien no prueba que el endpoint funcione.

## Preguntas de repaso

**1. ¿Cuál es la diferencia entre OpenAPI y Swagger UI?**

<details><summary>Respuesta</summary>

OpenAPI es el **estándar** que describe la API en YAML o JSON. Swagger UI es una **aplicación web** que lee ese documento y lo muestra de forma navegable. Se puede tener OpenAPI sin Swagger UI, pero Swagger UI sin documento no tiene nada que enseñar.
</details>

**2. ¿Por qué el documento generado por springdoc está vacío?**

<details><summary>Respuesta</summary>

Porque springdoc lo construye a partir de los `@RestController`, y todavía no hay ninguno (llegan en la fase 16). Por eso la UI apunta al contrato escrito a mano con `springdoc.swagger-ui.url=/openapi.yaml`.
</details>

**3. Si el contrato y el código no coinciden, ¿cuál se cambia?**

<details><summary>Respuesta</summary>

El código. En API First el contrato es el acuerdo con quien consume la API; cambiarlo rompe a los clientes. La excepción es que el contrato esté mal: entonces se discute y se versiona, no se cambia a escondidas.
</details>

**4. ¿Qué significa `$ref: '#/components/schemas/MovieSummary'`?**

<details><summary>Respuesta</summary>

"Usa el esquema llamado `MovieSummary` definido en `components`". Evita repetir la misma estructura en varios sitios y que las copias se desincronicen.
</details>

**5. ¿Basta con que Swagger UI muestre los endpoints para decir que la API funciona?**

<details><summary>Respuesta</summary>

No. La UI solo lee el documento; no ejecuta nada. Para saber si funciona hacen falta controladores y tests que comprueben las respuestas reales.
</details>

**6. ¿Por qué "Try it out" devuelve 404 hoy?**

<details><summary>Respuesta</summary>

Porque la petición llega al servidor, pero no hay ningún controlador que atienda `/movies`: Spring responde `404 Not Found`. El contrato describe lo que habrá, no lo que hay.
</details>
