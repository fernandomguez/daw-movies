package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class DirectorTest {

	private static final PersonName NOLAN = new PersonName("Christopher", "Nolan");
	private static final BirthDate NOLAN_BIRTH_DATE = new BirthDate(LocalDate.of(1970, 7, 30));

	@Test
	void keepsItsData() {
		Biography biography = new Biography("Director británico-estadounidense");

		Director director = new Director(NOLAN, NOLAN_BIRTH_DATE, biography);

		assertThat(director.getName()).isEqualTo(NOLAN);
		assertThat(director.getBirthDate()).isEqualTo(NOLAN_BIRTH_DATE);
		assertThat(director.getBiography()).isEqualTo(biography);
	}

	@Test
	void buildsTheFullNameFromThePersonName() {
		Director director = new Director(NOLAN, NOLAN_BIRTH_DATE, null);

		assertThat(director.fullName()).isEqualTo("Christopher Nolan");
	}

	@Test
	void acceptsNullBiography() {
		Director director = new Director(NOLAN, NOLAN_BIRTH_DATE, null);

		assertThat(director.getBiography()).isNull();
	}

	@Test
	void rejectsNullName() {
		assertThatThrownBy(() -> new Director(null, NOLAN_BIRTH_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nombre");
	}

	@Test
	void rejectsNullBirthDate() {
		assertThatThrownBy(() -> new Director(NOLAN, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("fecha de nacimiento");
	}

}
