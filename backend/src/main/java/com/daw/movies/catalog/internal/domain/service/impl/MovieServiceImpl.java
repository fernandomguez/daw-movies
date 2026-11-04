package com.daw.movies.catalog.internal.domain.service;

import java.util.Optional;

import jakarta.inject.Named;

import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;

/**
 * Implementación del servicio de dominio: delega en el puerto del repositorio.
 * Sin anotaciones de Spring ({@code @Named} es lo tolerado en {@code domain},
 * §3.3); nada de transacciones aquí: van con los casos de uso.
 */
@Named
public class MovieServiceImpl implements MovieService {

	private final MovieRepository movieRepository;

	public MovieServiceImpl(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@Override
	public Optional<Movie> findMovie(Long id) {
		return movieRepository.findById(id);
	}

	@Override
	public Optional<Director> findDirector(Long id) {
		return movieRepository.findDirectorById(id);
	}

}
