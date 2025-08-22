package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import core.Game;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** SkillTools is a collection of helper methods used for skills. */
public final class SkillTools {
  private static final Logger LOGGER = LoggerFactory.getLogger(SkillTools.class);

  /**
   * A skill has a range in which it is effective. This is a calculation of the last position in
   * range of the skill.
   *
   * @param startPoint Start point of the calculation.
   * @param aimPoint Point of the target.
   * @param range Range in which the skill is effective.
   * @return The last position in the range from startPoint to aimPoint.
   */
  public static Point calculateLastPositionInRange(
      final Point startPoint, final Point aimPoint, float range) {
    return startPoint.translate(startPoint.vectorTo(aimPoint).normalize().scale(range));
  }

  /**
   * Calculates the direction vector to move from the start point to the goal point with the given
   * speed.
   *
   * @param start Start point of the calculation.
   * @param goal End point of the calculation.
   * @return The velocity vector as a Point.
   */
  public static Vector2 calculateDirection(final Point start, final Point goal) {
    if (start.equals(goal)) {
      return Vector2.ZERO;
    }
    return start.vectorTo(goal).normalize();
  }

  /**
   * Gets the current cursor position as Point. The cursor is used to aim.
   *
   * <p>Note: This method uses the Gdx.graphics to unproject the cursor position from screen
   * coordinates to world coordinates. If Gdx.graphics is null, it returns a default Point (0, 0).
   *
   * @return The mouse cursor position as Point.
   */
  public static Point cursorPositionAsPoint() {
    if (Gdx.graphics == null) {
      LOGGER.error("Gdx.graphics is null, returning default Point(0, 0) for cursor position.");
      return new Point(0, 0);
    }

    Vector3 mousePosition =
        CameraSystem.camera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    return new Point(mousePosition.x, mousePosition.y);
  }

  /**
   * Gets the current hero position as Point.
   *
   * @return The current hero position as Point.
   */
  public static Point heroPositionAsPoint() {
    PositionComponent pc =
        Game.hero()
            .orElseThrow(() -> new MissingHeroException("There is no hero in the game."))
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(Game.hero().get(), PositionComponent.class));
    return pc.position();
  }
}
