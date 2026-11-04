# agents.md — frontend Angular 22

> **Qué es:** guía de arranque para cualquier asistente de IA (o persona) que trabaje en este frontend. Vale para empezar de cero sin memoria del chat: dice la tecnología, la arquitectura que se sigue y las reglas que hay que respetar.
>
> **Regla de oro:** si algo de este documento contradice el código real, manda el código — y se corrige el documento.

---

## 1. El proyecto

Frontend del portal de películas que habla con el monolito Spring Boot (`backend/`, endpoints REST en `/movies`, suite en la raíz del repo). Arranca con un proxy de desarrollo que reenvía `/movies` al backend en el puerto 8080; en producción se desplegaría detrás del mismo dominio.

| Dato | Valor |
|---|---|
| Framework | **Angular 22**, componentes **standalone** (sin NgModules), API de `@angular/core` moderna |
| Estado | **Signals** (`signal`, `computed`, `effect`, `input`, `output`) — en componentes y en el servicio |
| Control flow | Bloques modernos: `@if`, `@else`, `@for (…; track …), @empty` en las plantillas |
| Formularios | `ReactiveFormsModule` (`FormBuilder`, validaciones espejo del backend) |
| Estilos | Sass con tokens — arquitectura de capas `s01`–`s06` (sección 4) |
| Tests | Karma + Jasmine (`npx ng test`) — evidencia: 5 archivos, ~20 tests |
| Proxy | `proxy.conf.json`: `/movies` → `http://localhost:8080` (el backend debe estar arrancado) |

El JSON del backend se espeja tal cual en `src/app/core/models/movie.ts`: `Movie { id, title, releaseYear, duration, director: Person, actors: Person[] }` con `Person { id, fullName }`. **No se añade ni se quita campo:** lo que tipa `HttpClient` debe ser idéntico a `MovieResponse` del backend.

## 2. Dónde está cada cosa

```
src/app/
  core/
    models/movie.ts        ← espejo exacto del JSON del backend
    services/movie-service.ts  ← ÚNICO dueño del estado: señal movies(), error(),
                               total(), directores() y el CRUD (create/update/remove/refresh)
  features/
    movies/                ← página principal: listado con buscador por título,
                             filtro por director, toolbar "Nueva película", grid de cards
  shared/
    card-movies/           ← tarjeta: póster (con fallback), título, chips, director, botones
    modal-movies/          ← detalle de la película (fondo oscuro + ficha + reparto)
    form-movies/           ← crear/editar, validaciones y mensajes de error del backend
```

- **Estado central, no repartido:** quien necesite las películas las lee de `MovieService`. Las listas derivadas (`total`, `directores`) son `computed` del servicio, no copias por componente.
- Los componentes usan `input()` / `input.required()` / `output()` de beams signal, nunca los decoradores `@Input`/`@Output`.
- Nada de lógica HTTP fuera de `MovieService`; los componentes tratan con señales.

## 3. Reglas del código (las que no se negocian)

1. **Componentes standalone con `imports: [...]`**, archivos separados `.ts` / `.html` / `.scss`.
2. **Signals para todo estado.** Un `computed` para lo derivado; `effect` solo en formularios (rellenar al cambiar el `input`).
3. **Listas derivadas del backend, no mantenidas a mano** (el desplegable de directores se deduplica por id con un `Map` porque la clave única es el id y hay que llevar el nombre junto — un `Set` pierde el nombre o fusionaría homónimos). Al refrescar tras el CRUD, un director nuevo aparece solo en los desplegables.
4. **Desplegable de directores con [value] de texto + conversión única a número** en `enviarFormulario` (`Number(payload.directorId)`). **Nada de `ngValue`** (patrón antiguo): el `<option>` siempre manda `value` como cadena y se convierte en un solo punto del código.
5. **Señal de tres estados para lo "abierto/creando/editando":** `undefined` = cerrado, `null` = crear, objeto = editar. Motivo documentado: una señal **no re-renderiza si se le asigna el mismo valor** — con solo `null`, el botón "Nueva película" no pintaba nunca el formulario (bug real ya provoado y testeado). Del mismo modo, no apoyar `@if (x; as y)` para distinguir cerrado de crear: `null` es falsy.
6. **Estados de hover de un bloque son estados privados** (`--card-editState-*`): un solo sitio donde cambiar fondo, texto y borde a la vez.
7. Comentarios que explican el **por qué** (reglas del backend espejadas, decisiones ya tomadas), en español.

