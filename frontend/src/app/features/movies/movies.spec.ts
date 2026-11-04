import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Movies } from './movies';
import { Movie, Person } from '../../core/models/movie';

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

  it('monta el desplegable con los directores únicos de la lista', () => {
    (component as any).movieService.movies.set([inception(), pulpFiction(), otraDeTarantino()]);

    expect((component as any).movieService.directores().map((d: Person) => d.fullName)).toEqual([
      'Christopher Nolan',
      'Quentin Tarantino',
    ]);
  });

  it('filtra por director del desplegable y limpia con la opción de todos', () => {
    (component as any).movieService.movies.set([inception(), pulpFiction()]);

    (component as any).seleccionarDirector(selectFalso('2'));
    expect((component as any).moviesFiltradas().map((m: Movie) => m.id)).toEqual([2]);

    (component as any).seleccionarDirector(selectFalso(''));
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

  // Regresión del bug: crear usa null, que un @if con "as" trata como falsy
  // y el modal no se dibujaba al pulsar «Nueva película».
  it('dibuja el formulario al pulsar «Nueva película»', () => {
    const boton = (fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('.movies__nueva-button')!;
    boton.click();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('app-form-movies')).toBeTruthy();
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

// Segunda película del mismo director: el desplegable no debe repetirlo.
function otraDeTarantino(): Movie {
  return {
    id: 3,
    title: 'Jackie Brown',
    releaseYear: 1997,
    duration: 154,
    director: { id: 2, fullName: 'Quentin Tarantino' },
    actors: [],
  };
}

// Un input HTML falso que devuelve el value que pide la prueba.
function inputFalso(valor: string): Event {
  return { target: { value: valor } } as unknown as Event;
}

// Un select HTML falso; el desplegable de directores envía el value como texto.
function selectFalso(valor: string): Event {
  return { target: { value: valor } } as unknown as Event;
}
