package com.daw.movies.catalog.internal.application;

import jakarta.inject.Named;

import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.service.MovieService;

/**
 * Caso de uso: leer una película por id ({@code GET /movies/{id}}).
 * <p>
 * La regla «si no existe, no hay película que contar» es de aplicación, así
 * que aquí se decide el 404: el servicio de dominio solo dice si está.
 */
@Named
public class FindMovieByIdUseCase {

	private final MovieService movieService;

	public FindMovieByIdUseCase(MovieService movieService) {
		this.movieService = movieService;
	}

	public Movie execute(Long id) {
		return movieService.findMovie(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

}
