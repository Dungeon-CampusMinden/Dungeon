package contrib.hud.dialogs;

import core.ui.overlay.UiOverlay;
import java.awt.Graphics2D;

/**
 * Minimal pause overlay for the LITIENGINE backend.
 *
 * <p>This intentionally mirrors the very simple semantics of the existing libGDX pause menu:
 * it only visualizes the paused state and tells the user how to resume.
 */
final class LitienginePauseMenuOverlay implements UiOverlay {

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

    DialogOverlaySupport.RenderState state =
      DialogOverlaySupport.beginDialog(g);

    try {
      int textY = DialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, TITLE);

      DialogOverlaySupport.drawWrappedText(
        g,
        MESSAGE,
        x + DialogOverlaySupport.PADDING,
        textY,
        width - 2 * DialogOverlaySupport.PADDING);
    } finally {
      DialogOverlaySupport.finishDialog(g, state);
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
