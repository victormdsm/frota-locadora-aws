import { Pipe, PipeTransform } from '@angular/core';

// Converte o status vindo da API em classe de badge do MDB
@Pipe({ name: 'statusBadge' })
export class StatusBadgePipe implements PipeTransform {
  transform(status: string): string {
    switch (status) {
      case 'DISPONIVEL':
      case 'FINALIZADA':
        return 'badge badge-success';
      case 'LOCADO':
      case 'ATIVA':
        return 'badge badge-primary';
      case 'MANUTENCAO':
        return 'badge badge-warning';
      case 'CANCELADA':
        return 'badge badge-secondary';
      default:
        return 'badge badge-light';
    }
  }
}

@Pipe({ name: 'statusLabel' })
export class StatusLabelPipe implements PipeTransform {
  transform(status: string): string {
    const nomes: Record<string, string> = {
      DISPONIVEL: 'Disponível', LOCADO: 'Locado', MANUTENCAO: 'Manutenção',
      ATIVA: 'Ativa', FINALIZADA: 'Finalizada', CANCELADA: 'Cancelada'
    };
    return nomes[status] ?? status;
  }
}
