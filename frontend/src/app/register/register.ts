import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, CommonModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  username: string = '';
  email: string = '';
  password: string = '';
  confirmPassword: string = '';
  fullName: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  onSubmit() {
    if (this.isFormValid()) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.authService.register({
        username: this.username,
        email: this.email,
        password: this.password,
        fullName: this.fullName
      }).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.success) {
            this.successMessage = 'Account created successfully! You can now sign in.';
            console.log('Registration successful:', response.data);
            // Auto redirect to login after 2 seconds
            setTimeout(() => {
              this.router.navigate(['/login']);
            }, 2000);
          } else {
            this.errorMessage = response.message || 'Registration failed';
          }
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'An error occurred during registration';
          console.error('Registration error:', error);
        }
      });
    }
  }

  isFormValid(): boolean {
    return !!(this.username &&
              this.email &&
              this.password &&
              this.confirmPassword &&
              this.password === this.confirmPassword &&
              this.isValidEmail(this.email));
  }

  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  passwordsMatch(): boolean {
    return this.password === this.confirmPassword;
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}