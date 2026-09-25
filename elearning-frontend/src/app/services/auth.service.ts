import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

export interface AuthUser {
  id: number;
  name: string;
  email: string;
  role: 'LEARNER' | 'INSTRUCTOR' | 'ADMIN';
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<AuthUser | null>(this.restore());

  constructor(private http: HttpClient, private router: Router) {}

  private restore(): AuthUser | null {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  }

  login(email: string, password: string) {
    return this.http
      .post<AuthUser>('/api/auth/login', { email, password })
      .pipe(tap((u) => this.save(u)));
  }

  register(name: string, email: string, password: string, role: string) {
    return this.http
      .post<AuthUser>('/api/auth/register', { name, email, password, role })
      .pipe(tap((u) => this.save(u)));
  }

  private save(u: AuthUser) {
    localStorage.setItem('user', JSON.stringify(u));
    this.user.set(u);
  }

  logout() {
    localStorage.removeItem('user');
    this.user.set(null);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return this.user()?.token ?? null;
  }
}
