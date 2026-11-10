import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, FormsModule, TopBarComponent],
  template: `
    <app-top-bar />
    <div class="app-page max-w-[1440px] mx-auto px-4 sm:px-6 lg:px-8 py-6 technical-grid">
      <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div>
          <h1 class="font-headline-md text-on-surface font-bold tracking-tight">Governance Console</h1>
          <p class="font-label-sm text-on-surface-variant mt-1">User registry &amp; training module oversight // REGULATORY RECONCILIATION: OK</p>
        </div>
        <span class="flex items-center gap-1.5 px-2.5 py-1 bg-surface-container border border-outline-variant rounded font-label-sm text-tertiary">
          <span class="w-2 h-2 rounded-full bg-tertiary animate-pulse"></span>
          TELEMETRY NODE // ONLINE
        </span>
      </div>

      <section class="bg-surface border border-outline-variant rounded-xl p-4 mb-5">
        <h2 class="font-label-sm font-bold uppercase tracking-wider mb-3">Create operator account</h2>
        <form (ngSubmit)="createUser()" class="grid grid-cols-1 sm:grid-cols-5 gap-2">
          <input [(ngModel)]="newUser.name" name="newName" required placeholder="Name" class="input" />
          <input [(ngModel)]="newUser.email" name="newEmail" required type="email" placeholder="Email" class="input" />
          <input [(ngModel)]="newUser.password" name="newPassword" required type="password" placeholder="Temporary password" class="input" />
          <select [(ngModel)]="newUser.role" name="newRole" class="input">
            <option value="LEARNER">Learner</option>
            <option value="INSTRUCTOR">Instructor</option>
            <option value="ADMIN">Admin</option>
          </select>
          <button class="py-2 bg-secondary-container text-white rounded font-label-sm font-bold">Add user</button>
        </form>
      </section>

      <section class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
        <div class="panel p-4 border-l-4 border-primary"><div class="text-label-sm text-outline">USERS</div><div class="text-2xl font-bold text-primary">{{ users().length }}</div></div>
        <div class="panel p-4 border-l-4 border-secondary"><div class="text-label-sm text-outline">MODULES</div><div class="text-2xl font-bold text-secondary">{{ courses().length }}</div></div>
        <div class="panel p-4 border-l-4 border-tertiary"><div class="text-label-sm text-outline">SUBMISSIONS</div><div class="text-2xl font-bold text-tertiary">{{ reviews().length }}</div></div>
        <div class="panel p-4 border-l-4 border-error"><div class="text-label-sm text-outline">PENDING REVIEW</div><div class="text-2xl font-bold text-error">{{ pendingReviews() }}</div></div>
      </section>

      <section class="panel p-5 mb-5">
        <div class="flex items-center justify-between mb-3">
          <div><h2 class="font-headline-sm font-bold text-on-surface">Assessment submissions by module</h2><p class="text-body-sm text-on-surface-variant">Every learner submission with its review status and date.</p></div>
          <span class="material-symbols-outlined text-primary text-2xl">analytics</span>
        </div>
        <div *ngFor="let group of reviewGroups()" class="border-t border-outline-variant/50 py-3">
          <div class="flex justify-between items-center"><span class="font-semibold text-on-surface">{{ group.module }}</span><span class="font-label-sm text-outline">{{ group.items.length }} SUBMISSIONS</span></div>
          <div class="mt-2 grid gap-1">
            <div *ngFor="let item of group.items" class="flex flex-wrap justify-between gap-2 text-body-sm bg-surface-container-low rounded px-3 py-2">
              <span>{{ item.learner?.name }} · {{ item.quiz?.courseTitle }}</span>
              <span class="text-on-surface-variant">{{ item.date | date:'medium' }} · <strong [class.text-tertiary]="item.approved" [class.text-error]="!item.approved">{{ item.approved ? 'APPROVED' : 'PENDING' }}</strong></span>
            </div>
          </div>
        </div>
        <p *ngIf="reviews().length === 0" class="text-body-sm text-outline">No quiz submissions yet.</p>
      </section>

      <div class="grid grid-cols-1 xl:grid-cols-2 gap-5">
        <section class="bg-surface border border-outline-variant rounded-xl overflow-hidden shadow-[0_6px_20px_rgba(32,48,80,0.05)]">
          <div class="bg-surface-container-high px-4 py-2.5 border-b border-outline-variant flex items-center justify-between">
            <span class="font-label-sm text-on-surface font-bold uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-base text-secondary">badge</span>
              Operator Registry
            </span>
            <span class="font-label-sm text-outline">{{ users().length }} RECORDS</span>
          </div>
          <table class="w-full text-body-sm">
            <thead>
              <tr class="text-label-sm text-outline border-b border-outline-variant bg-surface-container-lowest">
                <th class="text-left px-4 py-2">ID</th>
                <th class="text-left px-4 py-2">NAME</th>
                <th class="text-left px-4 py-2">EMAIL</th>
                <th class="text-left px-4 py-2">ROLE</th>
                <th class="px-4 py-2"></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let u of users()" class="border-b border-outline-variant/40 hover:bg-surface-container-low transition-colors">
                <td class="px-4 py-2 font-mono text-outline">{{ u.id }}</td>
                <td class="px-4 py-2 text-on-surface font-semibold">{{ u.name }}</td>
                <td class="px-4 py-2 text-on-surface-variant">{{ u.email }}</td>
                <td class="px-4 py-2">
                  <span class="px-2 py-0.5 rounded border font-label-sm text-[10px] border-outline-variant"
                        [class.text-tertiary]="u.role === 'ADMIN'"
                        [class.text-secondary]="u.role === 'INSTRUCTOR'"
                        [class.text-primary]="u.role === 'LEARNER'">
                    {{ u.role }}
                  </span>
                </td>
                <td class="px-4 py-2 text-right">
                  <button (click)="delete(u.id)" title="Revoke access"
                          class="p-1.5 text-on-surface-variant hover:text-error rounded transition-colors">
                    <span class="material-symbols-outlined text-sm">delete</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <p *ngIf="users().length === 0" class="text-center text-on-surface-variant font-label-md py-6">No operators registered.</p>
        </section>

        <section class="bg-surface border border-outline-variant rounded-xl overflow-hidden shadow-[0_6px_20px_rgba(32,48,80,0.05)]">
          <div class="bg-surface-container-high px-4 py-2.5 border-b border-outline-variant flex items-center justify-between">
            <span class="font-label-sm text-on-surface font-bold uppercase tracking-wider flex items-center gap-1.5">
              <span class="material-symbols-outlined text-base text-secondary">menu_book</span>
              Training Module Registry
            </span>
            <span class="font-label-sm text-outline">{{ courses().length }} MODULES</span>
          </div>
          <table class="w-full text-body-sm">
            <thead>
              <tr class="text-label-sm text-outline border-b border-outline-variant bg-surface-container-lowest">
                <th class="text-left px-4 py-2">ID</th>
                <th class="text-left px-4 py-2">TITLE</th>
                <th class="text-left px-4 py-2">CATEGORY</th>
                <th class="text-left px-4 py-2">PRIORITY</th>
                <th class="text-left px-4 py-2">STATUS</th>
                <th class="px-4 py-2"></th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of courses()" class="border-b border-outline-variant/40 hover:bg-surface-container-low transition-colors">
                <td class="px-4 py-2 font-mono text-outline">{{ c.id }}</td>
                <td class="px-4 py-2 text-on-surface font-semibold">{{ c.title }}</td>
                <td class="px-4 py-2 text-on-surface-variant">{{ c.category }}</td>
                <td class="px-4 py-2">
                  <span class="px-2 py-0.5 rounded border font-label-sm text-[10px]"
                        [class.bg-error-container]="c.priority === 'MANDATORY'"
                        [class.text-on-error-container]="c.priority === 'MANDATORY'"
                        [class.border-error-container]="c.priority === 'MANDATORY'"
                        [class.text-on-surface-variant]="c.priority !== 'MANDATORY'"
                        [class.border-outline-variant]="c.priority !== 'MANDATORY'">
                    {{ c.priority || 'NORMAL' }}
                  </span>
                </td>
                <td class="px-4 py-2 text-on-surface-variant">{{ c.status }}</td>
                <td class="px-4 py-2 text-right">
                  <button (click)="deleteCourse(c.id!)" title="Archive module"
                          class="p-1.5 text-on-surface-variant hover:text-error rounded transition-colors">
                    <span class="material-symbols-outlined text-sm">delete</span>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
          <p *ngIf="courses().length === 0" class="text-center text-on-surface-variant font-label-md py-6">No modules registered.</p>
        </section>
      </div>

      <div class="mt-6 p-2 border border-outline-variant/60 rounded bg-surface-container-lowest font-mono text-[10px] text-outline leading-tight space-y-0.5">
        <div>ENCRYPTION: AES-256 GCM // CERT-NODE: TUN-IND-994</div>
        <div>SESSION: 0x9FA84210B // REGULATORY RECONCILIATION: OK</div>
      </div>
    </div>
  `,
})
export class AdminDashboardComponent implements OnInit {
  private api = inject(ApiService);
  readonly auth = inject(AuthService);

