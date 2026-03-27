package core.ui.gdx;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.hud.elements.AttributeBarHandle;
import java.util.Objects;

/**
 * libGDX-backed {@link AttributeBarHandle}.
 */
public final class GdxAttributeBarHandle implements AttributeBarHandle {

  private final ProgressBar progressBar;

  public GdxAttributeBarHandle(ProgressBar progressBar) {
    this.progressBar = Objects.requireNonNull(progressBar);
  }

  @Override
  public void remove() {
    progressBar.remove();
  }

  @Override
  public void setVisible(boolean visible) {
    progressBar.setVisible(visible);
  }

  @Override
  public void setPosition(float x, float y) {
    progressBar.setPosition(x, y);
  }

  @Override
  public void setValue(float value) {
    progressBar.setValue(value);
  }
}
