# E-Learning Platform (Spring Boot + Angular + MySQL + AI Quiz Generation)

## Stack
- **Backend:** Spring Boot 3 (Web, Data JPA, Security + JWT), MySQL (XAMPP), Apache PDFBox / POI for text extraction, OpenRouter-compatible AI API for quiz generation.
- **Frontend:** Angular 21 (standalone components, signals), role-based routing, dev proxy to backend.
- **VCS:** Git.

## Layout
- `elearning-backend/` â€” Spring Boot API (port 8080)
- `elearning-frontend/` â€” Angular app (port 4200, proxies `/api` â†’ 8080)

## Database (XAMPP)
1. Start **Apache/MySQL** in XAMPP control panel.
2. The app auto-creates the `elearning_db` schema (`createDatabaseIfNotExist=true`).
3. Default DB credentials in `elearning-backend/src/main/resources/application.yml` (root / empty password â€” adjust to your XAMPP setup).

Tables created via JPA (`ddl-auto: update`): `users`, `courses`, `materials`, `quizzes`, `questions`, `question_options`, `learner_progress`, `quiz_results`.

## Run backend
```bash
cd elearning-backend
# set your AI key (OpenRouter free tier or any OpenAI-compatible API)
set AI_API_KEY=sk-or-...        (PowerShell: $env:AI_API_KEY="sk-or-...")
mvn spring-boot:run
```

## Run frontend
```bash
cd elearning-frontend
npm install
npm start        # http://localhost:4200
```

## AI Quiz workflow
1. Instructor creates a course and uploads a PDF/DOCX/PPTX material.
2. Backend extracts text (PDFBox for PDF, POI for Word/PPT).
3. `POST /api/quizzes/generate` sends the text to the AI API â†’ parsed MCQs stored as an **unpublished** quiz.
4. Instructor reviews and calls `PUT /api/quizzes/{id}/publish` to release it to learners.

## API overview
| Endpoint | Method | Role |
|---|---|---|
| `/api/auth/register`, `/api/auth/login` | POST | public |
| `/api/courses` (CRUD) | GET/POST/PUT/DELETE | GET public; writes instructor/admin |
| `/api/materials/upload/{courseId}` | POST (multipart) | instructor/admin |
| `/api/quizzes/generate` | POST | instructor/admin |
| `/api/quizzes/{id}/publish` | PUT | instructor/admin |
| `/api/quizzes/{id}/take` | GET | learner |
| `/api/progress`, `/api/progress/quiz/{id}/submit` | PUT/POST | learner |
| `/api/users` | GET/DELETE | admin |
