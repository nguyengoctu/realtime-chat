import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
}

export interface UserRegistrationRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

export interface UserLoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  avatarUrl?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateRequest {
  email?: string;
  fullName?: string;
  avatarUrl?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: UserResponse;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }

  private processUserResponse(user: UserResponse): UserResponse {
    // Convert relative avatar path to full URL
    if (user.avatarUrl && user.avatarUrl.startsWith('/storage/')) {
      const appUrl = (window as any)['env']?.['APP_URL'] || window.location.origin;
      user.avatarUrl = appUrl + user.avatarUrl;
    }
    return user;
  }

  register(request: UserRegistrationRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/auth/register`, request);
  }

  login(request: UserLoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/auth/login`, request);
  }

  getUserById(id: number): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.apiUrl}/users/${id}`).pipe(
      map(response => {
        if (response.success && response.data) {
          response.data = this.processUserResponse(response.data);
        }
        return response;
      })
    );
  }

  searchUsers(keyword: string): Observable<ApiResponse<UserResponse[]>> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<ApiResponse<UserResponse[]>>(`${this.apiUrl}/users/search`, { params }).pipe(
      map(response => {
        if (response.success && response.data) {
          response.data = response.data.map(user => this.processUserResponse(user));
        }
        return response;
      })
    );
  }

  refreshToken(refreshToken: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/auth/refresh`, refreshToken, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  logout(refreshToken: string): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.apiUrl}/auth/logout`, { refreshToken });
  }

  updateUser(id: number, request: UserUpdateRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<ApiResponse<UserResponse>>(`${this.apiUrl}/users/${id}`, request).pipe(
      map(response => {
        if (response.success && response.data) {
          response.data = this.processUserResponse(response.data);
        }
        return response;
      })
    );
  }
}