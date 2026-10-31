package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class BirthDateTest {

	@Test
	void acceptsAPastDate() {
		LocalDate birth = LocalDate.of(1971, 5, 14);

		assertThat(new BirthDate(birth).value()).isEqualTo(birth);
	}

	@Test
	void acceptsToday() {
		// Fechas relativas: la regla usa LocalDate.now(), no una fecha fija.
		assertThat(new BirthDate(LocalDate.now()).value()).isEqualTo(LocalDate.now());
	}

	@Test
	void rejectsTomorrow() {
		assertThatThrownBy(() -> new BirthDate(LocalDate.now().plusDays(1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("futura");
	}

	@Test
	void rejectsNullDate() {
		assertThatThrownBy(() -> new BirthDate(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nacimiento");
	}

}
