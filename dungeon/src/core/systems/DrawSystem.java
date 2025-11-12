package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
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
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.*;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.shader.AbstractShader;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.IPath;
import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * This system draws the entities on the screen using a multi-pass rendering pipeline:
 *
 * <p>1. **Entity FBO Pass:** Entities with specific shaders are rendered to expanded FrameBuffers
 * (FBOs) using ping-ponging and local transforms (scale/padding).
 *
 * <p>2. **Intermediate Layer Passes:** The level and each entity depth layer are rendered into
 * individual FBOs, applying layer-specific shaders (Level Pass, Depth Pass).
 *
 * <p>3. **Scene Composition Pass:** All intermediate FBOs (Level and Depth layers) are composited
 * into a single screen-sized FBO.
 *
 * <p>4. **Post-Processing Pass (Optional):** If scene shaders are configured, the scene FBO is
 * ping-ponged through the post-processing shaders.
 *
 * <p>5. **Screen Final Draw:** The final output is presented on the screen.
 *
 * <p>Each entity with a {@link DrawComponent} and a {@link PositionComponent} will be drawn on the
 * screen.
 *
 * @see DrawComponent
 * @see Animation
 */
public final class DrawSystem extends System implements Disposable {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DrawSystem.class);

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
  private final Map<Entity, FrameBuffer> entityFboCache = new HashMap<>();

  // Shaders applied to the level layer
  private final ShaderList levelShaders = new ShaderList();
  // Shaders applied to each entity depth layer (depth = key)
  private final Map<Integer, ShaderList> entityDepthShaders = new HashMap<>();
  // Post-processing shaders applied to the entire scene
  private final ShaderList sceneShaders = new ShaderList();

  private float secondsElapsed = 0f;
  private int shadersActiveLastFrame = 0;

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

      // Clean up cached FBO if the entity is removed
      if (entityFboCache.containsKey(changed)) {
        FBO_POOL.free(entityFboCache.remove(changed));
      }

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

    List<Entity> oldGroup = sortedEntities.get(oldDepth);
    if (oldGroup != null) {
      oldGroup.remove(entity);
      if (oldGroup.isEmpty()) {
        sortedEntities.remove(oldDepth);
      }
    }

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
    entityFboCache.values().forEach(FBO_POOL::free);
    entityFboCache.clear();
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
   * Gets the ShaderList applied to the entire scene (post-processing).
   *
   * @return The scene ShaderList
   */
  public ShaderList sceneShaders() {
    return sceneShaders;
  }

  /**
   * Gets the ShaderList applied to the level layer.
   *
   * @return The level ShaderList
   */
  public ShaderList levelShaders() {
    return levelShaders;
  }

  /**
   * Gets the ShaderList for a specific entity depth. Creates one if it does not exist.
   *
   * @param depth The entity depth
   * @return The ShaderList for the specified depth
   */
  public ShaderList entityDepthShaders(int depth) {
    if (!entityDepthShaders.containsKey(depth)) {
      entityDepthShaders.put(depth, new ShaderList());
    }
    return entityDepthShaders.get(depth);
  }

  /**
   * Will draw entities using a multi-pass render process.
   *
   * <p>1. Entity FBO Pass: Render local shader effects into FBOs (Ping-Pong).
   *
   * <p>2. Intermediate Layer Passes: Draw level and depth groups into their own FBOs, applying
   * layer-specific shaders.
   *
   * <p>3. Scene Composition Pass: Composite all intermediate FBOs into a Scene FBO (or directly to
   * the screen if no scene shaders are applied).
   *
   * <p>4. Post-Processing Pass: If enabled, ping-pong the Scene FBO through the scene shaders.
   *
   * <p>5. Screen Final Draw: Present the final texture.
   */
  @Override
  public void execute() {
    shadersActiveLastFrame = 0;

    // Pass 1: Render shaders to FBOs (Entity-local shaders)
    renderEntitiesPass1();

    int sceneWidth = Gdx.graphics.getWidth();
    int sceneHeight = Gdx.graphics.getHeight();

    // Get FBOs for scene composition/post-processing
    FrameBuffer fboA = FBO_POOL.obtain(sceneWidth, sceneHeight);
    FrameBuffer fboB = FBO_POOL.obtain(sceneWidth, sceneHeight);
    setTextureFiltering(fboA.getColorBufferTexture());
    setTextureFiltering(fboB.getColorBufferTexture());

    // 1. Render Level to its FBO and apply level shaders
    FrameBuffer levelFbo =
        drawToIntermediateFbo(this::drawLevel, levelShaders, sceneWidth, sceneHeight);

    // 2. Render each Entity Depth Group to its FBO and apply depth shaders
    Map<Integer, FrameBuffer> depthFbos = new HashMap<>();
    for (Integer depth : sortedEntities.keySet()) {
      List<DSData> sortedGroup =
          sortedEntities.get(depth).stream()
              .map(DSData::build)
              .sorted(Comparator.comparingDouble((DSData d) -> -EntityUtils.getPosition(d.e).y()))
              .filter(this::shouldDraw)
              .toList();

      if (!sortedGroup.isEmpty()) {
        ShaderList shaders = entityDepthShaders.get(depth);
        FrameBuffer depthFbo =
            drawToIntermediateFbo(
                () -> sortedGroup.forEach(this::drawFinal), shaders, sceneWidth, sceneHeight);
        depthFbos.put(depth, depthFbo);
      }
    }

    // 3. Scene Composition Pass: Draw all intermediate FBOs into fboA in sorted order
    fboA.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    BATCH.setProjectionMatrix(fboProjectionMatrix.setToOrtho2D(0, 0, sceneWidth, sceneHeight));
    BATCH.begin();
    BATCH.setColor(Color.WHITE);

    // Draw Level FBO first
    drawFboToBatch(levelFbo, sceneWidth, sceneHeight);

    // Draw Depth FBOs in ascending order
    sortedEntities.keySet().stream()
        .forEach(
            depth ->
                Optional.ofNullable(depthFbos.get(depth))
                    .ifPresent(fbo -> drawFboToBatch(fbo, sceneWidth, sceneHeight)));

    BATCH.end();
    fboA.end();

    FBO_POOL.free(levelFbo);
    depthFbos.values().forEach(FBO_POOL::free);

    // 4. Post-Processing Pass (Scene Shaders) & 5. Screen Final Draw
    FrameBuffer finalSourceFbo =
        applyShadersAndCompose(fboA, fboB, sceneShaders, sceneWidth, sceneHeight);

    // Draw final result to screen
    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    Texture finalTexture = finalSourceFbo.getColorBufferTexture();
    fboRegion.setRegion(finalTexture);
    fboRegion.flip(false, true);

    BATCH.setProjectionMatrix(
        fboProjectionMatrix.setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    BATCH.begin();
    BlendUtils.setBlending(BATCH);
    BATCH.setColor(Color.WHITE);
    BATCH.draw(fboRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    BATCH.end();

    FBO_POOL.free(fboA);
    FBO_POOL.free(fboB);

    FBO_POOL.update();
    secondsElapsed += Gdx.graphics.getDeltaTime();
  }

  /**
   * Renders content to an intermediate FBO, applies a list of shaders, and returns the final FBO.
   *
   * @param renderAction The action to draw the content (level or entity group)
   * @param shaders The list of shaders to apply (can be null)
   * @param width The width of the scene/FBO
   * @param height The height of the scene/FBO
   * @return The FBO containing the final, processed layer content
   */
  private FrameBuffer drawToIntermediateFbo(
      Runnable renderAction, ShaderList shaders, int width, int height) {
    // Obtain two FBOs for ping-ponging
    FrameBuffer fboA = FBO_POOL.obtain(width, height);
    FrameBuffer fboB = FBO_POOL.obtain(width, height);
    setTextureFiltering(fboA.getColorBufferTexture());
    setTextureFiltering(fboB.getColorBufferTexture());

    // 1. Draw content into the first FBO (fboA)
    fboA.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    BATCH.setProjectionMatrix(CameraSystem.camera().combined);
    BATCH.begin();
    BlendUtils.setBlending(BATCH);
    renderAction.run();
    BATCH.end();

    fboA.end();

    // 2. Ping-Pong through shaders
    FrameBuffer finalFbo = applyShadersAndCompose(fboA, fboB, shaders, width, height);

    // Free the unused FBO
    FrameBuffer unusedFbo = finalFbo == fboA ? fboB : fboA;
    FBO_POOL.free(unusedFbo);

    return finalFbo;
  }

  /**
   * Applies a list of shaders to a source FBO using ping-pong and returns the FBO with the final
   * result. If the shader list is empty, the original source FBO is returned.
   *
   * @param fboA FBO A containing the initial source texture
   * @param fboB FBO B used for ping-ponging
   * @param shaders The shaders to apply (can be null)
   * @param width The width of the FBO
   * @param height The height of the FBO
   * @return The FBO containing the final processed result
   */
  private FrameBuffer applyShadersAndCompose(
      FrameBuffer fboA, FrameBuffer fboB, ShaderList shaders, int width, int height) {
    if (shaders == null || !shaders.hasEnabledShaders()) {
      return fboA;
    }

    FrameBuffer sourceFbo = fboA;
    FrameBuffer targetFbo;
    TextureRegion currentSourceRegion = fboRegion;
    fboBatch.setProjectionMatrix(fboProjectionMatrix.setToOrtho2D(0, 0, width, height));

    for (AbstractShader pass : shaders.getEnabledSorted()) {
      Rectangle worldBounds = getFboWorldBounds(null);
      Rectangle shaderBounds = pass.worldBounds();
      if (shaderBounds != null && !worldBounds.intersects(shaderBounds)) {
        continue;
      }
      shadersActiveLastFrame++;

      targetFbo = (sourceFbo == fboA) ? fboB : fboA;

      targetFbo.begin();
      Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      currentSourceRegion.setRegion(sourceFbo.getColorBufferTexture());
      currentSourceRegion.flip(false, true);

      fboBatch.begin();
      BlendUtils.setBlending(fboBatch);
      pass.bind(fboBatch, 1);
      setCommonUniforms(fboBatch.getShader(), width, height, worldBounds, 0);
      fboBatch.setColor(Color.WHITE);
      fboBatch.draw(currentSourceRegion, 0, 0, width, height);
      pass.unbind(fboBatch);
      fboBatch.end();
      targetFbo.end();

      sourceFbo = targetFbo;
    }

    return sourceFbo;
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
          .filter(dsd -> dsd.dc.shaders().hasEnabledShaders())
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

    // --- 1. Calculate FBO Size and Obtain Buffers ---
    float padding = dsd.dc.shaders().getTotalPadding();

    // Required size is sprite size + 2*padding. All in PIXELS.
    float unscaledWidth = dc.getSpriteWidth();
    float unscaledHeight = dc.getSpriteHeight();

    // Calculate the base pixel size (before upscaling)
    int baseFboWidth = (int) (unscaledWidth + 2 * padding);
    int baseFboHeight = (int) (unscaledHeight + 2 * padding);

    // Apply Upscale Factor to the FBO dimensions
    int shaderUpscaling = dsd.dc.shaders().getMaxUpscaling();
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
    BlendUtils.setBlending(fboBatch);
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
    for (AbstractShader pass : dc.shaders().getEnabledSorted()) {
      Rectangle worldBounds = getFboWorldBounds(dsd);
      Rectangle shaderBounds = pass.worldBounds();
      if (shaderBounds != null && !worldBounds.intersects(shaderBounds)) {
        continue;
      }
      shadersActiveLastFrame++;

      currentTarget = useFboAAsSource ? fboB : fboA;

      currentTarget.begin();
      Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      fboRegion.setRegion(currentSourceTexture);
      fboRegion.flip(false, true);

      fboBatch.setProjectionMatrix(fboProjectionMatrix);
      fboBatch.begin();
      BlendUtils.setBlending(fboBatch);
      pass.bind(fboBatch, shaderUpscaling);

      float rotation = dsd.pc.rotation() * MathUtils.degreesToRadians;
      setCommonUniforms(
          fboBatch.getShader(),
          fboRegion.getRegionWidth(),
          fboRegion.getRegionHeight(),
          worldBounds,
          rotation);

      fboBatch.draw(fboRegion, 0, 0, fboWidth, fboHeight);
      pass.unbind(fboBatch);
      fboBatch.end();

      currentTarget.end();

      currentSourceTexture = currentTarget.getColorBufferTexture();
      useFboAAsSource = !useFboAAsSource; // Swap the source flag
    }

    // --- 4. Store Final Result and Free the other FBO ---
    FrameBuffer oldFbo = entityFboCache.put(dsd.e, currentTarget);
    if (oldFbo != null) {
      LOGGER.warn("Entity FBO cache overwrite for entity: " + dsd.e);
      FBO_POOL.free(oldFbo);
    }

    FrameBuffer unusedFbo = currentTarget == fboA ? fboB : fboA;
    FBO_POOL.free(unusedFbo);
  }

  // region Draw Methods

  /**
   * Draws the content of an FBO (which is expected to be vertically flipped) to the current batch,
   * applying the required vertical flip.
   *
   * @param fbo The FBO to draw
   * @param width The width of the FBO
   * @param height The height of the FBO
   */
  private void drawFboToBatch(FrameBuffer fbo, int width, int height) {
    BlendUtils.setBlending(BATCH);
    fboRegion.setRegion(fbo.getColorBufferTexture());
    fboRegion.flip(false, true); // Flip back to draw correctly
    BATCH.draw(fboRegion, 0, 0, width, height);
  }

  /**
   * Draws the final output for an entity, either from its FBO (if shaders were applied) or the
   * original sprite.
   *
   * @param dsd the data record of the entity to draw
   */
  private void drawFinal(final DSData dsd) {
    dsd.dc.update();

    FrameBuffer finalFbo = entityFboCache.get(dsd.e);

    if (finalFbo != null) {
      // --- Draw FBO Texture (Shader Result) ---
      Texture fboTexture = finalFbo.getColorBufferTexture();

      float padding = dsd.dc.shaders().getTotalPadding();
      float unitSize = dsd.getUnitSizeInPixels();
      float paddingWorldUnits = padding / unitSize;

      // Scale is being factored into the transformation everywhere except the position, since it is
      // passed directly to the draw method. Thus, we need to factor it in here to offset the
      // padding.
      Point offsetPosition =
          dsd.pc
              .position()
              .translate(
                  -paddingWorldUnits * dsd.pc.scale().x(), -paddingWorldUnits * dsd.pc.scale().y());

      // Final world size includes padding on all sides
      float worldWidth = dsd.dc.getWidth();
      float worldHeight = dsd.dc.getHeight();
      Vector2 finalWorldSize =
          Vector2.of(worldWidth + 2 * paddingWorldUnits, worldHeight + 2 * paddingWorldUnits);

      DrawConfig conf = makeConfig(dsd, finalWorldSize, dsd.pc.scale());
      draw(offsetPosition, fboTexture, conf);

      FBO_POOL.free(finalFbo);
      entityFboCache.remove(dsd.e);
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
    BlendUtils.setBlending(BATCH);
    fboRegion.setRegion(texture);
    fboRegion.flip(config.mirrored(), true);
    Affine2 transform = makeTransform(position, config);
    BATCH.setColor(
        config.tintColor() != -1 ? ColorUtils.pmaColor(config.tintColor()) : Color.WHITE);
    BATCH.draw(fboRegion, config.size().x(), config.size().y(), transform);
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
    BlendUtils.setBlending(BATCH);
    Affine2 transform = makeTransform(position, config);
    BATCH.setColor(
        config.tintColor() != -1 ? ColorUtils.pmaColor(config.tintColor()) : Color.WHITE);
    BATCH.draw(sprite, config.size().x(), config.size().y(), transform);
  }

  private void draw(final DSData dsd) {
    Sprite sprite = dsd.dc.getSprite();
    DrawConfig conf =
        makeConfig(dsd, Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight()), dsd.pc.scale());
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

  // endregion

  // region Helpers

  /**
   * Sets any common uniforms needed by all shaders (for entity FBOs).
   *
   * @param shader The shader program to set uniforms for
   * @param textureWidth The width of the texture being processed
   * @param textureHeight The height of the texture being processed
   * @param worldBounds The world bounds of the entity
   * @param rotation The rotation of the entity in radians
   */
  private void setCommonUniforms(
      ShaderProgram shader,
      int textureWidth,
      int textureHeight,
      Rectangle worldBounds,
      float rotation) {
    shader.setUniformf("u_time", secondsElapsed);
    shader.setUniformf("u_resolution", textureWidth, textureHeight);
    shader.setUniformf("u_texelSize", 1.0f / textureWidth, 1.0f / textureHeight);
    shader.setUniformf("u_aspect", 1.0f, (float) textureWidth / (float) textureHeight);

    // Mouse position in screen space
    Point mousePos = SkillTools.cursorPositionAsPoint();
    Vector3 unprojected = CameraSystem.camera().project(new Vector3(mousePos.x(), mousePos.y(), 0));
    shader.setUniformf(
        "u_mouse", unprojected.x / Game.windowWidth(), unprojected.y / Game.windowHeight());

    shader.setUniformf(
        "u_entityBounds",
        worldBounds.x(),
        worldBounds.y(),
        worldBounds.width(),
        worldBounds.height());
    shader.setUniformf("u_rotation", rotation);
  }

  private Rectangle getFboWorldBounds(DSData dsd) {
    if (dsd == null) {
      OrthographicCamera camera = CameraSystem.camera();
      float worldWidth = camera.viewportWidth * camera.zoom;
      float worldHeight = camera.viewportHeight * camera.zoom;
      float camX = camera.position.x;
      float camY = camera.position.y;
      float posX = camX - (worldWidth / 2f);
      float posY = camY - (worldHeight / 2f);
      return new Rectangle(worldWidth, worldHeight, posX, posY);
    }
    float posX = dsd.pc.position().x();
    float posY = dsd.pc.position().y();
    float worldWidth = dsd.pc.scale().x() * dsd.dc.getWidth();
    float worldHeight = dsd.pc.scale().y() * dsd.dc.getHeight();
    return new Rectangle(worldWidth, worldHeight, posX, posY);
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
        .translate(cfg.mirrored() ? cfg.size().x() * -1 : 0f, 0f) // adjust for mirroring offset
        .translate(cfg.size().x() / 2f, cfg.size().y() / 2f)
        .rotate(cfg.rotation())
        .translate(-cfg.size().x() / 2f, -cfg.size().y() / 2f);
  }

  private void setTextureFiltering(Texture t) {
    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
  }

  private void drawLevel() {
    Game.currentLevel()
        .ifPresent(
            currentLevel -> {
              Tile[][] layout = currentLevel.layout();
              for (Tile[] tiles : layout) {
                for (int x = 0; x < layout[0].length; x++) {
                  Tile t = tiles[x];
                  if (t.levelElement() != LevelElement.SKIP
                      && !TileUtils.isTilePitAndOpen(t)
                      && t.visible()) {
                    IPath texturePath = t.texturePath();
                    int tintColor =
                        t.tintColor() == -1 ? Color.rgba8888(Color.WHITE) : t.tintColor();
                    draw(t.position(), texturePath, new DrawConfig().withTintColor(tintColor));
                  }
                }
              }
            });
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
                          return t != null && t.visible() && !TileUtils.isTilePitAndOpen(t);
                        }))
        .orElse(false);
  }

  // endregion

  /**
   * Gets the total seconds elapsed since the DrawSystem started. If the DrawSystem is not found in
   * the Game systems, returns 0.
   *
   * @return the total seconds elapsed
   */
  public static float secondsElapsed() {
    System s = Game.systems().get(DrawSystem.class);
    if (!(s instanceof DrawSystem ds)) return 0;
    return ds.secondsElapsed;
  }

  /**
   * Gets the number of active shaders in the last frame. If the DrawSystem is not found in the Game
   * systems, returns 0.
   * @return the number of active shaders in the last frame
   */
  public static float shadersActiveLastFrame() {
    System s = Game.systems().get(DrawSystem.class);
    if (!(s instanceof DrawSystem ds)) return 0;
    return ds.shadersActiveLastFrame;
  }

  private record DSData(Entity e, DrawComponent dc, PositionComponent pc) {
    /**
     * Builds the data record used by this system.
     *
     * @param entity The entity with a DrawComponent and a PositionComponent
     * @return The data record
     */
    static DSData build(final Entity entity) {
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
