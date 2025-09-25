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

/**
 * A skill that places a bomb at the caster's position which explodes after a fuse time.
 *
 * <p>The placed bomb blinks faster towards the end of the fuse to provide visual feedback. On
 * explosion, an {@link contrib.entities.ExplosionFactory}-based effect is spawned using the element
 * configured on the caster (via {@link BombElementComponent}) to determine damage type and
 * visuals/audio.
 *
 * <p>The bomb entity itself is added to the game world immediately, and removed when it explodes.
 *
 * @see Skill
 * @see ExplosionFactory
 * @see BombElementComponent
 */
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

  /** Creates a bomb placing skill with default fuse and cooldown. */
  public BombPlaceSkill() {
    this(DEFAULT_FUSE_MS, DEFAULT_COOLDOWN);
  }

  /**
   * Creates a bomb placing skill with a custom fuse and cooldown.
   *
   * @param fuseMs Fuse duration in milliseconds before the bomb explodes.
   * @param cooldownMs Cooldown in milliseconds between consecutive uses.
   */
  public BombPlaceSkill(long fuseMs, long cooldownMs) {
    super(SKILL_NAME, cooldownMs);
    this.fuseMs = fuseMs;
  }

  /**
   * Creates a bomb placing skill with custom fuse, cooldown and resource costs.
   *
   * @param fuseMs Fuse duration in milliseconds before the bomb explodes.
   * @param cooldownMs Cooldown in milliseconds between consecutive uses.
   * @param resourceCost Optional resource cost tuples required to cast this skill.
   */
  @SafeVarargs
  public BombPlaceSkill(long fuseMs, long cooldownMs, Tuple<Resource, Integer>... resourceCost) {
    super(SKILL_NAME, cooldownMs, resourceCost);
    this.fuseMs = fuseMs;
  }

  /**
   * Executes the skill for the given caster by placing a bomb entity at the caster's position.
   *
   * @param caster The entity invoking the skill.
   */
  @Override
  protected void executeSkill(Entity caster) {
    dropBomb(caster);
  }

  /**
   * Spawns a bomb entity at the caster's position, starts its blinking animation, and schedules its
   * explosion.
   *
   * <p>The placed bomb inherits the {@link BombElement} from the caster (or defaults), which later
   * determines the explosion visuals and damage type.
   *
   * @param caster The entity placing the bomb.
   * @see #scheduleBlinkRamp(AnimationConfig)
   * @see #scheduleExplosion(Entity)
   */
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

    BombElement element = BombElementComponent.getElementOrDefault(caster);
    bomb.add(new BombElementComponent(element));

    Game.add(bomb);

    AnimationConfig cfg = bombDC.currentAnimation().getConfig();
    cfg.centered(true);
    scheduleBlinkRamp(cfg);
    scheduleExplosion(bomb);
  }

  /**
   * Schedules the bomb's explosion after the configured fuse time.
   *
   * <p>On detonation, the bomb entity is removed and an explosion is created at the last known bomb
   * position. The {@link BombElement} on the bomb decides which {@link DamageType} is applied.
   *
   * @param bomb The bomb entity to explode.
   */
  private void scheduleExplosion(Entity bomb) {
    EventScheduler.scheduleAction(
        () -> {
          Point pos =
              bomb.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);

          BombElement element = BombElementComponent.getElementOrDefault(bomb);
          Game.remove(bomb);

          if (pos != null) {
            DamageType dmgType = element.toDamageType();
            ExplosionFactory.createExplosion(
                EXPLOSION_DIR, pos, DEFAULT_RADIUS, dmgType, DEFAULT_DAMAGE);
          }
        },
        fuseMs);
  }

  /**
   * Ramps the blink animation by decreasing frames-per-sprite over time, creating an increasing
   * blink rate as the fuse approaches zero.
   *
   * <p>Five milestones at 10%, 30%, 50%, 70% and 90% of the fuse adjust {@code framesPerSprite}
   * down from 5 to 1.
   *
   * @param cfg The animation configuration of the bomb sprite to modify.
   */
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
