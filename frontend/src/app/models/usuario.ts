export type Role = 'ADMIN' | 'USER';

export class Usuario {
  id!: number;
  nome!: string;
  username!: string;
  senha?: string;
  role: Role = 'USER';
}

export interface LoginResponse {
  token: string;
  expiraEm: number;
  username: string;
  nome: string;
  role: Role;
}
