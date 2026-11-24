import { Component, inject } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { Locacao } from '../../../models/locacao';
import { LocacaoService } from '../../../services/locacao.service';
import { AuthService } from '../../../services/auth.service';
import { mostrarErro, mostrarSucesso } from '../../../services/erro';
import { StatusBadgePipe, StatusLabelPipe } from '../../../pipes/status.pipe';

@Component({
  selector: 'app-locacoes-list',
  imports: [CurrencyPipe, DatePipe, RouterLink, StatusBadgePipe, StatusLabelPipe],
  templateUrl: './locacoes-list.component.html'
})
export class LocacoesListComponent {

  lista: Locacao[] = [];
  carregando = true;

  locacaoService = inject(LocacaoService);
  authService = inject(AuthService);

  ngOnInit() {
    this.findAll();
  }

  findAll() {
    this.carregando = true;
    // ADMIN vê todas; USER só as dele (o backend também garante isso)
    const requisicao = this.authService.isAdmin()
      ? this.locacaoService.listAll()
      : this.locacaoService.listMinhas();

    requisicao.subscribe({
      next: (lista) => {
        this.lista = lista;
        this.carregando = false;
      },
      error: (erro) => {
        this.carregando = false;
        mostrarErro('Falha para carregar as locações', erro);
      }
    });
  }

  devolver(locacao: Locacao) {
    Swal.fire({
      title: `Devolver ${locacao.carro.modelo}?`,
      text: 'O valor final é recalculado pela data de hoje (mínimo 1 diária).',
      icon: 'question',
      showCancelButton: true,
      cancelButtonText: 'Voltar',
      confirmButtonText: 'Devolver'
    }).then(r => {
      if (r.isConfirmed) {
        this.locacaoService.devolver(locacao.id).subscribe({
          next: () => {
            mostrarSucesso('Veículo devolvido!');
            this.findAll();
          },
          error: (erro) => mostrarErro('Não foi possível devolver', erro)
        });
      }
    });
  }

  cancelar(locacao: Locacao) {
    Swal.fire({
      title: `Cancelar a locação #${locacao.id}?`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Voltar',
      confirmButtonText: 'Cancelar locação',
      confirmButtonColor: '#dc4c64'
    }).then(r => {
      if (r.isConfirmed) {
        this.locacaoService.cancelar(locacao.id).subscribe({
          next: () => {
            mostrarSucesso('Locação cancelada.');
            this.findAll();
          },
          error: (erro) => mostrarErro('Não foi possível cancelar', erro)
        });
      }
    });
  }
}
