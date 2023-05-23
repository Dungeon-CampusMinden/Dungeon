package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;
import tools.Constants;
import tools.Point;

/** erzeugt Layout des Dialoges */
public class DialogDesign extends Table {
    private static final int DIFFERENCE_MEASURE = 150;
    private static final String SPACE_STRING = "   ";

    public DialogDesign() {
        super();
        setFillParent(true);
    }
    /** Verschiebung des Dialoges auf Spielbildschirm */

    /**
     * Constructor that allows text to be palced and does the layout for the text.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public void TextDialog(Skin skin, String outputMsg) {
        add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
                .size(
                        Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                        Constants.WINDOW_HEIGHT - DIFFERENCE_MEASURE * 2f);
    }

    /**
     * Presentation of the layouts of the questions and answers
     *
     * @param quizQuestion Various question configurations
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public void QuizQuestion(QuizQuestion quizQuestion, Skin skin, String outputMsg) {
        Label labelExersize = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
        labelExersize.setColor(Color.YELLOW);
        add(labelExersize);
        row();
        VisualizeQuestionSection(quizQuestion, skin, outputMsg);
        row();
        Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        add(labelSolution);
        row();
        VisualizeAnswerSection(quizQuestion, skin);
    }

    /**
     * Presentation of all possible variations of the questions as text, image or text and image
     *
     * @param quizQuestion .
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    private void VisualizeQuestionSection(QuizQuestion quizQuestion, Skin skin, String outputMsg) {
        final QuizQuestionContent.QuizQuestionContentType questionContentType =
                quizQuestion.question().type();
        ScreenImage img = null;
        switch (questionContentType) {
            case TEXT:
                add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
                        .size(
                                Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                                Constants.WINDOW_HEIGHT / 5f);
                break;
            case IMAGE:
                try {
                    img =
                            new ScreenImage(
                                    extractPictutePath(
                                            quizQuestion
                                                    .question()
                                                    .content()), // mit Regex den Pfad für das Bild
                                    // ermitteln umd damit
                                    // "Constants.TEST_IMAGE_PATH_FOR_DIALOG" ersetzen!
                                    new Point(0, 0),
                                    1.1f);
                } catch (Exception e) {
                    final String errorMsg = "Fehler! " + e.getMessage();

                    img =
                            new ScreenImage(
                                    Constants.PICTURE_NOT_FOUND_OR_SPACE_KEY_ERROR,
                                    new Point(0, 0),
                                    1.1f);
                }

                add(new Scroller(skin, img))
                        .size(
                                Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                                Constants.WINDOW_HEIGHT / 5f);
                break;
            case TEXT_AND_IMAGE:
                try {
                    img =
                            new ScreenImage(
                                    extractPictutePath(
                                            quizQuestion
                                                    .question()
                                                    .content()), // Constants.TEST_IMAGE_PATH_FOR_DIALOG,
                                    new Point(0, 0),
                                    1.1f);
                } catch (Exception e) {
                    final String errorMsg = "Fehler! " + e.getMessage();

                    img =
                            new ScreenImage(
                                    Constants.PICTURE_NOT_FOUND_OR_SPACE_KEY_ERROR,
                                    new Point(0, 0),
                                    1.1f);
                }

                add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
                        .size(
                                Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                                Constants.WINDOW_HEIGHT / 5f);
                row();
                add(new Label("", skin));
                row();
                add(new Scroller(skin, img))
                        .size(
                                Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                                Constants.WINDOW_HEIGHT / 5f);
                break;
            default:
                break;
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
            case FREETEXT:
                Table scrollTable = new Table();
                scrollTable.add(new EditableText(skin)).minHeight(800).expandX().fillX().colspan(1);

                ScrollPane scroller = new ScrollPane(scrollTable, skin);
                scroller.setFadeScrollBars(false);
                scroller.setScrollbarsVisible(true);

                add(scroller)
                        .size(
                                Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                                Constants.WINDOW_HEIGHT / 7f);

                break;
            case MULTIPLE_CHOICE, SINGLE_CHOICE:
                ButtonGroup btnGrp = new ButtonGroup(skin, quizQuestion);
                add(new Scroller(skin, btnGrp))
                        .align(Align.left)
                        .size(
                                Constants.WINDOW_WIDTH - DIFFERENCE_MEASURE,
                                Constants.WINDOW_HEIGHT / 7f);
                break;
            default:
                break;
        }
    }

    private String extractPictutePath(String quizQuestion) {
        final int numberOfOccurrences = 1;
        final String regexForFolderAssets =
                "[:alpha::\\\\\\+.\\w]+[\\/{1}\\\\{1}]+.\\w+.*(png|bmp|tiff|jpeg|gif|heic)";
        String[] pictutePaths =
                extracSubStringtWithRegex(quizQuestion, regexForFolderAssets, numberOfOccurrences);

        if (pictutePaths.length == numberOfOccurrences && !pictutePaths[0].contains(" ")) {
            return pictutePaths[0];
        }

        return Constants.PICTURE_NOT_FOUND_OR_SPACE_KEY_ERROR;
    }

    private String[] extracSubStringtWithRegex(
            String textToBeExamined, String regex, Object... numberOfOccurrences) {
        Integer numberOfSearchedSubstrings = 100;

        if (numberOfOccurrences.length > 0) {
            if (!(numberOfOccurrences[0] instanceof Integer)) {
                throw new IllegalArgumentException("...");
            }
            numberOfSearchedSubstrings = (Integer) numberOfOccurrences[0];
        }

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(textToBeExamined);
        int count = 0;
        String[] resultValues = new String[numberOfSearchedSubstrings.intValue()];

        while (matcher.find() && !matcher.group().isEmpty() || count < resultValues.length) {
            try {
                resultValues[count] = textToBeExamined.substring(matcher.start(), matcher.end());
            } catch (Exception e) {
                final String errorMsg = "Fehler! " + e.getMessage();
                System.out.println(errorMsg);
                resultValues[count] = SPACE_STRING;
                break;
            }
            count++;
        }

        return resultValues;
    }
}
