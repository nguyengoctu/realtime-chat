import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-home',
  imports: [],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  goToProfile() {
    this.router.navigate(['/profile']);
  }

  logout() {
    this.authService.logout().subscribe({
      complete: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        // Even if logout API fails, still redirect to login since local data is cleared
        console.error('Logout API error:', error);
        this.router.navigate(['/login']);
      }
    });
  }
}
