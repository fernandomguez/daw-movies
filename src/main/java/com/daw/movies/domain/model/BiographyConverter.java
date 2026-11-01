package com.daw.movies.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link Biography} (&lt;-&gt; la columna {@code biography}, TEXT NULL,
 * V1–V2).
 * <p>
 * Es el único de los cinco que atraviesa {@code null}: la ausencia de biografía
 * se guarda como {@code NULL} en la columna y llega como {@code null} al campo
 * de la entidad (D-37). Un texto de la base pasa por el constructor del
 * {@code record}, que rechaza el blanco.
 */
@Converter(autoApply = true)
public class BiographyConverter implements AttributeConverter<Biography, String> {

	@Override
	public String convertToDatabaseColumn(Biography attribute) {
		return attribute == null ? null : attribute.value();
	}

	@Override
	public Biography convertToEntityAttribute(String dbData) {
		return dbData == null ? null : new Biography(dbData);
	}

}
