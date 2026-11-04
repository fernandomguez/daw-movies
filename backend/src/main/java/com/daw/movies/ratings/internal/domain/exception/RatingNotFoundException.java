package com.daw.movies.ratings.internal.domain.exception;

/**
 * La valoración con ese id no existe.
 */
public class RatingNotFoundException extends RuntimeException {

	public RatingNotFoundException(Long id) {
		super("No existe la valoración con id " + id);
	}

}
