package contrib.utils.components.skill.placeSkill;

import contrib.entities.ExplosionFactory;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.DamageType;
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

  private static final List<IPath> BOMB_FRAMES_BLINK =
      List.of(
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_01.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_01_red.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_02.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_02_red.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_03.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_03_red.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_04.png"),
          new SimpleIPath(BOMB_TEXTURE_DIR + "bomb_04_red.png"));

  private static final AnimationConfig DEFAULT_BOMB_ANIM_CFG = new AnimationConfig();

  static {
    DEFAULT_BOMB_ANIM_CFG.framesPerSprite(6);
  }

  private static final IPath DEFAULT_EXPLOSION_DIR = new SimpleIPath("skills/bomb/explosion");
  private static final float DEFAULT_RADIUS = 2.0f;
  private static final int DEFAULT_DAMAGE = 8;
  private static final DamageType DEFAULT_DMG_TYPE = DamageType.FIRE;

  private static final long DEFAULT_FUSE_MS = 10000L;
  private static final long DEFAULT_COOLDOWN = 800L;

  private final long fuseMs;

  public BombPlaceSkill() {
    this(DEFAULT_FUSE_MS, DEFAULT_COOLDOWN);
  }

  public BombPlaceSkill(long fuseMs, long cooldownMs) {
    super(SKILL_NAME, cooldownMs);
    this.fuseMs = fuseMs;
  }

  @SafeVarargs
  public BombPlaceSkill(long fuseMs, long cooldownMs, Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, cooldownMs, resourceCost);
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

    AnimationConfig cfg = new AnimationConfig();
    cfg.framesPerSprite(6);
    cfg.centered(true);

    Entity bomb = new Entity("bomb_placed");
    bomb.add(new PositionComponent(dropPos));
    bomb.add(new DrawComponent(new Animation(BOMB_FRAMES_BLINK, cfg)));
    Game.add(bomb);
    scheduleBlinkRamp(cfg);
    explode(bomb);
  }

  private void explode(Entity bomb) {
    EventScheduler.scheduleAction(
        () -> {
          Point pos =
              bomb.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
          Game.remove(bomb);
          if (pos != null) {
            ExplosionFactory.createExplosion(
                DEFAULT_EXPLOSION_DIR, pos, DEFAULT_RADIUS, DEFAULT_DMG_TYPE, DEFAULT_DAMAGE);
          }
        },
        fuseMs);
  }

  private void scheduleBlinkRamp(AnimationConfig cfg) {
    long t1 = Math.round(fuseMs * 0.10);
    long t2 = Math.round(fuseMs * 0.30);
    long t3 = Math.round(fuseMs * 0.50);
    long t4 = Math.round(fuseMs * 0.70);
    long t5 = Math.round(fuseMs * 0.90);

    EventScheduler.scheduleAction(() -> cfg.framesPerSprite(5), t1);
    EventScheduler.scheduleAction(() -> cfg.framesPerSprite(4), t2);
    EventScheduler.scheduleAction(() -> cfg.framesPerSprite(3), t3);
    EventScheduler.scheduleAction(() -> cfg.framesPerSprite(2), t4);
    EventScheduler.scheduleAction(() -> cfg.framesPerSprite(1), t5);
  }
}
