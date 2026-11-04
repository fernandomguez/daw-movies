# 02 — Entidades, atributos y relaciones

> **Fase del plan:** 2 (Alcance del dominio y datos de prueba).
> En esta fase **no hay SQL ni Java**: describimos el problema con palabras. Es el "modelo conceptual".

## Objetivo

Aprender a mirar un problema real (el cine) y descubrir:

1. Qué **cosas** necesitamos guardar → **entidades**.
2. Qué **sabemos** de cada una → **atributos**.
3. Cómo se **conectan** entre sí → **relaciones** y su **cardinalidad**.

## 1. Entidades

Una **entidad** es una "cosa" del problema que:

- tiene **identidad propia**: podemos distinguir una de otra aunque se parezcan;
- queremos **guardar varias** de ellas;
- tiene **datos propios** que nos interesan.

### Cómo las descubrimos

Un truco sencillo: escribir frases sobre el problema y fijarse en los **sustantivos**.

> "Una **película** está dirigida por un **director** y en ella participan varios **actores**. Cada película tiene un **título**, un **año** y una **duración**."

| Sustantivo | ¿Entidad? | Motivo |
|---|---|---|
| Película | ✅ Sí | Tiene identidad (*Inception* no es *Interstellar*), hay muchas y tiene datos propios. |
| Director | ✅ Sí | Nolan es distinto de Tarantino; tiene nombre y fecha de nacimiento. |
| Actor | ✅ Sí | Igual que director. |
| Título | ❌ No | Es un **dato de** una película, no una cosa independiente → atributo. |
| Año, duración | ❌ No | Atributos de la película. |

### Un caso dudoso: la "participación"

"DiCaprio participa en *Inception*" también es algo que hay que guardar. Pero, en nuestro alcance, **no tiene datos propios**: solo une una película con un actor. Por eso, de momento, es una **relación**, no una entidad.

Si algún día quisiéramos guardar **el nombre del personaje** ("DiCaprio interpreta a *Dom Cobb* en *Inception*"), ese dato no sería ni de la película ni del actor, sino **de la participación**. En ese momento la participación tendría datos propios y pasaría a tratarse como una entidad (fase opcional 23).

## 2. Atributos

Un **atributo** es una característica de una entidad. Para cada uno nos preguntamos:

- **¿Qué tipo de dato es?** (texto, número entero, fecha...)
- **¿Es obligatorio?** (¿puede existir la entidad sin ese dato?)
- **¿Lo guardamos o lo calculamos?**

### Película

| Atributo | Tipo conceptual | ¿Obligatorio? | Ejemplo |
|---|---|---|---|
| Título | Texto | Sí | *Inception* |
| Año de estreno | Número entero | Sí | 2010 |
| Duración (minutos) | Número entero positivo | Sí | 148 |

### Director y actor

| Atributo | Tipo conceptual | ¿Obligatorio? | Ejemplo |
|---|---|---|---|
| Nombre | Texto | Sí | Christopher |
| Apellidos | Texto | Sí | Nolan |
| Fecha de nacimiento | Fecha | Por decidir (ver decisiones) | 1970-07-30 |

### Decisiones razonadas sobre los atributos

**¿Año o fecha completa de estreno?** → **Año.**
Una película se estrena en fechas distintas en cada país (estreno mundial, en España, en festivales...). Con una fecha completa tendríamos que decidir cuál guardar. El año es estable, suficiente para aprender y evita esa ambigüedad.

**¿Duración en minutos o en "horas y minutos"?** → **Minutos (un número entero).**
Un único número es fácil de guardar, comparar y ordenar ("películas de más de 150 minutos"). El formato "2 h 28 min" es una forma de **mostrar** el dato y se puede **calcular** a partir de los minutos.

**¿Nombre completo en un solo campo o separado?** → **Separado (nombre y apellidos).**
Permite ordenar por apellido y buscar por cualquiera de las dos partes. Tiene casos límite que conviene conocer:
- *Guillermo del Toro*: nombre "Guillermo", apellidos "del Toro".
- *Samuel L. Jackson*: nombre "Samuel L.", apellidos "Jackson".
- *Alejandro Amenábar*, *Maribel Verdú*: llevan tildes → la base de datos deberá admitir caracteres especiales (lo veremos en las fases 3 y 4).

**¿Guardamos la edad?** → **No, se calcula.**
La edad cambia cada año; si la guardáramos, quedaría desactualizada. Guardamos la **fecha de nacimiento** y la edad se **calcula** cuando se necesita. Lo mismo ocurre con el "nombre completo": se obtiene juntando nombre y apellidos. Estos son **atributos calculados** (o derivados) y volverán a aparecer cuando hagamos mapeos (fases 13 y 15).

