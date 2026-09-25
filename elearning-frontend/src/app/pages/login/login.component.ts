import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="auth-wrap">
      <div class="card">
        <h2>E-Learning Platform</h2>
        <div class="tabs">
          <button [class.active]="mode === 'login'" (click)="mode = 'login'">Login</button>
          <button [class.active]="mode === 'register'" (click)="mode = 'register'">Register</button>
        </div>

        <label>Name (register only)</label>
        <input [(ngModel)]="name" placeholder="Your name" />

        <label>Email</label>
        <input [(ngModel)]="email" type="email" placeholder="you@example.com" />

        <label>Password</label>
        <input [(ngModel)]="password" type="password" placeholder="Password" />

        <label>Role (register only)</label>
        <select [(ngModel)]="role">
          <option value="LEARNER">Learner</option>
          <option value="INSTRUCTOR">Instructor</option>
          <option value="ADMIN">Admin</option>
        </select>

        <button class="primary" (click)="submit()" [disabled]="loading">
          {{ loading ? 'Please wait…' : mode === 'login' ? 'Login' : 'Create account' }}
        </button>
        <p class="error" *ngIf="error">{{ error }}</p>
      </div>
    </div>
  `,
  styles: [
    `
      .auth-wrap { display: flex; justify-content: center; padding-top: 8vh; }
      .card { width: 340px; padding: 24px; border: 1px solid #ddd; border-radius: 10px;
              box-shadow: 0 4px 14px rgba(0,0,0,.08); display: flex; flex-direction: column; gap: 8px; }
      input, select { padding: 8px; border: 1px solid #ccc; border-radius: 6px; }
      .tabs { display: flex; gap: 8px; }
      .tabs button { flex: 1; padding: 8px; border: 1px solid #ccc; background: #f5f5f5; border-radius: 6px; cursor: pointer; }
      .tabs button.active { background: #3f51b5; color: #fff; }
      .primary { padding: 10px; background: #3f51b5; color: #fff; border: none; border-radius: 6px; cursor: pointer; }
      .error { color: #b00020; }
      label { font-size: 12px; color: #666; }
    `,
  ],
})
export class LoginComponent {
  mode: 'login' | 'register' = 'login';
  name = '';
  email = '';
  password = '';
  role = 'LEARNER';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading = true;
    this.error = '';
    const req =
      this.mode === 'login'
        ? this.auth.login(this.email, this.password)
        : this.auth.register(this.name, this.email, this.password, this.role);
    req.subscribe({
      next: (u) => {
        this.loading = false;
        this.router.navigate(['/' + u.role.toLowerCase()]);
      },
      error: (e) => {
        this.loading = false;
        this.error = e.error?.message || e.error || 'Login failed';
      },
    });
  }
}
