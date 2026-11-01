package com.daw.movies.domain.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Actor o actriz.
 * <p>
 * Reúne los datos de la tabla {@code actor} (V2): nombre y apellidos, fecha de
 * nacimiento y biografía. Es estructuralmente igual que {@link Director}, pero
 * son dos conceptos distintos y dos tablas distintas (D-11); unificarlos en una
 * jerarquía obligaría a JPA a elegir estrategia de herencia en la fase 11.
 * <p>
 * Es una entidad JPA ({@code @Entity}): su identidad es el {@code id} que
 * asigna la base de datos ({@code AUTO_INCREMENT} de la tabla {@code actor},
 * V2), por eso no implementa todavía {@code equals}/{@code hashCode}. Es
 * estructuralmente igual que {@link Director}, pero son dos conceptos distintos
 * y dos tablas distintas (D-11); unificarlos en una jerarquía obligaría a JPA a
 * elegir estrategia de herencia (D-29). El mapeo es idéntico al de
 * {@code director}: todas las anotaciones se repiten porque cada clase
 * describe su propia tabla.
 */
@Entity
@Table(name = "actor")
public class Actor {

	/** Identificador generado por {@code AUTO_INCREMENT}; {@code null} si aún no se ha guardado. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Nombre y apellidos; ocupa las columnas {@code first_name} y {@code last_name}. */
	@Embedded
	private PersonName name;

	private BirthDate birthDate;

	/** {@code null} cuando el actor no tiene biografía (D-37). */
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
