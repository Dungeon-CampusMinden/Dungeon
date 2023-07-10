package core.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

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
    public static ScrollPane createScrollPane(Skin skin, Actor container) {
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
    public static TextArea createEditableText(Skin skin) {
        return new TextArea("Click here...", skin);
    }

    /**
     * Creates a simple Dialog which only has static Text shown.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public static Group createTextDialog(Skin skin, String outputMsg) {
        return createScrollPane(
                skin, new Container<>(new Label(outputMsg, skin)).align(Align.center));
    }

    /**
     * a simple implementation to find a filepath in a String
     *
     * @param quizQuestion the string which may contain a path
     * @return an Optional of either the path or empty when there is no path in the given question
     */
    public static Optional<String> imagePathExtractor(String quizQuestion) {
        Optional<MatchResult> first =
                Pattern.compile(PATTERN_IMAGE_FINDER).matcher(quizQuestion).results().findFirst();
        return first.map(MatchResult::group);
    }
}

// <<<<<<< HEAD
// import com.badlogic.gdx.graphics.Color;
// import com.badlogic.gdx.scenes.scene2d.ui.*;
// import com.badlogic.gdx.utils.Align;
// import core.utils.Constants;
// import core.utils.Point;
// import quizquestion.QuizQuestion;
// import quizquestion.QuizQuestionContent;
//
//    /** creates layout ot a dialog */
//    public class DialogDesign extends Table {
//        private static final int DIFFERENCE_MEASURE = 150;
//
//        public DialogDesign() {
//            super();
//            setFillParent(true);
//        }
//
/// **
// * Constructor that allows text to be placed and does the layout for the text.
// public void TextDialog(Skin skin, String outputMsg) {
//    add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT - DIFFERENCE_MEASURE * 2f);
//    }
//
/// **
// * Presentation of the layouts of the questions and answers
// *
// * @param quizQuestion Various question configurations
// * @param skin Skin for the dialogue (resources that can be used by UI widgets)
// * @param outputMsg Content displayed in the scrollable label
// */
// public void QuizQuestion(QuizQuestion quizQuestion, Skin skin, String outputMsg) {
//    Label labelExercise = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
//    labelExercise.setColor(Color.YELLOW);
//    add(labelExercise);
//    row();
//    VisualizeQuestionSection(quizQuestion.question().type(), skin, outputMsg);
//    row();
//    Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
//    labelSolution.setColor(Color.GREEN);
//    add(labelSolution);
//    row();
//    VisualizeAnswerSection(quizQuestion, skin);
//    }
//
/// **
// * Presentation of all possible variations of the questions as text, image or text and image
// *
// * @param questionContentType represents the different types of quiz questions that can be
// *     created. The available types are SINGLE_CHOICE, MULTIPLE_CHOICE, and FREETEXT.
// * @param skin Skin for the dialogue (resources that can be used by UI widgets)
// * @param outputMsg Content displayed in the scrollable label
// */
// private void VisualizeQuestionSection(
//    QuizQuestionContent.QuizQuestionContentType questionContentType,
//    Skin skin,
//    String outputMsg) {
//    switch (questionContentType) {
//    case TEXT -> add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT / 5f);
//    case IMAGE -> add(new Scroller(
//    skin,
//    new ScreenImage(
//    Constants.TEST_IMAGE_PATH_FOR_DIALOG, new Point(0, 0), 1.1f)))
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT / 5f);
//    case TEXT_AND_IMAGE -> {
//    add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT / 5f);
//    row();
//    add(new Label("", skin));
//    row();
//    add(new Scroller(
//    skin,
//    new ScreenImage(
//    Constants.TEST_IMAGE_PATH_FOR_DIALOG,
//    new Point(0, 0),
//    1.1f)))
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT / 5f);
//    }
// default -> {}
//    }
//    }
//
/// **
// * Representation of all possible answer options as Single-Choice, Multiple-Choice or as
// * Freetext
// *
// * @param quizQuestion Various question configurations
// * @param skin Skin for the dialogue (resources that can be used by UI widgets)
// */
// private void VisualizeAnswerSection(QuizQuestion quizQuestion, Skin skin) {
//    switch (quizQuestion.type()) {
//    case FREETEXT -> {
//    Table scrollTable = new Table();
//    scrollTable.add(new EditableText(skin)).minHeight(800).expandX().fillX().colspan(1);
//    ScrollPane scroller = new ScrollPane(scrollTable, skin);
//    scroller.setFadeScrollBars(false);
//    scroller.setScrollbarsVisible(true);
//    add(scroller)
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT / 7f);
//    }
//    case MULTIPLE_CHOICE, SINGLE_CHOICE -> {
//    ButtonGroup btnGrp = new ButtonGroup(skin, quizQuestion);
//    add(new Scroller(skin, btnGrp))
//    .align(Align.left)
//    .size(
//    Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
//    Constants.WINDOW_HEIGHT / 7f);
//    }
// default -> {}
//    }
//    =======
