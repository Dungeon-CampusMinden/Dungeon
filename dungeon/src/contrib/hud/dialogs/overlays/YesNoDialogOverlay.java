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
 * A dialog overlay that presents a Yes/No choice to the user.
 *
 * <p>This class extends {@link AbstractUiOverlay} for rendering a modal
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
public final class YesNoDialogOverlay extends AbstractUiOverlay {

  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 230;
  private static final int BUTTON_GAP = 20;

  private static final String YES_LABEL = "Ja";
  private static final String NO_LABEL = "Nein";

  private final String title;
  private final String text;
  private final String dialogId;

  private boolean yesPressed = false;
  private boolean noPressed = false;
  private boolean leftButtonDownLastFrame = false;

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

      Rectangle no = noBounds();
      Rectangle yes = yesBounds();

      DialogFrameRenderer.drawButton(g, no, NO_LABEL, noPressed);
      DialogFrameRenderer.drawButton(g, yes, YES_LABEL, yesPressed);
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  private void handleInput() {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      yesPressed = false;
      noPressed = false;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    Rectangle no = noBounds();
    Rectangle yes = yesBounds();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      noPressed = no.contains(mouseX, mouseY);
      yesPressed = yes.contains(mouseX, mouseY);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      boolean releasedOnNo = noPressed && no.contains(mouseX, mouseY);
      boolean releasedOnYes = yesPressed && yes.contains(mouseX, mouseY);

      noPressed = false;
      yesPressed = false;

      if (releasedOnYes) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_YES)
          .accept(null);
      } else if (releasedOnNo) {
        DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_NO)
          .accept(null);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private Rectangle noBounds() {
    return DialogFrameRenderer.centeredButtonRow(
        x, y, width, height, 2, BUTTON_GAP)
      .getFirst();
  }

  private Rectangle yesBounds() {
    return DialogFrameRenderer.centeredButtonRow(
        x, y, width, height, 2, BUTTON_GAP)
      .get(1);
  }
}
