package com.daw.movies.catalog;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.daw.movies.catalog.persistence.dao.MovieDao;

/**
 * Implementación de la fachada: solo delega en el DAO interno y traduce el
 * dominio al record del contrato. Sin reglas de negocio (eso está en
 * {@code domain}).
 */
@Service
public class CatalogFacadeImpl implements CatalogFacade {

	private final MovieDao movieDao;

	public CatalogFacadeImpl(MovieDao movieDao) {
		this.movieDao = movieDao;
	}

	@Override
	public Optional<MovieLookup> findMovie(Long id) {
		return movieDao.findById(id)
				.map(movie -> new MovieLookup(movie.getId(), movie.getTitle()));
	}

}
