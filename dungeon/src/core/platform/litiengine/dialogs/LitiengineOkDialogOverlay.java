package core.platform.litiengine.dialogs;

import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import core.Game;
import core.input.MouseButtons;
import core.ui.overlay.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A minimal real OK dialog for the LITIENGINE backend.
 *
 * <p>Rendered as a custom overlay via the LITIENGINE Graphics2D render bridge.
 */
final class LitiengineOkDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 460;
  private static final int DEFAULT_HEIGHT = 220;
  private static final int BUTTON_GAP = 16;

  private final String title;
  private final String text;
  private final String dialogId;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;
  private boolean okPressed = false;
  private boolean leftButtonDownLastFrame = false;

  LitiengineOkDialogOverlay(String title, String text, String dialogId) {
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

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      int textY = LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, title);

      LitiengineDialogOverlaySupport.drawWrappedText(
        g, text, x + LitiengineDialogOverlaySupport.PADDING, textY,
        width - 2 * LitiengineDialogOverlaySupport.PADDING);

      Rectangle ok = okBounds();
      LitiengineDialogOverlaySupport.drawButton(g, ok, "OK", okPressed);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
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
    return LitiengineDialogOverlaySupport.centeredButtonRow(
        x, y, width, height, 1, BUTTON_GAP)
      .get(0);
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
