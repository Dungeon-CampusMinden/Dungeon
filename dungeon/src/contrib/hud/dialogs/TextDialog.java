package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.Scene2dElementFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class TextDialog {

  /**
   * Builds a text dialog from the given context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing message, buttons, and handlers
   * @return A fully configured text dialog or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    String text = ctx.require(DialogContextKeys.MESSAGE, String.class);
    String title = ctx.find(DialogContextKeys.TITLE, String.class).orElse("");
    String button =
      ctx.find(DialogContextKeys.CONFIRM_LABEL, String.class).orElse(OkDialog.DEFAULT_OK_BUTTON);

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      List<String> allButtons = new ArrayList<>();
      allButtons.add(button);
      return new HeadlessDialogGroup(title, text, allButtons.toArray(new String[0]));
    }

    return create(ctx, text, button, title);
  }

  /**
   * A simple Text Dialog that shows only the provided string.
   *
   * @param ctx The dialog context
   * @param message The text which should be shown in the middle of the dialog.
   * @param confirmButton Text that the button should have; also the ID for the result handler.
   * @param title Title for the dialog.
   * @return The fully configured Dialog, which can then be added where it is needed.
   */
  private static Dialog create(DialogContext ctx, String message, String confirmButton, String title) {
    Skin skin = UIUtils.defaultSkin();

    Dialog dialog = new HandledDialog(title, skin, (d, id) -> {
          if (id.equals(confirmButton)) {
            DialogCallbackResolver.createButtonCallback(
                ctx.dialogId(), DialogContextKeys.ON_CONFIRM)
              .accept(null);
          }
          return true;
        });

    DialogDesign.setDialogDefaults(dialog, title);
    Table content = dialog.getContentTable();

    Label label = Scene2dElementFactory.createLabel(message, DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    label.setWrap(true);

    Table labelTable = new Table();
    labelTable.top().left();
    labelTable.add(label).growX();

    ScrollPane pane = Scene2dElementFactory.createScrollPane(labelTable, false, true);
    pane.setScrollbarsOnTop(false);
    content.add(pane).width(450).maxHeight(350).padBottom(10).row();

    dialog.button(confirmButton, confirmButton, skin.get("clean-green", TextButton.TextButtonStyle.class));

    dialog.pack();
    return dialog;
  }
}
