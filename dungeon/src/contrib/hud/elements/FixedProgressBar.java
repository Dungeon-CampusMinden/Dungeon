package contrib.hud.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * A fixed version of LibGDX's ProgressBar, which omits rendering the knobBefore when no knob is
 * present.
 */
public class FixedProgressBar extends ProgressBar {

  /**
   * Creates a FixedProgressBar.
   *
   * @param min the minimum value
   * @param max the maximum value
   * @param stepSize the step size
   * @param vertical whether the bar is vertical
   * @param skin the skin to use
   */
  public FixedProgressBar(float min, float max, float stepSize, boolean vertical, Skin skin) {
    super(min, max, stepSize, vertical, skin);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    ProgressBarStyle style = getStyle();
    if (getPercent() <= 0f && style.knob == null && style.background != null) {
      style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
      return;
    }
    super.draw(batch, parentAlpha);
  }
}
