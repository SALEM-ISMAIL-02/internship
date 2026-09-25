import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import type { HttpInterceptorFn } from '@angular/common/http';

import { routes } from './app.routes';

const authInterceptor: HttpInterceptorFn = (req, next) => {
  const raw = localStorage.getItem('user');
  const token = raw ? JSON.parse(raw).token : null;
  const clone = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(clone);
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};


