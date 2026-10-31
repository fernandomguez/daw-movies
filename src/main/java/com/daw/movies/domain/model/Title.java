package com.daw.movies.domain.model;

/**
 * Título de una película.
 * <p>
 * Garantiza un texto no vacío de 255 caracteres como máximo, el mismo límite
 * que la columna {@code movie.title} ({@code VARCHAR(255) NOT NULL}, V3).
 */
public record Title(String value) {

	public Title {
		value = Texts.requireText(value, "El título", 255);
	}

}
