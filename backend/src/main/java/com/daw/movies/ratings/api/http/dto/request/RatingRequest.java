package com.daw.movies.ratings.api.http.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de entrada de {@code POST /movies/{movieId}/ratings}.
 * <p>
 * Aquí viven las validaciones de entrada del módulo (D-48): nota entre 1 y 10
 * ({@code CHECK chk_rating_score}) y comentario de hasta 500 caracteres
 * (columna {@code VARCHAR(500)}). El comentario es opcional.
 */
public record RatingRequest(
		@NotNull @Min(1) @Max(10) Integer score,
		@Size(max = 500) String comment) {
}
