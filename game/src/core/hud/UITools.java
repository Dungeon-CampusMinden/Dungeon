package core.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import core.Game;
import core.System;
import core.utils.Constants;

import quizquestion.QuizQuestion;

/**
 * Formatting of the window or dialog and controls the creation of a dialogue object depending on an
 * event.
 */
public class UITools {
    /** index of the dialogue in the controller. */
    private static int indexForDialogueInController;
    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     */
    private static final int MAX_ROW_LENGTH = 40;

    /**
     * display the Text-content (Info Message) in the Dialog
     *
     * @param arrayOfMessages Content 'msg', which is to be output on the screen, optional the name
     *     of the button, as well as the label heading can be passed. [0] Content displayed in the
     *     label; [1] Button name; [2]label heading
     */
    public static void showInfoText(String... arrayOfMessages) {
        formatStringForDialogWindow(arrayOfMessages);
        setDialogIndexInController(-1);
        generateTextDialogue(arrayOfMessages);
    }
    /**
     * display the Question-Content (Question and answer options (no pictures) as text, picture,
     * text and picture, single or multiple choice ) in the Dialog
     *
     * @param question Various question configurations
     */
    public static void showQuizDialog(QuizQuestion question) {

        if (question != null) {
            String[] contentArray = {question.question().content()};
            formatStringForDialogWindow(contentArray);
            setDialogIndexInController(-1);
            generateQuizDialogue(question, contentArray);
        }
    }
    /**
     * String formatting for content of the 'msg'(message) to be output on the screen
     *
     * @param arrayOfMessages Content 'msg', which is to be output on the screen, optional the name
     *     of the button, as well as the label heading can be passed. [0] Content displayed in the
     *     label; [1] Button name; [2]label heading
     */
    private static void formatStringForDialogWindow(String[] arrayOfMessages) {
        if (arrayOfMessages != null && arrayOfMessages.length != 0) {
            String infoMsg = arrayOfMessages[0];
            infoMsg = infoMsg.replaceAll("\n", " ");

            String[] words = infoMsg.split(" ");
            String formattedMsg = Constants.EMPTY_MESSAGE;
            int sumLength = 0;

            for (String word : words) {
                sumLength += word.length();
                formattedMsg = formattedMsg.concat(word).concat(" ");

                if (sumLength > MAX_ROW_LENGTH) {
                    formattedMsg += "\n";
                    sumLength = 0;
                }
            }
            arrayOfMessages[0] = formattedMsg;
        }
    }
    /**
     * set index of the dialogue in the controller
     *
     * @param index Index for the text dialogue found in the controller
     */
    public static void setDialogIndexInController(final int index) {
        indexForDialogueInController = index;
    }
    /**
     * searches for ResponsiveDialog in the controller. If it is contained, an index is used to
     * determine the position of the text dialogue.
     *
     * @param txtDialog Text dialogue, which is part of the ResponsiveDialogue and is also searched
     *     for in the controller.
     */
    private static void searchIndexOfResponsiveDialogInController(final Dialog txtDialog) {
        Game.controller
                .iterator()
                .forEachRemaining(
                        elementFromController -> {
                            for (int count = 0; count < Game.controller.size(); count++) {
                                if (elementFromController instanceof ResponsiveDialogue) {
                                    if (txtDialog == null
                                            || elementFromController.contains(txtDialog)) {
                                        setDialogIndexInController(count);
                                    }
                                }
                            }
                        });
    }

    /**
     * After leaving the dialogue, it is removed from the stage, the game will be continued by
     * releasing all systems and deleting the dialogue Object.
     *
     * @param txtDialog Text dialogue, which is part of the ResponsiveDialogue and is also searched
     *     for in the controller.
     */
    public static void deleteDialogue(Dialog txtDialog) {
        if (txtDialog != null) {
            searchIndexOfResponsiveDialogInController(txtDialog);

            if (indexForDialogueInController >= 0
                    && Game.controller != null
                    && Game.systems != null) {
                Game.controller.remove(indexForDialogueInController);
                Game.systems.forEach(System::stop);
            }
        }
    }

    /**
     * If no Text-Dialogue is created, a new dialogue is created according to the event key. Pause
     * all systems except DrawSystem
     *
     * @param arrayOfMessages Contains the text of the message in the dialogue and can contain the
     *     title of the dialogue and the button.
     */
    private static void generateTextDialogue(String... arrayOfMessages) {
        searchIndexOfResponsiveDialogInController(null);

        if (indexForDialogueInController == -1 && Game.controller != null && Game.systems != null) {
            Game.controller.add(
                    new ResponsiveDialogue<>(
                            new SpriteBatch(),
                            new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
                            Color.WHITE,
                            arrayOfMessages));

            Game.systems.forEach(System::stop);
        }
    }

    /**
     * If no Quiz-Dialogue is created, a new dialogue is created according to the event key. Pause
     * all systems except DrawSystem
     *
     * @param question Various question configurations
     * @param arrayOfMessages Content 'msg'(message), which is to be output on the screen, optional
     *     the name of the button, as well as the label heading can be passed. [0] Content displayed
     *     in the label; [1] Button name; [2]label heading
     */
    private static void generateQuizDialogue(QuizQuestion question, String... arrayOfMessages) {
        searchIndexOfResponsiveDialogInController(null);

        if (indexForDialogueInController == -1 && Game.controller != null && Game.systems != null) {
            Game.controller.add(
                    new ResponsiveDialogue<>(
                            new SpriteBatch(),
                            new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
                            Color.WHITE,
                            question,
                            arrayOfMessages));

            Game.systems.forEach(System::stop);
        }
    }
}
