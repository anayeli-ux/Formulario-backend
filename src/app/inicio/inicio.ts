import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  AuthService
} from '../services/auth.service';

@Component({
  selector: 'app-inicio',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl: './inicio.html',
  styleUrl: './inicio.css'
})
export class InicioComponent {

  usuario = '';
  password = '';

  mensajeError = '';

  cargando = false;

  mostrarPassword = false;

  private apiUrl =
    'http://localhost:8081/api/auth/admin';

  constructor(
    private http: HttpClient,
    private router: Router,
    private authService: AuthService
  ) {}

  iniciarSesion(): void {

    this.mensajeError = '';

    if (!this.usuario || !this.password) {

      this.mensajeError =
        'Ingresa tu usuario y contraseña.';

      return;
    }

    this.cargando = true;

    const datos = {

      usuario: this.usuario,

      password: this.password

    };

    this.http
      .post<{ acceso: boolean }>(
        this.apiUrl,
        datos
      )
      .subscribe({

        next: respuesta => {

          this.cargando = false;

          if (respuesta.acceso) {

            this.authService.iniciarSesion();

            this.router.navigate(['/admin']);

          } else {

            this.mensajeError =
              'Usuario o contraseña incorrectos.';

          }

        },

        error: error => {

          console.error(
            'Error al iniciar sesión:',
            error
          );

          this.cargando = false;

          this.mensajeError =
            'No se pudo conectar con el servidor.';

        }

      });

  }

  irRegistro(): void {

    this.router.navigate(['/registro']);

  }

}