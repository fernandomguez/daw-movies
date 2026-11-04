package com.daw.movies.catalog.api.http.dto.response;

import java.util.List;

/**
 * Representación de una película en las respuestas de la API.
 * <p>
 * El director y el reparto van como vistas de solo lectura con el id y el
 * nombre completo, que es lo único que consume el endpoint: la fecha de
 * nacimiento y la biografía de las personas no salen por esta API.
 */
public record MovieResponse(
		Long id,
		String title,
		Integer releaseYear,
		Integer duration,
		DirectorView director,
		List<ActorView> actors) {

	/** El director de la película: id y nombre completo. */
	public record DirectorView(Long id, String fullName) {
	}

	/** Un actor del reparto: id y nombre completo. */
	public record ActorView(Long id, String fullName) {
	}

}
