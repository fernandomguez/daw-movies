package com.daw.movies.ratings.api.http;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daw.movies.ratings.api.http.dto.request.RatingRequest;
import com.daw.movies.ratings.api.http.dto.response.RatingResponse;
import com.daw.movies.ratings.api.http.mapper.RatingMapper;
import com.daw.movies.ratings.internal.application.CreateRatingUseCase;
import com.daw.movies.ratings.internal.application.DeleteRatingUseCase;
import com.daw.movies.ratings.internal.application.FindRatingsByMovieUseCase;

import jakarta.validation.Valid;

/**
 * Contrato HTTP del módulo {@code Ratings} (§3.3, {@code api/http}).
 * <p>
 * Los controllers llaman a los <strong>casos de uso</strong> del propio
 * módulo; nunca a repositorios, a {@code internal.persistence} ni a fachadas
 * ajenas. Las valoraciones cuelgan de su película
 * ({@code /movies/{movieId}/ratings}), como indica la relación.
 */
@RestController
public class RatingController {

	private final FindRatingsByMovieUseCase findRatingsByMovie;
	private final CreateRatingUseCase createRating;
	private final DeleteRatingUseCase deleteRating;
	private final RatingMapper ratingMapper;

	public RatingController(FindRatingsByMovieUseCase findRatingsByMovie, CreateRatingUseCase createRating,
			DeleteRatingUseCase deleteRating, RatingMapper ratingMapper) {
		this.findRatingsByMovie = findRatingsByMovie;
		this.createRating = createRating;
		this.deleteRating = deleteRating;
		this.ratingMapper = ratingMapper;
	}

	/** Las valoraciones de una película, en orden de creación (D-39: 200 [] si no tiene). */
	@GetMapping("/movies/{movieId}/ratings")
	public List<RatingResponse> findByMovie(@PathVariable Long movieId) {
		return findRatingsByMovie.execute(movieId).stream()
				.map(ratingMapper::toResponse)
				.toList();
	}

	@PostMapping("/movies/{movieId}/ratings")
	@ResponseStatus(HttpStatus.CREATED)
	public RatingResponse create(@PathVariable Long movieId, @Valid @RequestBody RatingRequest request) {
		return ratingMapper.toResponse(createRating.execute(movieId, request.score(), request.comment()));
	}

	@DeleteMapping("/ratings/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		deleteRating.execute(id);
	}

}
