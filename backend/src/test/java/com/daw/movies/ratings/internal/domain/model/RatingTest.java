package com.daw.movies.ratings.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Comprobaciones defensivas del modelo {@code Rating}: solo lo que no puede
 * faltar (D-48). El rango de la nota vive en {@code RatingRequest} y en el
 * CHECK de la tabla.
 */
class RatingTest {

	@Test
	void creationBuildsTheRatingWithoutId() {
		Rating rating = new Rating(10L, 8, "Excelente");

		assertThat(rating.getId()).isNull();
		assertThat(rating.getMovieId()).isEqualTo(10L);
		assertThat(rating.getScore()).isEqualTo(8);
		assertThat(rating.getComment()).isEqualTo("Excelente");
	}

	@Test
	void rebuildKeepsTheIdAndTheValues() {
		Rating rating = new Rating(33L, 10L, 9, null);

		assertThat(rating.getId()).isEqualTo(33L);
		assertThat(rating.getMovieId()).isEqualTo(10L);
		assertThat(rating.getScore()).isEqualTo(9);
		assertThat(rating.getComment()).isNull();
	}

	@Test
	void rejectsNullMovieId() {
		assertThatThrownBy(() -> new Rating(null, 8, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La película de la valoración es obligatoria");
	}

	@Test
	void rejectsNullScore() {
		assertThatThrownBy(() -> new Rating(10L, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La nota de la valoración es obligatoria");
	}

}
