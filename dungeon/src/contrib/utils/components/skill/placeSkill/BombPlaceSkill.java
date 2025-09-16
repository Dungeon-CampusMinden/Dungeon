package contrib.utils.components.skill.placeSkill;

import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.List;

public class BombPlaceSkill extends Skill {

  public static final String SKILL_NAME = "BOMB_PLACE";

  public static final String BOMB_TEXTURE_DIR = "skills/bomb/";

  private static final List<IPath> DEFAULT_BOMB_FRAMES = List.of(
      new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_00.png"),
      new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_10.png"),
      new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_20.png"),
      new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_30.png")
  );

  private static final AnimationConfig DEFAULT_BOMB_ANIM_CFG = new AnimationConfig();
  static {
    DEFAULT_BOMB_ANIM_CFG.framesPerSprite(2);
  }
  
  private static final IPath DEFAULT_EXPLOSION_TEXTURE = new SimpleIPath("");
  private static final int DEFAULT_DAMAGE = 8;
  private static final long DEFAULT_FUSE_MS = 1800L;
  private static final float DEFAULT_RADIUS = 3.0f;
  private static final long DEFAULT_COOLDOWN = 800L;

  private final IPath explosionTexture;
  private final float radius;
  private final int damage;
  private final long fuseMs;

  public BombPlaceSkill() {
    this(
        DEFAULT_EXPLOSION_TEXTURE,
        DEFAULT_RADIUS,
        DEFAULT_DAMAGE,
        DEFAULT_FUSE_MS,
        DEFAULT_COOLDOWN);
  }

  public BombPlaceSkill(
      IPath explosionTexture,
      float radius,
      int damage,
      long fuseMs,
      long cooldownMs) {
    super(SKILL_NAME, cooldownMs);
    this.explosionTexture = explosionTexture;
    this.radius = radius;
    this.damage = damage;
    this.fuseMs = fuseMs;
  }

  @SafeVarargs
  public BombPlaceSkill(
      IPath explosionTexture,
      float radius,
      int damage,
      long fuseMs,
      long cooldownMs,
      Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, cooldownMs, resourceCost);
    this.explosionTexture = explosionTexture;
    this.radius = radius;
    this.damage = damage;
    this.fuseMs = fuseMs;
  }

  @Override
  protected void executeSkill(Entity caster) {
    dropBomb(caster);
  }

  private void dropBomb(Entity caster) {
    Point dropPos =
        caster
            .fetch(PositionComponent.class)
            .map(PositionComponent::position)
            .map(p -> Game.tileAt(p).map(t -> t.coordinate().toCenteredPoint()).orElse(p))
            .orElse(null);
    if (dropPos == null) return;

    Entity bomb = new Entity("bomb_placed");
    bomb.add(new PositionComponent(dropPos));

    bomb.add(new DrawComponent(new Animation(DEFAULT_BOMB_FRAMES, DEFAULT_BOMB_ANIM_CFG)));

    Game.add(bomb);
  }

  private void explode(Entity bomb, Entity source) {}

  private void applyAoE(Point center, float radius, int damage, Entity source) {}
}
