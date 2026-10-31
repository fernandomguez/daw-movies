package com.daw.movies.domain.model;

/**
 * Año de estreno de una película.
 * <p>
 * Garantiza el mínimo del {@code CHECK chk_movie_release_year} ({@code >= 1888}),
 * el año de la primera película proyectada. No se pone tope superior: una
 * película anunciada para el futuro es un dato legítimo.
 */
public record ReleaseYear(int value) {

	private static final int FIRST_FILM_YEAR = 1888;

	public ReleaseYear {
		if (value < FIRST_FILM_YEAR) {
			throw new IllegalArgumentException(
					"El año de estreno no puede ser anterior a " + FIRST_FILM_YEAR);
		}
	}

}
