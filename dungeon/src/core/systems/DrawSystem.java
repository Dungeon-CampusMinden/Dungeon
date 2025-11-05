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

  private final TreeMap<Integer, List<Entity>> sortedEntities = new TreeMap<>();

  private final FrameBufferPool FBO_POOL = FrameBufferPool.getInstance();
  // Dedicated SpriteBatch for rendering locally to FBOs (Pass 1)
  private final SpriteBatch fboBatch = new SpriteBatch();
  private final Matrix4 fboProjectionMatrix = new Matrix4();
  private final TextureRegion fboRegion = new TextureRegion();

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

    sortedEntities.get(oldDepth).remove(entity);
    List<Entity> entitiesAtDepth = sortedEntities.computeIfAbsent(depth, k -> new ArrayList<>());
    entitiesAtDepth.add(entity);
  }

  /**
   * Will draw entities in a two-pass render process.
   *
   * <p>Pass 1: Render shader effects into FBOs (Ping-Pong).
   *
   * <p>Pass 2: Draw level and final entity textures (either FBO output or original sprite) are
   * drawn to the screen using full world transforms.
   */
  @Override
  public void execute() {
    // Pass 1: Render shaders to FBOs
    renderEntitiesPass1();

    // Pass 2: Render to screen
    BATCH.begin();
    Game.currentLevel().ifPresent(this::drawLevel);
    renderEntitiesPass2();
    BATCH.end();

    FBO_POOL.update();
    secondsElapsed += Gdx.graphics.getDeltaTime();
  }

  /**
   * Pass 1: Renders entities that require shader processing into pooled FBOs using ping-ponging.
   * These FBOs use LOCAL transformations (padding, scale) and ignore world position/rotation.
   */
  private void renderEntitiesPass1() {
    // Only process entities that have shader passes configured
    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
          .map(this::buildDataObject)
          .filter(this::shouldDraw)
          // Filter for entities with shaders configured
          .filter(dsd -> !dsd.dc.shaders().isEmpty())
          .forEach(this::processShaderPasses);
    }
  }

  /**
   * Processes the shader passes for a single entity by rendering to FBOs using ping-ponging.
   *
   * @param dsd the data record of the entity to process
   */
  private void processShaderPasses(final DSData dsd) {
    DrawComponent dc = dsd.dc;
    PositionComponent pc = dsd.pc;

    // --- 1. Calculate FBO Size and Obtain Buffers ---
    // the padding is the maximum padding from all shaders.
    float padding = dsd.getTotalPadding();

    // Required size is sprite size * scale + 2*padding. All in PIXELS.
    float scaledWidth = dc.getSpriteWidth() * pc.scale().x();
    float scaledHeight = dc.getSpriteHeight() * pc.scale().y();

    // Calculate the base pixel size (before upscaling)
    int baseFboWidth = (int) (scaledWidth + 2 * padding);
    int baseFboHeight = (int) (scaledHeight + 2 * padding);

    // Apply Upscale Factor to the FBO dimensions
    int shaderUpscaling = dsd.getMaxUpscale();
    int fboWidth = (int) (baseFboWidth * shaderUpscaling);
    int fboHeight = (int) (baseFboHeight * shaderUpscaling);

    // Obtain two FBOs for ping-ponging
    FrameBuffer fboA = FBO_POOL.obtain(fboWidth, fboHeight);
    FrameBuffer fboB = FBO_POOL.obtain(fboWidth, fboHeight);

    // Set NEAREST filtering on the FBO textures to preserve pixel crispness.
    fboA.getColorBufferTexture()
        .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    fboB.getColorBufferTexture()
        .setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

    // Calculate the projection matrix using the UPscaled FBO dimensions.
    fboProjectionMatrix.setToOrtho2D(0, 0, fboWidth, fboHeight);

    // Initial state
    FrameBuffer currentTarget = fboA;
    Texture currentSourceTexture;
    boolean useFboAAsSource = false; // Flag to track which FBO is the source

    // Sprite is fine to take as TextureRegion, its internal state is unmodified.
    TextureRegion initialRegion = dc.getSprite();

    // --- 2. Initial Draw: Sprite -> FBO A ---
    // This sets up the initial texture within the expanded buffer using the default batch shader.
    currentTarget.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Set the projection matrix immediately before starting the batch.
    fboBatch.setProjectionMatrix(fboProjectionMatrix);
    fboBatch.begin(); // Use dedicated FBO batch
    // LOCAL TRANSFORM: Draw the original texture region at the padded offset (position)
    // using its scaled size, **both multiplied by the upscaling factor**.
    fboBatch.draw(
        initialRegion,
        padding * shaderUpscaling,
        padding * shaderUpscaling,
        scaledWidth * shaderUpscaling,
        scaledHeight * shaderUpscaling);
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

      // Re-set the projection matrix to enforce local-space rendering
      fboBatch.setProjectionMatrix(fboProjectionMatrix);

      fboBatch.begin(); // Use dedicated FBO batch

      // Set TextureRegion to source, and flip it for FBO drawing.
      fboRegion.setRegion(currentSourceTexture);
      fboRegion.flip(false, true); // Correct flip

      // Bind the custom shader and uniforms
      pass.bind(fboBatch);
      setCommonUniforms(fboBatch.getShader(), fboRegion);

      // Draw the current source texture to the target FBO
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

    FrameBuffer unusedFbo = currentTarget == fboA ? fboB : fboA;
    FBO_POOL.free(unusedFbo);
  }

  /**
   * Sets any common uniforms needed by all shaders.
   *
   * @param shader The shader program to set uniforms for
   * @param texture The texture region being processed
   */
  private void setCommonUniforms(ShaderProgram shader, TextureRegion texture) {
    shader.setUniformf("u_time", secondsElapsed);

    int textureWidth = texture.getRegionWidth();
    int textureHeight = texture.getRegionHeight();
    shader.setUniformf("u_resolution", textureWidth, textureHeight);
    shader.setUniformf("u_texelSize", 1.0f / textureWidth, 1.0f / textureHeight);
    shader.setUniformf("u_aspect", 1.0f, (float) textureWidth / (float) textureHeight);

    // Mouse position in screen space
    Point mousePos = SkillTools.cursorPositionAsPoint();
    Vector3 unprojected = CameraSystem.camera().project(new Vector3(mousePos.x(), mousePos.y(), 0));
    shader.setUniformf("u_mouse", unprojected.x, unprojected.y);
  }

  /**
   * Pass 2: Draws all entities to the screen. If an entity has an outputFbo, draw the FBO texture;
   * otherwise, draw the original sprite.
   */
  private void renderEntitiesPass2() {
    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
          .map(this::buildDataObject)
          .sorted(Comparator.comparingDouble((DSData d) -> -EntityUtils.getPosition(d.e).y()))
          .filter(this::shouldDraw)
          .forEach(this::drawFinal);
    }
  }

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

      // Convert FBO dimensions (pixels) to World Units
      // Divide the FBO size by the upscaling factor to get the base pixel size (sprite + padding),
      // then convert that base pixel size to world units.
      int shaderUpscaling = dsd.getMaxUpscale();
      float fboWidthWorldUnits =
          (float) finalFbo.getWidth() / shaderUpscaling / dsd.getUnitSizeInPixels();
      float fboHeightWorldUnits =
          (float) finalFbo.getHeight() / shaderUpscaling / dsd.getUnitSizeInPixels();

      Vector2 fboSize = Vector2.of(fboWidthWorldUnits, fboHeightWorldUnits);
      float padding = dsd.getTotalPadding(); // Padding is in PIXELS

      // Convert padding (pixels) to World Units
      float paddingWorldUnits = padding / dsd.getUnitSizeInPixels();

      // Translate the world position back by the padding amount (in world units)
      Point offsetPosition = dsd.pc.position().translate(-paddingWorldUnits, -paddingWorldUnits);

      // IMPORTANT: scale is (1, 1) because the texture is already scaled in Pass 1
      // and we are drawing it using its final, converted world size.
      DrawConfig conf =
          new DrawConfig(
              Vector2.ZERO,
              fboSize,
              Vector2.ONE,
              dsd.dc.tintColor(),
              dsd.dc.currentAnimation().getConfig().mirrored(),
              dsd.pc.rotation());

      drawFboTexture(offsetPosition, fboTexture, conf);

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
   * @param texture the FBO texture to draw
   * @param config the {@link DrawConfig} controlling the drawing parameters
   */
  public void drawFboTexture(final Point position, final Texture texture, final DrawConfig config) {
    if (config.tintColor() != -1) {
      BATCH.setColor(new Color(config.tintColor()));
    } else {
      BATCH.setColor(Color.WHITE);
    }

    // Use the reusable TextureRegion
    fboRegion.setRegion(texture);

    // FBO is upside down (vertical flip is mandatory).
    fboRegion.flip(config.mirrored(), true);

    // Calculate rotation origin (center of the FBO texture, which is the expanded sprite)
    float originX = config.size().x() / 2f;
    float originY = config.size().y() / 2f;

    // --- Standard Affine2 Transformation ---
    Affine2 transform = new Affine2();
    transform.setToTranslation(position.x(), position.y());

    // Scale first while origin is in the bottom-left
    transform.scale(config.scale().x(), config.scale().y());

    // Then rotate around the middle
    transform.translate(originX, originY);
    transform.rotate(config.rotation());
    transform.translate(-originX, -originY);

    BATCH.draw(fboRegion, config.size().x(), config.size().y(), transform);
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
      float spriteWidth = dc.getSpriteWidth();
      float spriteHeight = dc.getSpriteHeight();
      if (spriteWidth < spriteHeight) {
        return spriteWidth * pc.scale().x();
      } else {
        return spriteHeight * pc.scale().y();
      }
    }
  }
}
