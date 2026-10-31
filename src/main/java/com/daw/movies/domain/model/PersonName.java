package com.daw.movies.domain.model;

/**
 * Nombre y apellidos de una persona (director o actor).
 * <p>
 * Son dos columnas ({@code first_name} y {@code last_name}, {@code VARCHAR(100)
 * NOT NULL}, V1–V2), así que el <em>value object</em> también tiene dos partes.
 * El nombre completo no se guarda: se calcula con {@link #fullName()}.
 */
public record PersonName(String firstName, String lastName) {

	public PersonName {
		firstName = Texts.requireText(firstName, "El nombre", 100);
		lastName = Texts.requireText(lastName, "El apellido", 100);
	}

	/** Nombre completo para mostrar; no corresponde a ninguna columna. */
	public String fullName() {
		return firstName + " " + lastName;
	}

}
