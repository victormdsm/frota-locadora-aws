import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Marca } from '../models/marca';

@Injectable({
  providedIn: 'root'
})
export class MarcaService {

  API = `${environment.apiUrl}/marcas`;

  http = inject(HttpClient);

  listAll(): Observable<Marca[]> {
    return this.http.get<Marca[]>(this.API);
  }

  create(marca: Marca): Observable<Marca> {
    return this.http.post<Marca>(this.API, marca);
  }

  update(id: number, marca: Marca): Observable<Marca> {
    return this.http.put<Marca>(`${this.API}/${id}`, marca);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.API}/${id}`);
  }
}
