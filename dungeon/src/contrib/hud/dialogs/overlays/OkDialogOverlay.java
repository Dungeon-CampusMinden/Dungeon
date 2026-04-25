package contrib.hud.dialogs.overlays;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogButtonInputHandler;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.shared.DialogFrameMetrics;
import contrib.hud.dialogs.shared.DialogFrameRenderer;
import core.ui.overlay.BaseUiOverlay;
import java.awt.Graphics2D;

/**
 * A dialog overlay with a single "OK" button.
 *
 * <p>This class provides a user interface dialog overlay with a title and descriptive text,
 * rendered above the game scene.
 *
 * <p>The dialog includes an "OK" button that triggers a callback when clicked.
 *
 * <p>Features include:
 * <ul>
 *   <li>Rendering the dialog frame, title, and content text within a centered area.</li>
 *   <li>Handling mouse input to detect interaction with the "OK" button.</li>
 *   <li>Managing visibility, dimensions, and position of the overlay.</li>
 * </ul>
 *
 * <p>The overlay utilizes {@link DialogFrameRenderer} for drawing the dialog's frame and
 * content elements.
 *
 * <p>Key properties:
 * <ul>
 *   <li>Width and height default to {@code 460} and {@code 220}, respectively, but can be customized.</li>
 *   <li>Button layout includes predefined gaps and centering logic.</li>
 * </ul>
 *
 * <p>The input logic ensures correct handling of mouse button states and triggers a callback via
 * the {@link DialogCallbackResolver} on clicking the "OK" button.
 */
public final class OkDialogOverlay extends BaseUiOverlay {

  private static final int DEFAULT_WIDTH = 460;
  private static final int DEFAULT_HEIGHT = 220;
  private static final int BUTTON_GAP = 16;

  private final String title;
  private final String text;
  private final DialogButtonInputHandler buttonInput = new DialogButtonInputHandler(1);

  /**
   * Creates an OK dialog overlay.
   *
   * @param title dialog title
   * @param text message text
   * @param dialogId id used to resolve callbacks
   */
  public OkDialogOverlay(String title, String text, String dialogId) {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.title = title;
    this.text = text;
    this.buttonInput.onClick(
      0,
      () -> DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
        .accept(null));
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    buttonInput.updateBounds(DialogFrameRenderer.centeredButtonRow(
      x, y, width, height, 1, BUTTON_GAP));
    handleInput();

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      int textY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, title);

      DialogFrameRenderer.drawWrappedText(
        g, text, x + DialogFrameMetrics.PADDING, textY,
        width - 2 * DialogFrameMetrics.PADDING);

      DialogFrameRenderer.drawButton(g, buttonInput.bounds(0), "OK", buttonInput.isPressed(0));
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void handleInput() {
    buttonInput.updateFromStage();
  }
}
