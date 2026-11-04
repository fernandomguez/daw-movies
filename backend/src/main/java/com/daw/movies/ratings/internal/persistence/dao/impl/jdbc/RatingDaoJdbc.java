package com.daw.movies.ratings.internal.persistence.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.persistence.dao.RatingDao;

/**
 * Implementación JDBC de {@link RatingDao} con {@link JdbcTemplate}, igual
 * que {@code MovieDaoJdbc} en su módulo (D-48).
 * <p>
 * La tabla {@code rating} está desnormalizada contra la fachada: guarda solo
 * {@code movie_id}, así que no hay {@code JOIN} ninguno y el N+1 que se acepta
 * en {@code MovieDaoJdbc.findAll} aquí no existe.
 */
@Repository
public class RatingDaoJdbc implements RatingDao {

	private static final String SELECT_RATING = """
			SELECT id, movie_id, score, comment
			FROM rating
			""";

	private static final String SELECT_BY_MOVIE = """
			SELECT id, movie_id, score, comment
			FROM rating
			WHERE movie_id = ?
			ORDER BY id
			""";

	private static final String INSERT_RATING = """
			INSERT INTO rating (movie_id, score, comment)
			VALUES (?, ?, ?)
			""";

	private static final String DELETE_RATING = "DELETE FROM rating WHERE id = ?";

	private final JdbcTemplate jdbc;

	public RatingDaoJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public Optional<Rating> findById(Long id) {
		List<Rating> ratings = jdbc.query(SELECT_RATING + " WHERE id = ?", mapRating(), id);
		return ratings.stream().findFirst();
	}

	@Override
	public List<Rating> findByMovieId(Long movieId) {
		return jdbc.query(SELECT_BY_MOVIE, mapRating(), movieId);
	}

	@Override
	public Long insert(Rating rating) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(INSERT_RATING, Statement.RETURN_GENERATED_KEYS);
			ps.setLong(1, rating.getMovieId());
			ps.setInt(2, rating.getScore());
			ps.setString(3, rating.getComment());
			return ps;
		}, keyHolder);
		Number key = keyHolder.getKey();
		if (key == null) {
			throw new IllegalStateException("La base de datos no devolvió el id generado para la valoración");
		}
		return key.longValue();
	}

	@Override
	public void deleteById(Long id) {
		jdbc.update(DELETE_RATING, id);
	}

	private RowMapper<Rating> mapRating() {
		return (rs, rowNum) -> mapRatingRow(rs);
	}

	private Rating mapRatingRow(ResultSet rs) throws SQLException {
		return new Rating(
				rs.getLong("id"),
				rs.getLong("movie_id"),
				rs.getInt("score"),
				rs.getString("comment"));
	}

}
