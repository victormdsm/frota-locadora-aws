import { Marca } from './marca';

export type StatusCarro = 'DISPONIVEL' | 'LOCADO' | 'MANUTENCAO';

export class Carro {
  id!: number;
  modelo!: string;
  placa!: string;
  ano!: number;
  valorDiaria!: number;
  status: StatusCarro = 'DISPONIVEL';
  marca: Marca = new Marca();
}
