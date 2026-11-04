import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CardMovies } from './card-movies';
import { Movie } from '../../core/models/movie';

describe('CardMovies', () => {
  let component: CardMovies;
  let fixture: ComponentFixture<CardMovies>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CardMovies],
    }).compileComponents();

    fixture = TestBed.createComponent(CardMovies);
    fixture.componentRef.setInput('movie', mockMovie());
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('avisa con el id al pedir el detalle', () => {
    let recibido = 0;
    component.detail.subscribe((id) => (recibido = id));
    (component as any).abrirDetalle();
    expect(recibido).toBe(3);
  });

  it('avisa con el id al pedir editar y al borrar', () => {
    let editado = 0;
    let borrado = 0;
    component.edit.subscribe((id) => (editado = id));
    component.removeMovie.subscribe((id) => (borrado = id));
    (component as any).editarMovie();
    (component as any).eliminarMovie();
    expect(editado).toBe(3);
    expect(borrado).toBe(3);
  });
});

// Película de prueba con los mismos campos que manda el backend (MovieResponse).
function mockMovie(): Movie {
  return {
    id: 3,
    title: 'Inception',
    releaseYear: 2010,
    duration: 148,
    director: { id: 1, fullName: 'Christopher Nolan' },
    actors: [
      { id: 2, fullName: 'Michael Caine' },
      { id: 3, fullName: 'Christian Bale' },
    ],
  };
}
