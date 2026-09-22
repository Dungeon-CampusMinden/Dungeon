package rooms.systemRecovery.util;

import com.badlogic.gdx.Input;
import engine.configuration.KeyboardConfig;
import engine.language.Translation;
import engine.utils.Tuple;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Central access to localized System Recovery story, world and interface text. */
public final class SystemRecoveryText {

  /** Prefix transported to clients and resolved by {@link SystemRecoveryTranslator}. */
  public static final String KEY_PREFIX = "systemRecovery.";

  private static final Translation TEXT = new Translation("systemRecovery");
  private static final Translation QUESTLOG = new Translation("questlog");
  private static final String SPEAKER_IMAGE = "profiles/system_recovery_ai_profile_pixel.png";
  private static final String ECHO_SPEAKER_IMAGE = "profiles/system_recovery_echo_profile_pixel.png";
  private static final String ROBOT_SPEAKER_IMAGE =
      "profiles/system_recovery_search_robot_pixel.png";

  private SystemRecoveryText() {}

  /**
   * Resolves a System Recovery translation key relative to the project namespace.
   *
   * @param key translation key relative to the System Recovery namespace
   * @param values optional format arguments
   * @return localized text for the current process
   */
  public static String text(String key, Object... values) {
    return TEXT.text(key, values);
  }

  /**
   * Creates a transport-safe translation key for a server-created dialog.
   *
   * <p>Arguments are encoded so arbitrary translated text, including line breaks and rich-label
   * markup, can travel inside the same key without being confused with another key.
   *
   * @param key translation key relative to the System Recovery namespace
   * @param values optional format arguments
   * @return transport-safe translation key
   */
  public static String key(String key, Object... values) {
    return encodedKey(KEY_PREFIX, key, values);
  }

  /**
   * Creates a transport-safe quest-log key.
   *
   * <p>Quest-log identifiers remain stable on the server. The quest-log UI resolves these keys only
   * while rendering, so two clients can display the same shared entry in different languages.
   *
   * @param key translation key relative to the System Recovery namespace
   * @param values optional format arguments
   * @return transport-safe quest-log key
   */
  public static String questKey(String key, Object... values) {
    return encodedKey("questlog.", key, values);
  }

  private static String encodedKey(String prefix, String key, Object... values) {
    String fullKey = prefix + key;
    if (values == null || values.length == 0) return fullKey;
    String encodedValues =
        Stream.of(values)
            .map(String::valueOf)
            .map(SystemRecoveryText::encode)
            .collect(Collectors.joining(","));
    return fullKey + "||" + encodedValues;
  }

  /**
   * Resolves a localized quest-log value used by the hint catalog.
   *
   * @param key translation key relative to the quest-log namespace
   * @param values optional format arguments
   * @return localized quest-log text
   */
  public static String quest(String key, Object... values) {
    return QUESTLOG.text(key, values);
  }

  /**
   * Creates a Last Hour-style story script whose text is resolved on the rendering client.
   *
   * @param key story translation key
   * @param values optional format arguments
   * @return keyed story script
   */
  public static String story(String key, Object... values) {
    return "[speaker img="
        + SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + key("story.speaker")
        + "[/color]\"]"
        + key("story." + key, values);
  }

  /**
   * Creates a phone script with the lead AI as the Last Hour-style speaker.
   *
   * @param key phone translation key
   * @param values optional format arguments
   * @return keyed phone script
   */
  public static String phoneCall(String key, Object... values) {
    return "[speaker img="
        + SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + key("story.lead-ai")
        + "[/color]\"]"
        + key("story." + key, values);
  }

  /**
   * Creates a Last Hour-style phone script spoken by the isolated ECHO process.
   *
   * @param key story translation key
   * @param values optional format arguments
   * @return keyed story script
   */
  public static String echoCall(String key, Object... values) {
    return "[speaker img="
        + ECHO_SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + key("story.echo")
        + "[/color]\"]"
        + key("story." + key, values);
  }

  /**
   * Creates a Last Hour-style dialog spoken by the search robot.
   *
   * @param key story translation key
   * @param values optional format arguments
   * @return keyed story script
   */
  public static String robotCall(String key, Object... values) {
    return "[speaker img="
        + ROBOT_SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + key("story.search-robot")
        + "[/color]\"]"
        + key("story." + key, values);
  }

  /**
   * Returns the ECHO avatar used by dialogs that provide an explicit portrait fallback.
   *
   * @return ECHO avatar asset path
   */
  public static String echoSpeakerImage() {
    return ECHO_SPEAKER_IMAGE;
  }

  /**
   * Creates a Last Hour-style system script spoken by AXIOM.
   *
   * @param key story translation key
   * @param values optional format arguments
   * @return keyed story script
   */
  public static String axiomCall(String key, Object... values) {
    return "[speaker img="
        + SPEAKER_IMAGE
        + " name=\"[color=#aaaaaa]"
        + key("story.axiom")
        + "[/color]\"]"
        + key("story." + key, values);
  }

  /**
   * Returns the keyed Last Hour-style control overview for the opening call.
   *
   * @return keyed control overview
   */
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
        feature.input.configuration.KeyboardConfig.QUESTLOG_OPEN.value(),
        feature.input.configuration.KeyboardConfig.CLOSE_UI.value(),
        feature.input.configuration.KeyboardConfig.PAUSE_MENU.value());
  }

  /**
   * Returns keyed opening lore pages for client-side translation.
   *
   * @return opening lore pages and their display durations
   */
  public static List<Tuple<String, Integer>> introPages() {
    return List.of(
        Tuple.of(key("intro.page1"), 32),
        Tuple.of(key("intro.page2"), 32),
        Tuple.of(key("intro.page3"), 32),
        Tuple.of(key("intro.page4"), 32),
        Tuple.of(key("intro.title"), 120));
  }

  /**
   * Returns keyed ending pages shown after the player reaches the final exit point.
   *
   * @return ending lore pages and their display durations
   */
  public static List<Tuple<String, Integer>> endingPages() {
    return List.of(
        Tuple.of(key("outro.page1"), 32),
        Tuple.of(key("outro.page2"), 32),
        Tuple.of(key("outro.page3"), 32),
        Tuple.of(key("outro.title"), 120));
  }

  private static String encode(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
