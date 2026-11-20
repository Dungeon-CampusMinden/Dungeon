package engine.utils.components.draw.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// tests for @link AnimationConfig
class AnimationConfigTest {

  private static final String SHEET =
      "\"config\": {\"spriteWidth\": 32, \"spriteHeight\": 24, \"x\": 1, \"y\": 2,"
          + " \"rows\": 3, \"columns\": 4}";

  @TempDir Path tempDir;

  // loadAnimationConfigMap reads via Gdx.files.internal, which also accepts an absolute path
  private Map<String, AnimationConfig> load(String json) throws IOException {
    Path file = Files.writeString(tempDir.resolve("animations.json"), json);
    return AnimationConfig.loadAnimationConfigMap(file.toString().replace('\\', '/'));
  }

  @Test
  void defaultValues() {
    AnimationConfig config = new AnimationConfig();
    assertTrue(config.config().isEmpty());
    assertEquals(10, config.framesPerSprite());
    assertEquals(1f, config.scaleX());
    assertEquals(0f, config.scaleY());
    assertTrue(config.isLooping());
    assertFalse(config.centered());
    assertFalse(config.mirrored());
  }

  @Test
  void spritesheetFromConstructorIsKept() {
    SpritesheetConfig sheet = new SpritesheetConfig();
    assertSame(sheet, new AnimationConfig(sheet).config().orElseThrow());
  }

  @Test
  void settersChangeValuesAndAllowChaining() {
    AnimationConfig config = new AnimationConfig();
    SpritesheetConfig sheet = new SpritesheetConfig();
    AnimationConfig returned =
        config
            .config(sheet)
            .framesPerSprite(3)
            .scaleX(2f)
            .scaleY(0.5f)
            .isLooping(false)
            .centered(true)
            .mirrored(true);
    assertSame(config, returned);
    assertSame(sheet, config.config().orElseThrow());
    assertEquals(3, config.framesPerSprite());
    assertEquals(2f, config.scaleX());
    assertEquals(0.5f, config.scaleY());
    assertFalse(config.isLooping());
    assertTrue(config.centered());
    assertTrue(config.mirrored());
  }

  @Test
  void spritesheetCanBeRemovedAgain() {
    AnimationConfig config = new AnimationConfig(new SpritesheetConfig());
    config.config(null);
    assertTrue(config.config().isEmpty());
  }

  @Test
  void framesPerSpriteOfOneIsAccepted() {
    assertEquals(1, new AnimationConfig().framesPerSprite(1).framesPerSprite());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void framesPerSpriteBelowOneIsRejected(int framesPerSprite) {
    AnimationConfig config = new AnimationConfig().framesPerSprite(5);
    assertThrows(IllegalArgumentException.class, () -> config.framesPerSprite(framesPerSprite));
    assertEquals(5, config.framesPerSprite());
  }

  @Test
  void cloneAlsoCopiesTheSpritesheet() throws CloneNotSupportedException {
    AnimationConfig original = new AnimationConfig(new SpritesheetConfig(1, 2)).framesPerSprite(3);
    AnimationConfig copy = original.clone();
    assertNotSame(original, copy);
    assertEquals(3, copy.framesPerSprite());

    SpritesheetConfig originalSheet = original.config().orElseThrow();
    SpritesheetConfig copiedSheet = copy.config().orElseThrow();
    assertNotSame(originalSheet, copiedSheet);
    copiedSheet.x(99);
    assertEquals(1, originalSheet.x());
  }

  @Test
  void cloneWithoutSpritesheet() throws CloneNotSupportedException {
    assertTrue(new AnimationConfig().clone().config().isEmpty());
  }

  @Test
  void loadingAMissingFileReturnsNull() {
    String missing = tempDir.resolve("missing.json").toString().replace('\\', '/');
    assertNull(AnimationConfig.loadAnimationConfigMap(missing));
  }

  @Test
  void loadingReadsAllValues() throws IOException {
    Map<String, AnimationConfig> map =
        load(
            "{\"run\": {"
                + SHEET
                + ", \"framesPerSprite\": 5, \"scaleX\": 2.0, \"scaleY\": 0.5,"
                + " \"isLooping\": false, \"centered\": true, \"mirrored\": true}}");
    assertEquals(Set.of("run"), map.keySet());

    AnimationConfig run = map.get("run");
    assertEquals(5, run.framesPerSprite());
    assertEquals(2f, run.scaleX());
    assertEquals(0.5f, run.scaleY());
    assertFalse(run.isLooping());
    assertTrue(run.centered());
    assertTrue(run.mirrored());

    SpritesheetConfig sheet = run.config().orElseThrow();
    assertEquals(32, sheet.spriteWidth());
    assertEquals(24, sheet.spriteHeight());
    assertEquals(1, sheet.x());
    assertEquals(2, sheet.y());
    assertEquals(3, sheet.rows());
    assertEquals(4, sheet.columns());
  }

  @Test
  void loadingUsesDefaultsForMissingValues() throws IOException {
    AnimationConfig idle = load("{\"idle\": {" + SHEET + "}}").get("idle");
    assertEquals(10, idle.framesPerSprite());
    assertEquals(1f, idle.scaleX());
    assertEquals(0f, idle.scaleY());
    assertTrue(idle.isLooping());
    assertFalse(idle.centered());
    assertFalse(idle.mirrored());
  }

  @Test
  void loadingReadsEveryAnimation() throws IOException {
    Map<String, AnimationConfig> map =
        load("{\"idle\": {" + SHEET + "}, \"run\": {" + SHEET + "}}");
    assertEquals(Set.of("idle", "run"), map.keySet());
  }

  // documents the current behavior: a missing "config" block is not checked
  @Test
  void loadingAnAnimationWithoutSpritesheetThrows() {
    assertThrows(NullPointerException.class, () -> load("{\"idle\": {\"framesPerSprite\": 5}}"));
  }
}
