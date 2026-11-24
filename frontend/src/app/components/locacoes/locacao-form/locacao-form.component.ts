import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Carro } from '../../../models/carro';
import { Usuario } from '../../../models/usuario';
import { LocacaoRequest } from '../../../models/locacao';

function hojeISO(diasAMais = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + diasAMais);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

@Component({
  selector: 'app-locacao-form',
  imports: [FormsModule, CurrencyPipe],
  templateUrl: './locacao-form.component.html'
})
export class LocacaoFormComponent {

  @Input() carro!: Carro;
  @Input() usuarios: Usuario[] = [];
  @Input() admin = false;

  @Output() confirmar = new EventEmitter<LocacaoRequest>();

  hoje = hojeISO();
  dataInicio = hojeISO();
  dataFimPrevista = hojeISO(3);
  usuarioId?: number;

  get dias(): number {
    const ms = new Date(this.dataFimPrevista).getTime() - new Date(this.dataInicio).getTime();
    return Math.max(1, Math.round(ms / 86400000));
  }

  get datasValidas(): boolean {
    return !!this.dataInicio && !!this.dataFimPrevista && this.dataFimPrevista >= this.dataInicio;
  }

  get valorEstimado(): number {
    return this.dias * (this.carro?.valorDiaria ?? 0);
  }

  enviar() {
    this.confirmar.emit({
      carroId: this.carro.id,
      dataInicio: this.dataInicio,
      dataFimPrevista: this.dataFimPrevista,
      usuarioId: this.admin ? this.usuarioId : undefined
    });
  }
}
