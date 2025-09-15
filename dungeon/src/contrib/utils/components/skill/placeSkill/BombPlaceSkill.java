package contrib.utils.components.skill.placeSkill;

import core.Entity;
import core.utils.Point;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class BombPlaceSkill {

  private static final IPath BOMB_TEXTURE = new SimpleIPath("");
  private static final IPath EXPLOSION_TEXTURE = new SimpleIPath("");
  private static final int DAMAGE = 8;
  private static final long FUSE_MS = 1800L;
  private static final float RADIUS = 3.0f;

  private final IPath bombTexture;
  private final IPath explosionTexture;
  private final float radius;
  private final int damage;
  private final long fuseMs;

  public BombPlaceSkill() {
    this(BOMB_TEXTURE, EXPLOSION_TEXTURE, RADIUS, DAMAGE, FUSE_MS);
  }

  public BombPlaceSkill(
      IPath bombTexture, IPath explosionTexture, float radius, int damage, long fuseMs) {
    this.bombTexture = bombTexture;
    this.explosionTexture = explosionTexture;
    this.radius = radius;
    this.damage = damage;
    this.fuseMs = fuseMs;
  }

  public void cast(Entity caster) {}

  private void explode(Entity bomb) {}

  private void applyAoE(Point center, float radius, int damage, Entity source) {}
}
