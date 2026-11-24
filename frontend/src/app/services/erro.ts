import { HttpErrorResponse } from '@angular/common/http';
import Swal from 'sweetalert2';

// Mostra a mensagem que o backend devolveu ({ status, mensagem })
export function mostrarErro(titulo: string, erro: HttpErrorResponse) {
  let texto = erro.error?.mensagem;
  if (!texto) {
    texto = erro.status === 0 ? 'Não foi possível conectar ao servidor.'
      : erro.status === 403 ? 'Você não tem permissão para esta ação.'
      : 'Ocorreu uma falha não identificada.';
  }
  Swal.fire({ icon: 'error', title: titulo, text: texto });
}

export function mostrarSucesso(titulo: string) {
  Swal.fire({ icon: 'success', title: titulo, timer: 1500, showConfirmButton: false });
}
