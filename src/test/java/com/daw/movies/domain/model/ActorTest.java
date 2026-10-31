package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ActorTest {

	private static final PersonName DICAPRIO = new PersonName("Leonardo", "DiCaprio");
	private static final BirthDate DICAPRIO_BIRTH_DATE = new BirthDate(LocalDate.of(1974, 11, 11));

	@Test
	void keepsItsData() {
		Biography biography = new Biography("Actor estadounidense");

		Actor actor = new Actor(DICAPRIO, DICAPRIO_BIRTH_DATE, biography);

		assertThat(actor.getName()).isEqualTo(DICAPRIO);
		assertThat(actor.getBirthDate()).isEqualTo(DICAPRIO_BIRTH_DATE);
		assertThat(actor.getBiography()).isEqualTo(biography);
	}

	@Test
	void buildsTheFullNameFromThePersonName() {
		Actor actor = new Actor(DICAPRIO, DICAPRIO_BIRTH_DATE, null);

		assertThat(actor.fullName()).isEqualTo("Leonardo DiCaprio");
	}

	@Test
	void acceptsNullBiography() {
		Actor actor = new Actor(DICAPRIO, DICAPRIO_BIRTH_DATE, null);

		assertThat(actor.getBiography()).isNull();
	}

	@Test
	void rejectsNullName() {
		assertThatThrownBy(() -> new Actor(null, DICAPRIO_BIRTH_DATE, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nombre");
	}

	@Test
	void rejectsNullBirthDate() {
		assertThatThrownBy(() -> new Actor(DICAPRIO, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("fecha de nacimiento");
	}

}
