-- Fase 8, migración 1: tabla director (diseño de la fase 3; biography de D-37).
-- Flyway la ejecuta una sola vez. Una vez aplicada NO se edita: se corrige con otra migración.

CREATE TABLE director (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    birth_date DATE         NOT NULL,
    biography  TEXT         NULL,
    CONSTRAINT pk_director PRIMARY KEY (id)
);
