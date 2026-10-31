package com.daw.movies.domain.model;

/**
 * Actor o actriz.
 * <p>
 * Reúne los datos de la tabla {@code actor} (V2): nombre y apellidos, fecha de
 * nacimiento y biografía. Es estructuralmente igual que {@link Director}, pero
 * son dos conceptos distintos y dos tablas distintas (D-11); unificarlos en una
 * jerarquía obligaría a JPA a elegir estrategia de herencia en la fase 11.
 * <p>
 * Es una entidad: su identidad es el {@code id} que asigna la base de datos, por
 * eso no implementa todavía {@code equals}/{@code hashCode}. La fase 11 le
 * añadirá las anotaciones JPA (D-29).
 */
public class Actor {

	private Long id;
	private PersonName name;
	private BirthDate birthDate;
	private Biography biography;

	/** Solo para que JPA rellene los campos por reflexión (fase 11). */
	protected Actor() {
	}

	public Actor(PersonName name, BirthDate birthDate, Biography biography) {
		if (name == null) {
			throw new IllegalArgumentException("El nombre del actor es obligatorio");
		}
		if (birthDate == null) {
			throw new IllegalArgumentException("La fecha de nacimiento del actor es obligatoria");
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

	/** {@code null} cuando el actor no tiene biografía (D-37). */
	public Biography getBiography() {
		return biography;
	}

	/** Nombre completo para mostrar; delega en {@link PersonName#fullName()}. */
	public String fullName() {
		return name.fullName();
	}

}
