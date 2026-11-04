import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Movies } from './movies';
import { Movie } from '../../core/models/movie';

describe('Movies', () => {
  let component: Movies;
  let fixture: ComponentFixture<Movies>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Movies],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Movies);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('filtra por título al teclear y limpia con cadena vacía', () => {
    (component as any).movieService.movies.set([inception(), pulpFiction()]);

    (component as any).teclearBusqueda(inputFalso('pulp'));
    expect((component as any).moviesFiltradas().map((m: Movie) => m.id)).toEqual([2]);

    (component as any).teclearBusqueda(inputFalso(''));
    expect((component as any).moviesFiltradas().length).toBe(2);
  });

  it('abre el detalle de la película elegida y lo cierra', () => {
    (component as any).movieService.movies.set([inception(), pulpFiction()]);

    (component as any).abrirDetalle(1);
    expect((component as any).movieDetalle()?.title).toBe('Inception');

    (component as any).cerrarDetalle();
    expect((component as any).movieDetalle()).toBeNull();
  });

  it('abre el formulario vacío para crear (null) y cargado para editar', () => {
    (component as any).movieService.movies.set([inception()]);

    (component as any).abrirFormulario(null);
    expect((component as any).modalFormulario()).toBeNull();

    (component as any).abrirFormulario(1);
    expect((component as any).modalFormulario()?.title).toBe('Inception');
  });
});

// Películas de prueba con los mismos campos que manda el backend.
function inception(): Movie {
  return {
    id: 1,
    title: 'Inception',
    releaseYear: 2010,
    duration: 148,
    director: { id: 1, fullName: 'Christopher Nolan' },
    actors: [],
  };
}

function pulpFiction(): Movie {
  return {
    id: 2,
    title: 'Pulp Fiction',
    releaseYear: 1994,
    duration: 154,
    director: { id: 2, fullName: 'Quentin Tarantino' },
    actors: [],
  };
}

// Un input HTML falso que devuelve el value que pide la prueba.
function inputFalso(valor: string): Event {
  return { target: { value: valor } } as unknown as Event;
}
