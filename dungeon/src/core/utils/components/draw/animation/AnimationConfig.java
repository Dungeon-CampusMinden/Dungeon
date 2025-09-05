package core.utils.components.draw.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration class for an {@link Animation}. Defines scaling, frame timing, looping behavior,
 * and optional {@link SpritesheetConfig} for extracting frames from a spritesheet.
 *
 * <p>Instances of this class are usually loaded from a JSON file alongside a spritesheet via {@link
 * #loadAnimationConfigMap(String)}.
 */
public class AnimationConfig {

  /** Optional spritesheet configuration (width, height, offsets, etc.). */
  private SpritesheetConfig config;

  /** Number of update ticks each sprite frame is displayed for. Default: 10. */
  private int framesPerSprite = 10;

  /** Horizontal scaling factor. Default: 1. */
  private float scaleX = 1;

  /**
   * Vertical scaling factor. If set to {@code 0}, {@link #scaleX()} will be used instead. Default:
   * 0.
   */
  private float scaleY = 0;

  /** Whether the animation should loop once it reaches the end. Default: true. */
  private boolean isLooping = true;

  /** Whether the animation sprites should be drawn centered. Default: false. */
  private boolean centered = false;

  /**
   * Creates a new {@link AnimationConfig} with a specified {@link SpritesheetConfig}.
   *
   * @param config spritesheet configuration to use (can be {@code null})
   */
  public AnimationConfig(SpritesheetConfig config) {
    this.config = config;
  }

  /** Creates a new {@link AnimationConfig} with no {@link SpritesheetConfig}. */
  public AnimationConfig() {
    this(null);
  }

  /**
   * @return an Optional of the {@link SpritesheetConfig}
   */
  public Optional<SpritesheetConfig> config() {
    return Optional.ofNullable(config);
  }

  /**
   * Sets the {@link SpritesheetConfig}.
   *
   * @param config the spritesheet configuration
   * @return this config for chaining
   */
  public AnimationConfig config(SpritesheetConfig config) {
    this.config = config;
    return this;
  }

  /**
   * @return the number of update ticks each frame should last
   */
  public int framesPerSprite() {
    return framesPerSprite;
  }

  /**
   * Sets the number of update ticks each frame should last.
   *
   * @param framesPerSprite number of ticks per frame (must be ≥ 1)
   * @return this config for chaining
   * @throws IllegalArgumentException if {@code framesPerSprite < 1}
   */
  public AnimationConfig framesPerSprite(int framesPerSprite) {
    if (framesPerSprite <= 0)
      throw new IllegalArgumentException("framesPerSprite cannot be less than 1");
    this.framesPerSprite = framesPerSprite;
    return this;
  }

  /**
   * Sets the horizontal scaling factor.
   *
   * @param scaleX new scaleX
   * @return this config for chaining
   */
  public AnimationConfig scaleX(float scaleX) {
    this.scaleX = scaleX;
    return this;
  }

  /**
   * @return the horizontal scaling factor
   */
  public float scaleX() {
    return scaleX;
  }

  /**
   * Sets the vertical scaling factor.
   *
   * @param scaleY new scaleY
   * @return this config for chaining
   */
  public AnimationConfig scaleY(float scaleY) {
    this.scaleY = scaleY;
    return this;
  }

  /**
   * @return the vertical scaling factor, or 0 if it should be inferred from {@link #scaleX()}.
   */
  public float scaleY() {
    return scaleY;
  }

  /**
   * Sets whether the animation should loop.
   *
   * @param looping true if looping
   * @return this config for chaining
   */
  public AnimationConfig isLooping(boolean looping) {
    isLooping = looping;
    return this;
  }

  /**
   * @return whether the animation should loop
   */
  public boolean isLooping() {
    return isLooping;
  }

  /**
   * Sets whether sprites should be drawn centered.
   *
   * @param centered true if centered
   * @return this config for chaining
   */
  public AnimationConfig centered(boolean centered) {
    this.centered = centered;
    return this;
  }

  /**
   * @return whether sprites should be drawn centered
   */
  public boolean centered() {
    return centered;
  }

  @Override
  public String toString() {
    return "AnimationConfig{"
        + "framesPerSprite="
        + framesPerSprite
        + ", scaleX="
        + scaleX
        + ", scaleY="
        + scaleY
        + ", isLooping="
        + isLooping
        + ", centered="
        + centered
        + ", config="
        + config
        + '}';
  }

  /**
   * Loads a map of {@link AnimationConfig}s from a JSON file.
   *
   * @param jsonFilePath path to the JSON file (relative to assets)
   * @return a map of animation name → {@link AnimationConfig}, or {@code null} if file not found
   */
  public static Map<String, AnimationConfig> loadAnimationConfigMap(String jsonFilePath) {
    JsonReader jsonReader = new JsonReader();
    Map<String, AnimationConfig> animationMap = new HashMap<>();

    FileHandle f = Gdx.files.internal(jsonFilePath);
    if (!f.exists()) return null;

    JsonValue root = jsonReader.parse(f);

    for (JsonValue entry = root.child; entry != null; entry = entry.next) {
      String animationName = entry.name;

      // Read SpritesheetConfig
      JsonValue configJson = entry.get("config");
      SpritesheetConfig sheetConfig = new SpritesheetConfig();
      sheetConfig.spriteWidth(configJson.getInt("spriteWidth"));
      sheetConfig.spriteHeight(configJson.getInt("spriteHeight"));
      sheetConfig.x(configJson.getInt("x"));
      sheetConfig.y(configJson.getInt("y"));
      sheetConfig.rows(configJson.getInt("rows"));
      sheetConfig.columns(configJson.getInt("columns"));

      // Read AnimationConfig
      AnimationConfig animConfig = new AnimationConfig();
      animConfig.config(sheetConfig);
      animConfig.framesPerSprite(entry.getInt("framesPerSprite", 10));
      animConfig.scaleX(entry.getFloat("scaleX", 1f));
      animConfig.scaleY(entry.getFloat("scaleY", 0f));
      animConfig.isLooping(entry.getBoolean("isLooping", true));
      animConfig.centered(entry.getBoolean("centered", false));

      animationMap.put(animationName, animConfig);
    }

    return animationMap;
  }
}
