package com.daw.movies.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.domain.exception.DirectorNotFoundException;
import com.daw.movies.domain.exception.MovieNotFoundException;
import com.daw.movies.domain.model.Director;
import com.daw.movies.domain.model.Movie;
import com.daw.movies.domain.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTest {

	@Mock
	private MovieRepository movieRepository;

	private MovieServiceImpl movieService;

	private Director nolan;

	@BeforeEach
	void setUp() {
		movieService = new MovieServiceImpl(movieRepository);
		nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
	}

	private Movie inception() {
		return new Movie(10L, "Inception", 2010, 148, nolan);
	}

	@Test
	void findAllReturnsWhatTheRepositoryReturns() {
		List<Movie> movies = List.of(inception());
		when(movieRepository.findAll()).thenReturn(movies);

		assertThat(movieService.findAll()).isEqualTo(movies);
	}

	@Test
	void findAllReturnsEmptyListWhenThereAreNoMovies() {
		when(movieRepository.findAll()).thenReturn(List.of());

		assertThat(movieService.findAll()).isEmpty();
	}

	@Test
	void findByIdReturnsTheMovie() {
		Movie movie = inception();
		when(movieRepository.findById(10L)).thenReturn(Optional.of(movie));

		assertThat(movieService.findById(10L)).isSameAs(movie);
	}

	@Test
	void findByIdThrowsWhenMovieDoesNotExist() {
		when(movieRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.findById(99L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");
	}

	@Test
	void createSavesTheMovieWithItsDirectorAndReturnsTheStoredOne() {
		Movie stored = inception();
		when(movieRepository.findDirectorById(1L)).thenReturn(Optional.of(nolan));
		when(movieRepository.save(any(Movie.class))).thenReturn(10L);
		when(movieRepository.findById(10L)).thenReturn(Optional.of(stored));

		Movie result = movieService.create("Inception", 2010, 148, 1L);

		assertThat(result).isSameAs(stored);
		ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
		verify(movieRepository).save(captor.capture());
		Movie saved = captor.getValue();
		assertThat(saved.getId()).isNull();
		assertThat(saved.getTitle()).isEqualTo("Inception");
		assertThat(saved.getReleaseYear()).isEqualTo(2010);
		assertThat(saved.getDuration()).isEqualTo(148);
		assertThat(saved.getDirector()).isSameAs(nolan);
	}

	@Test
	void createThrowsAndDoesNotSaveWhenDirectorDoesNotExist() {
		when(movieRepository.findDirectorById(7L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.create("Inception", 2010, 148, 7L))
				.isInstanceOf(DirectorNotFoundException.class)
				.hasMessage("No existe el director con id 7");

		verify(movieRepository, never()).save(any());
	}

	@Test
	void createFailsWithAnInternalErrorWhenTheSavedMovieCannotBeReloaded() {
		when(movieRepository.findDirectorById(1L)).thenReturn(Optional.of(nolan));
		when(movieRepository.save(any(Movie.class))).thenReturn(10L);
		when(movieRepository.findById(10L)).thenReturn(Optional.empty());

		// No es un 404: el cliente no ha pedido nada que no exista, falla el sistema.
		assertThatThrownBy(() -> movieService.create("Inception", 2010, 148, 1L))
				.isInstanceOf(IllegalStateException.class)
				.isNotInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void updateFailsWithAnInternalErrorWhenTheUpdatedMovieCannotBeReloaded() {
		when(movieRepository.findById(10L)).thenReturn(Optional.of(inception()), Optional.empty());
		when(movieRepository.findDirectorById(1L)).thenReturn(Optional.of(nolan));

		assertThatThrownBy(() -> movieService.update(10L, "Inception", 2010, 148, 1L))
				.isInstanceOf(IllegalStateException.class)
				.isNotInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void updateReplacesTheDataAndReturnsTheReloadedMovie() {
		Movie reloaded = new Movie(10L, "Inception 2", 2012, 150, nolan);
		when(movieRepository.findById(10L)).thenReturn(Optional.of(inception()), Optional.of(reloaded));
		when(movieRepository.findDirectorById(1L)).thenReturn(Optional.of(nolan));

		Movie result = movieService.update(10L, "Inception 2", 2012, 150, 1L);

		assertThat(result).isSameAs(reloaded);
		ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
		verify(movieRepository).update(eq(10L), captor.capture());
		assertThat(captor.getValue().getTitle()).isEqualTo("Inception 2");
		assertThat(captor.getValue().getReleaseYear()).isEqualTo(2012);
		assertThat(captor.getValue().getDuration()).isEqualTo(150);
		assertThat(captor.getValue().getDirector()).isSameAs(nolan);
	}

	@Test
	void updateThrowsAndDoesNotUpdateWhenMovieDoesNotExist() {
		when(movieRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.update(99L, "X", 2000, 90, 1L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		verify(movieRepository, never()).update(anyLong(), any());
	}

	@Test
	void updateThrowsAndDoesNotUpdateWhenDirectorDoesNotExist() {
		when(movieRepository.findById(10L)).thenReturn(Optional.of(inception()));
		when(movieRepository.findDirectorById(7L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.update(10L, "X", 2000, 90, 7L))
				.isInstanceOf(DirectorNotFoundException.class)
				.hasMessage("No existe el director con id 7");

		verify(movieRepository, never()).update(anyLong(), any());
	}

	@Test
	void deleteRemovesAnExistingMovie() {
		when(movieRepository.findById(10L)).thenReturn(Optional.of(inception()));

		movieService.delete(10L);

		verify(movieRepository).deleteById(10L);
	}

	@Test
	void deleteThrowsAndDoesNotDeleteWhenMovieDoesNotExist() {
		when(movieRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.delete(99L))
				.isInstanceOf(MovieNotFoundException.class);

		verify(movieRepository, never()).deleteById(anyLong());
	}

}
