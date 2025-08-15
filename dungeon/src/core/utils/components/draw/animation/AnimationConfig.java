package core.utils.components.draw.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

public class AnimationConfig {

  private SpritesheetConfig config;
  private int framesPerSprite = 10;
  // Default width/height of 1 tile.
  private float scaleX = 1;
  private float scaleY = 0;
  private boolean isLooping = true;
  private boolean centered = false;

  public AnimationConfig(SpritesheetConfig config) {
    this.config = config;
  }

  public AnimationConfig() {
    this(null);
  }

  public SpritesheetConfig config() {
    return config;
  }

  public AnimationConfig config(SpritesheetConfig config) {
    this.config = config;
    return this;
  }

  public int framesPerSprite() {
    return framesPerSprite;
  }

  public AnimationConfig framesPerSprite(int framesPerSprite) {
    if (framesPerSprite <= 0)
      throw new IllegalArgumentException("framesPerSprite cannot be less than 1");
    this.framesPerSprite = framesPerSprite;
    return this;
  }

  public AnimationConfig scaleX(float scaleX) {
    this.scaleX = scaleX;
    return this;
  }

  public float scaleX() {
    return scaleX;
  }

  public AnimationConfig scaleY(float scaleY) {
    this.scaleY = scaleY;
    return this;
  }

  public float scaleY() {
    return scaleY;
  }

  public AnimationConfig isLooping(boolean looping) {
    isLooping = looping;
    return this;
  }

  public boolean isLooping() {
    return isLooping;
  }

  public AnimationConfig centered(boolean centered) {
    this.centered = centered;
    return this;
  }

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
