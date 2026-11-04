package com.daw.movies.persistence.dao.impl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.domain.model.Director;
import com.daw.movies.domain.model.Movie;
import com.daw.movies.persistence.dao.MovieDao;

// Necesita MariaDB arrancado (docker compose up -d).
@SpringBootTest
/*
 * PER-D8 adaptado: aquí hay una sola base de datos (la del compose), con el
 * seed.sql cargado para Swagger y el frontend. Vaciar las tablas en @BeforeEach
 * la dejaría en blanco, así que cada test corre en una transacción que se
 * revierte al terminar: los datos que el test crea desaparecen y el seed queda
 * intacto. Sigue prohibido mockear JdbcTemplate (PER-D8): es la BD real.
 */
@Transactional
class MovieDaoJdbcTest {

	@Autowired
	private MovieDao movieDao;

	@Autowired
	private JdbcTemplate jdbc;

	private Long directorId;
	private Long otroDirectorId;
	private Long primerActorId;
	private Long segundoActorId;

	@BeforeEach
	void preparaDirectoresYActores() {
		jdbc.update("""
				INSERT INTO director (first_name, last_name, birth_date, biography)
				VALUES ('Douglas', 'Adams', '1952-03-11', NULL)
				""");
		directorId = lastId("director");
		jdbc.update("""
				INSERT INTO director (first_name, last_name, birth_date, biography)
				VALUES ('Frank', 'Darabont', '1959-01-28', NULL)
				""");
		otroDirectorId = lastId("director");
		jdbc.update("INSERT INTO actor (first_name, last_name, birth_date) VALUES ('Stephen', 'Fry', '1957-08-24')");
		primerActorId = lastId("actor");
		jdbc.update("INSERT INTO actor (first_name, last_name, birth_date) VALUES ('Alan', 'Rickman', '1946-02-21')");
		segundoActorId = lastId("actor");
	}

	/** Los ids los asigna AUTO_INCREMENT: el último insertado es el mayor. */
	private Long lastId(String table) {
		return jdbc.queryForObject("SELECT MAX(id) FROM " + table, Long.class);
	}

	/**
	 * FILA COMPLETA de todas las películas menos una, para comparar antes y
	 * después: mucho más difícil que checkear solo cantidades (un UPDATE con el
	 * mismo número de filas engaña a un COUNT).
	 */
	private List<Map<String, Object>> otherMovieRows(Long excludedId) {
		return jdbc.queryForList("SELECT id, title, release_year, duration_minutes, director_id "
				+ "FROM movie WHERE id <> ?", excludedId);
	}

	/** JOIN de movie + director: insert, findById y los campos exactos. */
	@Test
	void insertReturnsTheGeneratedIdAndFindByIdReadsTheMovieWithItsDirector() {
		Long id = movieDao.insert(new Movie("Guía del autoestopista galáctico", 2005, 109,
				director(directorId)));

		assertThat(id).isNotNull();

		Optional<Movie> read = movieDao.findById(id);
		assertThat(read).isPresent();
		Movie movie = read.get();
		assertThat(movie.getId()).isEqualTo(id);
		assertThat(movie.getTitle()).isEqualTo("Guía del autoestopista galáctico");
		assertThat(movie.getReleaseYear()).isEqualTo(2005);
		assertThat(movie.getDuration()).isEqualTo(109);
		assertThat(movie.getDirector().getId()).isEqualTo(directorId);
		assertThat(movie.getDirector().fullName()).isEqualTo("Douglas Adams");
		// biografía NULL en la tabla y en el modelo (D-37)
		assertThat(movie.getDirector().getBiography()).isNull();
		// el reparto no se escribe en el insert: una película nueva no tiene actores
		assertThat(movie.getActors()).isEmpty();
	}

	/** La segunda consulta de findById: el reparto de movie_actor, ordenado por id de actor. */
	@Test
	void findByIdLoadsTheRepartoOrderedByActorId() {
		Long id = movieDao.insert(new Movie("The Man in the Iron Mask", 1998, 132,
				director(directorId)));
		jdbc.update("INSERT INTO movie_actor (movie_id, actor_id) VALUES (?, ?)", id, segundoActorId);
		jdbc.update("INSERT INTO movie_actor (movie_id, actor_id) VALUES (?, ?)", id, primerActorId);

		Optional<Movie> read = movieDao.findById(id);

		assertThat(read).isPresent();
		// el ORDER BY de la consulta manda, aunque las filas se inserten al revés
		assertThat(read.get().getActors())
				.extracting(actor -> actor.fullName())
				.containsExactly("Stephen Fry", "Alan Rickman");
	}

