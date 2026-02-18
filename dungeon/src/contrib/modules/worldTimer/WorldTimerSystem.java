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

public class WorldTimerSystem extends System {

  private static int HEIGHT = 64;
  private static int PADDING_X = 15;
  private static int PADDING_Y = 5;
  private static FontSpec TIMER_FONT = new FontSpec("fonts/Doto_Rounded-ExtraBold.ttf", HEIGHT, Color.RED, 0, Color.WHITE);
  private static BitmapFont FONT;
  private static SpriteBatch BATCH = new SpriteBatch();

  int frameCount = 0;

  public WorldTimerSystem() {
    super(AuthoritativeSide.CLIENT, 17, WorldTimerComponent.class, PositionComponent.class);
    FONT = FontHelper.getFont(TIMER_FONT);
  }

  @Override
  public void execute() {
    frameCount += 17;
    filteredEntityStream().map(Data::of).forEach(this::update);
  }

  private void update(Data data) {
    int seconds = 59 - (frameCount / 60);
    seconds = Math.max(seconds, 0);

    // Create timer sprite programmatically
    String timerString = "14:"+seconds;
    GlyphLayout layout = new GlyphLayout(FONT, timerString);

    int fboWidth = (int)layout.width + PADDING_X *2;
    int fboHeight = HEIGHT + PADDING_Y *2;
    FrameBuffer fbo = new FrameBuffer(
      Pixmap.Format.RGBA8888,
      fboWidth,
      fboHeight,
      false
    );

    fbo.begin();

    Gdx.gl.glClearColor(0f, 0f, 0f, 1.0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


    BATCH.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, fboWidth, fboHeight));
    BATCH.begin();
    FONT.draw(BATCH, timerString, PADDING_X, HEIGHT - PADDING_Y);
    BATCH.end();

    Pixmap pixmap = Pixmap.createFromFrameBuffer(
      0,
      0,
      fbo.getWidth(),
      fbo.getHeight()
    );
    fbo.end();

    String path = "@gen/worldTimer/"+data.e.name()+".png";
    IPath ipath = new SimpleIPath(path);
    TextureMap.instance().putPixmap(ipath, pixmap, true);
    TextureMap.instance().textureAt(ipath).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

    data.e.remove(DrawComponent.class);
    data.e.add(new DrawComponent(new Animation(ipath, new AnimationConfig().scaleX(0.5f))));
  }

  @Override
  public void stop() {
    // Cant be stopped
  }

  private record Data(Entity e, WorldTimerComponent tc, PositionComponent pc) {
    private static Data of(Entity e) {
      return new Data(e,
        e.fetch(WorldTimerComponent.class).orElseThrow(),
        e.fetch(PositionComponent.class).orElseThrow()
      );
    }
  }
}
