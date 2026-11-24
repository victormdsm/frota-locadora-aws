import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Só entra quem está logado
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (authService.isLogado()) {
    return true;
  }
  return inject(Router).createUrlTree(['/login']);
};

// Só entra ADMIN. USER é mandado para a lista de carros.
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  if (authService.isAdmin()) {
    return true;
  }
  return inject(Router).createUrlTree(['/sistema/carros']);
};
