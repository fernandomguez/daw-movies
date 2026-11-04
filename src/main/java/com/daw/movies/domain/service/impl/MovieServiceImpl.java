package com.daw.movies.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.domain.exception.DirectorNotFoundException;
import com.daw.movies.domain.exception.MovieNotFoundException;
import com.daw.movies.domain.model.Director;
import com.daw.movies.domain.model.Movie;
import com.daw.movies.domain.repository.MovieRepository;
import com.daw.movies.domain.service.MovieService;

/**
 * Casos de uso de películas.
 * <p>
 * Depende solo de {@link MovieRepository}, la interfaz, nunca de la
 * implementación. Todo caso de uso va dentro de una transacción.
 */
@Service
@Transactional
public class MovieServiceImpl implements MovieService {

	private final MovieRepository movieRepository;

	public MovieServiceImpl(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@Override
	public List<Movie> findAll() {
		return movieRepository.findAll();
	}

	@Override
	public Movie findById(Long id) {
		return movieRepository.findById(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	@Override
	public Movie create(String title, int releaseYear, int duration, Long directorId) {
		Director director = findDirector(directorId);
		Movie movie = new Movie(title, releaseYear, duration, director);
		Long id = movieRepository.save(movie);
		return movieRepository.findById(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	@Override
	public Movie update(Long id, String title, int releaseYear, int duration, Long directorId) {
		movieRepository.findById(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
		Director director = findDirector(directorId);
		movieRepository.update(id, new Movie(title, releaseYear, duration, director));
		return movieRepository.findById(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	@Override
	public void delete(Long id) {
		movieRepository.findById(id)
				.orElseThrow(() -> new MovieNotFoundException(id));
		movieRepository.deleteById(id);
	}

	private Director findDirector(Long directorId) {
		return movieRepository.findDirectorById(directorId)
				.orElseThrow(() -> new DirectorNotFoundException(directorId));
	}

}
