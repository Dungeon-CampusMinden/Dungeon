package quizquestion;

/** A class that provides methods for displaying quiz questions in a graphical user interface. */
public class QuizQuestionUI {

    /**
     * Displays a quiz question in a graphical user interface, allowing the user to select or input
     * an answer. This method takes a QuizQuestion object as input and displays it in a user
     * interface window. The user interface consists of a question text and an answer UI element,
     * which is generated based on the type of the question. An "OK" button is also displayed to
     * allow the user to submit their answer.
     *
     * @param question the QuizQuestion object to be displayed in the user interface
     */
    public static void showQuizQuestion(QuizQuestion question) {
        // todo setup basic ui window
        // todo add "Dialog" (?) with Questiontext (question.question() )
        // todo replace type Object with correct type
        Object answerUI;
        switch (question.type()) {
            case SINGLE_CHOICE -> answerUI = createSingleChoiceUI(question.answers());
            case MULTIPLE_CHOICE -> answerUI = createMultipleChoiceUI(question.answers());
            case FREETEXT -> answerUI = createFreetextInputField();
        }
        // todo add answerUI to the ui window

        // todo replace type Object with correct type
        Object okButton = createOkButton();
        // todo if okButton is pressed => close and delete ui window
        // todo add okButton to ui window
        // todo show ui window
    }

    // todo return ui konstrukt not Object
    private static Object createSingleChoiceUI(QuizQuestionContent[] answers) {
        // todo create a button for each answer
        // todo for each button -> if pressed unmark all other buttons and mark this one (maybe we
        // need some sort of button gropus for this)
        return new Object();
    }

    // todo return ui konstrukt not Object
    private static Object createMultipleChoiceUI(QuizQuestionContent[] answers) {
        // todo create a button for each answer
        // todo for each button -> if button is pressed mark him, if its presse again unmark him
        // (repeat)
        return new Object();
    }

    // todo return ui konstrukt not Object
    private static Object createFreetextInputField() {
        // todo create freetext input field
        return new Object();
    }

    // todo return Button-Objekt not Object
    private static Object createOkButton() {
        // todo create Button with "Ok"
        return new Object();
    }
}
