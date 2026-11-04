import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FormMovies } from './form-movies';
import { Movie } from '../../core/models/movie';

describe('FormMovies', () => {
  let component: FormMovies;
  let fixture: ComponentFixture<FormMovies>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormMovies],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(FormMovies);
    fixture.componentRef.setInput('movie', mockMovie());
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('rellena el formulario con la película que le llega', () => {
    const form = (component as any).form;
    expect(form.controls.title.value).toBe('Inception');
    expect(form.controls.releaseYear.value).toBe(2010);
    expect(form.controls.duration.value).toBe(148);
    expect(form.controls.directorId.value).toBe(1);
  });

  it('rechaza un formulario sin director', () => {
    (component as any).form.controls.directorId.setValue(null);
    (component as any).enviarFormulario();
    expect((component as any).aviso()).toContain('Revisa los campos');
  });

  it('avisa al pedir el cierre', () => {
    let cerrado = false;
    component.cerrar.subscribe(() => (cerrado = true));
    component.cerrar.emit();
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
    actors: [],
  };
}
