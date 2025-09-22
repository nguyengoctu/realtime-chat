import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username: string = '';
  password: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  onSubmit() {
    if (this.username && this.password) {
      this.isLoading = true;
      this.errorMessage = '';

      this.authService.login({
        usernameOrEmail: this.username,
        password: this.password
      }).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            console.log('Login successful:', response.data?.user);
            this.router.navigate(['/home']);
          } else {
            this.errorMessage = response.message || 'Login failed';
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'An error occurred during login';
          console.error('Login error:', error);
        }
      });
    }
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
}
