package contrib.utils.components.skill.projectileSkill;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import contrib.utils.components.skill.Resource;
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
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A base class for projectile-based skills. Handles creation, movement, collision, and lifecycle of
 * projectile entities. Subclasses define the projectile's target endpoint.
 */
public abstract class ProjectileSkill extends Skill {

  /** Default action to remove a projectile from the game world. */
  public static final Consumer<Entity> REMOVE_CONSUMER = Game::remove;

  /** A no-op collision consumer. */
  public static final TriConsumer<Entity, Entity, Direction> NOOP_TRICONSUMER =
      (entity1, entity2, direction) -> {};

  /** Default hitbox size for projectiles. */
  public static final Vector2 DEFAULT_HITBOX_SIZE = Vector2.of(1, 1);

  /** A no-op function returning a no-op consumer. */
  public static final Function<Entity, Consumer<Entity>> NOOP_FUNCTION = entity -> entity1 -> {};

  /** A no-op entity consumer. */
  public static final Consumer<Entity> NOOP_CONSUMER = entity -> {};

  protected IPath texture;
  protected Supplier<Point> start;
  protected Supplier<Point> end;
  protected float speed;
  protected float range;
  protected Vector2 hitBoxSize;
  protected int tintColor = -1;
  protected List<Entity> ignoreEntities;

  /**
   * Creates a new projectile skill.
   *
   * @param name Skill name.
   * @param cooldown Cooldown in ms.
   * @param texture Texture for the projectile.
   * @param speed Movement speed of the projectile.
   * @param range Maximum travel distance of the projectile.
   * @param hitBoxSize Hitbox size for collisions.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  public ProjectileSkill(
      String name,
      long cooldown,
      IPath texture,
      float speed,
      float range,
      Vector2 hitBoxSize,
      Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
    this.texture = texture;
    this.speed = speed;
    this.range = range;
    this.hitBoxSize = hitBoxSize;
    this.ignoreEntities = new ArrayList<>();
  }

  /**
   * Executes the projectile skill. Spawns and configures the projectile entity, applies velocity,
   * and sets up collision and lifetime handlers.
   *
   * @param caster The entity that casts this skill.
   */
  @Override
  protected void executeSkill(Entity caster) {
    Entity projectile = new Entity(name() + "_projectile");
    ignoreEntities.add(caster);
    ignoreEntities.add(projectile);

    Point start = start(caster);
    projectile.add(new FlyComponent());
    Point position = caster.fetch(PositionComponent.class).map(pc -> pc.position()).orElse(start);

    projectile.add(new PositionComponent(position));

    try {
      DrawComponent dc = new DrawComponent(texture);
      dc.tintColor(tintColor);
      projectile.add(dc);
    } catch (IOException e) {
      Skill.LOGGER.warning(
          String.format("The DrawComponent for the projectile %s cant be created. ", name())
              + e.getMessage());
      throw new RuntimeException(e);
    }

    // Target point calculation
    Point aimedOn = new Point(end(caster));
    Point targetPoint = SkillTools.calculateLastPositionInRange(start, aimedOn, range);

    // Calculate velocity
    Vector2 forceToApply = SkillTools.calculateDirection(start, targetPoint).scale(speed);

    // Add components
    projectile.add(new VelocityComponent(speed, onWallHit(caster), true));
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    CollideComponent cc =
        new CollideComponent(
            CollideComponent.DEFAULT_OFFSET,
            hitBoxSize,
            onCollideEnter(caster),
            onCollideLeave(caster));
    cc.onHold(onCollideHold(caster));
    projectile.add(cc);

    Game.add(projectile);
    onSpawn(caster, projectile);
  }

  /**
   * Defines what happens when the projectile collides with another entity (on collision enter).
   *
   * @param caster The entity that cast the projectile.
   * @return A collision handler.
   */
  protected TriConsumer<Entity, Entity, Direction> onCollideEnter(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  /**
   * Defines what happens when the projectile stays in collision (on hold).
   *
   * @param caster The entity that cast the projectile.
   * @return A collision handler.
   */
  protected TriConsumer<Entity, Entity, Direction> onCollideHold(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  /**
   * Defines what happens when the projectile leaves a collision.
   *
   * @param caster The entity that cast the projectile.
   * @return A collision handler.
   */
  protected TriConsumer<Entity, Entity, Direction> onCollideLeave(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  /**
   * Defines what happens when the projectile hits a wall.
   *
   * @param caster The entity that cast the projectile.
   * @return A consumer handling wall collisions.
   */
  protected Consumer<Entity> onWallHit(Entity caster) {
    return REMOVE_CONSUMER;
  }

  /**
   * Defines what happens when the projectile reaches its target point.
   *
   * @param caster The entity that cast the projectile.
   * @return A consumer handling end of trajectory.
   */
  protected Consumer<Entity> onEndReached(Entity caster) {
    return REMOVE_CONSUMER;
  }

  /**
   * Hook executed when the projectile spawns.
   *
   * @param caster The entity that cast the projectile.
   * @param projectile The newly spawned projectile entity.
   */
  protected void onSpawn(Entity caster, Entity projectile) {}

  /**
   * Calculates the start position of the projectile.
   *
   * @param caster The entity that cast the projectile.
   * @return The starting point.
   */
  protected Point start(Entity caster) {
    return caster
        .fetch(CollideComponent.class)
        .map(collideComponent -> collideComponent.center(caster))
        .orElse(new Point(0, 0));
  }

  /**
   * Calculates the end position (target point) of the projectile.
   *
   * @param caster The entity that cast the projectile.
   * @return The endpoint of the projectile.
   */
  protected abstract Point end(Entity caster);

  /**
   * Adds an entity to the list of ignored entities for collision.
   *
   * @param entity The entity to ignore.
   */
  public void ignoreEntity(Entity entity) {
    ignoreEntities.add(entity);
  }

  /**
   * Removes an entity from the ignore list.
   *
   * @param entity The entity to stop ignoring.
   */
  public void removeIgnoredEntity(Entity entity) {
    ignoreEntities.remove(entity);
  }

  /**
   * Sets the tint color of the projectile. Use -1 to disable tinting.
   *
   * @param tintColor The tint color.
   */
  public void tintColor(int tintColor) {
    this.tintColor = tintColor;
  }

  /**
   * Returns the current tint color of the projectile.
   *
   * @return The tint color, or -1 if none.
   */
  public int tintColor() {
    return tintColor;
  }

  /**
   * @return Current speed of the projectile.
   */
  public float speed() {
    return speed;
  }

  /**
   * @param speed New speed for the projectile.
   */
  public void speed(float speed) {
    this.speed = speed;
  }

  /**
   * @return Current range of the projectile.
   */
  public float range() {
    return range;
  }

  /**
   * @param range New maximum range of the projectile.
   */
  public void range(float range) {
    this.range = range;
  }

  /**
   * @return Current hitbox size of the projectile.
   */
  public Vector2 hitBoxSize() {
    return hitBoxSize;
  }

  /**
   * @param hitBoxSize New hitbox size of the projectile.
   */
  public void hitBoxSize(Vector2 hitBoxSize) {
    this.hitBoxSize = hitBoxSize;
  }

  /**
   * @return The current texture used by the projectile.
   */
  public IPath texture() {
    return texture;
  }

  /**
   * @param texture New texture for the projectile.
   */
  public void texture(IPath texture) {
    this.texture = texture;
  }
}
