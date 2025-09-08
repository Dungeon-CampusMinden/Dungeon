package core.utils.components.draw.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
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
public class Animation {

  /** Path to the missing texture fallback image. */
  public static final IPath MISSING_TEXTURE_PATH = new SimpleIPath("animation/missing_texture.png");

  private final AnimationConfig config;
  private float width = 1;
  private float height = 1;
  private float spriteScale;
  private int frameCount;
  private Sprite[] sprites;

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
    if (config == null) this.config = new AnimationConfig();
    else this.config = config;
    loadFromSingle(path);
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
    if (paths == null || paths.size() == 0)
      throw new IllegalArgumentException("paths can't be null or empty");
    if (config == null) this.config = new AnimationConfig();
    else this.config = config;
    loadSpritesFromPaths(paths);
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
   * Load sprites from a single path, resolving whether it is a single image, a folder with an
   * implied image, or a spritesheet.
   *
   * @param path The input path.
   */
  private void loadFromSingle(IPath path) {
    String pathString = path.pathString();
    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      // It's a directory path or doesn't end in image extension → assume `dir/dir.png`
      String dirName = pathString.replaceAll("/$", ""); // remove trailing slash if present
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1); // get last path component
      pathString = dirName + "/" + baseName + ".png";
    }
    IPath exactPath = new SimpleIPath(pathString);

    List<IPath> paths = new ArrayList<>();
    config
        .config()
        .ifPresentOrElse(
            c -> {
              // Case: spritesheet
              loadSpritesFromSpritesheet(exactPath);
            },
            () -> {
              // Case: single image
              paths.add(exactPath);
              loadSpritesFromPaths(paths);
            });
  }

  /**
   * Load sprites directly from a list of image paths.
   *
   * @param paths A list of image paths.
   */
  private void loadSpritesFromPaths(List<IPath> paths) {
    sprites = new Sprite[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      if (TextureMap.instance().containsKey(paths.get(i).pathString()) || Gdx.gl != null) {
        sprites[i] = new Sprite(TextureMap.instance().textureAt(paths.get(i)));
      } else {
        sprites[i] = new Sprite();
      }
    }

    int textWidth, textHeight;

    if (TextureMap.instance().containsKey(paths.get(0).pathString()) || Gdx.gl != null) {
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
    if (TextureMap.instance().containsKey(path.pathString()) || Gdx.gl != null) {
      spritesheet = TextureMap.instance().textureAt(path);
    }

    SpritesheetConfig ssc = config.config().orElseThrow();
    int sWidth = ssc.spriteWidth();
    int sHeight = ssc.spriteHeight();
    int offsetX = ssc.x();
    int offsetY = ssc.y();

    sprites = new Sprite[ssc.rows() * ssc.columns()];
    for (int y = 0; y < ssc.rows(); y++) {
      for (int x = 0; x < ssc.columns(); x++) {
        int index = y * ssc.columns() + x;
        if (Gdx.gl != null) {
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

  /**
   * Get the current sprite frame based on the frame counter and configuration.
   *
   * @return The current {@link Sprite}.
   */
  public Sprite getSprite() {
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

  @Override
  public String toString() {
    return "Animation{"
        + "width="
        + width
        + ", height="
        + height
        + ", frameCount="
        + frameCount
        + ", sprites="
        + Arrays.toString(sprites)
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
      // It's a directory path or doesn't end in image extension → assume `dir/dir.json`
      String dirName = pathString.replaceAll("/$", ""); // remove trailing slash if present
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1); // get last path component
      jsonPath = dirName + "/" + baseName + ".json";
      pathString = dirName + "/" + baseName + ".png";
    } else {
      // It's an image path → replace extension with `.json`
      jsonPath = pathString.replaceAll("\\.(png|jpg|jpeg)$", ".json");
    }

    // Check if image file exists
    if (Gdx.files != null && Gdx.gl != null) {
      // Throws a GdxRuntimeException if the image isn't found.
      TextureMap.instance().textureAt(new SimpleIPath(pathString));
    }

    // Load configs
    Map<String, AnimationConfig> configs = AnimationConfig.loadAnimationConfigMap(jsonPath);
    if (configs == null) return null;

    Map<String, Animation> animations = new HashMap<>();
    for (Map.Entry<String, AnimationConfig> entry : configs.entrySet()) {
      String name = entry.getKey();
      AnimationConfig config = entry.getValue();
      animations.put(name, new Animation(path, config));
    }

    return animations;
  }
}
