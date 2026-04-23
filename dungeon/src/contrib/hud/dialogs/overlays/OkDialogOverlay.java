package contrib.hud.dialogs.overlays;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.renderers.DialogFrameRenderer;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.ui.overlay.AbstractUiOverlay;
import core.utils.InputManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;

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
public final class OkDialogOverlay extends AbstractUiOverlay {

  private static final int DEFAULT_WIDTH = 460;
  private static final int DEFAULT_HEIGHT = 220;
  private static final int BUTTON_GAP = 16;

  private final String title;
  private final String text;
  private final String dialogId;

  private boolean okPressed = false;
  private boolean leftButtonDownLastFrame = false;

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
    this.dialogId = dialogId;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    handleInput();

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      int textY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, title);

      DialogFrameRenderer.drawWrappedText(
        g, text, x + DialogFrameRenderer.PADDING, textY,
        width - 2 * DialogFrameRenderer.PADDING);

      Rectangle ok = okBounds();
      DialogFrameRenderer.drawButton(g, ok, "OK", okPressed);
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      okPressed = false;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    Rectangle ok = okBounds();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      okPressed = ok.contains(mouseX, mouseY);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      boolean releasedInside = okPressed && ok.contains(mouseX, mouseY);
      okPressed = false;

      if (releasedInside) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
          .accept(null);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private Rectangle okBounds() {
    return DialogFrameRenderer.centeredButtonRow(
        x, y, width, height, 1, BUTTON_GAP).getFirst();
  }
}
