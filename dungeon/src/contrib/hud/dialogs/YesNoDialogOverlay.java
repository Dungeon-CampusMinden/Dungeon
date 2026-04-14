package contrib.hud.dialogs;

import contrib.hud.overlays.DialogFrameRenderer;
import core.Game;
import core.input.MouseButtons;
import core.ui.overlay.UiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A minimal real yes/no dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class YesNoDialogOverlay implements UiOverlay {

  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 230;
  private static final int BUTTON_GAP = 20;

  private static final String YES_LABEL = "Ja";
  private static final String NO_LABEL = "Nein";

  private final String title;
  private final String text;
  private final String dialogId;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private boolean yesPressed = false;
  private boolean noPressed = false;
  private boolean leftButtonDownLastFrame = false;

  YesNoDialogOverlay(String title, String text, String dialogId) {
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
      .get(0);
  }

  private Rectangle yesBounds() {
    return DialogFrameRenderer.centeredButtonRow(
        x, y, width, height, 2, BUTTON_GAP)
      .get(1);
  }

  @Override
  public int x() {
    return x;
  }

  @Override
  public void x(int x) {
    this.x = x;
  }

  @Override
  public int y() {
    return y;
  }

  @Override
  public void y(int y) {
    this.y = y;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public void width(int width) {
    this.width = width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void height(int height) {
    this.height = height;
  }

  @Override
  public boolean visible() {
    return visible;
  }

  @Override
  public void visible(boolean visible) {
    this.visible = visible;
  }
}
