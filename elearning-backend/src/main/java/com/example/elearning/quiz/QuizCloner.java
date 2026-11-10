package com.example.elearning.quiz;

import com.example.elearning.entity.Course;
import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import com.example.elearning.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * PROTOTYPE: produces new Quiz instances by cloning an existing one (deep copy of questions)
 * instead of re-running the expensive AI synthesis. Used by "Duplicate quiz" for a new cohort.
 */
@Component
public class QuizCloner {

    /** Clones {@code source} into a fresh, unpublished draft; questions and options are copied. */
    public Quiz clone(Quiz source, Course targetCourse, Long creatorId) {
        Quiz copy = Quiz.builder()
                .course(targetCourse != null ? targetCourse : source.getCourse())
                .createdBy(User.builder().id(creatorId != null ? creatorId : idOf(source.getCreatedBy())).build())
                .aiGenerated(source.isAiGenerated())
                .published(false)
                .questions(new ArrayList<>())
                .build();

        List<Question> cloned = new ArrayList<>();
        for (Question original : source.getQuestions()) {
            Question question = Question.builder()
                    .text(original.getText())
                    .options(new ArrayList<>(original.getOptions()))
                    .correctAnswer(original.getCorrectAnswer())
                    .quiz(copy)
                    .build();
            cloned.add(question);
        }
        copy.setQuestions(cloned);
        return copy;
    }

    private Long idOf(User user) {
        return user == null ? null : user.getId();
    }
}
