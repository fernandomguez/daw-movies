package com.daw.movies.catalog.internal.application;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import com.daw.movies.catalog.internal.domain.exception.DirectorNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;
import com.daw.movies.catalog.internal.domain.service.MovieService;

/**
 * Caso de uso: crear una película ({@code POST /movies}).
 * <p>
 * Orquesta el flujo completo (§3.3): localiza el director por el servicio de
 * dominio, construye la película, guarda por el puerto del repositorio y
 * relee. Va en transacción ({@code @jakarta.transaction.Transactional}) porque
 * escribe. No invoca otros casos de uso.
 */
@Named
public class CreateMovieUseCase {

	private final MovieService movieService;
	private final MovieRepository movieRepository;

	public CreateMovieUseCase(MovieService movieService, MovieRepository movieRepository) {
		this.movieService = movieService;
		this.movieRepository = movieRepository;
	}

	@Transactional
	public Movie execute(String title, int releaseYear, int duration, Long directorId) {
		Director director = movieService.findDirector(directorId)
				.orElseThrow(() -> new DirectorNotFoundException(directorId));
		Long id = movieRepository.save(new Movie(title, releaseYear, duration, director));
		return reload(id);
	}

	/**
	 * Vuelve a leer una película que acabamos de escribir. Si no aparece, no es
	 * un 404 (el cliente no ha pedido nada inexistente): es un fallo del
	 * sistema, así que se lanza {@link IllegalStateException}.
	 */
	private Movie reload(Long id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException(
						"La película " + id + " no se encuentra justo después de guardarla"));
	}

}
