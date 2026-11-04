package com.daw.movies.catalog.api.facade.impl;

import java.util.Optional;

import jakarta.inject.Named;

import com.daw.movies.catalog.api.facade.CatalogFacade;
import com.daw.movies.catalog.api.facade.MovieLookup;
import com.daw.movies.catalog.internal.domain.service.MovieService;

/**
 * Implementación de la fachada (§3.3). Sencilla dos cosas: delegar en el
 * servicio de dominio y mapear el modelo al record del contrato. Nos toca
 * {@code @Named} (no forma parte de la interfaz con nombre: ningún módulo
 * puede importar nada de {@code impl}), sin reglas de negocio, sin
 * repositorios y sin invocar casos de uso (§3.4).
 */
@Named
public class CatalogFacadeImpl implements CatalogFacade {

	private final MovieService movieService;
	private final CatalogFacadeMapper mapper;

	public CatalogFacadeImpl(MovieService movieService, CatalogFacadeMapper mapper) {
		this.movieService = movieService;
		this.mapper = mapper;
	}

	@Override
	public Optional<MovieLookup> findMovie(Long id) {
		return movieService.findMovie(id).map(mapper::toLookup);
	}

}
