import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile {
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  goToHome() {
    this.router.navigate(['/home']);
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
