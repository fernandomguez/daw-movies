-- Fase 8, migración 2: tabla actor (diseño de la fase 3; biography de D-37).
-- No depende de ninguna otra tabla, por eso puede ir antes que movie.

CREATE TABLE actor (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    birth_date DATE         NOT NULL,
    biography  TEXT         NULL,
    CONSTRAINT pk_actor PRIMARY KEY (id)
);
