package com.daw.movies.domain.model;

import java.time.LocalDate;

/**
 * Actor o actriz.
 * <p>
 * Reúne los datos de la tabla {@code actor} (V2): nombre y apellidos, fecha de
 * nacimiento y biografía. Es estructuralmente igual que {@link Director}, pero
 * son dos conceptos distintos y dos tablas distintas (D-11).
 * <p>
 * Es una entidad: su identidad es el {@code id} que asigna la base de datos, por
 * eso no implementa todavía {@code equals}/{@code hashCode}.
 * <p>
 * Usa tipos simples; las reglas de entrada se comprueban en la capa de entrada
 * con Jakarta Validation. El constructor conserva solo las comprobaciones
 * defensivas de lo que no puede faltar.
 */
public class Actor {

	private Long id;
	private String firstName;
	private String lastName;
	private LocalDate birthDate;
	private String biography;

	public Actor(String firstName, String lastName, LocalDate birthDate, String biography) {
		if (firstName == null) {
			throw new IllegalArgumentException("El nombre del actor es obligatorio");
		}
		if (lastName == null) {
			throw new IllegalArgumentException("El apellido del actor es obligatorio");
		}
		if (birthDate == null) {
			throw new IllegalArgumentException("La fecha de nacimiento del actor es obligatoria");
		}
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthDate = birthDate;
		this.biography = biography;
	}

	/** Reconstrucción desde la base de datos: con el id. */
	public Actor(Long id, String firstName, String lastName, LocalDate birthDate, String biography) {
		this(firstName, lastName, birthDate, biography);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	/** {@code null} cuando el actor no tiene biografía (D-37). */
	public String getBiography() {
		return biography;
	}

	/** Nombre completo para mostrar; no corresponde a ninguna columna. */
	public String fullName() {
		return firstName + " " + lastName;
	}

}
