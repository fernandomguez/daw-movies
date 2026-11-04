package com.daw.movies.catalog.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.service.MovieService;

@ExtendWith(MockitoExtension.class)
class FindMovieByIdUseCaseTest {

	@Mock
	private MovieService movieService;

	private Movie inception() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		return new Movie(10L, "Inception", 2010, 148, nolan);
	}

	@Test
	void executeReturnsTheMovie() {
		when(movieService.findMovie(10L)).thenReturn(Optional.of(inception()));

		Movie movie = new FindMovieByIdUseCase(movieService).execute(10L);

		assertThat(movie.getTitle()).isEqualTo("Inception");
	}

	@Test
	void executeThrowsWhenTheMovieDoesNotExist() {
		when(movieService.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new FindMovieByIdUseCase(movieService).execute(99L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");
	}

}
