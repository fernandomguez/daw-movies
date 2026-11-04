// Imports: formularios reactivos, el servicio que habla con la API y el modelo.
import { Component, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Movie, MoviePayload } from '../../core/models/movie';
import { MovieService } from '../../core/services/movie-service';

// Modal de crear/editar: si le llega una película, la edita (PUT);
// si no, crea una nueva (POST). Pinta los mensajes de validación del backend.
@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-form-movies',
  styleUrl: './form-movies.scss',
  templateUrl: './form-movies.html',
})

export class FormMovies {

  // Película a editar (o null para crear). El effect rellena el form cuando cambia.
  movie = input<Movie | null>(null);

  readonly cerrar = output<void>();

  protected movieService = inject(MovieService);
  private formBuilder = inject(FormBuilder);

  protected maxAno(): number {
    return new Date().getFullYear() + 10;
  }

  // Reglas espejo de MovieRequest: mismas que aplicará el backend con @Valid.
  // El desplegable del director entrega el value como texto (lo manda el HTML,
  // no usa ngValue): el control lo guarda tal cual y en enviarFormulario se
  // convierte en el id numérico que pide el MovieRequest.
  protected form = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    releaseYear: [null as number | null, [Validators.required, Validators.min(1888), Validators.max(this.maxAno())]],
    duration: [null as number | null, [Validators.required, Validators.min(1)]],
    directorId: [null as number | string | null, [Validators.required, Validators.min(1)]],
  });

  // Mensaje del backend (400/404) o del propio formulario, para pintarlo arriba.
  aviso = signal<string>('');

  // Cada vez que el padre abre el modal con OTRA película (o con null),
  // se rellena el formulario con sus datos y se limpia el aviso.
  constructor() {
    effect(() => {
      const movie = this.movie();
      this.aviso.set('');
      this.form.reset({
        title: movie?.title ?? '',
        releaseYear: movie?.releaseYear ?? null,
        duration: movie?.duration ?? null,
        directorId: movie?.director.id ?? null,
      });
    });
  }

  protected esEditando(): boolean {
    return this.movie() !== null;
  }

  protected enviarFormulario(): void {
    if (this.form.invalid) {
      this.aviso.set('Revisa los campos marcados en rojo.');
      return;
    }

    // El payload es exactamente el MovieRequest del backend (directorId, no director).
    const payload = this.form.getRawValue();
    // Única conversión de la cadena que llega del <select>: aquí ya es número
    // para que el MoviePayload viaje como pide el backend.
    const directorId = payload.directorId === null ? null : Number(payload.directorId);
    if (payload.releaseYear === null || payload.duration === null || directorId === null || Number.isNaN(directorId)) {
      this.aviso.set('Revisa los campos marcados en rojo.');
      return;
    }
    const datos = {
      title: payload.title,
      releaseYear: payload.releaseYear,
      duration: payload.duration,
      directorId,
    };

    if (this.movie()) {
      this.actualizarPelicula(datos);
    } else {
      this.crearPelicula(datos);
    }
  }

  protected crearPelicula(payload: MoviePayload): void {
    this.movieService.create(payload).subscribe({
      next: () => this.cerrar.emit(),
      error: (err) => this.aviso.set(this.movieService.mensajePublico(err)),
    });
  }

  protected actualizarPelicula(payload: MoviePayload): void {
    const id = this.movie()!.id;
    this.movieService.update(id, payload).subscribe({
      next: () => this.cerrar.emit(),
      error: (err) => this.aviso.set(this.movieService.mensajePublico(err)),
    });
  }
}
