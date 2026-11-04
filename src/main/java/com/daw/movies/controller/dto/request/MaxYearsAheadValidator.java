package com.daw.movies.controller.dto.request;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Comprueba {@link MaxYearsAhead} comparando con el año actual del sistema. */
public class MaxYearsAheadValidator implements ConstraintValidator<MaxYearsAhead, Integer> {

	private int yearsAhead;

	@Override
	public void initialize(MaxYearsAhead annotation) {
		this.yearsAhead = annotation.value();
	}

	@Override
	public boolean isValid(Integer year, ConstraintValidatorContext context) {
		return year == null || year <= Year.now().getValue() + yearsAhead;
	}

}
