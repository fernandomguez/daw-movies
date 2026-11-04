package com.daw.movies.ratings.controller.handler;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.daw.movies.ratings.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.domain.exception.RatingNotFoundException;

/**
 * Traduce las excepciones del dominio de {@code Ratings} a respuestas HTTP.
 * <p>
 * Cada módulo se trae su propio {@code @RestControllerAdvice} (así lo pide la
 * arquitectura objetivo): el de catalog no es visible desde aquí porque el
 * módulo está sellado.
 */
@RestControllerAdvice
public class RatingExceptionHandler {

	/** Cuerpo mínimo de error: el mensaje de la excepción (mismo estilo que catalog). */
	public record ErrorResponse(String message) {
	}

	@ExceptionHandler(MovieNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleMovieNotFound(MovieNotFoundException ex) {
		return new ErrorResponse(ex.getMessage());
	}

	@ExceptionHandler(RatingNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleRatingNotFound(RatingNotFoundException ex) {
		return new ErrorResponse(ex.getMessage());
	}

	/** El cuerpo no es JSON válido o un campo tiene un tipo imposible. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException ex) {
		return new ErrorResponse("El cuerpo de la petición no es un JSON válido");
	}

	/** Un parámetro de la URL no tiene el tipo esperado (p. ej. {@code /ratings/abc}). */
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
