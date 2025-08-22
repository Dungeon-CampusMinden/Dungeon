package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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

  /**
   * The default behavior when the projectile gets spawned.
   *
   * <p>The default behavior is to do nothing.
   */
  public static final Consumer<Entity> DEFAULT_ON_SPAWN = (a) -> {};

  /**
   * The default behavior when a wall is hit by the projectile.
   *
   * <p>The default behavior is to remove the projectile from the game.
   */
  public static final Consumer<Entity> DEFAULT_ON_WALL_HIT = Game::remove;

  /**
   * The default behavior when an entity is hit by the projectile.
   *
   * <p>The default behavior is to do nothing.
   */
  public static final BiConsumer<Entity, Entity> DEFAULT_ON_ENTITY_HIT = (a, b) -> {};

  private static final Logger LOGGER = Logger.getLogger(DamageProjectile.class.getSimpleName());
  private final IPath pathToTexturesOfProjectile;
  private final float projectileSpeed;
  private final float projectileRange;
  private final int damageAmount;
  private final DamageType damageType;
  private final Vector2 projectileHitBoxSize;
  private final Supplier<Point> selectionFunction;
  private final Consumer<Entity> onWallHit;
  private final Consumer<Entity> onSpawn;
  private final String name;
  private final List<Entity> ignoreEntities = new ArrayList<>();

  /**
   * The behavior when an entity is hit. (The first parameter is the projectile, the second the
   * entity that was hit)
   */
  private final BiConsumer<Entity, Entity> onEntityHit;

  private int tintColor = -1; // -1 means no tint

  /**
   * The DamageProjectile constructor sets the name, the path to the textures of the projectile, the
   * speed of the projectile, the damage amount and type to be dealt, the size of the projectile's
   * hit box, the target selection function, the range of the projectile, and the behavior when a
   * wall is hit.
   *
   * <p>For a specific implementation, see {@link FireballSkill}.
   *
   * @param name Name of the projectile.
   * @param pathToTexturesOfProjectile Path to the textures of the projectile.
   * @param projectileSpeed Speed of the projectile.
   * @param damageAmount Amount of damage to be dealt.
   * @param damageType Type of damage to be dealt.
   * @param projectileHitBoxSize Size of the hit box.
   * @param selectionFunction Specific functionality of the projectile.
   * @param projectileRange Range in which the projectile is effective.
   * @param onWallHit Behavior when a wall is hit.
   * @param onEntityHit Behavior when an entity is hit before the damage is applied.
   * @param onSpawn Behavior when the projectile is spawned.
   */
  public DamageProjectile(
      final String name,
      final IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      int damageAmount,
      final DamageType damageType,
      final Vector2 projectileHitBoxSize,
      final Supplier<Point> selectionFunction,
      float projectileRange,
      final Consumer<Entity> onWallHit,
      final BiConsumer<Entity, Entity> onEntityHit,
      final Consumer<Entity> onSpawn) {
    this.name = name;
    this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
    this.damageAmount = damageAmount;
    this.damageType = damageType;
    this.projectileSpeed = projectileSpeed;
    this.projectileRange = projectileRange;
    this.projectileHitBoxSize = projectileHitBoxSize;
    this.selectionFunction = selectionFunction;
    this.onWallHit = onWallHit;
    this.onEntityHit = onEntityHit;
    this.onSpawn = onSpawn;
  }

  /**
   * The DamageProjectile constructor sets the name, the path to the textures of the projectile, the
   * speed of the projectile, the damage amount and type to be dealt, the size of the projectile's
   * hit box, the target selection function, and the range of the projectile.
   *
   * <p>For a specific implementation, see {@link FireballSkill}
   *
   * @param name Name of the projectile.
   * @param pathToTexturesOfProjectile Path to the textures of the projectile.
   * @param projectileSpeed Speed of the projectile.
   * @param damageAmount Amount of damage to be dealt.
   * @param damageType Type of damage to be dealt.
   * @param projectileHitBoxSize Size of the hit box.
   * @param selectionFunction Specific functionality of the projectile.
   * @param projectileRange Range in which the projectile is effective.
   */
  public DamageProjectile(
      String name,
      final IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      int damageAmount,
      final DamageType damageType,
      final Vector2 projectileHitBoxSize,
      final Supplier<Point> selectionFunction,
      float projectileRange) {
    this(
        name,
        pathToTexturesOfProjectile,
        projectileSpeed,
        damageAmount,
        damageType,
        projectileHitBoxSize,
        selectionFunction,
        projectileRange,
        DEFAULT_ON_WALL_HIT,
        DEFAULT_ON_ENTITY_HIT,
        DEFAULT_ON_SPAWN);
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
      final Vector2 projectileHitBoxSize,
      final Supplier<Point> selectionFunction,
      float projectileRange) {
    this(
        "DamageProjectile",
        pathToTexturesOfProjectile,
        projectileSpeed,
        damageAmount,
        damageType,
        projectileHitBoxSize,
        selectionFunction,
        projectileRange);
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
    Entity projectile = new Entity(name);
    projectile.add(new FlyComponent());
    // Get the PositionComponent of the entity
    PositionComponent epc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    projectile.add(new PositionComponent(epc.position()));

    try {
      DrawComponent dc = new DrawComponent(pathToTexturesOfProjectile);
      dc.tintColor(tintColor());
      projectile.add(dc);
    } catch (IOException e) {
      LOGGER.warning(
          String.format("The DrawComponent for the projectile %s cant be created. ", entity)
              + e.getMessage());
      throw new RuntimeException();
    }

    Point startPoint =
        entity
            .fetch(CollideComponent.class)
            .map(collideComponent -> collideComponent.center(entity))
            .orElse(new Point(0, 0));

    // Get the target point based on the selection function and projectile range.
    // Use a copy, so you do not change the actual value (for example the hero position)
    Point aimedOn = new Point(selectionFunction.get());
    Point targetPoint =
        SkillTools.calculateLastPositionInRange(startPoint, aimedOn, projectileRange);

    // Calculate the velocity of the projectile
    Vector2 forceToApply =
        SkillTools.calculateDirection(startPoint, targetPoint).scale(projectileSpeed);

    // Add the VelocityComponent to the projectile
    projectile.add(new VelocityComponent(projectileSpeed, onWallHit, true));

    // Add the ProjectileComponent with the initial and target positions to the projectile
    projectile.add(
        new ProjectileComponent(startPoint, targetPoint, forceToApply, p -> Game.remove(p)));

    // Create a collision handler for the projectile
    TriConsumer<Entity, Entity, Direction> collide =
        (a, b, from) -> {
          if (b != entity && !ignoreEntities.contains(b)) {
            b.fetch(HealthComponent.class)
                .ifPresent(
                    hc -> {
                      onEntityHit.accept(projectile, b);
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
    if (Gdx.files != null) playSound();
    onSpawn.accept(projectile);
  }

  /**
   * Adds an entity to the list of entities to be ignored by the projectile. Entities in this list
   * will not be affected by the projectile's collision handler.
   *
   * @param entity The entity to be ignored by the projectile.
   */
  public void ignoreEntity(Entity entity) {
    ignoreEntities.add(entity);
  }

  /**
   * Removes an entity from the list of entities to be ignored by the projectile. Entities not in
   * this list will be affected by the projectile's collision handler.
   *
   * @param entity The entity to be removed from the ignore list.
   */
  public void removeIgnoredEntity(Entity entity) {
    ignoreEntities.remove(entity);
  }

  /** Override this method to play a Sound-effect on spawning the projectile if you want. */
  protected void playSound() {}

  /**
   * Sets the tint color of the projectile. Set to -1 to remove the tint.
   *
   * @param tintColor The tint color of the projectile.
   */
  public void tintColor(int tintColor) {
    this.tintColor = tintColor;
  }

  /**
   * Returns the tint color of the projectile.
   *
   * @return The tint color of the projectile. -1 means no tint.
   */
  public int tintColor() {
    return tintColor;
  }
}
