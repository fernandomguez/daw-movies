package com.daw.movies.ratings.domain.service;

import java.util.List;

import com.daw.movies.ratings.domain.model.Rating;

/**
 * Casos de uso de valoraciones. Recibe valores sueltos (con el id de la
 * película, no objetos cruzando la fachada) y devuelve el modelo del módulo.
 */
public interface RatingService {

	List<Rating> findByMovieId(Long movieId);

	Rating create(Long movieId, Integer score, String comment);

	void delete(Long id);

}
