// Espejo exacto del JSON que respone el backend (MovieResponse): no añadimos
// ni quitamos campos, para que HttpClient pueda tipar sin transformar nada.
export interface Person {
  id: number;
  fullName: string;
}

// Un Movie con su Director y sus Actor/es anidados (comparable al *value object* del backend).
export interface Movie {
  id: number;
  title: string;
  releaseYear: number;
  duration: number;
  director: Person;
  actors: Person[];
}

// Lo que el JSON de entrada (MovieRequest) acepta en POST y PUT.
export interface MoviePayload {
  title: string;
  releaseYear: number;
  duration: number;
  directorId: number;
}
