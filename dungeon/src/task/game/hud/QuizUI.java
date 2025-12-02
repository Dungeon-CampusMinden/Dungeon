package task.game.hud;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import contrib.hud.dialogs.DialogFactory;
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
  public static final String DEFAULT_DIALOG_CONFIRM = "Bestätigen";

  /** The default dialog abort. */
  public static final String DEFAULT_DIALOG_ABORT = "Abbrechen";

  /**
   * Ask a Quizquestion on the HUD and trigger the grading function, after the player confirmed the
   * answers.
   *
   * @param quiz Question to ask
   * @return the entity that stores the hud elements for the dialog window.
   */
  public static Entity askQuizOnHud(final Quiz quiz) {
    return showQuizDialog(
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
                          DialogFactory.showOkDialog(
                              task.correctAnswersAsString(), "Korrekte Antwort", () -> {});

                  DialogFactory.showOkDialog(
                      output.toString(),
                      "Ergebnis",
                      () -> {
                        // if task was finished wrong show correct answers
                        if (score < task.points()) {
                          if (!task.explanation().isBlank()
                              && !task.explanation().equals(Task.DEFAULT_EXPLANATION)) {
                            DialogFactory.showOkDialog(
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
   * @return the Entity that stores the {@link contrib.components.UIComponent} with the UI-Elements
   *     The entity will already be added to the game by this method.
   */
  public static Entity showQuizDialog(
      Quiz question, Function<Entity, BiFunction<Dialog, String, Boolean>> resulthandlerLinker) {
    return DialogFactory.showQuizDialog(question, resulthandlerLinker);
  }

  /**
   * Display the Question-Content (Question and answer options (no pictures) as text, picture, text
   * and picture, single or multiple choice ) on the HUD.
   *
   * <p>Use default callback method, that will delete the hud-entity from the game.
   *
   * @param question Question to show on the HUD
   * @return the Entity that stores the {@link contrib.components.UIComponent} with the UI-Elements
   *     The entity will already be added to the game by this method.
   */
  public static Entity showQuizDialog(Quiz question) {
    return showQuizDialog(
        question,
        (entity) -> createResultHandlerQuiz(entity, DEFAULT_DIALOG_CONFIRM, DEFAULT_DIALOG_ABORT));
  }

  /**
   * Create a default callback-function that will delete the entity that stores the hud-component.
   *
   * @param abortButtonID foo
   * @param confirmButtonID foo
   * @param entity foo
   * @return foo
   */
  public static BiFunction<Dialog, String, Boolean> createResultHandlerQuiz(
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
