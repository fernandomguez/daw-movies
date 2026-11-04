package com.daw.movies.catalog.api.http;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.daw.movies.catalog.api.http.dto.response.MovieResponse;
import com.daw.movies.catalog.api.http.handler.CatalogExceptionHandler;
import com.daw.movies.catalog.api.http.mapper.MovieMapper;
import com.daw.movies.catalog.internal.application.CreateMovieUseCase;
import com.daw.movies.catalog.internal.application.DeleteMovieUseCase;
import com.daw.movies.catalog.internal.application.FindMovieByIdUseCase;
import com.daw.movies.catalog.internal.application.FindMoviesUseCase;
import com.daw.movies.catalog.internal.application.UpdateMovieUseCase;
import com.daw.movies.catalog.internal.domain.exception.DirectorNotFoundException;
import com.daw.movies.catalog.internal.domain.exception.MovieNotFoundException;
import com.daw.movies.catalog.internal.domain.model.Actor;
import com.daw.movies.catalog.internal.domain.model.Director;
import com.daw.movies.catalog.internal.domain.model.Movie;

@WebMvcTest(MovieController.class)
@Import({ MovieMapper.class, CatalogExceptionHandler.class })
class MovieControllerTest {

	private static final String VALID_BODY = """
			{"title":"Inception","releaseYear":2010,"duration":148,"directorId":1}
			""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FindMoviesUseCase findMovies;

	@MockitoBean
	private FindMovieByIdUseCase findMovieById;

	@MockitoBean
	private CreateMovieUseCase createMovie;

	@MockitoBean
	private UpdateMovieUseCase updateMovie;

	@MockitoBean
	private DeleteMovieUseCase deleteMovie;

	private Movie inception() {
		Director nolan = new Director(1L, "Christopher", "Nolan", LocalDate.of(1970, 7, 30), null);
		Movie movie = new Movie(10L, "Inception", 2010, 148, nolan);
		movie.addActor(new Actor(5L, "Leonardo", "DiCaprio", LocalDate.of(1974, 11, 11), null));
		return movie;
	}

	@Test
	void getAllReturnsTheMoviesAsJson() throws Exception {
		when(findMovies.execute()).thenReturn(List.of(inception()));

		mockMvc.perform(get("/movies"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(10))
				.andExpect(jsonPath("$[0].title").value("Inception"))
				.andExpect(jsonPath("$[0].releaseYear").value(2010))
				.andExpect(jsonPath("$[0].duration").value(148))
				.andExpect(jsonPath("$[0].director.id").value(1))
				.andExpect(jsonPath("$[0].director.fullName").value("Christopher Nolan"))
				.andExpect(jsonPath("$[0].actors[0].id").value(5))
				.andExpect(jsonPath("$[0].actors[0].fullName").value("Leonardo DiCaprio"));
	}

	@Test
	void getAllReturnsEmptyArrayWhenThereAreNoMovies() throws Exception {
		when(findMovies.execute()).thenReturn(List.of());

		mockMvc.perform(get("/movies"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void getByIdReturnsTheMovie() throws Exception {
		when(findMovieById.execute(10L)).thenReturn(inception());

		mockMvc.perform(get("/movies/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.title").value("Inception"))
				.andExpect(jsonPath("$.director.fullName").value("Christopher Nolan"));
	}

	@Test
	void getByIdReturns404WhenMovieDoesNotExist() throws Exception {
		when(findMovieById.execute(99L)).thenThrow(new MovieNotFoundException(99L));

		mockMvc.perform(get("/movies/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe la película con id 99"));
	}

	@Test
	void postCreatesTheMovieAndReturns201() throws Exception {
		when(createMovie.execute("Inception", 2010, 148, 1L)).thenReturn(inception());

		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.title").value("Inception"));

		verify(createMovie).execute("Inception", 2010, 148, 1L);
	}

	@Test
	void postReturns400AndDoesNotCallTheUseCaseWhenBodyIsInvalid() throws Exception {
		String invalid = """
				{"title":"  ","releaseYear":1800,"duration":0,"directorId":1}
				""";

		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(invalid))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("title")))
				.andExpect(content().string(containsString("releaseYear")))
				.andExpect(content().string(containsString("duration")));

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void postReturns400WhenTheYearIsMoreThanTenYearsAhead() throws Exception {
		int tooFar = Year.now().getValue() + 11;
		String body = """
				{"title":"Inception","releaseYear":%d,"duration":148,"directorId":1}
				""".formatted(tooFar);

		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("releaseYear")));

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void postAcceptsAYearExactlyTenYearsAhead() throws Exception {
		int limit = Year.now().getValue() + 10;
		when(createMovie.execute("Inception", limit, 148, 1L)).thenReturn(inception());
		String body = """
				{"title":"Inception","releaseYear":%d,"duration":148,"directorId":1}
				""".formatted(limit);

		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());
	}

	@Test
	void postReturns400WhenTheJsonIsMalformed() throws Exception {
		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content("{\"title\": "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void postReturns400WhenANumberHasTheWrongType() throws Exception {
		String body = """
				{"title":"Inception","releaseYear":"dosmil","duration":148,"directorId":1}
				""";

		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").exists());

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void getReturns400WhenTheIdIsNotANumber() throws Exception {
		mockMvc.perform(get("/movies/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("id")));

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void postReturns415WhenTheContentTypeIsNotJson() throws Exception {
		mockMvc.perform(post("/movies").contentType(MediaType.TEXT_PLAIN).content("hola"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.message").exists());

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void postReturns400WhenRequiredFieldsAreMissing() throws Exception {
		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("directorId")));

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void postReturns404WhenDirectorDoesNotExist() throws Exception {
		when(createMovie.execute(anyString(), anyInt(), anyInt(), anyLong()))
				.thenThrow(new DirectorNotFoundException(1L));

		mockMvc.perform(post("/movies").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe el director con id 1"));
	}

	@Test
	void putUpdatesTheMovieAndReturns200() throws Exception {
		when(updateMovie.execute(10L, "Inception", 2010, 148, 1L)).thenReturn(inception());

		mockMvc.perform(put("/movies/10").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.title").value("Inception"));
	}

	@Test
	void putReturns404WhenMovieDoesNotExist() throws Exception {
		when(updateMovie.execute(anyLong(), anyString(), anyInt(), anyInt(), anyLong()))
				.thenThrow(new MovieNotFoundException(99L));

		mockMvc.perform(put("/movies/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("No existe la película con id 99"));
	}

	@Test
	void putReturns400WhenBodyIsInvalid() throws Exception {
		String invalid = """
				{"title":"Inception","releaseYear":2010,"duration":-5,"directorId":1}
				""";

		mockMvc.perform(put("/movies/10").contentType(MediaType.APPLICATION_JSON).content(invalid))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(findMovies, findMovieById, createMovie, updateMovie, deleteMovie);
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(delete("/movies/10"))
				.andExpect(status().isNoContent());

		verify(deleteMovie).execute(10L);
	}

	@Test
	void deleteReturns404WhenMovieDoesNotExist() throws Exception {
		doThrow(new MovieNotFoundException(99L)).when(deleteMovie).execute(99L);

		mockMvc.perform(delete("/movies/99"))
				.andExpect(status().isNotFound());
	}

}
