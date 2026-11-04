package com.daw.movies.ratings.internal.application;

import java.util.List;

import jakarta.inject.Named;

import com.daw.movies.ratings.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.domain.port.AvailableMoviesPort;
import com.daw.movies.ratings.internal.domain.repository.RatingRepository;

/**
 * Caso de uso: las valoraciones de una película
 * ({@code GET /movies/{movieId}/ratings}).
 * <p>
 * D-39: la lista de una película real sin valoraciones es {@code 200 []},
 * pero de una película que no existe es 404. Lectura: sin transacción.
 */
@Named
public class FindRatingsByMovieUseCase {

	private final AvailableMoviesPort availableMovies;
	private final RatingRepository ratingRepository;

	public FindRatingsByMovieUseCase(AvailableMoviesPort availableMovies, RatingRepository ratingRepository) {
		this.availableMovies = availableMovies;
		this.ratingRepository = ratingRepository;
	}

	public List<Rating> execute(Long movieId) {
		availableMovies.findMovie(movieId)
				.orElseThrow(() -> new MovieNotFoundException(movieId));
		return ratingRepository.findByMovieId(movieId);
	}

}
