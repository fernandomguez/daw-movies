-- Fase 8, migración 4: tabla intermedia movie_actor (N:M entre movie y actor).
-- Depende de movie y de actor, por eso es la última.
-- Clave primaria compuesta: una misma pareja no puede repetirse (D-14).

CREATE TABLE movie_actor (
    movie_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    CONSTRAINT pk_movie_actor PRIMARY KEY (movie_id, actor_id),
    CONSTRAINT fk_movie_actor_movie FOREIGN KEY (movie_id)
        REFERENCES movie (id) ON DELETE CASCADE,
    CONSTRAINT fk_movie_actor_actor FOREIGN KEY (actor_id)
        REFERENCES actor (id) ON DELETE RESTRICT
);
