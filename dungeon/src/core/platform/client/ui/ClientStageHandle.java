package core.platform.client.ui;

import core.ui.StageHandle;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * A client-specific implementation of the {@link StageHandle} interface for managing and
 * interacting with a UI stage in the context of the client-side environment.
 *
 * <p>This implementation provides an abstraction layer over client-specific UI stage management,
 * offering features such as retrieving stage dimensions, querying mouse input, and interacting with
 * the underlying UI stage object.
 */
public final class ClientStageHandle implements StageHandle {

  @Override
  public Object raw() {
    return Game.screens();
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
