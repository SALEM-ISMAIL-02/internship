import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'learner',
    canActivate: [roleGuard('LEARNER', 'ADMIN')],
    loadComponent: () =>
      import('./pages/learner-dashboard/learner-dashboard.component').then(
        (m) => m.LearnerDashboardComponent
      ),
  },
  {
    path: 'instructor',
    canActivate: [roleGuard('INSTRUCTOR', 'ADMIN')],
    loadComponent: () =>
      import('./pages/instructor-dashboard/instructor-dashboard.component').then(
        (m) => m.InstructorDashboardComponent
      ),
  },
  {
    path: 'admin',
    canActivate: [roleGuard('ADMIN')],
    loadComponent: () =>
      import('./pages/admin-dashboard/admin-dashboard.component').then(
        (m) => m.AdminDashboardComponent
      ),
  },
  { path: '**', redirectTo: 'login' },
];

