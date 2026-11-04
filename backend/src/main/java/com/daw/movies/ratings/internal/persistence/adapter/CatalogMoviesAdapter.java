package com.daw.movies.ratings.internal.persistence.adapter;

import java.util.Optional;

import jakarta.inject.Named;

import com.daw.movies.catalog.api.facade.CatalogFacade;
import com.daw.movies.catalog.api.facade.MovieLookup;
import com.daw.movies.ratings.internal.domain.port.AvailableMoviesPort;

/**
 * Adaptador del puerto {@link AvailableMoviesPort} sobre la fachada del
 * módulo catalog (§3.3): este paquete es el ÚNICO del módulo que importa una
 * fachada ajena. Traduce el DTO de otra facade ({@code MovieLookup}) al
 * record propio del puerto ({@code AvailableMovie}).
 */
@Named
public class CatalogMoviesAdapter implements AvailableMoviesPort {

	private final CatalogFacade catalogFacade;

	public CatalogMoviesAdapter(CatalogFacade catalogFacade) {
		this.catalogFacade = catalogFacade;
	}

	@Override
	public Optional<AvailableMovie> findMovie(Long id) {
		return catalogFacade.findMovie(id).map(CatalogMoviesAdapter::toAvailable);
	}

	private static AvailableMovie toAvailable(MovieLookup lookup) {
		return new AvailableMovie(lookup.id(), lookup.title());
	}

}
