import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';
import { User, LoginRequest, LoginResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private tokenService = inject(TokenService);

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor() {
    if (this.tokenService.hasToken() && !this.tokenService.isTokenExpired()) {
      this.isAuthenticatedSubject.next(true);
      this.loadCurrentUser();
    }
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/api/auth/login`, credentials)
      .pipe(
        tap((response: LoginResponse) => {
          this.tokenService.saveTokens(
            response.accessToken,
            response.refreshToken,
            response.expiresIn
          );
          this.isAuthenticatedSubject.next(true);
          this.loadCurrentUser();
        }),
        catchError((error: HttpErrorResponse) => {
          console.error('Login error:', error);
          // Provide more specific error messages
          let errorMessage = 'An unknown error occurred';
          if (error instanceof HttpErrorResponse) {
            if (error.status === 401) {
              errorMessage = 'Invalid username or password. Please check your credentials.';
            } else if (error.status === 0) {
              errorMessage = 'Unable to connect to the server. Please check your connection and try again.';
            } else if (error.error && error.error.message) {
              errorMessage = error.error.message;
            } else {
              errorMessage = `Server error (${error.status}): ${error.statusText}`;
            }
          }
          return throwError(() => new Error(errorMessage));
        })
      );
  }

  logout(): void {
    this.tokenService.clearTokens();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.tokenService.getRefreshToken();
    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<LoginResponse>(`${environment.apiUrl}/api/auth/refresh`, { refreshToken })
      .pipe(
        tap((response: LoginResponse) => {
          this.tokenService.saveTokens(
            response.accessToken,
            response.refreshToken,
            response.expiresIn
          );
        }),
        catchError((error: HttpErrorResponse) => {
          this.logout();
          return throwError(() => error);
        })
      );
  }

  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/api/auth/me`)
      .pipe(
        tap((user: User) => this.currentUserSubject.next(user)),
        catchError((error: HttpErrorResponse) => {
          console.error('Get current user error:', error);
          return throwError(() => error);
        })
      );
  }

  private loadCurrentUser(): void {
    this.getCurrentUser().subscribe({
      error: () => {
        this.logout();
      }
    });
  }

  isAuthenticated(): boolean {
    return this.tokenService.hasToken() && !this.tokenService.isTokenExpired();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }
}