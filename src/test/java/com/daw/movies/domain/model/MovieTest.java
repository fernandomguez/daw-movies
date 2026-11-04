package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class MovieTest {

	private static final Director NOLAN = new Director(
			"Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);

	private static final Actor DICAPRIO = new Actor(
			"Leonardo", "DiCaprio", LocalDate.of(1974, 11, 11), null);

	@Test
	void keepsItsData() {
		Movie movie = inception();

		assertThat(movie.getTitle()).isEqualTo("Inception");
		assertThat(movie.getReleaseYear()).isEqualTo(2010);
		assertThat(movie.getDuration()).isEqualTo(148);
		assertThat(movie.getDirector().fullName()).isEqualTo("Christopher Nolan");
	}

	@Test
	void startsWithoutActors() {
		assertThat(inception().getActors()).isEmpty();
	}

	@Test
	void addsAnActorToTheCast() {
		Movie movie = inception();

		movie.addActor(DICAPRIO);

		assertThat(movie.getActors()).containsExactly(DICAPRIO);
	}

	@Test
	void keepsTheOrderOfTheCast() {
		Actor caine = new Actor("Michael", "Caine", LocalDate.of(1933, 3, 14), null);
		Movie movie = inception();

		movie.addActor(DICAPRIO);
		movie.addActor(caine);

		assertThat(movie.getActors()).containsExactly(DICAPRIO, caine);
	}

	@Test
	void exposesTheCastAsAnUnmodifiableList() {
		Movie movie = inception();

		assertThatThrownBy(() -> movie.getActors().add(DICAPRIO))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void rejectsNullActor() {
		assertThatThrownBy(() -> inception().addActor(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("actor");
	}

	@Test
	void rejectsNullTitle() {
		assertThatThrownBy(() -> new Movie(null, 2010, 148, NOLAN))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("título");
	}

	@Test
	void rejectsNullDirector() {
		assertThatThrownBy(() -> new Movie("Inception", 2010, 148, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("director");
	}

	private static Movie inception() {
		return new Movie("Inception", 2010, 148, NOLAN);
	}

}
