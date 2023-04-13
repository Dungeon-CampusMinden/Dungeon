package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import ecs.systems.ECS_System;
import starter.Game;
import tools.Constants;

/**
 * Formatting of the main message (content displayed in the label) and controls the creation of a
 * dialogue object depending on an event.
 */
public class UITools {
    /**
     * Limits the length of the string to 40 characters, after which a line break occurs
     * automatically.
     */
    private static final int maxRowLength = 40;

    private static final String emptyMessage = "";

    /**
     * display the content in the Dialog
     *
     * @param arrayOfMessages Content 'msg', which is to be output on the screen, optional the name
     *     of the button, as well as the label heading can be passed. [0] Content displayed in the
     *     label; [1] Button name; [2]label heading
     */
    public static void showInfoText(String... arrayOfMessages) {
        formatStringForDialogWindow(arrayOfMessages);
        generateDialogue(arrayOfMessages);
    }

    private static void formatStringForDialogWindow(String[] arrayOfMessages) {
        if (arrayOfMessages != null && arrayOfMessages.length != 0) {
            String infoMsg = arrayOfMessages[0];
            infoMsg = infoMsg.replaceAll("\n", " ");

            String[] words = infoMsg.split(" ");
            String formatedMsg = emptyMessage;
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
     * After leaving the dialogue, it is removed from the stage, the game is unpaused by releasing
     * all systems and deleting the dialogue Object.
     */
    public static void deleteDialogue(Dialog dialog) {
        if (dialog != null) {

            if (Game.controller != null) Game.controller.remove(dialog);

            if (Game.systems != null) {
                Game.systems.forEach(ECS_System::run);
            }
        }
    }

    /**
     * If no dialogue is created, a new dialogue is created according to the event key. Pause all
     * systems except DrawSystem
     */
    private static void generateDialogue(String... arrayOfMessages) {
        ResponsiveDialogue dialog =
                new ResponsiveDialogue(
                        new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
                        Color.WHITE,
                        arrayOfMessages);

        if (Game.controller != null) Game.controller.add(dialog);
        if (Game.systems != null) {
            Game.systems.forEach(ECS_System::stop);
        }
    }
}
