import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
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
  private apiUrl = `${environment.apiUrl}/api/users`;

  constructor(private http: HttpClient) {}

  register(request: UserRegistrationRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.apiUrl}/register`, request);
  }

  login(request: UserLoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/login`, request);
  }

  getUserById(id: number): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.apiUrl}/${id}`);
  }

  searchUsers(keyword: string): Observable<ApiResponse<UserResponse[]>> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<ApiResponse<UserResponse[]>>(`${this.apiUrl}/search`, { params });
  }

  refreshToken(refreshToken: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.apiUrl}/refresh`, refreshToken, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }
}