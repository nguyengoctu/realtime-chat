import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UploadResponse {
  success: boolean;
  message?: string;
  data?: string; // Relative path like "/storage/avatars/uuid.jpg"
}

@Injectable({
  providedIn: 'root'
})
export class UploadService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }

  uploadAvatar(file: File): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<UploadResponse>(`${this.apiUrl}/upload/avatar`, formData);
  }

  uploadFile(file: File, bucketName: string): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bucketName', bucketName);

    return this.http.post<UploadResponse>(`${this.apiUrl}/upload`, formData);
  }
}