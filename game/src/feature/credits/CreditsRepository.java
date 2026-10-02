package feature.credits;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import engine.utils.logging.DungeonLogger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Loads optional room credits definitions from internal game assets. */
public final class CreditsRepository {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CreditsRepository.class);

  private CreditsRepository() {}

  /**
   * Loads one room's credits file when it exists and is valid.
   *
   * @param roomId room identifier used in {@code credits/<roomId>.json}
   * @return the parsed definition, or empty if the asset is absent or invalid
   */
  public static Optional<CreditsDefinition> load(String roomId) {
    String path = CreditsDefinition.resourcePath(roomId);
    if (Gdx.files == null) return Optional.empty();

    FileHandle file = Gdx.files.internal(path);
    if (!file.exists()) return Optional.empty();

    try {
      CreditsDefinition definition =
          CreditsDefinition.parse(file.readString(StandardCharsets.UTF_8.name()));
      if (!roomId.equals(definition.roomId())) {
        throw new IllegalArgumentException(
            "roomId '" + definition.roomId() + "' does not match file name '" + roomId + "'");
      }
      return Optional.of(definition);
    } catch (RuntimeException exception) {
      LOGGER.warn("Ignoring invalid credits file '{}': {}", path, exception.getMessage());
      return Optional.empty();
    }
  }
}
