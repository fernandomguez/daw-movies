package com.daw.movies.ratings.persistence.dao;

import java.util.List;
import java.util.Optional;

import com.daw.movies.ratings.domain.model.Rating;

/**
 * Acceso a datos de la tabla {@code rating}.
 */
public interface RatingDao {

	Optional<Rating> findById(Long id);

	List<Rating> findByMovieId(Long movieId);

	/** Inserta la fila y devuelve el id generado por la base de datos. */
	Long insert(Rating rating);

	void deleteById(Long id);

}
