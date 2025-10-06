package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.BombElementComponent.BombElement;
import contrib.components.CollideComponent;
import contrib.components.ExplodableComponent;
import contrib.components.HealthComponent;
import contrib.systems.EventScheduler;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;

/**
 * Factory for creating explosion effects with visuals, collision damage and sound playback.
 *
 * <p>The created entity is immediately added to the game and automatically removed once the
 * animation has finished.
 *
 * @see BombElement
 * @see DamageType
 * @see ExplodableComponent
 * @see HealthComponent
 */
public final class ExplosionFactory {

  private static final int FRAME_COUNT = 16;
  private static final String JSON_NAME = "explosion.json";
  private static final long SFX_IDLE_DISPOSE_MS = 5000L;

  private ExplosionFactory() {}

  /**
   * Creates and spawns an explosion entity at the given position with the specified radius and
   * damage parameters.
   *
   * @param textureDir Base directory containing explosion textures and the {@code explosion.json}
   *     config.
   * @param positionOpt World position of the explosion center.
   * @param radius Explosion radius in world units; also used to scale the sprite.
   * @param dmgType Damage type for art/SFX selection).
   * @param dmgAmount Amount of damage dealt to hit entities.
   * @return The created explosion entity, or {@link Optional#empty()} if position is missing.
   */
  public static Optional<Entity> createExplosion(
      IPath textureDir,
      Optional<Point> positionOpt,
      float radius,
      DamageType dmgType,
      int dmgAmount) {

    if (positionOpt.isEmpty()) return Optional.empty();
    Point position = positionOpt.get();

    float diameter = radius * 2f;

    String baseDir = textureDir.pathString();
    if (baseDir.endsWith("/")) baseDir = baseDir.substring(0, baseDir.length() - 1);

    BombElement element = BombElement.fromDamageType(dmgType);
    String state = element.spriteName();

    String spritePath = baseDir + "/" + state + ".png";

    AnimationConfig cfg = loadAnimationConfig(textureDir, state);
    cfg.framesPerSprite(Math.max(1, cfg.framesPerSprite()));
    cfg.scaleX(diameter);
    cfg.scaleY(diameter);
    cfg.centered(true);

    Animation anim = new Animation(new SimpleIPath(spritePath), cfg);
    DrawComponent dc = new DrawComponent(anim);

    Entity fx = new Entity("explosion");
    fx.add(new PositionComponent(position));
    fx.add(dc);
    fx.add(buildCollider(fx, position, radius, dmgType, dmgAmount));

    Game.add(fx);
    playExplosionSfx(element);

    long lifetimeMs = calculateLifetime(cfg);
    EventScheduler.scheduleAction(() -> Game.remove(fx), lifetimeMs);

    return Optional.of(fx);
  }

  /**
   * Loads an {@link AnimationConfig} for a specific explosion state from {@code explosion.json}.
   *
   * @param textureDir Directory containing the JSON file.
   * @param state The animation state key.
   * @return The matching {@link AnimationConfig}, a fallback config, or a default config.
   */
  private static AnimationConfig loadAnimationConfig(IPath textureDir, String state) {
    String base = textureDir.pathString();
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
    String jsonPath = base + "/" + JSON_NAME;

    var cfgsOpt = Optional.ofNullable(AnimationConfig.loadAnimationConfigMap(jsonPath));
    if (cfgsOpt.isEmpty()) return new AnimationConfig();

    var cfgs = cfgsOpt.get();
    var primaryOpt = Optional.ofNullable(cfgs.get(state));
    if (primaryOpt.isEmpty()) {
      var fallbackOpt = Optional.ofNullable(cfgs.get("explosion"));
      if (fallbackOpt.isEmpty()) return new AnimationConfig();
      return fallbackOpt.get();
    }
    return primaryOpt.get();
  }

  /**
   * Builds a non-solid collider that deals damage and notifies {@link ExplodableComponent}s.
   *
   * @param fx The explosion entity.
   * @param pos The explosion center.
   * @param radius The explosion radius.
   * @param dmgType The damage type to apply.
   * @param dmgAmount The amount of damage dealt.
   * @return The configured {@link CollideComponent}.
   */
  private static CollideComponent buildCollider(
      Entity fx, Point pos, float radius, DamageType dmgType, int dmgAmount) {
    float diameter = radius * 2f;
    CollideComponent cc =
        new CollideComponent(
            Vector2.of(-radius, -radius),
            Vector2.of(diameter, diameter),
            (self, other, dir) -> {
              other
                  .fetch(HealthComponent.class)
                  .ifPresent(hp -> hp.receiveHit(new Damage(dmgAmount, dmgType, fx)));
              other
                  .fetch(ExplodableComponent.class)
                  .ifPresent(
                      expl ->
                          expl.onExplosionHit()
                              .onExplosionHit(other, pos, radius, dmgType, dmgAmount, fx));
            },
            CollideComponent.DEFAULT_COLLIDER);
    cc.onHold(CollideComponent.DEFAULT_COLLIDER);
    cc.isSolid(false);
    return cc;
  }

  /**
   * Calculates the explosion lifetime in milliseconds based on frame rate and animation pacing.
   *
   * @param cfg The animation configuration of the explosion.
   * @return Lifetime in milliseconds after which the entity is removed.
   */
  private static long calculateLifetime(AnimationConfig cfg) {
    int fps = Math.max(1, Game.frameRate());
    int totalFramesShown = FRAME_COUNT * cfg.framesPerSprite();
    return Math.round(totalFramesShown * (1000.0 / fps));
  }

  /**
   * Creates and plays the element-specific explosion sound and schedules its disposal.
   *
   * @param element The element determining which SFX to play.
   */
  private static void playExplosionSfx(BombElement element) {
    Sound sfx = Gdx.audio.newSound(Gdx.files.internal(element.sfxPath()));
    sfx.play(0.2f);
    EventScheduler.scheduleAction(sfx::dispose, SFX_IDLE_DISPOSE_MS);
  }
}
