import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import type { HttpInterceptorFn } from '@angular/common/http';

import { routes } from './app.routes';

const authInterceptor: HttpInterceptorFn = (req, next) => {
  const raw = localStorage.getItem('user');
  let user: { email?: string; role?: string } | null = null;
  if (raw) {
    try {
      user = JSON.parse(raw);
    } catch {
      localStorage.removeItem('user');
    }
  }
  const headers: Record<string, string> = {};
  if (user?.email) headers['X-User-Email'] = user.email;
  if (user?.role) headers['X-User-Role'] = user.role;
  return next(Object.keys(headers).length ? req.clone({ setHeaders: headers }) : req);
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
