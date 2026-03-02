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
 * Represents an animation consisting of one or more frames.
 *
 * <p>Engine-agnostic: frames are exposed as {@link AnimationFrame}. Rendering backends are
 * responsible for converting frames into concrete drawables.
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

  /** Lazily-built engine-agnostic frames. Not serializable. */
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

  public Animation(IPath path) {
    this(path, new AnimationConfig());
  }

  public Animation(List<IPath> paths, AnimationConfig config) {
    if (paths == null || paths.isEmpty()) {
      throw new IllegalArgumentException("paths can't be null or empty");
    }
    this.config = config == null ? new AnimationConfig() : config;
    this.sourceType = SourceType.SINGLE_OR_MULTI;
    this.framePaths = new ArrayList<>(paths);

    int[] wh = tryReadImageSize(paths.get(0));
    if (wh != null) {
      spritePxW = wh[0];
      spritePxH = wh[1];
    }
    calculateWorldSize(spritePxW, spritePxH);
  }

  public Animation(IPath... paths) {
    this(Arrays.asList(paths), new AnimationConfig());
  }

  public Animation(List<IPath> paths) {
    this(paths, new AnimationConfig());
  }

  /**
   * Loads animations from a spritesheet directory or file path.
   *
   * <p>Resolves:
   * <ul>
   *   <li>image: &lt;dir&gt;/&lt;dirName&gt;.png (if a directory path is passed)
   *   <li>config: &lt;dir&gt;/&lt;dirName&gt;.json or &lt;file&gt;.json
   * </ul>
   *
   * @return name -> Animation map, or null if no json config exists.
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

  /** Returns the current engine-agnostic frame. */
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

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

  public float getScaleX() {
    return config.scaleX();
  }

  public float getScaleY() {
    return config.scaleY() == 0 ? config.scaleX() : config.scaleY();
  }

  public float getSpriteWidth() {
    // pixel width (not world width)
    return spritePxW;
  }

  public float getSpriteHeight() {
    // pixel height (not world height)
    return spritePxH;
  }

  public boolean isFinished() {
    ensureLoaded();
    int idx = frameCount / config.framesPerSprite();
    int len = (frames == null) ? 0 : frames.length;
    return len == 0 || idx >= len;
  }

  /** Advances the frame counter and returns the new current frame. */
  public AnimationFrame update() {
    frameCount++;
    return getFrame();
  }

  public int frameCount() {
    return frameCount;
  }

  public void frameCount(int frameCount) {
    this.frameCount = frameCount;
  }

  public AnimationConfig getConfig() {
    return config;
  }

  public void unload() {
    frames = null;
    loaded = false;
  }

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

      int[] wh = tryReadImageSize(framePaths.get(0));
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
          sheetPath,
          offX + spritePxW * x,
          offY + spritePxH * y,
          spritePxW,
          spritePxH,
          flipX);
    }
  }

  /**
   * Sets the width and height of the animation in world units.
   *
   * <p>Assumes the smallest dimension to be 1 tile in the world (before scale is applied).
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

  private static String resolveImplicitJsonPath(String pathString) {
    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      String dirName = pathString.replaceAll("/$", "");
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1);
      return dirName + "/" + baseName + ".json";
    }
    return pathString.replaceAll("\\.(png|jpg|jpeg)$", ".json");
  }

  /**
   * Reads image dimensions via Platform.resources() + ImageIO.
   *
   * @return int[]{w,h} or null if unavailable.
   */
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
