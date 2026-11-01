package com.daw.movies.domain.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
 * Es una entidad JPA ({@code @Entity}): su identidad es el {@code id} que
 * asigna la base de datos ({@code AUTO_INCREMENT}, V3), por eso no implementa
 * todavía {@code equals}/{@code hashCode} (fase 11/14). Los tres campos con
 * <em>value object</em> no necesitan {@code @Convert}: los converters del 11.3
 * se aplican solos ({@code autoApply}). El director es la clave foránea en JPA
 * (paso 11.4); el reparto es la relación N:M sobre la tabla intermedia
 * {@code movie_actor} (paso 11.5).
 */
@Entity
@Table(name = "movie")
public class Movie {

	/** Identificador generado por {@code AUTO_INCREMENT}; {@code null} si aún no se ha guardado. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Title title;

	private ReleaseYear releaseYear;

	/**
	 * Duración en minutos. La columna se llama {@code duration_minutes}, una
	 * palabra de más a la que la estrategia por defecto no puede llegar
	 * (`duration` → `duration`): se declara con {@code @Column}. Comprobado con
	 * el error real del validate en el 11.4.
	 */
	@Column(name = "duration_minutes")
	private DurationMinutes duration;

	/**
	 * Director de la película: la clave foránea {@code movie.director_id} en JPA.
	 * <p>
	 * {@code @ManyToOne} es la cardinalidad (muchas películas → un director) y
	 * {@code @JoinColumn} apunta la columna. La relación vive en este lado ("el
	 * lado muchos"): esta entidad es la <strong>dueña</strong> de la clave
	 * foránea, como lo es la tabla {@code movie} en el esquema. Es unidireccional
	 * (fase 14): {@code Director} no tiene lista de películas.
	 * <p>
	 * {@code FetchType.LAZY} (paso 11.6, D-49): el {@code @ManyToOne} carga la
	 * relación ansiosa por defecto; se declara perezosa porque una consulta de
	 * películas no debería traer el director si el caso de uso no lo pide. Al
	 * leer, Hibernate entrega un <em>proxy</em> que consulta la base solo la
	 * primera vez que se toca el objeto.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "director_id", nullable = false)
	private Director director;

	/**
	 * Reparto de la película (tabla intermedia {@code movie_actor}, V4).
	 * <p>
	 * La relación N:M en JPA: {@code @ManyToMany} declara la cardinalidad y
	 * {@code @JoinTable} apunta a la tabla intermedia que ya existe en el
	 * esquema: {@code movie_id} es la columna propia (la película actual) y
	 * {@code actor_id} la columna de la otra entidad. Es unidireccional (fase
	 * 14): {@code Actor} no tiene lista de películas, y solo este lado lee y
	 * escribe la relación. Por defecto se carga perezosa ({@code LAZY}); se
	 * decide de forma explícita en el 11.6.
	 */
	@ManyToMany
	@JoinTable(
			name = "movie_actor",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "actor_id")
	)
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
