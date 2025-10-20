package core.utils.components.draw.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.io.*;
import java.util.*;

/**
 * Represents an animation consisting of one or more {@link Sprite}s.
 *
 * <p>An {@code Animation} can be created from:
 *
 * <ul>
 *   <li>a single image file,
 *   <li>a folder of images,
 *   <li>a spritesheet with an accompanying {@link SpritesheetConfig},
 *   <li>or a list of explicit paths to images.
 * </ul>
 *
 * <p>The animation uses an {@link AnimationConfig} to determine scaling, frame duration, looping
 * behavior, and optionally spritesheet layout.
 *
 * <p>Animation frames are stored internally as {@link Sprite} objects and can be updated
 * frame-by-frame with {@link #update()}.
 */
public class Animation implements Serializable, Cloneable {
  @Serial private static final long serialVersionUID = 1L;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Animation.class);

  /** Path to the missing texture fallback image. */
  public static final IPath MISSING_TEXTURE_PATH = new SimpleIPath("animation/missing_texture.png");

  /** How this animation was sourced (single, multi, spritesheet). */
  private enum SourceType {
    SINGLE_OR_MULTI,
    SPRITESHEET
  }

  private AnimationConfig config;

  /** Logical world size (computed from sprite pixel size and scale). */
  private float width = 1;

  private float height = 1;

  /** Sprite pixel-to-world scale used to compute width/height. */
  private float spriteScale;

  /** Current frame counter. Serializable. */
  private int frameCount;

  /** Lazily-loaded runtime sprites. Not serializable. */
  private transient Sprite[] sprites;

  /** Indicates whether sprites are loaded. */
  private transient boolean loaded;

  /** Source description for lazy loading. */
  private final SourceType sourceType;

  /** For SINGLE_OR_MULTI: the explicit frame image paths (one per frame). */
  private List<IPath> framePaths;

  /** For SPRITESHEET: the path to the spritesheet image. */
  private IPath sheetPath;

  /**
   * Create a new animation from a path.
   *
   * @param path An {@link IPath} to either a single image or a folder of images.
   * @param config The configuration to use for this animation.
   * @throws IllegalArgumentException if the path is null or empty.
   */
  public Animation(IPath path, AnimationConfig config) {
    if (path == null || path.pathString().isEmpty())
      throw new IllegalArgumentException("path can't be null or empty");
    this.config = config == null ? new AnimationConfig() : config;

    // Resolve single vs spritesheet based on config presence
    String resolved = resolveImplicitFilePath(path.pathString());
    IPath exactPath = new SimpleIPath(resolved);

    if (this.config.config().isPresent()) {
      // Spritesheet path retained; lazy-load frames
      this.sourceType = SourceType.SPRITESHEET;
      this.sheetPath = exactPath;
      // Optional eager compute using known sprite px dimensions from config
      SpritesheetConfig ssc = this.config.config().get();
      calculateWorldSize(ssc.spriteWidth(), ssc.spriteHeight());
      // Attempt eager load only if GL is available
      tryEagerLoad();
    } else {
      // Single image treated as a single-frame multi source
      this.sourceType = SourceType.SINGLE_OR_MULTI;
      this.framePaths = new ArrayList<>();
      this.framePaths.add(exactPath);
      // Try to read actual texture size if available; else fallback
      if (canUseTextures() || TextureMap.instance().containsKey(exactPath.pathString())) {
        Texture t = TextureMap.instance().textureAt(exactPath);
        calculateWorldSize(t.getWidth(), t.getHeight());
      } else {
        // Fallback logical size for headless/server
        calculateWorldSize(16, 16);
      }
      tryEagerLoad();
    }
  }

  /**
   * Create a new animation with default configuration.
   *
   * @param path An {@link IPath} to either a single image or a folder of images.
   */
  public Animation(IPath path) {
    this(path, new AnimationConfig());
  }

  /**
   * Create a new animation from multiple paths.
   *
   * @param paths A list of image paths to use.
   * @param config The configuration to use for this animation.
   * @throws IllegalArgumentException if paths is null or empty.
   */
  public Animation(List<IPath> paths, AnimationConfig config) {
    if (paths == null || paths.isEmpty())
      throw new IllegalArgumentException("paths can't be null or empty");
    this.config = config == null ? new AnimationConfig() : config;

    this.sourceType = SourceType.SINGLE_OR_MULTI;
    this.framePaths = new ArrayList<>(paths);

    // Size from first frame if possible
    if (canUseTextures() || TextureMap.instance().containsKey(paths.get(0).pathString())) {
      Texture t = TextureMap.instance().textureAt(paths.get(0));
      calculateWorldSize(t.getWidth(), t.getHeight());
    } else {
      calculateWorldSize(16, 16);
    }
    tryEagerLoad();
  }

  /**
   * Create a new animation from multiple paths with default configuration.
   *
   * @param paths A varargs array of image paths.
   */
  public Animation(IPath... paths) {
    this(Arrays.asList(paths), new AnimationConfig());
  }

  /**
   * Create a new animation from a list of paths with default configuration.
   *
   * @param paths A list of image paths.
   */
  public Animation(List<IPath> paths) {
    this(paths, new AnimationConfig());
  }

  /**
   * Get the current sprite frame based on the frame counter and configuration.
   *
   * @return The current {@link Sprite}.
   */
  public Sprite getSprite() {
    ensureLoaded();
    int spriteIndex = frameCount / config.framesPerSprite();
    if (config.isLooping()) {
      spriteIndex = spriteIndex % sprites.length;
    } else {
      spriteIndex = Math.min(spriteIndex, sprites.length - 1);
    }
    return sprites[spriteIndex];
  }

  /**
   * Get the logical width of the animation in world units.
   *
   * @return The width of the animation.
   */
  public float getWidth() {
    return width;
  }

  /**
   * Get the logical height of the animation in world units.
   *
   * @return The height of the animation.
   */
  public float getHeight() {
    return height;
  }

  /**
   * Get the scale factor in the X direction.
   *
   * @return The scale factor in the X direction.
   */
  public float getScaleX() {
    return config.scaleX();
  }

  /**
   * Get the scale factor in the Y direction. If scaleY is not set, return scaleX.
   *
   * @return The scale factor in the Y direction.
   */
  public float getScaleY() {
    return config.scaleY() == 0 ? config.scaleX() : config.scaleY();
  }

  /**
   * Get the pixel width of the underlying sprite frame.
   *
   * @return The sprite width in pixels.
   */
  public float getSpriteWidth() {
    return width / spriteScale / getScaleX();
  }

  /**
   * Get the pixel height of the underlying sprite frame.
   *
   * @return The sprite height in pixels.
   */
  public float getSpriteHeight() {
    return height / spriteScale / getScaleY();
  }

  /**
   * Check whether this animation is set to loop.
   *
   * @return true if the animation loops, false otherwise.
   */
  public boolean isLooping() {
    return config.isLooping();
  }

  /**
   * Check whether this animation has finished playing.
   *
   * @return true if the animation has finished, false otherwise.
   */
  public boolean isFinished() {
    ensureLoaded();
    int spriteIndex = frameCount / config.framesPerSprite();
    return spriteIndex >= sprites.length;
  }

  /**
   * Update the animation by advancing the frame counter.
   *
   * @return The updated current {@link Sprite}.
   */
  public Sprite update() {
    frameCount++;
    return getSprite();
  }

  /**
   * Get the current frame counter.
   *
   * @return The frame counter.
   */
  public int frameCount() {
    return frameCount;
  }

  /**
   * Set the frame counter.
   *
   * @param frameCount The new frame count value.
   */
  public void frameCount(int frameCount) {
    this.frameCount = frameCount;
  }

  /**
   * Get the {@link AnimationConfig} used by this animation.
   *
   * @return The animation configuration.
   */
  public AnimationConfig getConfig() {
    return config;
  }

  /** Unload sprites to free memory; they will be reloaded on next use. */
  public void unload() {
    sprites = null;
    loaded = false;
  }

  /**
   * Sets whether the animation is mirrored horizontally.
   *
   * @param mirrored whether to mirror the animation
   * @return this animation instance
   */
  public Animation mirrored(boolean mirrored) {
    config.mirrored(mirrored);
    return this;
  }

  /**
   * Get whether the animation is mirrored horizontally.
   *
   * @return whether the animation is mirrored
   */
  public boolean mirrored() {
    return config.mirrored();
  }

  @Override
  public String toString() {
    return "Animation{"
        + "width="
        + width
        + ", height="
        + height
        + ", frameCount="
        + frameCount
        + ", loaded="
        + loaded
        + ", sourceType="
        + sourceType
        + ", frames="
        + (sourceType == SourceType.SINGLE_OR_MULTI
            ? (framePaths == null ? 0 : framePaths.size())
            : (config.config().map(c -> c.rows() * c.columns()).orElse(0)))
        + ", config="
        + config
        + '}';
  }

  /**
   * Load multiple animations from a spritesheet and its accompanying JSON configuration file.
   *
   * <p>The JSON file must be placed next to the spritesheet and have the same base name. It defines
   * a map of animation names to {@link AnimationConfig} objects.
   *
   * @param path Path to the spritesheet image or folder.
   * @return A map of animation names to {@link Animation} instances, or null if no config was
   *     found.
   */
  public static Map<String, Animation> loadAnimationSpritesheet(IPath path) {
    String pathString = path.pathString();
    String jsonPath;

    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      String dirName = pathString.replaceAll("/$", "");
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1);
      jsonPath = dirName + "/" + baseName + ".json";
      pathString = dirName + "/" + baseName + ".png";
    } else {
      jsonPath = pathString.replaceAll("\\.(png|jpg|jpeg)$", ".json");
    }

    if (Gdx.files != null && Gdx.files.internal(pathString).exists()) {
      if (canUseTextures()) {
        TextureMap.instance().textureAt(new SimpleIPath(pathString));
      }
    } else {
      throw new IllegalArgumentException("Image file not found: " + pathString);
    }

    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);
    if (configs == null) return null;

    Map<String, Animation> animations = new HashMap<>();
    for (Map.Entry<String, AnimationConfig> entry : configs.entrySet()) {
      String name = entry.getKey();
      AnimationConfig cfg = entry.getValue();
      animations.put(name, new Animation(path, cfg));
    }

    return animations;
  }

  private static boolean canUseTextures() {
    return Gdx.gl != null;
  }

  private void ensureLoaded() {
    if (loaded && sprites != null) return;

    if (sourceType == SourceType.SINGLE_OR_MULTI) {
      loadSpritesFromPaths(framePaths);
    } else {
      loadSpritesFromSpritesheet(sheetPath);
    }
    loaded = true;
  }

  private void tryEagerLoad() {
    if (canUseTextures()) {
      try {
        ensureLoaded();
      } catch (Exception e) {
        // Fail-safe: keep lazy; avoid crashing headless/server
        LOGGER.debug("Eager load failed; will lazy-load later: {}", e.getMessage());
        sprites = null;
        loaded = false;
      }
    }
  }

  /**
   * Load sprites directly from a list of image paths.
   *
   * @param paths A list of image paths.
   */
  private void loadSpritesFromPaths(List<IPath> paths) {
    if (paths == null || paths.isEmpty())
      throw new IllegalStateException("No frame paths provided");

    sprites = new Sprite[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      if (TextureMap.instance().containsKey(paths.get(i).pathString()) || canUseTextures()) {
        sprites[i] = new Sprite(TextureMap.instance().textureAt(paths.get(i)));
      } else {
        sprites[i] = new Sprite();
      }
    }

    int textWidth;
    int textHeight;

    if (TextureMap.instance().containsKey(paths.get(0).pathString()) || canUseTextures()) {
      Texture t = TextureMap.instance().textureAt(paths.get(0));
      textWidth = t.getWidth();
      textHeight = t.getHeight();
    } else {
      textWidth = 16;
      textHeight = 16;
    }

    calculateWorldSize(textWidth, textHeight);
  }

  /**
   * Load sprites from a spritesheet using {@link SpritesheetConfig}.
   *
   * @param path Path to the spritesheet image.
   */
  private void loadSpritesFromSpritesheet(IPath path) {
    Texture spritesheet = null;
    if (TextureMap.instance().containsKey(path.pathString()) || canUseTextures()) {
      spritesheet = TextureMap.instance().textureAt(path);
    }

    SpritesheetConfig ssc =
        config
            .config()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "SpritesheetConfig expected but not present in config"));
    int sWidth = ssc.spriteWidth();
    int sHeight = ssc.spriteHeight();
    int offsetX = ssc.x();
    int offsetY = ssc.y();

    sprites = new Sprite[ssc.rows() * ssc.columns()];
    for (int y = 0; y < ssc.rows(); y++) {
      for (int x = 0; x < ssc.columns(); x++) {
        int index = y * ssc.columns() + x;
        if (canUseTextures() && spritesheet != null) {
          sprites[index] =
              new Sprite(
                  new TextureRegion(
                      spritesheet, offsetX + sWidth * x, offsetY + sHeight * y, sWidth, sHeight));
        } else {
          sprites[index] = new Sprite();
        }
      }
    }

    calculateWorldSize(sWidth, sHeight);
  }

  /**
   * Sets the width and height of the animation. Assumes the smallest dimension to be 1 tile in the
   * world (before scale is applied).
   *
   * @param spriteWidth the width of the sprite
   * @param spriteHeight the height of the sprite
   */
  private void calculateWorldSize(int spriteWidth, int spriteHeight) {
    spriteScale = (float) 1 / Math.min(spriteWidth, spriteHeight);
    width = (float) spriteWidth * spriteScale * getScaleX();
    height = (float) spriteHeight * spriteScale * getScaleY();
  }

  private static String resolveImplicitFilePath(String pathString) {
    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      String dirName = pathString.replaceAll("/$", "");
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1);
      return dirName + "/" + baseName + ".png";
    }
    return pathString;
  }

  @Serial
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  @Serial
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.sprites = null;
    this.loaded = false;
    // width/height will be recomputed on load; keep existing values as hints
  }

  @Override
  public Animation clone() {
    try {
      Animation cloned = (Animation) super.clone();

      // Copy fields manually
      if (this.config != null) {
        cloned.config = config.clone();
      }

      // Clone sprite array
      if (this.sprites != null) {
        cloned.sprites = new Sprite[this.sprites.length];
        for (int i = 0; i < this.sprites.length; i++) {
          cloned.sprites[i] = this.sprites[i] != null ? new Sprite(this.sprites[i]) : null;
        }
      }

      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Failed to clone", e);
    }
  }
}
