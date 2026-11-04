package com.daw.movies.ratings.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.ratings.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.domain.port.AvailableMoviesPort;
import com.daw.movies.ratings.internal.domain.repository.RatingRepository;

@ExtendWith(MockitoExtension.class)
class FindRatingsByMovieUseCaseTest {

	@Mock
	private AvailableMoviesPort availableMovies;

	@Mock
	private RatingRepository ratingRepository;

	@Test
	void executeChecksTheMovieFirstAndThenReadsTheRatings() {
		when(availableMovies.findMovie(10L))
				.thenReturn(Optional.of(new AvailableMoviesPort.AvailableMovie(10L, "Inception")));
		Rating rating = new Rating(33L, 10L, 8, null);
		when(ratingRepository.findByMovieId(10L)).thenReturn(List.of(rating));

		List<Rating> ratings = new FindRatingsByMovieUseCase(availableMovies, ratingRepository).execute(10L);

		assertThat(ratings).containsExactly(rating);
	}

	@Test
	void executeThrowsMovieNotFoundWhenTheMovieDoesNotExist() {
		when(availableMovies.findMovie(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new FindRatingsByMovieUseCase(availableMovies, ratingRepository).execute(99L))
				.isInstanceOf(MovieNotFoundException.class)
				.hasMessage("No existe la película con id 99");

		// la lista ni se pide: la película no existe
		verify(ratingRepository, never()).findByMovieId(anyLong());
	}

}
