package com.daw.movies.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ActorTest {

	private static final LocalDate DICAPRIO_BIRTH_DATE = LocalDate.of(1974, 11, 11);

	@Test
	void keepsItsData() {
		Actor actor = new Actor("Leonardo", "DiCaprio", DICAPRIO_BIRTH_DATE, "Actor estadounidense");

		assertThat(actor.getFirstName()).isEqualTo("Leonardo");
		assertThat(actor.getLastName()).isEqualTo("DiCaprio");
		assertThat(actor.getBirthDate()).isEqualTo(DICAPRIO_BIRTH_DATE);
		assertThat(actor.getBiography()).isEqualTo("Actor estadounidense");
	}

	@Test
	void buildsTheFullNameFromTheNames() {
		Actor actor = new Actor("Leonardo", "DiCaprio", DICAPRIO_BIRTH_DATE, null);

		assertThat(actor.fullName()).isEqualTo("Leonardo DiCaprio");
	}

	@Test
	void acceptsNullBiography() {
		Actor actor = new Actor("Leonardo", "DiCaprio", DICAPRIO_BIRTH_DATE, null);

		assertThat(actor.getBiography()).isNull();
	}

	@Test
	void rejectsNullFirstName() {
		assertThatThrownBy(() -> new Actor(null, "DiCaprio", DICAPRIO_BIRTH_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nombre");
	}

	@Test
	void rejectsNullLastName() {
		assertThatThrownBy(() -> new Actor("Leonardo", null, DICAPRIO_BIRTH_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("apellido");
	}

	@Test
	void rejectsNullBirthDate() {
		assertThatThrownBy(() -> new Actor("Leonardo", "DiCaprio", null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("fecha de nacimiento");
	}

}
