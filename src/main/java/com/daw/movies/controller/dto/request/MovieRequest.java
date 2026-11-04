package com.daw.movies.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de entrada de {@code POST /movies} y {@code PUT /movies/{id}}.
 * <p>
 * Aquí viven las validaciones de entrada, que antes estaban en los
 * <em>value objects</em>: título no en blanco y de hasta 255 caracteres
 * (columna {@code VARCHAR(255)}), año desde 1888 ({@code CHECK
 * chk_movie_release_year}) y duración positiva ({@code CHECK
 * chk_movie_duration_positive}). Los tipos son envolventes para que
 * {@code @NotNull} pueda distinguir la ausencia del valor.
 */
public record MovieRequest(
		@NotBlank @Size(max = 255) String title,
		@NotNull @Min(1888) Integer releaseYear,
		@NotNull @Positive Integer duration,
		@NotNull @Positive Long directorId) {

}
