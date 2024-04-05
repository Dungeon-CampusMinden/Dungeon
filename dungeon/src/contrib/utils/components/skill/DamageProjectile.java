package contrib.utils.components.skill;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.SoundPlayer;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Abstract class that represents a projectile capable of dealing damage to entities.
 *
 * <p>The DamageProjectile class implements the Consumer interface, allowing it to accept an entity
 * as a parameter.
 */
public abstract class DamageProjectile implements Consumer<Entity> {

  private static final Consumer<Entity> DEFAULT_ON_WALL_HIT = Game::remove;
  private static final Logger LOGGER = Logger.getLogger(DamageProjectile.class.getSimpleName());
  private final IPath pathToTexturesOfProjectile;
  private final float projectileSpeed;
  private final float projectileRange;
  private final int damageAmount;
  private final DamageType damageType;
  private final Point projectileHitBoxSize;
  private final Supplier<Point> selectionFunction;
  private final Consumer<Entity> onWallHit;

  /**
   * The DamageProjectile constructor sets the path to the textures of the projectile, the speed of
   * the projectile, the damage amount and type to be dealt, the size of the projectile's hit box,
   * the target selection function, the range of the projectile, and the behavior when a wall is
   * hit.
   *
   * <p>For a specific implementation, see {@link FireballSkill}.
   *
   * @param pathToTexturesOfProjectile Path to the textures of the projectile.
   * @param projectileSpeed Speed of the projectile.
   * @param damageAmount Amount of damage to be dealt.
   * @param damageType Type of damage to be dealt.
   * @param projectileHitBoxSize Size of the hit box.
   * @param selectionFunction Specific functionality of the projectile.
   * @param projectileRange Range in which the projectile is effective.
   * @param onWallHit Behavior when a wall is hit.
   */
  public DamageProjectile(
      final IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      int damageAmount,
      final DamageType damageType,
      final Point projectileHitBoxSize,
      final Supplier<Point> selectionFunction,
      float projectileRange,
      final Consumer<Entity> onWallHit) {
    this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.projectileSpeed = projectileSpeed;
    this.projectileRange = projectileRange;
    this.projectileHitBoxSize = projectileHitBoxSize;
    this.selectionFunction = selectionFunction;
    this.onWallHit = onWallHit;
  }

  /**
   * The DamageProjectile constructor sets the path to the textures of the projectile, the speed of
   * the projectile, the damage amount and type to be dealt, the size of the projectile's hit box,
   * the target selection function, and the range of the projectile.
   *
   * <p>For a specific implementation, see {@link FireballSkill}
   *
   * @param pathToTexturesOfProjectile Path to the textures of the projectile.
   * @param projectileSpeed Speed of the projectile.
   * @param damageAmount Amount of damage to be dealt.
   * @param damageType Type of damage to be dealt.
   * @param projectileHitBoxSize Size of the hit box.
   * @param selectionFunction Specific functionality of the projectile.
   * @param projectileRange Range in which the projectile is effective.
   */
  public DamageProjectile(
      final IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      int damageAmount,
      final DamageType damageType,
      final Point projectileHitBoxSize,
      final Supplier<Point> selectionFunction,
      float projectileRange) {
    this(
        pathToTexturesOfProjectile,
        projectileSpeed,
        damageAmount,
        damageType,
        projectileHitBoxSize,
        selectionFunction,
        projectileRange,
        DEFAULT_ON_WALL_HIT);
  }

  /**
   * Performs the necessary actions to create and handle the damage projectile based on the provided
   * entity.
   *
   * <p>The projectile can not collide with the casting entity.
   *
   * <p>The cause for the damage will not be the projectile, but the entity that casts the
   * projectile.
   *
   * @param entity The entity that casts the projectile. The entity's position will be the start
   *     position for the projectile.
   * @throws MissingComponentException if the entity does not have a PositionComponent.
   */
  @Override
  public void accept(final Entity entity) {
    Entity projectile = new Entity("Projectile");
    // Get the PositionComponent of the entity
    PositionComponent epc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    projectile.add(new PositionComponent(epc.position()));

    try {
      projectile.add(new DrawComponent(pathToTexturesOfProjectile));
    } catch (IOException e) {
      LOGGER.warning(
          String.format("The DrawComponent for the projectile %s cant be created. ", entity)
              + e.getMessage());
      throw new RuntimeException();
    }

    Point startPoint = new Point(0, 0);
    entity
        .fetch(CollideComponent.class)
        .ifPresent(
            collideComponent -> {
              startPoint.x = collideComponent.center(entity).x;
              startPoint.y = collideComponent.center(entity).y;
            });

    // Get the target point based on the selection function and projectile range.
    // Use a copy, so you do not change the actual value (for example the hero position)
    Point aimedOn = new Point(selectionFunction.get());
    Point targetPoint =
        SkillTools.calculateLastPositionInRange(startPoint, aimedOn, projectileRange);

    // Calculate the velocity of the projectile
    Point velocity = SkillTools.calculateVelocity(startPoint, targetPoint, projectileSpeed);

    // Add the VelocityComponent to the projectile
    VelocityComponent vc = new VelocityComponent(velocity.x, velocity.y, onWallHit);
    projectile.add(vc);

    // Add the ProjectileComponent with the initial and target positions to the projectile
    projectile.add(new ProjectileComponent(startPoint, targetPoint));

    // Create a collision handler for the projectile
    TriConsumer<Entity, Entity, Tile.Direction> collide =
        (a, b, from) -> {
          if (b != entity) {
            b.fetch(HealthComponent.class)
                .ifPresent(
                    hc -> {
                      // Apply the projectile damage to the collided entity
                      hc.receiveHit(new Damage(damageAmount, damageType, entity));

                      // Remove the projectile entity from the game
                      Game.remove(projectile);
                    });
          }
        };

    // Add the CollideComponent with the appropriate hit box size and collision handler to the
    // projectile
    projectile.add(
        new CollideComponent(CollideComponent.DEFAULT_OFFSET, projectileHitBoxSize, collide, null));
    Game.add(projectile);
    playSound();
  }

  private void playSound() {
    SoundPlayer.playSound(
        getSoundPath(), false, getSoundVolume(), getSoundMinPitch(), getSoundMaxPitch());
  }

  /**
   * Override this method to play a Sound-effect on spawning the projectile if you want.
   *
   * @return the path to the sound-file that should be played on spawning the projectile.
   */
  protected IPath getSoundPath() {
    return null;
  }

  /**
   * You can override this method if you want, to adjust the default value.
   *
   * @return the sound volume this projectile should have (0.05f to 1.0f are reasonable values).
   */
  protected float getSoundVolume() {
    return 0.05f;
  }

  /**
   * You can override this method if you want, to adjust the default value.
   *
   * @return the sound minimum pitch value this projectile should have (1f to 10f are reasonable
   *     values).
   */
  protected float getSoundMinPitch() {
    return 2f;
  }

  /**
   * You can override this method if you want, to adjust the default value.
   *
   * @return the sound maximum pitch value this projectile should have (1f to 10f are reasonable
   *     values).
   */
  protected float getSoundMaxPitch() {
    return 3f;
  }
}
