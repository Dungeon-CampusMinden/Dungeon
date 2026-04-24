package contrib.hud.itemgrid;

import contrib.hud.renderers.DialogFrameRenderer;
import core.Game;
import core.ui.overlay.BaseUiOverlay;
import java.awt.Graphics2D;

/**
 * Shared overlay lifecycle for dialog-style item grid UIs.
 *
 * @param <M> measured dialog state used to size and lay out the overlay
 * @param <C> rendered content state reused for input handling and pointer feedback
 */
public abstract class BaseItemGridOverlay<M, C> extends BaseUiOverlay {

  protected BaseItemGridOverlay(int defaultWidth, int defaultHeight) {
    super(defaultWidth, defaultHeight);
  }

  @Override
  public final void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    M measurement = measureDialog();
    width = dialogWidth(measurement);
    height = dialogHeight(measurement);

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);
    try {
      int contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, dialogTitle());
      C content = renderContent(g, contentY, measurement);
      handleInput(content);
      drawPointerFeedback(g, content);
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }
  }

  protected abstract M measureDialog();

  protected abstract int dialogWidth(M measurement);

  protected abstract int dialogHeight(M measurement);

  protected abstract String dialogTitle();

  protected abstract C renderContent(Graphics2D g, int contentY, M measurement);

  protected abstract void handleInput(C content);

  protected abstract void drawPointerFeedback(Graphics2D g, C content);
}
