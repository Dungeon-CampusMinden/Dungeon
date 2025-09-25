package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.BombElementComponent.BombElement;
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
  private static final long SFX_IDLE_DISPOSE_MS = 5000L;

  private ExplosionFactory() {}

  public static Entity createExplosion(
      IPath textureDir, Point position, float radius, DamageType dmgType, int dmgAmount) {

    if (position == null) return null;

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

    return fx;
  }

  private static AnimationConfig loadAnimationConfig(IPath textureDir, String state) {
    String base = textureDir.pathString();
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
    String jsonPath = base + "/" + JSON_NAME;
    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);
    if (configs != null) {
      AnimationConfig cfg = configs.get(state);
      if (cfg != null) return cfg;
      cfg = configs.get("explosion");
      if (cfg != null) return cfg;
    }
    return new AnimationConfig();
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

  private static void playExplosionSfx(BombElement element) {
    Sound sfx = Gdx.audio.newSound(Gdx.files.internal(element.sfxPath()));
    sfx.play(0.2f);
    EventScheduler.scheduleAction(() -> disposeIfIdle(sfx), SFX_IDLE_DISPOSE_MS);
  }

  private static void disposeIfIdle(Sound s) {
    if (s != null) s.dispose();
  }
}
