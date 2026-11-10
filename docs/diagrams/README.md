# E-Learning platform architecture diagrams

This directory contains the report diagrams for the current implementation in
`elearning-backend`. The PlantUML sources are the authoritative artifacts; the
PNG and SVG files are generated exports for Word, LaTeX, or presentation use.

## Diagram catalogue

| # | Diagram | Source | Code basis |
|---|---|---|---|
| 1 | System use cases | `01-usecase-general.puml` | `SecurityConfig`, `User.Role`, controller routes |
| 2 | Detailed AI quiz-generation use case | `02-usecase-detailed-generate-quiz.puml` | `QuizController`, `QuizService`, `AbstractQuizGenerator` |
| 3 | AI quiz-generation sequence | `03-sequence-generate-quiz.puml` | `QuizService` -> `AiQuizGenerator` -> extractors/AI/repositories |
| 4 | Authentication and JWT sequence | `04-sequence-auth.puml` | `AuthController`, `AuthService`, `JwtUtil`, `JwtAuthFilter` |
| 5a | AI client classes | `05a-class-ai.puml` | `ai/AiChatClient` and its adapter/decorators |
| 5b | Text-extraction classes | `05b-class-extract.puml` | `extract/TextExtractor` strategies and factory |
| 5c | Quiz rules and lifecycle classes | `05c-class-quiz-validation.puml` | `quiz`, `validation`, and `state` packages |
| 5d | Services, entities, repositories, audit | `05d-class-services.puml` | `controller`, `service`, `entity`, `repository`, `audit` |
| 6 | Component architecture | `06-diagramme-composants.puml` | Angular API services, Spring controllers/services, integrations |
| 7 | Material upload and extraction activity | `07-activity-upload-extract.puml` | `MaterialService`, file storage/type resolution, extractor factory |
| 8 | Quiz publish/take/submit activity | `08-activity-publish-take.puml` | validation chain, quiz lifecycle, grading, progress |
| 9 | Current local deployment | `09-deployment.puml` | `application.yml`, Maven dependencies, Angular dev server |

`02-uc7-fiche-detaillee.md` is the complementary textual specification for
diagram 2. It intentionally distinguishes implemented behavior from desired
future error handling.

## Render PNG and SVG

PlantUML remains the best fit here because the report needs multiple UML
notations and the sources should remain diffable and editable. Use PlantUML
version 1.2024.6 or newer:

```powershell
Set-Location D:\internship\docs\diagrams
java -jar plantuml.jar -tpng *.puml
java -jar plantuml.jar -tsvg *.puml
```

The generated files are written next to their `.puml` sources. For printed
reports, prefer SVG because it remains vector-based.
