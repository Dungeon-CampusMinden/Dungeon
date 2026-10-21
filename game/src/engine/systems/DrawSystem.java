package engine.systems;

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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.game.ECSManagement;
import engine.input.CursorUtils;
import engine.rendering.AtmosphereRenderer;
import engine.rendering.EmissiveSprites;
import engine.rendering.WorldOverlay;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.MissingComponentException;
import engine.utils.components.draw.BlendUtils;
import engine.utils.components.draw.ColorUtils;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.DrawConfig;
import engine.utils.components.draw.FrameBufferPool;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.shader.AbstractShader;
import engine.utils.components.draw.shader.OutlineShader;
import engine.utils.components.draw.shader.ShaderList;
import engine.utils.components.path.IPath;
import engine.utils.logging.DungeonLogger;
import feature.utils.EntityUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * This system draws the entities on the screen using a multi-pass rendering pipeline:
 *
 * <p>1. **Entity FBO Pass:** Entities with specific shaders are rendered to expanded FrameBuffers
 * (FBOs) using ping-ponging and local transforms (scale/padding).
 *
 * <p>2. **Intermediate Layer Passes:** The level and each entity depth layer are rendered into
 * individual FBOs, applying layer-specific shaders (Level Pass, Depth Pass).
 *
 * <p>3. **Scene Composition Pass:** Depth layers at or below {@link DepthLayer#Level} are
 * composited before the level FBO; higher layers are composited afterward.
 *
 * <p>4. **Atmosphere Pass:** The {@link AtmosphereRenderer} lights and grades the scene; outlines
 * and world overlays are drawn over the lit scene.
 *
 * <p>5. **Post-Processing Pass (Optional):** If scene shaders are configured, the lit scene is
 * ping-ponged through them.
 *
 * <p>6. **Screen Final Draw:** The {@link AtmosphereRenderer} presents the scene on the screen.
 *
 * <p>All scene buffers are rendered at a fixed resolution of two texels per sprite pixel around a
 * texel-snapped camera; see {@link AtmosphereRenderer}.
 *
 * <p>Each entity with a {@link DrawComponent} and a {@link PositionComponent} will be drawn on the
 * screen.
 *
 * @see DrawComponent
 * @see Animation
 */
public final class DrawSystem extends System implements Disposable {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DrawSystem.class);
  private static DrawSystem INSTANCE = null;

  /**
   * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
   * batch.
   */
  private static SpriteBatch BATCH; // lazy initialized

  private static SpriteBatch FBO_BATCH; // lazy initialized

  private final TreeMap<Integer, List<Entity>> sortedEntities = new TreeMap<>();

  private final FrameBufferPool FBO_POOL = FrameBufferPool.getInstance();
  // Dedicated SpriteBatch for rendering locally to FBOs (Pass 1 & Post-Processing Ping-Pong)
  private final Matrix4 fboProjectionMatrix = new Matrix4();
  private final TextureRegion fboRegion = new TextureRegion();
  private final Sprite emissiveSprite = new Sprite();
  private final Map<Entity, FrameBuffer> entityFboCache = new HashMap<>();
  // Entities with an outline this frame and their sprites without it; the outlines are drawn over
  // the lit scene.
  private final Map<DSData, FrameBuffer> outlined = new LinkedHashMap<>();

  // Shaders applied to the level layer
  private final ShaderList levelShaders = new ShaderList();
  // Shaders applied to each entity depth layer (depth = key)
  private final Map<Integer, ShaderList> entityDepthShaders = new HashMap<>();
  // Post-processing shaders applied to the entire scene
  private final ShaderList sceneShaders = new ShaderList();

  private float secondsElapsed = 0f;
  private int shadersActiveLastFrame = 0;

  private int stableWidth = -1;
  private int stableHeight = -1;
  private int unstableWidth = -1;
  private int unstableHeight = -1;
  private int stableResizeFrames = -1;

  // Entities on these layers stand on the floor: they get a contact shadow and raise dust.
  private static final int[] GROUNDED_DEPTHS = {
    DepthLayer.Normal.depth(), DepthLayer.Player.depth()
  };
  private final AtmosphereRenderer atmosphere = new AtmosphereRenderer();

  /**
   * Gets the singleton instance of the DrawSystem, creating it if it does not exist.
   *
   * @return The singleton DrawSystem instance
   */
  public static DrawSystem getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new DrawSystem();
    }
    return INSTANCE;
  }

  /** Create a new DrawSystem. */
  private DrawSystem() {
    super(AuthoritativeSide.BOTH, DrawComponent.class, PositionComponent.class);
    onEntityAdd = (e) -> onEntityChanged(e, true);
    onEntityRemove = (e) -> onEntityChanged(e, false);
    INSTANCE = this;
  }

  private void onEntityChanged(Entity changed, boolean added) {
    if (changed.fetch(DrawComponent.class).isEmpty()) return;
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
    fboBatch().dispose();
    atmosphere.dispose();
    entityFboCache.values().forEach(FBO_POOL::free);
    entityFboCache.clear();
    // The static batch() is expected to be disposed externally (e.g., in the main game class)
  }

  /**
   * Get the {@link SpriteBatch} that is used by this system.
   *
   * <p>The batch is lazily initialized to ensure that the LibGDX environment is fully set up before
   * its creation.
   *
   * @return the {@link #BATCH} of the DrawSystem
   */
  public static SpriteBatch batch() {
    if (BATCH == null) {
      BATCH = new SpriteBatch();
    }

    return BATCH;
  }

  /**
   * Gets the dedicated SpriteBatch used for rendering to FBOs.
   *
   * <p>This batch is lazily initialized to ensure that the LibGDX environment is fully set up
   * before its creation.
   *
   * @return The {@link #FBO_BATCH} used for FBO rendering
   */
  private SpriteBatch fboBatch() {
    if (FBO_BATCH == null) {
      FBO_BATCH = new SpriteBatch();
    }
    return FBO_BATCH;
  }

  /**
   * Gets the ShaderList applied to the entire scene (post-processing). These shaders run on the lit
   * scene, after the light, fog, bloom and world overlays, so they can move, mask or tint what the
   * player sees; effects that should be lit themselves belong to the level or a depth layer.
   *
   * @return The scene ShaderList
   */
  public ShaderList sceneShaders() {
    return sceneShaders;
  }

  /**
   * Gets the renderer of the scene's light and atmosphere. Scene shaders draw into its scene
   * buffers, which show a little more than the screen; see {@link AtmosphereRenderer#view()}.
   *
   * @return the atmosphere renderer
   */
  public AtmosphereRenderer atmosphere() {
    return atmosphere;
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
   * <p>3. Scene Composition Pass: Composite depth layers at or below the level depth, then the
   * level, then higher entity layers.
   *
   * <p>4. Atmosphere Pass: Light and grade the scene, then draw outlines and world overlays over
   * it.
   *
   * <p>5. Post-Processing Pass: If enabled, ping-pong the lit scene through the scene shaders.
   *
   * <p>6. Screen Final Draw: Present the final texture.
   */
  @Override
  public void execute() {
    filteredEntityStream().map(DSData::build).forEach(dsd -> dsd.dc.update());

    if (stableWidth == -1) {
      stableWidth = Game.windowWidth();
      stableHeight = Game.windowHeight();
      stableResizeFrames = 0;
    }

    checkStableResize();
  }

  /** Tracks the most recent stable window size, to not flood the FBO creation while resizing. */
  private void checkStableResize() {
    int currentWidth = Game.windowWidth();
    int currentHeight = Game.windowHeight();

    if (currentWidth != unstableWidth || currentHeight != unstableHeight) {
      if (currentWidth == 0 || currentHeight == 0) return; // Ignore minimized window
      unstableWidth = currentWidth;
      unstableHeight = currentHeight;
      stableResizeFrames = 5;
    } else {
      if (stableResizeFrames == 0) {
        if (stableWidth != unstableWidth || stableHeight != unstableHeight) {
          stableWidth = unstableWidth;
          stableHeight = unstableHeight;
          LOGGER.info("Stable window resize detected: " + stableWidth + "x" + stableHeight);
        }
      } else {
        stableResizeFrames--;
      }
    }
  }

  /**
   * Makes the render pipeline use the current window size without waiting for resize debouncing.
   *
   * <p>Fullscreen transitions are discrete mode switches, not interactive resize drags. Deferring
   * FBO resizing during those transitions can leave stale-sized framebuffers on screen for several
   * frames on Windows and Linux.
   */
  public void useCurrentWindowSizeImmediately() {
    int currentWidth = Game.windowWidth();
    int currentHeight = Game.windowHeight();
    if (currentWidth <= 0 || currentHeight <= 0) return;

    stableWidth = currentWidth;
    stableHeight = currentHeight;
    unstableWidth = currentWidth;
    unstableHeight = currentHeight;
    stableResizeFrames = 0;
  }

  @Override
  public void render(float delta) {
    if (stableWidth == -1) return;

    shadersActiveLastFrame = 0;

    try {
      atmosphere.beginFrame(stableWidth, stableHeight, delta, secondsElapsed);
      renderScene();
    } finally {
      atmosphere.endFrame();
    }

    FBO_POOL.update();
    secondsElapsed += delta;
  }

  /** Renders the scene through all passes; runs between the atmosphere's frame start and end. */
  private void renderScene() {
    // Pass 1: Render shaders to FBOs (Entity-local shaders)
    renderEntitiesPass1();

    int sceneWidth = atmosphere.width();
    int sceneHeight = atmosphere.height();

    // Get FBOs for scene composition/post-processing
    FrameBuffer fboA = FBO_POOL.obtain(sceneWidth, sceneHeight);
    FrameBuffer fboB = FBO_POOL.obtain(sceneWidth, sceneHeight);
    setTextureFiltering(fboA.getColorBufferTexture());
    setTextureFiltering(fboB.getColorBufferTexture());

    // 1. Render Level to its FBO and apply level shaders
    FrameBuffer levelFbo =
        drawToIntermediateFbo(this::drawLevel, levelShaders, sceneWidth, sceneHeight);

    // 2. Render each Entity Depth Group to its FBO and apply depth shaders
    TreeMap<Integer, FrameBuffer> depthFbos = new TreeMap<>();
    List<DSData> drawn = new ArrayList<>();
    for (Integer depth : sortedEntities.keySet()) {
      List<DSData> sortedGroup =
          sortedEntities.get(depth).stream()
              .map(DSData::build)
              .sorted(
                  Comparator.comparingDouble(
                      (DSData d) -> -(EntityUtils.getPosition(d.e).y() + d.dc.sortOffset())))
              .filter(this::shouldDraw)
              .toList();

      if (!sortedGroup.isEmpty()) {
        drawn.addAll(sortedGroup);
        ShaderList shaders = entityDepthShaders.get(depth);
        FrameBuffer depthFbo =
            drawToIntermediateFbo(
                () -> sortedGroup.forEach(this::drawFinal), shaders, sceneWidth, sceneHeight);
        depthFbos.put(depth, depthFbo);
      }
    }

    // 3. Scene Composition Pass: Draw background layers, the level, then foreground layers
    fboA.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    batch().setProjectionMatrix(fboProjectionMatrix.setToOrtho2D(0, 0, sceneWidth, sceneHeight));
    batch().begin();
    batch().setColor(Color.WHITE);

    depthFbos
        .headMap(DepthLayer.Level.depth(), true)
        .values()
        .forEach(fbo -> drawFboToBatch(fbo, sceneWidth, sceneHeight));
    drawFboToBatch(levelFbo, sceneWidth, sceneHeight);
    depthFbos
        .tailMap(DepthLayer.Level.depth(), false)
        .values()
        .forEach(fbo -> drawFboToBatch(fbo, sceneWidth, sceneHeight));

    batch().setProjectionMatrix(CameraSystem.camera().combined);
    atmosphere.drawParticles(batch());

    batch().end();
    fboA.end();

    FBO_POOL.free(levelFbo);
    depthFbos.values().forEach(FBO_POOL::free);

    // 4. Atmosphere, 5. Post-Processing Pass (Scene Shaders) on the lit scene & Screen Final Draw
    Optional<FrameBuffer> emissive =
        drawn.stream().anyMatch(d -> EmissiveSprites.mask(d.dc).isPresent())
            ? Optional.of(
                drawToIntermediateFbo(() -> drawEmissive(drawn), null, sceneWidth, sceneHeight))
            : Optional.empty();
    FrameBuffer bases =
        drawToIntermediateFbo(() -> drawBases(drawn), null, sceneWidth, sceneHeight);
    FrameBuffer graded = atmosphere.grade(fboA, emissive, bases, batch());
    emissive.ifPresent(FBO_POOL::free);
    FBO_POOL.free(bases);
    drawOverlays(graded);
    FrameBuffer finalFbo =
        applyShadersAndCompose(graded, fboB, sceneShaders, sceneWidth, sceneHeight);

    Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    atmosphere.present(finalFbo, batch());

    FBO_POOL.free(fboA);
    FBO_POOL.free(fboB);
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

    batch().setProjectionMatrix(CameraSystem.camera().combined);
    batch().begin();
    BlendUtils.setBlending(batch());
    renderAction.run();
    batch().end();

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
    fboBatch().setProjectionMatrix(fboProjectionMatrix.setToOrtho2D(0, 0, width, height));

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

      fboBatch().begin();
      BlendUtils.setBlending(fboBatch());
      pass.bind(fboBatch(), 1);
      setCommonUniforms(fboBatch().getShader(), width, height, worldBounds, 0);
      fboBatch().setColor(Color.WHITE);
      fboBatch().draw(currentSourceRegion, 0, 0, width, height);
      pass.unbind(fboBatch());
      fboBatch().end();
      targetFbo.end();

      sourceFbo = targetFbo;
    }

    return sourceFbo;
  }

  /**
   * Pass 1: Hands every entity to the atmosphere and renders entities that require shader
   * processing into pooled FBOs using ping-ponging. These FBOs use LOCAL transformations (padding,
   * scale) and ignore world position/rotation.
   */
  private void renderEntitiesPass1() {
    for (List<Entity> group : sortedEntities.values()) {
      for (Entity entity : group) {
        DSData dsd = DSData.build(entity);
        atmosphere.addEntity(entity, dsd.dc, dsd.pc, isGrounded(dsd.dc));
        if (shouldDraw(dsd) && dsd.dc.shaders().hasEnabledShaders()) {
          processShaderPassesSingleEntity(dsd);
        }
      }
    }
  }

  /**
   * Processes the shader passes for a single entity by rendering to FBOs using ping-ponging.
   *
   * @param dsd the data record of the entity to process
   */
  private void processShaderPassesSingleEntity(final DSData dsd) {
    // The outline is drawn over the lit scene, see drawOverlays.
    drawOutlines(dsd.dc.shaders(), false);
    FrameBuffer generated = processShaders(dsd.dc.getSprite(), dsd.dc.shaders(), dsd.pc);
    drawOutlines(dsd.dc.shaders(), true);
    FrameBuffer oldFbo = entityFboCache.put(dsd.e, generated);
    if (oldFbo != null) {
      LOGGER.warn("Entity FBO cache overwrite for entity: " + dsd.e);
      FBO_POOL.free(oldFbo);
    }
  }

  /**
   * Processes a series of shaders on a given texture region using ping-pong FBO rendering.
   *
   * @param region The texture region to process
   * @param shaders The list of shaders to apply
   * @return The FBO containing the final processed result
   */
  public FrameBuffer processShaders(TextureRegion region, ShaderList shaders) {
    return processShaders(region, shaders, null);
  }

  /**
   * Processes a series of shaders on a given texture region using ping-pong FBO rendering.
   *
   * @param region The texture region to process
   * @param shaders The list of shaders to apply
   * @param pc The position component of the entity (for world bounds calculation), can be null
   * @return The FBO containing the final processed result
   */
  public FrameBuffer processShaders(
      TextureRegion region, ShaderList shaders, PositionComponent pc) {
    // --- 1. Calculate FBO Size and Obtain Buffers ---
    float padding = shaders.getTotalPadding();

    // Required size is sprite size + 2*padding. All in PIXELS.
    float unscaledWidth = region.getRegionWidth();
    float unscaledHeight = region.getRegionHeight();

    // Calculate the base pixel size (before upscaling)
    int baseFboWidth = (int) (unscaledWidth + 2 * padding);
    int baseFboHeight = (int) (unscaledHeight + 2 * padding);

    // Apply Upscale Factor to the FBO dimensions
    int shaderUpscaling = shaders.getMaxUpscaling();
    int fboWidth = baseFboWidth * shaderUpscaling;
    int fboHeight = baseFboHeight * shaderUpscaling;

    // Obtain two FBOs for ping-ponging
    FrameBuffer fboA = FBO_POOL.obtain(fboWidth, fboHeight);
    FrameBuffer fboB = FBO_POOL.obtain(fboWidth, fboHeight);

    // Set NEAREST filtering on the FBO textures to preserve pixel crispness.
    setTextureFiltering(fboA.getColorBufferTexture());
    setTextureFiltering(fboB.getColorBufferTexture());

    // Calculate the projection matrix using the Upscaled FBO dimensions.
    fboProjectionMatrix.setToOrtho2D(0, 0, fboWidth, fboHeight);

    // Initial state
    FrameBuffer currentTarget = fboA;
    Texture currentSourceTexture;
    boolean useFboAAsSource;

    // --- 2. Initial Draw: Sprite -> FBO A ---
    currentTarget.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    fboBatch().setProjectionMatrix(fboProjectionMatrix);
    fboBatch().begin();
    BlendUtils.setBlending(fboBatch());
    fboBatch()
        .draw(
            region,
            padding * shaderUpscaling,
            padding * shaderUpscaling,
            unscaledWidth * shaderUpscaling,
            unscaledHeight * shaderUpscaling);
    fboBatch().end();

    currentTarget.end();
    useFboAAsSource = true;
    currentSourceTexture = currentTarget.getColorBufferTexture();

    // --- 3. Ping-Pong Loop for Shader Passes ---
    for (AbstractShader pass : shaders.getEnabledSorted()) {
      Rectangle worldBounds = new Rectangle(1, 1, 0, 0);
      if (pc != null) {
        worldBounds =
            getFboWorldBounds(pc.position(), pc.scale(), Vector2.of(unscaledWidth, unscaledHeight));
      }
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

      fboBatch().setProjectionMatrix(fboProjectionMatrix);
      fboBatch().begin();
      BlendUtils.setBlending(fboBatch());
      pass.bind(fboBatch(), shaderUpscaling);

      float rotation = 0;
      if (pc != null) {
        rotation = pc.rotation() * MathUtils.degreesToRadians;
      }
      setCommonUniforms(
          fboBatch().getShader(),
          fboRegion.getRegionWidth(),
          fboRegion.getRegionHeight(),
          worldBounds,
          rotation);

      fboBatch().draw(fboRegion, 0, 0, fboWidth, fboHeight);
      pass.unbind(fboBatch());
      fboBatch().end();

      currentTarget.end();

      currentSourceTexture = currentTarget.getColorBufferTexture();
      useFboAAsSource = !useFboAAsSource;
    }

    // --- 4. Final Result and Free the other FBO ---
    FrameBuffer unusedFbo = currentTarget == fboA ? fboB : fboA;
    FBO_POOL.free(unusedFbo);

    return currentTarget;
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
    BlendUtils.setBlending(batch());
    fboRegion.setRegion(fbo.getColorBufferTexture());
    fboRegion.flip(false, true); // Flip back to draw correctly
    batch().draw(fboRegion, 0, 0, width, height);
  }

  /**
   * Draws the final output for an entity, either from its FBO (if shaders were applied) or the
   * original sprite.
   *
   * @param dsd the data record of the entity to draw
   */
  private void drawFinal(final DSData dsd) {
    FrameBuffer finalFbo = entityFboCache.get(dsd.e);

    if (finalFbo != null) {
      drawShaded(dsd, finalFbo);
      // An outlined sprite is kept to cut it out of its outline, see drawOverlays.
      if (hasOutline(dsd.dc.shaders())) outlined.put(dsd, finalFbo);
      else FBO_POOL.free(finalFbo);
      entityFboCache.remove(dsd.e);
    } else {
      draw(dsd);
    }
  }

  /**
   * Draws an entity's shader result at the entity, with the shaders' padding around the sprite.
   *
   * @param dsd the data record of the entity
   * @param shaded the shader result, rendered by {@link #processShaders}
   */
  private void drawShaded(final DSData dsd, final FrameBuffer shaded) {
    Texture fboTexture = shaded.getColorBufferTexture();

    float worldWidth = dsd.dc.getWidth();
    float worldHeight = dsd.dc.getHeight();

    float padding = dsd.dc.shaders().getTotalPadding();
    float paddingX = padding / dsd.dc.getSpriteWidth();
    float paddingY = padding / dsd.dc.getSpriteHeight();

    // Final world size includes padding on all sides
    Vector2 finalWorldSize =
        Vector2.of(
            worldWidth + 2 * paddingX * worldWidth, worldHeight + 2 * paddingY * worldHeight);

    // Scale is being factored into the transformation everywhere except the position, since it is
    // passed directly to the draw method. Thus, we need to factor it in here to offset the
    // padding.
    Point offsetPosition =
        dsd.pc
            .position()
            .translate(
                -paddingX * dsd.pc.scale().x() * worldWidth,
                -paddingY * dsd.pc.scale().y() * worldHeight);

    DrawConfig conf = makeConfig(dsd, finalWorldSize, dsd.pc.scale());
    draw(offsetPosition, fboTexture, conf);
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
    BlendUtils.setBlending(batch());
    fboRegion.setRegion(texture);
    fboRegion.flip(config.mirrored(), true);
    Affine2 transform = makeTransform(position, config);
    batch()
        .setColor(config.tintColor() != -1 ? ColorUtils.pmaColor(config.tintColor()) : Color.WHITE);
    batch().draw(fboRegion, config.size().x(), config.size().y(), transform);
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
    BlendUtils.setBlending(batch());
    Affine2 transform = makeTransform(position, config);
    batch()
        .setColor(config.tintColor() != -1 ? ColorUtils.pmaColor(config.tintColor()) : Color.WHITE);
    batch().draw(sprite, config.size().x(), config.size().y(), transform);
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
    Point mousePos = CursorUtils.positionInWorld();
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
      return CameraSystem.getCameraWorldBounds();
    }
    return getFboWorldBounds(
        dsd.pc.position(), dsd.pc.scale(), Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight()));
  }

  private Rectangle getFboWorldBounds(Point pos, Vector2 scale, Vector2 size) {
    float posX = pos.x();
    float posY = pos.y();
    float worldWidth = scale.x() * size.x();
    float worldHeight = scale.y() * size.y();
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

  /**
   * Draws the outlines of the frame and the world overlays of the registered systems onto the lit
   * scene. Like rendering itself, overlays keep drawing while a pausing dialog has stopped the
   * systems.
   *
   * @param graded the lit scene
   */
  private void drawOverlays(FrameBuffer graded) {
    List<WorldOverlay> overlays =
        ECSManagement.systems().values().stream()
            .filter(s -> s instanceof WorldOverlay)
            .map(WorldOverlay.class::cast)
            .toList();
    // Outlines mark what the player can use, so they keep their colour in the dark: render the
    // sprite with its outlines, cut the sprite out and draw the rest over the lit scene.
    Map<DSData, FrameBuffer> outlines = new LinkedHashMap<>();
    outlined.forEach(
        (dsd, sprite) -> {
          FrameBuffer outline = processShaders(dsd.dc.getSprite(), dsd.dc.shaders(), dsd.pc);
          cutOut(outline, sprite);
          FBO_POOL.free(sprite);
          outlines.put(dsd, outline);
        });
    outlined.clear();
    if (overlays.isEmpty() && outlines.isEmpty()) return;
    graded.begin();
    batch().setProjectionMatrix(CameraSystem.camera().combined);
    BlendUtils.setBlending(batch());
    batch().begin();
    outlines.forEach(this::drawShaded);
    overlays.forEach(o -> o.drawOverlay(batch()));
    batch().setColor(Color.WHITE);
    batch().end();
    graded.end();
    outlines.values().forEach(FBO_POOL::free);
  }

  private static boolean hasOutline(ShaderList shaders) {
    for (AbstractShader shader : shaders.getEnabledSorted()) {
      if (shader instanceof OutlineShader) return true;
    }
    return false;
  }

  private static void drawOutlines(ShaderList shaders, boolean draw) {
    for (AbstractShader shader : shaders.getEnabledSorted()) {
      if (shader instanceof OutlineShader outline) outline.drawsOutline(draw);
    }
  }

  /**
   * Subtracts a sprite from its outlined version, which leaves only the outlines. Both must come
   * from {@link #processShaders} with the same shaders, so they match in size and, as outline
   * shaders pass the sprite through unchanged, in every sprite pixel.
   *
   * @param target the sprite with its outlines
   * @param sprite the sprite without them
   */
  private void cutOut(FrameBuffer target, FrameBuffer sprite) {
    int width = target.getWidth();
    int height = target.getHeight();
    fboProjectionMatrix.setToOrtho2D(0, 0, width, height);
    target.begin();
    fboBatch().setProjectionMatrix(fboProjectionMatrix);
    fboBatch().begin();
    fboBatch().setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
    Gdx.gl.glBlendEquation(GL20.GL_FUNC_REVERSE_SUBTRACT);
    fboRegion.setRegion(sprite.getColorBufferTexture());
    fboRegion.flip(false, true);
    fboBatch().draw(fboRegion, 0, 0, width, height);
    fboBatch().end();
    Gdx.gl.glBlendEquation(GL20.GL_FUNC_ADD);
    target.end();
  }

  /**
   * Draws the floor, the contact shadows of the entities standing on it and the dust their feet
   * kick up, then the walls, which cover shadows and dust behind them.
   */
  private void drawLevel() {
    atmosphere.drawFloor(batch());

    for (int depth : GROUNDED_DEPTHS) {
      List<Entity> group = sortedEntities.get(depth);
      if (group == null) continue;
      group.stream()
          .map(DSData::build)
          .filter(this::shouldDraw)
          .forEach(d -> atmosphere.drawShadow(batch(), d.dc, d.pc));
    }
    atmosphere.drawGroundParticles(batch());
    atmosphere.drawWalls(batch(), secondsElapsed);
  }

  /**
   * Draws where the entities glow, see {@link EmissiveSprites}: the masks of glowing sprites in
   * white, every other sprite in black, in drawing order, so an entity in front of a screen covers
   * its glow.
   *
   * @param drawn the drawn entities, back to front
   */
  private void drawEmissive(List<DSData> drawn) {
    for (DSData d : drawn) {
      Optional<Texture> mask = EmissiveSprites.mask(d.dc);
      emissiveSprite.set(d.dc.getSprite());
      mask.ifPresent(emissiveSprite::setTexture);
      DrawConfig conf = makeConfig(d, Vector2.of(d.dc.getWidth(), d.dc.getHeight()), d.pc.scale());
      // Premultiplied: a half transparent sprite covers or glows half as much.
      float alpha = d.dc.tintColor() == -1 ? 1f : (d.dc.tintColor() & 0xFF) / 255f;
      float glow = mask.isPresent() ? alpha : 0f;
      batch().setColor(glow, glow, glow, alpha);
      batch()
          .draw(
              emissiveSprite,
              conf.size().x(),
              conf.size().y(),
              makeTransform(d.pc.position(), conf));
    }
  }

  /**
   * Draws the bases of the drawn entities for the light pass, see {@link
   * AtmosphereRenderer#beginBases}. Entities behind the level are hidden by it.
   *
   * @param drawn the drawn entities, in drawing order
   */
  private void drawBases(List<DSData> drawn) {
    atmosphere.beginBases(batch());
    for (DSData d : drawn) {
      if (d.dc.depth() <= DepthLayer.Level.depth() || (d.dc.tintColor() & 0xFF) == 0) continue;
      atmosphere.baseColor(batch(), d.dc, d.pc, isUpright(d.dc));
      DrawConfig conf = makeConfig(d, Vector2.of(d.dc.getWidth(), d.dc.getHeight()), d.pc.scale());
      batch()
          .draw(
              d.dc.getSprite(),
              conf.size().x(),
              conf.size().y(),
              makeTransform(d.pc.position(), conf));
    }
    atmosphere.endBases(batch());
  }

  private static boolean isUpright(DrawComponent dc) {
    // Floor decals and plates lie flat; overhead effects and flying entities float.
    return dc.depth() >= DepthLayer.Normal.depth() && dc.depth() < DepthLayer.AbovePlayer.depth();
  }

  private static boolean isGrounded(DrawComponent dc) {
    for (int depth : GROUNDED_DEPTHS) {
      if (dc.depth() == depth) return true;
    }
    return false;
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

    Rectangle cameraBounds = CameraSystem.getCameraWorldBounds();

    Point pos = data.pc.position();
    float width = data.dc.getWidth() * data.pc.scale().x();
    float height = data.dc.getHeight() * data.pc.scale().y();
    List<Point> corners =
        List.of(
            pos.translate(0, 0),
            pos.translate(width, 0),
            pos.translate(0, height),
            pos.translate(width, height));
    Point entityCenter = EntityUtils.getPosition(data.e);

    return Game.currentLevel()
        .map(
            level ->
                cameraBounds.intersects(new Rectangle(width, height, pos.x(), pos.y()))
                    || corners.stream().anyMatch(cameraBounds::contains)
                    || level.tileAt(entityCenter).filter(tile -> tile.visible()).isPresent())
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
    return getInstance().secondsElapsed;
  }

  /**
   * Gets the number of active shaders in the last frame. If the DrawSystem is not found in the Game
   * systems, returns 0.
   *
   * @return the number of active shaders in the last frame
   */
  public static int shadersActiveLastFrame() {
    return getInstance().shadersActiveLastFrame;
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
  }
}
