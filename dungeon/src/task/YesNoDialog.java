package task;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.hud.DialogDesign;
import contrib.hud.TextDialog;
import contrib.hud.UITools;

import core.Entity;
import core.Game;
import core.utils.IVoidFunction;

import task.quizquestion.QuizUI;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * A Dialog with a "yes" and "no" Button on the Bottom.
 *
 * <p>Use {@link #showYesNoDialog(String, String, IVoidFunction, IVoidFunction)} to create a simple
 * dialog.
 *
 * <p>Use {@link #showYesNoDialog(Task)} to create a dialog that will execute the grading function
 * of the given task.
 */
public class YesNoDialog {

    /**
     * Open a dialog window that shows the given task's text and will execute the {@link
     * Task#gradeTask()} if "yes" is pressed.
     *
     * @param task task that should be graded if the player presses "yes" on the dialog HUD.
     * @return The Entity that stores the HUD components.
     */
    public static Entity showYesNoDialog(final Task task) {
        return showYesNoDialog(task.taskText(), task.taskName(), gradeOn(task), () -> {});
    }

    /**
     * Show a Yes or No Dialog.
     *
     * @param text text to show in the dialog
     * @param title title of the dialog window
     * @param onYes function to execute if "yes" is pressed
     * @param onNo function to execute if "no" is pressed
     * @return Entity that stores the HUD components.
     */
    public static Entity showYesNoDialog(
            final String text,
            final String title,
            final IVoidFunction onYes,
            final IVoidFunction onNo) {

        Entity entity = showYesNoDialog(UITools.DEFAULT_SKIN, text, title, onYes, onNo);
        Game.add(entity);
        return entity;
    }

    /**
     * Show a Yes or No Dialog.
     *
     * @param skin UI skin to use
     * @param text text to show in the dialog
     * @param title title of the dialog window
     * @param onYes function to execute if "yes" is pressed
     * @param onNo function to execute if "no" is pressed
     * @return Entity that stores the HUD components.
     */
    public static Entity showYesNoDialog(
            final Skin skin,
            final String text,
            final String title,
            final IVoidFunction onYes,
            final IVoidFunction onNo) {
        Entity entity = new Entity();

        UITools.show(
                () -> {
                    Dialog dialog =
                            createYesNoDialog(
                                    skin,
                                    text,
                                    title,
                                    createResultHandlerYesNo(
                                            entity,
                                            UITools.DEFAULT_DIALOG_YES,
                                            UITools.DEFAULT_DIALOG_NO,
                                            onYes,
                                            onNo));
                    UITools.centerActor(dialog);
                    return dialog;
                },
                entity);
        Game.add(entity);
        return entity;
    }

    private static Dialog createYesNoDialog(
            final Skin skin,
            final String text,
            final String title,
            final BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
        textDialog
                .getContentTable()
                .add(DialogDesign.createTextDialog(skin, QuizUI.formatStringForDialogWindow(text)))
                .center()
                .grow();
        textDialog.button(UITools.DEFAULT_DIALOG_NO, UITools.DEFAULT_DIALOG_NO);
        textDialog.button(UITools.DEFAULT_DIALOG_YES, UITools.DEFAULT_DIALOG_YES);
        textDialog.pack(); // resizes to size
        return textDialog;
    }

    private static BiFunction<TextDialog, String, Boolean> createResultHandlerYesNo(
            final Entity entity,
            final String yesButtonID,
            final String noButtonID,
            final IVoidFunction onYes,
            final IVoidFunction onNo) {
        return (d, id) -> {
            if (Objects.equals(id, yesButtonID)) {
                onYes.execute();
                Game.remove(entity);
                return true;
            }
            if (Objects.equals(id, noButtonID)) {
                onNo.execute();
                Game.remove(entity);
                return true;
            }
            return false;
        };
    }

    private static IVoidFunction gradeOn(final Task t) {
        return t::gradeTask;
    }
}
