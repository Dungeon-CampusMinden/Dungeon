package rooms.systemRecovery.util;

import com.badlogic.gdx.Input;
import engine.configuration.KeyboardConfig;
import engine.language.Translation;
import engine.utils.Tuple;
import java.util.List;

/** Central access to localized System Recovery story, world and interface text. */
public final class SystemRecoveryText {

  private static final Translation TEXT = new Translation("systemRecovery");
  private static final Translation QUESTLOG = new Translation("questlog");
  private static final String SPEAKER_IMAGE = "logo/cat_logo_64x64.png";

  private SystemRecoveryText() {}

  /** Resolves a System Recovery translation key relative to the project namespace. */
  public static String text(String key, Object... values) {
    return TEXT.text(key, values);
  }

  /** Resolves a localized quest-log value used by the hint catalog. */
  public static String quest(String key, Object... values) {
    return QUESTLOG.text(key, values);
  }

  /** Resolves a story message and adds the remote user's Last Hour-style speaker header. */
  public static String story(String key, Object... values) {
    return "[speaker img="
        + SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + text("story.speaker")
        + "[/color]\"]"
        + text("story." + key, values);
  }

  /** Resolves an opening phone call with the lead AI as the Last Hour-style speaker. */
  public static String phoneCall(String key, Object... values) {
    return "[speaker img="
        + SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + text("story.lead-ai")
        + "[/color]\"]"
        + text("story." + key, values);
  }

  /** Returns the localized Last Hour-style control overview for the opening call. */
  public static String controls() {
    return phoneCall(
        "controls",
        KeyboardConfig.MOVEMENT_UP.value(),
        KeyboardConfig.MOVEMENT_LEFT.value(),
        KeyboardConfig.MOVEMENT_DOWN.value(),
        KeyboardConfig.MOVEMENT_RIGHT.value(),
        feature.input.configuration.KeyboardConfig.INTERACT_WORLD.value(),
        Input.Buttons.LEFT,
        feature.input.configuration.KeyboardConfig.INVENTORY_OPEN.value(),
        feature.input.configuration.KeyboardConfig.CLOSE_UI.value(),
        feature.input.configuration.KeyboardConfig.PAUSE_MENU.value());
  }

  /** Returns the localized opening lore pages for the room. */
  public static List<Tuple<String, Integer>> introPages() {
    return List.of(
        Tuple.of(text("intro.page1"), 32),
        Tuple.of(text("intro.page2"), 32),
        Tuple.of(text("intro.page3"), 32),
        Tuple.of(text("intro.page4"), 32),
        Tuple.of(text("intro.page5"), 32),
        Tuple.of(text("intro.title"), 120));
  }

  /** Returns the localized ending pages shown after the player reaches the final exit point. */
  public static List<Tuple<String, Integer>> endingPages() {
    return List.of(
        Tuple.of(text("outro.page1"), 32),
        Tuple.of(text("outro.page2"), 32),
        Tuple.of(text("outro.page3"), 32),
        Tuple.of(text("outro.title"), 120));
  }
}
