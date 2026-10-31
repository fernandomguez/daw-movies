package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TitleTest {

	@Test
	void trimsSurroundingSpaces() {
		assertThat(new Title("  Alien  ").value()).isEqualTo("Alien");
	}

	@Test
	void comparesByValue() {
		assertThat(new Title(" Alien ")).isEqualTo(new Title("Alien"));
	}

	@Test
	void rejectsNullText() {
		assertThatThrownBy(() -> new Title(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("título");
	}

	@Test
	void rejectsBlankText() {
		assertThatThrownBy(() -> new Title("   "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blanco");
	}

	@Test
	void acceptsTextOfExactly255Characters() {
		assertThat(new Title("a".repeat(255)).value()).hasSize(255);
	}

	@Test
	void rejectsTextLongerThan255Characters() {
		assertThatThrownBy(() -> new Title("a".repeat(256)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("255");
	}

}
