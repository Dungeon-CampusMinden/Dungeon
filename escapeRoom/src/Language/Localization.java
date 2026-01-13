package Language;

import com.badlogic.gdx.files.FileHandle;

/**
 * Interface for selected languages.
 *
 * <p>
 *  Enables switching languages using JSON files.
 * </p>
 */
public class Localization {
  private static final Language FALLBACK_LANGUAGE = Language.DE;
  private static Language CURRENT_LANGUAGE = FALLBACK_LANGUAGE;
  private static Localization INSTANCE;

  private Localization(){}

  /**
   * Get Instance of this class.
   *
   * @return INSTANCE of this class.
   */
  public static Localization getInstance() {
    if(INSTANCE == null) {
      INSTANCE= new Localization();
    }
    return INSTANCE;
  }

  /**
   * Get the current language.
   *
   * @return current language.
   */
  public static Language currentLanguage() {
    return CURRENT_LANGUAGE;
  }

  /**
   * Set the current language.
   *
   * @param currentLanguage Language currently in use.
   */
  public static void currentLanguage(Language currentLanguage) {
    CURRENT_LANGUAGE = currentLanguage;
  }

  /**
   * Get the translation of the text.
   *
   * @param key Key of what should be translated.
   * @return translation of the text.
   */
  public String text(String key) {
    return null;
  }

  /**
   * Adds language suffix to file.
   *
   * @param basePath Path of the base such as 'assets/images/open-book.png'.
   * @return FileHandler for the current asset.
   */
  public FileHandle asset(String basePath) {
    return null;
  }
}
