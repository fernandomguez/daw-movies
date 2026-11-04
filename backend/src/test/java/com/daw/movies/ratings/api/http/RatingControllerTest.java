package com.daw.movies.ratings.api.http;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.daw.movies.ratings.api.http.handler.RatingExceptionHandler;
import com.daw.movies.ratings.api.http.mapper.RatingMapper;
import com.daw.movies.ratings.internal.application.CreateRatingUseCase;
import com.daw.movies.ratings.internal.application.DeleteRatingUseCase;
import com.daw.movies.ratings.internal.application.FindRatingsByMovieUseCase;
import com.daw.movies.ratings.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.ratings.internal.domain.exception.RatingNotFoundException;
import com.daw.movies.ratings.internal.domain.model.Rating;

@WebMvcTest(RatingController.class)
@Import({ RatingMapper.class, RatingExceptionHandler.class })
class RatingControllerTest {

	private static final String VALID_BODY = """
			{"score":8,"comment":"Excelente"}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FindRatingsByMovieUseCase findRatingsByMovie;

	@MockitoBean
	private CreateRatingUseCase createRating;

	@MockitoBean
	private DeleteRatingUseCase deleteRating;

	private Rating rating() {
		return new Rating(33L, 10L, 8, "Excelente");
	}

	@Test
	void getByMovieReturnsTheRatingsAsJson() throws Exception {
		when(findRatingsByMovie.execute(10L)).thenReturn(List.of(rating()));

		mockMvc.perform(get("/movies/10/ratings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(33))
				.andExpect(jsonPath("$[0].movieId").value(10))
				.andExpect(jsonPath("$[0].score").value(8))
				.andExpect(jsonPath("$[0].comment").value("Excelente"));
	}

	@Test
	void getByMovieReturnsEmptyArrayWhenTheMovieHasNoRatings() throws Exception {
		when(findRatingsByMovie.execute(10L)).thenReturn(List.of());

		mockMvc.perform(get("/movies/10/ratings"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void getByMovieReturns404WhenTheMovieDoesNotExist() throws Exception {
		when(findRatingsByMovie.execute(99L)).thenThrow(new MovieNotFoundException(99L));

		mockMvc.perform(get("/movies/99/ratings"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe la película con id 99"));
	}

	@Test
	void postCreatesTheRatingAndReturns201() throws Exception {
		when(createRating.execute(10L, 8, "Excelente")).thenReturn(rating());

		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(33))
				.andExpect(jsonPath("$.movieId").value(10))
				.andExpect(jsonPath("$.score").value(8));

		verify(createRating).execute(10L, 8, "Excelente");
	}

	@Test
	void postAcceptsARatingWithoutComment() throws Exception {
		when(createRating.execute(10L, 7, null)).thenReturn(new Rating(34L, 10L, 7, null));

		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.APPLICATION_JSON).content("{\"score\":7}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.comment").doesNotExist());
	}

	@Test
	void postReturns400WhenTheScoreIsOutOfTenRange() throws Exception {
		String invalid = """
				{"score":11,"comment":"Excelente"}
				""";

		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.APPLICATION_JSON).content(invalid))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("score")));

		verifyNoInteractions(findRatingsByMovie, createRating, deleteRating);
	}

	@Test
	void postReturns400WhenTheScoreIsMissing() throws Exception {
		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("score")));

		verifyNoInteractions(findRatingsByMovie, createRating, deleteRating);
	}

	@Test
	void postReturns400WhenTheCommentIsTooLong() throws Exception {
		String tooLong = "a".repeat(501);
		String body = "{\"score\":8,\"comment\":\"" + tooLong + "\"}";

		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("comment")));

		verifyNoInteractions(findRatingsByMovie, createRating, deleteRating);
	}

	@Test
	void postReturns400WhenTheJsonIsMalformed() throws Exception {
		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.APPLICATION_JSON).content("{\"score\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());

		verifyNoInteractions(findRatingsByMovie, createRating, deleteRating);
	}

	@Test
	void postReturns415WhenTheContentTypeIsNotJson() throws Exception {
		mockMvc.perform(post("/movies/10/ratings").contentType(MediaType.TEXT_PLAIN).content("hola"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.message").exists());

		verifyNoInteractions(findRatingsByMovie, createRating, deleteRating);
	}

	@Test
	void getReturns400WhenTheMovieIdIsNotANumber() throws Exception {
		mockMvc.perform(get("/movies/abc/ratings"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("movieId")));

		verifyNoInteractions(findRatingsByMovie, createRating, deleteRating);
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(delete("/ratings/33"))
				.andExpect(status().isNoContent());

		verify(deleteRating).execute(33L);
	}

	@Test
	void deleteReturns404WhenTheRatingDoesNotExist() throws Exception {
		doThrow(new RatingNotFoundException(99L)).when(deleteRating).execute(99L);

		mockMvc.perform(delete("/ratings/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe la valoración con id 99"));
	}

}
