import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiHttpService } from './api-http.service';
import { Material } from './api-models';

/** SRP: material upload + listing only. */
@Injectable({ providedIn: 'root' })
export class MaterialApiService {
  constructor(private api: ApiHttpService) {}
  upload(courseId: number, file: File): Observable<Material> {
    const form = new FormData();
    form.append('file', file);
    return this.api.http.post<Material>(`${this.api.base}/materials/upload/${courseId}`, form);
  }
  byCourse(courseId: number): Observable<Material[]> {
    return this.api.http.get<Material[]>(`${this.api.base}/materials/course/${courseId}`);
  }
  delete(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.base}/materials/${id}`);
  }
  fileUrl(id: number): string {
    return `${this.api.base}/materials/${id}/file`;
  }
}
