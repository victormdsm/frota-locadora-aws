import { Routes } from '@angular/router';
import { LoginComponent } from './components/layout/login/login.component';
import { PrincipalComponent } from './components/layout/principal/principal.component';
import { CarrosListComponent } from './components/carros/carros-list/carros-list.component';
import { MarcasListComponent } from './components/marcas/marcas-list/marcas-list.component';
import { LocacoesListComponent } from './components/locacoes/locacoes-list/locacoes-list.component';
import { UsuariosListComponent } from './components/usuarios/usuarios-list/usuarios-list.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { adminGuard, authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {path:'', redirectTo:'login', pathMatch:'full'},
  {path:'login', component: LoginComponent},
  {
    path:'sistema',
    component: PrincipalComponent,
    canActivate: [authGuard],          // precisa estar logado
    children: [
      {path: '', redirectTo: 'carros', pathMatch: 'full'},
      {path: 'carros', component: CarrosListComponent},
      {path: 'locacoes', component: LocacoesListComponent},
      // só ADMIN
      {path: 'dashboard', component: DashboardComponent, canActivate: [adminGuard]},
      {path: 'marcas', component: MarcasListComponent, canActivate: [adminGuard]},
      {path: 'usuarios', component: UsuariosListComponent, canActivate: [adminGuard]},
    ]
  },
  {path: '**', redirectTo: 'login'}
];
