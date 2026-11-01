package com.daw.movies.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link DurationMinutes} (&lt;-&gt; la columna
 * {@code movie.duration_minutes}, SMALLINT, con el
 * {@code CHECK chk_movie_duration_positive > 0}, V3).
 */
@Converter(autoApply = true)
public class DurationMinutesConverter implements AttributeConverter<DurationMinutes, Integer> {

	@Override
	public Integer convertToDatabaseColumn(DurationMinutes attribute) {
		return attribute == null ? null : attribute.value();
	}

	@Override
	public DurationMinutes convertToEntityAttribute(Integer dbData) {
		return dbData == null ? null : new DurationMinutes(dbData);
	}

}
