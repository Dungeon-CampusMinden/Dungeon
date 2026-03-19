package core.language;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import core.utils.JsonHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Interface for selected languages.
 *
 * <p>Enables switching languages using JSON files.
 */
public class Localization {
  private static final Language FALLBACK_LANGUAGE = Language.DE;
  private static Language CURRENT_LANGUAGE = FALLBACK_LANGUAGE;

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
   * Gets the value behind a JSON node path for the selected language.
   *
   * @param jsonNodes Nodes of the JSON up to the desired value as single params.
   * @return value behind a JSON node path.
   */
  public static String text(String jsonNodes) throws IOException {
    String jsonPath = "language/";
    FileHandle fileHandler = Gdx.files.internal(jsonPath + currentLanguage().toString() + ".json");
    String[] nodes = jsonNodes.split("\\.");

    String jsonString = fileHandler.readString(StandardCharsets.UTF_8.name());
    ;
    String text = JsonHandler.getValueByPath(jsonString, nodes);

    if (text != null) {
      return text;
    } else {
      return fallbackText(jsonNodes);
    }
  }

  /* In the case that there is no value for the selected language. */
  private static String fallbackText(String jsonNodes) throws IOException {
    String jsonPath = "language/";
    FileHandle fileHandler = Gdx.files.internal(jsonPath + FALLBACK_LANGUAGE.toString() + ".json");
    String[] nodes = jsonNodes.split("\\.");

    String jsonString = fileHandler.readString(StandardCharsets.UTF_8.name());
    ;
    String text = JsonHandler.getValueByPath(jsonString, nodes);

    if (text != null) {
      return text;
    } else {
      return "Text not found!";
    }
  }

  /**
   * Adds language suffix to file.
   *
   * @param basePath Path of the base such as 'assets/images/open-book.png'.
   * @return FileHandler for the current asset.
   */
  public static String asset(String basePath) {
    FileHandle handler = Gdx.files.internal(addSuffix(basePath, false));

    if (handler.exists()) {
      return handler.path();
    } else {
      return Gdx.files.internal(addSuffix(basePath, true)).path();
    }
  }

  private static String addSuffix(String filePath, boolean fallback) {
    int index = filePath.lastIndexOf(".");
    String name = filePath.substring(0, index);
    String fileFormat = filePath.substring(index);

    if (fallback) {
      return name + "_" + FALLBACK_LANGUAGE.toString() + fileFormat;
    } else {
      return name + "_" + currentLanguage().toString() + fileFormat;
    }
  }
}
