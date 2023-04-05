package quizquestion;
/**
 * The QuizQuestionType enum represents the different types of quiz questions that can be created.
 * The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT. SINGLE_CHOICE represents a
 * question with multiple answer choices, where the user is required to select one answer.
 * MULTIPLE_CHOICE represents a question with multiple answer choices, where the user is allowed to
 * select multiple answers. FREETEXT represents a question where the user is required to input their
 * answer as text.
 */
public enum QuizQuestionType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    FREETEXT
}
