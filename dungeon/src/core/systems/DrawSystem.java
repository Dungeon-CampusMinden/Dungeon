package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import contrib.utils.EntityUtils;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.*;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.shader.AbstractShader;
import core.utils.components.path.IPath;
import java.util.*;

/**
 * This system draws the entities on the screen using a multi-pass rendering pipeline:
 *
 * <p>1. **Entity FBO Pass:** Entities with specific shaders are rendered to expanded FrameBuffers
 * (FBOs) using ping-ponging and local transforms (scale/padding).
 *
 * <p>2. **Scene Pass:** The level and all final entity textures (FBO output or original sprite) are
 * drawn into a screen-sized FBO if post-processing is enabled, or directly to the screen otherwise.
 *
 * <p>3. **Post-Processing Pass (Optional):** If scene shaders are configured, the scene FBO is
 * ping-ponged through the post-processing shaders.
 *
 * <p>4. **Screen Final Draw:** The final output (either the processed scene FBO or the direct scene
 * render) is presented on the screen.
 *
 * <p>Each entity with a {@link DrawComponent} and a {@link PositionComponent} will be drawn on the
 * screen.
 *
 * @see DrawComponent
 * @see Animation
 */
public final class DrawSystem extends System implements Disposable {

  /**
   * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
   * batch.
   */
  private static final SpriteBatch BATCH = new SpriteBatch();

  private final TreeMap<Integer, List<Entity>> sortedEntities = new TreeMap<>();

  private final FrameBufferPool FBO_POOL = FrameBufferPool.getInstance();
  // Dedicated SpriteBatch for rendering locally to FBOs (Pass 1 & Post-Processing Ping-Pong)
  private final SpriteBatch fboBatch = new SpriteBatch();
  private final Matrix4 fboProjectionMatrix = new Matrix4();
  private final TextureRegion fboRegion = new TextureRegion();

  // Post-processing shaders applied to the entire scene
  private List<AbstractShader> shaders = new ArrayList<>();

  private float secondsElapsed = 0f;

  /** Create a new DrawSystem. */
  public DrawSystem() {
    super(DrawComponent.class, PositionComponent.class);
    onEntityAdd = (e) -> onEntityChanged(e, true);
    onEntityRemove = (e) -> onEntityChanged(e, false);
  }

  private void onEntityChanged(Entity changed, boolean added) {
    DSData data = DSData.build(changed);
    int depth = data.dc.depth();
    List<Entity> entitiesAtDepth = sortedEntities.get(depth);

    if (entitiesAtDepth == null) {
      if (added) {
        entitiesAtDepth = new ArrayList<>();
        entitiesAtDepth.add(changed);
        sortedEntities.put(depth, entitiesAtDepth);
      }
    } else if (!entitiesAtDepth.contains(changed) && added) {
      entitiesAtDepth.add(changed);
    } else if (!added) {
      entitiesAtDepth.remove(changed);
      if (entitiesAtDepth.isEmpty()) {
        sortedEntities.remove(depth);
      }
    }
  }

  /**
   * Updates an entities depth. This needs to be called in order to update the internal sorting of
   * the DrawSystem.
   *
   * @param entity The entity that changed its depth
   * @param depth The new depth of the entity
   */
  public void changeEntityDepth(Entity entity, int depth) {
    DSData data = DSData.build(entity);

    int oldDepth = data.dc.depth();
    data.dc.depth(depth);

    sortedEntities.get(oldDepth).remove(entity);
    List<Entity> entitiesAtDepth = sortedEntities.computeIfAbsent(depth, k -> new ArrayList<>());
    entitiesAtDepth.add(entity);
  }

  /** DrawSystem can't be paused. */
  @Override
  public void stop() {
    run = true;
  }

  /** Disposes the internal resources, specifically the dedicated FBO SpriteBatch. */
  @Override
  public void dispose() {
    fboBatch.dispose();
    // The static BATCH is expected to be disposed externally (e.g., in the main game class)
  }

  /**
   * Get the {@link SpriteBatch} that is used by this system.
   *
   * @return the {@link #BATCH} of the DrawSystem
   */
  public static SpriteBatch batch() {
    return BATCH;
  }

