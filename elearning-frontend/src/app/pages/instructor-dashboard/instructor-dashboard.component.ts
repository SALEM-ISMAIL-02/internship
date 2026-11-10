import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Course, Material, Quiz, Question } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-instructor-dashboard',
  imports: [CommonModule, FormsModule, TopBarComponent],
  template: `
    <app-top-bar />
    <!-- Context strip -->
    <div class="bg-surface border-b border-outline-variant px-4 sm:px-6 lg:px-8 py-3 flex flex-wrap justify-between items-center text-xs">
      <div class="flex items-center gap-space-md">
        <div class="flex items-center gap-1.5 font-label-sm text-on-surface-variant">
          <span class="material-symbols-outlined text-secondary text-base">architecture</span>
          <span class="text-on-surface font-semibold">INSTRUCTOR STUDIO</span>
          <span>/</span>
          <span class="text-secondary">COURSE MANAGEMENT &amp; AI ASSESSMENT SYNTHESIS</span>
        </div>
      </div>
      <span class="text-tertiary flex items-center gap-1 font-label-sm">
        <span class="material-symbols-outlined text-xs">lock</span> AUDIT READY
      </span>
    </div>

    <div class="app-page max-w-[1440px] mx-auto px-4 sm:px-6 lg:px-8 py-6 blueprint-grid">
      <h1 class="font-headline-md text-on-surface font-bold tracking-tight mb-1">Training Modules &amp; AI Quiz Studio</h1>
      <p class="font-label-sm text-on-surface-variant mb-4">Create courses, ingest source documents, and synthesize verified assessments.</p>

      <section class="panel p-5 rounded-xl space-y-3 mb-6">
        <div class="flex items-center justify-between">
          <span class="font-label-sm uppercase tracking-wider text-primary font-bold">Learner submissions awaiting approval</span>
          <button (click)="loadReviews()" class="text-label-sm text-secondary">Refresh</button>
        </div>
        <div *ngFor="let r of reviewResults()" class="border-t border-outline-variant/50 pt-2 text-body-sm">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <span>{{ r.learner?.name }} — {{ r.quiz?.courseTitle }} — Quiz #{{ r.quiz?.id }} — <strong>{{ r.score }}%</strong></span>
            <div class="flex gap-2">
              <button (click)="toggleReview(r.id)" class="px-3 py-1 border border-outline-variant rounded text-label-sm font-bold">
                {{ isReviewExpanded(r.id) ? 'Hide details' : 'See details' }}
              </button>
              <button *ngIf="!r.approved" (click)="approveResult(r.id)" class="px-3 py-1 bg-tertiary-container text-on-tertiary-container rounded text-label-sm font-bold">Approve answers</button>
              <button *ngIf="r.approved" (click)="unapproveResult(r.id)" class="px-3 py-1 bg-error-container text-on-error-container rounded text-label-sm font-bold">Remove approval</button>
            </div>
          </div>
          <div *ngIf="isReviewExpanded(r.id)" class="mt-2 grid gap-2">
            <div *ngFor="let q of r.questions" class="p-2 bg-surface-container-low rounded border border-outline-variant/50">
              <div class="font-semibold">{{ q.text }}</div>
              <div class="text-label-sm text-outline">
                Learner answer: {{ answerLabel(r, q) }}
                <span class="text-tertiary"> | Correct: {{ q.options[q.correctAnswer] }}</span>
              </div>
            </div>
          </div>
        </div>
        <p *ngIf="reviewResults().length === 0" class="text-label-sm text-outline">No submissions yet.</p>
      </section>

      <!-- Create course panel -->
      <section class="panel p-5 rounded-xl space-y-4 mb-6 max-w-4xl">
        <div class="flex items-center justify-between border-b border-outline-variant/60 pb-2">
          <span class="font-label-sm uppercase tracking-wider text-primary flex items-center gap-1.5 font-bold">
            <span class="material-symbols-outlined text-base">menu_book</span>
            New Training Module
          </span>
        </div>
        <form (ngSubmit)="createCourse()" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <input [(ngModel)]="newCourse.title" name="title" placeholder="Module title (e.g. Electrical Lockout Procedures)" required
                 class="bg-surface-container-lowest text-on-surface font-body-md px-3 py-2.5 rounded border border-outline-variant focus:outline-none focus:border-secondary-container placeholder:text-outline" />
          <input [(ngModel)]="newCourse.category" name="category" placeholder="Category (e.g. HAZOP & Risk Studies)"
                 class="bg-surface-container-lowest text-on-surface font-body-md px-3 py-2.5 rounded border border-outline-variant focus:outline-none focus:border-secondary-container placeholder:text-outline" />
          <select [(ngModel)]="newCourse.priority" name="priority"
                  class="bg-surface-container-lowest text-on-surface font-body-md px-3 py-2.5 rounded border border-outline-variant focus:outline-none focus:border-secondary-container">
            <option value="LOW">LOW priority</option>
            <option value="MEDIUM">NORMAL priority</option>
            <option value="HIGH">HIGH priority</option>
            <option value="MANDATORY">MANDATORY // compliance required</option>
          </select>
          <input [(ngModel)]="newCourse.description" name="description" placeholder="Description"
                 class="bg-surface-container-lowest text-on-surface font-body-md px-3 py-2.5 rounded border border-outline-variant focus:outline-none focus:border-secondary-container placeholder:text-outline" />
          <button class="sm:col-span-2 py-2.5 bg-secondary-container hover:brightness-110 text-white font-label-md uppercase tracking-wider font-bold rounded transition-all shadow-md flex items-center justify-center gap-2 active:scale-[0.99]" type="submit">
            <span class="material-symbols-outlined text-base">add_box</span>
            Create Module
          </button>
        </form>
      </section>

      <!-- Module cards -->
      <section class="space-y-space-lg">
        <article *ngFor="let c of courses()" class="bg-surface border border-outline-variant rounded-xl overflow-hidden shadow-[0_6px_20px_rgba(32,48,80,0.05)]">
          <div class="bg-surface-container-high px-4 py-2.5 border-b border-outline-variant flex flex-wrap justify-between items-center gap-2">
            <div class="flex items-center gap-3">
              <span class="px-2 py-0.5 bg-primary-container text-primary border border-outline-variant rounded font-label-sm font-bold">
                MOD-{{ pad(c.id) }} // {{ c.category || 'GENERAL' }}
              </span>
              <span class="text-label-sm" [class.text-error]="c.priority === 'MANDATORY'" [class.text-outline]="c.priority !== 'MANDATORY'">
                {{ c.priority || 'NORMAL' }} PRIORITY
              </span>
              <button *ngIf="c.id" (click)="deleteCourse(c.id)" title="Delete module"
                      class="p-1 text-on-surface-variant hover:text-error rounded">
                <span class="material-symbols-outlined text-sm">delete</span>
              </button>
              <!-- STATE: course lifecycle transitions (illegal ones rejected by the backend) -->
              <select [value]="c.status ?? 'DRAFT'" (change)="changeStatus(c, $any($event.target).value)"
                      title="Course lifecycle (State pattern)"
                      class="text-label-sm font-bold bg-surface-container-lowest border border-outline-variant rounded px-1.5 py-0.5 focus:outline-none focus:border-secondary-container">
                <option value="DRAFT">DRAFT</option>
                <option value="PUBLISHED">PUBLISHED</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </select>
            </div>
            <span class="text-label-sm text-outline">{{ c.status || 'PUBLISHED' }}</span>
          </div>

          <div class="p-space-md space-y-4">
            <h2 class="text-headline-sm text-on-surface font-semibold">{{ c.title }}</h2>

            <!-- Source document intake zone -->
            <div class="space-y-2">
              <label class="font-label-sm text-on-surface-variant uppercase tracking-wider flex items-center justify-between">
                <span class="flex items-center gap-1.5">
                  <span class="material-symbols-outlined text-xs text-primary">upload_file</span>
                  Source Document Intake Zone
                </span>
                <span class="text-tertiary text-[11px]">[✓] {{ materialsOf(c.id!).length }} FILE(S) ATTACHED</span>
              </label>

              <div class="bg-surface-container-low border border-outline-variant rounded p-3" *ngFor="let m of materialsOf(c.id!)">
                <div class="flex items-start gap-3">
                  <div class="p-2 bg-primary-container text-primary rounded border border-outline-variant/60">
                    <span class="material-symbols-outlined text-2xl">{{ iconFor(m.type) }}</span>
                  </div>
                  <div>
                    <div class="font-label-sm text-on-surface font-semibold">{{ m.originalName }}</div>
                    <div class="font-label-sm text-[11px] text-on-surface-variant mt-0.5">
                      TYPE: {{ m.type }} <span class="text-outline">•</span> <span class="text-tertiary">INGESTED</span>
                    </div>
                    <button *ngIf="m.id" (click)="deleteMaterial(m.id)" title="Delete source material"
                            class="ml-auto p-1.5 text-on-surface-variant hover:text-error rounded">
                      <span class="material-symbols-outlined text-sm">delete</span>
                    </button>
                  </div>
                </div>
              </div>

              <label class="border border-dashed border-outline-variant hover:border-secondary/60 rounded p-3 text-center transition-colors cursor-pointer bg-surface-dim/50 flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-on-surface-variant text-xl">note_add</span>
                <span class="font-label-sm text-on-surface-variant">Upload PDF / DOCX / PPTX source material</span>
                <input type="file" class="hidden" (change)="upload($event, c.id!)" />
              </label>
            </div>

            <!-- AI synthesis engine -->
            <div class="bg-surface-container-low p-4 rounded border border-outline-variant space-y-3">
              <div class="flex items-center justify-between border-b border-outline-variant/60 pb-2">
                <span class="font-label-sm uppercase tracking-wider text-primary flex items-center gap-1.5 font-bold">
                  <span class="material-symbols-outlined text-base">psychology</span>
                  AI Assessment Synthesis Engine
                </span>
                <span class="font-label-sm text-xs text-on-surface-variant">v3.4-industrial</span>
              </div>
              <button (click)="generate(c.id!)" [disabled]="!hasMaterial(c.id!) || generating"
                      class="w-full py-3 px-4 bg-secondary-container hover:brightness-110 text-white font-label-md uppercase tracking-wider rounded font-bold transition-all shadow-md flex items-center justify-center gap-2 active:scale-[0.99] disabled:opacity-40 disabled:cursor-not-allowed">
                <span *ngIf="!generating" class="text-base">⚡</span>
                <span>{{ generating ? 'SYNTHESIZING…' : 'Synthesize Questions with AI' }}</span>
              </button>
              <div class="text-center font-label-sm text-[10px] text-on-surface-variant">
                Every question is generated from the ingested source document
              </div>
              <button (click)="createManualQuiz(c.id!)"
                      class="w-full py-2.5 border border-primary text-primary hover:bg-primary-container font-label-md uppercase tracking-wider rounded font-bold flex items-center justify-center gap-2">
                <span class="material-symbols-outlined text-base">edit_square</span>
                Create quiz manually
              </button>
            </div>

            <!-- Assessment drafts -->
            <div class="space-y-2" *ngIf="quizzesOf(c.id!).length">
              <div class="font-label-sm text-on-surface-variant uppercase tracking-wider">Assessment Drafts</div>
              <div *ngFor="let q of quizzesOf(c.id!)" class="bg-surface-container-low border border-outline-variant rounded">
                <div class="px-3 py-2.5 flex flex-wrap items-center justify-between gap-2">
                <div class="flex items-center gap-3">
                  <span class="font-label-sm text-on-surface">QUIZ #{{ q.id }}</span>
                  <span class="px-2 py-0.5 rounded font-label-sm text-[10px] border border-outline-variant"
                        [class.bg-tertiary-container]="q.published" [class.text-on-tertiary-container]="q.published"
                        [class.text-secondary]="!q.published">
                    {{ q.published ? 'PUBLISHED' : 'DRAFT — REVIEW REQUIRED' }}
                  </span>
                  <span class="font-label-sm text-[10px] text-outline">{{ q.aiGenerated ? 'AI-GENERATED' : 'MANUAL' }} // {{ q.questions.length }} QUESTIONS</span>
                </div>
                <div class="flex items-center gap-2 flex-wrap">
                  <button (click)="toggleEditor(q)"
                          class="px-3 py-2 border border-outline-variant hover:border-secondary text-on-surface font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-2 transition-all">
                    <span class="material-symbols-outlined text-base">edit_note</span>
                    {{ editingFor === q.id ? 'Close editor' : 'Edit questions' }}
                  </button>
                  <button *ngIf="q.published" (click)="unpublish(q)"
                          class="px-3 py-2 border border-outline-variant hover:border-error text-on-surface font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-2 transition-all"
                          title="STATE: move back to draft">
                    <span class="material-symbols-outlined text-base">unpublished</span>
                    Unpublish
                  </button>
                  <button *ngIf="!q.published" (click)="publish(q)"
                          class="px-4 py-2 bg-secondary-container hover:brightness-110 text-white font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-2 transition-all">
                    <span class="material-symbols-outlined text-base">publish</span>
                    Validate &amp; Publish
                  </button>
                  <button (click)="duplicateQuiz(q)"
                          class="px-3 py-2 border border-outline-variant hover:border-secondary text-on-surface font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-2 transition-all"
                          title="PROTOTYPE: clone this quiz into a new draft">
                    <span class="material-symbols-outlined text-base">content_copy</span>
                    Duplicate
                  </button>
                  <button (click)="deleteQuiz(q)"
                          class="px-3 py-2 border border-error-container text-error hover:bg-error-container/20 font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-2 transition-all">
                    <span class="material-symbols-outlined text-base">delete</span>
                    Delete
                  </button>
                </div>
              </div>
              <div *ngIf="editingFor === q.id" class="px-3 pb-3 space-y-3 border-t border-outline-variant/60">
                <div class="flex items-center justify-between pt-2">
                  <span class="font-label-sm uppercase tracking-wider text-secondary font-bold">Question editor</span>
                  <button (click)="addQuestion(q)"
                          class="px-3 py-1.5 border border-outline-variant hover:border-secondary text-on-surface font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-1.5 transition-all">
                    <span class="material-symbols-outlined text-base">add</span> Add question
                  </button>
                </div>

                <div *ngFor="let question of q.questions; let i = index"
                     class="bg-surface-container-lowest border border-outline-variant rounded p-3 space-y-2">
                  <div class="flex items-center justify-between gap-2">
                    <span class="font-label-sm font-bold text-secondary">Q{{ i + 1 }}</span>
                    <div class="flex items-center gap-1">
                      <button (click)="duplicateQuestion(q, i)" title="Duplicate Question"
                              class="text-on-surface-variant hover:text-on-surface p-1 rounded">
                        <span class="material-symbols-outlined text-sm">content_copy</span>
                      </button>
                      <button (click)="deleteQuestion(q, i)" title="Delete Question"
                              class="text-error hover:opacity-75 p-1 rounded">
                        <span class="material-symbols-outlined text-sm">delete</span>
                      </button>
                    </div>
                  </div>
                  <input [(ngModel)]="question.text" placeholder="Question stem"
                         class="w-full bg-surface-container text-on-surface font-body-md px-3 py-2 rounded border border-outline-variant focus:outline-none focus:border-secondary-container placeholder:text-outline" />
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    <div *ngFor="let option of question.options; let j = index" class="flex items-center gap-2">
                      <input type="radio" [name]="'correct-' + q.id + '-' + i" [value]="j"
                             [(ngModel)]="question.correctAnswer" title="Mark option {{ j + 1 }} correct"
                             class="accent-secondary-container shrink-0" />
                      <input [(ngModel)]="question.options[j]" [placeholder]="'Option ' + (j + 1)"
                             class="flex-1 min-w-0 bg-surface-container text-on-surface font-body-md px-2.5 py-1.5 rounded border border-outline-variant focus:outline-none focus:border-secondary-container placeholder:text-outline" />
                    </div>
                  </div>
                </div>

                <p *ngIf="q.questions.length === 0" class="text-on-surface-variant font-label-md">
                  No questions yet - add one before publishing (the validation chain rejects empty quizzes).
                </p>

                <button (click)="saveQuiz(q)"
                        class="px-4 py-2 bg-primary-container hover:brightness-110 text-on-surface font-label-md font-bold uppercase tracking-wider rounded flex items-center gap-2 transition-all">
                  <span class="material-symbols-outlined text-base">save</span>
                  Save draft
                </button>
              </div>
            </div>
            </div>
          </div>
        </article>
      </section>

      <p *ngIf="courses().length === 0" class="text-center text-on-surface-variant font-label-md py-10">
        No modules yet — create your first training module above.
      </p>
    </div>
  `,
})
export class InstructorDashboardComponent implements OnInit {
  private api = inject(ApiService);
  readonly auth = inject(AuthService);

