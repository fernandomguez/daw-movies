package com.daw.movies.ratings.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daw.movies.ratings.controller.dto.request.RatingRequest;
import com.daw.movies.ratings.controller.dto.response.RatingResponse;
import com.daw.movies.ratings.controller.mapper.RatingMapper;
import com.daw.movies.ratings.domain.service.RatingService;

import jakarta.validation.Valid;

/**
 * API REST de valoraciones del módulo {@code Ratings}.
 * <p>
 * Las valoraciones cuelgan de su película ({@code /movies/{movieId}/ratings}),
 * como indica la relación: una valoración sin película no tiene sentido. Solo
 * habla con {@link RatingService} y con sus propios DTOs.
 */
@RestController
public class RatingController {

	private final RatingService ratingService;
	private final RatingMapper ratingMapper;

	public RatingController(RatingService ratingService, RatingMapper ratingMapper) {
		this.ratingService = ratingService;
		this.ratingMapper = ratingMapper;
	}

	/** Las valoraciones de una película, en orden de creación (D-38: 200 [] si no tiene). */
	@GetMapping("/movies/{movieId}/ratings")
	public List<RatingResponse> findByMovie(@PathVariable Long movieId) {
		return ratingService.findByMovieId(movieId).stream()
				.map(ratingMapper::toResponse)
				.toList();
	}

	@PostMapping("/movies/{movieId}/ratings")
	@ResponseStatus(HttpStatus.CREATED)
	public RatingResponse create(@PathVariable Long movieId, @Valid @RequestBody RatingRequest request) {
		return ratingMapper.toResponse(ratingService.create(movieId, request.score(), request.comment()));
	}

	@DeleteMapping("/ratings/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		ratingService.delete(id);
	}

}
