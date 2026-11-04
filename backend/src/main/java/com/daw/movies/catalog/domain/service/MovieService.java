package com.daw.movies.catalog.domain.service;

import java.util.List;

import com.daw.movies.catalog.domain.model.Movie;

/**
 * Casos de uso de películas. Recibe valores sueltos, no DTOs: los DTO son de la
 * capa de entrada y el dominio no los conoce.
 */
public interface MovieService {

	List<Movie> findAll();

	Movie findById(Long id);

	Movie create(String title, int releaseYear, int duration, Long directorId);

	Movie update(Long id, String title, int releaseYear, int duration, Long directorId);

	void delete(Long id);

}
