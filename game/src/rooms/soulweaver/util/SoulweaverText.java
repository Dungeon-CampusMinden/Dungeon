package rooms.soulweaver.util;

import engine.language.Language;
import engine.language.Translation;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Central access to the localized Soulweaver texts.
 *
 * <p>The server never sends finished sentences. Dialogs, quest-log entries and the text fields of
 * the room states carry transport keys that {@link SoulweaverTranslator} resolves on the rendering
 * client, so two players can play in different languages. Client interfaces resolve their own
 * labels with {@link #text(String, Object...)}.
 */
public final class SoulweaverText {

  /** Prefix transported to clients and resolved by {@link SoulweaverTranslator}. */
  public static final String KEY_PREFIX = "soulweaver.";

  private static final Translation TEXT = new Translation("soulweaver");
  private static final SoulweaverTranslator TRANSLATOR = new SoulweaverTranslator();
  private static final SoulweaverTranslator GERMAN = new SoulweaverTranslator(Language.DE);

  /** Prefix of an encoded argument that is server text with transport keys. */
  static final char NESTED = '~';

  /** Prefix of an encoded argument that is a stored program expression. */
  static final char EXPRESSION = '*';

  private record Marked(char kind, String text) {}

  private SoulweaverText() {}

  /**
   * Resolves a key in the current process language.
   *
   * @param key key relative to the {@code soulweaver} namespace
   * @param values optional {@code $1}, {@code $2}, ... arguments
   * @return localized text
   */
  public static String text(String key, Object... values) {
    return TEXT.text(key, values);
  }

  /**
   * Creates a transport key for text that the server sends to clients.
   *
   * @param key key relative to the {@code soulweaver} namespace
   * @param values optional arguments; they are encoded so that they cannot break the key. Clients
   *     show them verbatim unless they are marked with {@link #nested} or {@link #expression}
   * @return transport key
   */
  public static String key(String key, Object... values) {
    String fullKey = KEY_PREFIX + key;
    if (values.length == 0) return fullKey;
    return fullKey
        + "||"
        + Stream.of(values)
            .map(
                value ->
                    value instanceof Marked marked
                        ? marked.kind() + encode(marked.text())
                        : encode(String.valueOf(value)))
            .collect(Collectors.joining(","));
  }

  /**
   * Marks an argument of {@link #key} as server text that clients resolve, such as the label of a
   * vessel. Unmarked arguments stay verbatim, so a player's input is never read as a key.
   *
   * @param text transport key or text containing transport keys
   * @return argument for {@link #key}
   */
  public static Object nested(String text) {
    return new Marked(NESTED, text);
  }

  /**
   * Marks an argument of {@link #key} as a stored program expression; clients show the game's names
   * and direction literals in it in their language and the player's own names verbatim.
   *
   * @param expression expression or name in its stored spelling
   * @return argument for {@link #key}
   */
  public static Object expression(String expression) {
    return new Marked(EXPRESSION, expression);
  }

  private static String encode(String value) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Resolves all transport keys in a text received from the server.
   *
   * @param text text that may contain transport keys
   * @return text in the current process language
   */
  public static String translate(String text) {
    return TRANSLATOR.translate(text);
  }

  /**
   * Resolves transport keys in German, so tracked failure reasons stay comparable regardless of the
   * language of the hosting process.
   *
   * @param text text that may contain transport keys
   * @return German text
   */
  public static String german(String text) {
    return GERMAN.translate(text);
  }
}
