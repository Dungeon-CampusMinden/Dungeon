package core.language;

import com.badlogic.gdx.Gdx;
import core.utils.components.draw.TextureMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Manages the global language state used for localization.
 *
 * <p>Holds the current and fallback language and enables switching between them. It also keeps a
 * registry of translation files per language; additional files can be registered via {@link
 * #registerTranslationFile(Language, String)} to support multiple sources, e.g. one file for the
 * core project and another for an implementing project.
 *
 * <p>The actual fetching of translations is delegated to {@link Translation}. For convenience, a
 * default {@link Translation} without a base key is exposed through {@link #text(String)}, so
 * translations can be retrieved without creating a dedicated {@link Translation} instance.
 */
public class Localization {
  private static final Localization INSTANCE = new Localization();

  private final Language fallbackLanguage = Language.DE;
  private Language currentLanguage = fallbackLanguage;

  /** Base directory of the core translation files, registered for every language by default. */
  private static final String DEFAULT_TRANSLATION_PATH = "language_default/";

  /** Registered translation files per language, stored in registration order. */
  private final Map<Language, List<TranslationFile>> translationFiles =
      new EnumMap<>(Language.class);

  /** Listeners notified whenever {@link #currentLanguage(Language)} changes the language. */
  private final List<Consumer<Language>> languageChangeListeners = new ArrayList<>();

  /** Default translation without a base key, backing the static {@link #text(String)} shortcut. */
  private final Translation defaultTranslation = new Translation();

  private Localization() {
    // Register the core translation file (e.g. "language/de.json") for every language by default.
    for (Language language : Language.values()) {
      registerTranslationFile(language, DEFAULT_TRANSLATION_PATH + language + ".json");
    }
  }

  /**
   * Returns the singleton instance that manages global localization state.
   *
   * @return The singleton instance
   */
  public static Localization getInstance() {
    return INSTANCE;
  }

  /**
   * Gets the current language.
   *
   * @return current language.
   */
  public Language currentLanguage() {
    return currentLanguage;
  }

  /**
   * Sets the current language.
   *
   * <p>If the language actually changes, all {@linkplain #registerLanguageChangeListener(Consumer)
   * registered listeners} are notified with the new language. This allows already-rendered UI (such
   * as the main menu) to refresh its localized texts.
   *
   * @param currentLanguage Language currently in use.
   */
  public void currentLanguage(Language currentLanguage) {
    if (this.currentLanguage == currentLanguage) {
      return;
    }
    this.currentLanguage = currentLanguage;
    // Iterate over a copy so listeners may safely unregister themselves while being notified.
    for (Consumer<Language> listener : new ArrayList<>(languageChangeListeners)) {
      listener.accept(currentLanguage);
    }
  }

  /**
   * Registers a listener that is notified whenever the {@linkplain #currentLanguage() current
   * language} changes.
   *
   * <p>Listeners are typically used to refresh already-built UI texts. Remember to {@linkplain
   * #removeLanguageChangeListener(Consumer) remove} the listener again once the owning UI is
   * disposed to avoid stale references.
   *
   * @param listener listener invoked with the new language whenever it changes.
   */
  public void registerLanguageChangeListener(Consumer<Language> listener) {
    languageChangeListeners.add(Objects.requireNonNull(listener, "listener"));
  }

  /**
   * Removes a previously {@linkplain #registerLanguageChangeListener(Consumer) registered} language
   * change listener.
   *
   * @param listener listener to remove; no-op if it was not registered.
   */
  public void removeLanguageChangeListener(Consumer<Language> listener) {
    languageChangeListeners.remove(listener);
  }

  /**
   * Gets the fallback language used when a translation is missing for the current language.
   *
   * @return fallback language.
   */
  public Language fallbackLanguage() {
    return fallbackLanguage;
  }

  /**
   * Gets the value behind a JSON node path for the selected language.
   *
   * <p>This is a convenience shortcut that defers to an internal {@link Translation} without a base
   * key. Create a dedicated {@link Translation} instance when a base key is desired.
   *
   * @param jsonNodes Nodes of the JSON up to the desired value as single params.
   * @return value behind a JSON node path.
   * @throws IOException if the language file cannot be read.
   */
  public String text(String jsonNodes) throws IOException {
    return defaultTranslation.text(jsonNodes);
  }

  /**
   * Gets the value behind a JSON node path for the selected language and applies template values.
   *
   * <p>Template placeholders use 1-based positional indices in the form {@code $1}, {@code $2}, ...
   * and map to {@code templateValues[0]}, {@code templateValues[1]}, ... . Use {@code $$1} to
   * render a literal {@code $1} (escaped placeholder).
   *
   * @param jsonNodes Nodes of the JSON up to the desired value as single params.
   * @param templateValues positional template values used for {@code $1}, {@code $2}, ...
   * @return value behind a JSON node path with templates resolved.
   * @throws IOException if the language file cannot be read.
   */
  public String text(String jsonNodes, Object... templateValues) throws IOException {
    return defaultTranslation.text(jsonNodes, templateValues);
  }

  /**
   * Registers an additional translation file for the given language.
   *
   * <p>This enables multiple translation sources per language, e.g. one file with the core
   * project's dialogs and another with an implementing project's own texts. When a key is
   * requested, the registered files are searched with last-registration-wins precedence: files
   * added later are treated as more specific and can override keys from earlier files. Files are
   * read and cached lazily the first time a translation is requested from them.
   *
   * @param language Language the translation file provides translations for.
   * @param path Path to the translation JSON file, e.g. "language/en.json".
   */
  public void registerTranslationFile(Language language, String path) {
    translationFiles
        .computeIfAbsent(language, key -> new ArrayList<>())
        .add(new TranslationFile(path));
  }

  /**
   * Gets the translation files registered for the given language in registration order.
   *
   * @param language Language to get the registered translation files for.
   * @return the registered translation files, or an empty list if none are registered.
   */
  List<TranslationFile> translationFiles(Language language) {
    return translationFiles.getOrDefault(language, List.of());
  }

  /**
   * Resolves the localized variant of an asset path for the current language.
   *
   * <p>The current language suffix is appended to the file name, e.g. {@code images/open-book.png}
   * becomes {@code images/open-book_en.png}. If no asset exists for the current language - neither
   * as an internal file nor as a texture registered in {@link TextureMap} - the fallback language
   * variant is tried next. If neither localized variant exists, the original unsuffixed {@code
   * basePath} is returned when available (for language-independent assets).
   *
   * @param basePath Path of the base asset such as 'images/open-book.png'.
   * @return path to the localized asset, or the fallback language variant if it does not exist.
   */
  public String asset(String basePath) {
    String localizedPath = addSuffix(basePath, false);

    if (assetExists(localizedPath)) {
      return localizedPath;
    }

    String fallbackPath = addSuffix(basePath, true);
    if (assetExists(fallbackPath)) {
      return fallbackPath;
    }

    return basePath; // Return basePath for language-independent assets
  }

  /* An asset is available if it exists as an internal file or is registered in the TextureMap. */
  private boolean assetExists(String path) {
    return Gdx.files.internal(path).exists() || TextureMap.instance().containsKey(path);
  }

  private String addSuffix(String filePath, boolean fallback) {
    int index = filePath.lastIndexOf(".");
    String name = filePath.substring(0, index);
    String fileFormat = filePath.substring(index);

    if (fallback) {
      return name + "_" + fallbackLanguage + fileFormat;
    } else {
      return name + "_" + currentLanguage().toString() + fileFormat;
    }
  }
}
