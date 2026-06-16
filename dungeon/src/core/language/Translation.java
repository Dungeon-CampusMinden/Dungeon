package core.language;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches translations from the translation files registered in {@link Localization}.
 *
 * <p>A {@code Translation} is stateful: it can be configured with a base key that is prepended to
 * every requested key. This simplifies repeated lookups that share a common JSON sub-tree.
 *
 * <p>Example:
 *
 * <pre>{@code
 * Translation t = new Translation("dialog.multiple_choice");
 * t.text("cancel"); // resolves the key "dialog.multiple_choice.cancel"
 * }</pre>
 *
 * <p>The language used for the lookup can be set explicitly via {@link #language(Language)}. When
 * it is {@code null} (the default), the current language of {@link Localization} is used. A key is
 * looked up in all translation files registered for that language; later registrations take
 * precedence over earlier ones (last registered file wins).
 */
public class Translation {

  private static final Pattern ESCAPED_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\$(\\d+)");
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$(\\d+)");
  private static final Pattern PROTECTED_PLACEHOLDER_PATTERN =
      Pattern.compile("__TPL_ESC_\\((\\d+)\\)__");

  private final String baseKey;
  private Language language = null;

  /** Creates a {@code Translation} without a base key. */
  public Translation() {
    this("");
  }

  /**
   * Creates a {@code Translation} with the given base key.
   *
   * @param baseKey Key prepended to every translation request, e.g. "dialog.multiple_choice". A
   *     {@code null} value is treated as no base key.
   */
  public Translation(String baseKey) {
    this.baseKey = baseKey == null ? "" : baseKey;
  }

  /**
   * Gets the language used by this {@code Translation}.
   *
   * @return the configured language, or {@code null} when the current language of {@link
   *     Localization} is used.
   */
  public Language language() {
    return language;
  }

  /**
   * Sets the language used by this {@code Translation}.
   *
   * @param language Language to use, or {@code null} to use the current language of {@link
   *     Localization}.
   */
  public void language(Language language) {
    this.language = language;
  }

  /**
   * Gets the value behind a JSON node path for the selected language.
   *
   * <p>The configured base key is prepended to the given node path before the lookup. The key is
   * searched in every translation file registered for the active language. If no value is found,
   * the fallback language of {@link Localization} is searched the same way.
   *
   * @param jsonNodes Nodes of the JSON up to the desired value as single params, relative to the
   *     base key.
   * @return value behind a JSON node path.
   */
  public String text(String jsonNodes) {
    return text(jsonNodes, new Object[0]);
  }

  /**
   * Gets the value behind a JSON node path for the selected language and applies template values.
   *
   * <p>Template placeholders use 1-based positional indices in the form {@code $1}, {@code $2}, ...
   * and map to {@code templateValues[0]}, {@code templateValues[1]}, ... . Values are inserted via
   * {@link String#valueOf(Object)}.
   *
   * <p>Escaping is supported: {@code $$1} renders as literal {@code $1} and is not replaced by the
   * first template value.
   *
   * @param jsonNodes Nodes of the JSON up to the desired value as single params, relative to the
   *     base key.
   * @param templateValues positional template values used for {@code $1}, {@code $2}, ...
   * @return value behind a JSON node path with templates resolved.
   */
  public String text(String jsonNodes, Object... templateValues) {
    String fullPath = resolveKey(jsonNodes);
    String[] nodes = fullPath.split("\\.");
    Localization localization = Localization.getInstance();

    String text = lookup(activeLanguage(), nodes);
    if (text != null) {
      return applyTemplate(text, templateValues);
    }

    text = lookup(localization.fallbackLanguage(), nodes);
    if (text != null) {
      return applyTemplate(text, templateValues);
    }

    return "{" + fullPath + "}";
  }

  /*
   * Searches the given node path in all translation files of the given language.
   * Later registrations are more specific and therefore checked first.
   */
  private String lookup(Language language, String[] nodes) {
    var files = Localization.getInstance().translationFiles(language);
    for (int i = files.size() - 1; i >= 0; i--) {
      TranslationFile file = files.get(i);
      String value = file.value(nodes);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /* The language to query: the explicitly set one, or the current language of Localization. */
  private Language activeLanguage() {
    return language != null ? language : Localization.getInstance().currentLanguage();
  }

  /* Prepends the base key (if any) to the requested node path. */
  private String resolveKey(String jsonNodes) {
    if (jsonNodes == null || jsonNodes.isEmpty()) {
      return baseKey;
    }
    if (baseKey.isEmpty()) {
      return jsonNodes;
    }
    return baseKey + "." + jsonNodes;
  }

  /* Applies positional templates ($1, $2, ...) with $$n escaping support. */
  private String applyTemplate(String text, Object... templateValues) {
    String protectedText = protectEscapedPlaceholders(text);

    String withTemplates = protectedText;
    if (templateValues != null && templateValues.length > 0) {
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(protectedText);
      StringBuffer sb = new StringBuffer();
      while (matcher.find()) {
        int placeholderIndex = Integer.parseInt(matcher.group(1)) - 1;
        if (placeholderIndex >= 0 && placeholderIndex < templateValues.length) {
          String replacement = String.valueOf(templateValues[placeholderIndex]);
          matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        } else {
          matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
        }
      }
      matcher.appendTail(sb);
      withTemplates = sb.toString();
    }

    return unprotectEscapedPlaceholders(withTemplates);
  }

  private String protectEscapedPlaceholders(String text) {
    Matcher matcher = ESCAPED_PLACEHOLDER_PATTERN.matcher(text);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(
          sb, Matcher.quoteReplacement("__TPL_ESC_(" + matcher.group(1) + ")__"));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private String unprotectEscapedPlaceholders(String text) {
    Matcher matcher = PROTECTED_PLACEHOLDER_PATTERN.matcher(text);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, Matcher.quoteReplacement("$" + matcher.group(1)));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
