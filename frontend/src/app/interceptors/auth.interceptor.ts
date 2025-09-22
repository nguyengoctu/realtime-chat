import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Add access token to requests
    const accessToken = this.authService.getAccessToken();

    if (accessToken && !this.isAuthEndpoint(req.url)) {
      req = this.addTokenToRequest(req, accessToken);
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // If 401 and we have a refresh token, try to refresh
        if (error.status === 401 && this.authService.getRefreshToken() && !this.isAuthEndpoint(req.url)) {
          return this.handle401Error(req, next);
        }

        return throwError(error);
      })
    );
  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return this.authService.refreshToken().pipe(
      switchMap(() => {
        // Retry the original request with new token
        const newToken = this.authService.getAccessToken();
        if (newToken) {
          req = this.addTokenToRequest(req, newToken);
        }
        return next.handle(req);
      }),
      catchError((refreshError) => {
        // Refresh failed, logout user
        this.authService.logout();
        return throwError(refreshError);
      })
    );
  }

  private addTokenToRequest(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private isAuthEndpoint(url: string): boolean {
    return url.includes('/login') || url.includes('/register') || url.includes('/refresh');
  }
}