  users = signal<any[]>([]);
  courses = signal<any[]>([]);
  quizzes = signal<any[]>([]);
  materials = signal<any[]>([]);
  reviews = signal<any[]>([]);
  newUser = { name: '', email: '', password: '', role: 'LEARNER' };

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.api.listUsers().subscribe((u) => this.users.set(u));
    this.api.listCourses().subscribe((c) => this.courses.set(c));
    this.api.listCourses().subscribe((courses) => {
      courses.forEach((course) => this.api.quizzesByCourse(course.id!).subscribe((quizzes) =>
        this.quizzes.update((current) => [...current.filter((q) => q.course?.id !== course.id), ...quizzes])));
      courses.forEach((course) => this.api.materialsByCourse(course.id!).subscribe((materials) =>
        this.materials.update((current) => [...current.filter((m) => m.course?.id !== course.id), ...materials])));
    });
    this.api.reviewResults().subscribe((results) => this.reviews.set(results));
  }

  delete(id: number) {
    this.api.deleteUser(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('User deletion failed: ' + (e.error?.error ?? e.message)),
    });
  }

  pendingReviews(): number {
    return this.reviews().filter((review) => !review.approved).length;
  }

  reviewGroups(): Array<{ module: string; items: any[] }> {
    const groups = new Map<string, any[]>();
    for (const review of this.reviews()) {
      const module = review.quiz?.courseTitle ?? 'Unknown module';
      groups.set(module, [...(groups.get(module) ?? []), review]);
    }
    return Array.from(groups, ([module, items]) => ({ module, items }));
  }

  deleteCourse(id: number) {
    this.api.deleteCourse(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Module deletion failed: ' + (e.error?.error ?? e.message)),
    });
  }

  quizzesOf(courses: any[]): any[] {
    const ids = new Set(courses.map((course) => course.id));
    return this.quizzes().filter((quiz) => ids.has(quiz.course?.id));
  }

  deleteQuiz(id: number) {
    if (!confirm('Delete this quiz permanently?')) return;
    this.api.deleteQuiz(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Quiz deletion failed: ' + (e.error?.error ?? e.message)),
    });
  }

  createUser() {
    this.api.createUser(this.newUser).subscribe({
      next: () => {
        this.newUser = { name: '', email: '', password: '', role: 'LEARNER' };
        this.reload();
      },
      error: (e) => alert('User creation failed: ' + (e.error?.message ?? e.message)),
    });
  }

  materialsOf(courses: any[]): any[] {
    const ids = new Set(courses.map((course) => course.id));
    return this.materials().filter((material) => ids.has(material.course?.id));
  }

  deleteMaterial(id: number) {
    if (!confirm('Delete this material permanently?')) return;
    this.api.deleteMaterial(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Material deletion failed: ' + (e.error?.error ?? e.message)),
    });
  }
}
