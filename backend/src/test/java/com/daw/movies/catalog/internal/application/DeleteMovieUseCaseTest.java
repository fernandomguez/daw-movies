package com.daw.movies.catalog.internal.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;
import com.daw.movies.catalog.internal.domain.service.MovieService;

@ExtendWith(MockitoExtension.class)
class DeleteMovieUseCaseTest {

	@Mock
	private MovieService movieService;

	@Mock
	private MovieRepository movieRepository;

	@Test
	void deleteRemovesAnExistingMovie() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		when(movieService.findMovie(10L)).thenReturn(Optional.of(new Movie(10L, "Inception", 2010, 148, nolan)));

		new DeleteMovieUseCase(movieService, movieRepository).execute(10L);

		verify(movieRepository).deleteById(10L);
	}

	@Test
	void deleteThrowsAndDoesNotDeleteWhenMovieDoesNotExist() {
		when(movieService.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new DeleteMovieUseCase(movieService, movieRepository).execute(99L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		verify(movieRepository, never()).deleteById(any());
	}

}
