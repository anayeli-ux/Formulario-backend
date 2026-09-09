import { Routes } from '@angular/router';

import {
  InicioComponent
} from './inicio/inicio';

import {
  Admin
} from './admin/admin';

import {
  Registro
} from './registro/registro';

import {
  Exito
} from './exito/exito';


import {
  adminGuard
} from './guards/admin.guard';


export const routes: Routes = [

  {
    path: '',
    component: InicioComponent
  },

  {
    path: 'registro',
    component: Registro
  },

  {
    path: 'admin',
    component: Admin,
    canActivate: [adminGuard]
  },

  {
    path: 'exito',
    component: Exito
  },

  {
    path: '**',
    redirectTo: ''
  }

];