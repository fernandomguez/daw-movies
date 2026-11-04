package com.daw.movies.controller.controllers;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.daw.movies.domain.exception.DirectorNotFoundException;
import com.daw.movies.domain.exception.MovieNotFoundException;

/**
 * Traduce las excepciones del dominio a respuestas HTTP.
 * <p>
 * El dominio no sabe de códigos de estado: aquí se decide que una película o
 * un director que no existen son 404 y que el cuerpo no cumple la validación
 * es 400.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** Cuerpo mínimo de error: el mensaje de la excepción. */
	public record ErrorResponse(String message) {
	}

	@ExceptionHandler(MovieNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleMovieNotFound(MovieNotFoundException ex) {
		return new ErrorResponse(ex.getMessage());
	}

	@ExceptionHandler(DirectorNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleDirectorNotFound(DirectorNotFoundException ex) {
		return new ErrorResponse(ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + " " + error.getDefaultMessage())
				.sorted()
				.collect(Collectors.joining("; "));
		return new ErrorResponse(message);
	}

}
