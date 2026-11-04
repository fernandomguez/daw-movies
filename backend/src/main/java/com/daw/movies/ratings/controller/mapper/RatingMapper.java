package com.daw.movies.ratings.controller.mapper;

import org.springframework.stereotype.Component;

import com.daw.movies.ratings.controller.dto.response.RatingResponse;
import com.daw.movies.ratings.domain.model.Rating;

/**
 * Solo la salida del módulo {@code Ratings}: dominio → {@code RatingResponse}.
 */
@Component
public class RatingMapper {

	public RatingResponse toResponse(Rating rating) {
		return new RatingResponse(rating.getId(), rating.getMovieId(), rating.getScore(), rating.getComment());
	}

}
