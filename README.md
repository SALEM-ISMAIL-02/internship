# E-Learning Platform (Spring Boot + Angular + MySQL + AI Quiz Generation)

## Stack
- **Backend:** Spring Boot 3 (Web, Data JPA, open API access), MySQL (XAMPP), Apache PDFBox / POI for text extraction, Gemini-compatible AI API for quiz generation.
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
# The backend loads elearning-backend/.env automatically.
# Alternatively set AI_API_KEY in the process environment.
# Windows PowerShell: $env:AI_API_KEY="your-key"
# Optional overrides: $env:AI_MODEL="gemini-flash-latest"
mvn spring-boot:run
```

The backend uses Gemini's OpenAI-compatible endpoint at
`https://generativelanguage.googleapis.com/v1beta/openai`. The key is read only
from `AI_API_KEY`; the local `.env` file is ignored by Git.

## Swagger API testing

With the backend running, open:

`http://localhost:8080/swagger-ui/index.html`

No JWT or bearer token is required. Swagger endpoints can be called directly. `Bearer <JWT>`. Log in through
The frontend sends informational user email and role headers for ownership and
dashboard behavior; these headers are not security controls.

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
