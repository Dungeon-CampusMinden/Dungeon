package quizquestion;
/**
 * The QuizQuestion class represents a single quiz question, including the question itself, possible
 * answer choices, and the type of question.
 *
 * @param question the QuizQuestionContent object representing the question text
 * @param answers an array of QuizQuestionContent objects representing the answer choices
 * @param type the QuizQuestionType representing the type of question
 */
public record QuizQuestion(
        QuizQuestionContent question, QuizQuestionContent[] answers, QuizQuestionType type) {
    /**
     * Asks the quiz question in a graphical user interface by calling the showQuizQuestion() method
     * of the QuizQuestionUI class.
     */
    public void askQuizQuestionWithUI() {
        QuizQuestionUI.showQuizQuestion(this);
    }
}