  /**
   * Sets the list of shaders to be applied to the entire scene as post-processing effects.
   *
   * @param shaders The list of shaders to apply (or null/empty list to disable).
   */
  public void shaders(List<AbstractShader> shaders) {
    this.shaders = Objects.requireNonNullElseGet(shaders, ArrayList::new);
  }

  public List<AbstractShader> shaders() {
    return shaders;
  }

  /**
   * Will draw entities using a multi-pass render process.
   *
   * <p>1. Entity FBO Pass: Render local shader effects into FBOs (Ping-Pong).
   *
   * <p>2. Scene Pass: Draw level and final entity textures into a Scene FBO (if post-processing is
   * enabled) or directly to the screen (if disabled).
   *
   * <p>3. Post-Processing Pass: If enabled, ping-pong the Scene FBO through the scene shaders.
   *
   * <p>4. Screen Final Draw: Present the final texture.
   */
  @Override
  public void execute() {
    // Pass 1: Render shaders to FBOs (Entity-local shaders)
    renderEntitiesPass1();

    if (shaders.isEmpty()) {
      // Option A: No post-processing, render directly to screen (Original Pass 2)
      BATCH.setProjectionMatrix(CameraSystem.camera().combined);
      BATCH.begin();
      drawSceneContent();
      BATCH.end();

    } else {
      // Option B: Post-processing required
      int sceneWidth = Gdx.graphics.getWidth();
      int sceneHeight = Gdx.graphics.getHeight();

      // Obtain two FBOs from the pool for ping-ponging
      FrameBuffer fboA = FBO_POOL.obtain(sceneWidth, sceneHeight);
      FrameBuffer fboB = FBO_POOL.obtain(sceneWidth, sceneHeight);

      // Set NEAREST filtering on the FBO textures to preserve pixel crispness.
      setTextureFiltering(fboA.getColorBufferTexture());
      setTextureFiltering(fboB.getColorBufferTexture());

      // 1. Draw entire scene (level + entities) into the first FBO (fboA)
      FrameBuffer sourceFbo = fboA;
      FrameBuffer targetFbo;

      sourceFbo.begin();
      Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      BATCH.setProjectionMatrix(CameraSystem.camera().combined);
      BATCH.begin();
      drawSceneContent();
      BATCH.end();

      sourceFbo.end();

      // 2. Ping-Pong through scene shaders (Uses fboBatch for screen-space rendering)
      TextureRegion currentSourceRegion = fboRegion;
      fboBatch.setProjectionMatrix(fboProjectionMatrix.setToOrtho2D(0, 0, sceneWidth, sceneHeight));

      for (AbstractShader pass : shaders) {
        targetFbo = (sourceFbo == fboA) ? fboB : fboA;

        targetFbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        currentSourceRegion.setRegion(sourceFbo.getColorBufferTexture());
        currentSourceRegion.flip(false, true);

        fboBatch.begin();
        pass.bind(fboBatch, 1);
        setCommonUniforms(fboBatch.getShader(), sceneWidth, sceneHeight);
        fboBatch.draw(currentSourceRegion, 0, 0, sceneWidth, sceneHeight);
        pass.unbind(fboBatch);
        fboBatch.end();
        targetFbo.end();

        sourceFbo = targetFbo;
      }

      // 3. Draw final FBO result to screen (The last sourceFbo holds the final result)
      Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
      Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      Texture finalTexture = sourceFbo.getColorBufferTexture();
      currentSourceRegion.setRegion(finalTexture);
      currentSourceRegion.flip(false, true);

      BATCH.setProjectionMatrix(
        fboProjectionMatrix.setToOrtho2D(
          0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
      BATCH.begin();
      BATCH.draw(currentSourceRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      BATCH.end();

      FBO_POOL.free(fboA);
      FBO_POOL.free(fboB);
    }

    FBO_POOL.update();
    secondsElapsed += Gdx.graphics.getDeltaTime();
  }

  /**
   * Pass 1: Renders entities that require shader processing into pooled FBOs using ping-ponging.
   * These FBOs use LOCAL transformations (padding, scale) and ignore world position/rotation.
   */
  private void renderEntitiesPass1() {
    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
        .map(DSData::build)
        .filter(this::shouldDraw)
        .filter(dsd -> !dsd.dc.shaders().isEmpty())
        .forEach(this::processShaderPassesSingleEntity);
    }
  }

  /**
   * Processes the shader passes for a single entity by rendering to FBOs using ping-ponging.
   *
   * @param dsd the data record of the entity to process
   */
  private void processShaderPassesSingleEntity(final DSData dsd) {
    DrawComponent dc = dsd.dc;
    PositionComponent pc = dsd.pc;

    // --- 1. Calculate FBO Size and Obtain Buffers ---
    float padding = dsd.getTotalPadding();

    // Required size is sprite size + 2*padding. All in PIXELS.
    float unscaledWidth = dc.getSpriteWidth();
    float unscaledHeight = dc.getSpriteHeight();

    // Calculate the base pixel size (before upscaling)
    int baseFboWidth = (int) (unscaledWidth + 2 * padding);
    int baseFboHeight = (int) (unscaledHeight + 2 * padding);

    // Apply Upscale Factor to the FBO dimensions
    int shaderUpscaling = dsd.getMaxUpscale();
    int fboWidth = baseFboWidth * shaderUpscaling;
    int fboHeight = baseFboHeight * shaderUpscaling;

    // Obtain two FBOs for ping-ponging
    FrameBuffer fboA = FBO_POOL.obtain(fboWidth, fboHeight);
    FrameBuffer fboB = FBO_POOL.obtain(fboWidth, fboHeight);

    // Set NEAREST filtering on the FBO textures to preserve pixel crispness.
    setTextureFiltering(fboA.getColorBufferTexture());
    setTextureFiltering(fboB.getColorBufferTexture());

    // Calculate the projection matrix using the UPscaled FBO dimensions.
    fboProjectionMatrix.setToOrtho2D(0, 0, fboWidth, fboHeight);

    // Initial state
    FrameBuffer currentTarget = fboA;
    Texture currentSourceTexture;
    boolean useFboAAsSource = false;

    TextureRegion initialRegion = dc.getSprite();

    // --- 2. Initial Draw: Sprite -> FBO A ---
    currentTarget.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    fboBatch.setProjectionMatrix(fboProjectionMatrix);
    fboBatch.begin();
    fboBatch.draw(
        initialRegion,
        padding * shaderUpscaling,
        padding * shaderUpscaling,
        unscaledWidth * shaderUpscaling,
        unscaledHeight * shaderUpscaling);
    fboBatch.end();

    currentTarget.end();
    useFboAAsSource = true;
    currentSourceTexture = currentTarget.getColorBufferTexture();

    // --- 3. Ping-Pong Loop for Shader Passes ---
    for (int i = 0; i < dsd.dc.shaders().size(); i++) {
      AbstractShader pass = dsd.dc.shaders().get(i);

      currentTarget = useFboAAsSource ? fboB : fboA;

      currentTarget.begin();
      Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      fboRegion.setRegion(currentSourceTexture);
      fboRegion.flip(false, true);

      fboBatch.setProjectionMatrix(fboProjectionMatrix);
      fboBatch.begin();
      pass.bind(fboBatch, shaderUpscaling);
      setCommonUniforms(
          fboBatch.getShader(), fboRegion.getRegionWidth(), fboRegion.getRegionHeight());
      fboBatch.draw(fboRegion, 0, 0, fboWidth, fboHeight);
      pass.unbind(fboBatch);
      fboBatch.end();

      currentTarget.end();

      currentSourceTexture = currentTarget.getColorBufferTexture();
      useFboAAsSource = !useFboAAsSource; // Swap the source flag
    }

    // --- 4. Store Final Result and Free the other FBO ---
    dsd.dc.frameBuffer(currentTarget);

    FrameBuffer unusedFbo = currentTarget == fboA ? fboB : fboA;
    FBO_POOL.free(unusedFbo);
  }

  /**
   * Helper method to draw the entire scene content (Level + Entities). This is called regardless of
   * whether the target is the screen or a post-processing FBO.
   */
  private void drawSceneContent() {
    Game.currentLevel().ifPresent(this::drawLevel);
    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
        .map(DSData::build)
        .sorted(Comparator.comparingDouble((DSData d) -> -EntityUtils.getPosition(d.e).y()))
        .filter(this::shouldDraw)
        .forEach(this::drawFinal);
    }
  }

  //#region Draw Methods

  /**
   * Draws the final output for an entity, either from its FBO (if shaders were applied) or the
   * original sprite.
   *
   * @param dsd the data record of the entity to draw
   */
  private void drawFinal(final DSData dsd) {
    dsd.dc.update();

    FrameBuffer finalFbo = dsd.dc.frameBuffer();

    if (finalFbo != null) {

      // --- Draw FBO Texture (Shader Result) ---
      Texture fboTexture = finalFbo.getColorBufferTexture();

      float padding = dsd.getTotalPadding();
      float paddingWorldUnits = padding / dsd.getUnitSizeInPixels();

      // Translate the world position back by the padding amount (in world units)
      Point offsetPosition = dsd.pc.position().translate(-paddingWorldUnits, -paddingWorldUnits);

      Vector2 baseSpriteSize = Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight());
      DrawConfig conf = makeConfig(dsd, baseSpriteSize, dsd.pc.scale());
      draw(offsetPosition, fboTexture, conf);

      FBO_POOL.free(finalFbo);
      dsd.dc.frameBuffer(null);

    } else {
      draw(dsd);
    }
  }

