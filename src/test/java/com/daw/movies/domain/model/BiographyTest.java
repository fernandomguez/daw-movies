package com.daw.movies.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BiographyTest {

	@Test
	void keepsTheText() {
		assertThat(new Biography("Directora estadounidense").value())
				.isEqualTo("Directora estadounidense");
	}

	@Test
	void trimsSurroundingSpaces() {
		assertThat(new Biography("  Directora  ").value()).isEqualTo("Directora");
	}

	@Test
	void acceptsLongText() {
		// La columna es TEXT: no se inventa un máximo.
		assertThat(new Biography("a".repeat(5000)).value()).hasSize(5000);
	}

	@Test
	void rejectsNullText() {
		assertThatThrownBy(() -> new Biography(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("biografía");
	}

	@Test
	void rejectsBlankText() {
		// "Sin biografía" se representa con null en la entidad, no con un texto vacío (D-37).
		assertThatThrownBy(() -> new Biography("   "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("blanco");
	}

}
