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

/** creates layout ot a dialog */
public class DialogDesign extends VerticalGroup {
    public static final String PATTERN_IMAGE_FINDER = "(\\w+[\\\\|/])*\\w+.(?>png|bmp|tiff|jpeg)";

    public DialogDesign() {
        super();
        setFillParent(true);
        left();
    }

    private static ScrollPane createScrollPane(Skin skin, Actor labelContent) {
        ScrollPane scrollPane = new ScrollPane(labelContent, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);
        return scrollPane;
    }

    private static TextArea createEditableText(Skin skin) {
        return new TextArea("Click here...", skin);
    }

    /**
     * Constructor that allows text to be placed and does the layout for the text.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public void TextDialog(Skin skin, String outputMsg) {
        addActor(createScrollPane(skin, new Label(outputMsg, skin)));
    }

    /**
     * Presentation of the layouts of the questions and answers
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public void QuizQuestion(QuizQuestion quizQuestion, Skin skin, String outputMsg) {
        Label labelExercise = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
        labelExercise.setColor(Color.YELLOW);
        addActor(labelExercise);
        VisualizeQuestionSection(quizQuestion.question().type(), skin, outputMsg);
        Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        addActor(labelSolution);

        VisualizeAnswerSection(quizQuestion, skin);
    }

    /**
     * Presentation of all possible variations of the questions as text, image or text and image
     *
     * @param questionContentType represents the different types of quiz questions that can be
     *     created. The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT.
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    private void VisualizeQuestionSection(
            QuizQuestionContent.QuizQuestionContentType questionContentType,
            Skin skin,
            String outputMsg) {

        switch (questionContentType) {
            case TEXT -> addActor(createScrollPane(skin, new Label(outputMsg, skin)));
            case IMAGE -> addActor(
                    createScrollPane(
                            skin, new Image(new Texture(ImagePathExtractor(outputMsg).get()))));

            case TEXT_AND_IMAGE -> {
                addActor(createScrollPane(skin, new Label(outputMsg, skin)));
                addActor(
                        createScrollPane(
                                skin, new Image(new Texture(ImagePathExtractor(outputMsg).get()))));
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
    private void VisualizeAnswerSection(QuizQuestion quizQuestion, Skin skin) {
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
     * Constructor Fills the vertical button group with text boxes and text contents for it
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

    private Optional<String> ImagePathExtractor(String quizqustion) {
        Optional<MatchResult> first =
                Pattern.compile(PATTERN_IMAGE_FINDER).matcher(quizqustion).results().findFirst();
        return first.map(x -> x.group());
    }
}
