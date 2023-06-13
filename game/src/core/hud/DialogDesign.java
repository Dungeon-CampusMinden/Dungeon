package core.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

import core.utils.Constants;

import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/** Some of the basic layout needed to show either a TextDialog or a QuestionDialog. */
public class DialogDesign extends VerticalGroup {
    // simple regex which allows path/to/image.png allowed file endings are png/bmp/tiff/jpeg
    private static final String PATTERN_IMAGE_FINDER = "(\\w+[\\\\|/])*\\w+.(?>png|bmp|tiff|jpeg)";

    /** Creates a Left aligned VerticalGroup which completely fills the Parent UI Element. */
    public DialogDesign() {
        super();
        setFillParent(true);
        left();
    }

    /**
     * Simple Helper with default ScollPane Configuration
     *
     * @param skin how the ScrollPane should look like
     * @param container a container which should be scrollable
     * @return the ScrollPane which then can be added to any UI Element
     */
    private static ScrollPane createScrollPane(Skin skin, Actor container) {
        ScrollPane scrollPane = new ScrollPane(container, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);

        return scrollPane;
    }

    /**
     * Simple default Textarea with default Text
     *
     * @param skin how the ScrollPane should look like
     * @return the TextArea which then can be added to any UI Element
     */
    private static TextArea createEditableText(Skin skin) {
        return new TextArea("Click here...", skin);
    }

    /**
     * Creates a simple Dialog which only has static Text shown.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public void createTextDialog(Skin skin, String outputMsg) {
        addActor(createScrollPane(skin, new Label(outputMsg, skin)));
    }

    /**
     * Creates a UI for a Quizquestion
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public void createQuizQuestion(QuizQuestion quizQuestion, Skin skin, String outputMsg) {
        Label labelExercise = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
        labelExercise.setColor(Color.YELLOW);
        addActor(labelExercise);
        visualizeQuestionSection(quizQuestion.question().type(), skin, outputMsg);
        Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        addActor(labelSolution);
        visualizeAnswerSection(quizQuestion, skin);
    }

    /**
     * creates needed UI Elements to vizualize all Question types.
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
    private void visualizeQuestionSection(
            QuizQuestionContent.QuizQuestionContentType questionContentType,
            Skin skin,
            String outputMsg) {

        switch (questionContentType) {
            case TEXT -> addActor(createScrollPane(skin, new Label(outputMsg, skin)));
            case IMAGE -> addActor(
                    createScrollPane(
                            skin, new Image(new Texture(imagePathExtractor(outputMsg).get()))));

            case TEXT_AND_IMAGE -> {
                addActor(createScrollPane(skin, new Label(outputMsg, skin)));
                addActor(
                        createScrollPane(
                                skin, new Image(new Texture(imagePathExtractor(outputMsg).get()))));
            }
            default -> {}
        }
    }

    /**
     * Representation of all possible answer options as Single-Choice, Multiple-Choice or as
     * Freetext
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     */
    private void visualizeAnswerSection(QuizQuestion quizQuestion, Skin skin) {
        switch (quizQuestion.type()) {
            case FREETEXT -> {
                ScrollPane scroller = new ScrollPane(createEditableText(skin), skin);
                scroller.setFadeScrollBars(false);
                scroller.setScrollbarsVisible(true);
                addActor(scroller);
            }
            case MULTIPLE_CHOICE, SINGLE_CHOICE -> {
                VerticalGroup btnGrp = createAnswerButtons(skin, quizQuestion);

                addActor(createScrollPane(skin, btnGrp));
            }
            default -> {}
        }
    }

    /**
     * Creates a vertical Button Group based on the answers provided by the QuizQuestion
     *
     * <p>currently does not support Image answers.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param quizQuestion Various question configurations
     */
    private static VerticalGroup createAnswerButtons(Skin skin, QuizQuestion quizQuestion) {
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
                                        QuizQuestionFormatted.formatStringForDialogWindow(
                                                answer.content()),
                                        style))
                .forEach(
                        checkBox -> {
                            btnGroup.add(checkBox);
                            answerButtons.addActor(checkBox);
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
     * a simple implementation to find a filepath in a String
     *
     * @param quizQuestion the string which may contain a path
     * @return an Optional of either the path or empty when there is no path in the given question
     */
    private Optional<String> imagePathExtractor(String quizQuestion) {
        Optional<MatchResult> first =
                Pattern.compile(PATTERN_IMAGE_FINDER).matcher(quizQuestion).results().findFirst();
        return first.map(MatchResult::group);
    }
}
