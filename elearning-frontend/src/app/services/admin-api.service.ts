import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiHttpService } from './api-http.service';

/** SRP: admin user/course registry only. */
@Injectable({ providedIn: 'root' })
export class AdminApiService {
  constructor(private api: ApiHttpService) {}
  listUsers(): Observable<any[]> { return this.api.http.get<any[]>(`${this.api.base}/users`); }
  createUser(user: { name: string; email: string; password: string; role: string }): Observable<any> {
    return this.api.http.post<any>(`${this.api.base}/users`, user);
  }
  deleteUser(id: number): Observable<void> {
    return this.api.http.delete<void>(`${this.api.base}/users/${id}`);
  }
}
