package com.daw.movies.ratings.persistence.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.ratings.domain.model.Rating;
import com.daw.movies.ratings.persistence.dao.impl.jdbc.RatingDaoJdbc;

// Necesita MariaDB arrancado (docker compose up -d).
@SpringBootTest
/*
 * Test del adaptador en espejo (PER-R09): el puerto RatingRepository delega en
 * el DAO y la BD real contesta. Rollback por test para conservar seed.sql.
 */
@Transactional
class RatingRepositoryImplTest {

	@Autowired
	private JdbcTemplate jdbc;

	private com.daw.movies.ratings.domain.repository.RatingRepository repository;

	private Long movieId;

	@BeforeEach
	void preparaPelicula() {
		jdbc.update("INSERT INTO director (first_name, last_name, birth_date) VALUES ('Ana', 'Reichert', '1873-08-23')");
		Long directorId = jdbc.queryForObject("SELECT MAX(id) FROM director", Long.class);
		jdbc.update("INSERT INTO movie (title, release_year, duration_minutes, director_id) "
				+ "VALUES ('Salt of the Earth', 1954, 94, ?)", directorId);
		movieId = jdbc.queryForObject("SELECT MAX(id) FROM movie", Long.class);
		repository = new com.daw.movies.ratings.persistence.repository.impl.RatingRepositoryImpl(
				new RatingDaoJdbc(jdbc));
	}

	@Test
	void saveReturnsTheGeneratedIdAndPersistsTheRating() {
		Long id = repository.save(new Rating(movieId, 8, "Excelente"));

		assertThat(id).isNotNull();
		assertThat(repository.findById(id))
				.hasValueSatisfying(rating -> {
					assertThat(rating.getMovieId()).isEqualTo(movieId);
					assertThat(rating.getScore()).isEqualTo(8);
				});
	}

	@Test
	void findByMovieIdReturnsTheRatingsOfTheMovie() {
		repository.save(new Rating(movieId, 8, "Excelente"));
		repository.save(new Rating(movieId, 5, "Regular"));

		List<Rating> ratings = repository.findByMovieId(movieId);

		assertThat(ratings).hasSize(2);
	}

	@Test
	void deleteByIdRemovesTheRatingThroughThePort() {
		Long id = repository.save(new Rating(movieId, 9, null));

		repository.deleteById(id);

		assertThat(repository.findById(id)).isEmpty();
	}

}
