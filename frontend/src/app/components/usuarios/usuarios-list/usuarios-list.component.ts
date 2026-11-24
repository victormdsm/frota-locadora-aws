import { Component, inject, TemplateRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MdbModalModule, MdbModalRef, MdbModalService } from 'mdb-angular-ui-kit/modal';
import Swal from 'sweetalert2';
import { Usuario } from '../../../models/usuario';
import { UsuarioService } from '../../../services/usuario.service';
import { AuthService } from '../../../services/auth.service';
import { mostrarErro, mostrarSucesso } from '../../../services/erro';

@Component({
  selector: 'app-usuarios-list',
  imports: [FormsModule, MdbModalModule],
  templateUrl: './usuarios-list.component.html'
})
export class UsuariosListComponent {

  @ViewChild('modalUsuario') modalUsuario!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  lista: Usuario[] = [];
  usuarioEdit: Usuario = new Usuario();

  usuarioService = inject(UsuarioService);
  authService = inject(AuthService);

  constructor(private modalService: MdbModalService) {
  }

  ngOnInit() {
    this.findAll();
  }

  findAll() {
    this.usuarioService.listAll().subscribe({
      next: (lista) => this.lista = lista,
      error: (erro) => mostrarErro('Falha para carregar os usuários', erro)
    });
  }

  novo() {
    this.usuarioEdit = new Usuario();
    this.modalRef = this.modalService.open(this.modalUsuario);
  }

  editar(usuario: Usuario) {
    this.usuarioEdit = Object.assign({}, usuario, { senha: '' });
    this.modalRef = this.modalService.open(this.modalUsuario);
  }

  salvar() {
    const requisicao = this.usuarioEdit.id
      ? this.usuarioService.update(this.usuarioEdit.id, this.usuarioEdit)
      : this.usuarioService.create(this.usuarioEdit);

    requisicao.subscribe({
      next: () => {
        this.modalRef.close();
        mostrarSucesso('Usuário salvo!');
        this.findAll();
      },
      error: (erro) => mostrarErro('Falha ao salvar o usuário', erro)
    });
  }

  deletar(usuario: Usuario) {
    Swal.fire({
      title: `Excluir o usuário ${usuario.username}?`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Excluir',
      confirmButtonColor: '#dc4c64'
    }).then(r => {
      if (r.isConfirmed) {
        this.usuarioService.delete(usuario.id).subscribe({
          next: () => {
            mostrarSucesso('Usuário excluído!');
            this.findAll();
          },
          error: (erro) => mostrarErro('Não foi possível excluir', erro)
        });
      }
    });
  }
}
