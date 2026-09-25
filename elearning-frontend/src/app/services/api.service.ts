import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Course {
  id?: number;
  title: string;
  description?: string;
  category?: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'MANDATORY';
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  instructor?: { id: number };
}

export interface Material {
  id?: number;
  type: string;
  filePath: string;
  originalName?: string;
  course?: { id: number };
}

export interface Question {
  id?: number;
  text: string;
  options: string[];
  correctAnswer: number;
}

export interface Quiz {
  id?: number;
  course?: { id: number };
  aiGenerated?: boolean;
  published?: boolean;
  questions: Question[];
}

export interface LearnerProgress {
  id?: number;
  progress: number;
  completed: boolean;
  course?: Course;
}

export interface QuizResult {
  id?: number;
  score: number;
  date: string;
  quiz?: Quiz;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = '/api';

  constructor(private http: HttpClient) {}

  // Courses
  listCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.base}/courses`);
  }
  createCourse(c: Course): Observable<Course> {
    return this.http.post<Course>(`${this.base}/courses`, c);
  }
  updateCourse(id: number, c: Course): Observable<Course> {
    return this.http.put<Course>(`${this.base}/courses/${id}`, c);
  }
  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/courses/${id}`);
  }

  // Materials
  uploadMaterial(courseId: number, file: File): Observable<Material> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<Material>(`${this.base}/materials/upload/${courseId}`, form);
  }
  materialsByCourse(courseId: number): Observable<Material[]> {
    return this.http.get<Material[]>(`${this.base}/materials/course/${courseId}`);
  }

  // Quizzes
  generateQuiz(materialId: number, numQuestions: number, creatorId: number): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.base}/quizzes/generate`, {
      materialId,
      numQuestions,
      creatorId,
    });
  }
  quizzesByCourse(courseId: number): Observable<Quiz[]> {
    return this.http.get<Quiz[]>(`${this.base}/quizzes/course/${courseId}`);
  }
  publishQuiz(id: number, quiz: Quiz): Observable<Quiz> {
    return this.http.put<Quiz>(`${this.base}/quizzes/${id}/publish`, quiz);
  }
  takeQuiz(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.base}/quizzes/${id}/take`);
  }

  // Progress
  updateProgress(learnerId: number, courseId: number, progress: number) {
    return this.http.put<LearnerProgress>(`${this.base}/progress`, {
      learnerId,
      courseId,
      progress,
    });
  }
  myProgress(learnerId: number): Observable<LearnerProgress[]> {
    return this.http.get<LearnerProgress[]>(`${this.base}/progress`, {
      params: new HttpParams().set('learnerId', learnerId),
    });
  }
  submitQuiz(quizId: number, learnerId: number, answers: Record<number, number>): Observable<QuizResult> {
    return this.http.post<QuizResult>(
      `${this.base}/progress/quiz/${quizId}/submit?learnerId=${learnerId}`,
      answers
    );
  }
  myResults(learnerId: number): Observable<QuizResult[]> {
    return this.http.get<QuizResult[]>(`${this.base}/progress/results`, {
      params: new HttpParams().set('learnerId', learnerId),
    });
  }

  // Admin
  listUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/users`);
  }
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/users/${id}`);
  }
}
