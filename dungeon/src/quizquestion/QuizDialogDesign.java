package quizquestion;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

import core.hud.DialogDesign;
import core.utils.Constants;

import java.util.Arrays;

public class QuizDialogDesign {
    /**
     * Creates a vertical Button Group based on the answers provided by the QuizQuestion
     *
     * <p>currently does not support Image answers.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     */
    public static VerticalGroup createAnswerButtons(Skin skin, QuizQuestion quizQuestion) {
        VerticalGroup answerButtons = new VerticalGroup();

        ButtonGroup<CheckBox> btnGroup = new ButtonGroup<>();
        btnGroup.setMinCheckCount(0);
        btnGroup.uncheckAll();

        final CheckBox.CheckBoxStyle style =
                switch (quizQuestion.type()) {
                    case MULTIPLE_CHOICE -> skin.get("radio", CheckBox.CheckBoxStyle.class);
                    default -> skin.get("default", CheckBox.CheckBoxStyle.class);
                };
        Arrays.stream(quizQuestion.answers())
                .filter(
                        answer ->
                                answer.type() != QuizQuestionContent.QuizQuestionContentType.IMAGE)
                .map(
                        answer ->
                                new CheckBox(
                                        QuizQuestionUI.formatStringForDialogWindow(
                                                answer.content()),
                                        style))
                .forEach(
                        checkBox -> {
                            btnGroup.add(checkBox);
                            answerButtons.addActor(checkBox);
                            checkBox.left();
                        });

        switch (quizQuestion.type()) {
            case MULTIPLE_CHOICE -> btnGroup.setMaxCheckCount(quizQuestion.answers().length);
            case SINGLE_CHOICE -> btnGroup.setMaxCheckCount(1);
        }

        answerButtons.align(Align.left);
        answerButtons.left();

        return answerButtons;
    }

    /**
     * Creates a UI for a Quizquestion
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public static Group createQuizQuestion(QuizQuestion quizQuestion, Skin skin, String outputMsg) {
        Label labelExercise = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
        labelExercise.setColor(Color.YELLOW);
        Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        VerticalGroup vg = new VerticalGroup();
        vg.addActor(labelExercise);
        vg.addActor(visualizeQuestionSection(quizQuestion.question().type(), skin, outputMsg));
        vg.addActor(labelSolution);
        vg.addActor(visualizeAnswerSection(quizQuestion, skin));
        return vg;
    }

    /**
     * creates needed UI Elements to visualize all Question types.
     *
     * <p>QuestionType.TEXT will be shown on a Label, QuestionType.IMAGE will be shown on a Image
     * and QuestionType.TEXT_AND_IMAGE will show the whole Text and then the Image. The Image needs
     * to be loaded from the Filesystem so there could be a IOException thrown if the path is not
     * correct in the Question.
     *
     * @param questionContentType represents the different types of quiz questions that can be
     *     created. The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT.
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    private static Group visualizeQuestionSection(
            QuizQuestionContent.QuizQuestionContentType questionContentType,
            Skin skin,
            String outputMsg) {

        VerticalGroup vg = new VerticalGroup();

        switch (questionContentType) {
            case TEXT -> vg.addActor(
                    DialogDesign.createScrollPane(skin, new Label(outputMsg, skin)));
            case IMAGE -> vg.addActor(
                    DialogDesign.createScrollPane(
                            skin,
                            new Image(
                                    new Texture(
                                            DialogDesign.imagePathExtractor(outputMsg).get()))));

            case TEXT_AND_IMAGE -> {
                vg.addActor(DialogDesign.createScrollPane(skin, new Label(outputMsg, skin)));
                vg.addActor(
                        DialogDesign.createScrollPane(
                                skin,
                                new Image(
                                        new Texture(
                                                DialogDesign.imagePathExtractor(outputMsg)
                                                        .get()))));
            }
            default -> {}
        }

        return vg;
    }

    /**
     * Representation of all possible answer options as Single-Choice, Multiple-Choice or as
     * Freetext
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    private static Group visualizeAnswerSection(QuizQuestion quizQuestion, Skin skin) {

        VerticalGroup vg = new VerticalGroup();

        switch (quizQuestion.type()) {
            case FREETEXT -> {
                ScrollPane scroller = new ScrollPane(DialogDesign.createEditableText(skin), skin);
                scroller.setFadeScrollBars(false);
                scroller.setScrollbarsVisible(true);
                vg.addActor(scroller);
            }
            case MULTIPLE_CHOICE, SINGLE_CHOICE -> {
                VerticalGroup btnGrp = createAnswerButtons(skin, quizQuestion);
                btnGrp.fill();
                btnGrp.left();
                vg.addActor(DialogDesign.createScrollPane(skin, btnGrp));
            }
            default -> {}
        }
        return vg;
    }
}
