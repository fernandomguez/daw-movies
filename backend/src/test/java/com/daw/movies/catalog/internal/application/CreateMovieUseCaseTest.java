package com.daw.movies.catalog.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.catalog.internal.domain.exception.DirectorNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;
import com.daw.movies.catalog.internal.domain.service.MovieService;

@ExtendWith(MockitoExtension.class)
class CreateMovieUseCaseTest {

	@Mock
	private MovieService movieService;

	@Mock
	private MovieRepository movieRepository;

	@Test
	void createSavesTheMovieWithItsDirectorAndReturnsTheStoredOne() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		when(movieService.findDirector(1L)).thenReturn(Optional.of(nolan));
		when(movieRepository.save(any(Movie.class))).thenReturn(10L);
		Movie stored = new Movie(10L, "Inception", 2010, 148, nolan);
		when(movieRepository.findById(10L)).thenReturn(Optional.of(stored));

		Movie movie = new CreateMovieUseCase(movieService, movieRepository)
				.execute("Inception", 2010, 148, 1L);

		assertThat(movie).isSameAs(stored);

		ArgumentCaptor<Movie> saved = ArgumentCaptor.forClass(Movie.class);
		verify(movieRepository).save(saved.capture());
		assertThat(saved.getValue().getTitle()).isEqualTo("Inception");
		assertThat(saved.getValue().getDirector().fullName()).isEqualTo("Christopher Nolan");
	}

	@Test
	void createThrowsAndDoesNotSaveWhenDirectorDoesNotExist() {
		when(movieService.findDirector(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new CreateMovieUseCase(movieService, movieRepository)
				.execute("Inception", 2010, 148, 1L))
				.isInstanceOf(DirectorNotFoundException.class)
				.hasMessage("No existe el director con id 1");

		verify(movieRepository, never()).save(any(Movie.class));
	}

	@Test
	void createFailsWithAnInternalErrorWhenTheSavedMovieCannotBeReloaded() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		when(movieService.findDirector(1L)).thenReturn(Optional.of(nolan));
		when(movieRepository.save(any(Movie.class))).thenReturn(10L);
		when(movieRepository.findById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new CreateMovieUseCase(movieService, movieRepository)
				.execute("Inception", 2010, 148, 1L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La película 10 no se encuentra justo después de guardarla");
	}

}
