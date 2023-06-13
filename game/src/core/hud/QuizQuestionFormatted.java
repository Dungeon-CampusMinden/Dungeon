package core.hud;

/**
 * A basic QuizQuestion String formatter which creates new linebreaks when the VirtualWindowWidth is
 * not big large enough.
 */
public class QuizQuestionFormatted {

    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     *
     * <p>BlackMagic number which can be tweaked for better line break VirtualWindowWidth / FontSize
     * = MAX_ROW_LENGTH 480 / 12 = 40
     */
    private static final int MAX_ROW_LENGTH = 40;

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
