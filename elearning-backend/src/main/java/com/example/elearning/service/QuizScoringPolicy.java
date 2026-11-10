package com.example.elearning.service;

import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SRP: ONLY scores a quiz. No persistence, no progress rules.
 * OCP: scoring algorithm is closed here; new strategies (negative marking, ...)
 * are new classes implementing {@link QuizScoringPolicy}.
 */
public interface QuizScoringPolicy {

    int score(Quiz quiz, java.util.Map<Long, Integer> answers);
}