  courses = signal<Course[]>([]);
  materials = signal<Map<number, Material[]>>(new Map());
  quizzes = signal<Map<number, Quiz[]>>(new Map());
  reviewResults = signal<any[]>([]);
  expandedReviews = signal<Set<number>>(new Set());
  generatingFor = signal<number | null>(null);
  /** Quiz id whose question editor is open (null = none). */
  editingFor: number | null = null;
  newCourse: Course = { title: '', priority: 'MEDIUM', status: 'PUBLISHED' };

  ngOnInit() {
    this.reload();
    this.loadReviews();
  }

  loadReviews() {
    this.api.reviewResults().subscribe((results) => this.reviewResults.set(results));
  }

  approveResult(id: number) {
    this.api.approveResult(id).subscribe({
      next: () => this.loadReviews(),
      error: (e) => alert('Approval failed: ' + (e.error?.error ?? e.message)),
    });
  }

  toggleReview(id: number) {
    this.expandedReviews.update((current) => {
      const next = new Set(current);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  isReviewExpanded(id: number): boolean {
    return this.expandedReviews().has(id);
  }

  answerLabel(review: any, question: Question): string {
    const answers = review.answers ?? {};
    const answer = question.id == null ? undefined : answers[String(question.id)] ?? answers[question.id];
    const answerIndex = answer == null ? NaN : Number(answer);
    return Number.isInteger(answerIndex) && question.options?.[answerIndex]
      ? question.options[answerIndex]
      : 'Not answered';
  }

  unapproveResult(id: number) {
    this.api.unapproveResult(id).subscribe({
      next: () => this.loadReviews(),
      error: (e) => alert('Removing approval failed: ' + (e.error?.error ?? e.message)),
    });
  }

  pad(id?: number): string {
    return String(id ?? 0).padStart(2, '0');
  }

  iconFor(type: string): string {
    return type === 'PDF' ? 'picture_as_pdf' : type === 'DOC' ? 'description' : type === 'PPT' ? 'co_present' : type === 'VIDEO' ? 'movie' : 'draft';
  }

  get generating(): boolean {
    return this.generatingFor() !== null;
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

  deleteCourse(id: number) {
    if (!confirm('Delete this module and its materials?')) return;
    this.api.deleteCourse(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Delete failed: ' + (e.error?.error ?? e.message)),
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
  deleteMaterial(id: number) {
    if (!confirm('Delete this source material?')) return;
    this.api.deleteMaterial(id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Delete failed: ' + (e.error?.error ?? e.message)),
    });
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
    this.generatingFor.set(courseId);
    this.api
      .generateQuiz(mats[mats.length - 1].id!, 5, this.auth.user()!.id)
      .subscribe({
        next: () => {
          this.generatingFor.set(null);
          this.reload();
        },
        error: (e) => {
          this.generatingFor.set(null);
          alert('AI generation failed: ' + (e.error?.error || e.error?.message || e.message));
        },
      });
  }

  createManualQuiz(courseId: number) {
    this.api.createManualQuiz(courseId, this.auth.user()?.id ?? 0).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Manual quiz creation failed: ' + (e.error?.error ?? e.message)),
    });
  }

  publish(quiz: Quiz) {
    this.api.publishQuiz(quiz.id!, quiz).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Validation failed: ' + (e.error?.error ?? e.message)),
    });
  }

  // --- STATE / PROTOTYPE wiring -------------------------------------------

  /** Opens/closes the per-quiz question editor. */
  toggleEditor(quiz: Quiz) {
    this.editingFor = this.editingFor === quiz.id ? null : (quiz.id ?? null);
  }

  /** Re-creates the quizzes Map so in-place question edits re-render. */
  private touchQuizzes() {
    this.quizzes.update((map) => new Map(map));
  }

  /** PROTOTYPE: clone a question (new object, no id) right after the original. */
  duplicateQuestion(quiz: Quiz, index: number) {
    const source = quiz.questions[index];
    const clone: Question = {
      text: source.text,
      options: [...source.options],
      correctAnswer: source.correctAnswer,
    };
    quiz.questions.splice(index + 1, 0, clone);
    this.touchQuizzes();
  }

  deleteQuestion(quiz: Quiz, index: number) {
    quiz.questions.splice(index, 1);
    this.touchQuizzes();
  }

  addQuestion(quiz: Quiz) {
    quiz.questions.push({ text: '', options: ['', '', '', ''], correctAnswer: 0 });
    this.touchQuizzes();
  }

  /** Persists editor changes as a draft (backend rejects edits while published). */
  saveQuiz(quiz: Quiz) {
    this.api.updateQuiz(quiz.id!, quiz).subscribe({
      next: () => {
        this.editingFor = null;
        this.reload();
      },
      error: (e) => alert('Save failed: ' + (e.error?.error ?? e.message)),
    });
  }

  /** PROTOTYPE: clone the whole quiz into a new draft on the server. */
  duplicateQuiz(quiz: Quiz) {
    this.api.duplicateQuiz(quiz.id!, quiz.course?.id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Duplicate failed: ' + (e.error?.error ?? e.message)),
    });
  }

  /** STATE: published -> draft. */
  unpublish(quiz: Quiz) {
    this.api.unpublishQuiz(quiz.id!).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Unpublish failed: ' + (e.error?.error ?? e.message)),
    });
  }

  deleteQuiz(quiz: Quiz) {
    if (!quiz.id || !confirm('Delete this quiz permanently?')) return;
    this.api.deleteQuiz(quiz.id).subscribe({
      next: () => this.reload(),
      error: (e) => alert('Quiz deletion failed: ' + (e.error?.error || e.message)),
    });
  }

  /** STATE: course lifecycle transition; illegal ones come back as a 400 with the allowed set. */
  changeStatus(course: Course, status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED') {
    this.api.changeCourseStatus(course.id!, status).subscribe({
      next: () => this.reload(),
      error: (e) => {
        alert(e.error?.error ?? e.message);
        this.reload(); // revert the select to the persisted state
      },
    });
  }
}
