package contrib.utils.components.skill.projectileSkill;

import contrib.components.CollideComponent;
import contrib.components.FlyComponent;
import contrib.components.ProjectileComponent;
import contrib.components.WallHitStateComponent;
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
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
  public static final Vector2 DEFAULT_HITBOX_SIZE = Vector2.of(0.7f, 0.7f);

  /** Default hitbox offset for projectiles. */
  public static final Vector2 DEFAULT_HITBOX_OFFSET = Vector2.of(0.15f, 0.15f);

  protected boolean ignoreOtherProjectiles = true;

  private static final float pushOffset = 1f;

  protected IPath texture;
  protected float speed;
  protected float range;
  protected Vector2 hitBoxSize;
  protected Vector2 hitBoxOffset;
  protected int tintColor = -1;
  protected List<Entity> ignoreEntities;
  private boolean ignoreFirstWall;

  /**
   * Creates a new projectile skill.
   *
   * @param name Skill name.
   * @param cooldown Cooldown in ms.
   * @param texture Texture for the projectile.
   * @param speed Movement speed of the projectile.
   * @param range Maximum travel distance of the projectile.
   * @param hitBoxSize Hitbox size for collisions.
   * @param hitBoxOffset Hitbox offset for collisions.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
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
      Vector2 hitBoxOffset,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    super(name, cooldown, resourceCost);
    this.texture = texture;
    this.speed = speed;
    this.range = range;
    this.hitBoxSize = hitBoxSize;
    this.hitBoxOffset = hitBoxOffset;
    this.ignoreEntities = new ArrayList<>();
    this.ignoreFirstWall = ignoreFirstWall;
  }

  /**
   * Creates a new projectile skill.
   *
   * @param name Skill name.
   * @param cooldown Cooldown in ms.
   * @param texture Texture for the projectile.
   * @param speed Movement speed of the projectile.
   * @param range Maximum travel distance of the projectile.
   * @param ignoreFirstWall whether the projectile ignores the first wall.
   * @param resourceCost Resource costs for casting.
   */
  @SafeVarargs
  public ProjectileSkill(
      String name,
      long cooldown,
      IPath texture,
      float speed,
      float range,
      boolean ignoreFirstWall,
      Tuple<Resource, Integer>... resourceCost) {
    this(
        name,
        cooldown,
        texture,
        speed,
        range,
        DEFAULT_HITBOX_SIZE,
        DEFAULT_HITBOX_OFFSET,
        ignoreFirstWall,
        resourceCost);
  }

  /**
   * Executes the projectile skill. Spawns and configures the projectile entity, applies velocity,
   * and sets up collision and lifetime handlers.
   *
   * @param caster The entity that casts this skill.
   */
  @Override
  protected void executeSkill(Entity caster) {
    shootProjectile(caster, start(caster), end(caster));
  }

  protected void shootProjectile(Entity caster, Point start, Point aimedOn) {
    Entity projectile = new Entity(name() + "_projectile");
    ignoreEntities.add(caster);
    ignoreEntities.add(projectile);

    projectile.add(new FlyComponent());
    DrawComponent dc = new DrawComponent(texture);

    dc.tintColor(tintColor);
    projectile.add(dc);

    // Target point calculation
    Point targetPoint = SkillTools.calculateLastPositionInRange(start, aimedOn, range);

    Point position = start.translate(hitBoxSize.scale(-0.5)); // +offset
    PositionComponent pc = new PositionComponent(position);
    projectile.add(pc);
    // calculate rotation
    double angleDeg = Vector2.of(position).angleToDeg(Vector2.of(targetPoint));
    pc.rotation((float) angleDeg);
    // Calculate velocity
    Vector2 forceToApply = SkillTools.calculateDirection(start, targetPoint).scale(speed);

    // Add components
    VelocityComponent vc = new VelocityComponent(speed, handleProjectileWallHit(caster), true);
    vc.moveboxSize(hitBoxSize);
    vc.moveboxOffset(hitBoxOffset);
    projectile.add(vc);
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    CollideComponent cc =
        new CollideComponent(
            hitBoxOffset, hitBoxSize, onCollideEnter(caster), onCollideLeave(caster));
    cc.onHold(onCollideHold(caster));
    cc.isSolid(false);
    projectile.add(cc);

    Game.add(projectile);
    onSpawn(caster, projectile);
  }

  /**
   * Defines the behavior when the projectile collides with another entity (on collision enter).
   *
   * @param caster the entity that created or cast the projectile
   * @return a collision handler; the parameters are:
   *     <ul>
   *       <li>the projectile entity
   *       <li>the entity the projectile collides with
   *       <li>the collision direction, relative to the projectile
   *     </ul>
   */
  protected TriConsumer<Entity, Entity, Direction> onCollideEnter(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  /**
   * Defines the behavior while the projectile remains in collision with another entity.
   *
   * @param caster the entity that created or cast the projectile
   * @return a collision handler; the parameters are:
   *     <ul>
   *       <li>the projectile entity
   *       <li>the entity the projectile is colliding with
   *       <li>the collision direction, relative to the projectile
   *     </ul>
   */
  protected TriConsumer<Entity, Entity, Direction> onCollideHold(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  /**
   * Defines the behavior when the projectile ends a collision with another entity.
   *
   * @param caster the entity that created or cast the projectile
   * @return a collision handler; the parameters are:
   *     <ul>
   *       <li>the projectile entity
   *       <li>the entity the projectile collided with
   *       <li>the collision direction, relative to the projectile
   *     </ul>
   */
  protected TriConsumer<Entity, Entity, Direction> onCollideLeave(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  /**
   * Defines the behavior when the projectile collides with a wall.
   *
   * @param caster the entity that created or cast the projectile
   * @param projectile the projectile entity
   */
  protected void onWallHit(Entity caster, Entity projectile) {
    Game.remove(projectile);
  }

  /**
   * Defines the behavior when the projectile reaches its target point.
   *
   * @param caster the entity that created or cast the projectile
   * @return a {@link Consumer} that is invoked when the projectile ends its trajectory; the
   *     projectile entity is passed to the consumer
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
        .or(() -> caster.fetch(PositionComponent.class).map(PositionComponent::position))
        .orElseThrow(() -> MissingComponentException.build(caster, PositionComponent.class));
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
   * @param tintColor The tint color in RGBA.
   */
  public void tintColor(int tintColor) {
    this.tintColor = tintColor;
  }

  /**
   * Returns the current tint color of the projectile.
   *
   * @return The tint color in RGBA, or -1 if none.
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
   * @return Current hitbox offset of the projectile.
   */
  public Vector2 hitBoxOffset() {
    return hitBoxOffset;
  }

  /**
   * @param hitBoxOffset New hitbox offset of the projectile.
   */
  public void hitBoxOffset(Vector2 hitBoxOffset) {
    this.hitBoxOffset = hitBoxOffset;
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

  /**
   * @return if the projectile ignores the first wall hit.
   */
  public boolean isIgnoreFirstWall() {
    return ignoreFirstWall;
  }

  /**
   * @param ignoreFirstWall new value for ignoreFirstWall.
   */
  public void setIgnoreFirstWall(boolean ignoreFirstWall) {
    this.ignoreFirstWall = ignoreFirstWall;
  }

  /**
   * Checks whether the projectiles's {@code ignoreFirstWall} is set to {@code true}. If so, the
   * projectile is allowed to ignore the first wall hit and continue flying through it.
   *
   * @param caster the entity that created or cast the projectile
   * @return a {@link Consumer} that handles wall collisions; the projectile entity is passed to the
   *     consumer
   */
  protected Consumer<Entity> handleProjectileWallHit(Entity caster) {
    return projectile -> {
      projectile
          .fetch(VelocityComponent.class)
          .ifPresent(
              vel -> {
                if (this.ignoreFirstWall) {
                  WallHitStateComponent state =
                      projectile
                          .fetch(WallHitStateComponent.class)
                          .orElseGet(
                              () -> {
                                WallHitStateComponent newState = new WallHitStateComponent();
                                projectile.add(newState);
                                return newState;
                              });

                  if (!state.firstWallHitIgnored()) {
                    // ignore the first wall hit
                    state.markFirstWallHitIgnored();

                    // push the projectile forward
                    moveThroughWall(projectile, vel);

                    return;
                  }
                }

                onWallHit(caster, projectile);
              });
    };
  }

  /**
   * Pushes the projectile a small distance forward in its movement direction so that it passes
   * through the first wall.
   *
   * @param projectile the projectile entity to push forward.
   * @param vel the VelocityComponent of the projectile.
   */
  protected void moveThroughWall(Entity projectile, VelocityComponent vel) {
    projectile
        .fetch(PositionComponent.class)
        .ifPresent(
            pos -> {
              Vector2 velocity = vel.currentVelocity();

              float length =
                  (float) Math.sqrt(velocity.x() * velocity.x() + velocity.y() * velocity.y());
              if (length > 0) {
                Vector2 push =
                    Vector2.of(
                        (velocity.x() / length) * pushOffset, (velocity.y() / length) * pushOffset);

                pos.position(pos.position().translate(push));
              }
            });
  }
}
