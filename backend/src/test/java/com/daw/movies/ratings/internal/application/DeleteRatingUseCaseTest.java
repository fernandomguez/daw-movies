package com.daw.movies.ratings.internal.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.ratings.internal.domain.exception.RatingNotFoundException;
import com.daw.movies.ratings.internal.domain.model.Rating;
import com.daw.movies.ratings.internal.domain.repository.RatingRepository;

@ExtendWith(MockitoExtension.class)
class DeleteRatingUseCaseTest {

	@Mock
	private RatingRepository ratingRepository;

	@Test
	void deleteDeletesOnceTheRatingExists() {
		when(ratingRepository.findById(33L)).thenReturn(Optional.of(new Rating(33L, 10L, 8, null)));

		new DeleteRatingUseCase(ratingRepository).execute(33L);

		verify(ratingRepository).deleteById(33L);
	}

	@Test
	void deleteThrowsWhenTheRatingDoesNotExist() {
		when(ratingRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new DeleteRatingUseCase(ratingRepository).execute(99L))
				.isInstanceOf(RatingNotFoundException.class)
				.hasMessage("No existe la valoración con id 99");

		verify(ratingRepository, never()).deleteById(anyLong());
	}

}
