import { Component, inject } from '@angular/core';
import { MdbCollapseModule } from 'mdb-angular-ui-kit/collapse';
import { RouterLink, RouterLinkActive } from "@angular/router";
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-menu',
  imports: [MdbCollapseModule, RouterLink, RouterLinkActive],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {

  authService = inject(AuthService);

  sair() {
    this.authService.logout();
  }
}
