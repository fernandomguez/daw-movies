# Carteles de películas

Imágenes extraídas de los artículos de Wikipedia de cada película vía su API
(`page/summary`), descargadas el 04/11/2026. Nombres: `{id}.jpg`, donde **id** es
el id de la película en la base de datos.

⚠️ Los carteles tienen copyright de sus respectivos estudios/productoras.
Wikipedia los acoge bajo *fair use*; para un proyecto educativo (DAW) se usan
aquí con esa misma finalidad y citando la fuente. Si el proyecto se publicara,
habría que sustituirlos o tener el permiso correspondiente.

| id | Película | Artículo de origen |
|----|----------|--------------------|
| 1 | The Prestige (2006) | en.wikipedia.org (poster) |
| 2 | The Dark Knight (2008) | en.wikipedia.org (poster) |
| 3 | Inception (2010) | en.wikipedia.org (poster) |
| 4 | Interstellar (2014) | en.wikipedia.org (poster) |
| 5 | Pulp Fiction (1994) | en.wikipedia.org (poster) |
| 6 | Once Upon a Time in Hollywood (2019) | en.wikipedia.org (poster) |
| 7 | The Departed (2006) | en.wikipedia.org (poster) |
| 8 | The Wolf of Wall Street (2013) | en.wikipedia.org (poster) |
| 9 | El laberinto del fauno (2006) | en.wikipedia.org (Pan's Labyrinth, poster) |
| 10 | Mar adentro (2004) | en.wikipedia.org (The Sea Inside, poster) |

El componente `card-movies` muestra la imagen; si el fichero no existe para una
película nueva, cae a un bloque degradado con el título (fallback en
`card-movies.ts` con `posterOk`).