## 4. Estilos: arquitectura de tokens

```
src/styles/
  s01_utilities/   → catálogo ($colores con 9 tonos, $font-sizes, paddings…)
                     + decisiones en _variables_semanticas.scss (siempre !default)
  s03_base/        → _system-tokens.scss genera los --mlt-sys-* REC a partir del catálogo
  (s02, s04, s05, s06 existen, con @forward en su _index.scss)
```

En los archivos `.scss` de componente (`movies`, `card-movies`, `modal-movies`, `form-movies`):

- **Los tokens del componente no llevan nombres descriptivos** («suave», «fuerte», «chip», «aviso», «boton»): llevan **el color real y su tono**. Convención:
  - `--<componente>-color-texto-<gama>-<tono>` para color de texto
  - `--<componente>-color-fondo-<gama>-<tono>` para color de fondo
  - `--<componente>-color-<gama>-<tono>` cuando no hay slot claro (líneas, bordes, hovers)
  - El tono sigue el catálogo: **1 = más oscuro, 9 = más claro** (p. ej. `--card-color-texto-gris-2` es el director, casi negro)
- Un token apunta siempre a un `--mlt-sys-*`; las propiedades del bloque solo leen tokens o estados privados;上网 jamais valores crudos fuera de `s01`.
- BEM sin prefiro: `card__director`, `movies__filter-select`; los `&__elemento` no tienen modificadores propios, heredan el estado del bloque.
- Compartir estilo entre elementos iguales (`&__filter-input, &__filter-select`) en vez de duplicar bloques.
- El `form-movies.scss` restauró su UTF-8 limpio; la trampa PowerShell de tildes con BOM está documentada en `backend/docs/guia-estudio/ESTADO.md` — usar el editor del proyecto, no `Set-Content`.

## 5. Verificación antes de darse por servido

```powershell
cd frontend
npx ng build             # debe acabar sin warnings de presupuesto
npx ng test --watch=false   # verde completo antes de proponer nada como "hecho"
```

Hay presupuesto de estilo subido a `anyComponentStyle 8kB/16kB` a propósito (los archivos de tokens son largos). Los `.AnyCommandError` de PowerShell al lanzar ng test son ruido del shell, no fallos.

## 6. Cosas que ya valen la pena no redescubrir (porque ya se rompieron)

- **El bug de "Nueva película":** señal nacida en `null` + `@if (x; as y)` ⇒ el modal de crear no abría nunca. Lo correcto: tres estados (`undefined`/`null`/objeto) y test de regresión que hace clic de verdad.
- **Las opciones vacías de un `<select>`:** si un `<option>` acaba sin texto (interpolación `undefined`, `ngValue` mal montado), el navegador pinta su `value` = la lista parece de ids. Por eso mandan `[value]` + texto interpolado y la conversión a número en el componente.
- **Los desplegables no se duerple duplican:** fuente única `movieService.directores()`; el filtro del listado y el formulario leyeron la misma señal.
- Los pósteres (`public/posters/{id}.jpg`, de Wikipedia) **no están aprobados por el alumno**: si se quieren quitar, en un commit aparte.
- Sin acceso a red: `ng serve` requiere el backend arriba (Docker + `backend/mvnw spring-boot:run`) o la página cargará con el aviso de conexión.

## 7. Qué NO hacer

- No irse a Angular antiguos: nada de NgModules, `@Input/@Output`, `*ngIf`/`*ngFor`, ni `ngValue`.
- No copiar el estado de `MovieService` en señales locales de los componentes: quien necesita datos, que lea la señal del servicio.
- No inventar endpoints: el backend solo expone `/movies` (GET lista, GET/{id}, POST, PUT/{id}, DELETE/{id}). Sin endpoint de directores: la lista se deriva en cliente.
- No commitear ni pushear sin que el alumno lo pida, y sin `npx ng test` en verde.
- No romper la arquitectura de estilos: nuevo valor de color = nuevo tono en el catálogo de `s01` si no existía, y token por color+tono; nunca un hex en un `.scss` de componente.
