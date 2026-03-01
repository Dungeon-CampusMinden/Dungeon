package core.utils.components.draw.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.platform.Platform;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.io.*;
import java.util.*;

/**
 * Represents an animation consisting of one or more frames.
 *
 * <p>NOTE: The public API is engine-agnostic and returns {@link AnimationFrame}.
 * libGDX {@link Sprite} objects may still be used internally for the GDX backend.
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

  /** Engine-agnostic frames (each may cache a backend handle). Not serializable. */
  private transient AnimationFrame[] frames;

  /** Indicates whether sprites/frames are loaded. */
  private transient boolean loaded;

  /** Source description for lazy loading. */
  private final SourceType sourceType;

  /** For SINGLE_OR_MULTI: the explicit frame image paths (one per frame). */
  private List<IPath> framePaths;

  /** For SPRITESHEET: the path to the spritesheet image. */
  private IPath sheetPath;

  public Animation(IPath path, AnimationConfig config) {
    if (path == null || path.pathString().isEmpty())
      throw new IllegalArgumentException("path can't be null or empty");
    this.config = config == null ? new AnimationConfig() : config;

    String resolved = resolveImplicitFilePath(path.pathString());
    IPath exactPath = new SimpleIPath(resolved);

    if (this.config.config().isPresent()) {
      this.sourceType = SourceType.SPRITESHEET;
      this.sheetPath = exactPath;

      SpritesheetConfig ssc = this.config.config().get();
      calculateWorldSize(ssc.spriteWidth(), ssc.spriteHeight());

      tryEagerLoad();
    } else {
      this.sourceType = SourceType.SINGLE_OR_MULTI;
      this.framePaths = new ArrayList<>();
      this.framePaths.add(exactPath);

      if (canUseTextures() || TextureMap.instance().containsKey(exactPath.pathString())) {
        Texture t = TextureMap.instance().textureAt(exactPath);
        calculateWorldSize(t.getWidth(), t.getHeight());
      } else {
        calculateWorldSize(16, 16);
      }
      tryEagerLoad();
    }
  }

  public Animation(IPath path) {
    this(path, new AnimationConfig());
  }

  public Animation(List<IPath> paths, AnimationConfig config) {
    if (paths == null || paths.isEmpty())
      throw new IllegalArgumentException("paths can't be null or empty");
    this.config = config == null ? new AnimationConfig() : config;

    this.sourceType = SourceType.SINGLE_OR_MULTI;
    this.framePaths = new ArrayList<>(paths);

    Texture t = canUseTextures() ? TextureMap.instance().textureAt(paths.get(0)) : null;
    if (t != null) {
      calculateWorldSize(t.getWidth(), t.getHeight());
    } else {
      calculateWorldSize(16, 16);
    }
    tryEagerLoad();
  }

  public Animation(IPath... paths) {
    this(Arrays.asList(paths), new AnimationConfig());
  }

  public Animation(List<IPath> paths) {
    this(paths, new AnimationConfig());
  }

  /** Returns the current engine-agnostic frame. */
  public AnimationFrame getFrame() {
    ensureLoaded();

    int idx = frameCount / config.framesPerSprite();
    if (frames == null || frames.length == 0) {
      return AnimationFrame.fullImage(MISSING_TEXTURE_PATH);
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
    ensureLoaded();
    Sprite s = (sprites != null && sprites.length > 0) ? sprites[0] : null;
    return s != null ? s.getRegionWidth() : 16;
  }

  public float getSpriteHeight() {
    ensureLoaded();
    Sprite s = (sprites != null && sprites.length > 0) ? sprites[0] : null;
    return s != null ? s.getRegionHeight() : 16;
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
    sprites = null;
    frames = null;
    loaded = false;
  }

  public Animation mirrored(boolean mirrored) {
    config.mirrored(mirrored);
    return this;
  }

  public boolean mirrored() {
    return config.mirrored();
  }

  @Override
  public String toString() {
    return "Animation{"
      + "width=" + width
      + ", height=" + height
      + ", frameCount=" + frameCount
      + ", loaded=" + loaded
      + ", sourceType=" + sourceType
      + ", frames=" + expectedSpriteCount()
      + ", config=" + config
      + "}";
  }

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

    if (!Platform.resources().exists(pathString)) {
      throw new IllegalArgumentException("Image file not found: " + pathString);
    }

    if (!canUseTextures()) {
      LOGGER.warn(
        "Loading animation configs without libGDX rendering backend. Frames will be placeholders until a render backend is available. path={}",
        pathString);
    }

    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);
    if (configs == null) return null;

    Map<String, Animation> animations = new HashMap<>();
    for (Map.Entry<String, AnimationConfig> entry : configs.entrySet()) {
      animations.put(entry.getKey(), new Animation(path, entry.getValue()));
    }
    return animations;
  }

  private static boolean canUseTextures() {
    return Platform.runtime().supportsGdxRendering();
  }

  private void ensureLoaded() {
    if (loaded && frames != null && sprites != null) return;

    if (!canUseTextures()) {
      initPlaceholderSprites();
      buildFrames();
      loaded = true;
      return;
    }

    if (sourceType == SourceType.SINGLE_OR_MULTI) {
      loadSpritesFromPaths(framePaths);
    } else {
      loadSpritesFromSpritesheet(sheetPath);
    }

    buildFrames();
    loaded = true;
  }

  private void initPlaceholderSprites() {
    final int count = Math.max(1, expectedSpriteCount());
    this.sprites = new Sprite[count];
    for (int i = 0; i < count; i++) {
      this.sprites[i] = new Sprite();
    }
    if (this.width <= 0) this.width = 1;
    if (this.height <= 0) this.height = 1;
  }

  private int expectedSpriteCount() {
    if (this.framePaths != null && !this.framePaths.isEmpty()) {
      return this.framePaths.size();
    }
    return this.config
      .config()
      .map(c -> Math.max(1, c.rows() * c.columns()))
      .orElse(1);
  }

  private void tryEagerLoad() {
    if (canUseTextures()) {
      try {
        ensureLoaded();
      } catch (Exception e) {
        LOGGER.debug("Eager load failed; will lazy-load later: {}", e.getMessage());
        sprites = null;
        frames = null;
        loaded = false;
      }
    }
  }

  private void buildFrames() {
    final int count = (sprites != null) ? sprites.length : Math.max(1, expectedSpriteCount());
    final AnimationFrame[] out = new AnimationFrame[count];

    if (sourceType == SourceType.SINGLE_OR_MULTI) {
      for (int i = 0; i < count; i++) {
        final IPath p =
          (framePaths != null && i < framePaths.size()) ? framePaths.get(i) : MISSING_TEXTURE_PATH;
        out[i] = AnimationFrame.fullImage(p);
        if (sprites != null && i < sprites.length) {
          out[i].backendHandle(sprites[i]);
        }
      }
    } else {
      final SpritesheetConfig ssc = config.config().orElse(null);
      final int cols = (ssc != null) ? ssc.columns() : 1;
      final int sW = (ssc != null) ? ssc.spriteWidth() : -1;
      final int sH = (ssc != null) ? ssc.spriteHeight() : -1;
      final int offX = (ssc != null) ? ssc.x() : 0;
      final int offY = (ssc != null) ? ssc.y() : 0;

      for (int i = 0; i < count; i++) {
        if (ssc != null && sW > 0 && sH > 0) {
          final int x = i % cols;
          final int y = i / cols;
          out[i] = AnimationFrame.region(sheetPath, offX + sW * x, offY + sH * y, sW, sH);
        } else {
          out[i] = AnimationFrame.fullImage(sheetPath != null ? sheetPath : MISSING_TEXTURE_PATH);
        }
        if (sprites != null && i < sprites.length) {
          out[i].backendHandle(sprites[i]);
        }
      }
    }

    this.frames = out;
  }

  private void loadSpritesFromPaths(List<IPath> paths) {
    if (paths == null || paths.isEmpty()) {
      throw new IllegalStateException("No frame paths provided");
    }

    sprites = new Sprite[paths.size()];

    if (!canUseTextures()) {
      for (int i = 0; i < sprites.length; i++) {
        sprites[i] = new Sprite();
      }
      calculateWorldSize(16, 16);
      return;
    }

    for (int i = 0; i < paths.size(); i++) {
      Texture tex = TextureMap.instance().textureAt(paths.get(i));
      sprites[i] = (tex != null) ? new Sprite(tex) : new Sprite();
    }

    int textWidth = 16;
    int textHeight = 16;

    Texture first = TextureMap.instance().textureAt(paths.get(0));
    if (first != null) {
      textWidth = first.getWidth();
      textHeight = first.getHeight();
    }

    calculateWorldSize(textWidth, textHeight);
  }

  private void loadSpritesFromSpritesheet(IPath path) {
    SpritesheetConfig ssc =
      config
        .config()
        .orElseThrow(
          () ->
            new IllegalStateException("SpritesheetConfig expected but not present in config"));

    int sWidth = ssc.spriteWidth();
    int sHeight = ssc.spriteHeight();
    int offsetX = ssc.x();
    int offsetY = ssc.y();

    sprites = new Sprite[ssc.rows() * ssc.columns()];

    if (!canUseTextures()) {
      for (int i = 0; i < sprites.length; i++) {
        sprites[i] = new Sprite();
      }
      calculateWorldSize(sWidth, sHeight);
      return;
    }

    Texture spritesheet = TextureMap.instance().textureAt(path);

    for (int y = 0; y < ssc.rows(); y++) {
      for (int x = 0; x < ssc.columns(); x++) {
        int index = y * ssc.columns() + x;
        if (spritesheet != null) {
          sprites[index] =
            new Sprite(
              new TextureRegion(
                spritesheet,
                offsetX + sWidth * x,
                offsetY + sHeight * y,
                sWidth,
                sHeight));
        } else {
          sprites[index] = new Sprite();
        }
      }
    }

    calculateWorldSize(sWidth, sHeight);
  }

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
      // Do NOT clone backend handles. Force lazy reload on the clone.
      cloned.sprites = null;
      cloned.frames = null;
      cloned.loaded = false;
      return cloned;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Failed to clone", e);
    }
  }
}
