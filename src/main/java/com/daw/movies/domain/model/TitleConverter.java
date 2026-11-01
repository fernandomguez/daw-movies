package com.daw.movies.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link Title} (&lt;-&gt; la columna {@code movie.title}, VARCHAR(255)
 * NOT NULL, V3).
 * <p>
 * Un {@link AttributeConverter} solo sabe de un tipo Java y de una columna, así
 * que los *value objects* de una columna caben aquí y solo aquí. Las flechas
 * van en los dos sentidos: al escribir, el *value object* entrega su valor
 * interno (una sola columna); al leer, el texto de la columna pasa de nuevo por
 * el constructor del {@code record}, que lo valida. Así, la validación de la
 * fase 10 se ejecuta también con datos que vienen de la base.
 */
@Converter(autoApply = true)
public class TitleConverter implements AttributeConverter<Title, String> {

	@Override
	public String convertToDatabaseColumn(Title attribute) {
		return attribute == null ? null : attribute.value();
	}

	@Override
	public Title convertToEntityAttribute(String dbData) {
		return dbData == null ? null : new Title(dbData);
	}

}
