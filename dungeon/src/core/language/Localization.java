package core.language;

import com.badlogic.gdx.Gdx;
import core.utils.components.draw.TextureMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
  private static final Language FALLBACK_LANGUAGE = Language.DE;
  private static Language CURRENT_LANGUAGE = FALLBACK_LANGUAGE;

  /** Base directory of the core translation files, registered for every language by default. */
  private static final String DEFAULT_TRANSLATION_PATH = "language_default/";

  /** Registered translation files per language, stored in registration order. */
  private static final Map<Language, List<TranslationFile>> TRANSLATION_FILES =
      new EnumMap<>(Language.class);

  /** Default translation without a base key, backing the static {@link #text(String)} shortcut. */
  private static final Translation DEFAULT_TRANSLATION = new Translation();

  static {
    // Register the core translation file (e.g. "language/de.json") for every language by default.
    for (Language language : Language.values()) {
      registerTranslationFile(language, DEFAULT_TRANSLATION_PATH + language + ".json");
    }
  }

  private Localization() {}

  /**
   * Gets the current language.
   *
   * @return current language.
   */
  public static Language currentLanguage() {
    return CURRENT_LANGUAGE;
  }

  /**
   * Sets the current language.
   *
   * @param currentLanguage Language currently in use.
   */
  public static void currentLanguage(Language currentLanguage) {
    CURRENT_LANGUAGE = currentLanguage;
  }

  /**
   * Gets the fallback language used when a translation is missing for the current language.
   *
   * @return fallback language.
   */
  public static Language fallbackLanguage() {
    return FALLBACK_LANGUAGE;
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
  public static String text(String jsonNodes) throws IOException {
    return DEFAULT_TRANSLATION.text(jsonNodes);
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
  public static String text(String jsonNodes, Object... templateValues) throws IOException {
    return DEFAULT_TRANSLATION.text(jsonNodes, templateValues);
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
  public static void registerTranslationFile(Language language, String path) {
    TRANSLATION_FILES
        .computeIfAbsent(language, key -> new ArrayList<>())
        .add(new TranslationFile(path));
  }

  /**
   * Gets the translation files registered for the given language in registration order.
   *
   * @param language Language to get the registered translation files for.
   * @return the registered translation files, or an empty list if none are registered.
   */
  static List<TranslationFile> translationFiles(Language language) {
    return TRANSLATION_FILES.getOrDefault(language, List.of());
  }

  /**
   * Resolves the localized variant of an asset path for the current language.
   *
   * <p>The current language suffix is appended to the file name, e.g. {@code images/open-book.png}
   * becomes {@code images/open-book_en.png}. If no asset exists for the current language - neither
   * as an internal file nor as a texture registered in {@link TextureMap} - the fallback language
   * variant is returned instead.
   *
   * @param basePath Path of the base asset such as 'images/open-book.png'.
   * @return path to the localized asset, or the fallback language variant if it does not exist.
   */
  public static String asset(String basePath) {
    String localizedPath = addSuffix(basePath, false);

    if (assetExists(localizedPath)) {
      return localizedPath;
    } else {
      return addSuffix(basePath, true);
    }
  }

  /* An asset is available if it exists as an internal file or is registered in the TextureMap. */
  private static boolean assetExists(String path) {
    return Gdx.files.internal(path).exists() || TextureMap.instance().containsKey(path);
  }

  private static String addSuffix(String filePath, boolean fallback) {
    int index = filePath.lastIndexOf(".");
    String name = filePath.substring(0, index);
    String fileFormat = filePath.substring(index);

    if (fallback) {
      return name + "_" + FALLBACK_LANGUAGE + fileFormat;
    } else {
      return name + "_" + currentLanguage().toString() + fileFormat;
    }
  }
}
