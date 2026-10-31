package com.daw.movies.domain.model;

/**
 * Director de cine.
 * <p>
 * Reúne los datos de la tabla {@code director} (V1): nombre y apellidos, fecha
 * de nacimiento y biografía. La biografía es opcional y se representa con
 * {@code null}, nunca con un {@link Biography} vacío (D-37).
 * <p>
 * Es una entidad: su identidad es el {@code id} que asigna la base de datos, por
 * eso no implementa todavía {@code equals}/{@code hashCode}. La fase 11 le
 * añadirá las anotaciones JPA; de ahí el constructor vacío y que los campos no
 * sean {@code final} (D-29).
 */
public class Director {

	private Long id;
	private PersonName name;
	private BirthDate birthDate;
	private Biography biography;

	/** Solo para que JPA rellene los campos por reflexión (fase 11). */
	protected Director() {
	}

	public Director(PersonName name, BirthDate birthDate, Biography biography) {
		if (name == null) {
			throw new IllegalArgumentException("El nombre del director es obligatorio");
		}
		if (birthDate == null) {
			throw new IllegalArgumentException("La fecha de nacimiento del director es obligatoria");
		}
		this.name = name;
		this.birthDate = birthDate;
		this.biography = biography;
	}

	public Long getId() {
		return id;
	}

	public PersonName getName() {
		return name;
	}

	public BirthDate getBirthDate() {
		return birthDate;
	}

	/** {@code null} cuando el director no tiene biografía (D-37). */
	public Biography getBiography() {
		return biography;
	}

	/** Nombre completo para mostrar; delega en {@link PersonName#fullName()}. */
	public String fullName() {
		return name.fullName();
	}

}
