// Imports: el listado de películas es lo único que se pinta en la raíz.
// (El Router ya está disponible en app.config.ts para cuando se pase a rutas /movies/:id.)
import { Component } from '@angular/core';
import { Movies } from './features/movies/movies';

@Component({
  imports: [Movies],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {

}
