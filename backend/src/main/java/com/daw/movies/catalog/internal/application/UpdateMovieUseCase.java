package com.daw.movies.catalog.internal.application;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import com.daw.movies.catalog.internal.domain.exception.DirectorNotFoundException;
import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;
import com.daw.movies.catalog.internal.domain.service.MovieService;

/**
 * Caso de uso: actualizar una película ({@code PUT /movies/{id}}).
 * <p>
 * Primero comprueba la película y después el director (el mismo criterio que
 * existía desde la fase 31: si ambas fallan solo se ve el 404 de la
 * película). Transacción porque escribe.
 */
@Named
public class UpdateMovieUseCase {

	private final MovieService movieService;
	private final MovieRepository movieRepository;

	public UpdateMovieUseCase(MovieService movieService, MovieRepository movieRepository) {
		this.movieService = movieService;
		this.movieRepository = movieRepository;
	}

	@Transactional
	public Movie execute(Long id, String title, int releaseYear, int duration, Long directorId) {
		movieService.findMovie(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
		Director director = movieService.findDirector(directorId)
				.orElseThrow(() -> new DirectorNotFoundException(directorId));
		movieRepository.update(id, new Movie(title, releaseYear, duration, director));
		return reload(id);
	}

	private Movie reload(Long id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException(
						"La película " + id + " no se encuentra justo después de guardarla"));
	}

}
