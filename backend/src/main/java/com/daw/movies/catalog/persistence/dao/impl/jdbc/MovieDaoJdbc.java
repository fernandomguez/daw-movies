package com.daw.movies.catalog.persistence.dao.impl.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.daw.movies.catalog.domain.model.Actor;
import com.daw.movies.catalog.domain.model.Director;
import com.daw.movies.catalog.domain.model.Movie;
import com.daw.movies.catalog.persistence.dao.MovieDao;

/**
 * Implementación JDBC de {@link MovieDao} con {@link JdbcTemplate}.
 * <p>
 * Lee la película con su director en un {@code JOIN} y el reparto en una
 * segunda consulta sobre la tabla intermedia {@code movie_actor}. Escribir el
 * reparto queda fuera: no hay endpoints que lo gestionen.
 */
@Repository
public class MovieDaoJdbc implements MovieDao {

	private static final String SELECT_MOVIE_WITH_DIRECTOR = """
			SELECT m.id, m.title, m.release_year, m.duration_minutes,
			       d.id AS director_id, d.first_name, d.last_name, d.birth_date, d.biography
			FROM movie m
			JOIN director d ON d.id = m.director_id
			""";

	private static final String SELECT_ACTORS_BY_MOVIE = """
			SELECT a.id AS actor_id, a.first_name, a.last_name, a.birth_date, a.biography
			FROM movie_actor ma
			JOIN actor a ON a.id = ma.actor_id
			WHERE ma.movie_id = ?
			ORDER BY a.id
			""";

	private static final String SELECT_DIRECTOR_BY_ID = """
			SELECT id AS director_id, first_name, last_name, birth_date, biography
			FROM director
			WHERE id = ?
			""";

	private static final String INSERT_MOVIE = """
			INSERT INTO movie (title, release_year, duration_minutes, director_id)
			VALUES (?, ?, ?, ?)
			""";

	private static final String UPDATE_MOVIE = """
			UPDATE movie
			SET title = ?, release_year = ?, duration_minutes = ?, director_id = ?
			WHERE id = ?
			""";

	private static final String DELETE_MOVIE = "DELETE FROM movie WHERE id = ?";

	private final JdbcTemplate jdbc;

	public MovieDaoJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public List<Movie> findAll() {
		List<Movie> movies = jdbc.query(SELECT_MOVIE_WITH_DIRECTOR, mapMovie());
		movies.forEach(this::loadActors);
		return movies;
	}

	@Override
	public Optional<Movie> findById(Long id) {
		List<Movie> movies = jdbc.query(SELECT_MOVIE_WITH_DIRECTOR + " WHERE m.id = ?", mapMovie(), id);
		movies.forEach(this::loadActors);
		return movies.stream().findFirst();
	}

	@Override
	public Optional<Director> findDirectorById(Long id) {
		List<Director> directors = jdbc.query(SELECT_DIRECTOR_BY_ID, mapDirector(), id);
		return directors.stream().findFirst();
	}

	@Override
	public Long insert(Movie movie) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbc.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(INSERT_MOVIE, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, movie.getTitle());
			ps.setInt(2, movie.getReleaseYear());
			ps.setInt(3, movie.getDuration());
			ps.setLong(4, movie.getDirector().getId());
			return ps;
		}, keyHolder);
		Number key = keyHolder.getKey();
		if (key == null) {
			throw new IllegalStateException("La base de datos no devolvió el id generado para la película");
		}
		return key.longValue();
	}

	@Override
	public void update(Long id, Movie movie) {
		jdbc.update(UPDATE_MOVIE,
				movie.getTitle(), movie.getReleaseYear(), movie.getDuration(),
				movie.getDirector().getId(), id);
	}

	@Override
	public void deleteById(Long id) {
		jdbc.update(DELETE_MOVIE, id);
	}

	private void loadActors(Movie movie) {
		List<Actor> actors = jdbc.query(SELECT_ACTORS_BY_MOVIE, mapActor(), movie.getId());
		actors.forEach(movie::addActor);
	}

	private RowMapper<Movie> mapMovie() {
		return (rs, rowNum) -> new Movie(
				rs.getLong("id"),
				rs.getString("title"),
				rs.getInt("release_year"),
				rs.getInt("duration_minutes"),
				mapDirectorRow(rs));
	}

	private RowMapper<Director> mapDirector() {
		return (rs, rowNum) -> mapDirectorRow(rs);
	}

	private RowMapper<Actor> mapActor() {
		return (rs, rowNum) -> mapActorRow(rs);
	}

	private Director mapDirectorRow(ResultSet rs) throws SQLException {
		return new Director(
				rs.getLong("director_id"),
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getObject("birth_date", LocalDate.class),
				rs.getString("biography"));
	}

	private Actor mapActorRow(ResultSet rs) throws SQLException {
		return new Actor(
				rs.getLong("actor_id"),
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getObject("birth_date", LocalDate.class),
				rs.getString("biography"));
	}

}
