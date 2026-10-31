# 02 — Alcance del dominio y datos de prueba

> **Conceptos de esta fase:** [temario/02-entidades-atributos-y-relaciones.md](temario/02-entidades-atributos-y-relaciones.md).

## Objetivo

Dejar cerrado, **con palabras y sin código**, qué vamos a guardar y con qué datos de prueba lo comprobaremos.

## Qué hicimos

| Paso | Resultado |
|---|---|
| 2.1 Identificar entidades | Película, director y actor. La participación película–actor es una relación (sin datos propios). |
| 2.2 Identificar atributos | Película: título, año de estreno, duración en minutos. Director y actor: nombre, apellidos, fecha de nacimiento. |
| 2.3 Identificar relaciones | Director–película 1:N (película con director obligatorio). Película–actor N:M. |
| 2.4 Elegir los datos de prueba | 10 películas, 5 directores, 14 actores y 24 participaciones (abajo). |

## Datos de prueba

Datos reales y públicamente conocidos. Usamos el **título original** de cada película. Las duraciones son las de la versión de cines más citada; algunas fuentes pueden diferir en un minuto.

### Directores (5)

| Nombre | Apellidos | Fecha de nacimiento |
|---|---|---|
| Christopher | Nolan | 1970-07-30 |
| Quentin | Tarantino | 1963-03-27 |
| Martin | Scorsese | 1942-11-17 |
| Guillermo | del Toro | 1964-10-09 |
| Alejandro | Amenábar | 1972-03-31 |

### Películas (10)

| Título | Año | Duración (min) | Director |
|---|---|---|---|
| The Prestige | 2006 | 130 | Christopher Nolan |
| The Dark Knight | 2008 | 152 | Christopher Nolan |
| Inception | 2010 | 148 | Christopher Nolan |
| Interstellar | 2014 | 169 | Christopher Nolan |
| Pulp Fiction | 1994 | 154 | Quentin Tarantino |
| Once Upon a Time in Hollywood | 2019 | 161 | Quentin Tarantino |
| The Departed | 2006 | 151 | Martin Scorsese |
| The Wolf of Wall Street | 2013 | 180 | Martin Scorsese |
| El laberinto del fauno | 2006 | 118 | Guillermo del Toro |
| Mar adentro | 2004 | 125 | Alejandro Amenábar |

Variedad intencionada: un director con 4 películas, dos con 2 y dos con 1.

### Actores (14)

| Nombre | Apellidos | Fecha de nacimiento | Nº de películas |
|---|---|---|---|
| Leonardo | DiCaprio | 1974-11-11 | 4 |
| Michael | Caine | 1933-03-14 | 4 |
| Christian | Bale | 1974-01-30 | 2 |
| Matt | Damon | 1970-10-08 | 2 |
| Matthew | McConaughey | 1969-11-04 | 2 |
| Margot | Robbie | 1990-07-02 | 2 |
| Anne | Hathaway | 1982-11-12 | 1 |
| Hugh | Jackman | 1968-10-12 | 1 |
| Samuel L. | Jackson | 1948-12-21 | 1 |
| Uma | Thurman | 1970-04-29 | 1 |
| Brad | Pitt | 1963-12-18 | 1 |
| Maribel | Verdú | 1970-10-02 | 1 |
| Ivana | Baquero | 1994-06-11 | 1 |
| Javier | Bardem | 1969-03-01 | 1 |

### Participaciones película–actor (24)

| Película | Actores |
|---|---|
| The Prestige | Christian Bale, Hugh Jackman, Michael Caine |
| The Dark Knight | Christian Bale, Michael Caine |
| Inception | Leonardo DiCaprio, Michael Caine |
| Interstellar | Matthew McConaughey, Anne Hathaway, Michael Caine, Matt Damon |
| Pulp Fiction | Samuel L. Jackson, Uma Thurman |
| Once Upon a Time in Hollywood | Leonardo DiCaprio, Brad Pitt, Margot Robbie |
| The Departed | Leonardo DiCaprio, Matt Damon |
| The Wolf of Wall Street | Leonardo DiCaprio, Margot Robbie, Matthew McConaughey |
| El laberinto del fauno | Maribel Verdú, Ivana Baquero |
| Mar adentro | Javier Bardem |

No incluimos todo el reparto de cada película: solo lo necesario para comprobar las relaciones. Los actores compartidos (DiCaprio, Caine, Bale, Damon, McConaughey, Robbie) permiten comprobar que la relación N:M funciona en los dos sentidos, también entre directores distintos.

### Casos que se quedan fuera (a propósito)

- **The Matrix**: la dirigieron dos personas (Lana y Lilly Wachowski). Con "un director principal" tendríamos que falsear el dato.
- **Tarantino como actor en Pulp Fiction**: lo usaremos en la fase 3 para razonar si director y actor deberían ser una sola entidad.
- **Un director sin películas**: no está en los datos iniciales. Lo añadiremos en la fase 9 para practicar `LEFT JOIN`.

## Decisiones de esta fase

Ver [decisiones-tecnicas.md](decisiones-tecnicas.md): D-09 (atributos) y D-10 (obligatoriedad de la fecha de nacimiento, resuelta en la fase 3).

## Resumen

El modelo conceptual queda así:

```
DIRECTOR (nombre, apellidos, fecha de nacimiento)
   │ 1
   │ dirige
   │ N
PELÍCULA (título, año de estreno, duración en minutos)
   │ N
   │ participa
   │ M
ACTOR (nombre, apellidos, fecha de nacimiento)
```
