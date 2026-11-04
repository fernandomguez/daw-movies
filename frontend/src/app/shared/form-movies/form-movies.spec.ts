import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
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

  it('no llama a la API si el formulario es inválido y avisa', () => {
    const http = TestBed.inject(HttpTestingController);
    // El MovieService hace un GET /movies al instanciarse: lo descargamos aquí.
    http.expectOne('/movies').flush([]);
    (component as any).form.controls.title.setValue('');
    (component as any).form.controls.title.markAsTouched();
    (component as any).enviarFormulario();
    expect((component as any).aviso()).toContain('Revisa los campos');
    // Falla si se ha publicado algo más de lo que esperábamos (nada)
    http.verify();
  });

  it('ofrece los directores del backend en el desplegable', async () => {
    const http = TestBed.inject(HttpTestingController);
    // La lista que llega del backend alimenta las opciones (ya con dedup del servicio).
    http.expectOne('/movies').flush([mockMovie()]);
    await fixture.whenStable();

    const opciones = ((fixture.nativeElement as HTMLElement).querySelector('#directorId') as HTMLSelectElement).options;
    expect(opciones.length).toBe(2); // la vacía + el director de la película
    expect(opciones[0].text).toContain('Elige un director');
    expect(opciones[1].text).toBe('Christopher Nolan');
  });
  it('convierte el value del desplegable en el id numérico que pide el backend', () => {
    const http = TestBed.inject(HttpTestingController);
    // El GET inicial del servicio se desecha; la edición del mockMovie va por PUT.
    http.expectOne('/movies').flush([]);
    (component as any).form.patchValue({ directorId: '1' }); // el texto del <option>
    (component as any).enviarFormulario();

    const peticion = http.expectOne({ method: 'PUT', url: '/movies/3' });
    expect(peticion.request.body.directorId).toBe(1); // ya es número
    peticion.flush({});
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
