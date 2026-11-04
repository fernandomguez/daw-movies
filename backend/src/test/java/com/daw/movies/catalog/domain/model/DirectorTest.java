package com.daw.movies.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DirectorTest {

	private static final LocalDate NOLAN_BIRTH_DATE = LocalDate.of(1970, 7, 30);

	@Test
	void keepsItsData() {
		Director director = new Director("Christopher", "Nolan", NOLAN_BIRTH_DATE,
				"Director británico-estadounidense");

		assertThat(director.getFirstName()).isEqualTo("Christopher");
		assertThat(director.getLastName()).isEqualTo("Nolan");
		assertThat(director.getBirthDate()).isEqualTo(NOLAN_BIRTH_DATE);
		assertThat(director.getBiography()).isEqualTo("Director británico-estadounidense");
	}

	@Test
	void buildsTheFullNameFromTheNames() {
		Director director = new Director("Christopher", "Nolan", NOLAN_BIRTH_DATE, null);

		assertThat(director.fullName()).isEqualTo("Christopher Nolan");
	}

	@Test
	void acceptsNullBiography() {
		Director director = new Director("Christopher", "Nolan", NOLAN_BIRTH_DATE, null);

		assertThat(director.getBiography()).isNull();
	}

	@Test
	void rejectsNullFirstName() {
		assertThatThrownBy(() -> new Director(null, "Nolan", NOLAN_BIRTH_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nombre");
	}

	@Test
	void rejectsNullLastName() {
		assertThatThrownBy(() -> new Director("Christopher", null, NOLAN_BIRTH_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("apellido");
	}

	@Test
	void rejectsNullBirthDate() {
		assertThatThrownBy(() -> new Director("Christopher", "Nolan", null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("fecha de nacimiento");
	}

}