	/**
	 * update() cambia solo las columnas de la película: el reparto y las demás
	 * filas de las tablas quedan igual.
	 */
	@Test
	void updateChangesTheColumnsAndLeavesEverythingElseUntouched() {
		Long id = movieDao.insert(new Movie("The Green Mile", 1999, 189, director(directorId)));
		jdbc.update("INSERT INTO movie_actor (movie_id, actor_id) VALUES (?, ?)", id, primerActorId);
		List<Map<String, Object>> otherMoviesBefore = otherMovieRows(id);
		Map<String, Object> repartoBefore = jdbc.queryForMap(
				"SELECT movie_id, actor_id FROM movie_actor WHERE movie_id = ?", id);

		movieDao.update(id, new Movie("La milla verde", 1999, 188, director(otroDirectorId)));

		Optional<Movie> read = movieDao.findById(id);
		assertThat(read).isPresent();
		assertThat(read.get().getTitle()).isEqualTo("La milla verde");
		assertThat(read.get().getReleaseYear()).isEqualTo(1999);
		assertThat(read.get().getDuration()).isEqualTo(188);
		assertThat(read.get().getDirector().getId()).isEqualTo(otroDirectorId);
		assertThat(read.get().getActors())
				.extracting(actor -> actor.fullName())
				.containsExactly("Stephen Fry");
		assertThat(otherMovieRows(id)).isEqualTo(otherMoviesBefore);
		assertThat(jdbc.queryForMap(
				"SELECT movie_id, actor_id FROM movie_actor WHERE movie_id = ?", id)).isEqualTo(repartoBefore);
	}

	/** deleteById borra la fila: findById queda vacío y findAll la pierde. */
	@Test
	void deleteByIdRemovesTheMovie() {
		Long id = movieDao.insert(new Movie("Tenet", 2020, 150, director(directorId)));

		movieDao.deleteById(id);

		assertThat(movieDao.findById(id)).isEmpty();
		assertThat(movieDao.findAll())
				.extracting(Movie::getId)
				.doesNotContain(id);
	}

	/** findAll trae todas las películas (comparado con COUNT) con su director y su reparto. */
	@Test
	void findAllReturnsEveryMovieWithDirectorAndReparto() {
		Long id = movieDao.insert(new Movie("Three Wishes for Cinderella", 2021, 88,
				director(otroDirectorId)));
		jdbc.update("INSERT INTO movie_actor (movie_id, actor_id) VALUES (?, ?)", id, primerActorId);
		Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM movie", Integer.class);

		List<Movie> movies = movieDao.findAll();

		assertThat(movies).hasSize(total);
		Movie inserted = movies.stream()
				.filter(movie -> id.equals(movie.getId()))
				.findFirst()
				.orElseThrow();
		assertThat(inserted.getTitle()).isEqualTo("Three Wishes for Cinderella");
		assertThat(inserted.getDirector().fullName()).isEqualTo("Frank Darabont");
		assertThat(inserted.getActors())
				.extracting(actor -> actor.fullName())
				.containsExactly("Stephen Fry");
	}

	/** findDirectorById: el director existe; un id que no existe devuelve empty. */
	@Test
	void findDirectorByIdReturnsTheDirectorAndIsEmptyWhenItDoesNotExist() {
		assertThat(movieDao.findDirectorById(directorId))
				.hasValueSatisfying(director -> {
					assertThat(director.getId()).isEqualTo(directorId);
					assertThat(director.fullName()).isEqualTo("Douglas Adams");
				});
		assertThat(movieDao.findDirectorById(999L)).isEmpty();
	}

	/**
	 * Al DAO solo le importa el id del director (solo se guarda e hidrata con él),
	 * así que el nombre y la fecha son el mismo en todos los casos de prueba.
	 */
	private Director director(Long id) {
		return new Director(id, "Evelyn", "Parker", LocalDate.of(1966, 6, 18), null);
	}

}
