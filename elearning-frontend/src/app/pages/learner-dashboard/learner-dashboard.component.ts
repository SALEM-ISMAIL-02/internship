import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Course, LearnerProgress, Quiz, QuizResult } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-learner-dashboard',
  imports: [CommonModule, FormsModule],
  template: `
    <header>
      <h2>Learner Dashboard</h2>
      <span>Welcome, {{ auth.user()?.name }}</span>
      <button (click)="auth.logout()">Logout</button>
    </header>

    <section>
      <h3>Browse Courses</h3>
      <div class="grid">
        <div class="course-card" *ngFor="let c of courses()">
          <h4>
            {{ c.title }}
            <span class="badge mandatory" *ngIf="c.priority === 'MANDATORY'">MANDATORY</span>
          </h4>
          <p>{{ c.description }}</p>
          <small>Category: {{ c.category }} | Status: {{ c.status }}</small>

          <div class="progress-bar">
            <div class="fill" [style.width.%]="progressFor(c.id)"></div>
          </div>
          <small>{{ progressFor(c.id) }}% complete</small>

          <div class="actions">
            <button (click)="markProgress(c.id!, progressFor(c.id) + 25)">+25% progress</button>
            <button (click)="openQuiz(c.id!)" [disabled]="!hasPublishedQuiz(c.id!)">Take quiz</button>
          </div>
        </div>
      </div>
    </section>

    <section *ngIf="activeQuiz()">
      <h3>Quiz: {{ activeCourseTitle() }}</h3>
      <div *ngFor="let q of activeQuiz()!.questions; let i = index">
        <p><b>{{ i + 1 }}. {{ q.text }}</b></p>
        <label *ngFor="let opt of q.options; let j = index">
          <input type="radio" [name]="'q' + i" (change)="answers[i] = j" /> {{ opt }}
        </label>
      </div>
      <button class="primary" (click)="submitQuiz()">Submit quiz</button>
    </section>

    <section>
      <h3>My Quiz Results</h3>
      <ul>
        <li *ngFor="let r of results()">
          Score: {{ r.score }}% — {{ r.date | date: 'short' }}
        </li>
      </ul>
    </section>
  `,
  styles: [
    `
      header { display: flex; align-items: center; gap: 16px; padding: 12px 20px; background: #3f51b5; color: #fff; }
      header button { margin-left: auto; }
      section { padding: 16px 20px; }
      .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
      .course-card { border: 1px solid #ddd; border-radius: 8px; padding: 12px; }
      .badge.mandatory { background: #b00020; color: #fff; font-size: 10px; padding: 2px 6px; border-radius: 4px; }
      .progress-bar { height: 8px; background: #eee; border-radius: 4px; overflow: hidden; }
      .fill { height: 100%; background: #4caf50; }
      .actions { display: flex; gap: 8px; margin-top: 8px; }
      button { padding: 6px 10px; cursor: pointer; }
      .primary { background: #3f51b5; color: #fff; border: none; border-radius: 6px; }
    `,
  ],
})
export class LearnerDashboardComponent implements OnInit {
  private api = inject(ApiService);
  readonly auth = inject(AuthService);

  courses = signal<Course[]>([]);
  progress = signal<LearnerProgress[]>([]);
  results = signal<QuizResult[]>([]);
  quizzesByCourseId = signal<Map<number, Quiz[]>>(new Map());
  activeQuiz = signal<Quiz | null>(null);
  activeCourseTitle = signal('');
  answers: Record<number, number> = {};

  ngOnInit() {
    const uid = this.auth.user()!.id;
    this.api.listCourses().subscribe((c) => this.courses.set(c));
    this.api.myProgress(uid).subscribe((p) => this.progress.set(p));
    this.api.myResults(uid).subscribe((r) => this.results.set(r));
    this.api.listCourses().subscribe((courses) =>
      courses.forEach((c) =>
        this.api.quizzesByCourse(c.id!).subscribe((qs) => {
          const map = new Map(this.quizzesByCourseId());
          map.set(c.id!, qs);
          this.quizzesByCourseId.set(map);
        })
      )
    );
  }

  progressFor(courseId?: number): number {
    return this.progress().find((p) => p.course?.id === courseId)?.progress ?? 0;
  }

  hasPublishedQuiz(courseId?: number): boolean {
    return (this.quizzesByCourseId().get(courseId!) ?? []).some((q) => q.published);
  }

  markProgress(courseId: number, value: number) {
    const uid = this.auth.user()!.id;
    this.api.updateProgress(uid, courseId, Math.min(100, value)).subscribe((p) => {
      this.progress.update((list) => [...list.filter((x) => x.course?.id !== courseId), p]);
    });
  }

  openQuiz(courseId: number) {
    const quiz = (this.quizzesByCourseId().get(courseId) ?? []).find((q) => q.published);
    if (!quiz?.id) return;
    this.answers = {};
    this.api.takeQuiz(quiz.id).subscribe((q) => {
      this.activeQuiz.set(q);
      this.activeCourseTitle.set(
        this.courses().find((c) => c.id === courseId)?.title ?? ''
      );
    });
  }

  submitQuiz() {
    const quiz = this.activeQuiz();
    if (!quiz?.id) return;
    this.api.submitQuiz(quiz.id, this.auth.user()!.id, this.answers).subscribe((r) => {
      alert('You scored ' + r.score + '%');
      this.activeQuiz.set(null);
      const uid = this.auth.user()!.id;
      this.api.myResults(uid).subscribe((res) => this.results.set(res));
      this.api.myProgress(uid).subscribe((p) => this.progress.set(p));
    });
  }
}
