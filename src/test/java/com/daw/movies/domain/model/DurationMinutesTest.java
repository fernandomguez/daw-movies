package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DurationMinutesTest {

	@Test
	void acceptsOneMinute() {
		assertThat(new DurationMinutes(1).value()).isEqualTo(1);
	}

	@Test
	void acceptsALongFilm() {
		// Sin tope superior: el esquema no lo tiene.
		assertThat(new DurationMinutes(600).value()).isEqualTo(600);
	}

	@Test
	void rejectsZeroMinutes() {
		assertThatThrownBy(() -> new DurationMinutes(0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("cero");
	}

	@Test
	void rejectsNegativeMinutes() {
		assertThatThrownBy(() -> new DurationMinutes(-5))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("cero");
	}

}
