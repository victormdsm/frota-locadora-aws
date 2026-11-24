import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MdbFormsModule } from 'mdb-angular-ui-kit/forms';
import { Carro } from '../../../models/carro';
import { Marca } from '../../../models/marca';

@Component({
  selector: 'app-carros-details',
  imports: [MdbFormsModule, FormsModule],
  templateUrl: './carros-details.component.html',
  styleUrl: './carros-details.component.scss'
})
export class CarrosDetailsComponent {

  @Input() carro: Carro = new Carro();
  @Input() marcas: Marca[] = [];

  @Output() retorno = new EventEmitter<Carro>();

  // compara marcas pelo id no <select>
  compararMarca = (a: Marca, b: Marca) => a?.id === b?.id;

  salvar(){
    this.retorno.emit(this.carro);
  }

}
