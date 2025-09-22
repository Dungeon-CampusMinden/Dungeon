package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Map;

public final class ExplosionFactory {

  private static final int FRAME_COUNT = 16;
  private static final int FRAMES_PER_SPRITE = 1;

  private static final String JSON_NAME = "explosion.json";

  private static final float EXPLOSION_SCALE = 0.75f;

  private static final long SFX_IDLE_DISPOSE_MS = 30_000L;
  private static long lastPlayAtMs = 0L;

  private static final SimpleIPath EXPLOSION_SFX = new SimpleIPath("sounds/bomb_explosion.wav");

  private static Sound explosionSound;

  private ExplosionFactory() {}

  public static Entity createExplosion(
      IPath textureDir, Point position, float radius, DamageType dmgType, int dmgAmount) {

    if (position == null) return null;

    float diameter = radius * 2f;

    String base = textureDir.pathString();
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

    String jsonPath = base + "/" + JSON_NAME;
    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);
    AnimationConfig cfg;
    if (configs != null && configs.containsKey("explode")) {
      cfg = configs.get("explode");
    } else {
      cfg = new AnimationConfig();
    }

    int fps = Game.frameRate();
    if (fps < 1) fps = 1;

    int fpsPerSprite = cfg.framesPerSprite();
    if (fpsPerSprite < 1) fpsPerSprite = 1;

    cfg.framesPerSprite(fpsPerSprite);
    cfg.scaleX(diameter * EXPLOSION_SCALE);
    cfg.scaleY(0);
    cfg.centered(true);

    IPath sheetPath = new SimpleIPath(base);
    Animation anim = new Animation(sheetPath, cfg);

    Entity fx = new Entity("explosion");
    fx.add(new PositionComponent(position));
    fx.add(new DrawComponent(anim));
    Game.add(fx);

    playExplosionSfx();
    applyAoeDamage(position, radius, dmgType, dmgAmount, fx);

    int totalFramesShown = FRAME_COUNT * fpsPerSprite;
    long lifetimeMs = Math.round(totalFramesShown * (1000.0 / fps));
    EventScheduler.scheduleAction(() -> Game.remove(fx), lifetimeMs);

    return fx;
  }

  private static void applyAoeDamage(
      Point center, float radius, DamageType type, int amount, Entity source) {

    Game.levelEntities()
        .forEach(
            e -> {
              var posOpt = e.fetch(PositionComponent.class);
              if (posOpt.isEmpty()) return;

              Point ep = posOpt.get().position();
              if (Point.calculateDistance(center, ep) <= radius) {
                e.fetch(HealthComponent.class)
                    .ifPresent(hp -> hp.receiveHit(new Damage(amount, type, source)));
                e.fetch(ExplosableComponent.class)
                    .ifPresent(
                        expl ->
                            expl.onExplosionHit()
                                .onExplosionHit(e, center, radius, type, amount, source));
              }
            });
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
