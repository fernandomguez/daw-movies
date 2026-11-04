package com.daw.movies.catalog.api.http;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.daw.movies.catalog.api.http.dto.request.MovieRequest;
import com.daw.movies.catalog.api.http.dto.response.MovieResponse;
import com.daw.movies.catalog.api.http.mapper.MovieMapper;
import com.daw.movies.catalog.internal.application.CreateMovieUseCase;
import com.daw.movies.catalog.internal.application.DeleteMovieUseCase;
import com.daw.movies.catalog.internal.application.FindMovieByIdUseCase;
import com.daw.movies.catalog.internal.application.FindMoviesUseCase;
import com.daw.movies.catalog.internal.application.UpdateMovieUseCase;

import jakarta.validation.Valid;

/**
 * Contrato HTTP del módulo (§3.3, {@code api/http}).
 * <p>
 * Los controllers llaman a los <strong>casos de uso</strong> del propio
 * módulo; nunca a repositorios, a {@code persistence} ni a fachadas ajenas.
 * El cuerpo de entrada se valida con {@code @Valid} en los DTO web.
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

	private final FindMoviesUseCase findMovies;
	private final FindMovieByIdUseCase findMovieById;
	private final CreateMovieUseCase createMovie;
	private final UpdateMovieUseCase updateMovie;
	private final DeleteMovieUseCase deleteMovie;
	private final MovieMapper movieMapper;

	public MovieController(FindMoviesUseCase findMovies, FindMovieByIdUseCase findMovieById,
			CreateMovieUseCase createMovie, UpdateMovieUseCase updateMovie,
			DeleteMovieUseCase deleteMovie, MovieMapper movieMapper) {
		this.findMovies = findMovies;
		this.findMovieById = findMovieById;
		this.createMovie = createMovie;
		this.updateMovie = updateMovie;
		this.deleteMovie = deleteMovie;
		this.movieMapper = movieMapper;
	}

	@GetMapping
	public List<MovieResponse> findAll() {
		return findMovies.execute().stream()
				.map(movieMapper::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public MovieResponse findById(@PathVariable Long id) {
		return movieMapper.toResponse(findMovieById.execute(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MovieResponse create(@Valid @RequestBody MovieRequest request) {
		return movieMapper.toResponse(createMovie.execute(
				request.title(), request.releaseYear(), request.duration(), request.directorId()));
	}

	@PutMapping("/{id}")
	public MovieResponse update(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
		return movieMapper.toResponse(updateMovie.execute(
				id, request.title(), request.releaseYear(), request.duration(), request.directorId()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		deleteMovie.execute(id);
	}

}
