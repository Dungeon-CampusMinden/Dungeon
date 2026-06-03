package contrib.modules.worldTimer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import core.Entity;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.FontHelper;
import core.utils.FontSpec;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

/**
 * This system is responsible for rendering a timer image to a texture, which will be fed into the
 * DrawComponent of entities with a WorldTimerComponent.
 */
public class WorldTimerSystem extends System {

  private static int HEIGHT = 64;
  private static int PADDING_X = 15;
  private static int PADDING_Y = 5;
  private static FontSpec TIMER_FONT =
      new FontSpec("fonts/Doto_Rounded-ExtraBold.ttf", HEIGHT, Color.RED, 0, Color.WHITE);
  private static BitmapFont FONT;
  private static SpriteBatch BATCH = new SpriteBatch();

  private int currentUnixTime;

  /** Callback fired once locally when the timer reaches zero, or {@code null} if unset. */
  private Runnable onTimerExpired;

  /** Whether the local expiry callback has already been fired. */
  private boolean expiredFired = false;

  /** Create a new WorldTimerSystem. */
  public WorldTimerSystem() {
    super(AuthoritativeSide.CLIENT, 5, WorldTimerComponent.class, PositionComponent.class);
    FONT = FontHelper.getFont(TIMER_FONT);
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
    currentUnixTime = (int) (java.lang.System.currentTimeMillis() / 1000L);
    filteredEntityStream().map(Data::of).forEach(this::update);
  }

  private void update(Data data) {
    int secondsSinceStart = currentUnixTime - data.tc.timestamp();
    int secondsLeft = data.tc.duration() - secondsSinceStart;

    if (secondsLeft <= 0 && !expiredFired) {
      expiredFired = true;
      if (onTimerExpired != null) {
        onTimerExpired.run();
      }
    }

    int displaySeconds = Math.max(0, secondsLeft);
    String timerString =
        String.format("%02d:%02d", displaySeconds / 60, displaySeconds % 60);
    GlyphLayout layout = new GlyphLayout(FONT, timerString);

    int fboWidth = (int) layout.width + PADDING_X * 2;
    int fboHeight = HEIGHT + PADDING_Y * 2;
    FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);

    fbo.begin();

    Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    BATCH.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, fboWidth, fboHeight));
    BATCH.begin();
    FONT.draw(BATCH, timerString, PADDING_X, HEIGHT - PADDING_Y);
    BATCH.end();

    Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, fbo.getWidth(), fbo.getHeight());
    fbo.end();
    fbo.dispose();

    String path = "@gen/worldTimer/" + data.e.name() + ".png";
    IPath ipath = new SimpleIPath(path);
    TextureMap.instance().putPixmap(ipath, pixmap, true);
    TextureMap.instance()
        .textureAt(ipath)
        .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

    data.e.remove(DrawComponent.class);
    data.e.add(new DrawComponent(new Animation(ipath, new AnimationConfig().scaleX(0.5f))));
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
