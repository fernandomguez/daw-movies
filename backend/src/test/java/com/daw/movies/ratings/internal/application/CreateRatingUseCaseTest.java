package com.daw.movies.ratings.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.ratings.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.domain.port.AvailableMoviesPort;
import com.daw.movies.ratings.internal.domain.repository.RatingRepository;

// Tests unitarios del caso de uso: el módulo catalog entra simulado por su
// PUERTO (no por la fachada: esa traducción la cubre test del adapter) y la
// persistencia por su puerto (PER-R09 deja las BD reales para el DAO).
@ExtendWith(MockitoExtension.class)
class CreateRatingUseCaseTest {

	@Mock
	private AvailableMoviesPort availableMovies;

	@Mock
	private RatingRepository ratingRepository;

	@Test
	void createChecksTheMovieThroughThePortAndSaves() {
		when(availableMovies.findMovie(10L))
				.thenReturn(Optional.of(new AvailableMoviesPort.AvailableMovie(10L, "Inception")));
		when(ratingRepository.save(any(Rating.class))).thenReturn(33L);
		Rating stored = new Rating(33L, 10L, 8, "Excelente");
		when(ratingRepository.findById(33L)).thenReturn(Optional.of(stored));

		Rating created = new CreateRatingUseCase(availableMovies, ratingRepository)
				.execute(10L, 8, "Excelente");

		assertThat(created).isSameAs(stored);

		ArgumentCaptor<Rating> saved = ArgumentCaptor.forClass(Rating.class);
		verify(ratingRepository).save(saved.capture());
		assertThat(saved.getValue().getMovieId()).isEqualTo(10L);
		assertThat(saved.getValue().getScore()).isEqualTo(8);
	}

	@Test
	void createThrowsMovieNotFoundAndDoesNotSaveWhenTheMovieDoesNotExist() {
		when(availableMovies.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new CreateRatingUseCase(availableMovies, ratingRepository)
				.execute(99L, 8, null))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		verify(ratingRepository, never()).save(any(Rating.class));
	}

	@Test
	void createFailsWithAnInternalErrorWhenTheSavedRatingCannotBeReloaded() {
		when(availableMovies.findMovie(10L))
				.thenReturn(Optional.of(new AvailableMoviesPort.AvailableMovie(10L, "Inception")));
		when(ratingRepository.save(any(Rating.class))).thenReturn(33L);
		when(ratingRepository.findById(33L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new CreateRatingUseCase(availableMovies, ratingRepository)
				.execute(10L, 8, null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La valoración 33 no se encuentra justo después de guardarla");
	}

}
