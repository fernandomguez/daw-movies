package com.daw.movies.ratings.internal.domain.repository;

import java.util.List;
import java.util.Optional;

import com.daw.movies.ratings.internal.domain.model.Rating;

/**
 * Puerto del dominio del módulo {@code Ratings} para la persistencia de
 * valoraciones (DIP), igual que {@code MovieRepository} en su módulo.
 */
public interface RatingRepository {

	Optional<Rating> findById(Long id);

	List<Rating> findByMovieId(Long movieId);

	/** Inserta una valoración nueva y devuelve el id que genera la base de datos. */
	Long save(Rating rating);

	void deleteById(Long id);

}
