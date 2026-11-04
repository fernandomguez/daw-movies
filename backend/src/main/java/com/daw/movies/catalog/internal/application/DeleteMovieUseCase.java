package com.daw.movies.catalog.internal.application;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.catalog.internal.domain.service.MovieService;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;

/**
 * Caso de uso: borrar una película ({@code DELETE /movies/{id}}). En
 * transacción porque escribe.
 */
@Named
public class DeleteMovieUseCase {

	private final MovieService movieService;
	private final MovieRepository movieRepository;

	public DeleteMovieUseCase(MovieService movieService, MovieRepository movieRepository) {
		this.movieService = movieService;
		this.movieRepository = movieRepository;
	}

	@Transactional
	public void execute(Long id) {
		movieService.findMovie(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
		movieRepository.deleteById(id);
	}

}
