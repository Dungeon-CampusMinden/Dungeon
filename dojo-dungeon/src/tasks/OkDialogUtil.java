package tasks;

import contrib.hud.dialogs.OkDialog;
import core.utils.IVoidFunction;

public class OkDialogUtil {
  public static void showOkDialog(String message, String title) {
    showOkDialog(message, title, () -> {});
  }

  public static void showOkDialog(String message, String title, IVoidFunction callback) {
    OkDialog.showOkDialog(message, title, callback);
  }
}
