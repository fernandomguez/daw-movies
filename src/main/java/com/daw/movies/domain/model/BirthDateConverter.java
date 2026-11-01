package com.daw.movies.domain.model;

import java.time.LocalDate;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link BirthDate} (&lt;-&gt; la columna {@code birth_date}, DATE
 * NOT NULL, V1–V2).
 * <p>
 * JPA ya entiende {@link LocalDate}, de modo que el conversor solo traduce
 * entre el *value object* y la fecha; al leer, el constructor vuelve a
 * comprobar que la fecha no sea futura.
 */
@Converter(autoApply = true)
public class BirthDateConverter implements AttributeConverter<BirthDate, LocalDate> {

	@Override
	public LocalDate convertToDatabaseColumn(BirthDate attribute) {
		return attribute == null ? null : attribute.value();
	}

	@Override
	public BirthDate convertToEntityAttribute(LocalDate dbData) {
		return dbData == null ? null : new BirthDate(dbData);
	}

}
