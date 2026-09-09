import { Injectable } from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  PostaliaResponse
} from '../models/postalia.model';


@Injectable({
  providedIn: 'root'
})
export class PostaliaService {

  private apiUrl =
    'http://localhost:8081/api/postalia';


  constructor(
    private http: HttpClient
  ) {}


  buscarCodigoPostal(
    cp: string
  ): Observable<PostaliaResponse> {

    return this.http.get<PostaliaResponse>(
      `${this.apiUrl}/${cp}`
    );
  }

}