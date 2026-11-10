import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-background flex items-center justify-center p-4 sm:p-8 technical-grid">
      <div class="w-full max-w-[980px] grid lg:grid-cols-[1fr_420px] gap-8 items-center">
        <div class="hidden lg:block px-6">
          <div class="flex items-center mb-10">
            <img src="Logo.png" alt="Safyron Learning" class="w-64 h-24 object-contain object-left rounded-xl shadow-sm" />
          </div>
          <div class="eyebrow mb-3">Workforce readiness</div>
          <h1 class="text-4xl xl:text-5xl font-bold tracking-tight text-on-surface leading-tight">Build confidence through better training.</h1>
          <p class="mt-5 max-w-lg text-lg leading-8 text-on-surface-variant">A focused space for teams to learn, complete assessments, and keep every certification up to date.</p>
          <div class="mt-8 flex items-center gap-6 text-sm text-on-surface-variant">
            <span class="flex items-center gap-2"><span class="status-dot"></span> Secure workspace</span>
            <span class="flex items-center gap-2"><span class="material-symbols-outlined text-primary text-base">verified</span> Audit-ready records</span>
          </div>
        </div>

        <div class="w-full">
          <div class="flex items-center mb-6 lg:hidden">
            <img src="Logo.png" alt="Safyron Learning" class="w-52 h-20 object-contain object-left rounded-xl shadow-sm" />
          </div>

          <div class="panel overflow-hidden">
            <div class="px-6 py-5 border-b border-outline-variant">
              <div class="flex items-center justify-between">
                <div>
                  <h2 class="text-xl font-bold text-on-surface">Welcome back</h2>
                  <p class="text-sm text-on-surface-variant mt-1">Sign in to continue your learning journey.</p>
                </div>
                <span class="flex items-center gap-1.5 text-[11px] font-semibold text-tertiary"><span class="status-dot"></span> SECURE</span>
              </div>
            </div>

            <div class="p-6 space-y-5">
              <div class="bg-primary-container border border-primary/15 p-3.5 rounded-lg flex items-start gap-2.5">
                <span class="material-symbols-outlined text-primary shrink-0">info</span>
                <div class="text-xs leading-5 text-on-primary-container">Your learning activity is recorded securely to support compliance and professional development.</div>
              </div>

              <label class="block">
                <span class="text-xs font-semibold text-on-surface">Email address</span>
                <input [(ngModel)]="email" type="email" class="field-control mt-1.5 w-full text-on-surface px-3 py-2.5 placeholder:text-outline" placeholder="you@company.com" />
              </label>
              <label class="block">
                <span class="text-xs font-semibold text-on-surface">Password</span>
                <input [(ngModel)]="password" type="password" class="field-control mt-1.5 w-full text-on-surface px-3 py-2.5 placeholder:text-outline" placeholder="Enter your password" />
              </label>
              <p class="text-sm text-error" *ngIf="error"><span class="font-semibold">Unable to continue:</span> {{ error }}</p>
              <button (click)="submit()" [disabled]="loading"
                      class="w-full py-3 bg-primary hover:bg-on-primary-container text-white font-semibold rounded-lg transition-colors shadow-sm flex items-center justify-center gap-2 disabled:opacity-50">
                <span class="material-symbols-outlined text-base">login</span>
                {{ loading ? 'Signing in…' : 'Sign in securely' }}
              </button>
            </div>
          </div>
          <div class="mt-4 text-center text-xs text-on-surface-variant">Protected workspace · Your data is encrypted in transit</div>
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent {
  mode = 'login';
  email = '';
  password = '';
  loading = false;
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.loading = true;
    this.error = '';
    this.auth.login(this.email, this.password).subscribe({
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
