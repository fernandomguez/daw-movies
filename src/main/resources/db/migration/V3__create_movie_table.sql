-- Fase 8, migración 3: tabla movie.
-- Depende de director (clave foránea), por eso va después de la V1.

CREATE TABLE movie (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    title            VARCHAR(255) NOT NULL,
    release_year     INT          NOT NULL,
    duration_minutes INT          NOT NULL,
    director_id      BIGINT       NOT NULL,
    CONSTRAINT pk_movie PRIMARY KEY (id),
    CONSTRAINT fk_movie_director FOREIGN KEY (director_id)
        REFERENCES director (id) ON DELETE RESTRICT,
    CONSTRAINT chk_movie_duration_positive CHECK (duration_minutes > 0),
    CONSTRAINT chk_movie_release_year CHECK (release_year >= 1888)
);
