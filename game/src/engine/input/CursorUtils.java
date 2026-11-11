package engine.input;

import com.badlogic.gdx.math.Vector3;
import engine.Game;
import engine.systems.CameraSystem;
import engine.utils.Point;
import engine.utils.Vector2;

/** Converts the current pointer position between screen and game-world coordinates. */
public final class CursorUtils {

  private CursorUtils() {}

  /**
   * Returns the current cursor position in world coordinates.
   *
   * @return the cursor position in the game world
   * @throws IllegalStateException if no graphical runtime is available
   */
  public static Point positionInWorld() {
    if (Game.isHeadless()) {
      throw new IllegalStateException("Cannot read the cursor in headless mode.");
    }

    Vector2 pointer = GameInput.pointer();
    Vector3 mousePosition =
        CameraSystem.camera().unproject(new Vector3(pointer.x(), pointer.y(), 0));
    return new Point(mousePosition.x, mousePosition.y);
  }
}
