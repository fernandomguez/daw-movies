// Imports: Angular (signals), el servicio y los tres componentes reutilizables.
import { Component, computed, inject, signal } from '@angular/core';
import { MovieService } from '../../core/services/movie-service';
import { Movie } from '../../core/models/movie';
import { CardMovies } from '../../shared/card-movies/card-movies';
import { ModalMovies } from '../../shared/modal-movies/modal-movies';
import { FormMovies } from '../../shared/form-movies/form-movies';

// Configuración del componente: usa card + los dos modales, selector <app-movies>
// y tiene su HTML y su SCSS en archivos aparte (el patrón de la referencia).
@Component({
  imports: [CardMovies, ModalMovies, FormMovies],
  selector: 'app-movies',
  styleUrl: './movies.scss',
  templateUrl: './movies.html',
})
export class Movies {

  // Servicio con la lista de películas y las operaciones contra la API.
  // Es protected para poder usarlo directamente desde el HTML.
  protected movieService = inject(MovieService);

  // =================   EL BUSCADOR (por título, en el cliente)   ====================

  textoBusqueda = signal<string>('');

  // Cambia el texto del buscador. event.target es el <input>;
  // el "as HTMLInputElement" le dice a TypeScript que sí tiene .value.
  teclearBusqueda(event: Event) {
    const input = event.target as HTMLInputElement;
    this.textoBusqueda.set(input.value);
  }

  moviesFiltradas = computed(() => {
    let texto = this.textoBusqueda().toLowerCase();
    let movies = this.movieService.movies();
    if (texto === '') {
      return movies;
    }
    return movies.filter((m) => m.title.toLowerCase().includes(texto));
  });

  // =================   LOS MODALES   ====================

  // Película abierta en el modal de detalle (null = cerrado).
  movieDetalle = signal<Movie | null>(null);

  // Película cargada en el modal del formulario (null = crear).
  modalFormulario = signal<Movie | null>(null);

  // Aviso informativo arriba de la lista (creada, borrada, no se pudo borrar...).
  aviso = signal<string>('');

  protected abrirDetalle(id: number): void {
    this.movieDetalle.set(this.movieService.movies().find((m) => m.id === id) ?? null);
  }

  protected abrirFormulario(id: number | null): void {
    this.modalFormulario.set(id === null ? null : (this.movieService.movies().find((m) => m.id === id) ?? null));
  }

  protected cerrarDetalle(): void {
    this.movieDetalle.set(null);
  }

  protected cerrarFormulario(): void {
    this.modalFormulario.set(null);
    // El formulario avisa solo sobre errores; aquí se refresca la lista por si el CRUD cambió algo.
    this.movieService.refresh();
  }

  // Borrar: confirma antes (el backend no pregunta) y pinta el resultado en el aviso.
  protected borrarPelicula(id: number): void {
    const movie = this.movieService.movies().find((m) => m.id === id);
    if (!movie || !confirm(`¿Borrar "${movie.title}" (id ${id})? Esta operación no se puede deshacer.`)) {
      return;
    }

    this.movieService.remove(id).subscribe({
      next: () => {
        this.aviso.set(`Película "${movie.title}" borrada.`);
        this.movieService.refresh();
        if (this.movieDetalle()?.id === id) {
          this.cerrarDetalle();
        }
      },
      error: (err) => this.aviso.set(`No se pudo borrar: ${this.movieService.mensajePublico(err)}`),
    });
  }
}
