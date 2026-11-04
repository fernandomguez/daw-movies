package com.daw.movies.ratings.api.http.dto.response;

/**
 * Datos que salen de una valoración. Igual que en el módulo catalog, el
 * mapeo dominio → DTO es cosa del mapper, y este record no conoce Spring.
 */
public record RatingResponse(
		Long id,
		Long movieId,
		Integer score,
		String comment) {
}
