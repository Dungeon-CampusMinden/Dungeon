package engine.language;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Text authored in German and English inside a JSON asset, with fallback to the other supplied
 * language.
 *
 * @param values language code to text mapping
 */
public record LocalizedText(Map<String, String> values) {
  /**
   * Creates localized text and rejects empty translations.
   *
   * @param values language code to text mapping
   */
  public LocalizedText {
    Objects.requireNonNull(values, "values");
    Map<String, String> copy = new LinkedHashMap<>();
    values.forEach(
        (language, value) -> {
          String normalizedLanguage = language == null ? "" : language.toLowerCase();
          if (!normalizedLanguage.equals("de") && !normalizedLanguage.equals("en")) {
            throw new IllegalArgumentException("text language must be 'de' or 'en': " + language);
          }
          if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("translations must not be blank");
          }
          copy.put(normalizedLanguage, value.trim());
        });
    if (copy.isEmpty()) {
      throw new IllegalArgumentException("text must contain at least one translation");
    }
    values = Map.copyOf(copy);
  }

  /**
   * Resolves text for the requested language, falling back to the other provided language.
   *
   * @param language preferred language
   * @return localized text
   */
  public String text(Language language) {
    Objects.requireNonNull(language, "language");
    String requested = language.toString();
    String text = values.get(requested);
    if (text != null) return text;
    text = values.get("de");
    if (text != null) return text;
    return values.get("en");
  }
}
