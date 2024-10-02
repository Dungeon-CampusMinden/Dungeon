package task.game.hud;

import contrib.hud.dialogs.OkDialog;
import core.Entity;
import core.utils.IVoidFunction;
import newdsl.tasks.Task;
import newdsl.tasks.TaskState;

public class NewDSLYesNoDialog {

    public static Entity showYesNoDialog(final Task task) {
        String text =
            task.getTitle()
                + System.lineSeparator()
                + System.lineSeparator()
                + System.lineSeparator()
                + "Bist du fertig?";
        String title = task.getTitle();
        return contrib.hud.dialogs.YesNoDialog.showYesNoDialog(text, title, gradeOn(task), () -> {
        });
    }

    private static IVoidFunction gradeOn(final Task t) {
        return () -> {
            float score = t.gradeTask(null);
            StringBuilder output = new StringBuilder();
            output
                .append("Du hast ")
                .append(score)
                .append("/")
                .append(t.getMaxPoints())
                .append(" Punkte erreicht")
                .append(System.lineSeparator())
                .append("Die Aufgabe ist damit ");
            if (t.getState() == TaskState.FINISHED_CORRECT) output.append("korrekt ");
            else output.append("falsch ");
            output.append("gelöst");

            IVoidFunction showCorrectAnswer =
                () -> OkDialog.showOkDialog(t.correctAnswersAsString(), "Korrekte Antwort", () -> {
                });

            OkDialog.showOkDialog(
                output.toString(),
                "Ergebnis",
                () -> {
                    // if task was finished wrong show correct answers
                    if (!t.pass(t.gradeTask(null))) {
                        if (!t.getExplanation().isBlank() && !t.getExplanation().equals(Task.DEFAULT_EXPLANATION)) {
                            OkDialog.showOkDialog(t.getExplanation(), "Erklärung", showCorrectAnswer);
                        } else {
                            showCorrectAnswer.execute();
                        }
                    }
                });
        };
    }
}
