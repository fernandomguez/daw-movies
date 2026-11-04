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
import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;
import com.daw.movies.catalog.internal.domain.service.MovieService;

@ExtendWith(MockitoExtension.class)
class UpdateMovieUseCaseTest {

	@Mock
	private MovieService movieService;

	@Mock
	private MovieRepository movieRepository;

	private Director nolan() {
		return new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
	}

	@Test
	void updateReplacesTheDataAndReturnsTheReloadedMovie() {
		Director nolan = nolan();
		when(movieService.findMovie(10L)).thenReturn(Optional.of(new Movie(10L, "Dud", 2000, 100, nolan)));
		when(movieService.findDirector(1L)).thenReturn(Optional.of(nolan));
		when(movieRepository.findById(10L))
				.thenReturn(Optional.of(new Movie(10L, "Inception", 2010, 148, nolan)));

		Movie movie = new UpdateMovieUseCase(movieService, movieRepository)
				.execute(10L, "Inception", 2010, 148, 1L);

		assertThat(movie.getTitle()).isEqualTo("Inception");
		ArgumentCaptor<Movie> updates = ArgumentCaptor.forClass(Movie.class);
		verify(movieRepository).update(org.mockito.ArgumentMatchers.eq(10L), updates.capture());
		assertThat(updates.getValue().getTitle()).isEqualTo("Inception");
		assertThat(updates.getValue().getReleaseYear()).isEqualTo(2010);
		assertThat(updates.getValue().getDuration()).isEqualTo(148);
	}

	@Test
	void updateThrowsAndDoesNotUpdateWhenMovieDoesNotExist() {
		when(movieService.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new UpdateMovieUseCase(movieService, movieRepository)
				.execute(99L, "Inception", 2010, 148, 1L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		verify(movieRepository, never()).update(any(), any());
	}

	@Test
	void updateThrowsAndDoesNotUpdateWhenDirectorDoesNotExist() {
		Director nolan = nolan();
		when(movieService.findMovie(10L)).thenReturn(Optional.of(new Movie(10L, "Dud", 2000, 100, nolan)));
		when(movieService.findDirector(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new UpdateMovieUseCase(movieService, movieRepository)
				.execute(10L, "Inception", 2010, 148, 99L))
				.isInstanceOf(DirectorNotFoundException.class)
				.hasMessage("No existe el director con id 99");

		verify(movieRepository, never()).update(any(), any());
	}

	@Test
	void updateFailsWithAnInternalErrorWhenTheUpdatedMovieCannotBeReloaded() {
		Director nolan = nolan();
		when(movieService.findMovie(10L)).thenReturn(Optional.of(new Movie(10L, "Dud", 2000, 100, nolan)));
		when(movieService.findDirector(1L)).thenReturn(Optional.of(nolan));
		when(movieRepository.findById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new UpdateMovieUseCase(movieService, movieRepository)
				.execute(10L, "Inception", 2010, 148, 1L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La película 10 no se encuentra justo después de guardarla");
	}

}
