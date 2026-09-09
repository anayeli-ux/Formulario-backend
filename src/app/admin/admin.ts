import {
  CommonModule,
} from '@angular/common';

import {
  Component,
  Inject,
  OnInit,
  signal,
  computed
} from '@angular/core';

import { FormsModule } from '@angular/forms';

import {
  HttpErrorResponse
} from '@angular/common/http'; //servicio

import { Router } from '@angular/router';

import {
  catchError,
  of,
  timeout
} from 'rxjs'; //servicio **

import { Usuario } from '../models/usuario.model';

import {
  PostaliaResponse
} from '../models/postalia.model';

import {
  UsuarioService
} from '../services/usuario.service';

import {
  PostaliaService
} from '../services/postalia.service';

import {
  AuthService
} from '../services/auth.service';


//modelos
//servicios
//guard
//interceptor


@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin implements OnInit {

  // ==========================================
  // SIGNALS DE USUARIOS
  // ==========================================

  usuarios = signal<Usuario[]>([]);

  usuariosEliminados = signal<Usuario[]>([]);


  totalUsuariosActivos = computed(
    () => this.usuarios().length
  );

  totalUsuariosEliminados = computed(
    () => this.usuariosEliminados().length
  );


  // ==========================================
  // EDICIÓN Y MODAL
  // ==========================================

  editando = false;

  modalEditarAbierto = false;

  usuarioEditandoId?: number;


  usuarioFormulario: Usuario =
    this.crearUsuarioVacio();


  // ==========================================
  // CÓDIGO POSTAL
  // ==========================================

  cpLoading = false;

  cpError = '';

  colonias: string[] = [];


  // ==========================================
  // VALIDACIÓN DE EDAD
  // ==========================================

  fechaMaximaAdulto: string =
    this.obtenerFechaMaximaAdulto();


  constructor(
    private router: Router,
    private usuarioService: UsuarioService,
    private postaliaService: PostaliaService,
    private authService: AuthService,

  ) {}


  // ==========================================
  // AL INICIAR ADMIN
  // ==========================================

  ngOnInit(): void {

    this.cargarUsuarios();

    this.cargarUsuariosEliminados();
  }


  // ==========================================
  // CARGAR USUARIOS ACTIVOS
  // ==========================================

  cargarUsuarios(): void {

    // Reinicia tabla y contador
    this.usuarios.set([]);

    this.usuarioService
      .listarUsuarios()
      .pipe(

        timeout(10000),

        catchError(
          (error: HttpErrorResponse) => {

            console.error(
              'Error al cargar usuarios:',
              error
            );

            return of([]);
          }
        )

      )
      .subscribe(datos => {

        const usuariosOrdenados =
          [...datos].sort(
            (a, b) =>
              (a.id ?? 0) - (b.id ?? 0)
          );

        this.usuarios.set(
          usuariosOrdenados
        );

        console.log(
          'Usuarios activos:',
          this.usuarios()
        );

        console.log(
          'Total activos:',
          this.totalUsuariosActivos()
        );

      });
  }


  // ==========================================
  // CARGAR USUARIOS ELIMINADOS
  // ==========================================

  cargarUsuariosEliminados(): void {

    // Reinicia tabla y contador
    this.usuariosEliminados.set([]);

    this.usuarioService
      .listarUsuariosEliminados()

      .pipe(

        timeout(10000),

        catchError(
          (error: HttpErrorResponse) => {

            console.error(
              'Error al cargar usuarios eliminados:',
              error
            );

            return of([]);
          }
        )

      )
      .subscribe(datos => {

        const usuariosOrdenados =
          [...datos].sort(
            (a, b) =>
              (a.id ?? 0) - (b.id ?? 0)
          );

        this.usuariosEliminados.set(
          usuariosOrdenados
        );

        console.log(
          'Usuarios eliminados:',
          this.usuariosEliminados()
        );

        console.log(
          'Total eliminados:',
          this.totalUsuariosEliminados()
        );

      });
  }


  // ==========================================
  // GUARDAR
  // ==========================================

  guardarUsuario(): void {

    if (this.editando) {

      this.actualizarUsuario();

    } else {

      this.crearUsuario();

    }
  }


  // ==========================================
  // CREAR USUARIO
  // ==========================================

  crearUsuario(): void { //observables - rxjs

    // Validamos la edad antes de enviar a Spring
    if (!this.esMayorDeEdad(
      this.usuarioFormulario.fechaNacimiento
    )) {

      alert(
        'El usuario debe tener al menos 18 años.'
      );

      return;
    }


    this.usuarioService
      .crearUsuario(
        this.usuarioFormulario
      )
      .pipe(

        timeout(10000), //no

        catchError(
          (error: HttpErrorResponse) => {

            console.error(
              'Error al crear usuario:',
              error
            );

            alert(
              'No se pudo crear el usuario.'
            );

            return of(null);
          }
        )

      )
      .subscribe(respuesta => {

        if (!respuesta) {
          return; //personalizar error
        }

        alert(
          'Usuario creado correctamente.'
        );

        this.limpiarFormulario();

        this.cargarUsuarios();

      });
  }


  // ==========================================
  // ABRIR MODAL PARA EDITAR
  // ==========================================

  editarUsuario(
    usuario: Usuario
  ): void {

    this.editando = true;

    this.modalEditarAbierto = true;

    this.usuarioEditandoId =
      usuario.id;

    this.usuarioFormulario = {
      ...usuario
    };

    this.cpError = '';

    this.cpLoading = false;

    this.colonias = [];
  }


  // ==========================================
  // CERRAR MODAL
  // ==========================================

  cerrarModalEditar(): void {

    this.modalEditarAbierto = false;

    this.limpiarFormulario();
  }


  // ==========================================
  // ACTUALIZAR USUARIO
  // ==========================================

  actualizarUsuario(): void {

    if (
      this.usuarioEditandoId === undefined
    ) {
      return;
    }


    // Validamos nuevamente la edad
    if (!this.esMayorDeEdad(
      this.usuarioFormulario.fechaNacimiento
    )) {

      alert(
        'El usuario debe tener al menos 18 años.'
      );

      return;
    }


    this.usuarioService
      .actualizarUsuario(
        this.usuarioEditandoId,
        this.usuarioFormulario
      )
      .pipe(

        timeout(10000),

        catchError(
          (error: HttpErrorResponse) => {

            console.error(
              'Error al actualizar usuario:',
              error
            );

            alert(
              'No se pudo actualizar el usuario.'
            );

            return of(null);
          }
        )

      )
      .subscribe(respuesta => {

        if (!respuesta) {
          return;
        }

        alert(
          'Usuario actualizado correctamente.'
        );

        this.modalEditarAbierto = false;

        this.limpiarFormulario();

        this.cargarUsuarios();

      });
  }


  // ==========================================
  // ELIMINACIÓN LÓGICA
  // ==========================================

  eliminarUsuario(
    id: number
  ): void {

    const confirmar =
      confirm(
        '¿Quieres eliminar este usuario?'
      );

    if (!confirmar) {
      return;
    }

    this.usuarioService
      .eliminarUsuario(id)
      .pipe(

        timeout(10000),

        catchError(
          (error: HttpErrorResponse) => {

            console.error(
              'Error al eliminar usuario:',
              error
            );

            alert(
              'No se pudo eliminar el usuario.'
            );

            return of(null);
          }
        )

      )
      .subscribe(() => {

        alert(
          'Usuario eliminado correctamente.'
        );

        this.cargarUsuarios();

        this.cargarUsuariosEliminados();

      });
  }


  // ==========================================
  // BUSCAR CÓDIGO POSTAL
  // ==========================================

  buscarCodigoPostal(): void {

    this.cpError = '';

    this.colonias = [];

    const cp =
      this.usuarioFormulario
        .codigoPostal
        .trim();

    if (!/^\d{5}$/.test(cp)) {

      this.usuarioFormulario.estado = '';

      this.usuarioFormulario.municipio = '';

      return;
    }

    this.cpLoading = true;

    this.postaliaService
      .buscarCodigoPostal(cp)
      .pipe(

        timeout(10000),

        catchError(
          (error: HttpErrorResponse) => {

            console.error(
              'Error consultando código postal:',
              error
            );

            if (error.status === 404) {

              this.cpError =
                'Código postal no encontrado.';

            } else {

              this.cpError =
                'No se pudo consultar el código postal.';

            }

            this.usuarioFormulario.estado = '';

            this.usuarioFormulario.municipio = '';

            this.cpLoading = false;

            return of(null);
          }
        )

      )
      .subscribe(respuesta => {

        if (!respuesta) {
          return;
        }

        console.log(
          'Respuesta recibida de Spring:',
          respuesta
        );

        this.usuarioFormulario.estado =
          respuesta.estado;

        this.usuarioFormulario.municipio =
          respuesta.municipio;

        this.colonias =
          respuesta.colonias.map(
            colonia => colonia.nombre
          );

        this.cpLoading = false;

      });
  }


  // ==========================================
  // CANCELAR EDICIÓN
  // ==========================================

  cancelarEdicion(): void {

    this.cerrarModalEditar();
  }


  // ==========================================
  // LIMPIAR FORMULARIO
  // ==========================================

  limpiarFormulario(): void {

    this.editando = false;

    this.usuarioEditandoId =
      undefined;

    this.usuarioFormulario =
      this.crearUsuarioVacio();

    this.cpError = '';

    this.cpLoading = false;

    this.colonias = [];
  }


  // ==========================================
  // CERRAR SESIÓN
  // ==========================================

  cerrarSesion(): void {

    this.authService.cerrarSesion();

    this.router.navigate(['/']);

  }


  // ==========================================
  // CALCULAR FECHA MÁXIMA PARA +18
  // ==========================================

  private obtenerFechaMaximaAdulto(): string {

    const hoy = new Date();

    const fechaLimite = new Date(
      hoy.getFullYear() - 18,
      hoy.getMonth(),
      hoy.getDate()
    );

    const anio =
      fechaLimite.getFullYear();

    const mes =
      String(
        fechaLimite.getMonth() + 1
      ).padStart(2, '0');

    const dia =
      String(
        fechaLimite.getDate()
      ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }


  // ==========================================
  // COMPROBAR SI TIENE 18 AÑOS
  // ==========================================

  private esMayorDeEdad(
    fechaNacimiento: string
  ): boolean {

    if (!fechaNacimiento) {
      return false;
    }

    return (
      fechaNacimiento <=
      this.fechaMaximaAdulto
    );
  }


  // ==========================================
  // USUARIO VACÍO
  // ==========================================

  private crearUsuarioVacio(): Usuario {

    return {

      nombre: '',

      primerApellido: '',

      segundoApellido: '',

      telefono: '',

      codigoPostal: '',

      estado: '',

      municipio: '',

      direccion: '',

      fechaNacimiento: '',

      animalFavorito: '',

      activo: true

    };
  }

}