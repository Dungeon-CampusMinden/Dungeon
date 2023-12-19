package task.game.hud;

import contrib.hud.dialogs.OkDialog;
import core.Entity;
import core.utils.IVoidFunction;
import task.Task;

/**
 * A Dialog with a "yes" and "no" Button on the Bottom.
 *
 * <p>Use {@link #showYesNoDialog(Task)} to create a dialog that will execute the grading function
 * of the given task.
 */
public class YesNoDialog {

  /**
   * Open a dialog window that shows the given task's text and will execute the {@link
   * Task#gradeTask()} if "yes" is pressed.
   *
   * @param task task that should be graded if the player presses "yes" on the dialog HUD.
   * @return The Entity that stores the HUD components.
   */
  public static Entity showYesNoDialog(final Task task) {
    String text =
        task.taskText()
            + System.lineSeparator()
            + System.lineSeparator()
            + task.scenarioText()
            + System.lineSeparator()
            + System.lineSeparator()
            + "Bist du fertig?";
    String title = task.taskName();
    return contrib.hud.dialogs.YesNoDialog.showYesNoDialog(text, title, gradeOn(task), () -> {});
  }

  private static IVoidFunction gradeOn(final Task t) {
    return () -> {
      float score = t.gradeTask();
      StringBuilder output = new StringBuilder();
      output
          .append("Du hast ")
          .append(score)
          .append("/")
          .append(t.points())
          .append(" Punkte erreicht")
          .append(System.lineSeparator())
          .append("Die Aufgabe ist damit ");
      if (t.state() == Task.TaskState.FINISHED_CORRECT) output.append("korrekt ");
      else output.append("falsch ");
      output.append("gelöst");

      IVoidFunction showCorrectAnswer =
          () -> OkDialog.showOkDialog(t.correctAnswersAsString(), "Korrekte Antwort", () -> {});

      OkDialog.showOkDialog(
          output.toString(),
          "Ergebnis",
          () -> {
            // if task was finished wrong show correct answers
            if (score < t.points()) {
              if (!t.explanation().isBlank() && !t.explanation().equals(Task.DEFAULT_EXPLANATION)) {
                OkDialog.showOkDialog(t.explanation(), "Erklärung", showCorrectAnswer);
              } else {
                showCorrectAnswer.execute();
              }
            }
          });
    };
  }
}
