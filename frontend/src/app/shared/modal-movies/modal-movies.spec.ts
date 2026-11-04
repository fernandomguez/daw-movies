import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ModalMovies } from './modal-movies';
import { Movie } from '../../core/models/movie';

describe('ModalMovies', () => {
  let component: ModalMovies;
  let fixture: ComponentFixture<ModalMovies>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModalMovies],
    }).compileComponents();

    fixture = TestBed.createComponent(ModalMovies);
    fixture.componentRef.setInput('movie', mockMovie());
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('avisa al pedir el cierre', () => {
    let cerrado = false;
    component.cerrar.subscribe(() => (cerrado = true));
    (component as any).cerrarModal();
    expect(cerrado).toBe(true);
  });
});

function mockMovie(): Movie {
  return {
    id: 3,
    title: 'Inception',
    releaseYear: 2010,
    duration: 148,
    director: { id: 1, fullName: 'Christopher Nolan' },
    actors: [{ id: 1, fullName: 'Leonardo DiCaprio' }],
  };
}
