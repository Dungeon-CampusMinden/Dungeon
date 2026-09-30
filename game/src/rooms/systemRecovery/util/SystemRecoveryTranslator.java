package rooms.systemRecovery.util;

import engine.language.Translation;
import feature.utils.Translator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves System Recovery translation keys on the client that renders the UI. */
public final class SystemRecoveryTranslator extends Translator {

  private static final Pattern KEY_PATTERN =
      Pattern.compile(
          // Translation keys may be a single leaf (for example questlog.hint-prefix) or a
          // nested path (for example questlog.riddle1.tab).
          "(?:systemRecovery|questlog)\\.[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*(?:\\|\\|([^\\s\\]]+))?");

  private final Translation translation = new Translation("systemRecovery");
  private final Translation questLog = new Translation("questlog");

  /** Creates the translator and registers the System Recovery key namespace. */
  public SystemRecoveryTranslator() {
    registerKey(SystemRecoveryText.KEY_PREFIX);
  }

  /** Replaces all System Recovery keys in a dialog or cutscene payload. */
  @Override
  public String translate(String text) {
    Matcher matcher = KEY_PATTERN.matcher(text);
    StringBuffer translated = new StringBuffer();
    while (matcher.find()) {
      String token = matcher.group();
      String[] parts = token.split("\\|\\|", 2);
      boolean isQuestLogKey = parts[0].startsWith("questlog.");
      String prefix = isQuestLogKey ? "questlog." : SystemRecoveryText.KEY_PREFIX;
      String path = parts[0].substring(prefix.length());
      Object[] values = parts.length == 1 ? new Object[0] : decodeValues(parts[1]);
      matcher.appendReplacement(
          translated,
          Matcher.quoteReplacement((isQuestLogKey ? questLog : translation).text(path, values)));
    }
    matcher.appendTail(translated);
    String result = translated.toString();
    // Dynamic values may themselves be System Recovery keys (for example archive status labels).
    // Resolve those nested keys as well, while keeping a hard limit against malformed cycles.
    for (int pass = 0;
        pass < 4 && !result.equals(text) && KEY_PATTERN.matcher(result).find();
        pass++) {
      String next = translate(result);
      if (next.equals(result)) break;
      result = next;
    }
    return result;
  }

  private Object[] decodeValues(String encodedValues) {
    if (encodedValues.isBlank()) return new Object[0];
    String[] values = encodedValues.split(",", -1);
    Object[] decoded = new Object[values.length];
    for (int index = 0; index < values.length; index++) {
      decoded[index] =
          new String(Base64.getUrlDecoder().decode(values[index]), StandardCharsets.UTF_8);
    }
    return decoded;
  }
}
