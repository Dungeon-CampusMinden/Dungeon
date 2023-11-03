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

public class YesNoDialog {

    public static Entity showYesNoDialog(Task t) {
        return showYesNoDialog();
    }

    public static Entity showYesNoDialog(
            String text, String title, IVoidFunction onYes, IVoidFunction onNo) {

        Entity entity = showYesNoDialog(UITools.DEFAULT_SKIN, text, title, onYes, onNo);
        Game.add(entity);
        return entity;
    }

    public static Entity showYesNoDialog(
            Skin skin, String text, String title, IVoidFunction onYes, IVoidFunction onNo) {
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
            Skin skin,
            String text,
            String title,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
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

    public static BiFunction<TextDialog, String, Boolean> createResultHandlerYesNo(
            final Entity entity,
            final String yesButtonID,
            String noButtonID,
            IVoidFunction onYes,
            IVoidFunction onNo) {
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

    public static IVoidFunction gradeOn(Task t) {
        return t::gradeTask;
    }
}
