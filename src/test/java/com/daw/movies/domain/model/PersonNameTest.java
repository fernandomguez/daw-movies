package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PersonNameTest {

	@Test
	void buildsTheFullName() {
		assertThat(new PersonName("Sofia", "Coppola").fullName()).isEqualTo("Sofia Coppola");
	}

	@Test
	void trimsBothParts() {
		PersonName name = new PersonName("  Sofia ", " Coppola  ");

		assertThat(name.firstName()).isEqualTo("Sofia");
		assertThat(name.lastName()).isEqualTo("Coppola");
		assertThat(name.fullName()).isEqualTo("Sofia Coppola");
	}

	@Test
	void acceptsPartsOfExactly100Characters() {
		PersonName name = new PersonName("a".repeat(100), "b".repeat(100));

		assertThat(name.firstName()).hasSize(100);
		assertThat(name.lastName()).hasSize(100);
	}

	@Test
	void rejectsNullFirstName() {
		assertThatThrownBy(() -> new PersonName(null, "Coppola"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("nombre");
	}

	@Test
	void rejectsBlankLastName() {
		assertThatThrownBy(() -> new PersonName("Sofia", "  "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("apellido");
	}

	@Test
	void rejectsPartsLongerThan100Characters() {
		assertThatThrownBy(() -> new PersonName("a".repeat(101), "Coppola"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("100");
	}

}
