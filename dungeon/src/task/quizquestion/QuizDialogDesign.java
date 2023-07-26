package task.quizquestion;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

import core.hud.DialogDesign;
import core.utils.Constants;

public class QuizDialogDesign {

    public static final String ANSWERS_GROUP_NAME = "Answers";

    /**
     * Creates a vertical Button Group based on the answers provided by the QuizQuestion
     *
     * <p>currently does not support Image answers.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     */
    public static VerticalGroup createAnswerButtons(Skin skin, Quiz quizQuestion) {
        VerticalGroup answerButtons = new VerticalGroup();

        ButtonGroup<CheckBox> btnGroup = new ButtonGroup<>();
        btnGroup.setMinCheckCount(0);
        btnGroup.uncheckAll();

        final CheckBox.CheckBoxStyle style =
                switch (quizQuestion.type()) {
                    case SINGLE_CHOICE -> skin.get("radio", CheckBox.CheckBoxStyle.class);
                    default -> skin.get("default", CheckBox.CheckBoxStyle.class);
                };
        quizQuestion
                .contentStream()
                .map(answer -> (Quiz.Content) answer)
                .filter(answer -> answer.type() != Quiz.Content.Type.IMAGE)
                .map(
                        answer ->
                                new CheckBox(
                                        QuizUI.formatStringForDialogWindow(answer.content()),
                                        style))
                .forEach(
                        checkBox -> {
                            btnGroup.add(checkBox);
                            answerButtons.addActor(checkBox);
                            checkBox.left();
                        });

        switch (quizQuestion.type()) {
            case MULTIPLE_CHOICE -> btnGroup.setMaxCheckCount(
                    (int) quizQuestion.contentStream().count());
            case SINGLE_CHOICE -> btnGroup.setMaxCheckCount(1);
        }

        answerButtons.align(Align.left);
        answerButtons.left();
        answerButtons.space(10);

        return answerButtons;
    }

    /**
     * Creates a UI for a Quizquestion
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public static Group createQuizQuestion(Quiz quizQuestion, Skin skin, String outputMsg) {
        Label labelExercise = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
        labelExercise.setColor(Color.YELLOW);
        Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        VerticalGroup vg = new VerticalGroup();
        vg.addActor(labelExercise);
        vg.addActor(visualizeQuestionSection(quizQuestion.question(), skin));
        vg.addActor(labelSolution);
        vg.addActor(visualizeAnswerSection(quizQuestion, skin));
        vg.grow();
        return vg;
    }

    /**
     * creates needed UI Elements to visualize all Question types.
     *
     * <p>QuestionType.TEXT will be shown on a Label, QuestionType.IMAGE will be shown on a Image
     * and QuestionType.TEXT_AND_IMAGE will show the whole Text and then the Image.
     *
     * @param questionContent the {@link Quiz.Content} to show on the hud.
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    private static Group visualizeQuestionSection(Quiz.Content questionContent, Skin skin) {

        VerticalGroup vg = new VerticalGroup();

        switch (questionContent.type()) {
            case TEXT -> vg.addActor(
                    DialogDesign.createScrollPane(
                            skin, new Label(questionContent.content(), skin)));
            case IMAGE -> vg.addActor(
                    DialogDesign.createScrollPane(
                            skin,
                            questionContent
                                    .image()
                                    .orElse(
                                            new Image(
                                                    new Texture(
                                                            "animation/missing_texture.png")))));

            case TEXT_AND_IMAGE -> {
                vg.addActor(
                        DialogDesign.createScrollPane(
                                skin, new Label(questionContent.content(), skin)));
                vg.addActor(
                        DialogDesign.createScrollPane(
                                skin,
                                questionContent
                                        .image()
                                        .orElse(
                                                new Image(
                                                        new Texture(
                                                                "animation/missing_texture.png")))));
            }
            default -> {}
        }
        vg.grow();
        return vg;
    }

    /**
     * Representation of all possible answer options as Single-Choice, Multiple-Choice or as
     * Freetext
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    private static Group visualizeAnswerSection(Quiz quizQuestion, Skin skin) {

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
        vg.grow();
        vg.setName(ANSWERS_GROUP_NAME);
        return vg;
    }
}
