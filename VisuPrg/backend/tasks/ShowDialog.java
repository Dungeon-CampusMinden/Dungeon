package tasks;

import contrib.hud.dialogs.TextDialog;
import core.Entity;
import systems.VisualProgrammingSystem;

public class ShowDialog extends VisuTask {

  private String dialogMessage;
  private Entity dialog;

  public ShowDialog(
      String message, VisualProgrammingSystem visualProgrammingSystem, String dialogMessage) {
    super(message, visualProgrammingSystem);
    this.dialogMessage = dialogMessage;
  }

  @Override
  public void execute() {
    dialog = TextDialog.textDialog(dialogMessage, "Ok", "");
    visualProgrammingSystem.setTaskDone();
  }
}
