package com.example.elearning;

import com.example.elearning.ai.AiChatClient;
import com.example.elearning.entity.Course;
import com.example.elearning.entity.Material;
import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import com.example.elearning.quiz.QuizGenerator;
import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.MaterialRepository;
import com.example.elearning.service.CourseService;
import com.example.elearning.service.MaterialService;
import com.example.elearning.service.ProgressService;
import com.example.elearning.service.QuizService;
import com.example.elearning.service.UserAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * SOLID regression test: exercises the full SOLID-wired graph
 * (QuizGenerator abstraction, QuizService, ProgressService+QuizGradingService,
 * CourseService, MaterialService with FileStorage/FileType collaborators)
 * against H2, with only the AI port faked.
 */
@SpringBootTest
@ActiveProfiles("test")
class SolidWiringIT {

    @MockBean
    AiChatClient aiChatClient;

    @Autowired
    CourseService courseService;
    @Autowired
    MaterialService materialService;
    @Autowired
    MaterialRepository materialRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    QuizService quizService;
    @Autowired
    QuizGenerator quizGenerator;
    @Autowired
    ProgressService progressService;
    @Autowired
    UserAdminService userAdminService;
    @Autowired
    com.example.elearning.repository.UserRepository userRepository;

    @Test
    @org.springframework.transaction.annotation.Transactional
    void fullFlowWorks() throws Exception {
        when(aiChatClient.complete(anyString())).thenReturn(
                "[{\"text\":\"Q1?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correctAnswer\":1},"
                        + "{\"text\":\"Q2?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correctAnswer\":0},"
                        + "{\"text\":\"Q3?\",\"options\":[\"A\",\"B\",\"C\",\"D\"],\"correctAnswer\":2}]");

        // Course aggregate via service (DIP: controller no longer resolves instructor).
        Course course = courseService.create(Course.builder()
                .title("Lockout").priority(Course.Priority.MANDATORY).build(), null);
        assertThat(course.getId()).isNotNull();

        // H2 enforces FKs: a real creator + learner row is needed.
        com.example.elearning.entity.User creator = userRepository.save(
                com.example.elearning.entity.User.builder()
                        .name("Tester").email("tester@example.com").passwordHash("x")
                        .role(com.example.elearning.entity.User.Role.INSTRUCTOR).build());

        // Material row created directly (upload covers FS; extract covers strategy).
        // NOTE: in the test profile a test-only OTHER extractor is registered (OCP),
        // so OTHER now yields "test source text" while VIDEO still falls back to Null ("").
        Material material = materialRepository.save(Material.builder()
                .type(Material.MaterialType.OTHER).filePath("n/a").originalName("x.txt")
                .course(course).build());
        assertThat(materialService.extractText(material)).isEqualTo("test source text");

        // Quiz: generate -> publish (validation chain) -> take (answers hidden, entity untouched).
        Quiz draft = quizService.generate(material.getId(), 3, creator.getId(), "tester");
        assertThat(draft.getQuestions()).hasSize(3);
        Quiz published = quizService.publish(draft.getId(), null, "tester");
        assertThat(published.isPublished()).isTrue();
        Quiz take = quizService.takeableCopy(published.getId());
        assertThat(take.getQuestions()).allMatch(q -> q.getCorrectAnswer() == -1);

        // Grading via ProgressService -> QuizGradingService -> QuizScoringPolicy (SRP chain).
        // Reload ids inside the session (avoids LazyInitialization on detached collections).
        List<Long> qIds = published.getQuestions().stream().map(Question::getId).toList();
        Map<Long, Integer> answers = Map.of(qIds.get(0), 1, qIds.get(1), 0, qIds.get(2), 2);
        var result = progressService.submitQuiz(creator.getId(), published.getId(), answers);
        assertThat(result.getScore()).isEqualTo(100);
        assertThat(progressService.getProgress(creator.getId()).get(0).getProgress()).isEqualTo(25);

        // QuizGenerator resolves to the concrete AiQuizGenerator through the abstraction.
        assertThat(quizGenerator.getClass().getSimpleName()).isEqualTo("AiQuizGenerator");

        // Admin read path + validation failure path.
        assertThat(userAdminService.list()).isNotNull();
        Quiz empty = quizService.duplicate(published.getId(), null, "tester");
        empty.getQuestions().clear();
        assertThatThrownBy(() -> quizService.publish(empty.getId(), empty, "tester"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no questions");

        // Course lifecycle via service.
        assertThat(courseService.changeStatus(course.getId(), Course.Status.PUBLISHED).getStatus())
                .isEqualTo(Course.Status.PUBLISHED);
        assertThat(courseService.list()).isNotEmpty();
    }

    @Test
    void nullExtractorFallbackKeepsLspContract() throws Exception {
        assertThat(materialService.extractText(Material.builder()
                .type(Material.MaterialType.VIDEO).filePath("n/a").build())).isEqualTo("");
    }

    @Test
    void fileTypeResolverIsRegistryBased(@Autowired com.example.elearning.service.FileTypeResolver resolver) {
        assertThat(resolver.resolve("doc.pdf")).isEqualTo(Material.MaterialType.PDF);
        assertThat(resolver.resolve("slides.pptx")).isEqualTo(Material.MaterialType.PPT);
        assertThat(resolver.resolve("notes.docx")).isEqualTo(Material.MaterialType.DOC);
        resolver.register("md", Material.MaterialType.OTHER);
        assertThat(resolver.resolve("readme.md")).isEqualTo(Material.MaterialType.OTHER);
    }
}
