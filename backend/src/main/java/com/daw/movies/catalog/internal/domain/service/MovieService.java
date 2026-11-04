package com.daw.movies.catalog.internal.domain.service;

import java.util.Optional;

import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;

/**
 * Servicio de dominio (§3.3): las consultas del módulo que usan los casos de
 * uso ({@code internal.application}) y la fachada ({@code api.facade}).
 * <p>
 * Java puro salvo lo tolerado: la orquestación (guardar, releer, decidir
 * códigos) es cosa de los casos de uso, y las transacciones van con ellos.
 */
public interface MovieService {

	/** La película con ese id, o {@code empty} si no existe. */
	Optional<Movie> findMovie(Long id);

	/** El director con ese id, o {@code empty} si no existe. */
	Optional<Director> findDirector(Long id);

}
