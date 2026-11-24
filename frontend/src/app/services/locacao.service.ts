import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Locacao, LocacaoRequest } from '../models/locacao';

@Injectable({
  providedIn: 'root'
})
export class LocacaoService {

  API = `${environment.apiUrl}/locacoes`;

  http = inject(HttpClient);

  // ADMIN
  listAll(): Observable<Locacao[]> {
    return this.http.get<Locacao[]>(this.API);
  }

  // usuário logado
  listMinhas(): Observable<Locacao[]> {
    return this.http.get<Locacao[]>(`${this.API}/minhas`);
  }

  locar(request: LocacaoRequest): Observable<Locacao> {
    return this.http.post<Locacao>(this.API, request);
  }

  devolver(id: number): Observable<Locacao> {
    return this.http.put<Locacao>(`${this.API}/${id}/devolver`, {});
  }

  cancelar(id: number): Observable<Locacao> {
    return this.http.put<Locacao>(`${this.API}/${id}/cancelar`, {});
  }
}
