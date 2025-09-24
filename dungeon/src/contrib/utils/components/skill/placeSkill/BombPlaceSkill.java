package contrib.utils.components.skill.placeSkill;

import contrib.components.BombElementComponent;
import contrib.components.BombElementComponent.BombElement;
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
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class BombPlaceSkill extends Skill {

  public static final String SKILL_NAME = "BOMB_PLACE";
  public static final String STATE_NAME = "blink";

  private static final IPath BOMB_SPRITESHEET = new SimpleIPath("skills/bomb");
  private static final IPath EXPLOSION_DIR = new SimpleIPath("skills/bomb/explosion");
  private static final float DEFAULT_RADIUS = 2.0f;
  private static final int DEFAULT_DAMAGE = 8;

  private static final long DEFAULT_FUSE_MS = 10_000L;
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
    Point heroPos =
        caster.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
    if (heroPos == null) return;

    Point spawnPos = heroPos;
    var dcOpt = caster.fetch(DrawComponent.class);
    if (dcOpt.isPresent()) {
      var anim = dcOpt.get().currentAnimation();
      boolean heroCentered = anim.getConfig().centered();
      float heroW = anim.getWidth();
      float heroH = anim.getHeight();
      if (!heroCentered) {
        spawnPos = new Point(heroPos.x() + heroW / 2f, heroPos.y() + heroH / 2f);
      } else {
        spawnPos = heroPos;
      }
    }

    Entity bomb = new Entity("bomb_placed");
    bomb.add(new PositionComponent(spawnPos));
    DrawComponent bombDC = new DrawComponent(BOMB_SPRITESHEET, STATE_NAME);
    bomb.add(bombDC);

    BombElement element = BombElementComponent.getOrDefault(caster);
    bomb.add(new BombElementComponent(element));

    Game.add(bomb);

    AnimationConfig cfg = bombDC.currentAnimation().getConfig();
    cfg.centered(true);
    scheduleBlinkRamp(cfg);
    scheduleExplosion(bomb);
  }

  private void scheduleExplosion(Entity bomb) {
    EventScheduler.scheduleAction(
        () -> {
          Point pos =
              bomb.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);

          BombElement element = BombElementComponent.getOrDefault(bomb);
          Game.remove(bomb);

          if (pos != null) {
            DamageType dmgType = element.toDamageType();
            System.out.println("BombPlaceSkill -> Explosion DamageType=" + dmgType);
            ExplosionFactory.createExplosion(
                EXPLOSION_DIR, pos, DEFAULT_RADIUS, dmgType, DEFAULT_DAMAGE);
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
