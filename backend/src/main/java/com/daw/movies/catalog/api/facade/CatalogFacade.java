package com.daw.movies.catalog.api.facade;

import java.util.Optional;

/**
 * Fachada del módulo {@code Catalog} para los demás módulos (D-50).
 * <p>
 * La raíz del paquete del módulo es la única zona pública de un módulo
 * sellado de Spring Modulith: nada de {@code controller}, {@code domain} o
 * {@code persistence} cruza el borde. La fachada ofrece contratos **por lo que
 * otros módulos necesitan**, no por lo que internamente hay (skill
 * {@code monolito-modular}, §3.4): hoy eso es solo la referencia mínima a una
 * película (id y título) para que otro módulo la use por id (D-45).
 * <p>
 * El contrato son {@code record}s inmutables ({@link MovieLookup}), nunca el
 * modelo de dominio.
 */
public interface CatalogFacade {

	/** Referencia mínima a la película con ese id, o {@code empty} si no existe. */
	Optional<MovieLookup> findMovie(Long id);

}
