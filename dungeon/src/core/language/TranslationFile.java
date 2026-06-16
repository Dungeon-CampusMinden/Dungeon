package core.language;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import core.utils.JsonHandler;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A single translation file for one language.
 *
 * <p>The file content is read from disk and parsed lazily on first access and cached afterwards, so
 * each file is only loaded once regardless of how many translations are requested from it.
 */
class TranslationFile {
  private final String path;
  private Map<String, Object> content;

  /**
   * Creates a handle for the translation file at the given path.
   *
   * <p>The file is not read until a translation is requested for the first time.
   *
   * @param path Path to the translation JSON file, e.g. "language/de.json".
   */
  TranslationFile(String path) {
    this.path = path;
  }

  /**
   * Gets the value behind the given JSON node path.
   *
   * <p>Triggers the lazy read and caching of the file on first access.
   *
   * @param nodes Nodes of the JSON up to the desired value.
   * @return the value, or {@code null} if this file does not contain the path.
   */
  String value(String[] nodes) {
    return JsonHandler.getValueByPath(content(), nodes);
  }

  /* Lazily reads and caches the parsed file content. */
  private Map<String, Object> content() {
    if (content == null) {
      FileHandle fileHandler = Gdx.files.internal(path);
      String jsonString = fileHandler.readString(StandardCharsets.UTF_8.name());
      content = JsonHandler.readJson(jsonString);
    }
    return content;
  }
}
