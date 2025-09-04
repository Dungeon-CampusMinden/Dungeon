package contrib.entities;

import contrib.components.AIComponent;
import contrib.utils.components.ai.fight.SentryFightBehaviour;
import contrib.utils.components.ai.idle.SentryPatrolWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * A factory class for creating different types of sentry entities.
 *
 * <p>This class centralizes the setup of common components such as position, velocity, drawing, and
 * AI behavior for sentries. It provides utility methods to build sentries with specific patrol and
 * fight logic, e.g. a projectile-launching sentry.
 */
public class SentryFactory {
  private static final IPath PROJECTILE_LAUNCHER_PATH =
      new SimpleIPath("character/monster/necromancer");

  /**
   * Creates a generic sentry entity with the given attributes and behavior.
   *
   * @param name the name of the entity
   * @param texture the texture path for the entity's draw component
   * @param speed the movement speed of the entity
   * @param ai the AI component controlling this sentry
   * @param a the spawn position (and also one patrol point)
   * @return a fully constructed sentry entity
   * @throws IOException if the texture cannot be loaded
   */
  public static Entity buildSentry(
      String name, IPath texture, float speed, AIComponent ai, Point a, Point b)
      throws IOException {
    Entity sentry = new Entity(name);

    // Check whether the points are aligned horizontally or vertically
    boolean alignedHorizontally = a.y() == b.y();
    boolean alignedVertically = a.x() == b.x();

    if (!(alignedHorizontally || alignedVertically)) {
      throw new IllegalArgumentException(
          "Points a ("
              + a
              + ") and b ("
              + b
              + ") must be either horizontally or vertically aligned.");
    }

    PositionComponent positionComponent = new PositionComponent();
    positionComponent.position(a);
    sentry.add(positionComponent);
    sentry.add(ai);
    sentry.add(new DrawComponent(texture));
    sentry.add(new VelocityComponent(speed));

    return sentry;
  }

  /**
   * Creates a projectile-launching sentry.
   *
   * <p>This sentry:
   *
   * <ul>
   *   <li>Patrols between two points {@code a} and {@code b}.
   *   <li>Uses {@link SentryFightBehaviour} to fire projectiles in the given {@link Direction}.
   *   <li>Attacks with a {@link FireballSkill} when the hero is within range.
   * </ul>
   *
   * @param a the first patrol point and spawn position
   * @param b the second patrol point
   * @param shootDirection the fixed direction in which the sentry will shoot
   * @return a sentry entity that patrols and launches projectiles
   * @throws IOException if the texture cannot be loaded
   */
  public static Entity projectileLauncherSentry(
      Point a, Point b, Direction shootDirection, long cooldown) throws IOException {
    return buildSentry(
        "projectileLauncher",
        PROJECTILE_LAUNCHER_PATH,
        4.0f,
        new AIComponent(
            new SentryFightBehaviour(
                a,
                b,
                10.0f,
                new FireballSkill(SkillTools::heroPositionAsPoint, cooldown),
                shootDirection),
            new SentryPatrolWalk(a, b),
            new RangeTransition(5.0f)),
        a,
        b);
  }
}
