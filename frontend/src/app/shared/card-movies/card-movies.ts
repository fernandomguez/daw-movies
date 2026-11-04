// Imports: herramientas para recibir la película (input) y avisar al listado (output).
import { Component, input, output } from '@angular/core';
import { Movie } from '../../core/models/movie';

// Configuración del componente: no usa nadie más, selector <app-card-movies>,
// y tiene su HTML y su SCSS en archivos aparte (el patrón de card-workers).
@Component({
  imports: [],
  selector: 'app-card-movies',
  styleUrl: './card-movies.scss',
  templateUrl: './card-movies.html',
})

export class CardMovies {

  // La película que pinta la tarjeta; la obliga a venir desde fuera.
  movie = input.required<Movie>();

  // Avisos para el listado: click en la tarjeta → detalle; botones → editar o borrar.
  readonly detail = output<number>();
  readonly edit = output<number>();
  readonly removeMovie = output<number>();

  protected abrirDetalle(): void {
    this.detail.emit(this.movie().id);
  }

  protected editarMovie(): void {
    this.edit.emit(this.movie().id);
  }

  protected eliminarMovie(): void {
    this.removeMovie.emit(this.movie().id);
  }
}
