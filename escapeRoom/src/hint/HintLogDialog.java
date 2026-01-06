package hint;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.*;
import core.Entity;
import core.Game;
import java.util.Objects;
import java.util.function.BiFunction;
import mushRoom.modules.EscapeRoomDialogTypes;

/**
 * Utility class for displaying and navigating through a list of {@link Hint} objects as interactive
 * dialogs in the game.
 *
 * <p>The {@code HintLog} manages a simple dialog-based UI where players can view hints one by one
 * and navigate back and forth between them. It integrates with the {@link Game} entity system,
 * attaching dialogs to {@link Entity} objects.
 */
public final class HintLogDialog {

  private static final Hint NO_HINT = new Hint("No hint", "There is no hint left");

  /** Default label for the "next hint" button. */
  private static final String DEFAULT_DIALOG_NEXT = "-->";

  /** Default label for the "previous hint" button. */
  private static final String DEFAULT_DIALOG_PREVIOUS = "<--";

  /** Default style name for the hint dialog window. */
  private static final String DEFAULT_WINDOW_STYLE_NAME = "Letter";

  /** Callback key for next hint navigation. */
  public static final String CALLBACK_NEXT = "next";

  /** Callback key for previous hint navigation. */
  public static final String CALLBACK_PREV = "prev";

  static {
    DialogFactory.register(EscapeRoomDialogTypes.SIMPLE_HINT, HintLogDialog::createHintDialog);
  }

  /**
   * Opens a dialog showing the latest hint in the given storage.
   *
   * <p>If the storage contains no hints, a fallback hint will be displayed.
   *
   * @param log the {@link HintLogComponent} containing all available hints
   * @return the {@link Entity} that holds the dialog
   */
  public static Entity showHintLog(HintLogComponent log) {
    return showHintLog(log, log.hints.size() - 1);
  }

  /**
   * Opens a dialog showing the hint at a specific index within the storage.
   *
   * <p>If the index is out of bounds or the hint is {@code null}, a fallback hint will be displayed
   * instead.
   *
   * @param log the {@link HintLogComponent} containing all available hints
   * @param index the index of the hint to display
   * @return the {@link Entity} that holds the dialog
   */
  private static Entity showHintLog(HintLogComponent log, int index) {
    Hint hint;
    if (index < 0 || index > log.hints.size() - 1) hint = NO_HINT;
    else hint = log.hints().get(index);
    if (hint == null) hint = NO_HINT;

    DialogContext context =
        DialogContext.builder()
            .type(EscapeRoomDialogTypes.SIMPLE_HINT)
            .put("hint", hint)
            .put("hintIndex", index)
            .build();

    UIComponent ui = DialogFactory.show(context);

    // Register navigation callbacks
    ui.registerCallback(
        CALLBACK_NEXT,
        data -> {
          if (!log.hints().isEmpty()) {
            int i = (index + 1) % log.hints().size();
            showHintLog(log, i);
          } else {
            showHintLog(log);
          }
          UIUtils.closeDialog(ui, true);
        });

    ui.registerCallback(
        CALLBACK_PREV,
        data -> {
          if (!log.hints().isEmpty()) {
            int i = (index - 1 + log.hints().size()) % log.hints().size();
            showHintLog(log, i);
          } else {
            showHintLog(log);
          }
          UIUtils.closeDialog(ui, true);
        });

    return ui.dialogContext().ownerEntity();
  }

  /**
   * Creates a dialog for displaying a single {@link Hint}.
   *
   * <p>The dialog includes the hint title and text, as well as navigation buttons for switching to
   * the previous or next hint.
   *
   * @param context the {@link DialogContext} containing the hint
   * @return a configured {@link Dialog} instance
   */
  private static Group createHintDialog(DialogContext context) {
    // On headless server, return placeholder
    if (Game.isHeadless()) {
      Hint hint = context.require("hint", Hint.class);
      return new HeadlessDialogGroup(
          hint.title(), hint.text(), DEFAULT_DIALOG_PREVIOUS, DEFAULT_DIALOG_NEXT);
    }

    Skin skin = defaultSkin();
    Hint hint = context.require("hint", Hint.class);
    String dialogId = context.dialogId();

    BiFunction<Dialog, String, Boolean> resultHandler =
        (d, id) -> {
          if (Objects.equals(id, DEFAULT_DIALOG_NEXT)) {
            DialogCallbackResolver.createButtonCallback(dialogId, CALLBACK_NEXT).accept(null);
            return true;
          }
          if (Objects.equals(id, DEFAULT_DIALOG_PREVIOUS)) {
            DialogCallbackResolver.createButtonCallback(dialogId, CALLBACK_PREV).accept(null);
            return true;
          }
          return false;
        };

    Dialog textDialog =
        new TextDialog(hint.title(), skin, DEFAULT_WINDOW_STYLE_NAME, resultHandler);
    textDialog
        .getContentTable()
        .add(DialogDesign.createTextDialog(skin, UIUtils.formatString(hint.text())))
        .center()
        .grow();
    textDialog.button(DEFAULT_DIALOG_PREVIOUS, DEFAULT_DIALOG_PREVIOUS);
    textDialog.button(DEFAULT_DIALOG_NEXT, DEFAULT_DIALOG_NEXT);
    textDialog.pack(); // resizes to size
    return textDialog;
  }
}
