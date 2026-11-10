import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiHttpService } from './api-http.service';
import { LearnerProgress, QuizResult } from './api-models';

/** SRP: progress + quiz submissions + results only. */
@Injectable({ providedIn: 'root' })
export class ProgressApiService {
  constructor(private api: ApiHttpService) {}
  mine(learnerId: number): Observable<LearnerProgress[]> {
    return this.api.http.get<LearnerProgress[]>(`${this.api.base}/progress`, {
      params: new HttpParams().set('learnerId', learnerId),
    });
  }
  submitQuiz(quizId: number, learnerId: number, answers: Record<number, number>): Observable<QuizResult> {
    return this.api.http.post<QuizResult>(
      `${this.api.base}/progress/quiz/${quizId}/submit?learnerId=${learnerId}`,
      answers
    );
  }
  results(learnerId: number): Observable<QuizResult[]> {
    return this.api.http.get<QuizResult[]>(`${this.api.base}/progress/results`, {
      params: new HttpParams().set('learnerId', learnerId),
    });
  }
}
