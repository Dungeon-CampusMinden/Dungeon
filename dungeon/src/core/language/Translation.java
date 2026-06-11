package core.language;

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
 * looked up in all translation files registered for that language (in registration order); the
 * first file that contains the key wins.
 */
public class Translation {

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
    String fullPath = resolveKey(jsonNodes);
    String[] nodes = fullPath.split("\\.");

    String text = lookup(activeLanguage(), nodes);
    if (text != null) {
      return text;
    }

    text = lookup(Localization.fallbackLanguage(), nodes);
    if (text != null) {
      return text;
    }

    return "{" + fullPath + "}";
  }

  /* Searches the given node path in all translation files of the given language. */
  private String lookup(Language language, String[] nodes) {
    for (TranslationFile file : Localization.translationFiles(language)) {
      String value = file.value(nodes);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /* The language to query: the explicitly set one, or the current language of Localization. */
  private Language activeLanguage() {
    return language != null ? language : Localization.currentLanguage();
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
}
