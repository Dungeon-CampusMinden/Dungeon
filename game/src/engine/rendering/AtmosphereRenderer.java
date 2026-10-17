package engine.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.utils.LevelElement;
import engine.systems.CameraSystem;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.BlendUtils;
import engine.utils.components.draw.FrameBufferPool;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The frame-level part of the render pipeline in {@link engine.systems.DrawSystem}.
 *
 * <p>A frame runs in this order:
 *
 * <ol>
 *   <li>{@link #beginFrame}: the camera is snapped to the texel grid and widened by a margin, so
 *       the scene renders into buffers with two texels per art pixel (32 per tile). Sprite pixels
 *       stay uniform at that integer ratio, while detail finer than an art pixel (text, dot-matrix
 *       displays) survives. The level's {@link Atmosphere} and lights are collected.
 *   <li>The draw system renders level and entities into those buffers; this class contributes
 *       {@link #drawFloor the floor}, {@link #drawWalls walls}, {@link #drawShadow contact
 *       shadows}, {@link #drawGroundParticles kicked-up dust}, {@link #addEntity entity lights} and
 *       {@link #drawParticles particles}.
 *   <li>{@link #grade}: ambient and point lights with wall shadows, fog, grading and bloom. The
 *       draw system then adds world overlays and runs the scene shaders on the lit scene.
 *   <li>{@link #present}: the scene is scaled to the screen with sharp bilinear filtering, vignette
 *       and grain.
 *   <li>{@link #endFrame}: the camera is restored and the frame's buffers are freed.
 * </ol>
 */
public final class AtmosphereRenderer implements Disposable {

  private static final float TEXELS_PER_TILE = 32f;
  // Buffer sizes snap to this many texels, so zooming reuses pooled buffers of a few sizes.
  private static final int SIZE_STEP = 32;
  private static final float MAX_LIGHT_RADIUS = 8f;
  // Walking speeds in tiles per second; standing still, teleports and thrown objects raise no dust.
  private static final float MIN_WALK_SPEED = 0.8f;
  private static final float MAX_WALK_SPEED = 12f;
  private static final String SHADER_DIR = "shaders/atmosphere/";
  // Base assumed for frames without a readable image: most of the box, at its bottom edge.
  private static final SpriteFootprints.Footprint BOX_BASE =
      new SpriteFootprints.Footprint(0.1f, 0.9f, 0f);

  private final LevelRenderer level = new LevelRenderer();
  private final AmbientParticles particles = new AmbientParticles();
  private final FrameBufferPool pool = FrameBufferPool.getInstance();
  private final Matrix4 pixelProjection = new Matrix4();
  private final List<Light> lights = new ArrayList<>();
  private final List<Point> flames = new ArrayList<>();
  private final List<Point> trails = new ArrayList<>();
  private final List<FrameBuffer> frameBuffers = new ArrayList<>();
  private Map<Entity, Point> lastPositions = new HashMap<>();
  private Map<Entity, Point> positions = new HashMap<>();

  private ShaderProgram lightShader;
  private ShaderProgram compositeShader;
  private ShaderProgram brightShader;
  private ShaderProgram blurShader;
  private ShaderProgram presentShader;

  private Pixmap occlusion;
  private Texture occlusionTexture;
  private int occlusionX;
  private int occlusionY;

  private float savedX;
  private float savedY;
  private float savedViewportWidth;
  private float savedViewportHeight;

  private static final Tile[][] NO_LAYOUT = new Tile[0][0];

  private Tile[][] layout = NO_LAYOUT;
  private Atmosphere atmosphere = Atmosphere.NEUTRAL;
  private float delta;
  private float time;
  private int width;
  private int height;
  private int screenWidth;
  private int screenHeight;
  private Rectangle view = new Rectangle(0, 0, 0, 0);
  private float srcU;
  private float srcV;
  private float srcU2;
  private float srcV2;
  private FrameBuffer bloomNear;
  private FrameBuffer bloomFar;

  /**
   * Starts a frame: switches the camera to the pixel-aligned scene view and collects the atmosphere
   * and lights of the current level. Must be paired with {@link #endFrame}.
   *
   * @param screenWidth screen width in pixels
   * @param screenHeight screen height in pixels
   * @param delta seconds since the last frame
   * @param time seconds since start
   */
  public void beginFrame(int screenWidth, int screenHeight, float delta, float time) {
    Optional<ILevel> current = Game.currentLevel();
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    layout = current.map(ILevel::layout).orElse(NO_LAYOUT);
    atmosphere = current.map(ILevel::atmosphere).orElse(Atmosphere.NEUTRAL);
    if (LevelEditorSystem.active()) {
      // The level editor shows the level fully lit and ungraded.
      atmosphere =
          new Atmosphere(Color.WHITE, Color.WHITE, Color.WHITE, 0f, 0f, 0f, atmosphere.wallDecor());
    }
    this.delta = delta;
    this.time = time;
    Map<Entity, Point> swap = lastPositions;
    lastPositions = positions;
    positions = swap;
    positions.clear();
    OrthographicCamera cam = CameraSystem.camera();
    savedX = cam.position.x;
    savedY = cam.position.y;
    savedViewportWidth = cam.viewportWidth;
    savedViewportHeight = cam.viewportHeight;

    float viewWidth = cam.viewportWidth * cam.zoom;
    float viewHeight = cam.viewportHeight * cam.zoom;
    // Two texels per art pixel, but never more texels than screen pixels.
    float texelsPerUnit = Math.min(TEXELS_PER_TILE, screenWidth / viewWidth);
    width = roundUp(MathUtils.ceil(viewWidth * texelsPerUnit) + 4, SIZE_STEP);
    height = roundUp(MathUtils.ceil(viewHeight * texelsPerUnit) + 4, SIZE_STEP);

    // Snapped to the texels of the half-resolution light buffer, so its texels stay on the
    // art-pixel grid the light shader samples; present shifts the rest for smooth scrolling.
    float snap = 2f / texelsPerUnit;
    float snappedX = Math.round(savedX / snap) * snap;
    float snappedY = Math.round(savedY / snap) * snap;
    float left = snappedX - width / (2f * texelsPerUnit);
    float bottom = snappedY - height / (2f * texelsPerUnit);
    view = new Rectangle(width / texelsPerUnit, height / texelsPerUnit, left, bottom);

    srcU = (savedX - viewWidth / 2f - left) * texelsPerUnit / width;
    srcV = (savedY - viewHeight / 2f - bottom) * texelsPerUnit / height;
    srcU2 = srcU + viewWidth * texelsPerUnit / width;
    srcV2 = srcV + viewHeight * texelsPerUnit / height;

    cam.position.set(snappedX, snappedY, 0);
    cam.viewportWidth = width / texelsPerUnit / cam.zoom;
    cam.viewportHeight = height / texelsPerUnit / cam.zoom;
    cam.update();

    lights.clear();
    flames.clear();
    trails.clear();
    if (layout.length > 0 && layout[0].length > 0 && atmosphere.wallDecor()) {
      int[] b = tileBounds(layout, MAX_LIGHT_RADIUS);
      level.collectLights(layout, b[0], b[1], b[2], b[3], lights);
      level.collectFlames(layout, b[0], b[1], b[2], b[3], flames);
    }
    current.ifPresent(l -> l.lights(lights));
  }

  /**
   * Width of the scene buffers of the current frame, in texels.
   *
   * @return the width
   */
  public int width() {
    return width;
  }

  /**
   * Height of the scene buffers of the current frame, in texels.
   *
   * @return the height
   */
  public int height() {
    return height;
  }

  /**
   * Draws the floor pass of the level inside the scene view. Contact shadows and kicked-up dust
   * follow, then {@link #drawWalls}.
   *
   * @param batch batch with the world projection, already begun
   */
  public void drawFloor(SpriteBatch batch) {
    if (layout.length == 0 || layout[0].length == 0) return;
    int[] b = tileBounds(layout, 1);
    level.drawFloor(batch, layout, b[0], b[1], b[2], b[3]);
  }

  /**
   * Draws the wall pass of the level inside the scene view, covering the shadows that reach under
   * walls.
   *
   * @param batch batch with the world projection, already begun
   * @param time seconds since start
   */
  public void drawWalls(SpriteBatch batch, float time) {
    if (layout.length == 0 || layout[0].length == 0) return;
    int[] b = tileBounds(layout, 1);
    level.drawWalls(batch, layout, b[0], b[1], b[2], b[3], time, atmosphere.wallDecor());
  }

  /**
   * Draws the soft contact shadow below an entity, under the visible base of its current frame. The
   * shadow only darkens what is already drawn: over the void it stays invisible. Call {@link
   * #drawGroundParticles} after the last shadow; it restores the blending.
   *
   * @param batch batch with the world projection, already begun
   * @param dc the entity's draw component
   * @param pc the entity's position component
   */
  public void drawShadow(SpriteBatch batch, DrawComponent dc, PositionComponent pc) {
    // The shadow fades with the entity's tint, so invisible entities cast none.
    float alpha = (dc.tintColor() & 0xFF) / 255f;
    if (alpha == 0) return;
    float spriteWidth = dc.getWidth() * Math.abs(pc.scale().x());
    float spriteHeight = dc.getHeight() * Math.abs(pc.scale().y());
    SpriteFootprints.Footprint base = SpriteFootprints.of(dc.getSprite()).orElse(BOX_BASE);
    float left = base.left();
    float right = base.right();
    if (dc.currentAnimation().getConfig().mirrored()) {
      left = 1 - base.right();
      right = 1 - base.left();
    }
    // A little wider than the base; wide furniture gets a flat ellipse, not a tall blob.
    float w = MathUtils.clamp((right - left) * spriteWidth + 4 / 16f, 0.4f, 3.5f);
    float h = Math.min(w * 10f / 24f, 0.45f);
    float centerX = pc.position().x() + (left + right) / 2f * spriteWidth;
    float feetY = pc.position().y() + base.bottom() * spriteHeight;
    batch.setColor(alpha, alpha, alpha, alpha);
    batch.setBlendFunctionSeparate(
        GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ZERO, GL20.GL_ONE);
    // Mostly below the base row, so the sprite hides only its upper part.
    batch.draw(RenderTextures.blobShadow(), centerX - w / 2f, feetY - h * 0.55f, w, h);
  }

  /**
   * Draws the dust kicked up by walking feet onto the floor, after the contact shadows and before
   * the entities, so feet and bodies cover it. It shows the particles as of the last {@link
   * #drawParticles} call.
   *
   * @param batch batch with the world projection, already begun
   */
  public void drawGroundParticles(SpriteBatch batch) {
    BlendUtils.setBlending(batch);
    particles.drawPuffs(batch);
    batch.setColor(Color.WHITE);
  }

  /**
   * Registers an entity of the frame: collects the light it emits (see {@link SceneLights}) and, if
   * it walks, kicks up dust at its feet.
   *
   * @param entity the entity
   * @param dc its draw component
   * @param pc its position component
   * @param grounded whether the entity stands on the floor (gets a contact shadow)
   */
  public void addEntity(Entity entity, DrawComponent dc, PositionComponent pc, boolean grounded) {
    Rectangle v = view;
    Point p = pc.position();
    if (p.x() < v.x() - MAX_LIGHT_RADIUS
        || p.y() < v.y() - MAX_LIGHT_RADIUS
        || p.x() > v.x() + v.width() + MAX_LIGHT_RADIUS
        || p.y() > v.y() + v.height() + MAX_LIGHT_RADIUS) {
      return;
    }
    boolean glows =
        SceneLights.collect(entity, dc, pc, atmosphere.lantern(), lights, flames, trails);

    positions.put(entity, p);
    Point last = lastPositions.get(entity);
    if (!grounded
        || glows
        || last == null
        || delta <= 0
        || !dc.isVisible()
        || (dc.tintColor() & 0xFF) == 0) {
      return;
    }
    float speed = (float) last.distance(p) / delta;
    if (speed > MIN_WALK_SPEED && speed < MAX_WALK_SPEED) {
      float w = dc.getWidth() * Math.abs(pc.scale().x());
      particles.step(p.translate(w / 2f, 1 / 16f), delta);
    }
  }

  /**
   * Advances the particles and draws the airborne ones into the composed scene, after all entities
   * are registered and before the lighting and scene shaders: dust motes over the scene, flame
   * glow, embers, spark trails and magic motes added on top. Running before the scene shaders lets
   * their masks hide particles too.
   *
   * @param batch batch with the world projection, already begun
   */
  public void drawParticles(SpriteBatch batch) {
    particles.update(delta, time, view, flames, trails, lights);
    particles.drawDust(batch, layout, time);
    // Additive light that keeps the scene's coverage alpha, which the fog reads.
    batch.setBlendFunctionSeparate(GL20.GL_ONE, GL20.GL_ONE, GL20.GL_ZERO, GL20.GL_ONE);
    particles.drawEmbers(batch, flames, time);
    particles.drawSparks(batch, trails, time);
    particles.drawMotes(batch, time);
    batch.setColor(Color.WHITE);
    BlendUtils.setBlending(batch);
  }

  /**
   * Lights, fogs and grades the composed scene and adds the bloom of the light that exceeds full
   * brightness.
   *
   * @param scene the composed scene, {@link #width()} x {@link #height()}
   * @param batch the batch to render with, not begun
   * @return the lit scene, premultiplied; owned by this renderer until {@link #endFrame}
   */
  public FrameBuffer grade(FrameBuffer scene, SpriteBatch batch) {
    ensureShaders();
    // Light is smooth, so half resolution is enough.
    FrameBuffer light = obtain(width / 2, height / 2);
    linear(light);
    renderLights(light, batch);
    renderBloom(scene, light, batch);

    FrameBuffer graded = obtain(width, height);
    linear(scene);
    graded.begin();
    batch.setShader(compositeShader);
    batch.setProjectionMatrix(pixelProjection.setToOrtho2D(0, 0, width, height));
    batch.disableBlending();
    batch.begin();
    light.getColorBufferTexture().bind(1);
    bloomNear.getColorBufferTexture().bind(2);
    bloomFar.getColorBufferTexture().bind(3);
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    Atmosphere a = atmosphere;
    compositeShader.setUniformi("u_light", 1);
    compositeShader.setUniformi("u_bloomNear", 2);
    compositeShader.setUniformi("u_bloomFar", 3);
    setColor(compositeShader, "u_ambient", a.ambient());
    setColor(compositeShader, "u_shadowTint", a.shadowTint());
    setColor(compositeShader, "u_lightTint", a.lightTint());
    Rectangle v = view;
    compositeShader.setUniformf("u_view", v.x(), v.y(), v.width(), v.height());
    compositeShader.setUniformf("u_time", time);
    compositeShader.setUniformf("u_fog", a.fog());
    batch.setColor(Color.WHITE);
    batch.draw(scene.getColorBufferTexture(), 0, 0, width, height, 0, 0, 1, 1);
    batch.end();
    batch.setShader(null);
    batch.enableBlending();
    graded.end();
    return graded;
  }

  /**
   * Presents the lit scene on the screen.
   *
   * @param graded the result of {@link #grade}, or of the scene shaders run on it
   * @param batch the batch to render with, not begun
   */
  public void present(FrameBuffer graded, SpriteBatch batch) {
    linear(graded);
    batch.setShader(presentShader);
    batch.setProjectionMatrix(pixelProjection.setToOrtho2D(0, 0, screenWidth, screenHeight));
    batch.disableBlending();
    batch.begin();
    presentShader.setUniformf("u_texSize", width, height);
    presentShader.setUniformf(
        "u_scale",
        screenWidth / ((srcU2 - srcU) * width),
        screenHeight / ((srcV2 - srcV) * height));
    presentShader.setUniformf("u_screen", screenWidth, screenHeight);
    presentShader.setUniformf("u_time", time);
    presentShader.setUniformf("u_vignette", atmosphere.vignette());
    batch.setColor(Color.WHITE);
    batch.draw(
        graded.getColorBufferTexture(), 0, 0, screenWidth, screenHeight, srcU, srcV, srcU2, srcV2);
    batch.end();
    batch.setShader(null);
    batch.enableBlending();
    BlendUtils.setBlending(batch);
  }

  /**
   * Ends a frame started by {@link #beginFrame}: restores the camera and frees the buffers of the
   * frame. Must run even if rendering the frame failed.
   */
  public void endFrame() {
    OrthographicCamera cam = CameraSystem.camera();
    cam.position.set(savedX, savedY, 0);
    cam.viewportWidth = savedViewportWidth;
    cam.viewportHeight = savedViewportHeight;
    cam.update();

    for (FrameBuffer fbo : frameBuffers) {
      fbo.getColorBufferTexture()
          .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
      pool.free(fbo);
    }
    frameBuffers.clear();
  }

  private void renderLights(FrameBuffer target, SpriteBatch batch) {
    updateOcclusion();
    target.begin();
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (!lights.isEmpty() && occlusionTexture != null) {
      batch.setShader(lightShader);
      batch.setProjectionMatrix(CameraSystem.camera().combined);
      batch.enableBlending();
      batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
      batch.begin();
      occlusionTexture.bind(1);
      Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
      lightShader.setUniformi("u_occlusion", 1);
      lightShader.setUniformf(
          "u_occRect", occlusionX, occlusionY, occlusion.getWidth(), occlusion.getHeight());
      Rectangle v = view;
      float scale = lightScale(atmosphere.ambient());
      for (Light l : lights) {
        // A slow brightness wobble at a fixed reach: a changing radius would shift every
        // falloff step of the art-pixel light and make the whole pool shimmer.
        float intensity =
            scale * l.intensity() * (1f + l.flicker() * Light.wobble(l.position(), time));
        float r = l.radius();
        Point p = l.position();
        if (p.x() + r < v.x()
            || p.y() + r < v.y()
            || p.x() - r > v.x() + v.width()
            || p.y() - r > v.y() + v.height()) {
          continue;
        }
        lightShader.setUniformf("u_lightPos", p.x(), p.y());
        lightShader.setUniformf("u_radius", r);
        lightShader.setUniformf(
            "u_color", l.color().r * intensity, l.color().g * intensity, l.color().b * intensity);
        batch.draw(RenderTextures.pixel(), p.x() - r, p.y() - r, 2 * r, 2 * r);
        batch.flush();
      }
      batch.end();
      batch.setShader(null);
      BlendUtils.setBlending(batch);
    }
    target.end();
  }

  private static float lightScale(Color ambient) {
    // In bright rooms point lights add a glow instead of washing the scene out: above half
    // brightness, their strength falls to 30 % at full ambient light.
    float luma = 0.299f * ambient.r + 0.587f * ambient.g + 0.114f * ambient.b;
    return 1f - 0.7f * MathUtils.clamp((luma - 0.5f) / 0.5f, 0f, 1f);
  }

  /**
   * Rebuilds the occlusion map (one texel per tile) around the view: red marks tiles that block
   * light; green, blue and alpha hold how much floor shows left of, right of and below the art of
   * an autotiled wall, in sixteenths of a tile, so the light shader lights that floor as floor. On
   * other tiles green marks open ground, as opposed to the void.
   */
  private void updateOcclusion() {
    if (layout.length == 0 || layout[0].length == 0) return;
    int[] b = tileBounds(layout, MAX_LIGHT_RADIUS + 1);
    int w = b[1] - b[0] + 1;
    int h = b[3] - b[2] + 1;
    if (w <= 0 || h <= 0) return;
    if (occlusion == null || occlusion.getWidth() != w || occlusion.getHeight() != h) {
      if (occlusion != null) occlusion.dispose();
      if (occlusionTexture != null) occlusionTexture.dispose();
      occlusion = new Pixmap(w, h, Pixmap.Format.RGBA8888);
      occlusion.setBlending(Pixmap.Blending.None);
      occlusionTexture = new Texture(occlusion);
      occlusionTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }
    occlusionX = b[0];
    occlusionY = b[2];
    for (int y = 0; y < h; y++) {
      Tile[] row = layout[b[2] + y];
      for (int x = 0; x < w; x++) {
        Tile t = row[b[0] + x];
        // Light passes over the void, so holes in a room cast no shadow.
        boolean blocks =
            t == null
                || t.levelElement() == LevelElement.WALL
                || (t.levelElement() == LevelElement.DOOR && !t.canSeeThrough());
        boolean ground = !blocks && t.levelElement() != LevelElement.SKIP;
        int texel = blocks ? 0xff000000 : ground ? 0x00ff0000 : 0;
        if (blocks && t != null && LevelRenderer.isAutoWall(t)) {
          DesignTileset tileset = DesignTileset.of(t.designLabel());
          int cell = LevelRenderer.wallCell(layout, b[0] + x, b[2] + y);
          texel |=
              sixteenths(tileset.leftInset(cell)) << 16
                  | sixteenths(tileset.rightInset(cell)) << 8
                  | sixteenths(tileset.bottomInset(cell));
        }
        occlusion.drawPixel(x, y, texel);
      }
    }
    occlusionTexture.draw(occlusion, 0, 0);
  }

  private static int sixteenths(float tiles) {
    // An inset as an occlusion map channel value: sixteenths of a tile, times 16.
    return Math.round(tiles * 16) * 16;
  }

  private void renderBloom(FrameBuffer scene, FrameBuffer light, SpriteBatch batch) {
    int hw = Math.max(1, width / 2);
    int hh = Math.max(1, height / 2);
    int qw = Math.max(1, width / 4);
    int qh = Math.max(1, height / 4);
    bloomNear = obtain(hw, hh);
    FrameBuffer nearTmp = obtain(hw, hh);
    bloomFar = obtain(qw, qh);
    FrameBuffer farTmp = obtain(qw, qh);

    pass(
        batch,
        brightShader,
        scene,
        bloomNear,
        s -> {
          light.getColorBufferTexture().bind(1);
          Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
          s.setUniformi("u_light", 1);
          setColor(s, "u_ambient", atmosphere.ambient());
        });
    pass(batch, blurShader, bloomNear, nearTmp, s -> s.setUniformf("u_dir", 1f / hw, 0));
    pass(batch, blurShader, nearTmp, bloomNear, s -> s.setUniformf("u_dir", 0, 1f / hh));
    pass(batch, null, bloomNear, bloomFar, s -> {});
    pass(batch, blurShader, bloomFar, farTmp, s -> s.setUniformf("u_dir", 1f / qw, 0));
    pass(batch, blurShader, farTmp, bloomFar, s -> s.setUniformf("u_dir", 0, 1f / qh));
  }

  private void pass(
      SpriteBatch batch,
      ShaderProgram shader,
      FrameBuffer source,
      FrameBuffer target,
      Consumer<ShaderProgram> uniforms) {
    linear(source);
    target.begin();
    batch.setShader(shader);
    batch.setProjectionMatrix(
        pixelProjection.setToOrtho2D(0, 0, target.getWidth(), target.getHeight()));
    batch.disableBlending();
    batch.begin();
    if (shader != null) uniforms.accept(shader);
    batch.setColor(Color.WHITE);
    batch.draw(
        source.getColorBufferTexture(), 0, 0, target.getWidth(), target.getHeight(), 0, 0, 1, 1);
    batch.end();
    batch.setShader(null);
    batch.enableBlending();
    target.end();
  }

  private FrameBuffer obtain(int w, int h) {
    // Freed at endFrame.
    FrameBuffer fbo = pool.obtain(w, h);
    frameBuffers.add(fbo);
    return fbo;
  }

  private static void setColor(ShaderProgram shader, String name, Color color) {
    shader.setUniformf(name, color.r, color.g, color.b);
  }

  private static int roundUp(int value, int step) {
    return (value + step - 1) / step * step;
  }

  private static void linear(FrameBuffer fbo) {
    fbo.getColorBufferTexture()
        .setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
  }

  private int[] tileBounds(Tile[][] layout, float margin) {
    // minX, maxX, minY, maxY of the view grown by the margin, clamped to the layout.
    Rectangle v = view;
    return new int[] {
      Math.max(0, MathUtils.floor(v.x() - margin)),
      Math.min(layout[0].length - 1, MathUtils.ceil(v.x() + v.width() + margin)),
      Math.max(0, MathUtils.floor(v.y() - margin)),
      Math.min(layout.length - 1, MathUtils.ceil(v.y() + v.height() + margin))
    };
  }

  private void ensureShaders() {
    if (lightShader != null) return;
    String vert = Gdx.files.internal(SHADER_DIR + "atmosphere.vert").readString();
    lightShader = compile(vert, "light.frag");
    compositeShader = compile(vert, "composite.frag");
    brightShader = compile(vert, "bright.frag");
    blurShader = compile(vert, "blur.frag");
    presentShader = compile(vert, "present.frag");
  }

  private static ShaderProgram compile(String vert, String frag) {
    ShaderProgram.pedantic = false;
    ShaderProgram program =
        new ShaderProgram(vert, Gdx.files.internal(SHADER_DIR + frag).readString());
    if (!program.isCompiled()) {
      throw new IllegalStateException(
          "Shader compilation failed for " + frag + "\nLog: " + program.getLog());
    }
    return program;
  }

  @Override
  public void dispose() {
    for (ShaderProgram s :
        new ShaderProgram[] {
          lightShader, compositeShader, brightShader, blurShader, presentShader
        }) {
      if (s != null) s.dispose();
    }
    if (occlusion != null) occlusion.dispose();
    if (occlusionTexture != null) occlusionTexture.dispose();
  }
}
