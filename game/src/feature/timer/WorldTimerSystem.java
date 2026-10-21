package feature.timer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import engine.Entity;
import engine.System;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.rendering.EmissiveSprites;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This system is responsible for rendering a timer image to a texture, which will be fed into the
 * DrawComponent of entities with a WorldTimerComponent.
 *
 * <p>The timer shows as a red LED display in a 3x5 pixel font. Each pixel of its texture covers one
 * art pixel of the world, so the digits stay sharp at every zoom.
 */
public class WorldTimerSystem extends System {

  // Half a tile high: eight art pixels.
  private static final int HEIGHT = 8;
  private static final int PADDING_X = 1;
  private static final int PADDING_Y = 1;
  private static final int DIGIT_WIDTH = 3;
  private static final int DIGIT_HEIGHT = 5;
  private static final int GAP = 1;
  private static final Color BACKGROUND = Color.BLACK;
  private static final Color LIT = new Color(1f, 0.16f, 0.1f, 1f);
  // Pixels that are off still show faintly, like on a real LED display.
  private static final Color UNLIT = new Color(0.22f, 0.03f, 0.02f, 1f);
  // The digits 0 to 9, one bit per pixel, row by row; the highest bit is the top left pixel.
  private static final int[] DIGITS = {
    0b111_101_101_101_111,
    0b010_110_010_010_111,
    0b111_001_111_100_111,
    0b111_001_111_001_111,
    0b101_101_111_001_001,
    0b111_100_111_001_111,
    0b111_100_111_101_111,
    0b111_001_001_001_001,
    0b111_101_111_101_111,
    0b111_101_111_001_111
  };
  private static final int PIXELS = DIGIT_WIDTH * DIGIT_HEIGHT;

  /** Callback fired once locally when the timer reaches zero, or {@code null} if unset. */
  private Runnable onTimerExpired;

  // The text each timer shows, so it is only redrawn when it changes.
  private final Map<Entity, String> shown = new HashMap<>();

  /** Whether the local expiry callback has already been fired. */
  private boolean expiredFired = false;

  /** Create a new WorldTimerSystem. */
  public WorldTimerSystem() {
    super(AuthoritativeSide.CLIENT, 5, WorldTimerComponent.class, PositionComponent.class);
    onEntityRemove =
        e -> {
          shown.remove(e);
          EmissiveSprites.forget(texture(e));
        };
  }

  /**
   * Registers a callback that is invoked once, locally, at the moment the timer reaches zero (i.e.
   * the remaining time becomes less than or equal to zero).
   *
   * <p>This system runs locally on each client, so the callback fires independently on each client
   * for its own view of the timer.
   *
   * @param callback the callback to run when the timer expires
   * @return this system, for fluent chaining
   */
  public WorldTimerSystem onTimerExpired(Runnable callback) {
    this.onTimerExpired = callback;
    return this;
  }

  @Override
  public void execute() {
    filteredEntityStream().map(Data::of).forEach(this::update);
  }

  private void update(Data data) {
    int secondsLeft = (int) ((data.tc.remainingMs() + 999) / 1000);

    if (secondsLeft <= 0 && !expiredFired) {
      expiredFired = true;
      if (onTimerExpired != null) {
        onTimerExpired.run();
      }
    }

    int displaySeconds = Math.max(0, secondsLeft);
    // Root locale: the display only has the digits 0 to 9.
    String timerString =
        String.format(Locale.ROOT, "%02d:%02d", displaySeconds / 60, displaySeconds % 60);
    // The display changes once a second; rebuilding it costs a texture and a glow mask.
    if (timerString.equals(shown.put(data.e, timerString))) return;
    Pixmap pixmap = display(timerString);
    IPath ipath = texture(data.e);
    // The display glows by itself and lights the wall and floor in front of it.
    EmissiveSprites.glow(ipath, pixmap);
    TextureMap.instance().putPixmap(ipath, pixmap, false);
    TextureMap.instance()
        .textureAt(ipath)
        .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    data.e.remove(DrawComponent.class);
    data.e.add(new DrawComponent(new Animation(ipath, new AnimationConfig().scaleX(0.5f))));
  }

  private static IPath texture(Entity e) {
    return new SimpleIPath("@gen/worldTimer/" + e.name() + ".png");
  }

  private static Pixmap display(String text) {
    int width = PADDING_X * 2 - GAP;
    for (char c : text.toCharArray()) width += (c == ':' ? 1 : DIGIT_WIDTH) + GAP;
    Pixmap pixmap = new Pixmap(width, HEIGHT, Pixmap.Format.RGBA8888);
    pixmap.setColor(BACKGROUND);
    pixmap.fill();
    int x = PADDING_X;
    for (char c : text.toCharArray()) {
      if (c == ':') {
        pixmap.setColor(LIT);
        pixmap.drawPixel(x, PADDING_Y + 1);
        pixmap.drawPixel(x, PADDING_Y + DIGIT_HEIGHT - 2);
        x += 1 + GAP;
      } else {
        drawDigit(pixmap, DIGITS[c - '0'], x);
        x += DIGIT_WIDTH + GAP;
      }
    }
    return pixmap;
  }

  private static void drawDigit(Pixmap pixmap, int digit, int x) {
    // The pixels of an 8 that the digit leaves off glow faintly.
    int display = DIGITS[8] | digit;
    for (int i = 0; i < PIXELS; i++) {
      int bit = 1 << (PIXELS - 1 - i);
      if ((display & bit) == 0) continue;
      pixmap.setColor((digit & bit) != 0 ? LIT : UNLIT);
      pixmap.drawPixel(x + i % DIGIT_WIDTH, PADDING_Y + i / DIGIT_WIDTH);
    }
  }

  @Override
  public void stop() {
    // Cant be stopped
  }

  private record Data(Entity e, WorldTimerComponent tc, PositionComponent pc) {
    private static Data of(Entity e) {
      return new Data(
          e,
          e.fetch(WorldTimerComponent.class).orElseThrow(),
          e.fetch(PositionComponent.class).orElseThrow());
    }
  }
}
