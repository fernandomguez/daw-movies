package com.daw.movies.ratings.persistence.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.daw.movies.ratings.domain.model.Rating;
import com.daw.movies.ratings.domain.repository.RatingRepository;
import com.daw.movies.ratings.persistence.dao.RatingDao;

/**
 * Adaptador del puerto {@link RatingRepository} sobre el DAO.
 * <p>
 * Depende solo de la interfaz {@link RatingDao}, nunca de {@code RatingDaoJdbc}.
 */
@Repository
public class RatingRepositoryImpl implements RatingRepository {

	private final RatingDao ratingDao;

	public RatingRepositoryImpl(RatingDao ratingDao) {
		this.ratingDao = ratingDao;
	}

	@Override
	public Optional<Rating> findById(Long id) {
		return ratingDao.findById(id);
	}

	@Override
	public List<Rating> findByMovieId(Long movieId) {
		return ratingDao.findByMovieId(movieId);
	}

	@Override
	public Long save(Rating rating) {
		return ratingDao.insert(rating);
	}

	@Override
	public void deleteById(Long id) {
		ratingDao.deleteById(id);
	}

}
