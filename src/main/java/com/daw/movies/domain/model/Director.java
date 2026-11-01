package com.daw.movies.domain.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Director de cine.
 * <p>
 * Reúne los datos de la tabla {@code director} (V1): nombre y apellidos, fecha
 * de nacimiento y biografía. La biografía es opcional y se representa con
 * {@code null}, nunca con un {@link Biography} vacío (D-37).
 * <p>
 * Es una entidad JPA ({@code @Entity}): su identidad es el {@code id} que
 * asigna la base de datos ({@code AUTO_INCREMENT} de la tabla {@code director},
 * V1), por eso no implementa todavía {@code equals}/{@code hashCode}. Las
 * anotaciones describen las columnas de la tabla; Hibernate solo las comprueba
 * contra el esquema de Flyway ({@code ddl-auto: validate}, D-34): nunca crea
 * tablas. Los campos no son {@code final} y existe un constructor vacío
 * protegido porque JPA los exige (D-29).
 */
@Entity
@Table(name = "director")
public class Director {

	/** Identificador generado por {@code AUTO_INCREMENT}; {@code null} si aún no se ha guardado. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Nombre y apellidos; ocupa las columnas {@code first_name} y {@code last_name}. */
	@Embedded
	private PersonName name;

	private BirthDate birthDate;

	/** {@code null} cuando el director no tiene biografía (D-37). */
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
