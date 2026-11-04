package com.daw.movies.ratings.internal.application;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import com.daw.movies.ratings.internal.domain.exception.RatingNotFoundException;
import com.daw.movies.ratings.internal.domain.repository.RatingRepository;

/**
 * Caso de uso: borrar una valoración ({@code DELETE /ratings/{id}}).
 * En transacción porque escribe.
 */
@Named
public class DeleteRatingUseCase {

	private final RatingRepository ratingRepository;

	public DeleteRatingUseCase(RatingRepository ratingRepository) {
		this.ratingRepository = ratingRepository;
	}

	@Transactional
	public void execute(Long id) {
		ratingRepository.findById(id)
				.orElseThrow(() -> new RatingNotFoundException(id));
		ratingRepository.deleteById(id);
	}

}
