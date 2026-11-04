package com.daw.movies.catalog.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class FindMoviesUseCaseTest {

	@Mock
	private MovieRepository movieRepository;

	@Test
	void executeReturnsWhatTheRepositoryReturns() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		Movie movie = new Movie(10L, "Inception", 2010, 148, nolan);
		when(movieRepository.findAll()).thenReturn(List.of(movie));

		List<Movie> result = new FindMoviesUseCase(movieRepository).execute();

		assertThat(result).containsExactly(movie);
	}

	@Test
	void executeReturnsEmptyListWhenThereAreNoMovies() {
		when(movieRepository.findAll()).thenReturn(List.of());

		assertThat(new FindMoviesUseCase(movieRepository).execute()).isEmpty();
	}

}
