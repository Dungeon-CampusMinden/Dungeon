package rooms.morphco.util;

import engine.language.Translation;
import feature.utils.Translator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves Morph & Co. transport keys on the client that renders a dialog or the quest log. */
public final class MorphCoTranslator extends Translator {

  private static final Pattern KEY_PATTERN =
      Pattern.compile(
          "(?:morphCo|questlog)\\.[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*(?:\\|\\|([^\\s\\]]+))?");

  private final Translation translation = new Translation("morphCo");
  private final Translation questLog = new Translation("questlog");

  /** Creates the translator and registers the room's key namespaces. */
  public MorphCoTranslator() {
    registerKey(MorphCoText.KEY_PREFIX);
    registerKey(MorphCoText.QUEST_PREFIX);
  }

  /** Replaces every Morph & Co. key in a dialog, cutscene or quest-log payload. */
  @Override
  public String translate(String text) {
    if (text == null) return null;
    String result = replaceKeys(text);
    // Arguments may themselves be keys, for example the label of a repaired machine.
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
      boolean quest = parts[0].startsWith(MorphCoText.QUEST_PREFIX);
      String prefix = quest ? MorphCoText.QUEST_PREFIX : MorphCoText.KEY_PREFIX;
      String path = parts[0].substring(prefix.length());
      Object[] values = parts.length == 1 ? new Object[0] : decode(parts[1]);
      String value = (quest ? questLog : translation).text(path, values);
      matcher.appendReplacement(translated, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(translated);
    return translated.toString();
  }

  private static Object[] decode(String encodedValues) {
    if (encodedValues.isBlank()) return new Object[0];
    String[] values = encodedValues.split(",", -1);
    Object[] decoded = new Object[values.length];
    for (int index = 0; index < values.length; index++) {
      try {
        decoded[index] =
            new String(Base64.getUrlDecoder().decode(values[index]), StandardCharsets.UTF_8);
      } catch (IllegalArgumentException malformed) {
        decoded[index] = values[index];
      }
    }
    return decoded;
  }
}
