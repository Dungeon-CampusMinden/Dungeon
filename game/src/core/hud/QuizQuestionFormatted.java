package core.hud;

import core.utils.Constants;

import quizquestion.QuizQuestion;

public class QuizQuestionFormatted {

    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     */
    private static final int MAX_ROW_LENGTH = 40;

    public static QuizQuestionFormatted parse(QuizQuestion question) {
        return new QuizQuestionFormatted();
    }

    /**
     * All values for the variable Identifier are correctly read from the parameter arrayOfMessages
     * and assigned to it.
     *
     * @param formatIdentifier Parameters that are passed to the dialogue
     * @param arrayOfMessages Parameters defined by the user
     */
    private void setupMessagesForIdentifier(String[] formatIdentifier, String... arrayOfMessages) {
        final int inputArraySize = arrayOfMessages.length;
        final int outputArraySize = formatIdentifier.length;
        final String[] defaultCaptions = {
            Constants.DEFAULT_MESSAGE, Constants.DEFAULT_BUTTON_MESSAGE, Constants.DEFAULT_HEADING
        };

        for (int counter = 0; counter < outputArraySize; counter++) {
            if (inputArraySize > counter
                    && arrayOfMessages[counter] != null
                    && arrayOfMessages[counter].length() > 0)
                formatIdentifier[counter] = arrayOfMessages[counter];
            else formatIdentifier[counter] = defaultCaptions[counter];
        }
    }

    /**
     * creates line breaks after a word once a certain char count is reached
     *
     * @param string which should be reformatted.
     */
    static String formatStringForDialogWindow(String string) {
        String[] words = string.split(" ");
        StringBuilder formattedMsg = new StringBuilder(string.length());
        int sumLength = 0;

        for (String word : words) {
            sumLength += word.length();
            formattedMsg.append(word);
            formattedMsg.append(" ");

            if (sumLength > MAX_ROW_LENGTH) {
                formattedMsg.append("\n");
                sumLength = 0;
            }
        }
        return formattedMsg.toString();
    }
}
