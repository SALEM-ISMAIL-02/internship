import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminApiService } from './admin-api.service';
import {
  Course,
  LearnerProgress,
  Material,
  Quiz,
  QuizResult,
} from './api-models';
import { CourseApiService } from './course-api.service';
import { MaterialApiService } from './material-api.service';
import { ProgressApiService } from './progress-api.service';
import { QuizApiService } from './quiz-api.service';

export type { Course, LearnerProgress, Material, Question, Quiz, QuizResult } from './api-models';

/**
 * SRP facade kept for backward compatibility: re-exports the domain models
 * and delegates every call to the focused *ApiService behind it (DIP).
 * New code should inject CourseApiService / QuizApiService / ... directly.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(
    private courses: CourseApiService,
    private materials: MaterialApiService,
    private quizzes: QuizApiService,
    private progress: ProgressApiService,
    private admin: AdminApiService
  ) {}

  listCourses(): Observable<Course[]> { return this.courses.list(); }
  createCourse(c: Course): Observable<Course> { return this.courses.create(c); }
  updateCourse(id: number, c: Course): Observable<Course> { return this.courses.update(id, c); }
  deleteCourse(id: number): Observable<void> { return this.courses.delete(id); }
  changeCourseStatus(id: number, status: NonNullable<Course['status']>): Observable<Course> {
    return this.courses.changeStatus(id, status);
  }

  uploadMaterial(courseId: number, file: File): Observable<Material> {
    return this.materials.upload(courseId, file);
  }
  materialsByCourse(courseId: number): Observable<Material[]> {
    return this.materials.byCourse(courseId);
  }
  deleteMaterial(id: number): Observable<void> { return this.materials.delete(id); }

  generateQuiz(materialId: number, numQuestions: number, creatorId: number): Observable<Quiz> {
    return this.quizzes.generate(materialId, numQuestions, creatorId);
  }
  createManualQuiz(courseId: number, creatorId: number): Observable<Quiz> {
    return this.quizzes.createManual(courseId, creatorId);
  }
  quizzesByCourse(courseId: number): Observable<Quiz[]> { return this.quizzes.byCourse(courseId); }
  publishQuiz(id: number, quiz: Quiz): Observable<Quiz> { return this.quizzes.publish(id, quiz); }
  /** Saves draft edits to a quiz's questions (backend rejects edits while published). */
  updateQuiz(id: number, quiz: Quiz): Observable<Quiz> { return this.quizzes.updateDraft(id, quiz); }
  /** PROTOTYPE: deep-clone a quiz (questions included) into a new draft. */
  duplicateQuiz(id: number, courseId?: number): Observable<Quiz> {
    return this.quizzes.duplicate(id, courseId);
  }
  /** STATE: move a published quiz back to draft. */
  unpublishQuiz(id: number): Observable<Quiz> { return this.quizzes.unpublish(id); }
  deleteQuiz(id: number): Observable<void> { return this.quizzes.delete(id); }
  takeQuiz(id: number): Observable<Quiz> { return this.quizzes.take(id); }
  reviewResults(): Observable<any[]> { return this.quizzes.reviewResults(); }
  approveResult(id: number): Observable<any> { return this.quizzes.approveResult(id); }
  unapproveResult(id: number): Observable<any> { return this.quizzes.unapproveResult(id); }

  myProgress(learnerId: number): Observable<LearnerProgress[]> {
    return this.progress.mine(learnerId);
  }
  submitQuiz(quizId: number, learnerId: number, answers: Record<number, number>): Observable<QuizResult> {
    return this.progress.submitQuiz(quizId, learnerId, answers);
  }
  myResults(learnerId: number): Observable<QuizResult[]> { return this.progress.results(learnerId); }

  listUsers(): Observable<any[]> { return this.admin.listUsers(); }
  createUser(user: { name: string; email: string; password: string; role: string }): Observable<any> {
    return this.admin.createUser(user);
  }
  deleteUser(id: number): Observable<void> { return this.admin.deleteUser(id); }
}
