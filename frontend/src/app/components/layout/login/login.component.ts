import { Component, inject } from '@angular/core';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { FormsModule } from "@angular/forms";
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [MdbFormsModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  usuario = '';
  senha = '';
  carregando = false;
  erro = '';

  router = inject(Router);
  authService = inject(AuthService);

  constructor() {
    if (this.authService.isLogado()) {
      this.irParaInicio();
    }
  }

  logar() {
    this.erro = '';
    if (!this.usuario || !this.senha) {
      this.erro = 'Preencha usuário e senha.';
      return;
    }
    this.carregando = true;
    this.authService.login(this.usuario, this.senha).subscribe({
      next: () => {
        this.carregando = false;
        this.irParaInicio();
      },
      error: (erro: HttpErrorResponse) => {
        this.carregando = false;
        this.erro = erro.status === 0
          ? 'Não foi possível conectar ao servidor.'
          : (erro.error?.mensagem ?? 'Usuário ou senha incorretos.');
      }
    });
  }

  private irParaInicio() {
    this.router.navigate([this.authService.isAdmin() ? '/sistema/dashboard' : '/sistema/carros']);
  }
}
