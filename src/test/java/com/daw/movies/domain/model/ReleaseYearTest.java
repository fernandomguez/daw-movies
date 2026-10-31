package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ReleaseYearTest {

	@Test
	void acceptsTheFirstFilmYear() {
		assertThat(new ReleaseYear(1888).value()).isEqualTo(1888);
	}

	@Test
	void acceptsAFutureReleaseYear() {
		// El esquema solo pone el mínimo: una película anunciada es un dato legítimo.
		assertThat(new ReleaseYear(2100).value()).isEqualTo(2100);
	}

	@Test
	void rejectsYearsBefore1888() {
		assertThatThrownBy(() -> new ReleaseYear(1887))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("1888");
	}

}
