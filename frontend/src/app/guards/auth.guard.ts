import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if user has valid access token
  if (authService.isAuthenticated()) {
    return true;
  }

  // Check if user has refresh token to get new access token
  const refreshToken = authService.getRefreshToken();
  if (!refreshToken) {
    router.navigate(['/login']);
    return false;
  }

  // Try to refresh access token
  return authService.refreshToken().pipe(
    map(() => {
      // Refresh successful, allow access
      return true;
    }),
    catchError(() => {
      // Refresh failed, redirect to login
      authService.logout().subscribe();
      router.navigate(['/login']);
      return of(false);
    })
  );
};