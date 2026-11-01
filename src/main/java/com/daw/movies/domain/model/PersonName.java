package com.daw.movies.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Nombre y apellidos de una persona (director o actor).
 * <p>
 * Son dos columnas ({@code first_name} y {@code last_name}, {@code VARCHAR(100)
 * NOT NULL}, V1–V2), así que el <em>value object</em> también tiene dos partes
 * y no cabe en un {@code AttributeConverter}: esa conversión va de un tipo Java
 * a una columna y vuelta. La solución es el <em>embeddable</em>: el
 * {@code record} se anida dentro de la entidad y cada componente se mapea a su
 * columna (decidido con una prueba en el 11.3, no por suposición).
 * <p>
 * Hibernate 7 lo instancia por su constructor canónico, de modo que la
 * validación del {@code record} se ejecuta también al leer de la base. El
 * nombre completo no se guarda: se calcula con {@link #fullName()}.
 */
@Embeddable
public record PersonName(
		@Column(name = "first_name", nullable = false, length = 100) String firstName,
		@Column(name = "last_name", nullable = false, length = 100) String lastName) {

	public PersonName {
		firstName = Texts.requireText(firstName, "El nombre", 100);
		lastName = Texts.requireText(lastName, "El apellido", 100);
	}

	/** Nombre completo para mostrar; no corresponde a ninguna columna. */
	public String fullName() {
		return firstName + " " + lastName;
	}

}
