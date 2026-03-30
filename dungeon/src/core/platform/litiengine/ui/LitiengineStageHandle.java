package core.platform.litiengine.ui;

import core.ui.StageHandle;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * LITIENGINE-backed {@link StageHandle}.
 *
 * <p>At the current migration stage, LITIENGINE dialogs are rendered as custom screen overlays
 * instead of a widget tree comparable to libGDX Scene2D. This handle still exposes the stage-like
 * information that the engine-agnostic HUD lifecycle expects.
 */
public final class LitiengineStageHandle implements StageHandle {

  @Override
  public Object raw() {
    return Game.screens();
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    Object raw = raw();
    return type.isInstance(raw) ? Optional.of(type.cast(raw)) : Optional.empty();
  }

  @Override
  public float getWidth() {
    try {
      return Game.window().getWidth();
    } catch (Exception ignored) {
      return 0f;
    }
  }

  @Override
  public float getHeight() {
    try {
      return Game.window().getHeight();
    } catch (Exception ignored) {
      return 0f;
    }
  }

  @Override
  public void addActor(Object actor) {
    // no-op for now:
    // LITIENGINE dialog overlays are rendered through EcsRenderScreen, not a Stage widget tree.
  }

  @Override
  public void setKeyboardFocus(Object actor) {
    // no-op for now
  }

  @Override
  public int mouseX() {
    try {
      Point2D mouse = Input.mouse().getLocation();
      return mouse == null ? 0 : (int) Math.round(mouse.getX());
    } catch (Exception ignored) {
      return 0;
    }
  }

  @Override
  public int mouseY() {
    try {
      Point2D mouse = Input.mouse().getLocation();
      return mouse == null ? 0 : (int) Math.round(mouse.getY());
    } catch (Exception ignored) {
      return 0;
    }
  }
}
