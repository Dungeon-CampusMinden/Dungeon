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
   * @param values optional arguments, which may be transport keys themselves; they are encoded so
   *     that they cannot break the key
   * @return transport key
   */
  public static String key(String key, Object... values) {
    String fullKey = KEY_PREFIX + key;
    if (values.length == 0) return fullKey;
    return fullKey
        + "||"
        + Stream.of(values)
            .map(String::valueOf)
            .map(
                value ->
                    Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(value.getBytes(StandardCharsets.UTF_8)))
            .collect(Collectors.joining(","));
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
