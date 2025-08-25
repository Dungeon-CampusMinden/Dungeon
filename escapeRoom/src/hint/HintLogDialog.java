package hint;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogDesign;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.Game;
import java.util.Objects;
import java.util.function.BiFunction;

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

  /**
   * Opens a dialog showing the latest hint in the given storage.
   *
   * <p>If the storage contains no hints, a fallback hint will be displayed.
   *
   * @param log the {@link HintLogComponent} containing all available hints
   * @return the {@link Entity} that holds the dialog
   */
  public static Entity showHintLog(HintLogComponent log) {
    Entity entity = showHintLog(defaultSkin(), log, log.hints.size() - 1);
    return entity;
  }

  /**
   * Opens a dialog showing the hint at a specific index within the storage.
   *
   * <p>If the index is out of bounds or the hint is {@code null}, a fallback hint will be displayed
   * instead.
   *
   * @param skin the UI {@link Skin} to use for styling
   * @param log the {@link HintLogComponent} containing all available hints
   * @param index the index of the hint to display
   * @return the {@link Entity} that holds the dialog
   */
  private static Entity showHintLog(final Skin skin, HintLogComponent log, int index) {
    Hint hint;
    if (index < 0 || index > log.hints.size() - 1) hint = NO_HINT;
    else hint = log.hints().get(index);
    if (hint == null) hint = NO_HINT;

    Entity entity = new Entity("hintDialog_" + hint.titel());

    Hint finalHint = hint;
    UIUtils.show(
        () -> {
          Dialog dialog =
              createHintDialog(
                  skin, finalHint, createResultHandlerNextPrev(skin, entity, log, index));
          UIUtils.center(dialog);
          return dialog;
        },
        entity);
    Game.add(entity);
    return entity;
  }

  /**
   * Creates a dialog for displaying a single {@link Hint}.
   *
   * <p>The dialog includes the hint title and text, as well as navigation buttons for switching to
   * the previous or next hint.
   *
   * @param skin the UI {@link Skin} to use for styling
   * @param hint the {@link Hint} to display
   * @param resultHandler the callback to handle button clicks
   * @return a configured {@link Dialog} instance
   */
  private static Dialog createHintDialog(
      final Skin skin,
      final Hint hint,
      final BiFunction<TextDialog, String, Boolean> resultHandler) {
    Dialog textDialog =
        new TextDialog(hint.titel(), skin, DEFAULT_WINDOW_STYLE_NAME, resultHandler);
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

  /**
   * Creates a handler for processing dialog button results.
   *
   * <p>This handler enables navigation through the hint list by responding to the "next" and
   * "previous" buttons. When triggered, it removes the current entity and displays the appropriate
   * next or previous hint dialog.
   *
   * @param skin the UI {@link Skin} to use for styling
   * @param entity the current dialog {@link Entity} to remove after navigation
   * @param log the storage containing all available hints
   * @param index the index of the currently displayed hint
   * @return a {@link BiFunction} that processes dialog button clicks
   */
  private static BiFunction<TextDialog, String, Boolean> createResultHandlerNextPrev(
      final Skin skin, final Entity entity, final HintLogComponent log, final int index) {
    return (d, id) -> {
      if (Objects.equals(id, DEFAULT_DIALOG_NEXT)) {
        if (log.hints().size() != 0) {
          int i = (index + 1) % log.hints().size();
          showHintLog(skin, log, i);
        } else showHintLog(log);
        Game.remove(entity);
        return true;
      }
      if (Objects.equals(id, DEFAULT_DIALOG_PREVIOUS)) {
        if (log.hints().size() != 0) {
          int i = (index - 1 + log.hints().size()) % log.hints().size();
          showHintLog(skin, log, i);
        } else showHintLog(log);
        Game.remove(entity);
        return true;
      }
      return false;
    };
  }
}
