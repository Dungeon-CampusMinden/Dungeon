package contrib.entities;

import contrib.components.AIComponent;
import contrib.components.CollideComponent;
import contrib.utils.components.ai.fight.SentryFightBehaviour;
import contrib.utils.components.ai.fight.StationarySentryAttack;
import contrib.utils.components.ai.idle.SentryPatrolWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.skill.projectileSkill.DamageProjectileSkill;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A factory class for creating different types of sentry entities.
 *
 * <p>This class centralizes the setup of common components such as position, velocity, drawing, and
 * AI behavior for sentries. It provides utility methods to build sentries with specific patrol and
 * fight logic, e.g. a projectile-launching sentry.
 */
public class SentryFactory {
  private static final IPath SENTRY_SPRITESHEET = new SimpleIPath("objects/sentrys");

  /**
   * Creates a generic moving sentry entity with the given attributes and behavior.
   *
   * @param name the name of the entity.
   * @param speed the movement speed of the entity.
   * @param ai the AI component controlling this sentry.
   * @param a the spawn position (and also one patrol point).
   * @param b the second patrol point
   * @param canEnterWalls whether the sentry can move inside walls.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @return a fully constructed sentry entity.
   */
  public static Entity buildMovingSentry(
      String name,
      float speed,
      AIComponent ai,
      Point a,
      Point b,
      boolean canEnterWalls,
      Direction shootDirection) {
    Entity sentry = new Entity(name);

    // Check whether the points are aligned horizontally or vertically
    boolean alignedHorizontally = a.y() == b.y();
    boolean alignedVertically = a.x() == b.x();

    if (!(alignedHorizontally || alignedVertically)) {
      throw new IllegalArgumentException(
          "Moving Sentry: Points a ("
              + a
              + ") and b ("
              + b
              + ") must be either horizontally or vertically aligned.");
    }

    sentry.add(new PositionComponent(a));
    sentry.add(ai);
    sentry.add(new DrawComponent(chooseTextureFromSpriteSheet(shootDirection)));
    sentry.add(new VelocityComponent(speed));
    sentry.add(new CollideComponent());
    if (canEnterWalls) {
      sentry
          .fetch(VelocityComponent.class)
          .ifPresent(
              vc -> {
                vc.canEnterWalls(true);
              });
    }

    return sentry;
  }

  /**
   * Creates a generic sentry entity with the given attributes and behavior.
   *
   * @param name the name of the entity.
   * @param speed the movement speed of the entity.
   * @param ai the AI component controlling this sentry.
   * @param spawnPoint the spawn position.
   * @param canEnterWalls whether the sentry can move inside walls.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @return a fully constructed sentry entity.
   */
  public static Entity buildStationarySentry(
      String name,
      float speed,
      AIComponent ai,
      Point spawnPoint,
      boolean canEnterWalls,
      Direction shootDirection) {
    Entity sentry = new Entity(name);

    sentry.add(new PositionComponent(spawnPoint));
    sentry.add(ai);
    sentry.add(new DrawComponent(chooseTextureFromSpriteSheet(shootDirection)));
    sentry.add(new VelocityComponent(speed));
    sentry.add(new CollideComponent());
    if (canEnterWalls) {
      sentry
          .fetch(VelocityComponent.class)
          .ifPresent(
              vc -> {
                vc.canEnterWalls(true);
              });
    }

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
   *   <li>Attacks with a {@link DamageProjectileSkill} when the hero is within range.
   * </ul>
   *
   * @param a the first patrol point and spawn position.
   * @param b the second patrol point.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param dps the {@link DamageProjectileSkill} used to attack.
   * @param range Maximum shooting (projectile travel) range.
   * @param canEnterWalls whether the sentry can move inside walls.
   * @return a sentry entity that patrols and launches projectiles.
   */
  public static Entity projectileLauncherSentry(
      Point a,
      Point b,
      Direction shootDirection,
      DamageProjectileSkill dps,
      float range,
      boolean canEnterWalls) {

    return buildMovingSentry(
        "sentry",
        4.0f,
        new AIComponent(
            new SentryFightBehaviour(a, b, range, dps, shootDirection, canEnterWalls),
            new SentryPatrolWalk(a, b, canEnterWalls),
            new RangeTransition(range)),
        a,
        b,
        canEnterWalls,
        shootDirection);
  }

  /**
   * Creates a stationary projectile-launching sentry.
   *
   * <p>This sentry:
   *
   * <ul>
   *   <li>stands on a fixed Point.
   *   <li>Uses {@link StationarySentryAttack} to fire projectiles in the given {@link Direction}.
   *   <li>Attacks with a {@link DamageProjectileSkill} when the hero is within range.
   * </ul>
   *
   * @param spawnPoint the spawn position of the entity.
   * @param shootDirection the fixed direction in which the sentry will shoot.
   * @param dps the {@link DamageProjectileSkill} used to attack.
   * @param range Maximum shooting (projectile travel) range.
   * @param canEnterWalls whether the sentry can move inside walls.
   * @return a sentry entity standing still and shooting projectiles.
   */
  public static Entity stationarySentry(
      Point spawnPoint,
      Direction shootDirection,
      DamageProjectileSkill dps,
      float range,
      boolean canEnterWalls) {

    return buildStationarySentry(
        "stationarySentry",
        4.0f,
        new AIComponent(
            new StationarySentryAttack(spawnPoint, range, dps, shootDirection, canEnterWalls),
            entity -> {},
            new RangeTransition(range)),
        spawnPoint,
        canEnterWalls,
        shootDirection);
  }

  private static StateMachine chooseTextureFromSpriteSheet(Direction direction) {
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(SENTRY_SPRITESHEET);

    State stDown = new State("down", animationMap.get("sentrys_down"));
    State stUp = new State("up", animationMap.get("sentrys_up"));
    State stLeft = new State("left", animationMap.get("sentrys_left"));
    State stRight = new State("right", animationMap.get("sentrys_right"));

    StateMachine sm;

    switch (direction) {
      case DOWN:
        sm = new StateMachine(List.of(stDown));
        break;
      case UP:
        sm = new StateMachine(List.of(stUp));
        break;
      case LEFT:
        sm = new StateMachine(List.of(stLeft));
        break;
      case RIGHT:
        sm = new StateMachine(List.of(stRight));
        break;
      default:
        sm = new StateMachine(Arrays.asList(stRight, stLeft, stDown, stUp));
    }
    return sm;
  }
}