### ¿Qué identifica a una película?

Podríamos pensar que el título identifica a una película, pero **no es único**: hay remakes con el mismo título (por ejemplo, existen varias películas tituladas *Ocean's Eleven* o *Scarface*). Tampoco "título + año" es totalmente seguro. Por eso, cada entidad tendrá un **identificador** propio (un número) que no depende de sus datos. Cómo se elige y se genera ese identificador lo veremos en la fase 3 (claves primarias).

## 3. Relaciones y cardinalidad

Una **relación** describe cómo se conectan dos entidades. La **cardinalidad** indica **cuántos** elementos de un lado pueden conectarse con cuántos del otro.

El truco es **leer la relación en los dos sentidos**:

### Director — Película: uno a muchos (1:N)

- Un director dirige **varias** películas (Nolan: 4 en nuestros datos).
- Una película tiene **un** director principal.

→ **Uno a muchos (1:N).**

### Película — Actor: muchos a muchos (N:M)

- Una película tiene **varios** actores (*Interstellar*: McConaughey, Hathaway, Caine, Damon).
- Un actor participa en **varias** películas (DiCaprio: 4 en nuestros datos).

→ **Muchos a muchos (N:M).** Ya adelantamos que, en una base de datos relacional, esta relación necesitará una **tabla intermedia** (fase 3).

### Obligatoriedad (mínimos)

Además del máximo ("uno" o "muchos"), importa el **mínimo**: ¿puede existir un elemento **sin** la relación?

| Pregunta | Respuesta | Motivo |
|---|---|---|
| ¿Puede existir una película sin director? | **No** | Es un requisito del proyecto: toda película tiene director principal. |
| ¿Puede existir un director sin películas? | **Sí** | Podemos dar de alta a un director antes de añadir sus películas. |
| ¿Puede existir una película sin actores? | **Sí** (en la base de datos) | La película se guarda primero y los actores se le asignan después. En nuestros datos todas tendrán actores. |
| ¿Puede existir un actor sin películas? | **Sí** | Igual que el director. |

Notación habitual (la usaremos en el diagrama de la fase 3):

```
DIRECTOR  1 ────────< N  PELÍCULA  N >────────< M  ACTOR
(exactamente uno)  (cero o más)      (cero o más)   (cero o más)
```

## 4. Una duplicación detectada (para la fase 3)

Director y actor tienen **exactamente los mismos atributos**. Además, una misma persona puede ser ambas cosas: Quentin Tarantino dirige *Pulp Fiction* y también actúa en ella.

Opciones que estudiaremos en la fase 3:

- **Dos entidades separadas** (`director` y `actor`): más sencillo de entender; si alguien es ambas cosas, aparece dos veces.
- **Una entidad "persona"** con sus papeles: evita la duplicación, pero complica el modelo.

De momento, mantenemos **director y actor separados**, que es lo aprobado en el alcance. La decisión definitiva se tomará en la fase 3.

## Resumen

- **Entidad:** cosa con identidad propia, de la que guardamos varias y que tiene datos → película, director, actor.
- **Atributo:** característica de una entidad; decidimos tipo, obligatoriedad y si se guarda o se calcula.
- **Atributo calculado:** no se guarda porque se obtiene a partir de otros (edad, nombre completo).
- **Cardinalidad:** se descubre leyendo la relación en los dos sentidos → director–película 1:N, película–actor N:M.
- Una relación sin datos propios no es una entidad; si llega a tenerlos (nombre del personaje), pasa a serlo.

## Preguntas de repaso

**¿Por qué "duración" no es una entidad?**
Porque no tiene identidad propia ni datos propios: es una característica de la película.

**¿Por qué guardamos la fecha de nacimiento y no la edad?**
Porque la edad cambia con el tiempo y quedaría desactualizada; se calcula a partir de la fecha de nacimiento.

**¿Por qué no usamos el título como identificador de la película?**
Porque puede repetirse (remakes) y, además, un título puede corregirse; necesitamos un identificador que no cambie ni se repita.

**¿Cómo se descubre que película–actor es N:M?**
Leyendo la relación en los dos sentidos: una película tiene varios actores y un actor participa en varias películas.

**¿En qué caso la participación de un actor en una película se convertiría en entidad?**
Cuando tenga datos propios, como el nombre del personaje que interpreta.
