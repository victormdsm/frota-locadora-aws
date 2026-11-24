import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';
import { Marca } from '../../../models/marca';
import { MarcaService } from '../../../services/marca.service';
import { mostrarErro, mostrarSucesso } from '../../../services/erro';

@Component({
  selector: 'app-marcas-list',
  imports: [FormsModule],
  templateUrl: './marcas-list.component.html'
})
export class MarcasListComponent {

  lista: Marca[] = [];
  marcaEdit: Marca = new Marca();

  marcaService = inject(MarcaService);

  ngOnInit() {
    this.findAll();
  }

  findAll() {
    this.marcaService.listAll().subscribe({
      next: (lista) => this.lista = lista,
      error: (erro) => mostrarErro('Falha para carregar as marcas', erro)
    });
  }

  editar(marca: Marca) {
    this.marcaEdit = Object.assign({}, marca);
  }

  limpar() {
    this.marcaEdit = new Marca();
  }

  salvar() {
    if (!this.marcaEdit.nome?.trim()) {
      return;
    }
    const requisicao = this.marcaEdit.id
      ? this.marcaService.update(this.marcaEdit.id, this.marcaEdit)
      : this.marcaService.create(this.marcaEdit);

    requisicao.subscribe({
      next: () => {
        mostrarSucesso('Marca salva!');
        this.limpar();
        this.findAll();
      },
      error: (erro) => mostrarErro('Falha ao salvar a marca', erro)
    });
  }

  deletar(marca: Marca) {
    Swal.fire({
      title: `Excluir a marca ${marca.nome}?`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: 'Cancelar',
      confirmButtonText: 'Excluir',
      confirmButtonColor: '#dc4c64'
    }).then(r => {
      if (r.isConfirmed) {
        this.marcaService.delete(marca.id).subscribe({
          next: () => {
            mostrarSucesso('Marca excluída!');
            this.findAll();
          },
          error: (erro) => mostrarErro('Não foi possível excluir', erro)
        });
      }
    });
  }
}
