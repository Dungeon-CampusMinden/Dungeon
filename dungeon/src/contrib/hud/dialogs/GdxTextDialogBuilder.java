package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import contrib.hud.UIUtils;
import core.Game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds the libGDX-backed text dialog.
 */
public final class GdxTextDialogBuilder {

  private GdxTextDialogBuilder() {}

  /**
   * Builds a Scene2D text dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the text dialog
   */
  public static Group build(DialogContext ctx) {
    String title = ctx.require(DialogContextKeys.TITLE, String.class);
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String confirmButton =
      ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class)
        .orElse(OkDialog.DEFAULT_OK_BUTTON);
    String cancelButton =
      ctx.find(DialogContextKeys.CANCEL_LABEL, String.class).orElse(null);
    String[] extraButtons =
      ctx.find(DialogContextKeys.ADDITIONAL_BUTTONS, String[].class)
        .orElse(new String[] {});

    if (Game.isHeadless()) {
      List<String> allButtons = new ArrayList<>();
      allButtons.add(confirmButton);
      if (cancelButton != null) {
        allButtons.add(cancelButton);
      }
      allButtons.addAll(Arrays.asList(extraButtons));
      return new HeadlessDialogGroup(title, text, allButtons.toArray(new String[0]));
    }

    Dialog dialog =
      TextDialog.create(
        UIUtils.defaultSkin(),
        text,
        confirmButton,
        title,
        (d, id) -> {
          if (id.equals(confirmButton)) {
            DialogCallbackResolver.createButtonCallback(
                ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
              .accept(null);
          } else if (id.equals(cancelButton)) {
            DialogCallbackResolver.createButtonCallback(
                ctx.dialogId(), DialogContextKeys.ON_CANCEL)
              .accept(null);
          } else {
            for (String extraButton : extraButtons) {
              if (id.equals(extraButton)) {
                DialogCallbackResolver.createButtonCallback(
                    ctx.dialogId(), "on" + extraButton)
                  .accept(null);
              }
            }
          }
          return true;
        });

    if (cancelButton != null) {
      dialog.button(cancelButton, cancelButton);
    }
    for (String extraButton : extraButtons) {
      dialog.button(extraButton, extraButton);
    }
    dialog.pack();
    return dialog;
  }
}
