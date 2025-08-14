package core.utils.components.draw.animation;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.*;

public class Animation {

  private static final IPath MISSING_TEXTURE_PATH =
      new SimpleIPath("animation/missing_texture.png");
  private static final float DEFAULT_SCALE = 1f / 16;

  private final AnimationConfig config;
  private float width = 1;
  private float height = 1;
  private int frameCount;
  private Sprite[] sprites;

  /**
   * A new animation
   *
   * @param path An IPath to either a single image or a folder of images
   * @param config The configuration to use for this animation
   */
  public Animation(IPath path, AnimationConfig config) {
    if (path == null || path.pathString().isEmpty())
      throw new IllegalArgumentException("path can't be null or empty");
    if (config == null) this.config = new AnimationConfig();
    else this.config = config;
    loadFromSingle(path);
  }

  public Animation(IPath path) {
    this(path, new AnimationConfig());
  }

  /**
   * A new animation
   *
   * @param paths A series of textures to use
   * @param config The configuration to use for this animation
   */
  public Animation(List<IPath> paths, AnimationConfig config) {
    if (paths == null || paths.size() == 0)
      throw new IllegalArgumentException("paths can't be null or empty");
    if (config == null) this.config = new AnimationConfig();
    else this.config = config;
    loadSpritesFromPaths(paths);
  }

  public Animation(IPath... paths) {
    this(Arrays.asList(paths), new AnimationConfig());
  }

  public Animation(List<IPath> paths) {
    this(paths, new AnimationConfig());
  }

  /*
   * Algorithm:
   * 1. Figure out if we are running from a jar
   * 2. Get all Textures specified through the AnimationConfig (cached)
   * 2.1. If single path, load the image as single Texture in a Texture[]
   * 2.2. If the path leads to a directory, assume its meant to target the image .png with the same name as the directory
   * 2.3. If single path and SpritesheetConfig is set, load the image as single Texture and get all relevant regions as TextureRegion[]
   * 3. Load Textures into Sprites (cached)
   * 3.1. If Texture[], load into Sprites (cached)
   * 3.2. If TextureRegion[], load into Sprites (cached (somehow))
   * 4. Set the sprites array
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
    if (config.config() == null) {
      // Case: 2.1.
      paths.add(exactPath);
      loadSpritesFromPaths(paths);
    } else {
      // Case: 2.3.
      loadSpritesFromSpritesheet(exactPath);
    }
  }

  private void loadSpritesFromPaths(List<IPath> paths) {
    sprites = new Sprite[paths.size()];
    for (int i = 0; i < paths.size(); i++) {
      sprites[i] = new Sprite(TextureMap.instance().textureAt(paths.get(i)));
    }

    // Set the sprite width/height based on the scaling values and the texture width/height
    Texture t = TextureMap.instance().textureAt(paths.get(0));
    width = t.getWidth() * config.scaleX();
    height =
        config.scaleY() == 0 ? t.getHeight() * config.scaleX() : t.getHeight() * config.scaleY();

    width *= DEFAULT_SCALE;
    height *= DEFAULT_SCALE;
  }

  private void loadSpritesFromSpritesheet(IPath path) {
    Texture spritesheet = TextureMap.instance().textureAt(path);
    SpritesheetConfig ssc = config.config();
    int sWidth = ssc.spriteWidth();
    int sHeight = ssc.spriteHeight();
    int offsetX = ssc.x();
    int offsetY = ssc.y();

    sprites = new Sprite[ssc.rows() * ssc.columns()];
    for (int y = 0; y < ssc.rows(); y++) {
      for (int x = 0; x < ssc.columns(); x++) {
        int index = y * ssc.columns() + x;
        sprites[index] =
            new Sprite(
                new TextureRegion(
                    spritesheet, offsetX + sWidth * x, offsetY + sHeight * y, sWidth, sHeight));
      }
    }

    width = sWidth * config.scaleX();
    height = config.scaleY() == 0 ? sHeight * config.scaleX() : sHeight * config.scaleY();
    width *= DEFAULT_SCALE;
    height *= DEFAULT_SCALE;
  }

  public Sprite getSprite() {
    int spriteIndex = frameCount / config.framesPerSprite();
    if (config.isLooping()) {
      spriteIndex = spriteIndex % sprites.length;
    } else {
      spriteIndex = Math.min(spriteIndex, sprites.length - 1);
    }
    return sprites[spriteIndex];
  }

  public float getSpriteWidth() {
    return width;
  }

  public float getSpriteHeight() {
    return height;
  }

  public boolean isLooping() {
    return config.isLooping();
  }

  public boolean isFinished() {
    int spriteIndex = frameCount / config.framesPerSprite();
    return spriteIndex >= sprites.length;
  }

  /**
   * Updates the animation frame counter
   *
   * @return The updated sprite
   */
  public Sprite update() {
    frameCount++;
    return getSprite();
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

  public static Map<String, Animation> loadAnimationSpritesheet(IPath path) {
    String pathString = path.pathString();
    String jsonPath;

    if (pathString.endsWith("/") || !pathString.matches(".*\\.(png|jpg|jpeg)$")) {
      // It's a directory path or doesn't end in image extension → assume `dir/dir.json`
      String dirName = pathString.replaceAll("/$", ""); // remove trailing slash if present
      String baseName = dirName.substring(dirName.lastIndexOf('/') + 1); // get last path component
      jsonPath = dirName + "/" + baseName + ".json";
    } else {
      // It's an image path → replace extension with `.json`
      jsonPath = pathString.replaceAll("\\.(png|jpg|jpeg)$", ".json");
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
