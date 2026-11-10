import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

/**
 * SRP: the ONLY place that knows the API root URL + HttpClient.
 * Domain services depend on this abstraction (DIP), never on HttpClient directly.
 */
@Injectable({ providedIn: 'root' })
export class ApiHttpService {
  readonly base = '/api';
  constructor(readonly http: HttpClient) {}
}
