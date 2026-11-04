package com.daw.movies.catalog.internal.application;

import java.util.List;

import jakarta.inject.Named;

import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;

/**
 * Caso de uso: listar las películas ({@code GET /movies}).
 * <p>
 * Un solo método público ({@code execute}), sin transacción porque es lectura,
 * y sin acceder a {@code persistence} ni a {@code api} (§3.3): el dominio se
 * consulta por sus puertos y servicios.
 */
@Named
public class FindMoviesUseCase {

	private final MovieRepository movieRepository;

	public FindMoviesUseCase(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	public List<Movie> execute() {
		return movieRepository.findAll();
	}

}
