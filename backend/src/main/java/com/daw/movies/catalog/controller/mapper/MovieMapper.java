package com.daw.movies.catalog.controller.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.daw.movies.catalog.controller.dto.response.MovieResponse;
import com.daw.movies.catalog.domain.model.Movie;

/**
 * Traduce el dominio a DTO de respuesta.
 * <p>
 * El camino de entrada no necesita mapper: el controlador pasa los campos del
 * {@code MovieRequest} al servicio, que recibe valores sueltos.
 */
@Component
public class MovieMapper {

	public MovieResponse toResponse(Movie movie) {
		MovieResponse.DirectorView director = new MovieResponse.DirectorView(
				movie.getDirector().getId(), movie.getDirector().fullName());
		List<MovieResponse.ActorView> actors = movie.getActors().stream()
				.map(actor -> new MovieResponse.ActorView(actor.getId(), actor.fullName()))
				.toList();
		return new MovieResponse(
				movie.getId(),
				movie.getTitle(),
				movie.getReleaseYear(),
				movie.getDuration(),
				director,
				actors);
	}

}
