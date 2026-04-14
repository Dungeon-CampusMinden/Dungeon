package core.utils.components.draw.animation;

import java.io.Serial;
import java.io.Serializable;
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
public class AnimationConfig implements Cloneable, Serializable {
  @Serial private static final long serialVersionUID = 1L;

  /** Optional spritesheet configuration (width, height, offsets, etc.). */
  private SpritesheetConfig config;

  /** The number of update ticks each sprite frame is displayed for. Default: 10. */
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

  /** Whether the animation sprites should be drawn horizontally mirrored. Default: false. */
  private boolean mirrored = false;

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
   * Sets the horizontal scaling factor. If y-scale is empty, it will be inferred from this value.
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
   * Sets the vertical scaling factor. If set to {@code 0}, {@link #scaleX()} will be used instead.
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
   * Sets whether sprites should be drawn-centered.
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

  /**
   * Sets whether sprites should be drawn horizontally mirrored.
   *
   * @param mirrored true if mirrored
   * @return this config for chaining
   */
  public AnimationConfig mirrored(boolean mirrored) {
    this.mirrored = mirrored;
    return this;
  }

  /**
   * @return whether sprites should be drawn horizontally mirrored
   */
  public boolean mirrored() {
    return mirrored;
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
        + ", mirrored="
        + mirrored
        + ", config="
        + config
        + '}';
  }

  /**
   * Loads a map of {@link AnimationConfig}s from a JSON file.
   *
   * @param jsonFilePath path to the JSON file (relative to assets)
   * @return a map of animation name → {@link AnimationConfig}, or {@code null} if a file is not found
   */
  public static Map<String, AnimationConfig> loadAnimationConfigMap(String jsonFilePath) {
    Map<String, AnimationConfig> animationMap = new HashMap<>();

    // Engine-neutral resource lookup (works for libGDX and non-libGDX hosts like LITIENGINE)
    if (jsonFilePath == null || jsonFilePath.isBlank()
      || !core.platform.Platform.resources().exists(jsonFilePath)) {
      return null;
    }

    final String json;
    try (java.io.InputStream in = core.platform.Platform.resources().open(jsonFilePath)) {
      json = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    } catch (Exception e) {
      return null;
    }

    final Map<String, Object> root;
    try {
      root = core.utils.JsonHandler.readJson(json);
    } catch (IllegalArgumentException e) {
      return null;
    }

    if (root.isEmpty()) return animationMap;

    for (Map.Entry<String, Object> animEntry : root.entrySet()) {
      String animationName = animEntry.getKey();
      Map<String, Object> entry = asMap(animEntry.getValue());
      if (entry == null) continue;

      // Read SpritesheetConfig (optional)
      SpritesheetConfig sheetConfig = null;
      Map<String, Object> cfg = asMap(entry.get("config"));
      if (cfg != null) {
        sheetConfig = new SpritesheetConfig();
        sheetConfig.spriteWidth(getInt(cfg, "spriteWidth", sheetConfig.spriteWidth()));
        sheetConfig.spriteHeight(getInt(cfg, "spriteHeight", sheetConfig.spriteHeight()));
        sheetConfig.x(getInt(cfg, "x", sheetConfig.x()));
        sheetConfig.y(getInt(cfg, "y", sheetConfig.y()));
        sheetConfig.rows(getInt(cfg, "rows", sheetConfig.rows()));
        sheetConfig.columns(getInt(cfg, "columns", sheetConfig.columns()));
      }

      // Read AnimationConfig
      AnimationConfig animConfig = new AnimationConfig();
      if (sheetConfig != null) {
        animConfig.config(sheetConfig);
      }

      animConfig.framesPerSprite(getInt(entry, "framesPerSprite", 10));
      animConfig.scaleX(getFloat(entry, "scaleX", 1f));
      animConfig.scaleY(getFloat(entry, "scaleY", 0f));
      animConfig.isLooping(getBoolean(entry, "isLooping", true));
      animConfig.centered(getBoolean(entry, "centered", false));
      animConfig.mirrored(getBoolean(entry, "mirrored", false));

      animationMap.put(animationName, animConfig);
    }

    return animationMap;
  }

  private static Map<String, Object> asMap(Object o) {
    if (o instanceof Map<?, ?> m) {
      Map<String, Object> out = new HashMap<>();
      for (Map.Entry<?, ?> e : m.entrySet()) {
        if (e.getKey() instanceof String k) {
          out.put(k, e.getValue());
        }
      }
      return out;
    }
    return null;
  }

  private static int getInt(Map<String, Object> m, String key, int def) {
    Object v = m.get(key);
    if (v == null) return def;
    if (v instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(String.valueOf(v));
    } catch (Exception ignored) {
      return def;
    }
  }

  private static float getFloat(Map<String, Object> m, String key, float def) {
    Object v = m.get(key);
    if (v == null) return def;
    if (v instanceof Number n) return n.floatValue();
    try {
      return Float.parseFloat(String.valueOf(v));
    } catch (Exception ignored) {
      return def;
    }
  }

  private static boolean getBoolean(Map<String, Object> m, String key, boolean def) {
    Object v = m.get(key);
    if (v == null) return def;
    if (v instanceof Boolean b) return b;
    return Boolean.parseBoolean(String.valueOf(v));
  }

  @Override
  public AnimationConfig clone() throws CloneNotSupportedException {
    AnimationConfig cloned = (AnimationConfig) super.clone();

    // Deep copy spritesheet config if present
    if (this.config != null) {
      cloned.config = this.config.clone();
    }

    return cloned;
  }
}
