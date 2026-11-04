package com.daw.movies.ratings.domain.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.catalog.CatalogFacade;
import com.daw.movies.ratings.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.domain.exception.RatingNotFoundException;
import com.daw.movies.ratings.domain.model.Rating;
import com.daw.movies.ratings.domain.repository.RatingRepository;
import com.daw.movies.ratings.domain.service.RatingService;

/**
 * Casos de uso de valoraciones.
 * <p>
 * La dependencia con el módulo catalog vive <strong>solo aquí</strong>, a
 * través de {@link CatalogFacade}: si la película no existe, nace la
 * {@link MovieNotFoundException} propia del módulo (la fachada no exporta
 * excepciones ajenas). Depende solo de {@link RatingRepository}, la interfaz,
 * y vuelve a leer la valoración tras guardarla, igual que hace el módulo
 * catalog (lanza {@link IllegalStateException} si la relectura falla).
 */
@Service
@Transactional
public class RatingServiceImpl implements RatingService {

	private final RatingRepository ratingRepository;
	private final CatalogFacade catalogFacade;

	public RatingServiceImpl(RatingRepository ratingRepository, CatalogFacade catalogFacade) {
		this.ratingRepository = ratingRepository;
		this.catalogFacade = catalogFacade;
	}

	@Override
	public List<Rating> findByMovieId(Long movieId) {
		requireMovie(movieId);
		return ratingRepository.findByMovieId(movieId);
	}

	@Override
	public Rating create(Long movieId, Integer score, String comment) {
		requireMovie(movieId);
		Long id = ratingRepository.save(new Rating(movieId, score, comment));
		return reload(id);
	}

	@Override
	public void delete(Long id) {
		ratingRepository.findById(id)
				.orElseThrow(() -> new RatingNotFoundException(id));
		ratingRepository.deleteById(id);
	}

	private void requireMovie(Long movieId) {
		if (catalogFacade.findMovie(movieId).isEmpty()) {
			throw new MovieNotFoundException(movieId);
		}
	}

	/**
	 * Vuelve a leer una valoración que acabamos de escribir. Si no aparece es
	 * un fallo del sistema, no un 404 (mismo criterio que el módulo catalog).
	 */
	private Rating reload(Long id) {
		return ratingRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException(
						"La valoración " + id + " no se encuentra justo después de guardarla"));
	}

}
