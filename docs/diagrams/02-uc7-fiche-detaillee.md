# UC — Generate an AI quiz draft from course material

Associated diagram: `02-usecase-detailed-generate-quiz.puml`.

## Primary actor

Instructor or administrator. The route is covered by
`SecurityConfig` as part of `/api/quizzes/**`.

## Preconditions

1. The caller has an authenticated JWT with the required role.
2. A material already exists and points to a file in `./uploads`.
3. The material has extractable text for AI generation.

## Main scenario

1. The frontend sends
   `POST /api/quizzes/generate` with `materialId`, `numQuestions`, and
   `creatorId`.
2. `QuizController` parses the request and delegates to `QuizService`.
3. `QuizService` delegates the generation algorithm to `QuizGenerator`.
4. `AbstractQuizGenerator` loads the material from `MaterialRepository`.
5. `TextExtractorFactory` selects the extractor for PDF, DOCX, PPTX, or a
   null extractor for unsupported types.
6. The extracted text is normalized and truncated to 12,000 characters.
7. `AiQuizGenerator` builds an MCQ prompt and calls the `AiChatClient`
   abstraction. The configured adapter targets Gemini's OpenAI-compatible
   `/chat/completions` endpoint; logging and retry decorators wrap the call.
8. The JSON array response is parsed into `Question` objects.
9. A new `Quiz` is persisted with `published=false` and `aiGenerated=true`.
10. `QuizService` publishes `AuditEvent("QUIZ_GENERATED")` and returns the
    draft.

## Input constraints

`QuizGenerationRequest` requires a non-null `materialId` and a question count
between 1 and 25 inclusive.

## Alternative outcomes as implemented

- Missing material: `IllegalArgumentException` from the generation pipeline.
- Unsupported or empty source: `IllegalStateException` from the extractor
  pipeline.
- Invalid or empty AI output: `IllegalStateException` from
  `AiQuizGenerator`.
- AI transport retries: `RetryChatClient` applies the configured retry policy
  before propagating the failure.

The current `QuizController.generate()` does not catch these generation
exceptions. Therefore the diagram deliberately describes the failure as an
uncaught server error rather than claiming a normalized 400 response.

## Postcondition

On success, an unpublished quiz draft with generated questions is stored and
ready for instructor review and the later publication validation flow.
