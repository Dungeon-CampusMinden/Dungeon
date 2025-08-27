package contrib.utils.components.skill.damageSkill.projectile;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.HealthComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
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

public class DamageProjectileSkill extends DamageSkill {

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
  public static final BiConsumer<Entity, Entity> DEFAULT_BONUS_EFFECT = (a, b) -> {};

  private final IPath pathToTexturesOfProjectile;
  private final float projectileSpeed;
  private final float projectileRange;
  private final Vector2 projectileHitBoxSize;
  private final Consumer<Entity> onWallHit;
  private final Consumer<Entity> onSpawn;
  private final List<Entity> ignoreEntities = new ArrayList<>();
  private int tintColor = -1; // -1 means no tint
  private Supplier<Point> targetSelector;

  private BiConsumer<Entity, Entity> bonusEffect;

  public DamageProjectileSkill(
      String name,
      long cooldown,
      Supplier<Point> targetSelector,
      int damageAmount,
      DamageType damageType,
      IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      float projectileRange,
      Vector2 projectileHitBoxSize,
      Consumer<Entity> onWallHit,
      Consumer<Entity> onSpawn,
      BiConsumer<Entity, Entity> bonusEffect) {
    super(name, cooldown, damageAmount, damageType);
    this.targetSelector = targetSelector;
    this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
    this.projectileSpeed = projectileSpeed;
    this.projectileRange = projectileRange;
    this.projectileHitBoxSize = projectileHitBoxSize;
    this.onWallHit = onWallHit;
    this.onSpawn = onSpawn;
    this.bonusEffect = bonusEffect;
  }

  @Override
  protected void executeSkill(Entity caster) {
    spawnProjectile(caster);
  }

  protected Entity spawnProjectile(Entity caster) {
    Entity projectile = new Entity(this.name() + "_projectile");
    projectile.add(new FlyComponent());
    // Get the PositionComponent of the entity
    PositionComponent epc =
        caster
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(caster, PositionComponent.class));
    projectile.add(new PositionComponent(epc.position()));

    try {
      DrawComponent dc = new DrawComponent(pathToTexturesOfProjectile);
      dc.tintColor(tintColor());
      projectile.add(dc);
    } catch (IOException e) {
      Skill.LOGGER.warning(
          String.format("The DrawComponent for the projectile %s cant be created. ", caster)
              + e.getMessage());
      throw new RuntimeException();
    }

    Point startPoint =
        caster
            .fetch(CollideComponent.class)
            .map(collideComponent -> collideComponent.center(caster))
            .orElse(new Point(0, 0));

    // Get the target point based on the selection function and projectile range.
    // Use a copy, so you do not change the actual value (for example the hero position)
    Point aimedOn = new Point(targetSelector.get());
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
          if (b != caster && !ignoreEntities.contains(b)) {
            b.fetch(HealthComponent.class)
                .ifPresent(
                    hc -> {
                      bonusEffect.accept(projectile, b);
                      // Apply the projectile damage to the collided entity
                      hc.receiveHit(calculateDamage(caster, b, from));

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
    onSpawn.accept(projectile);
    return projectile;
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
