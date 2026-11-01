package com.daw.movies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.daw.movies.domain.model.Actor;
import com.daw.movies.domain.model.Director;
import com.daw.movies.domain.model.Movie;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Paso 11.3: las entidades simples (Director y Actor) están mapeadas y el
 * mapeo encaja con las tablas de Flyway.
 * <p>
 * Lee filas reales del {@code seed.sql} a través del {@link EntityManager}
 * (JPA puro, sin repositorios: los llegan en la fase 12). Dos comprobaciones:
 * <ul>
 * <li>el nombre se recupera como {@link com.daw.movies.domain.model.PersonName}
 * completo, lo que demuestra que Hibernate entra por el constructor canónico
 * del {@code record} (un {@code record} no tiene otro camino);</li>
 * <li>una columna {@code NULL} llega como {@code null}, no como texto vacío
 * (los actores del seed no tienen biografía, D-37).</li>
 * </ul>
 */
@SpringBootTest
@Transactional
class EntityMappingTests {

	@PersistenceContext
	private EntityManager em;

	@Test
	void mapsTheSixSeedDirectorsWithTheirNameRebuilt() {
		List<Director> directors = em
				.createQuery("select d from Director d order by d.id", Director.class)
				.getResultList();

		assertEquals(6, directors.size());

		Director nolan = em.find(Director.class, 1L);
		assertEquals("Christopher", nolan.getName().firstName());
		assertEquals("Nolan", nolan.getName().lastName());
		assertEquals("Christopher Nolan", nolan.fullName());
		assertEquals(LocalDate.of(1970, 7, 30), nolan.getBirthDate().value());
		assertTrue(nolan.getBiography() != null && !nolan.getBiography().value().isBlank());
	}

	@Test
	void mapsActorBiographyNullWhenTheColumnIsEmpty() {
		Actor dicaprio = em.find(Actor.class, 1L);

		assertEquals("Leonardo", dicaprio.getName().firstName());
		assertEquals("DiCaprio", dicaprio.getName().lastName());
		assertNull(dicaprio.getBiography());
	}

	@Test
	void mapsTheSeedMoviesAndTheirDirector() {
		List<Movie> movies = em
				.createQuery("select m from Movie m order by m.id", Movie.class)
				.getResultList();

		assertEquals(10, movies.size());

		Movie inception = em.find(Movie.class, 3L);
		assertEquals("Inception", inception.getTitle().value());
		assertEquals(2010, inception.getReleaseYear().value());
		assertEquals(148, inception.getDuration().value());
		assertEquals("Nolan", inception.getDirector().getName().lastName());
	}

	@Test
	void mapsTheCastOfInceptionThroughTheJoinTable() {
		Movie inception = em.find(Movie.class, 3L);

		List<String> fullNames = inception.getActors().stream()
				.map(Actor::fullName)
				.toList();

		assertEquals(2, fullNames.size());
		assertTrue(fullNames.contains("Leonardo DiCaprio"));
		assertTrue(fullNames.contains("Michael Caine"));
	}

	/**
	 * Paso 11.6: las relaciones se cargan perezosas. Con fetch LAZY, Hibernate
	 * entrega para el director un "proxy" (sustituto que va a la base de datos
	 * solo si se toca), cuya clase no puede ser la original: es una subclase
	 * generada en tiempo de ejecución. Con EAGER, en cambio, el objeto ya está
	 * cargado y sí es de la clase real.
	 */
	@Test
	void loadsTheDirectorLazily() {
		Movie inception = em.find(Movie.class, 3L);

		assertNotEquals(Director.class, inception.getDirector().getClass());
	}

}
