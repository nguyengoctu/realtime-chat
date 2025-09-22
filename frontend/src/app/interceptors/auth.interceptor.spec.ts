import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { AuthInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('AuthInterceptor', () => {
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getAccessToken', 'getRefreshToken', 'refreshToken', 'logout']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: HTTP_INTERCEPTORS,
          useClass: AuthInterceptor,
          multi: true
        },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should add Authorization header when access token exists', () => {
    authService.getAccessToken.and.returnValue('test-access-token');

    httpClient.get('/api/test').subscribe();

    const httpRequest = httpMock.expectOne('/api/test');
    expect(httpRequest.request.headers.get('Authorization')).toBe('Bearer test-access-token');
    httpRequest.flush({});
  });

  it('should not add Authorization header for auth endpoints', () => {
    authService.getAccessToken.and.returnValue('test-access-token');

    httpClient.post('/api/login', {}).subscribe();

    const httpRequest = httpMock.expectOne('/api/login');
    expect(httpRequest.request.headers.get('Authorization')).toBeNull();
    httpRequest.flush({});
  });

  it('should not add Authorization header when no access token', () => {
    authService.getAccessToken.and.returnValue(null);

    httpClient.get('/api/test').subscribe();

    const httpRequest = httpMock.expectOne('/api/test');
    expect(httpRequest.request.headers.get('Authorization')).toBeNull();
    httpRequest.flush({});
  });

  it('should refresh token on 401 error and retry request', () => {
    authService.getAccessToken.and.returnValue('expired-token');
    authService.getRefreshToken.and.returnValue('refresh-token');
    authService.refreshToken.and.returnValue(of({}));

    httpClient.get('/api/test').subscribe();

    const httpRequest = httpMock.expectOne('/api/test');
    expect(httpRequest.request.headers.get('Authorization')).toBe('Bearer expired-token');

    httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    authService.getAccessToken.and.returnValue('new-access-token');

    const retryRequest = httpMock.expectOne('/api/test');
    expect(retryRequest.request.headers.get('Authorization')).toBe('Bearer new-access-token');
    retryRequest.flush({ data: 'success' });
  });

  it('should logout user when refresh token fails', () => {
    authService.getAccessToken.and.returnValue('expired-token');
    authService.getRefreshToken.and.returnValue('refresh-token');
    authService.refreshToken.and.returnValue(throwError(() => new Error('Refresh failed')));

    httpClient.get('/api/test').subscribe({
      error: (error) => {
        expect(error.message).toBe('Refresh failed');
      }
    });

    const httpRequest = httpMock.expectOne('/api/test');
    httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.logout).toHaveBeenCalled();
  });

  it('should not attempt refresh for 401 on auth endpoints', () => {
    authService.getAccessToken.and.returnValue('test-token');
    authService.getRefreshToken.and.returnValue('refresh-token');

    httpClient.post('/api/login', {}).subscribe({
      error: () => {}
    });

    const httpRequest = httpMock.expectOne('/api/login');
    httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.refreshToken).not.toHaveBeenCalled();
    expect(authService.logout).not.toHaveBeenCalled();
  });

  it('should not attempt refresh when no refresh token available', () => {
    authService.getAccessToken.and.returnValue('expired-token');
    authService.getRefreshToken.and.returnValue(null);

    httpClient.get('/api/test').subscribe({
      error: () => {}
    });

    const httpRequest = httpMock.expectOne('/api/test');
    httpRequest.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authService.refreshToken).not.toHaveBeenCalled();
    expect(authService.logout).not.toHaveBeenCalled();
  });
});