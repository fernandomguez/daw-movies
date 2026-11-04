package com.daw.movies.catalog.internal.persistence.dao;

import java.util.List;
import java.util.Optional;

import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;

/**
 * Acceso a datos de la tabla {@code movie} (y de lo necesario para hidratar
 * una {@link Movie}: su director y su reparto).
 */
public interface MovieDao {

	List<Movie> findAll();

	Optional<Movie> findById(Long id);

	/** Director por id, para construir la película que se va a guardar. */
	Optional<Director> findDirectorById(Long id);

	/** Inserta la fila y devuelve el id generado por la base de datos. */
	Long insert(Movie movie);

	/** Actualiza la fila con ese id. No toca {@code movie_actor}. */
	void update(Long id, Movie movie);

	void deleteById(Long id);

}
