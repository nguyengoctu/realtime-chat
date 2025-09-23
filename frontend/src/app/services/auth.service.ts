import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { UserService, UserLoginRequest, UserRegistrationRequest, AuthResponse, UserResponse } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserResponse | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);

  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private userService: UserService) {
    // Check if user is already logged in on service initialization
    this.checkAuthStatus();
  }

  register(userData: UserRegistrationRequest): Observable<any> {
    return this.userService.register(userData);
  }

  login(credentials: UserLoginRequest): Observable<any> {
    return this.userService.login(credentials).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setAuthData(response.data);
        }
      })
    );
  }

  logout(): Observable<any> {
    const refreshToken = this.getRefreshToken();

    // Clear local data first
    this.clearAuthData();

    // If we have refresh token, call logout API to revoke it (ignore errors)
    if (refreshToken) {
      return this.userService.logout(refreshToken).pipe(
        tap(() => console.log('Refresh token revoked successfully')),
        // Ignore errors - logout should always succeed locally
        catchError(() => {
          console.log('Failed to revoke refresh token, but logout still successful');
          return of(null);
        })
      );
    }

    // Return empty observable if no refresh token
    return of(null);
  }

  refreshToken(): Observable<any> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      throw new Error('No refresh token available');
    }

    return this.userService.refreshToken(refreshToken).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.setAuthData(response.data);
        }
      })
    );
  }

  private setAuthData(authResponse: AuthResponse): void {
    localStorage.setItem('accessToken', authResponse.accessToken);
    localStorage.setItem('refreshToken', authResponse.refreshToken);
    localStorage.setItem('currentUser', JSON.stringify(authResponse.user));

    this.currentUserSubject.next(authResponse.user);
    this.isAuthenticatedSubject.next(true);
  }

  private clearAuthData(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');

    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  private checkAuthStatus(): void {
    const token = this.getAccessToken();
    const user = this.getCurrentUser();

    if (token && user) {
      this.currentUserSubject.next(user);
      this.isAuthenticatedSubject.next(true);
    }
  }

  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  getCurrentUser(): UserResponse | null {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
  }

  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch (error) {
      return true;
    }
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    return token ? !this.isTokenExpired(token) : false;
  }
}