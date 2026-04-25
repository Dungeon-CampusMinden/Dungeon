package contrib.hud.dialogs.overlays;

import contrib.hud.frame.DialogFrameMetrics;
import contrib.hud.frame.DialogFrameRenderer;
import core.ui.overlay.BaseUiOverlay;
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
 * <p>Extends {@link BaseUiOverlay}, allowing it to be rendered above the
 * game scene with configurable properties such as x and y coordinates, dimensions,
 * and visibility.
 */
public final class PauseMenuDialogOverlay extends BaseUiOverlay {

  private static final int DEFAULT_WIDTH = 400;
  private static final int DEFAULT_HEIGHT = 200;

  private static final String TITLE = "Pause";
  private static final String MESSAGE = "Game Paused\n\nPress <P> to resume";

  public PauseMenuDialogOverlay() {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

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
        x + DialogFrameMetrics.PADDING,
        textY,
        width - 2 * DialogFrameMetrics.PADDING);
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }
}
