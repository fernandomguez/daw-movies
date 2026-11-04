# Tema 08 (paso 8.2) — Diseñar los endpoints antes de crear las tablas

> Fase del plan: 8, paso 8.2. Lo que hicimos: [08-flyway-migraciones.md, paso 8.2](../08-flyway-migraciones.md#82-diseño-de-los-endpoints-y-revisión-del-esquema).

**En una frase:** antes de construir una aplicación, se piensa **qué le van a pedir** (los endpoints). Así se descubre qué datos hay que guardar.

**Para qué sirve:** para no crear tablas a las que luego les falta algo. En este proyecto, al pensar los endpoints apareció una columna que faltaba: la biografía del director.

---

## 1. Qué es un endpoint

Una API es como la **ventanilla de un organismo**. No puedes entrar en la oficina a coger los papeles tú mismo: pides en la ventanilla y te dan lo que pides.

Un **endpoint** es cada una de las cosas que se pueden pedir en esa ventanilla. Está formado por **dos partes juntas**:

| Parte | Qué dice | Ejemplo |
|---|---|---|
| **Método HTTP** | Qué quieres hacer | `GET` (leer) |
| **URL** | Sobre qué | `/movies/3` |

`GET /movies/3` se lee: "quiero **leer** la película **3**".

Los cuatro métodos más usados:

| Método | Significa | Ejemplo |
|---|---|---|
| `GET` | Leer | Ver las películas |
| `POST` | Crear | Añadir una película |
| `PUT` | Reemplazar o modificar | Cambiar los datos de una película |
| `DELETE` | Borrar | Quitar una película |

## 2. Pantalla no es lo mismo que endpoint

Una **pantalla** la pinta el **frontend** (la web o la app). Para pintarla, el frontend **pide los datos** a la API. Cada petición es un endpoint.

```
Pantalla "ficha del director"
   │
   ├── pide  GET /directors/1         → nombre, fecha de nacimiento, biografía
   └── pide  GET /directors/1/movies  → lista de sus películas
```

Una pantalla puede necesitar **uno o varios** endpoints.

**Cómo se empieza:** no por las URLs, sino por **lo que quiere hacer el usuario**, en frases normales ("quiero ver todas las películas"). Después, cada frase se traduce a un endpoint.

## 3. Cuatro reglas para escribir las URLs (estilo REST)

1. **Sustantivos en plural, sin verbos.** `/movies` sí; `/getMovies` no. El verbo ya lo pone el método.
2. **Un elemento concreto, por su `id` en la ruta:** `/movies/3`.
3. **Una relación, anidando:** las películas del director 1 son `/directors/1/movies`.
4. **Filtros y búsquedas, detrás de `?`:** `/movies?year=2006`.

## 4. Los endpoints de nuestra versión 1

| # | Método | URL | Para qué pantalla |
|---|---|---|---|
| 1 | `GET` | `/movies` | Inicio: todas las películas |
| 2 | `GET` | `/movies/{id}` | Ficha de una película |
| 3 | `GET` | `/directors/{id}` | Ficha de un director |
| 4 | `GET` | `/directors/{id}/movies` | Ficha de un director: su lista de películas |

`{id}` es un hueco: en cada petición se cambia por un número (`/movies/1`, `/movies/2`...).

## 5. Qué devuelve cada uno (la forma del JSON)

### Una lista devuelve poco; una ficha devuelve más

La lista de inicio solo necesita lo que se ve en ella:

```json
[
  { "id": 1, "title": "The Prestige", "releaseYear": 2006 },
  { "id": 2, "title": "The Dark Knight", "releaseYear": 2008 }
]
```

La ficha de una película trae más datos:

```json
{
  "id": 1,
  "title": "The Prestige",
  "releaseYear": 2006,
  "durationMinutes": 130,
  "director": { "id": 1, "fullName": "Christopher Nolan" },
  "actors": [
    { "id": 3, "fullName": "Christian Bale" },
    { "id": 2, "fullName": "Michael Caine" },
    { "id": 8, "fullName": "Hugh Jackman" }
  ]
}
```

Los actores salen **por orden alfabético de apellido** (Bale, Caine, Jackman), como se decidió en D-38.

### El `id` es lo que hace posibles los enlaces

En la pantalla no se ve el `id`, pero sin él **no hay enlaces**. Si el director no trajera su `id`, el frontend no sabría a qué ficha llevar al pulsar "Christopher Nolan".

### La respuesta es un contrato

Los actores, de momento, solo se muestran por su nombre. Aun así, se devuelven **con su `id`**. ¿Por qué?

| Si devuelves... | Y mañana quieres enlaces a los actores... |
|---|---|
| `["Christian Bale", "Hugh Jackman"]` | **Cambia la forma** de la respuesta. Cualquier frontend que la usara deja de funcionar. |
| `[{ "id": 3, "fullName": "Christian Bale" }, ...]` | No cambia nada en la API. El frontend empieza a usar el `id` que ya tenía. |

La forma de una respuesta es un **contrato** con quien usa la API: una vez publicada, no se rompe a la ligera.

### Separado en la ficha, junto dentro de otra respuesta

```json
{
  "id": 1,
  "firstName": "Christopher",
  "lastName": "Nolan",
  "birthDate": "1970-07-30",
  "biography": "Director, guionista y productor británico-estadounidense..."
}
```

- **En su ficha**, el director lleva `firstName` y `lastName` por separado. Es su respuesta completa, y el frontend puede mostrarlo como prefiera ("Nolan, Christopher").
- **Dentro de otra respuesta** (el director en la ficha de una película), basta `fullName`: es solo la etiqueta del enlace.

`fullName` **no es una columna**: se calcula juntando `first_name` y `last_name`. Los datos de la API no tienen por qué coincidir con las columnas (lo veremos con los DTOs en la fase 15).

## 6. Los códigos de estado: cómo dice la API si ha ido bien

Toda respuesta HTTP lleva un **número** que resume qué ha pasado:

| Código | Nombre | Cuándo lo usamos |
|---|---|---|
| `200` | OK | La petición ha ido bien |
| `400` | Bad Request | La petición está mal hecha: `/movies/abc` (el `id` no es un número) |
| `404` | Not Found | Lo que pides no existe: `/movies/999` |

Regla rápida: **2xx** = todo bien; **4xx** = el error es de quien pide; **5xx** = el error es del servidor.

Cuando hay un error, además del código se envía un cuerpo que lo explica. Spring trae un formato estándar llamado ***Problem Details***:

```json
{
  "status": 404,
  "title": "Not Found",
  "detail": "Movie with id 999 not found"
}
```

Aquí solo decidimos **qué** se responde. **Cómo** se programa (un *exception handler* con `@RestControllerAdvice`) se ve en la fase 18.

## 7. Una lista vacía no es un error

Esta fue la gran duda del paso: *"Si un director no tiene películas, ¿no se puede lanzar una excepción que diga 'este director no tiene películas'?"*

**No**, porque no ha fallado nada.

Piensa en el buscador de Netflix. Escribes "asdfgh":
- ❌ No ves una pantalla de "ERROR, algo ha fallado".
- ✅ Ves la página normal con el texto "No hay resultados".

Por dentro, el servidor ha contestado "todo bien, aquí tienes los resultados: **ninguno**". Es una lista vacía, y es una respuesta **correcta**. Es como preguntar "¿cuántas películas tiene?" y que te digan "0": el 0 es una respuesta, no un fallo.

| Petición | Situación | Respuesta |
|---|---|---|
| `GET /directors/999/movies` | El director **no existe** | `404`: no hay director del que dar una lista |
| `GET /directors/6/movies` | El director **existe**, pero tiene cero películas | `200` con `[]` |

Si los dos casos devolvieran `404`, el frontend **no podría distinguirlos**: no sabría si enseñar una página de error o la ficha normal con un aviso.

**¿Y el mensaje "Este director no tiene películas en su catálogo"?** Lo escribe el **frontend** al ver la lista vacía:

```
Back:   200 OK  →  []
Front:  "si la lista está vacía, muestro: Este director no tiene películas en su catálogo"
```

> **El back devuelve datos y códigos; el front decide cómo contárselo al usuario.** Así cada frontend (web, móvil) puede mostrar el aviso a su manera y en su idioma.

## 8. Comprobar los endpoints contra el esquema

Con la lista hecha, se coge **cada dato de cada respuesta** y se busca **de qué tabla y columna sale**. Tres resultados posibles:

| Resultado | Ejemplo en este proyecto |
|---|---|
| Sale directamente de una columna | `title` → `movie.title` |
| Se calcula a partir de varias | `fullName` → `first_name` + `last_name` |
| **No sale de ningún sitio** | `biography` → **faltaba en `director`** |

El tercer caso es el que importa: obliga a cambiar el diseño. Como **todavía no se había creado ninguna tabla**, el cambio fue editar el documento de la fase 3. Con las tablas ya creadas, habría hecho falta una migración nueva.

Esto también aparece al comprobar: **la base de datos no garantiza ningún orden** si no se pide. Por eso hubo que decidir el orden de cada lista (alfabético, por año...), aunque eso no cambie ninguna tabla.

## 9. Por qué los endpoints van antes que las tablas

```
Lo que se puede pedir (endpoints)  →  qué datos hacen falta  →  qué tablas y columnas
```

Si se diseña al revés (primero las tablas), es fácil que falte algo, o que sobre algo que nadie va a pedir nunca. Los endpoints son la **lista de requisitos** de la base de datos.

---

## Lo que tienes que recordar

- Un **endpoint** = método HTTP + URL. `GET /movies/3` = leer la película 3.
- Se diseña **desde las pantallas**: qué quiere ver el usuario → qué datos pide el frontend → qué endpoint los da.
- URLs con **sustantivos en plural**, `id` en la ruta, relaciones anidadas y filtros detrás de `?`.
- Devuelve siempre el **`id`** de lo que pueda ser un enlace. La respuesta es un **contrato**.
- `200` bien, `400` petición mal hecha, `404` no existe.
- **Lista vacía = `200 []`**, no un error. El mensaje para el usuario lo pone el frontend.
- Comprueba cada dato contra el esquema **antes de crear las tablas**.

## Preguntas de repaso

### La idea

**1. ¿Qué es un endpoint?**
Una operación que ofrece la API, formada por un método HTTP y una URL. Por ejemplo, `GET /movies`.

**2. ¿Por qué `/getMovies` es una mala URL?**
Porque lleva un verbo. El verbo ya lo dice el método (`GET`); la URL solo dice **sobre qué** se actúa: `/movies`.

**3. ¿Cómo escribirías "las películas del director 4"?**
`GET /directors/4/movies`.

**4. ¿Es lo mismo una pantalla que un endpoint?**
No. La pantalla la pinta el frontend; para hacerlo, pide datos a uno o varios endpoints. La ficha del director usa dos: `/directors/{id}` y `/directors/{id}/movies`.

### Las respuestas

**5. En la pantalla de inicio no se ve el `id` de las películas. ¿Por qué lo devuelve `GET /movies`?**
Porque, sin él, el frontend no sabría a qué ficha llevar al pulsar una película.

**6. Los actores solo se muestran por su nombre. ¿Por qué se devuelve también su `id`?**
Porque la respuesta es un contrato. Si mañana se quieren enlaces a los actores, el `id` ya está y la forma de la respuesta no cambia. Si solo se devolvieran los nombres, habría que cambiarla y se romperían los frontends que la usan.

**7. ¿`fullName` es una columna de la tabla `director`?**
No. Se calcula juntando `first_name` y `last_name`.

### Los códigos de estado

**8. ¿Qué devuelve `GET /movies/999` si esa película no existe?**
`404 Not Found`, con un cuerpo *Problem Details* que lo explica.

**9. ¿Y `GET /movies/abc`?**
`400 Bad Request`: la petición está mal hecha, porque el `id` debe ser un número.

**10. El director 6 existe, pero no tiene películas. ¿Qué devuelve `GET /directors/6/movies`?**
`200 OK` con una lista vacía `[]`. No ha fallado nada: la respuesta correcta es "ninguna".

**11. ¿Quién escribe el mensaje "Este director no tiene películas en su catálogo"?**
El frontend, al ver que la lista está vacía. La API solo devuelve los datos.

**12. ¿Por qué no devolver `404` también cuando el director no tiene películas?**
Porque entonces el frontend no podría distinguir "el director no existe" (página de error) de "existe, pero no tiene películas" (ficha normal con un aviso).

### Endpoints y esquema

**13. ¿Qué se descubrió al comprobar los endpoints contra el esquema?**
Que faltaba la columna de la biografía del director. Se añadió `biography TEXT NULL` antes de crear las tablas.

**14. ¿Por qué hubo que decidir el orden de las listas, si eso no cambia ninguna tabla?**
Porque la base de datos no garantiza ningún orden si no se le pide. Sin decidirlo, la misma petición podría devolver las películas en un orden distinto cada vez.

**15. ¿Por qué es mejor pensar los endpoints antes de crear las tablas?**
Porque los endpoints dicen qué datos hacen falta. Cambiar el diseño antes de crear las tablas cuesta editar un documento; después cuesta una migración nueva.
