package com.daw.movies.ratings.domain.model;

/**
 * Valoración de una película (1–10 puntos, comentario opcional) del módulo
 * {@code Ratings}.
 * <p>
 * Referencia a la película <strong>por identificador</strong> y no por objeto:
 * la película vive en otro módulo (D-45) y llegaría imposible por objeto
 * cruzando la fachada.
 * <p>
 * Misma disciplina que en los modelos del módulo catalog (D-48): tipos
 * simples, sin setters, y el constructor solo comprueba lo que no puede
 * faltar. El rango 1–10 lo validan la entrada ({@code RatingRequest}) y el
 * esquema ({@code CHECK chk_rating_score}).
 */
public class Rating {

	private Long id;
	private Long movieId;
	private Integer score;
	private String comment;

	public Rating(Long movieId, Integer score, String comment) {
		if (movieId == null) {
			throw new IllegalArgumentException("La película de la valoración es obligatoria");
		}
		if (score == null) {
			throw new IllegalArgumentException("La nota de la valoración es obligatoria");
		}
		this.movieId = movieId;
		this.score = score;
		this.comment = comment;
	}

	/** Reconstrucción desde la base de datos: con el id. */
	public Rating(Long id, Long movieId, Integer score, String comment) {
		this(movieId, score, comment);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public Long getMovieId() {
		return movieId;
	}

	public Integer getScore() {
		return score;
	}

	/** {@code null} cuando la valoración no lleva comentario. */
	public String getComment() {
		return comment;
	}

}
