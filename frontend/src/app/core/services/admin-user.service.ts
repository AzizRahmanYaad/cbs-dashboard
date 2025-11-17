import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateUserRequest, Role, UpdateUserRequest, User } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/admin`;

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.baseUrl}/roles`);
  }

  createUser(payload: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users`, payload);
  }

  updateUser(id: number, payload: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/users/${id}`, payload);
  }
}

