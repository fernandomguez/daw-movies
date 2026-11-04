// Imports: Angular DI (inject) + HttpClient para hablar con el backend vía proxy.
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { Movie, MoviePayload } from '../models/movie';

@Injectable({
  providedIn: 'root',
})
export class MovieService {

  // HttpClient aprovecha el proxy de dev: /movies → http://localhost:8080/movies.
  private http = inject(HttpClient);
  private url = '/movies';

  // =================   EL ESTADO (signals)   ====================

  // La lista de películas tal como la devuelve el backend.
  movies = signal<Movie[]>([]);

  // Para avisar en pantalla si el backend no responde (BD parada, ensayo fallido, etc).
  error = signal<string>('');

  // Número de películas; se recalcula solo cuando cambia la lista.
  total = computed(() => this.movies().length);

  constructor() {
    // Al arrancar la app, carga la lista del backend.
    this.load();
  }

  // Descarga la lista completa y sustituye el signal con ella.
  // Así, tras crear/actualizar/borrar, refrescar() vuelve a leer al backend.
  load(): void {
    this.http.get<Movie[]>(this.url).subscribe({
      next: (movies) => this.movies.set(movies),
      error: () => this.error.set('No se pudo conectar con el backend: ¿está arrancado?'),
    });
  }

  // Convierte el error de la API en el mensaje que devuelve el
  // GlobalExceptionHandler (JSON { "message": "..." } en 400/404);
  // lo usan los formularios para pintar el aviso exacto del backend.
  mensajePublico(err: HttpErrorResponse): string {
    return err?.error?.message ?? 'No se pudo conectar con el backend.';
  }

  // =================   LAS OPERACIONES (CRUD)   ====================

  // POST /movies. Devuelve el Observable para que el formulario componga
  // su respuesta (éxito → cerrar; error → pintar el mensaje del backend).
  create(payload: MoviePayload): Observable<Movie> {
    return this.http.post<Movie>(this.url, payload);
  }

  // PUT /movies/{id}. Igual que create, pero reemplazando la película existente.
  update(id: number, payload: MoviePayload): Observable<Movie> {
    return this.http.put<Movie>(`${this.url}/${id}`, payload);
  }

  // DELETE /movies/{id}. El backend responde 204 sin cuerpo si va bien.
  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  // Refresca la lista desde el backend; components la llaman tras el CRUD.
  refresh(): void {
    this.load();
  }
}
