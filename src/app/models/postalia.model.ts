export interface ColoniaResponse {
  nombre: string;
}

export interface PostaliaResponse {
  codigo_postal: string;
  estado: string;
  municipio: string;
  zona: string;
  colonias: ColoniaResponse[];
}