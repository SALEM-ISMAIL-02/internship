import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiHttpService } from './api-http.service';
import { Course } from './api-models';

/** SRP: course CRUD + lifecycle only. */
@Injectable({ providedIn: 'root' })
export class CourseApiService {
  constructor(private api: ApiHttpService) {}
  list(): Observable<Course[]> { return this.api.http.get<Course[]>(`${this.api.base}/courses`); }
  create(c: Course): Observable<Course> { return this.api.http.post<Course>(`${this.api.base}/courses`, c); }
  update(id: number, c: Course): Observable<Course> {
    return this.api.http.put<Course>(`${this.api.base}/courses/${id}`, c);
  }
  delete(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.base}/courses/${id}`);
  }
  changeStatus(id: number, status: NonNullable<Course['status']>): Observable<Course> {
    return this.api.http.put<Course>(`${this.api.base}/courses/${id}/status`, { status });
  }
}
