package com.daw.movies.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Película.
 * <p>
 * Agrupa los valores de la tabla {@code movie} (V3) y sus dos relaciones:
 * <ul>
 * <li>un director ({@code director_id}): muchas películas pueden tener el mismo
 * director;</li>
 * <li>un reparto de actores (tabla intermedia {@code movie_actor}, V4).</li>
 * </ul>
 * Las dos relaciones son <strong>unidireccionales</strong>: la película conoce a
 * su director y a sus actores; ni el director ni el actor conocen sus películas.
 * Se evitan así referencias circulares al serializar y consultas que no hacen
 * falta.
 * <p>
 * Es una entidad: su identidad es el {@code id} que asigna la base de datos, por
 * eso no implementa todavía {@code equals}/{@code hashCode}.
 * <p>
 * Usa tipos simples; las reglas de entrada (título no en blanco, año desde 1888,
 * duración positiva) se comprueban en la capa de entrada, {@code MovieRequest}
 * con Jakarta Validation. El constructor conserva solo las comprobaciones
 * defensivas de lo que no puede faltar.
 */
public class Movie {

	private Long id;
	private String title;
	private int releaseYear;
	private int duration;
	private Director director;
	private List<Actor> actors = new ArrayList<>();

	public Movie(String title, int releaseYear, int duration, Director director) {
		if (title == null) {
			throw new IllegalArgumentException("El título es obligatorio");
		}
		if (director == null) {
			throw new IllegalArgumentException("El director es obligatorio");
		}
		this.title = title;
		this.releaseYear = releaseYear;
		this.duration = duration;
		this.director = director;
	}

	/** Reconstrucción desde la base de datos: con el id y el reparto ya cargado. */
	public Movie(Long id, String title, int releaseYear, int duration, Director director) {
		this(title, releaseYear, duration, director);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public int getReleaseYear() {
		return releaseYear;
	}

	public int getDuration() {
		return duration;
	}

	public Director getDirector() {
		return director;
	}

	/**
	 * Reparto de la película, en el orden en que se añadieron.
	 * <p>
	 * Devuelve una copia inmutable: para cambiar el reparto se usa
	 * {@link #addActor(Actor)}, que es el único camino que controla el dominio.
	 */
	public List<Actor> getActors() {
		return List.copyOf(actors);
	}

	/**
	 * Añade un actor al reparto.
	 * <p>
	 * No comprueba duplicados: la pareja película-actor es única por la clave
	 * primaria compuesta de {@code movie_actor} (D-14), y {@link Actor} todavía no
	 * tiene igualdad por valor, así que comparar objetos aquí daría falsos
	 * negativos.
	 */
	public void addActor(Actor actor) {
		if (actor == null) {
			throw new IllegalArgumentException("El actor no puede ser nulo");
		}
		actors.add(actor);
	}

}
