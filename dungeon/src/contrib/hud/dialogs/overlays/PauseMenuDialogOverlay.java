package contrib.hud.dialogs.overlays;

import contrib.hud.renderers.DialogFrameRenderer;
import core.ui.overlay.UiOverlay;
import java.awt.Graphics2D;

/**
 * Represents a pause menu overlay in the game's UI.
 *
 * <p>The pause menu overlay appears as a dialog box when the game is paused. It includes
 * a title and a message instructing the user how to resume the game.
 *
 * <p>The overlay can be shown, hidden, and customized in terms of its position, size, and visibility.
 *
 * <p>Defaults:
 * <ul>
 *   <li>Width: 400 pixels</li>
 *   <li>Height: 200 pixels</li>
 * </ul>
 *
 * <p>Implements the {@link UiOverlay} interface, allowing it to be rendered above the
 * game scene with configurable properties such as x and y coordinates, dimensions,
 * and visibility.
 */
final class PauseMenuDialogOverlay implements UiOverlay {

  private static final int DEFAULT_WIDTH = 400;
  private static final int DEFAULT_HEIGHT = 200;

  private static final String TITLE = "Pause";
  private static final String MESSAGE = "Game Paused\n\nPress <P> to resume";

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      int textY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, TITLE);

      DialogFrameRenderer.drawWrappedText(
        g,
        MESSAGE,
        x + DialogFrameRenderer.PADDING,
        textY,
        width - 2 * DialogFrameRenderer.PADDING);
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
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
