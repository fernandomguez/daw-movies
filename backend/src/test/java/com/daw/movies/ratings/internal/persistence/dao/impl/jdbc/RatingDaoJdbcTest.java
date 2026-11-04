package com.daw.movies.ratings.internal.persistence.dao.impl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.persistence.dao.RatingDao;

// Necesita MariaDB arrancado (docker compose up -d).
@SpringBootTest
/*
 * PER-D8 adaptado, como en MovieDaoJdbcTest: transacción con rollback para
 * conservar seed.sql, JdbcTemplate real sin mocks. Con la FK a movie, cada
 * test crea SU película (director incluido) y no depende del seed.
 */
@Transactional
class RatingDaoJdbcTest {

	@Autowired
	private RatingDao ratingDao;

	@Autowired
	private JdbcTemplate jdbc;

	private Long movieId;
	private Long otraMovieId;

	@BeforeEach
	void preparaPeliculas() {
		jdbc.update("INSERT INTO director (first_name, last_name, birth_date) VALUES ('Ana', 'Reichert', '1873-08-23')");
		Long directorId = lastId("director");
		jdbc.update("INSERT INTO movie (title, release_year, duration_minutes, director_id) "
				+ "VALUES ('Salt of the Earth', 1954, 94, ?)", directorId);
		movieId = lastId("movie");
		jdbc.update("INSERT INTO movie (title, release_year, duration_minutes, director_id) "
				+ "VALUES ('Nina Ansary Documentary', 1976, 110, ?)", directorId);
		otraMovieId = lastId("movie");
	}

	private Long lastId(String table) {
		return jdbc.queryForObject("SELECT MAX(id) FROM " + table, Long.class);
	}

	@Test
	void insertReturnsTheGeneratedIdAndFindByIdReadsTheRating() {
		Long id = ratingDao.insert(new Rating(movieId, 8, "Excelente"));

		assertThat(id).isNotNull();
		Optional<Rating> read = ratingDao.findById(id);
		assertThat(read).isPresent();
		assertThat(read.get().getId()).isEqualTo(id);
		assertThat(read.get().getMovieId()).isEqualTo(movieId);
		assertThat(read.get().getScore()).isEqualTo(8);
		assertThat(read.get().getComment()).isEqualTo("Excelente");
	}

	@Test
	void insertAcceptsARatingWithoutComment() {
		Long id = ratingDao.insert(new Rating(movieId, 7, null));

		assertThat(ratingDao.findById(id))
				.hasValueSatisfying(rating -> assertThat(rating.getComment()).isNull());
	}

	@Test
	void findByMovieIdReturnsOnlyThatMoviesRatingsOrderedById() {
		Long firstId = ratingDao.insert(new Rating(movieId, 8, null));
		ratingDao.insert(new Rating(otraMovieId, 3, "Otra película"));
		Long thirdId = ratingDao.insert(new Rating(movieId, 5, "Regular"));

		List<Rating> ratings = ratingDao.findByMovieId(movieId);

		assertThat(ratings)
				.extracting(Rating::getId)
				.containsExactly(firstId, thirdId);
	}

	@Test
	void findByMovieIdReturnsEmptyListWhenTheMovieHasNoRatings() {
		assertThat(ratingDao.findByMovieId(otraMovieId)).isEmpty();
	}

	@Test
	void deleteByIdRemovesTheRating() {
		Long id = ratingDao.insert(new Rating(movieId, 9, null));

		ratingDao.deleteById(id);

		assertThat(ratingDao.findById(id)).isEmpty();
		assertThat(ratingDao.findByMovieId(movieId)).isEmpty();
	}

}
