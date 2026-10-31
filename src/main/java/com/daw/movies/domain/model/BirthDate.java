package com.daw.movies.domain.model;

import java.time.LocalDate;

/**
 * Fecha de nacimiento de una persona.
 * <p>
 * La columna es {@code birth_date DATE NOT NULL} (V1–V2). La única regla que
 * no está en el esquema es que no sea futura: una fecha de nacimiento futura
 * es imposible.
 */
public record BirthDate(LocalDate value) {

	public BirthDate {
		if (value == null) {
			throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula");
		}
		if (value.isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
		}
	}

}
