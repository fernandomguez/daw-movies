package com.daw.movies.ratings.internal.domain.exception;

/**
 * La película que se quiere valorar no existe en el módulo catalog (la
 * fachada respondió vacío).
 */
public class MovieNotFoundException extends RuntimeException {

	public MovieNotFoundException(Long movieId) {
		super("No existe la película con id " + movieId);
	}

}
