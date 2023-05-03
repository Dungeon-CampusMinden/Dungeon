package quizquestion;

/**
 * A class that askQuizQuestionWithUI provides methods for displaying quiz questions in a graphical
 * user interface.
 */
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

        // todo check the type of the QuizQuestionContent question.qestion()
        // if text -> show it
        // if image -> use the inage path in question.qestion() -> show it
        // if text_and_image -> use a regex to find the path in the text  -> show text and image

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
        // todo setup answer ui
        for (QuizQuestionContent abswer : answers) {
            // todo create a button
            // todo add content on the button
            // todo check the type of the answers
            // if text -> show it
            // if image -> use the inage path in answers -> show it
            // if text_and_image -> use a regex to find the path in the text  -> show text and image
            // todo  if pressed unmark all other buttons and mark this one (maybe weneed some sort
            // of button gropus for this)
            // todo add answer button to answer ui
        }
        // todo return answer ui
        return new Object();
    }

    // todo return ui konstrukt not Object
    private static Object createMultipleChoiceUI(QuizQuestionContent[] answers) {
        // todo setup answer ui
        for (QuizQuestionContent abswer : answers) {
            // todo create a button
            // todo add content on the button
            // todo check the type of the answers
            // if text -> show it
            // if image -> use the inage path in answers -> show it
            // if text_and_image -> use a regex to find the path in the text  -> show text and image
            // todo  if button is pressed mark him, if its presse again unmark him
            // todo add answer button to answer ui

        }
        // todo return answer ui
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
