package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import quizquestion.QuizQuestion;
import quizquestion.QuizQuestionContent;
import tools.Constants;
import tools.Point;

public class DialogDesign extends Table{

    public DialogDesign()
    {
        super();
        this.setFillParent(true);
    }

    private static final int differenceMeasure = 150;

    public void TextDialog(Skin skin, String outputMsg ) {
        add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
            .size(Constants.WINDOW_WIDTH - differenceMeasure, Constants.WINDOW_HEIGHT - differenceMeasure*2f);
        row();
    }

    public void QuizQuestion( QuizQuestion quizQuestion, Skin skin, String outputMsg ){

        Label labelExersize = new Label(Constants.QUIZ_MESSAGE_TASK, skin);
        labelExersize.setColor(Color.YELLOW);
        add(labelExersize);
        row();

        VisualizeQuestionSection( quizQuestion.question().type(), skin, outputMsg );
        row();

        Label labelSolution = new Label(Constants.QUIZ_MESSAGE_SOLUTION, skin);
        labelSolution.setColor(Color.GREEN);
        add(labelSolution);
        row();

        VisualizeAnswerSection( quizQuestion, skin ) ;
        row();
    }

    private void VisualizeQuestionSection(QuizQuestionContent.QuizQuestionContentType questionContentType, Skin skin, String outputMsg) {
        switch (questionContentType) {
            case TEXT -> add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
                .size(
                    Constants.WINDOW_WIDTH - differenceMeasure,
                    Constants.WINDOW_HEIGHT / 5f);
            case IMAGE ->
                add(new Scroller(skin, new ScreenImage(Constants.TEST_IMAGE_PATH_FOR_DIALOG, new Point(0, 0), 1.1f)))
                    .size(
                        Constants.WINDOW_WIDTH - differenceMeasure,
                        Constants.WINDOW_HEIGHT / 4f);
            case TEXT_AND_IMAGE -> {
                add(new Scroller(skin, new NotEditableText(outputMsg, skin)))
                    .size(
                        Constants.WINDOW_WIDTH - differenceMeasure,
                        Constants.WINDOW_HEIGHT / 5f);
                row();
                add(new Label("", skin));
                row();
                add(new Scroller(skin, new ScreenImage(Constants.TEST_IMAGE_PATH_FOR_DIALOG, new Point(0, 0), 1.1f)))
                    .size(
                        Constants.WINDOW_WIDTH - differenceMeasure,
                        Constants.WINDOW_HEIGHT / 4f);
            }
            default -> {
            }
        }
    }

    private void VisualizeAnswerSection( QuizQuestion quizQuestion, Skin skin) {
        switch (quizQuestion.type()) {
            case FREETEXT -> add(new Scroller(skin, new EditableText(skin)))
                .size(
                    Constants.WINDOW_WIDTH - differenceMeasure,
                    Constants.WINDOW_HEIGHT / 8f);
            case MULTIPLE_CHOICE, SINGLE_CHOICE -> add(new Scroller(skin, new ButtonGroup(skin, quizQuestion)))
                .size(
                    Constants.WINDOW_WIDTH - differenceMeasure,
                    Constants.WINDOW_HEIGHT / 8f);
            default -> {
            }
        }
    }

}
