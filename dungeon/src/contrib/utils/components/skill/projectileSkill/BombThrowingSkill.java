package contrib.utils.components.skill.projectileSkill;

import contrib.components.CollideComponent;
import contrib.entities.ExplosionFactory;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.function.Consumer;

public class BombThrowingSkill extends ProjectileSkill {

  public static final String SKILL_NAME = "BOMB_THROW";

  private static final long DEFAULT_COOLDOWN = 800L;
  private static final IPath DEFAULT_BOMB_TEXTURE = new SimpleIPath("skills/bomb/bomb_01.png");
  private static final float DEFAULT_SPEED = 8f;
  private static final float DEFAULT_RANGE = 6f;
  private static final Vector2 DEFAULT_HITBOX = Vector2.ONE;
  private static final IPath DEFAULT_EXPLOSION_DIR = new SimpleIPath("skills/bomb/explosion");
  private static final float DEFAULT_RADIUS = 2.2f;
  private static final int DEFAULT_DAMAGE = 8;
  private static final DamageType DEFAULT_DMG_TYPE = DamageType.FIRE;

  private final IPath explosionTextureDir;
  private final float explosionRadius;
  private final DamageType damageType;
  private final int damageAmount;

  public BombThrowingSkill() {
    this(DEFAULT_RANGE, DEFAULT_COOLDOWN);
  }

  public BombThrowingSkill(float range, long cooldownMs) {
    super(SKILL_NAME, cooldownMs, DEFAULT_BOMB_TEXTURE, DEFAULT_SPEED, range, DEFAULT_HITBOX);
    this.explosionTextureDir = DEFAULT_EXPLOSION_DIR;
    this.explosionRadius = DEFAULT_RADIUS;
    this.damageType = DEFAULT_DMG_TYPE;
    this.damageAmount = DEFAULT_DAMAGE;
  }

  @SafeVarargs
  public BombThrowingSkill(float range, long cooldownMs, Tuple<Resource, Integer>... resourceCost) {
    super(
        SKILL_NAME,
        cooldownMs,
        DEFAULT_BOMB_TEXTURE,
        DEFAULT_SPEED,
        range,
        DEFAULT_HITBOX,
        resourceCost);
    this.explosionTextureDir = DEFAULT_EXPLOSION_DIR;
    this.explosionRadius = DEFAULT_RADIUS;
    this.damageType = DEFAULT_DMG_TYPE;
    this.damageAmount = DEFAULT_DAMAGE;
  }

  @Override
  protected Point end(Entity caster) {
    Point cursor = SkillTools.cursorPositionAsPoint();
    Point s = start(caster);
    if (cursor == null || cursor.equals(s)) return new Point(s.x() + 1, s.y());
    return cursor;
  }

  @Override
  protected Consumer<Entity> onWallHit(Entity caster) {
    return p -> {
      explodeAtProjectileCenter(p);
      Game.remove(p);
    };
  }

  @Override
  protected Consumer<Entity> onEndReached(Entity caster) {
    return p -> {
      explodeAtProjectileCenter(p);
      Game.remove(p);
    };
  }

  private void explodeAtProjectileCenter(Entity projectile) {
    Point pos =
        projectile.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
    if (pos == null) return;
    Point center = new Point(pos.x() + hitBoxSize().x() / 2f, pos.y() + hitBoxSize().y() / 2f);
    ExplosionFactory.createExplosion(
        explosionTextureDir, center, explosionRadius, damageType, damageAmount);
  }

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
}
