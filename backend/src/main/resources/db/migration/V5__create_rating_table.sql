-- Fase 5 (módulo ratings): tabla rating.
-- Depende de movie (clave foránea), por eso va después de la V3.
-- El módulo ratings es negocio propio: sus filas se borran con su película
-- (CASCADE), al contrario que movie → director (RESTRICT): una película no
-- es propiedad del director, una valoración sí lo es de su película.

CREATE TABLE rating (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    movie_id  BIGINT       NOT NULL,
    score     INT          NOT NULL,
    comment   VARCHAR(500) NULL,
    CONSTRAINT pk_rating PRIMARY KEY (id),
    CONSTRAINT fk_rating_movie FOREIGN KEY (movie_id)
        REFERENCES movie (id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_score CHECK (score BETWEEN 1 AND 10)
);
