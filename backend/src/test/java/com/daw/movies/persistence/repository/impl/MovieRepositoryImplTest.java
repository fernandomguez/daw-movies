package com.daw.movies.persistence.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.domain.model.Director;
import com.daw.movies.domain.model.Movie;
import com.daw.movies.domain.repository.MovieRepository;
import com.daw.movies.persistence.dao.impl.jdbc.MovieDaoJdbc;

// Necesita MariaDB arrancado (docker compose up -d).
@SpringBootTest
/*
 * Test de integración del adaptador en espejo (PER-R09): el puerto
 * MovieRepository delega en el DAO y la BD real la contesta. Como en
 * MovieDaoJdbcTest (PER-D8 adaptado): transacción con rollback para conservar
 * el seed.sql, y el JdbcTemplate se usa de verdad, sin mocks.
 */
@Transactional
class MovieRepositoryImplTest {

	@Autowired
	private JdbcTemplate jdbc;

	/*
	 * PER-R09: el adaptador se instancia con su colaboración real; el puerto lo
	 * usa el servicio y aquí se comprueba a través de la interfaz de domain.
	 */
	private MovieRepository repository;

	private Long directorId;

	@BeforeEach
	void preparaDirector() {
		jdbc.update("""
				INSERT INTO director (first_name, last_name, birth_date, biography)
				VALUES ('Ang', 'Lee', '1954-10-23', NULL)
				""");
		directorId = jdbc.queryForObject("SELECT MAX(id) FROM director", Long.class);
		repository = new MovieRepositoryImpl(new MovieDaoJdbc(jdbc));
	}

	/** save devuelve el id generado y la fila queda en la tabla. */
	@Test
	void saveReturnsTheGeneratedIdAndPersistsTheMovie() {
		Long id = repository.save(new Movie("Crouching Tiger, Hidden Dragon", 2000, 120,
				director(directorId)));

		assertThat(id).isNotNull();
		Integer rows = jdbc.queryForObject("SELECT COUNT(*) FROM movie WHERE id = ?", Integer.class, id);
		assertThat(rows).isEqualTo(1);
	}

	/** La peli guardada se lee por el puerto con los mismos datos. */
	@Test
	void findByIdReturnsTheSavedMovieThroughThePort() {
		Long id = repository.save(new Movie("Brokeback Mountain", 2005, 134,
				director(directorId)));

		assertThat(repository.findById(id))
				.hasValueSatisfying(movie -> {
					assertThat(movie.getTitle()).isEqualTo("Brokeback Mountain");
					assertThat(movie.getReleaseYear()).isEqualTo(2005);
					assertThat(movie.getDuration()).isEqualTo(134);
					assertThat(movie.getDirector().getId()).isEqualTo(directorId);
				});
	}

	/** findDirectorById es la única operación propia de director que expone el puerto. */
	@Test
	void findDirectorByIdReturnsTheDirectorAndIsEmptyWhenItDoesNotExist() {
		assertThat(repository.findDirectorById(directorId))
				.hasValueSatisfying(director -> assertThat(director.fullName()).isEqualTo("Ang Lee"));
		assertThat(repository.findDirectorById(999L)).isEmpty();
	}

	/**
	 * update cambia la fila por el puerto y deleteById la quita.
	 */
	@Test
	void updateChangesTheRowAndDeleteByIdRemovesItThroughThePort() {
		Long id = repository.save(new Movie("Sense and Sensibility", 1995, 136,
				director(directorId)));

		repository.update(id, new Movie("Tigre y dragón", 2000, 120, director(directorId)));

		assertThat(repository.findById(id))
				.hasValueSatisfying(movie -> assertThat(movie.getTitle()).isEqualTo("Tigre y dragón"));
		repository.deleteById(id);
		assertThat(repository.findById(id)).isEmpty();
	}

	/**
	 * Al puerto solo le importa el id del director, ya cargado por el DAO.
	 */
	private Director director(Long id) {
		return new Director(id, "Ang", "Lee", LocalDate.of(1954, 10, 23), null);
	}

}
