package com.daw.movies.catalog.domain.repository;

import java.util.List;
import java.util.Optional;

import com.daw.movies.catalog.domain.model.Director;
import com.daw.movies.catalog.domain.model.Movie;

/**
 * Puerto del dominio para la persistencia de películas (DIP).
 * <p>
 * El dominio solo ve esta interfaz; la implementación (JDBC) vive en
 * {@code com.daw.movies.catalog.persistence}.
 */
public interface MovieRepository {

	List<Movie> findAll();

	Optional<Movie> findById(Long id);

	/**
	 * Director por id. Lo necesita el servicio para construir la {@link Movie}
	 * con su director cargado antes de guardarla.
	 */
	Optional<Director> findDirectorById(Long id);

	/** Inserta una película nueva y devuelve el id que genera la base de datos. */
	Long save(Movie movie);

	/**
	 * Actualiza los datos de la fila de la película con ese id. No toca el
	 * reparto: no hay aún endpoints que lo gestionen.
	 */
	void update(Long id, Movie movie);

	void deleteById(Long id);

}
