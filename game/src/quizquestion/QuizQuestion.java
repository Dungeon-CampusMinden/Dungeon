package quizquestion;

public class QuizQuestion {
    private QuizQuestionContent question;
    private QuizQuestionContent[] answers;
    private QuizQuestionType type;

    // later move into type builder  (bitte Kommentar übernehmen)
    public void askQuizQuestionWithUI() {
        QuizQuestionUI.showQuizQuestion(this);
    }
}
