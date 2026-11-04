package com.daw.movies.ratings.api.http.mapper;

import org.springframework.stereotype.Component;

import com.daw.movies.ratings.api.http.dto.response.RatingResponse;
import com.daw.movies.ratings.internal.domain.model.Rating;

/**
 * Solo la salida del módulo {@code Ratings}: dominio → {@code RatingResponse}.
 */
@Component
public class RatingMapper {

	public RatingResponse toResponse(Rating rating) {
		return new RatingResponse(rating.getId(), rating.getMovieId(), rating.getScore(), rating.getComment());
	}

}
