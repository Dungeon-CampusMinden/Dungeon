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
import com.badlogic.gdx.math.Matrix4; // <-- ADDED: Needed for setting FBO projection
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
import core.level.elements.tile.PitTile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.DrawConfig;
import core.utils.components.draw.FrameBufferPool;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.shader.AbstractShader;
import core.utils.components.path.IPath;
import java.util.*;

/**
 * This system draws the entities on the screen using a two-pass rendering pipeline: 1. FBO Pass:
 * Entities with shader passes are rendered to expanded FrameBuffers (FBOs) using ping-ponging and
 * local transforms (scale/padding). 2. Screen Pass: Level and final entity textures (either FBO
 * output or original sprite) are drawn to the screen using full world transforms.
 *
 * <p>Each entity with a {@link DrawComponent} and a {@link PositionComponent} will be drawn on the
 * screen.
 *
 * <p>This System will also draw the level.
 *
 * <p>The system will get the current animation from the {@link DrawComponent} and will get the next
 * animation frame from the {@link Animation}, and then draw it on the current position stored in
 * the {@link PositionComponent}.
 *
 * <p>This system will not queue animations. This must be done by other systems. The system
 * evaluates the queue and draws the animation with the highest priority in the queue.
 *
 * <p>The DrawSystem can't be paused.
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

  // FIX: This constant is needed to translate FBO pixel sizes back into world unit sizes.
  // Assuming 16x16 pixel sprites are 1x1 world units. Adjust if your game uses a different value.
  private static final float PIXELS_PER_WORLD_UNIT = 16.0f;

  // NEW: Define the scale factor for the internal shader rendering resolution.
  // Shaders will run on a buffer that is this many times larger than required.
  private static final float SHADER_UPSCALING_FACTOR = 2.0f;

  private final TreeMap<Integer, List<Entity>> sortedEntities = new TreeMap<>();

  // NEW: Dependency on the FBO Pool
  private final FrameBufferPool FBO_POOL = FrameBufferPool.getInstance();

  // A reusable TextureRegion for drawing FBO textures (needed for flipping)
  private final TextureRegion fboRegion = new TextureRegion();

  // Dedicated SpriteBatch for rendering locally to FBOs (Pass 1)
  private final SpriteBatch fboBatch = new SpriteBatch();

  // Reusable Matrix4 for setting the FBO batch projection
  private final Matrix4 fboProjectionMatrix = new Matrix4();

  private float secondsElapsed = 0f;

  /** Create a new DrawSystem. */
  public DrawSystem() {
    super(DrawComponent.class, PositionComponent.class);
    onEntityAdd = (e) -> onEntityChanged(e, true);
    onEntityRemove = (e) -> onEntityChanged(e, false);
  }

  /**
   * Get the {@link SpriteBatch} that is used by this system.
   *
   * @return the {@link #BATCH} of the DrawSystem
   */
  public static SpriteBatch batch() {
    return BATCH;
  }

  private void onEntityChanged(Entity changed, boolean added) {
    DSData data = buildDataObject(changed);
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
    DSData data = buildDataObject(entity);

    int oldDepth = data.dc.depth();
    data.dc.depth(depth);

    // Remove old entry
    sortedEntities.get(oldDepth).remove(entity);

    // Add at new depth
    List<Entity> entitiesAtDepth = sortedEntities.computeIfAbsent(depth, k -> new ArrayList<>());
    entitiesAtDepth.add(entity);
  }

  /**
   * Will draw entities in a two-pass render process.
   *
   * <p>Pass 1: Render shader effects into FBOs (Ping-Pong).
   *
   * <p>Pass 2: Draw level, then draw all entities (using FBO texture or original sprite).
   */
  @Override
  public void execute() {
    float deltaTime = Gdx.graphics.getDeltaTime();

    // 1. FBO Shader Generation Pass (only for entities with shaders)
    renderEntitiesPass1(deltaTime);

    // 2. Final Screen Render Pass (Draw Level + Entities)
    BATCH.begin();

    // Draw the level first
    Game.currentLevel().ifPresent(this::drawLevel);

    // Draw entities (FBO textures or original sprites)
    renderEntitiesPass2();

    BATCH.end();

    // Update the FBO Pool to cull unused FBOs
    FBO_POOL.update();

    secondsElapsed += deltaTime;
  }

  /**
   * Pass 1: Renders entities that require shader processing into pooled FBOs using ping-ponging.
   * These FBOs use LOCAL transformations (padding, scale) and ignore world position/rotation.
   */
  private void renderEntitiesPass1(float deltaTime) {
    // Only process entities that have shader passes configured
    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
          .map(this::buildDataObject)
          // Filter for entities with shaders configured
          .filter(dsd -> !dsd.dc.shaders().isEmpty())
          .forEach(dsd -> processShaderPasses(dsd, deltaTime));
    }
  }

  /** Handles the ping-pong rendering loop for a single entity's shader passes. */
  private void processShaderPasses(final DSData dsd, float deltaTime) {
    DrawComponent dc = dsd.dc;
    PositionComponent pc = dsd.pc;

    // --- 1. Calculate FBO Size and Obtain Buffers ---
    // the padding is the maximum padding from all shaders.
    float padding = dsd.getMaxPadding();

    // Required size is sprite size * scale + 2*padding. All in PIXELS.
    float scaledWidth = dc.getSpriteWidth() * pc.scale().x();
    float scaledHeight = dc.getSpriteHeight() * pc.scale().y();

    // Calculate the base pixel size (before upscaling)
    int baseFboWidth = (int) (scaledWidth + 2 * padding);
    int baseFboHeight = (int) (scaledHeight + 2 * padding);

    // Apply Upscale Factor to the FBO dimensions
    int fboWidth = (int) (baseFboWidth * SHADER_UPSCALING_FACTOR);
    int fboHeight = (int) (baseFboHeight * SHADER_UPSCALING_FACTOR);

    // Obtain two FBOs for ping-ponging
    FrameBuffer fboA = FBO_POOL.obtain(fboWidth, fboHeight);
    FrameBuffer fboB = FBO_POOL.obtain(fboWidth, fboHeight);

    // CRITICAL FIX: Set NEAREST filtering on the FBO textures to preserve pixel crispness.
    fboA.getColorBufferTexture()
        .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    fboB.getColorBufferTexture()
        .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    // Set the projection matrix for the local FBO batch.
    fboProjectionMatrix.setToOrtho2D(0, 0, fboWidth, fboHeight);
    fboBatch.setProjectionMatrix(fboProjectionMatrix);

    // Initial state
    FrameBuffer currentTarget = fboA;
    Texture currentSourceTexture = dc.getSprite().getTexture();
    boolean useFboAAsSource = false; // Flag to track which FBO is the source

    // Sprite is fine to take as TextureRegion, its internal state is unmodified.
    TextureRegion initialRegion = dc.getSprite();

    // --- 2. Initial Draw: Sprite -> FBO A ---
    // This sets up the initial texture within the expanded buffer.
    currentTarget.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    fboBatch.begin(); // Use dedicated FBO batch
    // LOCAL TRANSFORM: Draw the original texture region at the padded offset (position)
    // using its scaled size, **both multiplied by the upscaling factor**.
    fboBatch.draw(
        initialRegion,
        padding * SHADER_UPSCALING_FACTOR,
        padding * SHADER_UPSCALING_FACTOR,
        scaledWidth * SHADER_UPSCALING_FACTOR,
        scaledHeight * SHADER_UPSCALING_FACTOR);
    fboBatch.end(); // Use dedicated FBO batch

    currentTarget.end();
    useFboAAsSource = true;
    currentSourceTexture = currentTarget.getColorBufferTexture();

    // --- 3. Ping-Pong Loop for Shader Passes ---
    for (int i = 0; i < dsd.dc.shaders().size(); i++) {
      AbstractShader pass = dsd.dc.shaders().get(i);

      // Determine the next target FBO
      currentTarget = useFboAAsSource ? fboB : fboA;

      // Start rendering to the target FBO
      currentTarget.begin();
      Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

      fboBatch.begin(); // Use dedicated FBO batch

      // Bind the custom shader and uniforms
      pass.bind(fboBatch, deltaTime);
      setCommonUniforms(fboBatch.getShader(), fboRegion);

      // Draw the current source texture to the target FBO
      // Note: FBO textures are typically rendered upside down. We use TextureRegion
      // to correct the flip before drawing to the next FBO.
      fboRegion.setRegion(currentSourceTexture);
      fboRegion.flip(false, true);

      fboBatch.draw(fboRegion, 0, 0, fboWidth, fboHeight);

      // Unbind the shader
      pass.unbind(fboBatch);
      fboBatch.end();

      currentTarget.end();

      // Prepare for the next pass
      currentSourceTexture = currentTarget.getColorBufferTexture();
      useFboAAsSource = !useFboAAsSource; // Swap the source flag
    }

    // --- 4. Store Final Result and Free the other FBO ---
    dsd.dc.frameBuffer(currentTarget);

    // The one that was NOT the final target is returned to the pool
    FrameBuffer unusedFbo = currentTarget == fboA ? fboB : fboA;
    FBO_POOL.free(unusedFbo);
  }

  /** Sets any common uniforms needed by all shaders. */
  private void setCommonUniforms(ShaderProgram shader, TextureRegion texture) {
    shader.setUniformf("u_time", secondsElapsed);
    shader.setUniformf("u_resolution", texture.getRegionWidth(), texture.getRegionHeight());
    Point mousePos = SkillTools.cursorPositionAsPoint();
    shader.setUniformf("u_mouse", mousePos.x(), mousePos.y());
    shader.setUniformf(
        "u_texelSize", 1f / texture.getRegionWidth(), 1f / texture.getRegionHeight());
    shader.setUniformf(
        "u_aspect", (float) texture.getRegionWidth() / (float) texture.getRegionHeight());
  }

  /**
   * Pass 2: Draws all entities to the screen. If an entity has an outputFbo, draw the FBO texture;
   * otherwise, draw the original sprite.
   */
  private void renderEntitiesPass2() {
    // Use the existing depth and Y-sorting logic
    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
          .map(this::buildDataObject)
          .sorted(Comparator.comparingDouble((DSData d) -> -EntityUtils.getPosition(d.e).y()))
          .filter(this::shouldDraw)
          .forEach(this::drawFinal);
    }
  }

  /** Determines whether to draw the FBO texture or the original sprite, and frees the FBO. */
  private void drawFinal(final DSData dsd) {
    dsd.dc.update();

    FrameBuffer finalFbo = dsd.dc.frameBuffer();

    if (finalFbo != null) {

      // --- Draw FBO Texture (Shader Result) ---
      Texture fboTexture = finalFbo.getColorBufferTexture();

      // FIX 1: Convert FBO dimensions (pixels) to World Units
      // Divide the FBO size by the upscaling factor to get the base pixel size (sprite + padding),
      // then convert that base pixel size to world units.
      float fboWidthWorldUnits =
          finalFbo.getWidth() / SHADER_UPSCALING_FACTOR / PIXELS_PER_WORLD_UNIT;
      float fboHeightWorldUnits =
          finalFbo.getHeight() / SHADER_UPSCALING_FACTOR / PIXELS_PER_WORLD_UNIT;

      Vector2 fboSize = Vector2.of(fboWidthWorldUnits, fboHeightWorldUnits);
      float padding = dsd.getMaxPadding(); // Padding is in PIXELS

      // FIX 2: Convert padding (pixels) to World Units
      float paddingWorldUnits = padding / PIXELS_PER_WORLD_UNIT;

      // Translate the world position back by the padding amount (in world units)
      Point offsetPosition = dsd.pc.position().translate(-paddingWorldUnits, -paddingWorldUnits);

      // IMPORTANT: scale is (1, 1) because the texture is already scaled in Pass 1
      // and we are drawing it using its final, converted world size.
      DrawConfig conf =
          new DrawConfig(
              Vector2.ZERO,
              fboSize,
              Vector2.of(1, 1),
              dsd.dc.tintColor(),
              dsd.dc.currentAnimation().getConfig().mirrored(),
              dsd.pc.rotation());

      drawFboTexture(offsetPosition, fboTexture, conf);

      // Return the temporary FBO reference to the pool and clear the component
      FBO_POOL.free(finalFbo);
      dsd.dc.frameBuffer(null);

    } else {
      // --- Draw Original Sprite (No Shader) ---
      Sprite sprite = dsd.dc.getSprite();
      DrawConfig conf =
          new DrawConfig(
              Vector2.ZERO,
              Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight()),
              dsd.pc.scale(),
              dsd.dc.tintColor(),
              dsd.dc.currentAnimation().getConfig().mirrored(),
              dsd.pc.rotation());

      draw(dsd.pc.position(), sprite, conf);
    }
  }

  /**
   * Draws the FBO texture at the world position. This method handles the vertical flip required for
   * FBO textures and applies the world transform using the full parameter draw overload for better
   * control over dimensions.
   */
  public void drawFboTexture(final Point position, final Texture texture, final DrawConfig config) {
    // Apply tint color if specified
    if (config.tintColor() != -1) {
      BATCH.setColor(new Color(config.tintColor()));
    } else {
      BATCH.setColor(Color.WHITE);
    }

    // Use the reusable TextureRegion to ensure the FBO texture is drawn correctly
    fboRegion.setRegion(texture);
    // FBO textures are typically upside down; we flip here for correct display on screen
    fboRegion.flip(false, true);

    // Calculate rotation origin (center of the FBO texture, which is the expanded sprite)
    float originX = config.size().x() / 2f;
    float originY = config.size().y() / 2f;

    // Use the full parameter draw overload to explicitly set the size and rotation.
    // config.size() is now in world units, which BATCH expects.
    BATCH.draw(
        fboRegion,
        position.x(), // x (bottom left corner of the FBO texture in world space)
        position.y(), // y
        originX, // originX (for rotation)
        originY, // originY (for rotation)
        config.size().x(), // width (The calculated FBO width in world units)
        config.size().y(), // height
        config.scale().x(), // scaleX (Should be 1.0f from drawFinal)
        config.scale().y(), // scaleY (Should be 1.0f from drawFinal)
        config.rotation() // rotation
        );
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
    // New check: first check if entity.dc is visible. Otherwise check if any tiles under the
    // corners of the sprite are visible.
    if (!data.dc.isVisible()) {
      return false;
    }

    Point pos = data.pc.position();
    // Use data.dc.getSpriteWidth() and similar
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
                          return t != null && t.visible() && !isTilePitAndOpen(t);
                        }))
        .orElse(false);
  }

  private void draw(final DSData dsd) {
    dsd.dc.update();
    Sprite sprite = dsd.dc.getSprite();
    DrawConfig conf =
        new DrawConfig(
            Vector2.ZERO,
            Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight()),
            dsd.pc.scale(),
            dsd.dc.tintColor(),
            dsd.dc.currentAnimation().getConfig().mirrored(),
            dsd.pc.rotation());

    draw(dsd.pc.position(), sprite, conf);
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
   * Builds the data record used by this system.
   *
   * @param entity The entity with a DrawComponent and a PositionComponent
   * @return The data record
   */
  private DSData buildDataObject(final Entity entity) {
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

  private void drawLevel(ILevel currentLevel) {
    if (currentLevel == null) throw new IllegalArgumentException("Level to draw can´t be null.");

    Tile[][] layout = currentLevel.layout();
    for (Tile[] tiles : layout) {
      for (int x = 0; x < layout[0].length; x++) {
        Tile t = tiles[x];
        if (t.levelElement() != LevelElement.SKIP && !isTilePitAndOpen(t) && t.visible()) {
          IPath texturePath = t.texturePath();
          draw(t.position(), texturePath, new DrawConfig());
        }
      }
    }
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
    sprite.setFlip(config.mirrored(), false);

    // Calculate transformations
    Affine2 transform = new Affine2();

    transform.setToTranslation(position.x(), position.y());

    // Scale first while origin is in the bottom-left
    transform.scale(config.scale().x(), config.scale().y());

    // Then rotate around the middle
    transform.translate(config.size().x() / 2f, config.size().y() / 2f);
    transform.rotate(config.rotation());
    transform.translate(-config.size().x() / 2f, -config.size().y() / 2f);

    // Apply tint color if specified
    if (config.tintColor() != -1) {
      BATCH.setColor(new Color(config.tintColor()));
    } else {
      BATCH.setColor(Color.WHITE);
    }
    BATCH.draw(sprite, config.size().x(), config.size().y(), transform);
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

  /**
   * Checks if the provided tile is an instance of PitTile and if it's open.
   *
   * @param tile The tile to check.
   * @return true if the tile is an instance of PitTile, and it's open, false otherwise.
   */
  private boolean isTilePitAndOpen(final Tile tile) {
    if (tile instanceof PitTile) {
      return ((PitTile) tile).isOpen();
    } else {
      return false;
    }
  }

  private record DSData(Entity e, DrawComponent dc, PositionComponent pc) {
    float getMaxPadding() {
      return dc.shaders().stream()
          .max(Comparator.comparingDouble(AbstractShader::getPadding))
          .map(AbstractShader::getPadding)
          .orElse(0f);
    }
  }
}
