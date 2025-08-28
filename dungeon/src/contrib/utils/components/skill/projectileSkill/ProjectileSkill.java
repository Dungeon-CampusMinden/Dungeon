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
import core.utils.*;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ProjectileSkill extends Skill {

  public static final Consumer<Entity> REMOVE_CONSUMER = projectile -> Game.remove(projectile);
  public static final TriConsumer<Entity, Entity, Direction> NOOP_TRICONSUMER =
      (entity1, entity2, direction) -> {};
  public static final Vector2 DEFAULT_HITBOX_SIZE = Vector2.of(1, 1);
  public static final Function<Entity, Consumer<Entity>> NOOP_FUNCTION = entity -> entity1 -> {};
  public static final Consumer<Entity> NOOP_CONSUMER = entity -> {};

  protected IPath texture;
  protected Supplier<Point> start;
  protected Supplier<Point> end;
  protected float speed;
  protected float range;
  protected Vector2 hitBoxSize;
  protected int tintColor = -1;

  protected List<Entity> ignoreEntities;

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

  @Override
  protected void executeSkill(Entity caster) {
    Entity projectile = new Entity(name() + "_projectile");
    ignoreEntities.add(caster);
    ignoreEntities.add(projectile);

    Point start = start(caster);
    projectile.add(new FlyComponent());
    projectile.add(new PositionComponent(start));

    try {
      DrawComponent dc = new DrawComponent(texture);
      dc.tintColor(tintColor);
      projectile.add(dc);
    } catch (IOException e) {
      Skill.LOGGER.warning(
          String.format("The DrawComponent for the projectile %s cant be created. ")
              + e.getMessage());
      throw new RuntimeException();
    }

    // Get the target point based on the selection function and projectile range.
    // Use a copy, so you do not change the actual value (for example the hero position)
    Point aimedOn = new Point(end(caster));
    Point targetPoint = SkillTools.calculateLastPositionInRange(start, aimedOn, range);

    // Calculate the velocity of the projectile
    Vector2 forceToApply = SkillTools.calculateDirection(start, targetPoint).scale(speed);

    // Add the VelocityComponent to the projectile
    projectile.add(new VelocityComponent(speed, onWallHit(caster), true));

    // Add the ProjectileComponent with the initial and target positions to the projectile
    projectile.add(new ProjectileComponent(start, targetPoint, forceToApply, onEndReached(caster)));

    // Add the CollideComponent with the appropriate hit box size and collision handler to the
    // projectile
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

  protected TriConsumer<Entity, Entity, Direction> onCollideEnter(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  protected TriConsumer<Entity, Entity, Direction> onCollideHold(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  protected TriConsumer<Entity, Entity, Direction> onCollideLeave(Entity caster) {
    return NOOP_TRICONSUMER;
  }

  protected Consumer<Entity> onWallHit(Entity caster) {
    return REMOVE_CONSUMER;
  }

  protected Consumer<Entity> onEndReached(Entity caster) {
    return REMOVE_CONSUMER;
  }

  protected void onSpawn(Entity caster, Entity projectile) {
    return;
  }

  protected Point start(Entity caster) {
    return caster
        .fetch(CollideComponent.class)
        .map(collideComponent -> collideComponent.center(caster))
        .orElse(new Point(0, 0));
  }
  ;

  protected abstract Point end(Entity caster);

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
