import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, Role } from '../models/usuario';

const CHAVE = 'frota_sessao';

// Guarda o token JWT e os dados do usuário logado (localStorage)
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  http = inject(HttpClient);
  router = inject(Router);

  login(username: string, senha: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, { username, senha }).pipe(
      tap(resposta => localStorage.setItem(CHAVE, JSON.stringify(resposta)))
    );
  }

  logout() {
    localStorage.removeItem(CHAVE);
    this.router.navigate(['/login']);
  }

  get sessao(): LoginResponse | null {
    const dados = localStorage.getItem(CHAVE);
    return dados ? JSON.parse(dados) : null;
  }

  get token(): string | null {
    return this.sessao?.token ?? null;
  }

  get role(): Role | null {
    return this.sessao?.role ?? null;
  }

  isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  // Logado = tem token e ele ainda não expirou (lê o "exp" do payload do JWT)
  isLogado(): boolean {
    const token = this.token;
    if (!token) {
      return false;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }
}
