package com.daw.movies.controller.dto.request;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * El año no puede superar el año actual en más de {@link #value()} años.
 * <p>
 * {@code @Max} solo admite un número fijo, y aquí el tope se mueve cada año
 * (con {@code value = 10}: en 2026 el máximo es 2036, en 2027 es 2037...). Por
 * eso hay una anotación propia con su {@link MaxYearsAheadValidator}.
 * <p>
 * Un valor {@code null} se considera válido: lo rechaza {@code @NotNull}.
 */
@Documented
@Constraint(validatedBy = MaxYearsAheadValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxYearsAhead {

	/** Años por delante del actual que se admiten. */
	int value();

	String message() default "no puede ser más de {value} años posterior al año actual";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
