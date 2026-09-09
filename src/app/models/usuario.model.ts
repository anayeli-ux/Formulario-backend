export interface Usuario {
  id?: number;
  nombre: string;
  primerApellido: string;
  segundoApellido: string;
  telefono: string;
  codigoPostal: string;
  estado: string;
  municipio: string;
  direccion: string;
  fechaNacimiento: string;
  animalFavorito: string;
  activo?: boolean;
}