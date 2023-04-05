package quizquestion;

public record QuizQuestion(
        QuizQuestionContent question, QuizQuestionContent[] answers, QuizQuestionType type) {
    public void askQuizQuestionWithUI() {
        QuizQuestionUI.showQuizQuestion(this);
    }
}
