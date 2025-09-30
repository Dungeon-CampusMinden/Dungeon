package contrib.utils.components.skill.projectileSkill;

import contrib.components.BombElementComponent;
import contrib.components.BombElementComponent.BombElement;
import contrib.components.CollideComponent;
import contrib.entities.ExplosionFactory;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Tuple;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

/**
 * A projectile-based skill that throws a bomb towards the cursor position and explodes on impact or
 * when the maximum range is reached.
 *
 * <p>The projectile inherits its {@link BombElement} from the caster, which defines the resulting
 * explosion's {@link DamageType} and visuals/audio. If the projectile collides with a solid entity
 * or reaches its end position, it detonates at its center.
 *
 * @see ProjectileSkill
 * @see ExplosionFactory
 * @see BombElementComponent
 */
public class BombThrowingSkill extends ProjectileSkill {

  public static final String SKILL_NAME = "BOMB_THROW";

  private static final long DEFAULT_COOLDOWN = 800L;
  private static final IPath DEFAULT_BOMB_TEXTURE = new SimpleIPath("skills/bomb");
  private static final float DEFAULT_SPEED = 8f;
  private static final float DEFAULT_RANGE = 6f;
  private static final IPath DEFAULT_EXPLOSION_DIR = new SimpleIPath("skills/bomb/explosion");
  private static final float DEFAULT_RADIUS = 2.0f;
  private static final int DEFAULT_DAMAGE = 8;
  private static final boolean IGNORE_FIRST_WALL = false;

  private final IPath explosionTextureDir;
  private final float explosionRadius;
  private final int damageAmount;

  /** Creates a throwing bomb skill with default range and cooldown. */
  public BombThrowingSkill() {
    this(DEFAULT_RANGE, DEFAULT_COOLDOWN);
  }

  /**
   * Creates a throwing bomb skill with custom range and cooldown.
   *
   * @param range Maximum throwing range of the projectile in world units.
   * @param cooldownMs Cooldown in milliseconds between consecutive uses.
   */
  public BombThrowingSkill(float range, long cooldownMs) {
    super(
        SKILL_NAME,
        cooldownMs,
        DEFAULT_BOMB_TEXTURE,
        DEFAULT_SPEED,
        range,
        DEFAULT_HITBOX_SIZE,
        DEFAULT_HITBOX_OFFSET,
        IGNORE_FIRST_WALL);
    this.explosionTextureDir = DEFAULT_EXPLOSION_DIR;
    this.explosionRadius = DEFAULT_RADIUS;
    this.damageAmount = DEFAULT_DAMAGE;
  }

  /**
   * Creates a throwing bomb skill with custom range, cooldown and resource costs.
   *
   * @param range Maximum throwing range of the projectile in world units.
   * @param cooldownMs Cooldown in milliseconds between consecutive uses.
   * @param resourceCost Optional resource cost tuples required to cast this skill.
   */
  @SafeVarargs
  public BombThrowingSkill(float range, long cooldownMs, Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldownMs,
        DEFAULT_BOMB_TEXTURE,
        DEFAULT_SPEED,
        range,
        DEFAULT_HITBOX_SIZE,
        DEFAULT_HITBOX_OFFSET,
        IGNORE_FIRST_WALL,
        resourceCost);
    this.explosionTextureDir = DEFAULT_EXPLOSION_DIR;
    this.explosionRadius = DEFAULT_RADIUS;
    this.damageAmount = DEFAULT_DAMAGE;
  }

  /**
   * Determines the projectile's end position based on the current cursor location.
   *
   * @param caster The entity casting the skill.
   * @return The target point where the projectile aims to travel to.
   */
  @Override
  protected Point end(Entity caster) {
    Point cursor = SkillTools.cursorPositionAsPoint();
    Point s = start(caster);
    if (cursor.equals(s)) return new Point(s.x() + 1, s.y());
    return cursor;
  }

  /**
   * Called when the projectile collides with a wall or solid tile before reaching its end.
   *
   * <p>Triggers an explosion at the projectile's center and removes the projectile entity.
   *
   * @param caster The casting entity.
   * @param projectile The projectile entity that hit the wall.
   */
  @Override
  protected void onWallHit(Entity caster, Entity projectile) {
    explodeAtProjectileCenter(projectile);
    Game.remove(projectile);
  }

  /**
   * Callback invoked when the projectile has reached its planned end position.
   *
   * <p>Detonates the projectile at its center and removes it from the game world.
   *
   * @param caster The casting entity.
   * @return A consumer applied to the projectile upon arrival.
   */
  @Override
  protected Consumer<Entity> onEndReached(Entity caster) {
    return p -> {
      explodeAtProjectileCenter(p);
      Game.remove(p);
    };
  }

  /**
   * Computes the projectile's center and spawns an explosion at this position using the
   * projectile's {@link BombElement} to determine the {@link DamageType}.
   *
   * @param projectile The projectile entity to explode.
   */
  private void explodeAtProjectileCenter(Entity projectile) {
    var posOpt = projectile.fetch(PositionComponent.class).map(PositionComponent::position);
    if (posOpt.isEmpty()) return;
    Point pos = posOpt.get();
    Point center = new Point(pos.x() + hitBoxSize().x() / 2f, pos.y() + hitBoxSize().y() / 2f);
    BombElement element = BombElementComponent.getElementOrDefault(projectile);
    DamageType dmgType = element.toDamageType();
    ExplosionFactory.createExplosion(
        explosionTextureDir, center, explosionRadius, dmgType, damageAmount);
  }

  /**
   * Called when the projectile starts colliding with another entity.
   *
   * <p>If the other entity is solid and not in the ignore list, the projectile detonates and is
   * removed.
   *
   * @param caster The casting entity.
   * @return A tri-consumer handling projectile vs. other entity collision events.
   */
  @Override
  protected TriConsumer<Entity, Entity, Direction> onCollideEnter(Entity caster) {
    return (projectile, other, dir) -> {
      if (ignoreEntities.contains(other)) return;
      boolean isSolid =
          other.fetch(CollideComponent.class).map(CollideComponent::isSolid).orElse(false);
      if (isSolid) {
        explodeAtProjectileCenter(projectile);
        Game.remove(projectile);
      }
    };
  }

  /**
   * Initializes the projectile's appearance and element on spawn.
   *
   * <p>Replaces any existing {@link DrawComponent} on the projectile with the bomb sprite (state
   * {@code "static_first"}). The projectile inherits the caster's {@link BombElement}.
   *
   * @param caster The casting entity.
   * @param projectile The projectile that has just been spawned.
   */
  @Override
  protected void onSpawn(Entity caster, Entity projectile) {
    projectile
        .fetch(DrawComponent.class)
        .ifPresent(
            old -> {
              int depth = old.depth();
              int tint = old.tintColor();
              boolean vis = old.isVisible();
              DrawComponent dc = new DrawComponent(DEFAULT_BOMB_TEXTURE, "static_first");
              dc.depth(depth);
              dc.tintColor(tint);
              dc.setVisible(vis);
              projectile.add(dc);
            });

    BombElement element = BombElementComponent.getElementOrDefault(caster);
    projectile.add(new BombElementComponent(element));
  }
}
