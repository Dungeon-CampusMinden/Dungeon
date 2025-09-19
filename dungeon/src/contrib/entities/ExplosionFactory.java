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
import java.util.ArrayList;
import java.util.List;

public final class ExplosionFactory {

  private static final String FRAME_PREFIX = "explosion_";
  private static final int FRAME_COUNT = 16;
  private static final int FRAMES_PER_SPRITE = 1;

  private static final long SFX_IDLE_DISPOSE_MS = 30_000L;
  private static long lastPlayAtMs = 0L;

  private static final SimpleIPath EXPLOSION_SFX = new SimpleIPath("sounds/bomb_explosion.wav");

  private static Sound explosionSound;

  private ExplosionFactory() {}

  public static Entity createExplosion(
      IPath textureDir, Point position, float radius, DamageType dmgType, int dmgAmount) {

    if (position == null) return null;

    float diameter = radius * 2f;

    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(FRAMES_PER_SPRITE).scaleX(diameter).centered(true);

    Animation anim = new Animation(buildFrames(textureDir), cfg);

    Entity fx = new Entity("explosion");
    fx.add(new PositionComponent(position));
    fx.add(new DrawComponent(anim));
    Game.add(fx);

    playExplosionSfx();

    applyAoeDamage(position, radius, dmgType, dmgAmount, fx);

    int fps = Game.frameRate();
    if (fps < 1) fps = 1;
    int totalFramesShown = FRAME_COUNT * FRAMES_PER_SPRITE;
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

  private static List<IPath> buildFrames(IPath textureDir) {
    String dir = textureDir.pathString();
    if (dir.endsWith("/")) dir = dir.substring(0, dir.length() - 1);

    List<IPath> frames = new ArrayList<>(FRAME_COUNT);

    for (int i = 1; i <= FRAME_COUNT; i++) {
      String num;
      if (i < 10) {
        num = "0" + i;
      } else {
        num = String.valueOf(i);
      }

      String filename = dir + "/" + FRAME_PREFIX + num + ".png";
      frames.add(new SimpleIPath(filename));
    }
    return frames;
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
