import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiHttpService } from './api-http.service';
import { Quiz } from './api-models';

/** SRP: quiz generation / review / publish / duplicate / take only. */
@Injectable({ providedIn: 'root' })
export class QuizApiService {
  constructor(private api: ApiHttpService) {}
  generate(materialId: number, numQuestions: number, creatorId: number): Observable<Quiz> {
    return this.api.http.post<Quiz>(`${this.api.base}/quizzes/generate`, {
      materialId, numQuestions, creatorId,
    });
  }
  createManual(courseId: number, creatorId: number): Observable<Quiz> {
    return this.api.http.post<Quiz>(`${this.api.base}/quizzes/manual`, { courseId, creatorId });
  }
  byCourse(courseId: number): Observable<Quiz[]> {
    return this.api.http.get<Quiz[]>(`${this.api.base}/quizzes/course/${courseId}`);
  }
  publish(id: number, quiz: Quiz): Observable<Quiz> {
    return this.api.http.put<Quiz>(`${this.api.base}/quizzes/${id}/publish`, quiz);
  }
  updateDraft(id: number, quiz: Quiz): Observable<Quiz> {
    return this.api.http.put<Quiz>(`${this.api.base}/quizzes/${id}`, quiz);
  }
  duplicate(id: number, courseId?: number): Observable<Quiz> {
    return this.api.http.post<Quiz>(`${this.api.base}/quizzes/${id}/duplicate`, courseId ? { courseId } : {});
  }
  unpublish(id: number): Observable<Quiz> {
    return this.api.http.put<Quiz>(`${this.api.base}/quizzes/${id}/unpublish`, {});
  }
  delete(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.base}/quizzes/${id}`);
  }
  take(id: number): Observable<Quiz> {
    return this.api.http.get<Quiz>(`${this.api.base}/quizzes/${id}/take`);
  }
  reviewResults(): Observable<any[]> {
    return this.api.http.get<any[]>(`${this.api.base}/quizzes/results`);
  }
  approveResult(id: number): Observable<any> {
    return this.api.http.put<any>(`${this.api.base}/quizzes/results/${id}/approve`, {});
  }
  unapproveResult(id: number): Observable<any> {
    return this.api.http.put<any>(`${this.api.base}/quizzes/results/${id}/unapprove`, {});
  }
}
