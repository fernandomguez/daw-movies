package com.daw.movies.domain.model;

/**
 * Comprobaciones de texto compartidas por los <em>value objects</em> del dominio.
 * <p>
 * Existe para no repetir en cada tipo el mismo trío de reglas: no nulo, no vacío
 * tras {@code trim} y longitud máxima. Es de paquete: no forma parte del modelo
 * que se usa fuera de {@code com.daw.movies.domain.model}.
 */
final class Texts {

	private Texts() {
	}

	/** Texto obligatorio, sin máximo de longitud (columnas {@code TEXT}). */
	static String requireText(String value, String label) {
		if (value == null) {
			throw new IllegalArgumentException(label + " no puede ser nulo");
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException(label + " no puede estar en blanco");
		}
		return trimmed;
	}

	/** Texto obligatorio con la longitud máxima de la columna ({@code VARCHAR}). */
	static String requireText(String value, String label, int maxLength) {
		String trimmed = requireText(value, label);
		if (trimmed.length() > maxLength) {
			throw new IllegalArgumentException(label + " no puede superar " + maxLength + " caracteres");
		}
		return trimmed;
	}

}
