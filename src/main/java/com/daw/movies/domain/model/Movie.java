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
 * falta (fase 14).
 * <p>
 * Es una entidad: su identidad es el {@code id} que asigna la base de datos, por
 * eso no implementa todavía {@code equals}/{@code hashCode} (fase 11/14). La
 * fase 11 añadirá las anotaciones JPA (D-29).
 */
public class Movie {

	private Long id;
	private Title title;
	private ReleaseYear releaseYear;
	private DurationMinutes duration;
	private Director director;
	private List<Actor> actors = new ArrayList<>();

	/** Solo para que JPA rellene los campos por reflexión (fase 11). */
	protected Movie() {
	}

	public Movie(Title title, ReleaseYear releaseYear, DurationMinutes duration, Director director) {
		if (title == null) {
			throw new IllegalArgumentException("El título es obligatorio");
		}
		if (releaseYear == null) {
			throw new IllegalArgumentException("El año de estreno es obligatorio");
		}
		if (duration == null) {
			throw new IllegalArgumentException("La duración es obligatoria");
		}
		if (director == null) {
			throw new IllegalArgumentException("El director es obligatorio");
		}
		this.title = title;
		this.releaseYear = releaseYear;
		this.duration = duration;
		this.director = director;
	}

	public Long getId() {
		return id;
	}

	public Title getTitle() {
		return title;
	}

	public ReleaseYear getReleaseYear() {
		return releaseYear;
	}

	public DurationMinutes getDuration() {
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