  /**
   * Draws the FBO texture at the world position. This method handles the vertical flip required for
   * FBO textures and applies the world transform using the standard Affine2 transformation.
   *
   * @param position the world position where the texture should be drawn
   * @param texture the region to draw
   * @param config the {@link DrawConfig} controlling the drawing parameters
   */
  public void draw(final Point position, final Texture texture, final DrawConfig config) {
    BlendUtil.setBlending(BATCH);
    fboRegion.setRegion(texture);
    fboRegion.flip(config.mirrored(), true);
    Affine2 transform = makeTransform(position, config);
    BATCH.setColor(config.tintColor() != -1 ? ColorUtil.pmaColor(config.tintColor()) : Color.WHITE);
    BATCH.draw(fboRegion, config.size().x(), config.size().y(), transform);
    BATCH.setColor(Color.WHITE);
  }

  /**
   * Draws a sprite at a given position with the specified configuration and rotation.
   *
   * <p>The sprite will only be drawn if its position is within the camera's frustum.
   *
   * @param position the world position where the sprite should be drawn
   * @param sprite the {@link Sprite} to draw
   * @param config the {@link DrawConfig} controlling scaling, tint, and offset
   */
  public void draw(final Point position, final Sprite sprite, final DrawConfig config) {
    BlendUtil.setBlending(BATCH);
    Affine2 transform = makeTransform(position, config);
    BATCH.setColor(config.tintColor() != -1 ? ColorUtil.pmaColor(config.tintColor()) : Color.WHITE);
    BATCH.draw(sprite, config.size().x(), config.size().y(), transform);
    BATCH.setColor(Color.WHITE);
  }

