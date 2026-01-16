package core.language;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import core.utils.JsonHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Interface for selected languages.
 *
 * <p>Enables switching languages using JSON files.
 */
public class Localization {
  private static final Language FALLBACK_LANGUAGE = Language.DE;
  private static Language CURRENT_LANGUAGE = FALLBACK_LANGUAGE;
  private static Localization INSTANCE;

  private Localization() {}

  /**
   * Gets Instance of this class.
   *
   * @return INSTANCE of this class.
   */
  public static Localization getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Localization();
    }
    return INSTANCE;
  }

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
   * Gets the translation of the text.
   *
   * @param key Key of what should be translated.
   * @return translation of the text.
   */
  public String text(String key) throws IOException {
    String jsonPath = "dungeon/assets/language/";
    File file = new File(jsonPath + currentLanguage().toString() + ".json");
    try {
      String JsonAsString = readFileContent(file);
      return JsonHandler.readJson(JsonAsString).get(key).toString();
    } catch (IOException exception) {
      throw new IOException("Failed to read File", exception);
    }
  }

  /**
   * Adds language suffix to file.
   *
   * @param basePath Path of the base such as 'assets/images/open-book.png'.
   * @return FileHandler for the current asset.
   */
  public FileHandle asset(String basePath) {
    FileHandle fileHandle = Gdx.files.internal(addSuffix(basePath));

    if (fileHandle.exists()) {
      return fileHandle;
    } else {
      return Gdx.files.internal(addSuffix(FALLBACK_LANGUAGE.toString()));
    }
  }

  private String addSuffix(String filePath) {
    int index = filePath.lastIndexOf(".");
    String name = filePath.substring(0, index);
    String fileFormat = filePath.substring(index);
    return name + "_" + currentLanguage().toString() + fileFormat;
  }

  private String readFileContent(File file) throws IOException {
    try (InputStream fileInputStream = new FileInputStream(file); ) {
      return new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IOException("Failed to read File", exception);
    }
  }
}
