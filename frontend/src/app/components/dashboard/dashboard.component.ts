import { Component, inject } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Carro } from '../../models/carro';
import { Locacao } from '../../models/locacao';
import { CarroService } from '../../services/carro.service';
import { LocacaoService } from '../../services/locacao.service';
import { AuthService } from '../../services/auth.service';
import { mostrarErro } from '../../services/erro';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {

  carros: Carro[] = [];
  locacoes: Locacao[] = [];

  carroService = inject(CarroService);
  locacaoService = inject(LocacaoService);
  authService = inject(AuthService);

  ngOnInit() {
    forkJoin({ carros: this.carroService.listAll(), locacoes: this.locacaoService.listAll() }).subscribe({
      next: ({ carros, locacoes }) => {
        this.carros = carros;
        this.locacoes = locacoes;
      },
      error: (erro) => mostrarErro('Falha para carregar o painel', erro)
    });
  }

  contar(status: string) {
    return this.carros.filter(c => c.status === status).length;
  }

  get ativas(): Locacao[] {
    return this.locacoes.filter(l => l.status === 'ATIVA');
  }

  get ocupacao(): number {
    return this.carros.length ? Math.round(this.contar('LOCADO') * 100 / this.carros.length) : 0;
  }

  get faturamento(): number {
    return this.locacoes.filter(l => l.status === 'FINALIZADA').reduce((t, l) => t + l.valorTotal, 0);
  }

  get previsto(): number {
    return this.ativas.reduce((t, l) => t + l.valorTotal, 0);
  }
}
