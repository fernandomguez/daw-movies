package com.daw.movies.ratings.internal.domain.port;

import java.util.Optional;

/**
 * Puerto del dominio de {@code Ratings} hacia las películas del módulo
 * {@code catalog} (§3.3: contrato declarado por el consumidor, con sus
 * propios records; nunca importar DTO del proveedor en el dominio).
 * <p>
 * Lo implementa el adaptador de {@code internal.persistence}, que es el único
 * sitio del módulo que importa la fachada ajena. La película entra apenas
 * como referencia con id (D-45).
 */
public interface AvailableMoviesPort {

	/** Record propio del puerto, ajeno al DTO de la fachada ajena. */
	record AvailableMovie(Long id, String title) {
	}

	/** La película con ese id, o {@code empty} si no existe. */
	Optional<AvailableMovie> findMovie(Long id);

}
