package com.daw.movies.domain.exception;

/**
 * La película pedida no existe.
 * <p>
 * El dominio no sabe de HTTP: la capa de entrada la traduce a 404.
 */
public class MovieNotFoundException extends RuntimeException {

	public MovieNotFoundException(Long id) {
		super("No existe la película con id " + id);
	}

}
