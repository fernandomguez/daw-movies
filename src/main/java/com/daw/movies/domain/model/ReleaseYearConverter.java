package com.daw.movies.domain.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link ReleaseYear} (&lt;-&gt; la columna {@code movie.release_year},
 * SMALLINT, con el {@code CHECK chk_movie_release_year >= 1888}, V3).
 * <p>
 * El tipo de la base es numérico, no de texto: el {@code Integer} de la columna
 * entra por el constructor del {@code record}, que aplica el mínimo de 1888.
 */
@Converter(autoApply = true)
public class ReleaseYearConverter implements AttributeConverter<ReleaseYear, Integer> {

	@Override
	public Integer convertToDatabaseColumn(ReleaseYear attribute) {
		return attribute == null ? null : attribute.value();
	}

	@Override
	public ReleaseYear convertToEntityAttribute(Integer dbData) {
		return dbData == null ? null : new ReleaseYear(dbData);
	}

}
