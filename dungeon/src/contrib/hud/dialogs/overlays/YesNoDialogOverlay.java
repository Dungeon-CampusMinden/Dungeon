package contrib.hud.dialogs.overlays;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogButtonInputHandler;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.frame.DialogFrameMetrics;
import contrib.hud.frame.DialogFrameRenderer;
import core.ui.overlay.BaseUiOverlay;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * A dialog overlay that presents a Yes/No choice to the user.
 *
 * <p>This class extends {@link BaseUiOverlay} for rendering a modal
 * dialog box with a title, descriptive text, and Yes/No buttons.
 *
 * <p>The dialog overlays the game screen and captures user input for the buttons.
 *
 * <p>The dialog's visual appearance is managed via the {@link DialogFrameRenderer} utility
 * class, which handles frame rendering, button layout, and text wrapping.
 *
 * <p>User interactions with the Yes/No buttons are relayed to registered callbacks using the {@link DialogCallbackResolver}.
 *
 * <p>Primary functionality:
 * <ul>
 *   <li>Render a dialog with customizable title and text</li>
 *   <li>Capture input for Yes/No button presses</li>
 *   <li>Trigger callbacks upon button confirmation</li>
 *   <li>Support visibility toggling and positioning of the dialog overlay</li>
 * </ul>
 */
public final class YesNoDialogOverlay extends BaseUiOverlay {

  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 230;
  private static final int BUTTON_GAP = 20;

  private static final String YES_LABEL = "Ja";
  private static final String NO_LABEL = "Nein";

  private final String title;
  private final String text;
  private final DialogButtonInputHandler buttonInput = new DialogButtonInputHandler(2);

  /**
   * Creates a yes/no dialog overlay.
   *
   * @param title dialog title
   * @param text message text
   * @param dialogId id used to resolve callbacks
   */
  public YesNoDialogOverlay(String title, String text, String dialogId) {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.title = title;
    this.text = text;
    this.buttonInput.onClick(
      0,
      () -> DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_NO)
        .accept(null));
    this.buttonInput.onClick(
      1,
      () -> DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
        .accept(null));
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    List<Rectangle> buttons = buttonBounds();
    buttonInput.updateBounds(buttons);
    handleInput();

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      int textY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, title);

      DialogFrameRenderer.drawWrappedText(
        g, text, x + DialogFrameMetrics.PADDING, textY,
        width - 2 * DialogFrameMetrics.PADDING);

      DialogFrameRenderer.drawButton(g, buttons.get(0), NO_LABEL, buttonInput.isPressed(0));
      DialogFrameRenderer.drawButton(g, buttons.get(1), YES_LABEL, buttonInput.isPressed(1));
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void handleInput() {
    buttonInput.updateFromStage();
  }

  private List<Rectangle> buttonBounds() {
    return DialogFrameRenderer.centeredButtonRow(
      x, y, width, height, 2, BUTTON_GAP);
  }
}
