package task.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogDesign;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.FreeText;
import task.tasktype.quizquestion.MultipleChoice;
import task.tasktype.quizquestion.SingleChoice;

/** WTF? . */
public class QuizDialogDesign {
  /** The name of the answers group. */
  public static final String ANSWERS_GROUP_NAME = "Answers";

  private static final String QUIZ_MESSAGE_TASK = "Aufgabenstellung";
  private static final String QUIZ_MESSAGE_SOLUTION = "Answers:";

  /**
   * Creates a vertical Button Group based on the answers provided by the QuizQuestion
   *
   * <p>currently does not support Image answers.
   *
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @param quizQuestion Various question configurations
   * @return foo
   */
  public static VerticalGroup createAnswerButtons(Skin skin, Quiz quizQuestion) {
    VerticalGroup answerButtons = new VerticalGroup();

    ButtonGroup<CheckBox> btnGroup = new ButtonGroup<>();
    btnGroup.setMinCheckCount(0);
    btnGroup.uncheckAll();
    final CheckBox.CheckBoxStyle style;
    if (quizQuestion instanceof SingleChoice)
      style = skin.get("radio", CheckBox.CheckBoxStyle.class);
    else style = skin.get("default", CheckBox.CheckBoxStyle.class);
    quizQuestion
        .contentStream()
        .filter(answer -> answer instanceof Quiz.Content)
        .map(answer -> (Quiz.Content) answer)
        .filter(answer -> answer.type() != Quiz.Content.Type.IMAGE)
        .map(answer -> new CheckBox(UIUtils.formatString(answer.content()), style))
        .forEach(
            checkBox -> {
              btnGroup.add(checkBox);
              answerButtons.addActor(checkBox);
              checkBox.left();
            });

    if (quizQuestion instanceof MultipleChoice)
      btnGroup.setMaxCheckCount((int) quizQuestion.contentStream().count());
    else if (quizQuestion instanceof SingleChoice) btnGroup.setMaxCheckCount(1);

    answerButtons.align(Align.left);
    answerButtons.left();
    answerButtons.space(10);

    return answerButtons;
  }

  /**
   * Creates a UI for a {@link Quiz}.
   *
   * @param quizQuestion Various question configurations
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @param outputMsg Content displayed in the scrollable label
   * @return foo
   */
  public static Group createQuizQuestion(Quiz quizQuestion, Skin skin, String outputMsg) {
    // Label labelExercise = new Label(QUIZ_MESSAGE_TASK, skin);
    // labelExercise.setColor(Color.YELLOW);
    Label labelSolution = new Label(QUIZ_MESSAGE_SOLUTION, skin);
    labelSolution.setColor(Color.GREEN);
    VerticalGroup vg = new VerticalGroup();
    // vg.addActor(labelExercise);
    vg.addActor(visualizeQuestionSection(quizQuestion.question(), skin));
    vg.addActor(labelSolution);
    vg.addActor(visualizeAnswerSection(quizQuestion, skin));
    vg.grow();
    return vg;
  }

  /**
   * creates needed UI Elements to visualize all Question types.
   *
   * <p>QuestionType.TEXT will be shown on a Label, QuestionType.IMAGE will be shown on an Image and
   * QuestionType.TEXT_AND_IMAGE will show the whole Text and then the Image.
   *
   * @param questionContent the {@link Quiz.Content} to show on the hud.
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @return foo
   */
  private static Group visualizeQuestionSection(Quiz.Content questionContent, Skin skin) {

    VerticalGroup vg = new VerticalGroup();

    switch (questionContent.type()) {
      case TEXT ->
          vg.addActor(
              DialogDesign.createScrollPane(
                  skin, new Label(UIUtils.formatString(questionContent.content()), skin)));
      case IMAGE ->
          vg.addActor(
              DialogDesign.createScrollPane(
                  skin,
                  questionContent
                      .image()
                      .orElse(new Image(new Texture("animation/missing_texture.png")))));

      case TEXT_AND_IMAGE -> {
        vg.addActor(
            DialogDesign.createScrollPane(skin, new Label(questionContent.content(), skin)));
        vg.addActor(
            DialogDesign.createScrollPane(
                skin,
                questionContent
                    .image()
                    .orElse(new Image(new Texture("animation/missing_texture.png")))));
      }
      default -> {}
    }
    vg.grow();
    return vg;
  }

  /**
   * Representation of all possible answer options as Single-Choice, Multiple-Choice or as Freetext.
   *
   * @param quizQuestion Various question configurations
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @return foo
   */
  private static Group visualizeAnswerSection(Quiz quizQuestion, Skin skin) {

    VerticalGroup vg = new VerticalGroup();

    if (quizQuestion instanceof FreeText) {
      ScrollPane scroller = new ScrollPane(DialogDesign.createEditableText(skin), skin);
      scroller.setFadeScrollBars(false);
      scroller.setScrollbarsVisible(true);
      vg.addActor(scroller);
    } else {
      VerticalGroup btnGrp = createAnswerButtons(skin, quizQuestion);
      btnGrp.fill();
      btnGrp.left();
      vg.addActor(DialogDesign.createScrollPane(skin, btnGrp));
    }

    vg.grow();
    vg.setName(ANSWERS_GROUP_NAME);
    return vg;
  }
}
