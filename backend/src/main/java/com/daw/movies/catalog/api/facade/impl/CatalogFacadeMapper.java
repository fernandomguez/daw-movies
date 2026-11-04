package com.daw.movies.catalog.api.facade.impl;

import com.daw.movies.catalog.api.facade.MovieLookup;
import com.daw.movies.catalog.internal.domain.model.Movie;

import jakarta.inject.Named;

/**
 * Mapper de la fachada (§3.3): dominio ↔ DTO del contrato. A mano mientras
 * MapStruct entra con la migración JPA: para un record de dos campos, pronto
 * es genérico. Bean de la fachada impl, cableado por escaneo.
 */
@Named
public class CatalogFacadeMapper {

	public MovieLookup toLookup(Movie movie) {
		return new MovieLookup(movie.getId(), movie.getTitle());
	}

}
