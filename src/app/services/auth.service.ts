import {
  Inject,
  Injectable,
  PLATFORM_ID
} from '@angular/core';

import {
  isPlatformBrowser
} from '@angular/common';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(
    @Inject(PLATFORM_ID)
    private platformId: Object
  ) {}


  iniciarSesion(): void {

    if (
      isPlatformBrowser(
        this.platformId
      )
    ) {
      localStorage.setItem(
        'adminSesion',
        'true'
      );
    }
  }


  haySesion(): boolean {

    if (
      !isPlatformBrowser(
        this.platformId
      )
    ) {
      return false;
    }

    return (
      localStorage.getItem(
        'adminSesion'
      ) === 'true'
    );
  }


  cerrarSesion(): void {

    if (
      isPlatformBrowser(
        this.platformId
      )
    ) {
      localStorage.removeItem(
        'adminSesion'
      );
    }
  }

}