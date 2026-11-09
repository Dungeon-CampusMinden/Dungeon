package rooms.soulweaver.util;

import engine.language.Language;
import engine.language.Translation;
import feature.utils.Translator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves Soulweaver transport keys on the client that renders a dialog or the quest log. */
public final class SoulweaverTranslator extends Translator {

  // Arguments are Base64url values; the closed alphabet keeps markup such as [/color] outside.
  private static final Pattern KEY_PATTERN =
      Pattern.compile(
          "soulweaver\\.[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*(?:\\|\\|([A-Za-z0-9_,-]*))?");

  private final Translation translation = new Translation("soulweaver");

  /** Creates the translator for the current language and registers the room's key namespace. */
  public SoulweaverTranslator() {
    registerKey(SoulweaverText.KEY_PREFIX);
  }

  SoulweaverTranslator(Language language) {
    this();
    translation.language(language);
  }

  /** Replaces every Soulweaver key in a dialog, cutscene, quest-log or room-state text. */
  @Override
  public String translate(String text) {
    String result = replaceKeys(text);
    // Arguments may themselves be keys, for example the label of a vessel in a feedback line.
    for (int pass = 0; pass < 3 && KEY_PATTERN.matcher(result).find(); pass++) {
      String next = replaceKeys(result);
      if (next.equals(result)) break;
      result = next;
    }
    return result;
  }

  private String replaceKeys(String text) {
    Matcher matcher = KEY_PATTERN.matcher(text);
    StringBuilder translated = new StringBuilder();
    while (matcher.find()) {
      String[] parts = matcher.group().split("\\|\\|", 2);
      Object[] values = parts.length == 1 ? new Object[0] : decode(parts[1]);
      String value =
          translation.text(parts[0].substring(SoulweaverText.KEY_PREFIX.length()), values);
      matcher.appendReplacement(translated, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(translated);
    return translated.toString();
  }

  private static Object[] decode(String encodedValues) {
    if (encodedValues.isEmpty()) return new Object[0];
    String[] values = encodedValues.split(",", -1);
    Object[] decoded = new Object[values.length];
    for (int index = 0; index < values.length; index++) {
      try {
        decoded[index] =
            new String(Base64.getUrlDecoder().decode(values[index]), StandardCharsets.UTF_8);
      } catch (IllegalArgumentException malformed) {
        // Player notes in the quest log pass through here and may contain key-like text.
        decoded[index] = values[index];
      }
    }
    return decoded;
  }
}
