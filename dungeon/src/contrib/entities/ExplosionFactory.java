package contrib.entities;

import contrib.systems.EventScheduler;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.components.HealthComponent;
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

  private ExplosionFactory() {}

  public static Entity createExplosion(
      IPath textureDir, Point position, float radius, DamageType dmgType, int dmgAmount) {

    if (position == null) return null;

    AnimationConfig cfg = new AnimationConfig();
    cfg.framesPerSprite(FRAMES_PER_SPRITE);

    Animation anim = new Animation(buildFrames(textureDir), cfg);

    Entity fx = new Entity("explosion");
    fx.add(new PositionComponent(position));
    fx.add(new DrawComponent(anim));
    Game.add(fx);

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
              var hpOpt = e.fetch(HealthComponent.class);
              if (posOpt.isEmpty() || hpOpt.isEmpty()) return;

              Point ep = posOpt.get().position();
              if (Point.calculateDistance(center, ep) <= radius) {
                hpOpt.get().receiveHit(new Damage(amount, type, source));
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
}
