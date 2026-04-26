package core.utils.components.draw.animation;

import core.platform.Platform;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Represents an animated sprite with support for multiple animation sources and playback control.
 *
 * <p>Animation encapsulates sprite animation data and provides methods for frame management,
 * playback control, and scaling. It supports three source types:
 *
 * <ul>
 *   <li>Single images or multiple individual image files (one frame per file)
 *   <li>Spritesheets with a JSON configuration file defining frame regions
 * </ul>
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Lazy loading of animation frames for memory efficiency
 *   <li>Configurable frame timing and playback behavior
 *   <li>Support for looping and non-looping animations
 *   <li>Horizontal mirroring/flipping capability
 *   <li>Serialization and cloning support
 *   <li>Automatic world size calculation based on sprite dimensions and scale
 * </ul>
 *
 * <p>Animations track frame progression via a frame counter that advances with each update call.
 * The current frame is determined by dividing the frame counter by the configured frames-per-sprite
 * value.
 *
 * <p>This class is serializable and cloneable for use in persistence and entity cloning scenarios.
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

  /** Animation configuration. */
  private AnimationConfig config;

  /** Logical world size (computed from sprite pixel size and scale). */
  private float width = 1;

  private float height = 1;

  /** Current frame counter. Serializable. */
  private int frameCount;

  /** The frames of this animation. */
  private transient AnimationFrame[] frames;

  /** Indicates whether frames are built. */
  private transient boolean loaded;

  /** Source description for lazy loading. */
  private final SourceType sourceType;

  /** For SINGLE_OR_MULTI: the explicit frame image paths (one per frame). */
  private List<IPath> framePaths;

  /** For SPRITESHEET: the path to the spritesheet image. */
  private IPath sheetPath;

  /** Cached sprite pixel dimensions (used by getSpriteWidth/Height and world-size calculation). */
  private int spritePxW = 16;

  private int spritePxH = 16;

  /**
   * Constructs an Animation from a single image or spritesheet with custom configuration.
   *
   * <p>The path is resolved implicitly: if it lacks an image extension, ".png" is appended. If a
   * spritesheet configuration is provided, the animation loads from a spritesheet; otherwise, it
   * treats the path as a single image.
   *
   * @param path the path to the image or spritesheet (must not be null or empty)
   * @param config the animation configuration; uses default if null
   * @throws IllegalArgumentException if the path is null or empty
   */
  public Animation(IPath path, AnimationConfig config) {
    if (path == null || path.pathString().isEmpty()) {
      throw new IllegalArgumentException("path can't be null or empty");
    }
    this.config = config == null ? new AnimationConfig() : config;

    final String resolved = resolveImplicitFilePath(path.pathString());
    final IPath exactPath = new SimpleIPath(resolved);

    if (this.config.config().isPresent()) {
      this.sourceType = SourceType.SPRITESHEET;
      this.sheetPath = exactPath;

      // Prefer explicit sprite sizes from config.
      SpritesheetConfig ssc = this.config.config().orElse(null);
      if (ssc != null && ssc.spriteWidth() > 0 && ssc.spriteHeight() > 0) {
        spritePxW = ssc.spriteWidth();
        spritePxH = ssc.spriteHeight();
      } else {
        int[] wh = tryReadImageSize(exactPath);
        if (wh != null) {
          spritePxW = wh[0];
          spritePxH = wh[1];
        }
      }
      calculateWorldSize(spritePxW, spritePxH);
    } else {
      this.sourceType = SourceType.SINGLE_OR_MULTI;
      this.framePaths = new ArrayList<>();
      this.framePaths.add(exactPath);

      int[] wh = tryReadImageSize(exactPath);
      if (wh != null) {
        spritePxW = wh[0];
        spritePxH = wh[1];
      }
      calculateWorldSize(spritePxW, spritePxH);
    }
  }

  /**
   * Constructs an Animation from a single image or spritesheet with the default configuration.
   *
   * @param path the path to the image or spritesheet (must not be null or empty)
   * @throws IllegalArgumentException if the path is null or empty
   */
  public Animation(IPath path) {
    this(path, new AnimationConfig());
  }

  /**
   * Constructs an Animation from a list of image paths with custom configuration.
   *
   * <p>Each path represents one frame of the animation, played in sequence.
   *
   * @param paths the list of paths to animation frames (must not be null or empty)
   * @param config the animation configuration; uses default if null
   * @throws IllegalArgumentException if paths are null or empty
   */
  public Animation(List<IPath> paths, AnimationConfig config) {
    if (paths == null || paths.isEmpty()) {
      throw new IllegalArgumentException("paths can't be null or empty");
    }
    this.config = config == null ? new AnimationConfig() : config;
    this.sourceType = SourceType.SINGLE_OR_MULTI;
    this.framePaths = new ArrayList<>(paths);

    int[] wh = tryReadImageSize(paths.getFirst());
    if (wh != null) {
      spritePxW = wh[0];
      spritePxH = wh[1];
    }
    calculateWorldSize(spritePxW, spritePxH);
  }

  /**
   * Constructs an Animation from a variable number of image paths with the default configuration.
   *
   * @param paths the image paths for each animation frame
   */
  public Animation(IPath... paths) {
    this(Arrays.asList(paths), new AnimationConfig());
  }

  /**
   * Constructs an Animation from a list of image paths with the default configuration.
   *
   * @param paths the list of paths to animation frames (must not be null or empty)
   * @throws IllegalArgumentException if paths are null or empty
   */
  public Animation(List<IPath> paths) {
    this(paths, new AnimationConfig());
  }

  /**
   * Loads a set of named animations from a spritesheet with JSON configuration.
   *
   * <p>The configuration file must be a JSON file in the same directory as the spritesheet image,
   * with a matching name. For example, if the image is "sprites/character.png", the config should
   * be "sprites/character.json".
   *
   * <p>The JSON configuration defines named animation sequences, each specifying row/column regions
   * within the spritesheet.
   *
   * @param path the path to the spritesheet or its containing directory (must not be null or empty)
   * @return a map of animation names to Animation objects, or null if the configuration cannot be
   *     loaded
   * @throws IllegalArgumentException if the path is null or empty
   * @throws IllegalArgumentException if the image file does not exist
   */
  public static Map<String, Animation> loadAnimationSpritesheet(IPath path) {
    if (path == null || path.pathString().isEmpty()) {
      throw new IllegalArgumentException("path can't be null or empty");
    }

    String pathString = path.pathString();
    String imgPath = resolveImplicitFilePath(pathString);
    String jsonPath = resolveImplicitJsonPath(pathString);

    // Image must exist (otherwise config is meaningless).
    if (!Platform.resources().exists(imgPath)) {
      throw new IllegalArgumentException("Image file not found: " + imgPath);
    }

    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);
    if (configs == null) return null;

    Map<String, Animation> animations = new HashMap<>();
    IPath img = new SimpleIPath(imgPath);

    for (Map.Entry<String, AnimationConfig> entry : configs.entrySet()) {
      String name = entry.getKey();
      AnimationConfig cfg = entry.getValue();
      animations.put(name, new Animation(img, cfg));
    }

    return animations;
  }

  /**
   * Gets the current animation frame based on the frame counter and configuration.
   *
   * <p>The frame is determined by dividing the frame counter by the configured frames-per-sprite
   * value. For looping animations, the frame index wraps around; for non-looping animations, it
   * clamps to the last frame.
   *
   * <p>If frames have not yet been loaded, this method triggers lazy loading.
   *
   * @return the current AnimationFrame, or a missing texture placeholder if no frames are available
   */
  public AnimationFrame getFrame() {
    ensureLoaded();

    int idx = frameCount / config.framesPerSprite();
    if (frames == null || frames.length == 0) {
      return AnimationFrame.fullImage(MISSING_TEXTURE_PATH, config.mirrored());
    }

    if (config.isLooping()) {
      idx = idx % frames.length;
    } else {
      idx = Math.min(idx, frames.length - 1);
    }
    return frames[idx];
  }

  /**
   * Gets the world-space width of this animation.
   *
   * <p>This is calculated from the sprite pixel width, sprite scale, and the animation's scale X
   * factor.
   *
   * @return the width in world units
   */
  public float getWidth() {
    return width;
  }

  /**
   * Gets the world-space height of this animation.
   *
   * <p>This is calculated from the sprite pixel height, sprite scale, and the animation's scale Y
   * factor.
   *
   * @return the height in world units
   */
  public float getHeight() {
    return height;
  }

  /**
   * Gets the horizontal scale factor of this animation.
   *
   * @return the scale X value from the animation configuration
   */
  public float getScaleX() {
    return config.scaleX();
  }

  /**
   * Gets the vertical scale factor of this animation.
   *
   * <p>If scale Y is 0 in the configuration, returns scale X instead.
   *
   * @return the scale Y value, or scale X if scale Y is 0
   */
  public float getScaleY() {
    return config.scaleY() == 0 ? config.scaleX() : config.scaleY();
  }

  /**
   * Gets the sprite width in pixels (not world coordinates).
   *
   * @return the sprite pixel width
   */
  public float getSpriteWidth() {
    // pixel width (not world width)
    return spritePxW;
  }

  /**
   * Gets the sprite height in pixels (not world coordinates).
   *
   * @return the sprite pixel height
   */
  public float getSpriteHeight() {
    // pixel height (not world height)
    return spritePxH;
  }

  /**
   * Checks whether this animation has finished playing.
   *
   * <p>For looping animations, this method always returns false. For non-looping animations, it
   * returns true when the frame counter has advanced beyond the last frame.
   *
   * @return true if the animation has finished, false otherwise
   */
  public boolean isFinished() {
    ensureLoaded();
    int idx = frameCount / config.framesPerSprite();
    int len = (frames == null) ? 0 : frames.length;
    return len == 0 || idx >= len;
  }

  /**
   * Advances the animation by one frame and returns the current frame.
   *
   * <p>This increments the internal frame counter and returns the updated current frame.
   *
   * @return the current AnimationFrame after the update
   */
  public AnimationFrame update() {
    frameCount++;
    return getFrame();
  }

  /**
   * Gets the current frame counter-value.
   *
   * <p>The frame counter tracks animation progression. It is divided by frames-per-sprite to
   * determine the current frame index.
   *
   * @return the current frame counter-value
   */
  public int frameCount() {
    return frameCount;
  }

  /**
   * Sets the frame counter to a specific value.
   *
   * <p>This allows direct control over animation progression, useful for seeking to a specific
   * point in the animation.
   *
   * @param frameCount the new frame counter value
   */
  public void frameCount(int frameCount) {
    this.frameCount = frameCount;
  }

  /**
   * Gets the animation configuration used by this animation.
   *
   * @return the animation configuration
   */
  public AnimationConfig getConfig() {
    return config;
  }

  /**
   * Unloads all loaded animation frames and marks the animation as needing reload.
   *
   * <p>This forces lazy reloading on the next frame access. Useful for releasing memory or forcing
   * frame reconstruction after configuration changes.
   */
  public void unload() {
    frames = null;
    loaded = false;
  }

  /**
   * Sets the horizontal flip (mirroring) state and rebuilds frames.
   *
   * <p>This modifies the animation configuration and forces frames to be rebuilt with the new flip
   * state applied.
   *
   * @param mirrored true to flip the animation horizontally, false for normal orientation
   * @return this Animation instance for method chaining
   */
  public Animation mirrored(boolean mirrored) {
    config.mirrored(mirrored);
    unload(); // rebuild frames with new flip hint
    return this;
  }

  private void ensureLoaded() {
    if (loaded && frames != null) return;
    buildFrames();
    loaded = true;
  }

  private void buildFrames() {
    boolean flipX = config.mirrored();

    if (sourceType == SourceType.SINGLE_OR_MULTI) {
      if (framePaths == null || framePaths.isEmpty()) {
        frames = new AnimationFrame[] {AnimationFrame.fullImage(MISSING_TEXTURE_PATH, flipX)};
        spritePxW = 16;
        spritePxH = 16;
        calculateWorldSize(spritePxW, spritePxH);
        return;
      }

      frames = new AnimationFrame[framePaths.size()];
      for (int i = 0; i < framePaths.size(); i++) {
        IPath p = framePaths.get(i) == null ? MISSING_TEXTURE_PATH : framePaths.get(i);
        frames[i] = AnimationFrame.fullImage(p, flipX);
      }

      int[] wh = tryReadImageSize(framePaths.getFirst());
      if (wh != null) {
        spritePxW = wh[0];
        spritePxH = wh[1];
      } else {
        spritePxW = 16;
        spritePxH = 16;
      }
      calculateWorldSize(spritePxW, spritePxH);
      return;
    }

    // SPRITESHEET
    SpritesheetConfig ssc = config.config().orElse(null);
    if (sheetPath == null) {
      frames = new AnimationFrame[] {AnimationFrame.fullImage(MISSING_TEXTURE_PATH, flipX)};
      spritePxW = 16;
      spritePxH = 16;
      calculateWorldSize(spritePxW, spritePxH);
      return;
    }

    if (ssc == null) {
      // No grid info -> treat as single full image.
      frames = new AnimationFrame[] {AnimationFrame.fullImage(sheetPath, flipX)};
      int[] wh = tryReadImageSize(sheetPath);
      if (wh != null) {
        spritePxW = wh[0];
        spritePxH = wh[1];
      } else {
        spritePxW = 16;
        spritePxH = 16;
      }
      calculateWorldSize(spritePxW, spritePxH);
      return;
    }

    int cols = Math.max(1, ssc.columns());
    int rows = Math.max(1, ssc.rows());
    int count = rows * cols;

    spritePxW = Math.max(1, ssc.spriteWidth());
    spritePxH = Math.max(1, ssc.spriteHeight());
    calculateWorldSize(spritePxW, spritePxH);

    frames = new AnimationFrame[count];
    int offX = ssc.x();
    int offY = ssc.y();

    for (int i = 0; i < count; i++) {
      int x = i % cols;
      int y = i / cols;
      frames[i] =
          AnimationFrame.region(
              sheetPath, offX + spritePxW * x, offY + spritePxH * y, spritePxW, spritePxH, flipX);
    }
  }

  private void calculateWorldSize(int spriteWidth, int spriteHeight) {
    float spriteScale = (float) 1 / Math.min(spriteWidth, spriteHeight);
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

  private static String resolveImplicitJsonPath(String pathString) {
    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      String dirName = pathString.replaceAll("/$", "");
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1);
      return dirName + "/" + baseName + ".json";
    }
    return pathString.replaceAll("\\.(png|jpg|jpeg)$", ".json");
  }

  private static int[] tryReadImageSize(IPath path) {
    if (path == null || path.pathString() == null || path.pathString().isBlank()) return null;
    String p = path.pathString();

    if (!Platform.resources().exists(p)) return null;

    try (InputStream in = Platform.resources().open(p)) {
      BufferedImage img = ImageIO.read(in);
      if (img == null) return null;
      return new int[] {img.getWidth(), img.getHeight()};
    } catch (IOException e) {
      LOGGER.debug("Failed to read image size for {}: {}", p, e.getMessage());
      return null;
    }
  }

  @Serial
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  @Serial
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    this.frames = null;
    this.loaded = false;
  }

  @Override
  public Animation clone() {
    try {
      Animation cloned = (Animation) super.clone();
      if (this.config != null) {
        cloned.config = config.clone();
      }
      cloned.frames = null;
      cloned.loaded = false;
      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Failed to clone", e);
    }
  }
}
