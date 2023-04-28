package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import ecs.systems.ECS_System;
import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;
import starter.Game;
import tools.Constants;

/**
 * Formatting of the main message (content displayed in the label) and controls the creation of a
 * dialogue object depending on an event.
 */
public class UITools {
    /** index of the dialogue in the controller */
    private static int indexForDialogueInController;
    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     */
    private static final int maxRowLength = 40;

    /**
     * display the content in the Dialog
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

   public static void showQuizDialog(QuizQuestion question) {

        if( question != null) {
            String[] contentArray = {question.question().content()};
            formatStringForDialogWindow(contentArray);
            setDialogIndexInController(-1);
            generateQuizDialogue(question, contentArray);
        }
   }

    private static void formatStringForDialogWindow(String[] arrayOfMessages) {
        if (arrayOfMessages != null && arrayOfMessages.length != 0) {
            String infoMsg = arrayOfMessages[0];
            infoMsg = infoMsg.replaceAll("\n", " ");

            String[] words = infoMsg.split(" ");
            String formatedMsg = Constants.EMPTY_MESSAGE;
            int sumLength = 0;

            for (String word : words) {
                sumLength += word.length();
                formatedMsg = formatedMsg.concat(word).concat(" ");

                if (sumLength > maxRowLength) {
                    formatedMsg += "\n";
                    sumLength = 0;
                }
            }
            arrayOfMessages[0] = formatedMsg;
        }
    }
    /**
     * set index of the dialogue in the controller
     *
     * @param index Index für den Text-Dialog der im Controller gefunden wurde
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
     * After leaving the dialogue, it is removed from the stage, the game is unpaused by releasing
     * all systems and deleting the dialogue Object.
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
                Game.systems.forEach(ECS_System::run);
            }
        }
    }

    /**
     * If no dialogue is created, a new dialogue is created according to the event key. Pause all
     * systems except DrawSystem
     *
     * @param arrayOfMessages Contains the text of the message in the dialogue and can contain the
     *     title of the dialogue and the button.
     */
    private static void generateTextDialogue(String... arrayOfMessages) {
        searchIndexOfResponsiveDialogInController(null);

        if (indexForDialogueInController == -1 && Game.controller != null && Game.systems != null) {
            Game.controller.add(
                new ResponsiveDialogue(
                    new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
                    Color.WHITE,
                    arrayOfMessages));

            Game.systems.forEach(ECS_System::stop);
        }
    }

    /**
     */
    private static void generateQuizDialogue(QuizQuestion question, String... arrayOfMessages) {
        searchIndexOfResponsiveDialogInController(null);

        if (indexForDialogueInController == -1 && Game.controller != null && Game.systems != null) {
            Game.controller.add(
                new ResponsiveDialogue(
                    new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
                    Color.WHITE,
                    question,
                    arrayOfMessages));

            Game.systems.forEach(ECS_System::stop);
        }
    }
}
