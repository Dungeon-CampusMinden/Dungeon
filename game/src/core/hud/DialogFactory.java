package core.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import quizquestion.QuizQuestion;

import java.util.function.BiFunction;

public class DialogFactory {

    /**
     * Constructor for Quiz Question
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     * @param outputMsg Content displayed in the scrollable label
     * @param buttonMsg text for the button
     * @param title Title of the dialogue
     */
    public static Dialog createQuizDialog(
            Skin skin,
            QuizQuestion quizQuestion,
            String outputMsg,
            String buttonMsg,
            String title,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, resultHandler);
        DialogDesign dialogDesign = new DialogDesign();
        dialogDesign.QuizQuestion(quizQuestion, skin, outputMsg);
        textDialog.button(buttonMsg, buttonMsg);
        textDialog
                .getContentTable()
                .add(dialogDesign)
                .expand()
                .fill(); // changes size based on childrens
        textDialog.pack(); // resizes to size
        return textDialog;
    }

    public static Dialog createTextDialog(
            Skin skin,
            String outputMsg,
            String buttonMsg,
            String title,
            BiFunction<TextDialog, String, Boolean> resultHandler) {
        Dialog textDialog = new TextDialog(title, skin, resultHandler);
        DialogDesign dialogDesign = new DialogDesign();
        dialogDesign.TextDialog(skin, outputMsg);
        textDialog.addActor(dialogDesign);
        textDialog.button(buttonMsg, buttonMsg);
        return textDialog;
    }
}
