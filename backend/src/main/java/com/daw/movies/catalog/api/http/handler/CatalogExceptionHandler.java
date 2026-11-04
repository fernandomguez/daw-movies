package com.daw.movies.catalog.api.http.handler;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.daw.movies.catalog.internal.domain.exception.DirectorNotFoundException;
import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;

/**
 * Manejador de excepciones del módulo catalog (§3.3: un
 * {@code @RestControllerAdvice} por módulo).
 * <p>
 * El dominio no sabe de códigos de estado: aquí se decide que una película o
 * un director que no existen son 404 y que el cuerpo no cumple la validación
 * es 400.
 */
@RestControllerAdvice
public class CatalogExceptionHandler {

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

	/** El cuerpo no es JSON válido o un campo tiene un tipo imposible (p. ej. un texto donde va un año). */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException ex) {
		return new ErrorResponse("El cuerpo de la petición no es un JSON válido");
	}

	/** Un parámetro de la URL no tiene el tipo esperado (p. ej. {@code /movies/abc}). */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return new ErrorResponse("El parámetro " + ex.getName() + " tiene un valor no válido");
	}

	/** La petición no declara {@code Content-Type: application/json}. */
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	public ErrorResponse handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
		return new ErrorResponse("El tipo de contenido no está soportado: use application/json");
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
