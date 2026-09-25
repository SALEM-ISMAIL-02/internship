import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  template: `
    <header>
      <h2>Admin Dashboard</h2>
      <span>{{ auth.user()?.name }}</span>
      <button (click)="auth.logout()">Logout</button>
    </header>

    <section>
      <h3>Users</h3>
      <table>
        <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th></th></tr>
        <tr *ngFor="let u of users()">
          <td>{{ u.id }}</td><td>{{ u.name }}</td><td>{{ u.email }}</td><td>{{ u.role }}</td>
          <td><button (click)="delete(u.id)">Delete</button></td>
        </tr>
      </table>
    </section>

    <section>
      <h3>Courses</h3>
      <table>
        <tr><th>ID</th><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th></th></tr>
        <tr *ngFor="let c of courses()">
          <td>{{ c.id }}</td><td>{{ c.title }}</td><td>{{ c.category }}</td>
          <td>{{ c.priority }}</td><td>{{ c.status }}</td>
          <td><button (click)="deleteCourse(c.id!)">Delete</button></td>
        </tr>
      </table>
    </section>
  `,
  styles: [
    `
      header { display: flex; align-items: center; gap: 16px; padding: 12px 20px; background: #455a64; color: #fff; }
      header button { margin-left: auto; }
      section { padding: 16px 20px; }
      table { border-collapse: collapse; width: 100%; }
      th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
      th { background: #f5f5f5; }
      button { cursor: pointer; }
    `,
  ],
})
export class AdminDashboardComponent implements OnInit {
  private api = inject(ApiService);
  readonly auth = inject(AuthService);

  users = signal<any[]>([]);
  courses = signal<any[]>([]);

  ngOnInit() {
    this.reload();
  }

  reload() {
    this.api.listUsers().subscribe((u) => this.users.set(u));
    this.api.listCourses().subscribe((c) => this.courses.set(c));
  }

  delete(id: number) {
    this.api.deleteUser(id).subscribe(() => this.reload());
  }

  deleteCourse(id: number) {
    this.api.deleteCourse(id).subscribe(() => this.reload());
  }
}
