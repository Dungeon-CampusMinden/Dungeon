package task.game.hud;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.Game;
import core.utils.IVoidFunction;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import task.Task;
import task.tasktype.Quiz;

/** WTF? . */
public class QuizUI {
  /** The default dialog confirmation. */
  public static final String DEFAULT_DIALOG_CONFIRM = "Confirm";

  /** The default dialog abort. */
  public static final String DEFAULT_DIALOG_ABORT = "Abort";

  /**
   * Ask a Quizquestion on the HUD and trigger the grading function, after the player confirmed the
   * answers.
   *
   * @param quiz Question to ask
   * @return the entity that stores the hud elements for the dialog window.
   */
  public static Entity askQuizOnHud(final Quiz quiz) {
    return QuizUI.showQuizDialog(
        quiz,
        (Entity hudEntity) ->
            UIAnswerCallback.uiCallback(
                quiz,
                hudEntity,
                (task, taskContents) -> {
                  float score = task.gradeTask(taskContents);
                  StringBuilder output = new StringBuilder();
                  output
                      .append("Du hast ")
                      .append(score)
                      .append("/")
                      .append(task.points())
                      .append(" Punkte erreicht")
                      .append(System.lineSeparator())
                      .append("Die Aufgabe ist damit ");
                  if (task.state() == Task.TaskState.FINISHED_CORRECT) output.append("korrekt ");
                  else output.append("falsch ");
                  output.append("gelöst");

                  IVoidFunction showCorrectAnswer =
                      () ->
                          OkDialog.showOkDialog(
                              task.correctAnswersAsString(), "Korrekte Antwort", () -> {});

                  OkDialog.showOkDialog(
                      output.toString(),
                      "Ergebnis",
                      () -> {
                        // if task was finished wrong show correct answers
                        if (score < task.points()) {
                          if (!task.explanation().isBlank()
                              && !task.explanation().equals(Task.DEFAULT_EXPLANATION)) {
                            OkDialog.showOkDialog(
                                task.explanation(), "Erklärung", showCorrectAnswer);
                          } else {
                            showCorrectAnswer.execute();
                          }
                        }
                      });
                }));
  }

  /**
   * Display the Question-Content (Question and answer options (no pictures) as text, picture, text
   * and picture, single or multiple choice ) on the HUD.
   *
   * @param question Question to show on the HUD
   * @param resulthandlerLinker callback function
   * @return the Entity that stores the {@link UIComponent} with the UI-Elements The entity will
   *     already be added to the game by this method.
   */
  public static Entity showQuizDialog(
      Quiz question,
      Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {

    String title = question.taskName();
    Entity entity =
        showQuizDialog(
            question,
            UIUtils.formatString(question.taskText()),
            DEFAULT_DIALOG_CONFIRM,
            title,
            resulthandlerLinker);
    Game.add(entity);
    return entity;
  }

  /**
   * Display the Question-Content (Question and answer options (no pictures) as text, picture, text
   * and picture, single or multiple choice ) on the HUD.
   *
   * <p>Use default callback method, that will delete the hud-entity from the game.
   *
   * @param question Question to show on the HUD
   * @return the Entity that stores the {@link UIComponent} with the UI-Elements The entity will
   *     already be added to the game by this method.
   */
  public static Entity showQuizDialog(Quiz question) {
    return showQuizDialog(
        question,
        (entity) -> createResultHandlerQuiz(entity, DEFAULT_DIALOG_CONFIRM, DEFAULT_DIALOG_ABORT));
  }

  /**
   * If no Quiz-Dialogue is created, a new dialogue is created according to the event key. Pause all
   * systems except DrawSystem
   *
   * <p>display the Question-Content (Question and answer options (no pictures) as text, picture,
   * text and picture, single or multiple choice ) in the Dialog
   *
   * @param question Various question configurations
   * @param questionMsg foo
   * @param buttonMsg foo
   * @param dialogTitle foo
   * @param resulthandlerLinker foo
   * @return the Entity that stores the {@link UIComponent} with the UI-Elements The entity will
   *     already be added to the game by this method.
   */
  private static Entity showQuizDialog(
      Quiz question,
      String questionMsg,
      String buttonMsg,
      String dialogTitle,
      Function<Entity, BiFunction<TextDialog, String, Boolean>> resulthandlerLinker) {
    Entity entity = new Entity();

    UIUtils.show(
        () -> {
          Dialog quizDialog =
              createQuizDialog(
                  defaultSkin(),
                  question,
                  questionMsg,
                  buttonMsg,
                  dialogTitle,
                  resulthandlerLinker.apply(entity));
          UIUtils.center(quizDialog);
          return quizDialog;
        },
        entity);
    Game.add(entity);
    return entity;
  }

  /**
   * Factory for a generic Quizquestion.
   *
   * @param skin Skin for the dialogue (resources that can be used by UI widgets)
   * @param quizQuestion Various question configurations
   * @param outputMsg Content displayed in the scrollable label
   * @param buttonMsg text for the button
   * @param title Title of the dialogue
   * @param resultHandler a callback method which is called when the confirm button is pressed
   * @return the fully configured Dialog which then can be added where it is needed
   */
  private static Dialog createQuizDialog(
      Skin skin,
      Quiz quizQuestion,
      String outputMsg,
      String buttonMsg,
      String title,
      BiFunction<TextDialog, String, Boolean> resultHandler) {
    Dialog textDialog = new TextDialog(title, skin, "Letter", resultHandler);
    textDialog
        .getContentTable()
        .add(QuizDialogDesign.createQuizQuestion(quizQuestion, skin, outputMsg))
        .grow()
        .fill(); // changes size based on childrens;
    textDialog.button(DEFAULT_DIALOG_ABORT, DEFAULT_DIALOG_ABORT);
    textDialog.button(buttonMsg, buttonMsg);
    textDialog.pack(); // resizes to size
    return textDialog;
  }

  /**
   * Create a default callback-function that will delete the entity that stores the hud-component.
   *
   * @param abortButtonID foo
   * @param confirmButtonID foo
   * @param entity foo
   * @return foo
   */
  public static BiFunction<TextDialog, String, Boolean> createResultHandlerQuiz(
      final Entity entity, final String confirmButtonID, String abortButtonID) {
    return (d, id) -> {
      if (Objects.equals(id, confirmButtonID)) {
        Game.remove(entity);
        return true;
      }
      if (Objects.equals(id, abortButtonID)) {
        Game.remove(entity);
        return true;
      }
      return false;
    };
  }
}
