import { HttpInterceptorFn } from '@angular/common/http';
import { inject, Injector } from '@angular/core';
import { TokenService } from '../services/token.service';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const injector = inject(Injector);

  if (req.url.includes('/api/auth/login') || req.url.includes('/api/auth/refresh')) {
    return next(req);
  }

  const token = tokenService.getAccessToken();

  if (token) {
    if (tokenService.isTokenExpired()) {
      const authService = injector.get(AuthService);
      return authService.refreshToken().pipe(
        switchMap(() => {
          const newToken = tokenService.getAccessToken();
          const clonedReq = req.clone({
            setHeaders: {
              Authorization: `Bearer ${newToken}`
            }
          });
          return next(clonedReq);
        }),
        catchError(error => {
          authService.logout();
          return throwError(() => error);
        })
      );
    }

    const clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedReq);
  }

  return next(req);
};
