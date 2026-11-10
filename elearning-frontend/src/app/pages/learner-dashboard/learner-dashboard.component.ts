import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Course, LearnerProgress, Material, Quiz, QuizResult } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { TopBarComponent } from '../../shared/top-bar.component';

@Component({
  selector: 'app-learner-dashboard',
  imports: [CommonModule, FormsModule, TopBarComponent],
  template: `
    <app-top-bar />
    <div class="app-page px-4 sm:px-6 lg:px-8 py-6 technical-grid">
      <div class="max-w-[1440px] mx-auto">
      <div class="bg-surface border border-outline-variant/80 px-4 py-3 mb-6 flex items-center justify-between text-label-sm rounded-xl shadow-sm">
        <div class="flex items-center gap-space-md">
          <span class="bg-secondary-container/20 text-secondary border border-secondary-container/40 px-2 py-0.5 rounded text-[10px] tracking-wider uppercase font-bold flex items-center gap-1">
            <span class="w-1.5 h-1.5 rounded-full bg-secondary-container animate-pulse"></span>
            LEARNER PORTAL
          </span>
          <span class="text-on-surface-variant hidden sm:inline">Mandatory course completion is tracked for site clearance compliance</span>
        </div>
        <span class="text-[11px] hidden md:inline">AUDIT LOG: <span class="text-tertiary">SYNCED</span></span>
      </div>

      <div class="flex flex-wrap items-end justify-between gap-4 mb-6">
        <div>
          <h1 class="font-headline-md text-on-surface font-bold tracking-tight">Course Catalog &amp; Compliance</h1>
          <p class="font-label-sm text-on-surface-variant mt-1">Complete mandatory modules to maintain your site access clearance.</p>
        </div>
        <input [(ngModel)]="search"
               class="bg-surface-container-lowest text-on-surface font-body-md px-3 py-2 rounded-lg border border-outline-variant focus:outline-none focus:border-secondary-container w-full sm:w-72 placeholder:text-outline"
               placeholder="Search courses, standards, keywords..." />
      </div>

      <section class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
        <article *ngFor="let c of filteredCourses()"
                 class="bg-surface border border-outline-variant/80 rounded-xl flex flex-col justify-between overflow-hidden shadow-[0_6px_20px_rgba(32,48,80,0.05)] transition-transform duration-150"
                 [class.opacity-60]="hasSubmitted(c.id)"
                 [class.grayscale]="hasSubmitted(c.id)"
                 [class.ring-2]="hasSubmitted(c.id)"
                 [class.ring-tertiary]="hasSubmitted(c.id)">
          <div class="px-4 py-2 bg-surface-container-lowest border-b border-outline-variant flex items-center justify-between text-label-sm text-outline">
            <span>MOD-{{ pad(c.id) }} // {{ c.category || 'GENERAL' }}</span>
            <span>{{ c.status || 'PUBLISHED' }}</span>
          </div>

          <div class="p-5 flex-1 flex flex-col justify-between space-y-4">
            <div class="space-y-2">
              <div class="flex items-center justify-between">
                <span class="px-2 py-0.5 rounded border font-label-sm font-medium uppercase tracking-wider"
                      [class.bg-error-container]="c.priority === 'MANDATORY'"
                      [class.text-on-error-container]="c.priority === 'MANDATORY'"
                      [class.border-error-container]="c.priority === 'MANDATORY'"
                      [class.bg-surface-container-high]="c.priority !== 'MANDATORY'"
                      [class.text-on-surface-variant]="c.priority !== 'MANDATORY'"
                      [class.border-outline-variant]="c.priority !== 'MANDATORY'">
                  {{ c.priority === 'MANDATORY' ? 'MANDATORY' : (c.priority || 'NORMAL') + ' PRIORITY' }}
                </span>
                <span class="text-label-sm text-outline font-mono">{{ progressFor(c.id) }}% {{ progressFor(c.id) >= 100 ? 'COMPLETE' : progressFor(c.id) > 0 ? 'IN PROGRESS' : 'NOT STARTED' }}</span>
              </div>
              <h2 class="text-headline-sm text-on-surface font-semibold">{{ c.title }}</h2>
              <p class="text-body-sm text-on-surface-variant">{{ c.description }}</p>
            </div>

            <div class="w-full bg-surface-container-lowest h-2 rounded p-0.5 border flex gap-0.5"
                 [class.border-tertiary/40]="progressFor(c.id) >= 100"
                 [class.border-secondary/40]="progressFor(c.id) < 100">
              <div class="h-full rounded-sm bg-tertiary transition-all" [style.width.%]="progressFor(c.id)"></div>
            </div>

            <div class="pt-2 border-t border-outline-variant/50 grid grid-cols-2 gap-1 text-center">
              <div class="bg-surface-container-high py-1 rounded border border-outline-variant/40 flex items-center justify-center gap-1 text-label-sm text-on-surface-variant">
                <span class="material-symbols-outlined text-xs">quiz</span>
                {{ hasPublishedQuiz(c.id) ? 'Quiz Ready' : 'No Quiz' }}
              </div>
              <div class="bg-surface-container-high py-1 rounded border border-outline-variant/40 flex items-center justify-center gap-1 text-label-sm"
                   [class.text-tertiary]="isCertified(c.id)" [class.text-on-surface-variant]="!isCertified(c.id)">
                <span class="material-symbols-outlined text-xs">{{ isCertified(c.id) ? 'verified' : 'timer' }}</span>
                {{ isCertified(c.id) ? 'Certified' : 'Awaiting approval' }}
              </div>
            </div>
          </div>

          <div class="p-4 pt-0 grid grid-cols-3 gap-2">
            <button (click)="openMaterials(c)"
                    title="Open multi-format material workspace"
                    class="py-2.5 rounded bg-surface-container hover:bg-surface-container-high text-on-surface-variant border border-outline-variant font-label-md uppercase tracking-wider font-semibold transition-colors flex items-center justify-center gap-1.5">
              <span class="material-symbols-outlined text-base">folder_open</span>
              Files
            </button>
            <button (click)="openQuiz(c.id!)" [disabled]="!hasPublishedQuiz(c.id!) || hasSubmitted(c.id!)"
                    title="Launch assessment session"
                    class="py-2.5 rounded bg-secondary-container hover:brightness-110 text-white font-label-md uppercase tracking-wider font-semibold transition-colors flex items-center justify-center gap-1.5 disabled:bg-surface-container-highest disabled:text-outline disabled:opacity-100 disabled:cursor-not-allowed">
              <span class="material-symbols-outlined text-base">assignment</span>
              {{ hasSubmitted(c.id!) ? 'Submitted' : 'Quiz' }}
            </button>
          </div>
        </article>
      </section>
      <p *ngIf="filteredCourses().length === 0" class="text-center text-on-surface-variant font-label-md py-10">
        No courses in the catalog yet. Contact your training officer.
      </p>
      </div>

      <!-- Multi-format material workspace -->
      <section *ngIf="activeCourse() as ac" class="mt-8 bg-primary-container border border-outline-variant/70 rounded-lg p-space-lg flex flex-col gap-space-lg shadow-sm">
        <div class="flex flex-wrap items-center gap-2 text-label-sm font-label-sm text-on-surface-variant border-b border-outline-variant/60 pb-2">
          <span class="material-symbols-outlined text-secondary text-base">layers</span>
          <span>CATALOG</span>
          <span class="text-outline">/</span>
          <span>{{ ac.category || 'GENERAL' }}</span>
          <span class="text-outline">/</span>
          <span class="text-secondary font-semibold bg-surface-dim px-space-xs py-0.5 border border-outline-variant/60 rounded">
            MOD-{{ pad(ac.id) }} // {{ ac.title }}
          </span>
          <button (click)="closeMaterials()" class="ml-auto text-outline hover:text-on-surface flex items-center gap-1 transition-colors">
            <span class="material-symbols-outlined text-sm">close</span> CLOSE
          </button>
        </div>

        <div class="flex flex-col md:flex-row md:items-center justify-between gap-space-md">
          <div>
            <div class="flex flex-wrap items-center gap-2 mb-1">
              <span class="text-label-sm font-label-sm text-secondary bg-surface-dim px-2 py-0.5 border border-outline-variant rounded">SYS-NODE {{ pad(ac.id) }}</span>
              <span class="text-label-sm font-label-sm text-tertiary bg-tertiary-container px-2 py-0.5 border border-tertiary/40 rounded">[✓] AUDIT READY</span>
              <span class="text-label-sm font-label-sm text-on-surface-variant font-mono">COMPLETION: {{ progressFor(ac.id) }}%</span>
            </div>
            <h1 class="text-headline-md font-headline-md text-on-surface">{{ ac.title }}</h1>
            <p class="text-body-md font-body-md text-on-surface-variant mt-1 max-w-3xl">{{ ac.description }}</p>
          </div>
          <span class="self-start md:self-auto bg-surface-container-high text-on-surface px-space-md py-space-xs rounded border border-outline-variant text-label-md font-label-md flex items-center gap-2">
            <span class="material-symbols-outlined text-sm">attach_file</span>
            {{ materialsOf(ac.id!).length }} SOURCE FILE(S)
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-space-lg">
          <div class="bg-surface-container-low border border-outline-variant/60 rounded p-space-md">
            <div class="flex items-center justify-between border-b border-outline-variant/40 pb-2 mb-3">
              <span class="text-label-sm font-label-sm uppercase tracking-wider text-primary font-bold">Referenced Engineering Files</span>
              <span class="text-label-sm font-label-sm" [class.text-tertiary]="materialsOf(ac.id!).length > 0" [class.text-outline]="materialsOf(ac.id!).length === 0">
                {{ materialsOf(ac.id!).length }} VERIFIED ATTACHMENT(S)
              </span>
            </div>
            <div class="flex flex-col gap-2">
              <button *ngFor="let m of materialsOf(ac.id!)" (click)="activeMaterial.set(m)"
                      class="flex items-center justify-between gap-2 p-2 bg-surface-dim hover:bg-surface-container-high border rounded transition-all text-left"
                      [class.border-secondary]="activeMaterial()?.id === m.id"
                      [class.border-outline-variant]="activeMaterial()?.id !== m.id">
                <div class="flex items-center gap-2.5 min-w-0">
                  <span class="material-symbols-outlined text-xl shrink-0"
                        [class.text-error]="m.type === 'PDF'"
                        [class.text-tertiary]="m.type === 'DOC' || m.type === 'PPT'"
                        [class.text-primary]="m.type !== 'PDF' && m.type !== 'DOC' && m.type !== 'PPT'">{{ iconFor(m.type) }}</span>
                  <div class="flex flex-col min-w-0">
                    <span class="text-xs font-label-md text-on-surface font-medium truncate">{{ m.originalName || m.filePath }}</span>
                    <span class="text-[10px] text-on-surface-variant font-mono">{{ m.type }} // INGESTED</span>
                  </div>
                </div>
                <span class="material-symbols-outlined text-outline text-sm shrink-0">chevron_right</span>
              </button>
              <p *ngIf="materialsOf(ac.id!).length === 0" class="text-body-sm text-on-surface-variant py-2">
                No source material has been ingested for this module yet.
              </p>
            </div>
          </div>

          <div class="bg-surface-container-low border border-outline-variant/60 rounded p-space-md flex flex-col">
            <div class="flex items-center justify-between border-b border-outline-variant/40 pb-2 mb-3">
              <span class="text-label-sm font-label-sm uppercase tracking-wider text-primary font-bold">Material Viewer</span>
              <span class="text-label-sm font-label-sm text-on-surface-variant">OPEN OR DOWNLOAD</span>
            </div>
            <div *ngIf="activeMaterial() as m; else noMaterial" class="flex flex-col flex-1">
              <div class="relative flex-1 min-h-[200px] bg-surface-container-lowest border border-outline-variant rounded flex items-center justify-center blueprint-grid">
                <div class="flex flex-col items-center gap-2 text-center px-4">
                  <ng-container *ngIf="m.type === 'PDF'; else fileIcon">
                    <iframe [src]="fileUrl(m.id)" class="w-full h-56 border-0" title="Course material preview"></iframe>
                  </ng-container>
                  <ng-template #fileIcon>
                    <span class="material-symbols-outlined text-5xl text-primary">{{ iconFor(m.type) }}</span>
                  </ng-template>
                  <span class="font-label-md text-on-surface">{{ m.originalName || m.filePath }}</span>
                  <span class="font-label-sm text-on-surface-variant">{{ m.type }} SOURCE</span>
                </div>
              </div>
              <div class="mt-3 flex gap-2">
                <a [href]="fileUrl(m.id)" target="_blank" rel="noopener"
                   class="flex-1 text-center py-2 rounded bg-primary text-white font-label-md font-bold">Open file</a>
                <a [href]="fileUrl(m.id)" [download]="m.originalName"
                   class="flex-1 text-center py-2 rounded border border-primary text-primary font-label-md font-bold">Download</a>
              </div>
              <div class="mt-3 space-y-2.5 font-label-sm text-xs">
                <div class="flex justify-between items-center py-1 border-b border-outline-variant/20">
                  <span class="text-on-surface-variant">Material ID:</span>
                  <span class="font-mono text-on-surface">MAT-{{ pad(m.id) }}</span>
                </div>
                <div class="flex justify-between items-center py-1 border-b border-outline-variant/20 gap-3">
                  <span class="text-on-surface-variant shrink-0">Source Path:</span>
                  <span class="font-mono text-on-surface-variant truncate" [title]="m.filePath">{{ m.filePath }}</span>
                </div>
                <div class="flex justify-between items-center py-1">
                  <span class="text-on-surface-variant">Ingestion Status:</span>
                  <span class="font-mono text-tertiary">VERIFIED // AUDIT READY</span>
                </div>
              </div>
            </div>
            <ng-template #noMaterial>
              <div class="flex-1 min-h-[200px] border border-dashed border-outline-variant rounded flex items-center justify-center px-4 text-center">
                <span class="font-label-sm text-on-surface-variant">SELECT A REFERENCED FILE TO LOAD THE VIEWER</span>
              </div>
            </ng-template>
          </div>
        </div>
      </section>

      <!-- Compliance lockout banner strip -->
      <div *ngIf="activeQuiz()" class="mt-8 max-w-3xl mx-auto w-full bg-surface-container-low border border-outline-variant rounded p-2.5 flex items-center gap-3 shadow-inner">
        <div class="w-1 self-stretch bg-secondary-container rounded-sm shrink-0"></div>
        <div class="flex-1">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <span class="font-label-sm text-secondary font-bold">MANDATORY CERTIFICATION CHECKPOINT</span>
            <span class="font-label-sm text-on-surface-variant">NODE-03B // REAC</span>
          </div>
          <p class="font-body-sm text-on-surface-variant">Standardized examination module for active industrial field compliance officers.</p>
        </div>
      </div>

      <section *ngIf="activeQuiz()" class="mt-4 max-w-3xl mx-auto bg-primary-container border border-outline-variant rounded shadow-lg overflow-hidden">
        <div class="bg-surface-container-low px-3 py-1.5 border-b border-outline-variant flex flex-wrap items-center justify-between gap-2">
          <div class="flex items-center gap-2 min-w-0">
            <span class="material-symbols-outlined text-secondary text-sm">engineering</span>
            <span class="font-label-sm text-primary tracking-wide truncate">MODULE // {{ activeCourseTitle() || 'GENERAL' }}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="px-2 py-0.5 bg-surface-container-high border border-outline-variant rounded flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full bg-secondary-container animate-pulse"></span>
              <span class="font-label-sm text-secondary">{{ answeredCount() }}/{{ activeQuiz()!.questions.length }} LOGGED</span>
            </span>
            <span class="font-label-sm text-tertiary flex items-center gap-1">
              <span class="material-symbols-outlined text-xs">lock</span> PROCTORED
            </span>
          </div>
        </div>
        <div class="p-space-lg space-y-6">
          <fieldset *ngFor="let q of activeQuiz()!.questions; let i = index" class="space-y-2">
            <legend class="sr-only">Select response for question {{ i + 1 }}</legend>
            <div class="space-y-1">
              <span class="font-label-sm text-on-primary-container">EVALUATION QUERY {{ i + 1 }}/{{ activeQuiz()!.questions.length }} :</span>
              <h2 class="font-headline-sm text-on-surface leading-snug">{{ q.text }}</h2>
            </div>
            <label *ngFor="let opt of q.options; let j = index"
                   class="relative overflow-hidden flex items-start gap-3 p-3 rounded cursor-pointer transition-all active:scale-[0.99] hover:bg-surface-container-high"
                   [class.bg-surface-container-high]="answers[q.id!] === j"
                   [class.border-2]="answers[q.id!] === j"
                   [class.border-secondary-container]="answers[q.id!] === j"
                   [class.shadow-md]="answers[q.id!] === j"
                   [class.bg-surface-container-low]="answers[q.id!] !== j"
                   [class.border]="answers[q.id!] !== j"
                   [class.border-outline-variant]="answers[q.id!] !== j">
              <span *ngIf="answers[q.id!] === j" class="absolute left-0 top-0 bottom-0 w-1 bg-secondary-container"></span>
              <input class="sr-only" type="radio" [name]="'q' + q.id" [value]="j" [(ngModel)]="answers[q.id!]" />
              <span class="w-5 h-5 mt-0.5 rounded-sm border flex items-center justify-center shrink-0"
                    [class.border-secondary-container]="answers[q.id!] === j"
                    [class.text-secondary]="answers[q.id!] === j"
                    [class.border-outline]="answers[q.id!] !== j"
                    [class.text-on-surface-variant]="answers[q.id!] !== j"
                    [class.bg-surface-container-lowest]="answers[q.id!] !== j">
                <span class="font-label-sm">{{ letter(j) }}</span>
              </span>
              <p class="flex-1 font-body-md leading-tight"
                 [class.text-on-surface]="answers[q.id!] === j"
                 [class.text-on-surface-variant]="answers[q.id!] !== j">{{ opt }}</p>
            </label>
          </fieldset>

          <div class="pt-2 flex items-center justify-between gap-3 border-t border-outline-variant">
            <button (click)="activeQuiz.set(null)"
                    class="px-3 py-2 bg-surface-container text-on-surface-variant border border-outline-variant rounded font-label-md hover:bg-surface-container-highest">
              Abort Session
            </button>
            <button (click)="submitQuiz()" [disabled]="answeredCount() < activeQuiz()!.questions.length"
                    class="flex-1 px-4 py-2 bg-secondary-container text-white rounded font-label-md font-bold flex items-center justify-center gap-2 shadow-sm hover:brightness-110 disabled:opacity-40 disabled:cursor-not-allowed">
              Submit Evaluation
              <span class="material-symbols-outlined text-sm">send</span>
            </button>
          </div>
        </div>
      </section>

      <!-- Submission status drawer (scores are instructor-only) -->
      <section *ngIf="lastResult() as r" class="max-w-3xl mx-auto mt-4 bg-surface-container-low border border-outline-variant rounded shadow-xl overflow-hidden">
        <div class="bg-surface-container px-4 py-2.5 border-b border-outline-variant flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="w-2 h-2 rounded-full bg-secondary"></span>
            <span class="font-label-md font-bold text-secondary">SUBMISSION RECEIVED</span>
          </div>
          <div class="flex items-center gap-1 font-label-sm text-on-surface-variant">
            <span class="material-symbols-outlined text-sm">lock_open</span>
            AUDIT ID #{{ pad(r.id) }}-B
          </div>
        </div>
        <div class="p-4 space-y-3">
          <div class="bg-surface-container-high border border-tertiary-fixed-dim/30 p-2.5 rounded flex items-center justify-between">
            <div class="flex items-center gap-2.5">
              <div class="w-7 h-7 rounded bg-tertiary-container border border-tertiary flex items-center justify-center text-tertiary">
                <span class="material-symbols-outlined text-base">verified</span>
              </div>
              <div class="flex flex-col">
                <span class="font-label-sm text-tertiary font-bold">
                  Awaiting instructor verification
                </span>
                <span class="font-body-sm text-[11px] text-on-surface-variant">Verified by ISO-14001 Validation Matrix</span>
              </div>
            </div>
            <button (click)="lastResult.set(null)" class="material-symbols-outlined text-tertiary text-lg" title="Dismiss">download</button>
          </div>
        </div>
      </section>

      <section class="bg-surface-container-lowest border border-outline-variant rounded-lg p-5 mt-8 space-y-4" *ngIf="results().length">
        <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 border-b border-outline-variant pb-3">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-tertiary">analytics</span>
            <h3 class="text-headline-sm text-on-surface font-semibold">Assessment Results &amp; Compliance Ledger</h3>
          </div>
          <span class="text-label-sm text-outline font-mono">AUDIT SYNC: PASS</span>
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 text-label-sm">
          <div class="p-3 bg-surface-container-low rounded border border-outline-variant/50 space-y-2" *ngFor="let r of results()">
            <div class="text-outline uppercase">QUIZ #{{ r.quiz?.id ?? '—' }}</div>
            <div class="text-body-sm font-semibold text-secondary">{{ r.approved ? 'Approved' : 'Under instructor review' }}</div>
            <div class="text-body-sm text-on-surface-variant">{{ r.date | date: 'short' }}</div>
          </div>
        </div>
      </section>
    </div>
  `,
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
  lastResult = signal<QuizResult | null>(null);
  activeCourse = signal<Course | null>(null);
  materials = signal<Map<number, Material[]>>(new Map());
  activeMaterial = signal<Material | null>(null);
  answers: Record<string, number> = {};
  search = '';

  ngOnInit() {
    const uid = this.auth.user()!.id;
    this.api.listCourses().subscribe((courses) => {
      this.courses.set(courses);
      courses.forEach((c) =>
        this.api.quizzesByCourse(c.id!).subscribe((qs) =>
          this.quizzesByCourseId.update((m) => new Map(m).set(c.id!, qs))
        )
      );
    });
    this.api.myProgress(uid).subscribe((p) => this.progress.set(p));
    this.api.myResults(uid).subscribe((r) => this.results.set(r));
  }

  filteredCourses(): Course[] {
    const q = this.search.toLowerCase().trim();
    const list = this.courses();
    if (!q) return list;
    return list.filter(
      (c) =>
        (c.title ?? '').toLowerCase().includes(q) ||
        (c.category ?? '').toLowerCase().includes(q) ||
        (c.description ?? '').toLowerCase().includes(q)
    );
  }

  pad(id?: number): string {
    return String(id ?? 0).padStart(2, '0');
  }

  letter(i: number): string {
    return String.fromCharCode(65 + i);
  }

  answeredCount(): number {
    return Object.keys(this.answers).length;
  }

  // ---- Multi-format material workspace ----
  openMaterials(course: Course) {
    this.activeCourse.set(course);
    this.activeMaterial.set(null);
    const cached = this.materials().get(course.id!);
    if (!cached) {
      this.api.materialsByCourse(course.id!).subscribe((list) => {
        this.materials.update((map) => new Map(map).set(course.id!, list));
        this.activeMaterial.set(list[0] ?? null);
      });
    } else {
      this.activeMaterial.set(cached[0] ?? null);
    }
  }

  closeMaterials() {
    this.activeCourse.set(null);
    this.activeMaterial.set(null);
  }

  materialsOf(courseId: number): Material[] {
    return this.materials().get(courseId) ?? [];
  }

  iconFor(type: string): string {
    switch (type) {
      case 'PDF':
        return 'picture_as_pdf';
      case 'DOC':
        return 'description';
      case 'PPT':
        return 'co_present';
      case 'VIDEO':
        return 'movie';
      case 'XLS':
        return 'table_chart';
      default:
        return 'draft';
    }
  }

  units(score: number): number[] {
    return Array.from({ length: 10 }, (_, i) => i);
  }

  correctUnits(score: number): number {
    return Math.round((score / 100) * this.units(score).length);
  }

  progressFor(courseId?: number): number {
    return this.progress().find((p) => p.course?.id === courseId)?.progress ?? 0;
  }

  hasPublishedQuiz(courseId?: number): boolean {
    return (this.quizzesByCourseId().get(courseId!) ?? []).some((q) => q.published);
  }

  hasSubmitted(courseId?: number): boolean {
    return this.results().some((r) => r.courseId === courseId || r.quiz?.course?.id === courseId);
  }

  fileUrl(id?: number): string {
    return id ? `/api/materials/${id}/file` : '';
  }

  isCertified(courseId?: number): boolean {
    return this.results().some((r) => r.courseId === courseId && r.approved);
  }

  openQuiz(courseId: number) {
    if (this.hasSubmitted(courseId)) return;
    const quiz = (this.quizzesByCourseId().get(courseId) ?? []).find((q) => q.published);
    if (!quiz?.id) return;
    this.answers = {};
    this.lastResult.set(null);
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
      this.lastResult.set(r);
      this.activeQuiz.set(null);
      const uid = this.auth.user()!.id;
      this.api.myResults(uid).subscribe((res) => this.results.set(res));
      this.api.myProgress(uid).subscribe((p) => this.progress.set(p));
    });
  }
}
