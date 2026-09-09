import { Injectable } from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  Usuario
} from '../models/usuario.model';


@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private apiUrl =
    'http://localhost:8081/api/usuarios';


  constructor(
    private http: HttpClient
  ) {}


  listarUsuarios(): Observable<Usuario[]> {

    return this.http.get<Usuario[]>(
      this.apiUrl
    );
  }


  listarUsuariosEliminados():
    Observable<Usuario[]> {

    return this.http.get<Usuario[]>(
      `${this.apiUrl}/eliminados`
    );
  }


  crearUsuario(
    usuario: Usuario
  ): Observable<Usuario> {

    return this.http.post<Usuario>(
      this.apiUrl,
      usuario
    );
  }


  actualizarUsuario(
    id: number,
    usuario: Usuario
  ): Observable<Usuario> {

    return this.http.put<Usuario>(
      `${this.apiUrl}/${id}`,
      usuario
    );
  }


  eliminarUsuario(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }

}