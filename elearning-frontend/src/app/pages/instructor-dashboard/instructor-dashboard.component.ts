import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Course, Material, Quiz } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-instructor-dashboard',
  imports: [CommonModule, FormsModule],
  template: `
    <header>
      <h2>Instructor Dashboard</h2>
      <span>{{ auth.user()?.name }}</span>
      <button (click)="auth.logout()">Logout</button>
    </header>

    <section>
      <h3>Create Course</h3>
      <form (ngSubmit)="createCourse()" class="row">
        <input [(ngModel)]="newCourse.title" name="title" placeholder="Title" required />
        <input [(ngModel)]="newCourse.category" name="category" placeholder="Category" />
        <select [(ngModel)]="newCourse.priority" name="priority">
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="MANDATORY">Mandatory</option>
        </select>
        <input [(ngModel)]="newCourse.description" name="description" placeholder="Description" />
        <button class="primary" type="submit">Create</button>
      </form>
    </section>

    <section>
      <h3>My Courses — materials & quizzes</h3>
      <div class="course-card" *ngFor="let c of courses()">
        <h4>{{ c.title }} <small>({{ c.status }})</small></h4>

        <input type="file" (change)="upload($event, c.id!)" />
        <ul>
          <li *ngFor="let m of materialsOf(c.id!)">{{ m.originalName }} ({{ m.type }})</li>
        </ul>

        <button (click)="generate(c.id!)" [disabled]="!hasMaterial(c.id!) || generating">
          {{ generating ? 'Generating…' : 'Generate AI quiz' }}
        </button>

        <div *ngFor="let q of quizzesOf(c.id!)">
          Quiz #{{ q.id }} — {{ q.aiGenerated ? 'AI-generated' : 'manual' }},
          {{ q.published ? 'published' : 'draft' }}
          <button *ngIf="!q.published" (click)="publish(q)">Review & publish</button>
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      header { display: flex; align-items: center; gap: 16px; padding: 12px 20px; background: #2e7d32; color: #fff; }
      header button { margin-left: auto; }
      section { padding: 16px 20px; }
      .row { display: flex; gap: 8px; flex-wrap: wrap; }
      input, select { padding: 8px; }
      .course-card { border: 1px solid #ddd; border-radius: 8px; padding: 12px; margin-bottom: 12px; }
      .primary { background: #2e7d32; color: #fff; border: none; border-radius: 6px; padding: 8px 12px; cursor: pointer; }
      button { cursor: pointer; }
    `,
  ],
})
export class InstructorDashboardComponent implements OnInit {
  private api = inject(ApiService);
  readonly auth = inject(AuthService);

  courses = signal<Course[]>([]);
  materials = signal<Map<number, Material[]>>(new Map());
  quizzes = signal<Map<number, Quiz[]>>(new Map());
  generating = false;
  newCourse: Course = { title: '', priority: 'MEDIUM', status: 'PUBLISHED' };

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.api.listCourses().subscribe((courses) => {
      this.courses.set(courses);
      courses.forEach((c) => {
        this.api.materialsByCourse(c.id!).subscribe((m) =>
          this.materials.update((map) => new Map(map).set(c.id!, m))
        );
        this.api.quizzesByCourse(c.id!).subscribe((q) =>
          this.quizzes.update((map) => new Map(map).set(c.id!, q))
        );
      });
    });
  }

  createCourse() {
    if (!this.newCourse.title) return;
    this.api.createCourse({ ...this.newCourse }).subscribe(() => {
      this.newCourse = { title: '', priority: 'MEDIUM', status: 'PUBLISHED' };
      this.reload();
    });
  }

  upload(event: Event, courseId: number) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.api.uploadMaterial(courseId, input.files[0]).subscribe(() => this.reload());
  }

  materialsOf(courseId: number): Material[] {
    return this.materials().get(courseId) ?? [];
  }
  quizzesOf(courseId: number): Quiz[] {
    return this.quizzes().get(courseId) ?? [];
  }
  hasMaterial(courseId: number): boolean {
    return this.materialsOf(courseId).length > 0;
  }

  generate(courseId: number) {
    const mats = this.materialsOf(courseId);
    if (!mats.length) return;
    this.generating = true;
    this.api
      .generateQuiz(mats[mats.length - 1].id!, 5, this.auth.user()!.id)
      .subscribe({
        next: () => {
          this.generating = false;
          this.reload();
        },
        error: (e) => {
          this.generating = false;
          alert('AI generation failed: ' + (e.error?.message || e.message));
        },
      });
  }

  publish(quiz: Quiz) {
    this.api.publishQuiz(quiz.id!, quiz).subscribe(() => this.reload());
  }
}
