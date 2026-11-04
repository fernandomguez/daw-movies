package com.daw.movies.catalog.controller;

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

import com.daw.movies.catalog.controller.dto.request.MovieRequest;
import com.daw.movies.catalog.controller.dto.response.MovieResponse;
import com.daw.movies.catalog.controller.mapper.MovieMapper;
import com.daw.movies.catalog.domain.service.MovieService;

import jakarta.validation.Valid;

/**
 * API REST de películas.
 * <p>
 * Solo habla con {@link MovieService}, la interfaz del dominio, y con sus
 * propios DTOs. El cuerpo de entrada se valida con {@code @Valid}.
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

	private final MovieService movieService;
	private final MovieMapper movieMapper;

	public MovieController(MovieService movieService, MovieMapper movieMapper) {
		this.movieService = movieService;
		this.movieMapper = movieMapper;
	}

	@GetMapping
	public List<MovieResponse> findAll() {
		return movieService.findAll().stream()
				.map(movieMapper::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public MovieResponse findById(@PathVariable Long id) {
		return movieMapper.toResponse(movieService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MovieResponse create(@Valid @RequestBody MovieRequest request) {
		return movieMapper.toResponse(movieService.create(
				request.title(), request.releaseYear(), request.duration(), request.directorId()));
	}

	@PutMapping("/{id}")
	public MovieResponse update(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
		return movieMapper.toResponse(movieService.update(
				id, request.title(), request.releaseYear(), request.duration(), request.directorId()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		movieService.delete(id);
	}

}
