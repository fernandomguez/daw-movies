package com.daw.movies.ratings.domain.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.catalog.CatalogFacade;
import com.daw.movies.catalog.MovieLookup;
import com.daw.movies.ratings.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.domain.exception.RatingNotFoundException;
import com.daw.movies.ratings.domain.model.Rating;
import com.daw.movies.ratings.domain.repository.RatingRepository;

// Tests unitarios del caso de uso: el módulo catalog se simula por su fachada
// y la persistencia por su puerto (PER-R09 deja los tests de integración para
// el DAO).
@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

	@Mock
	private RatingRepository ratingRepository;

	@Mock
	private CatalogFacade catalogFacade;

	@InjectMocks
	private RatingServiceImpl service;

	@Test
	void createChecksTheMovieThroughTheFacadeAndSavesWithItsId() {
		when(catalogFacade.findMovie(10L)).thenReturn(Optional.of(new MovieLookup(10L, "Inception")));
		when(ratingRepository.save(any(Rating.class))).thenReturn(33L);
		when(ratingRepository.findById(33L)).thenReturn(Optional.of(new Rating(33L, 10L, 8, "Excelente")));

		Rating created = service.create(10L, 8, "Excelente");

		assertThat(created.getId()).isEqualTo(33L);
		assertThat(created.getMovieId()).isEqualTo(10L);
		assertThat(created.getScore()).isEqualTo(8);
		assertThat(created.getComment()).isEqualTo("Excelente");

		ArgumentCaptor<Rating> saved = ArgumentCaptor.forClass(Rating.class);
		verify(ratingRepository).save(saved.capture());
		assertThat(saved.getValue().getMovieId()).isEqualTo(10L);
		assertThat(saved.getValue().getScore()).isEqualTo(8);
	}

	@Test
	void createThrowsMovieNotFoundAndDoesNotSaveWhenTheFacadeFindsNothing() {
		when(catalogFacade.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.create(99L, 8, null))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		verify(ratingRepository, never()).save(any(Rating.class));
	}

	@Test
	void createFailsWithAnInternalErrorWhenTheSavedRatingCannotBeReloaded() {
		when(catalogFacade.findMovie(10L)).thenReturn(Optional.of(new MovieLookup(10L, "Inception")));
		when(ratingRepository.save(any(Rating.class))).thenReturn(33L);
		when(ratingRepository.findById(33L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.create(10L, 8, null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("La valoración 33 no se encuentra justo después de guardarla");
	}

	@Test
	void findByMovieIdChecksTheMovieFirstAndThenReadsTheRatings() {
		when(catalogFacade.findMovie(10L)).thenReturn(Optional.of(new MovieLookup(10L, "Inception")));
		Rating rating = new Rating(33L, 10L, 8, null);
		when(ratingRepository.findByMovieId(10L)).thenReturn(List.of(rating));

		List<Rating> ratings = service.findByMovieId(10L);

		assertThat(ratings).containsExactly(rating);
	}

	@Test
	void findByMovieIdThrowsMovieNotFoundWhenTheMovieDoesNotExist() {
		when(catalogFacade.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findByMovieId(99L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		// la lista ni se pide: la película no existe
		verify(ratingRepository, never()).findByMovieId(anyLong());
	}

	@Test
	void deleteThrowsWhenTheRatingDoesNotExist() {
		when(ratingRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.delete(99L))
				.isInstanceOf(RatingNotFoundException.class)
				.hasMessage("No existe la valoración con id 99");

		verify(ratingRepository, never()).deleteById(anyLong());
	}

	@Test
	void deleteDeletesOnceTheRatingExists() {
		when(ratingRepository.findById(33L)).thenReturn(Optional.of(new Rating(33L, 10L, 8, null)));

		service.delete(33L);

		verify(ratingRepository).deleteById(33L);
	}

}
