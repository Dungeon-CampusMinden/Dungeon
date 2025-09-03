package contrib.skill;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProjectileSkill extends Skill {

  public static final Consumer<Entity> DEFAULT_ON_WALL_HIT =entity -> Game.remove(entity);
  public static final Consumer<Entity> DEFAULT_ON_TARGET_REACHED =entity -> Game.remove(entity);
  public static final TriConsumer<Entity, Entity, Direction> DEFAULT_ON_COLLIDE_LEAVE = (entity, entity2, direction) -> {

  };
  ;
  private final Supplier<Point> start;
  private final Supplier<Point> target;
  private final IPath pathToTexturesOfProjectile;
  private final float projectileSpeed;
  private final float projectileRange;
  private final Vector2 projectileHitBoxSize;
  private final Consumer<Entity> onWallHit;
  private final Consumer<Entity> onSpawn;
  private final Consumer<Entity> onTargetReached;
  private TriConsumer<Entity, Entity, Direction> onCollide;
  private final TriConsumer<Entity, Entity, Direction> onCollideLeave;

  private final List<Entity> ignoreEntities = new ArrayList<>();
  private int tintColor = -1; // -1 means no tint

  public ProjectileSkill(
      String name,
      long cooldown,
      Supplier<Point> start,
      Supplier<Point> target,
      IPath pathToTexturesOfProjectile,
      float projectileSpeed,
      float projectileRange,
      Vector2 projectileHitBoxSize,
      Consumer<Entity> onWallHit,
      Consumer<Entity> onSpawn,
      Consumer<Entity> onTargetReached,
      TriConsumer<Entity, Entity, Direction> onCollide,
      TriConsumer<Entity, Entity, Direction> onCollideLeave) {
    super(name, cooldown);
    this.start = start;
    this.target = target;
    this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
    this.projectileSpeed = projectileSpeed;
    this.projectileRange = projectileRange;
    this.projectileHitBoxSize = projectileHitBoxSize;
    this.onWallHit = onWallHit;
    this.onSpawn = onSpawn;
    this.onTargetReached = onTargetReached;
    this.onCollide = onCollide;
    this.onCollideLeave = onCollideLeave;
  }

  @Override
  protected void executeSkill(Entity caster) {
    spawnProjectile(caster);
  }

  protected Entity spawnProjectile(Entity caster) {
    Entity projectile = new Entity(this.name() + "_projectile");
    projectile.add(new FlyComponent());
    projectile.add(new PositionComponent(start.get()));

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
    Point aimedOn = new Point(target.get());
    Point targetPoint =
        SkillTools.calculateLastPositionInRange(startPoint, aimedOn, projectileRange);

    // Calculate the velocity of the projectile
    Vector2 forceToApply =
        SkillTools.calculateDirection(startPoint, targetPoint).scale(projectileSpeed);

    // Add the VelocityComponent to the projectile
    projectile.add(new VelocityComponent(projectileSpeed, onWallHit, true));

    // Add the ProjectileComponent with the initial and target positions to the projectile
    projectile.add(new ProjectileComponent(startPoint, targetPoint, forceToApply, onTargetReached));

    // Add the CollideComponent with the appropriate hit box size and collision handler to the
    // projectile
    projectile.add(
        new CollideComponent(
            CollideComponent.DEFAULT_OFFSET, projectileHitBoxSize, onCollide, onCollideLeave));
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

  protected void onCollide(TriConsumer<Entity, Entity, Direction> onCollide) {
    this.onCollide = onCollide;
  }
}
