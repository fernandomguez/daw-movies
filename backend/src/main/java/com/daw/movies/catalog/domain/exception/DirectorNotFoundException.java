package com.daw.movies.catalog.domain.exception;

/**
 * El director pedido no existe.
 * <p>
 * Ocurre al crear o actualizar una película con un {@code directorId} que no
 * está en la tabla {@code director}. El dominio no sabe de HTTP: la capa de
 * entrada la traduce a 404.
 */
public class DirectorNotFoundException extends RuntimeException {

	public DirectorNotFoundException(Long id) {
		super("No existe el director con id " + id);
	}

}
