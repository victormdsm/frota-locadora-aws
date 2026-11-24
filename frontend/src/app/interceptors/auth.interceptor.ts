import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

// Coloca "Authorization: Bearer <token>" em toda requisição.
// Se o backend responder 401 (token ausente/expirado), volta para o login.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.token;

  if (token && !req.url.endsWith('/auth/login')) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && !req.url.endsWith('/auth/login')) {
        authService.logout();
      }
      return throwError(() => erro);
    })
  );
};
