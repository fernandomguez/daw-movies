package com.daw.movies.domain.model;

/**
 * Duración de una película en minutos.
 * <p>
 * Garantiza la regla del {@code CHECK chk_movie_duration_positive}
 * ({@code > 0}). Sin tope superior: el esquema no lo tiene.
 */
public record DurationMinutes(int value) {

	public DurationMinutes {
		if (value <= 0) {
			throw new IllegalArgumentException("La duración debe ser mayor que cero minutos");
		}
	}

}
