import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../models';

export interface SignatureStatus {
  hasSignature: boolean;
  signatureData?: string;
}

export interface UpdateProfilePayload {
  fullName: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/users/me`;

  getSignatureStatus(): Observable<SignatureStatus> {
    return this.http.get<SignatureStatus>(`${this.baseUrl}/signature`);
  }

  saveSignature(signatureData: string): Observable<{ success: boolean }> {
    return this.http.put<{ success: boolean }>(`${this.baseUrl}/signature`, { signatureData });
  }

  updateProfile(payload: UpdateProfilePayload): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/profile`, payload);
  }

  changePassword(payload: ChangePasswordPayload): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.baseUrl}/password`, payload);
  }
}
