package com.daw.movies.catalog.internal.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;
import com.daw.movies.catalog.internal.domain.repository.MovieRepository;

// El servicio de dominio es una capa fina sobre el puerto: lo que se comprueba
// es que delega tal cual (las reglas de negocio viven en los casos de uso).
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

	@Mock
	private MovieRepository movieRepository;

	private Movie inception() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		return new Movie(10L, "Inception", 2010, 148, nolan);
	}

	private MovieServiceImpl service(MovieRepository repository) {
		return new MovieServiceImpl(repository);
	}

	@Test
	void findMovieReturnsWhatTheRepositoryReturns() {
		when(movieRepository.findById(10L)).thenReturn(Optional.of(inception()));

		assertThat(service(movieRepository).findMovie(10L))
				.hasValueSatisfying(movie -> assertThat(movie.getTitle()).isEqualTo("Inception"));
	}

	@Test
	void findMovieReturnsEmptyWhenThereIsNoMovie() {
		when(movieRepository.findById(99L)).thenReturn(Optional.empty());

		assertThat(service(movieRepository).findMovie(99L)).isEmpty();
	}

	@Test
	void findDirectorReturnsWhatTheRepositoryReturns() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		when(movieRepository.findDirectorById(1L)).thenReturn(Optional.of(nolan));

		assertThat(service(movieRepository).findDirector(1L))
				.hasValueSatisfying(director -> assertThat(director.fullName()).isEqualTo("Christopher Nolan"));
	}

	@Test
	void findDirectorReturnsEmptyWhenThereIsNoDirector() {
		when(movieRepository.findDirectorById(99L)).thenReturn(Optional.empty());

		assertThat(service(movieRepository).findDirector(99L)).isEmpty();
	}

}
