import { Component, inject, TemplateRef, ViewChild } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MdbModalModule, MdbModalRef, MdbModalService } from 'mdb-angular-ui-kit/modal';
import Swal from 'sweetalert2';
import { Carro } from '../../../models/carro';
import { Marca } from '../../../models/marca';
import { Usuario } from '../../../models/usuario';
import { LocacaoRequest } from '../../../models/locacao';
import { CarrosDetailsComponent } from '../carros-details/carros-details.component';
import { LocacaoFormComponent } from '../../locacoes/locacao-form/locacao-form.component';
import { CarroService } from '../../../services/carro.service';
import { MarcaService } from '../../../services/marca.service';
import { UsuarioService } from '../../../services/usuario.service';
import { LocacaoService } from '../../../services/locacao.service';
import { AuthService } from '../../../services/auth.service';
import { mostrarErro, mostrarSucesso } from '../../../services/erro';
import { StatusBadgePipe, StatusLabelPipe } from '../../../pipes/status.pipe';

@Component({
  selector: 'app-carros-list',
  imports: [MdbModalModule, CarrosDetailsComponent, LocacaoFormComponent, CurrencyPipe, FormsModule,
            StatusBadgePipe, StatusLabelPipe],
  templateUrl: './carros-list.component.html',
  styleUrl: './carros-list.component.scss'
})
export class CarrosListComponent {

  @ViewChild('modalCarroDetails') modalCarroDetails!: TemplateRef<any>;
  @ViewChild('modalLocar') modalLocar!: TemplateRef<any>;
  modalRef!: MdbModalRef<any>;

  lista: Carro[] = [];
  marcas: Marca[] = [];
  usuarios: Usuario[] = [];
  carroEdit: Carro = new Carro();
  carroLocar!: Carro;
  filtro: 'TODOS' | 'DISPONIVEL' | 'LOCADO' | 'MANUTENCAO' = 'TODOS';
  busca = '';
  carregando = true;

  carroService = inject(CarroService);
  marcaService = inject(MarcaService);
  usuarioService = inject(UsuarioService);
  locacaoService = inject(LocacaoService);
  authService = inject(AuthService);
  router = inject(Router);

  constructor(private modalService: MdbModalService) {
  }

  ngOnInit() {
    // USER começa vendo só os carros disponíveis
    this.filtro = this.authService.isAdmin() ? 'TODOS' : 'DISPONIVEL';
    this.findAll();
    if (this.authService.isAdmin()) {
      this.marcaService.listAll().subscribe(m => this.marcas = m);
      this.usuarioService.listAll().subscribe(u => this.usuarios = u);
    }
  }

  get listaFiltrada(): Carro[] {
    const termo = this.busca.trim().toLowerCase();
    return this.lista.filter(c =>
      (this.filtro === 'TODOS' || c.status === this.filtro) &&
      (!termo || `${c.modelo} ${c.placa} ${c.marca?.nome}`.toLowerCase().includes(termo))
    );
  }

  contar(status: string): number {
    return this.lista.filter(c => c.status === status).length;
  }

  findAll() {
    this.carregando = true;
    this.carroService.listAll().subscribe({
      next: (listaRetornada) => {
        this.lista = listaRetornada;
        this.carregando = false;
      },
      error: (erro) => {
        this.carregando = false;
        mostrarErro('Falha para carregar a frota', erro);
      }
    })
  }

  novoCarro() {
    this.carroEdit = new Carro();
    if (this.marcas.length) {
      this.carroEdit.marca = this.marcas[0];
    }
    this.modalRef = this.modalService.open(this.modalCarroDetails, { modalClass: 'modal-lg' });
  }

  editarCarro(carro:Carro){
    this.carroEdit = Object.assign({}, carro);
    this.modalRef = this.modalService.open(this.modalCarroDetails, { modalClass: 'modal-lg' });
  }

  deletar(carro: Carro) {
    Swal.fire({
      title: `Excluir ${carro.modelo} (${carro.placa})?`,
      icon: 'warning',
      showCancelButton: true,
      cancelButtonText: "Cancelar",
      confirmButtonText: "Excluir",
      confirmButtonColor: '#dc4c64'
    }).then((result) => {
      if(result.isConfirmed) {
        this.carroService.delete(carro.id).subscribe({
          next: () => {
            mostrarSucesso('Excluído com sucesso!');
            this.findAll();
          },
          error: (erro) => mostrarErro('Não foi possível excluir', erro)
        });
      }
    })
  }

  retornoDetalhe(carro: Carro) {
    const requisicao = carro.id > 0
      ? this.carroService.update(carro.id, carro)
      : this.carroService.create(carro);

    requisicao.subscribe({
      next: () => {
        this.modalRef.close();
        mostrarSucesso(carro.id > 0 ? 'Editado com sucesso!' : 'Salvo com sucesso!');
        this.findAll();
      },
      error: (erro: HttpErrorResponse) => mostrarErro('Falha ao salvar o veículo', erro)
    });
  }

  abrirLocacao(carro: Carro) {
    this.carroLocar = carro;
    this.modalRef = this.modalService.open(this.modalLocar);
  }

  confirmarLocacao(request: LocacaoRequest) {
    this.locacaoService.locar(request).subscribe({
      next: () => {
        this.modalRef.close();
        mostrarSucesso('Locação registrada!');
        this.router.navigate(['/sistema/locacoes']);
      },
      error: (erro) => mostrarErro('Não foi possível locar', erro)
    });
  }
}
