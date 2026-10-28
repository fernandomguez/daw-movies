-- Fase 9: datos de prueba. NO es una migración de Flyway.
-- Vive en db/testdata, fuera de db/migration, para que Flyway no lo ejecute nunca (D-42).
-- Se carga a mano con los dos comandos de docs/guia-estudio/09-datos-de-prueba.md.
--
-- Orden obligatorio por las claves foráneas: director -> movie -> actor -> movie_actor.
-- Las tablas están vacías, así que los ids son conocidos y los usamos para leer mejor
-- quién es quién. (En una base con datos habría que dejar que AUTO_INCREMENT los asigne.)
--
-- Datos: docs/guia-estudio/02-alcance-del-dominio.md (5 directores, 10 películas, 14 actores,
-- 24 participaciones) + Sofia Coppola (id 6), que no dirige ninguna película para practicar
-- LEFT JOIN en el paso 9.5 (D-43).
--
-- Para recargar desde cero (vaciar en el orden inverso a las claves foráneas):
--   DELETE FROM movie_actor; DELETE FROM movie; DELETE FROM actor; DELETE FROM director;
--   ALTER TABLE director AUTO_INCREMENT = 1; ALTER TABLE movie AUTO_INCREMENT = 1;
--   ALTER TABLE actor AUTO_INCREMENT = 1;
-- Si se ejecuta dos veces sin vaciar, la primera fila ya existe y MariaDB responde
-- ERROR 1062 (Duplicate entry): es el error que se lee en el paso 9.3.

-- 1) Directores (5 con películas + 1 sin películas).
INSERT INTO director (id, first_name, last_name, birth_date, biography) VALUES
    (1, 'Christopher', 'Nolan', '1970-07-30',
     'Director, guionista y productor británico-estadounidense nacido en Londres. Es conocido por sus narraciones complejas y no lineales, como Memento, y por la trilogía de El caballero oscuro. Ganó el Óscar a la mejor dirección por Oppenheimer.'),
    (2, 'Quentin', 'Tarantino', '1963-03-27',
     'Director y guionista estadounidense nacido en Knoxville (Tennessee). Trabajó en un videoclub antes de debutar con Reservoir Dogs (1992). Pulp Fiction ganó la Palma de Oro en Cannes y el Óscar al mejor guion original.'),
    (3, 'Martin', 'Scorsese', '1942-11-17',
     'Director estadounidense nacido en Nueva York, ciudad que aparece en buena parte de su obra. Dirigió Taxi Driver, Toro salvaje y Uno de los nuestros. Ganó el Óscar a la mejor dirección por Infiltrados.'),
    (4, 'Guillermo', 'del Toro', '1964-10-09',
     'Director, guionista y productor mexicano nacido en Guadalajara. Su cine mezcla la fantasía, el terror y las criaturas. El laberinto del fauno ganó tres Óscar, y La forma del agua, los de mejor película y mejor dirección.'),
    (5, 'Alejandro', 'Amenábar', '1972-03-31',
     'Director, guionista y compositor nacido en Santiago de Chile y criado en Madrid. Debutó con Tesis (1996) y dirigió Abre los ojos y Los otros. Mar adentro ganó el Óscar a la mejor película de habla no inglesa.'),
    (6, 'Sofia', 'Coppola', '1971-05-14',
     'Directora, guionista y productora estadounidense nacida en Nueva York. Debutó con The Virgin Suicides (1999) y ganó el Óscar al mejor guion original por Lost in Translation (2003).');

-- 2) Películas: cada una apunta a su director por id.
INSERT INTO movie (id, title, release_year, duration_minutes, director_id) VALUES
    (1, 'The Prestige', 2006, 130, 1),
    (2, 'The Dark Knight', 2008, 152, 1),
    (3, 'Inception', 2010, 148, 1),
    (4, 'Interstellar', 2014, 169, 1),
    (5, 'Pulp Fiction', 1994, 154, 2),
    (6, 'Once Upon a Time in Hollywood', 2019, 161, 2),
    (7, 'The Departed', 2006, 151, 3),
    (8, 'The Wolf of Wall Street', 2013, 180, 3),
    (9, 'El laberinto del fauno', 2006, 118, 4),
    (10, 'Mar adentro', 2004, 125, 5);

-- 3) Actores (biography queda NULL: aún no tenemos biografías de actores, D-37).
INSERT INTO actor (id, first_name, last_name, birth_date) VALUES
    (1, 'Leonardo', 'DiCaprio', '1974-11-11'),
    (2, 'Michael', 'Caine', '1933-03-14'),
    (3, 'Christian', 'Bale', '1974-01-30'),
    (4, 'Matt', 'Damon', '1970-10-08'),
    (5, 'Matthew', 'McConaughey', '1969-11-04'),
    (6, 'Margot', 'Robbie', '1990-07-02'),
    (7, 'Anne', 'Hathaway', '1982-11-12'),
    (8, 'Hugh', 'Jackman', '1968-10-12'),
    (9, 'Samuel L.', 'Jackson', '1948-12-21'),
    (10, 'Uma', 'Thurman', '1970-04-29'),
    (11, 'Brad', 'Pitt', '1963-12-18'),
    (12, 'Maribel', 'Verdú', '1970-10-02'),
    (13, 'Ivana', 'Baquero', '1994-06-11'),
    (14, 'Javier', 'Bardem', '1969-03-01');

-- 4) Participaciones (tabla intermedia). Una fila por pareja película-actor: 24 en total.
INSERT INTO movie_actor (movie_id, actor_id) VALUES
    -- The Prestige
    (1, 3), (1, 8), (1, 2),
    -- The Dark Knight
    (2, 3), (2, 2),
    -- Inception
    (3, 1), (3, 2),
    -- Interstellar
    (4, 5), (4, 7), (4, 2), (4, 4),
    -- Pulp Fiction
    (5, 9), (5, 10),
    -- Once Upon a Time in Hollywood
    (6, 1), (6, 11), (6, 6),
    -- The Departed
    (7, 1), (7, 4),
    -- The Wolf of Wall Street
    (8, 1), (8, 6), (8, 5),
    -- El laberinto del fauno
    (9, 12), (9, 13),
    -- Mar adentro
    (10, 14);
