import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateUserRequest, ModuleRole, Role, UpdateUserRequest, User } from '../models';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/admin`;

  getUsers(page: number = 0, size: number = 10, sortBy: string = 'id', sortDir: string = 'asc', search?: string): Observable<PageResponse<User>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    
    if (search) {
      params = params.set('search', search);
    }
    
    return this.http.get<PageResponse<User>>(`${this.baseUrl}/users`, { params });
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users/all`);
  }

  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.baseUrl}/roles`);
  }

  getRolesByModule(): Observable<ModuleRole[]> {
    return this.http.get<ModuleRole[]>(`${this.baseUrl}/roles/by-module`);
  }

  createUser(payload: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/users`, payload);
  }

  updateUser(id: number, payload: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/users/${id}`, payload);
  }
}

