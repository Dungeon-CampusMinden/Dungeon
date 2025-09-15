package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.*;
import core.utils.components.MissingComponentException;

/** SkillTools is a collection of helper methods used for skills. */
public final class SkillTools {

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
   * @return The mouse cursor position as Point.
   */
  public static Point cursorPositionAsPoint() {
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

  /**
   * Makes an entity visually "blink" by alternating its tint color for a specified duration.
   *
   * <p>The blink effect is created by scheduling alternating color changes between the given {@code
   * tint} color and the default color ({@code 0xFFFFFFFF}) over the total duration. Each blink
   * consists of two phases: "on" (tint applied) and "off" (default color).
   *
   * @param entity the entity to apply the blink effect on; must have a {@link DrawComponent}
   * @param tint the RGBA color to use for the blink (e.g. {@code 0xFF0000FF} for red)
   * @param totalDuration the total time (in milliseconds) the blinking should last
   * @param times the number of full blinks (each blink = on + off cycle)
   */
  public static void blink(Entity entity, int tint, long totalDuration, int times) {
    // Each blink has two phases: on and off
    long interval = totalDuration / (times * 2);
    DrawComponent dc =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
    int oldtint = dc.tintColor();
    for (int i = 0; i < times * 2; i++) {
      final int step = i;
      EventScheduler.scheduleAction(
          () -> {
            if (step % 2 == 0) {
              dc.tintColor(tint); // blink color
            } else {
              dc.tintColor(oldtint); // old color
            }
          },
          interval * i);
    }
  }
}
