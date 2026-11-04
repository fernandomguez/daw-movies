package com.daw.movies.catalog.persistence.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.daw.movies.catalog.domain.model.Director;
import com.daw.movies.catalog.domain.model.Movie;
import com.daw.movies.catalog.domain.repository.MovieRepository;
import com.daw.movies.catalog.persistence.dao.MovieDao;

/**
 * Adaptador del puerto {@link MovieRepository} sobre el DAO.
 * <p>
 * Depende solo de la interfaz {@link MovieDao}, nunca de {@code MovieDaoJdbc}.
 */
@Repository
public class MovieRepositoryImpl implements MovieRepository {

	private final MovieDao movieDao;

	public MovieRepositoryImpl(MovieDao movieDao) {
		this.movieDao = movieDao;
	}

	@Override
	public List<Movie> findAll() {
		return movieDao.findAll();
	}

	@Override
	public Optional<Movie> findById(Long id) {
		return movieDao.findById(id);
	}

	@Override
	public Optional<Director> findDirectorById(Long id) {
		return movieDao.findDirectorById(id);
	}

	@Override
	public Long save(Movie movie) {
		return movieDao.insert(movie);
	}

	@Override
	public void update(Long id, Movie movie) {
		movieDao.update(id, movie);
	}

	@Override
	public void deleteById(Long id) {
		movieDao.deleteById(id);
	}

}
