import { Carro } from './carro';
import { Usuario } from './usuario';

export type StatusLocacao = 'ATIVA' | 'FINALIZADA' | 'CANCELADA';

export interface Locacao {
  id: number;
  carro: Carro;
  usuario: Usuario;
  dataInicio: string;
  dataFimPrevista: string;
  dataDevolucao: string | null;
  valorTotal: number;
  status: StatusLocacao;
}

export interface LocacaoRequest {
  carroId: number;
  dataInicio: string;
  dataFimPrevista: string;
  usuarioId?: number;
}
