package rooms.morphco.util;

import com.badlogic.gdx.Input;
import engine.Entity;
import engine.configuration.KeyboardConfig;
import engine.language.Translation;
import engine.utils.Tuple;
import feature.components.CharacterClassComponent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Central access to the localized Morph & Co. texts.
 *
 * <p>The server never sends finished sentences. It sends transport keys that {@link
 * MorphCoTranslator} resolves on the rendering client, so two players can play in different
 * languages.
 */
public final class MorphCoText {

  /** Prefix transported to clients and resolved by {@link MorphCoTranslator}. */
  public static final String KEY_PREFIX = "morphCo.";

  /** Prefix of quest-log keys. */
  public static final String QUEST_PREFIX = "questlog.";

  /** Portrait of the shift lead in speaker dialogs. */
  public static final String SHIFT_LEAD_IMAGE = "rooms/morphCo/shift_lead_portrait.png";

  /** Portrait of a player in speaker dialogs whose character has no portrait of its own. */
  public static final String PLAYER_IMAGE = "rooms/morphCo/player_portrait.png";

  private static final Translation TEXT = new Translation("morphCo");

  private MorphCoText() {}

  /**
   * Resolves a key in the current process language.
   *
   * @param key key relative to the {@code morphCo} namespace
   * @param values optional {@code $1}, {@code $2}, ... arguments
   * @return localized text
   */
  public static String text(String key, Object... values) {
    return TEXT.text(key, values);
  }

  /**
   * Creates a transport key for a server-created dialog.
   *
   * @param key key relative to the {@code morphCo} namespace
   * @param values optional arguments; they are encoded so that they cannot break the key
   * @return transport key
   */
  public static String key(String key, Object... values) {
    return encodedKey(KEY_PREFIX, key, values);
  }

  /**
   * Creates a transport key for a quest-log tab or entry.
   *
   * @param key key relative to the {@code questlog} namespace
   * @param values optional arguments
   * @return transport key
   */
  public static String questKey(String key, Object... values) {
    return encodedKey(QUEST_PREFIX, key, values);
  }

  private static String encodedKey(String prefix, String key, Object... values) {
    String fullKey = prefix + key;
    if (values == null || values.length == 0) return fullKey;
    String encodedValues =
        Stream.of(values)
            .map(String::valueOf)
            .map(MorphCoText::encode)
            .collect(Collectors.joining(","));
    return fullKey + "||" + encodedValues;
  }

  /**
   * Creates a speaker script spoken by the shift lead.
   *
   * @param key story key below {@code morphCo.story}
   * @return keyed speaker script
   */
  public static String shiftLead(String key) {
    return speaker(SHIFT_LEAD_IMAGE, "story.shift-lead", key);
  }

  /**
   * Returns the dialog portrait of a player's character.
   *
   * @param player the player
   * @return portrait path inside the assets
   */
  public static String playerImage(Entity player) {
    return player
        .fetch(CharacterClassComponent.class)
        .map(CharacterClassComponent::characterClass)
        .map(
            characterClass ->
                switch (characterClass) {
                  case TECHNICIAN -> "rooms/morphCo/technician_portrait.png";
                  case THE_LAST_HOUR_ROGUE -> "rooms/morphCo/rogue_portrait.png";
                  default -> PLAYER_IMAGE;
                })
        .orElse(PLAYER_IMAGE);
  }

  /**
   * Creates the arrival conversation between the shift lead and the player.
   *
   * @param playerImage portrait of the player, see {@link #playerImage(Entity)}
   * @return keyed multi-page speaker script
   */
  public static String arrival(String playerImage) {
    return shiftLead("arrival")
        + "[p]"
        + speaker(playerImage, "story.you", "arrival-you")
        + "[p]"
        + shiftLead("arrival-alarm")
        + "[p]"
        + speaker(playerImage, "story.you", "arrival-card");
  }

  private static String speaker(String image, String nameKey, String key) {
    return "[speaker img="
        + image
        + " name=\"[color=black]"
        + key(nameKey)
        + "[/color]\"]"
        + key("story." + key);
  }

  /**
   * Returns the keyed control overview shown once per player.
   *
   * @return keyed control overview
   */
  public static String controls() {
    return "[speaker img="
        + SHIFT_LEAD_IMAGE
        + " name=\"[color=black]"
        + key("story.shift-lead")
        + "[/color]\"]"
        + key(
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
   * Returns the opening pages of the room.
   *
   * @return keyed pages and their font sizes
   */
  public static List<Tuple<String, Integer>> introPages() {
    return List.of(
        Tuple.of(key("intro.page1"), 30),
        Tuple.of(key("intro.page2"), 30),
        Tuple.of(key("intro.page3"), 30),
        Tuple.of(key("intro.page4"), 40),
        Tuple.of(key("title"), 64));
  }

  /**
   * Returns the ending pages after the production has been restarted.
   *
   * @return keyed pages and their font sizes
   */
  public static List<Tuple<String, Integer>> endingPages() {
    return List.of(
        Tuple.of(key("ending.brrr"), 30),
        Tuple.of(key("ending.status"), 40),
        Tuple.of(key("ending.final"), 30),
        Tuple.of(key("title"), 64));
  }

  private static String encode(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }
}