  private void draw(final DSData dsd) {
    dsd.dc.update();
    Sprite sprite = dsd.dc.getSprite();
    DrawConfig conf = makeConfig(dsd, Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight()), dsd.pc.scale());
    draw(dsd.pc.position(), sprite, conf);
  }

  /**
   * Draws a texture from a path at a given position using the specified configuration.
   *
   * <p>This method automatically wraps the texture in a {@link Sprite} using {@link TextureMap} and
   * delegates to {@link #draw(Point, Sprite, DrawConfig)}.
   *
   * @param position the world position where the texture should be drawn
   * @param path the {@link IPath} identifying the texture to draw
   * @param config the {@link DrawConfig} controlling scaling, tint, and offset
   */
  public void draw(final Point position, final IPath path, final DrawConfig config) {
    draw(position, new Sprite(TextureMap.instance().textureAt(path)), config);
  }

  //#endregion

  //#region Helpers

  /**
   * Sets any common uniforms needed by all shaders (for entity FBOs).
   *
   * @param shader The shader program to set uniforms for
   * @param textureWidth The width of the texture being processed
   * @param textureHeight The height of the texture being processed
   */
  private void setCommonUniforms(ShaderProgram shader, int textureWidth, int textureHeight) {
    shader.setUniformf("u_time", secondsElapsed);
    shader.setUniformf("u_resolution", textureWidth, textureHeight);
    shader.setUniformf("u_texelSize", 1.0f / textureWidth, 1.0f / textureHeight);
    shader.setUniformf("u_aspect", 1.0f, (float) textureWidth / (float) textureHeight);

    // Mouse position in screen space
    Point mousePos = SkillTools.cursorPositionAsPoint();
    Vector3 unprojected = CameraSystem.camera().project(new Vector3(mousePos.x(), mousePos.y(), 0));
    shader.setUniformf("u_mouse", unprojected.x, unprojected.y);
  }

  private DrawConfig makeConfig(DSData dsd, Vector2 size, Vector2 scale) {
    return new DrawConfig(
        Vector2.ZERO,
        size,
        scale,
        dsd.dc.tintColor(),
        dsd.dc.currentAnimation().getConfig().mirrored(),
        dsd.pc.rotation());
  }

  private Affine2 makeTransform(Point pos, DrawConfig cfg) {
    float scaleX = cfg.scale().x() * (cfg.mirrored() ? -1f : 1f);
    float scaleY = cfg.scale().y();
    return new Affine2()
      .setToTranslation(pos.x(), pos.y())
      .scale(scaleX, scaleY)
      .translate(cfg.mirrored() ? cfg.size().x() * -1 : 0f, 0f) //adjust for mirroring offset
      .translate(cfg.size().x() / 2f, cfg.size().y() / 2f)
      .rotate(cfg.rotation())
      .translate(-cfg.size().x() / 2f, -cfg.size().y() / 2f);
  }

  private void setTextureFiltering(Texture t){
    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
  }

  private void drawLevel(ILevel currentLevel) {
    if (currentLevel == null) throw new IllegalArgumentException("Level to draw can´t be null.");

    Tile[][] layout = currentLevel.layout();
    for (Tile[] tiles : layout) {
      for (int x = 0; x < layout[0].length; x++) {
        Tile t = tiles[x];
        if (t.levelElement() != LevelElement.SKIP && !TileUtil.isTilePitAndOpen(t) && t.visible()) {
          IPath texturePath = t.texturePath();
          draw(t.position(), texturePath, new DrawConfig());
        }
      }
    }
  }

  /**
   * Checks if an entity should be drawn. By checking:
   *
   * <ol>
   *   <li>The entity itself is visible
   *   <li>Any corner of the sprite is visible
   * </ol>
   *
   * @param data the components of the entity to check
   * @return true if the entity should be drawn, false otherwise
   * @see DrawComponent#isVisible()
   */
  private boolean shouldDraw(DSData data) {
    if (!data.dc.isVisible()) {
      return false;
    }

    Point pos = data.pc.position();
    float width = data.dc.getWidth() * data.pc.scale().x();
    float height = data.dc.getHeight() * data.pc.scale().y();
    List<Point> corners =
      List.of(
        pos.translate(0, 0),
        pos.translate(width, 0),
        pos.translate(0, height),
        pos.translate(width, height));

    return Game.currentLevel()
      .map(
        level ->
          corners.stream()
            .anyMatch(
              c -> {
                Tile t = level.tileAt(c).orElse(null);
                return t != null && t.visible() && !TileUtil.isTilePitAndOpen(t);
              }))
      .orElse(false);
  }

  //#endregion

  private record DSData(Entity e, DrawComponent dc, PositionComponent pc) {
    /**
     * Builds the data record used by this system.
     *
     * @param entity The entity with a DrawComponent and a PositionComponent
     * @return The data record
     */
    static DSData build(final Entity entity){
      DrawComponent dc =
        entity
          .fetch(DrawComponent.class)
          .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
      PositionComponent pc =
        entity
          .fetch(PositionComponent.class)
          .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
      return new DSData(entity, dc, pc);
    }

    /**
     * Returns the total padding required by all shaders in the DrawComponent.
     *
     * @return the total padding in pixels
     */
    int getTotalPadding() {
      return (int) dc.shaders().stream().mapToDouble(AbstractShader::getPadding).sum();
    }

    /**
     * Returns the highest upscaling factor required by any shader in the DrawComponent.
     *
     * @return the maximum upscaling factor
     */
    int getMaxUpscale() {
      Optional<AbstractShader> max =
          dc.shaders().stream().max(Comparator.comparingInt(AbstractShader::upscaling));
      int upscale = max.map(AbstractShader::upscaling).orElse(1);
      return Math.max(1, upscale);
    }

    /**
     * Returns the size of one world unit that this texture is drawn with, in pixels. The smallest
     * dimension is assumed to be 1 world unit.
     *
     * @return the size of one world unit in pixels
     */
    float getUnitSizeInPixels() {
      return Math.min(dc.getSpriteWidth(), dc.getSpriteHeight());
    }
  }
}
