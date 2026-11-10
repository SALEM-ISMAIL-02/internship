import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-top-bar',
  template: `
    <header class="bg-surface sticky top-0 z-50 border-b border-outline-variant/80 shadow-[0_2px_12px_rgba(32,48,80,0.06)]">
      <div class="flex justify-between items-center w-full max-w-[1440px] mx-auto px-4 sm:px-6 lg:px-8 h-[4.5rem]">
        <!-- Brand -->
        <div class="flex items-center">
          <img src="Logo.png" alt="Safyron Learning" class="w-40 h-14 object-contain object-left rounded-xl shadow-sm" />
        </div>
        <!-- Role nav -->
        <nav class="hidden md:flex items-center gap-1 h-full bg-surface-container-low p-1 rounded-lg">
          <a routerLink="/learner"
             class="font-label-md flex items-center px-4 py-2 rounded-md transition-colors"
             [class.bg-primary-container]="active === 'learner'" [class.text-primary]="active === 'learner'"
             [class.text-on-surface-variant]="active !== 'learner'" [class.hover:text-on-surface]="active !== 'learner'">
            Learner
          </a>
          <a routerLink="/instructor"
             class="font-label-md flex items-center px-4 py-2 rounded-md transition-colors"
             [class.bg-primary-container]="active === 'instructor'" [class.text-primary]="active === 'instructor'"
             [class.text-on-surface-variant]="active !== 'instructor'" [class.hover:text-on-surface]="active !== 'instructor'">
            Instructor
          </a>
          <a routerLink="/admin"
             class="font-label-md flex items-center px-4 py-2 rounded-md transition-colors"
             [class.bg-primary-container]="active === 'admin'" [class.text-primary]="active === 'admin'"
             [class.text-on-surface-variant]="active !== 'admin'" [class.hover:text-on-surface]="active !== 'admin'">
            Admin
          </a>
        </nav>
        <!-- Profile -->
        <div class="flex items-center gap-3">
          <div class="relative">
            <div class="w-9 h-9 rounded-full bg-primary-container border border-primary/20 flex items-center justify-center font-label-md text-primary font-bold">
              {{ initials() }}
            </div>
            <span class="status-dot absolute bottom-0 right-0 ring-2 ring-surface"></span>
          </div>
          <div class="hidden xl:flex flex-col">
            <span class="text-body-sm text-on-surface leading-none font-semibold">{{ auth.user()?.name }}</span>
            <span class="text-label-sm text-tertiary leading-tight">{{ auth.user()?.role }}</span>
          </div>
          <button (click)="auth.logout()"
                  class="ml-1 px-3 py-2 bg-transparent border border-outline-variant rounded-lg font-label-sm text-on-surface-variant hover:bg-surface-container-low hover:text-on-surface transition-colors">
            Sign out
          </button>
        </div>
      </div>
    </header>
  `,
  imports: [RouterLink],
})
export class TopBarComponent {
  readonly auth = inject(AuthService);
  private router = inject(Router);
  active = '';

  ngOnInit() {
    this.router.events.subscribe(() => this.sync());
    this.sync();
  }

  private sync() {
    const url = this.router.url;
    this.active = url.includes('/instructor') ? 'instructor' : url.includes('/admin') ? 'admin' : 'learner';
  }

  initials(): string {
    const name = this.auth.user()?.name ?? '';
    return name.split(/\s+/).map((p) => p[0]).join('').slice(0, 2).toUpperCase() || '??';
  }
}
