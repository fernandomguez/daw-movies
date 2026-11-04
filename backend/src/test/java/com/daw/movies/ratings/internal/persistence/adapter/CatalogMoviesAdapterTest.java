package com.daw.movies.ratings.internal.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.daw.movies.ratings.internal.domain.port.AvailableMoviesPort;
import com.daw.movies.catalog.api.facade.CatalogFacade;
import com.daw.movies.catalog.api.facade.MovieLookup;

// El adapter es la única pieza que traduce el DTO de la fachada ajena al
// record propio del puerto: aquí se comprueba esa traducción (§3.3).
@ExtendWith(MockitoExtension.class)
class CatalogMoviesAdapterTest {

	@Mock
	private CatalogFacade catalogFacade;

	@Test
	void findMovieMapsTheFacadeDtoToThePortRecord() {
		when(catalogFacade.findMovie(10L)).thenReturn(Optional.of(new MovieLookup(10L, "Inception")));

		Optional<AvailableMoviesPort.AvailableMovie> movie =
				new CatalogMoviesAdapter(catalogFacade).findMovie(10L);
		assertThat(movie)
				.hasValueSatisfying(available -> {
					assertThat(available.id()).isEqualTo(10L);
					assertThat(available.title()).isEqualTo("Inception");
				});
	}

	@Test
	void findMovieContinuesEmptyWhenTheFacadeFindsNothing() {
		when(catalogFacade.findMovie(99L)).thenReturn(Optional.empty());

		assertThat(new CatalogMoviesAdapter(catalogFacade).findMovie(99L)).isEmpty();
	}

}
