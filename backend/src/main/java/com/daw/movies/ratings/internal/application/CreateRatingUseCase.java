package com.daw.movies.ratings.internal.application;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import com.daw.movies.ratings.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.domain.port.AvailableMoviesPort;
import com.daw.movies.ratings.internal.domain.repository.RatingRepository;

/**
 * Caso de uso: valorar una película ({@code POST /movies/{movieId}/ratings}).
 * <p>
 * Primero comprueba por el puerto (la película es de OTRO módulo: si la
 * fachada no la encuentra, nace la excepción propia de este módulo) y
 * después guarda y relee. En transacción ({@code @jakarta.transaction.Transactional})
 * porque escribe.
 */
@Named
public class CreateRatingUseCase {

	private final AvailableMoviesPort availableMovies;
	private final RatingRepository ratingRepository;

	public CreateRatingUseCase(AvailableMoviesPort availableMovies, RatingRepository ratingRepository) {
		this.availableMovies = availableMovies;
		this.ratingRepository = ratingRepository;
	}

	@Transactional
	public Rating execute(Long movieId, Integer score, String comment) {
		availableMovies.findMovie(movieId)
				.orElseThrow(() -> new MovieNotFoundException(movieId));
		Long id = ratingRepository.save(new Rating(movieId, score, comment));
		return reload(id);
	}

	private Rating reload(Long id) {
		return ratingRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException(
						"La valoración " + id + " no se encuentra justo después de guardarla"));
	}

}
