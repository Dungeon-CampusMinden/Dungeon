package graphic.hud;

/** Formatting various contents of the widgets */
public class UITools {
    /** beschränkt länge des Strings auf 40 Zeichen, danach erfolgt automatisch ein Zeilenumbruch */
    private static final int maxRowLength = 40;

    /**
     * display the content in the Dialog
     *
     * @param arrayOfMessages optional Content of the text that is printed in the label and Caption
     *     for Dialogue elements
     * @param arrayOfMessages [0]Content displayed in the label [1]Button name [2]label heading
     */
    public static void showInfoText(String... arrayOfMessages) {
        String infoMsg = "";

        if (arrayOfMessages.length > 0) infoMsg = arrayOfMessages[0];

        /** Removes all wraps and replaces them with blanks */
        infoMsg = infoMsg.replaceAll("\n", " ");

        String[] words = infoMsg.split(" ");
        String formatedMsg = "";

        /** maxRowLength limits line length to a maximum of 40 characters */
        int sumLength = 0;

        /** String formatting after certain line length */
        for (String word : words) {
            sumLength += word.length();
            formatedMsg = formatedMsg + word + " ";

            if (sumLength > maxRowLength) {
                formatedMsg += "\n";
                sumLength = 0;
            }
        }
        arrayOfMessages[0] = formatedMsg;
    }
}
