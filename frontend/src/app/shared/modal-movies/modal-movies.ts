// Imports: herramientas para recibir la película abierta y avisar al padre que se cierra.
import { Component, input, output } from '@angular/core';
import { Movie } from '../../core/models/movie';

// Modal de detalle: ventana flotante con la ficha completa de la película.
// El padre decide renderizarlo con @if; el clic en el fondo también cierra.
@Component({
  imports: [],
  selector: 'app-modal-movies',
  styleUrl: './modal-movies.scss',
  templateUrl: './modal-movies.html',
})

export class ModalMovies {

  movie = input.required<Movie>();

  readonly cerrar = output<void>();

  protected cerrarModal(): void {
    this.cerrar.emit();
  }
}
