package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.CollideComponent;
import contrib.components.ExplosableComponent;
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
import java.util.Map;

public final class ExplosionFactory {

  private static final int FRAME_COUNT = 16;
  private static final String JSON_NAME = "explosion.json";
  private static final long SFX_IDLE_DISPOSE_MS = 30_000L;

  private static final SimpleIPath EXPLOSION_SFX = new SimpleIPath("sounds/bomb_explosion.wav");

  private static Sound explosionSound;
  private static long lastPlayAtMs = 0L;

  private ExplosionFactory() {}

  public static Entity createExplosion(
      IPath textureDir, Point position, float radius, DamageType dmgType, int dmgAmount) {

    if (position == null) return null;

    float diameter = radius * 2f;

    AnimationConfig cfg = loadAnimationConfig(textureDir, diameter);
    Animation anim = new Animation(new SimpleIPath(textureDir.pathString()), cfg);

    Entity fx = new Entity("explosion");
    fx.add(new PositionComponent(position));
    fx.add(new DrawComponent(anim));
    fx.add(buildCollider(fx, position, radius, dmgType, dmgAmount));

    Game.add(fx);
    playExplosionSfx();

    long lifetimeMs = calculateLifetime(cfg);
    EventScheduler.scheduleAction(() -> Game.remove(fx), lifetimeMs);

    return fx;
  }

  private static AnimationConfig loadAnimationConfig(IPath textureDir, float diameter) {
    String base = textureDir.pathString();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }

    String jsonPath = base + "/" + JSON_NAME;
    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);

    AnimationConfig cfg;
    if (configs != null && configs.containsKey("explode")) {
      cfg = configs.get("explode");
    } else {
      cfg = new AnimationConfig();
    }

    int fpsPerSprite = Math.max(1, cfg.framesPerSprite());

    cfg.framesPerSprite(fpsPerSprite);
    cfg.scaleX(diameter);
    cfg.scaleY(diameter);
    cfg.centered(true);

    return cfg;
  }

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
                  .fetch(ExplosableComponent.class)
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

  private static long calculateLifetime(AnimationConfig cfg) {
    int fps = Math.max(1, Game.frameRate());
    int totalFramesShown = FRAME_COUNT * cfg.framesPerSprite();
    return Math.round(totalFramesShown * (1000.0 / fps));
  }

  private static void playExplosionSfx() {
    if (explosionSound == null) {
      explosionSound = Gdx.audio.newSound(Gdx.files.internal(EXPLOSION_SFX.pathString()));
    }
    explosionSound.play(0.2f);
    lastPlayAtMs = System.currentTimeMillis();
    long stamp = lastPlayAtMs;
    EventScheduler.scheduleAction(() -> disposeIfIdle(stamp), SFX_IDLE_DISPOSE_MS);
  }

  private static void disposeIfIdle(long stamp) {
    if (explosionSound == null) return;
    if (lastPlayAtMs == stamp) {
      explosionSound.dispose();
      explosionSound = null;
    }
  }
}
