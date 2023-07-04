package quest.quizquestion;

import quest.Quest;

public class QuizQuestion extends Quest {

    private final QuizQuestionType type;

    /**
     * The QuizQuestion class represents a single quiz question, including the question itself,
     * possible answer choices, and the type of question.
     *
     * @param question the QuizQuestionContent object representing the question text
     * @param answers an array of QuizQuestionContent objects representing the answer choices
     * @param type the QuizQuestionType representing the type of question
     */
    public QuizQuestion(
            QuizQuestionContent question, QuizQuestionContent[] answers, QuizQuestionType type) {
        super(answers, question);
        this.type = type;
    }

    public QuizQuestionType type() {
        return type;
    }
    /**
     * The QuizQuestionType enum represents the different types of quiz questions that can be
     * created. The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT. SINGLE_CHOICE
     * represents a question with multiple answer choices, where the user is required to select one
     * answer. MULTIPLE_CHOICE represents a question with multiple answer choices, where the user is
     * allowed to select multiple answers. FREETEXT represents a question where the user is required
     * to input their answer as text.
     */
    public enum QuizQuestionType {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        FREETEXT
    }
}
