package com.daw.movies.domain.model;

/**
 * Biografía de un director: texto libre, opcional.
 * <p>
 * La columna es {@code biography TEXT NULL} (D-37). La ausencia de biografía se
 * representa con {@code null} en el campo de la entidad, nunca con un
 * {@code Biography} vacío; pasar un texto en blanco a {@code null} es
 * normalización de entrada (mapper de la fase 17 y DTO de la fase 18).
 */
public record Biography(String value) {

	public Biography {
		value = Texts.requireText(value, "La biografía");
	}

}
