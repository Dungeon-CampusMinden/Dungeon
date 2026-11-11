package rooms.soulweaver.util;

import engine.language.Language;
import engine.language.Translation;
import feature.utils.Translator;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rooms.soulweaver.modules.methods.MethodsWorkshop;

/** Resolves Soulweaver transport keys on the client that renders a dialog or the quest log. */
public final class SoulweaverTranslator extends Translator {

  // Arguments are Base64url values, optionally marked by SoulweaverText; the closed alphabet keeps
  // markup such as [/color] outside. The game's names in a block source never contain a minus, so
  // "kristalle-3" ends the key before the minus.
  private static final Pattern KEY_PATTERN =
      Pattern.compile(
          "soulweaver\\.(?:methods\\.(?:identifier|direction)\\.[A-Za-z0-9_]+"
              + "|[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*)(?:\\|\\|([A-Za-z0-9_,~*-]*))?");

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
    Matcher matcher = KEY_PATTERN.matcher(text);
    StringBuilder translated = new StringBuilder();
    while (matcher.find()) {
      String[] parts = matcher.group().split("\\|\\|", 2);
      Object[] values = parts.length == 1 ? new Object[0] : decode(parts[1]);
      String value =
          translation.text(parts[0].substring(SoulweaverText.KEY_PREFIX.length()), values);
      // A player's quest-log note may contain text that only looks like a key; it stays as typed.
      if (value.equals("{" + parts[0] + "}")) value = matcher.group();
      matcher.appendReplacement(translated, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(translated);
    return translated.toString();
  }

  // Only arguments the server marked are resolved; everything else may be a player's input.
  private Object[] decode(String encodedValues) {
    String[] values = encodedValues.split(",", -1);
    Object[] decoded = new Object[values.length];
    for (int index = 0; index < values.length; index++) {
      String encoded = values[index];
      char kind = encoded.isEmpty() ? ' ' : encoded.charAt(0);
      boolean marked = kind == SoulweaverText.NESTED || kind == SoulweaverText.EXPRESSION;
      String value;
      try {
        value =
            new String(
                Base64.getUrlDecoder().decode(marked ? encoded.substring(1) : encoded),
                StandardCharsets.UTF_8);
      } catch (IllegalArgumentException malformed) {
        // Key-like text in a player's quest-log note need not carry valid arguments.
        decoded[index] = encoded;
        continue;
      }
      decoded[index] =
          switch (kind) {
            case SoulweaverText.NESTED -> translate(value);
            case SoulweaverText.EXPRESSION -> MethodsWorkshop.localized(value, translation::text);
            default -> value;
          };
    }
    return decoded;
  }
}
