import {
  Component,
  inject,
  ChangeDetectorRef
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  HttpClient,
  HttpErrorResponse
} from '@angular/common/http';

import {
  catchError,
  of,
  timeout
} from 'rxjs';

import { Router } from '@angular/router';


/* ============================
   RESPUESTA DE COLONIAS
   ============================ */

interface ColoniaResponse {
  nombre: string;
}


/* ============================
   RESPUESTA DE POSTALIA
   ============================ */

interface PostaliaResponse {

  codigo_postal: string;

  estado: string;

  municipio: string;

  zona: string;

  colonias: ColoniaResponse[];
}


@Component({

  selector: 'app-registro',

  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './registro.html',

  styleUrl: './registro.css'

})

export class Registro {


  /* ============================
     SERVICIOS DE ANGULAR
     ============================ */

  private http = inject(HttpClient);

  private router = inject(Router);

  private cdr = inject(ChangeDetectorRef);


  /* ============================
     MODELO DEL FORMULARIO
     ============================ */

  model = {

    nombre: '',

    primerApellido: '',

    segundoApellido: '',

    telefono: '',

    codigoPostal: '',

    estado: '',

    municipio: '',

    direccion: '',

    fechaNacimiento: '',

    animalFavorito: ''

  };


  /* ============================
     FECHA MÁXIMA
     ============================ */

  readonly fechaMaxima =
    this.obtenerFechaActual();


  /* ============================
     CÓDIGO POSTAL
     ============================ */

  cpLoading = false;

  cpError = '';

  colonias: string[] = [];


  onCodigoPostalChange(): void {

    // Limpiamos errores anteriores
    this.cpError = '';

    // Limpiamos colonias anteriores
    this.colonias = [];


    const cp =
      this.model.codigoPostal.trim();


    // Validamos que tenga exactamente
    // 5 números.
    if (!/^\d{5}$/.test(cp)) {

      this.model.estado = '';

      this.model.municipio = '';

      return;
    }


    // Mostramos indicador de carga
    this.cpLoading = true;


    /*
     * Angular consulta a nuestro
     * backend Spring Boot.
     *
     * Angular NO consulta directamente
     * a Postalia.
     */
    this.http.get<PostaliaResponse>(
      `http://localhost:8081/api/postalia/${cp}`
    )
    .pipe(

      // Esperamos máximo 10 segundos.
      timeout(10000),

      catchError(
        (err: HttpErrorResponse) => {

          console.error(
            'Error consultando código postal:',
            err
          );


          if (err.status === 404) {

            this.cpError =
              'Código postal no encontrado.';

          } else {

            this.cpError =
              'No se pudo consultar el código postal.';

          }


          this.model.estado = '';

          this.model.municipio = '';

          this.cpLoading = false;


          // Actualizamos la pantalla
          this.cdr.detectChanges();


          return of(null);
        }
      )

    )
    .subscribe(res => {


      // Si ocurrió un error,
      // no continuamos.
      if (!res) {
        return;
      }


      console.log(
        'Respuesta recibida de Spring:',
        res
      );


      // Guardamos el estado recibido
      this.model.estado =
        res.estado;


      // Guardamos el municipio recibido
      this.model.municipio =
        res.municipio;


      // Convertimos las colonias
      // de objetos a textos.
      this.colonias =
        res.colonias.map(
          colonia => colonia.nombre
        );


      // Terminamos la carga
      this.cpLoading = false;


      /*
       * Fuerza a Angular a actualizar
       * inmediatamente los campos
       * Estado y Municipio.
       */
      this.cdr.detectChanges();

    });

  }


  /* ============================
     FECHA ACTUAL
     ============================ */

  private obtenerFechaActual(): string {

    const hoy = new Date();


    const año =
      hoy.getFullYear();


    const mes =
      String(
        hoy.getMonth() + 1
      ).padStart(
        2,
        '0'
      );


    const dia =
      String(
        hoy.getDate()
      ).padStart(
        2,
        '0'
      );


    return `${año}-${mes}-${dia}`;
  }


  /* ============================
     ENVÍO DEL FORMULARIO
     ============================ */

  submitted = false;

  saving = false;

  saveError = '';


  onSubmit(form: any): void {

    this.submitted = true;


    /*
     * Si algún campo obligatorio
     * está incorrecto, no enviamos
     * información.
     */
    if (form.invalid) {

      return;

    }


    this.saving = true;

    this.saveError = '';


    /*
     * Enviamos directamente el model
     * a nuestro backend Spring Boot.
     */
    this.http.post(
      'http://localhost:8081/api/usuarios',
      this.model
    )
    .pipe(

      timeout(10000),

      catchError(
        (err: HttpErrorResponse) => {

          console.error(
            'Error al guardar usuario:',
            err
          );


          this.saveError =
            'No se pudo guardar el registro. Intenta nuevamente.';


          this.saving = false;


          this.cdr.detectChanges();


          return of(null);
        }
      )

    )
    .subscribe(res => {


      if (!res) {
        return;
      }


      console.log(
        'Usuario guardado correctamente:',
        res
      );


      this.saving = false;


      this.cdr.detectChanges();


      /*
       * Solamente vamos a /exito
       * si Spring guardó correctamente
       * el usuario.
       */
      this.router.navigate([
        '/exito'
      ]);

    });

  }

}