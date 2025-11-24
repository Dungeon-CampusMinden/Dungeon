package core.network.messages.s2c;

import core.Game;
import core.level.DungeonLevel;
import core.level.elements.ILevel;
import core.level.loader.DungeonLoader;
import core.level.loader.DungeonSaver;
import core.level.loader.LevelParser;
import core.network.messages.NetworkMessage;

/**
 * This event is sent by the server to all clients when the level changes.
 *
 * <p>It contains the name of the new level and the serialized data of the level.
 *
 * @param levelName The name of the new level.
 * @param levelData The serialized data of the new level. (See {@link DungeonSaver} for format
 *     details.)
 */
public record LevelChangeEvent(String levelName, String levelData) implements NetworkMessage {

  /**
   * Creates a LevelChangeEvent for the current level in the game.
   *
   * @return A LevelChangeEvent containing the current level's name and data.
   * @throws IllegalStateException if there is no current level or if the current level is not a
   *     DungeonLevel.
   */
  public static LevelChangeEvent currentLevel() {
    return new LevelChangeEvent(DungeonLoader.currentLevel(), getCurrentLevelData());
  }

  private static String getCurrentLevelData() {
    ILevel currentLevel =
        Game.currentLevel()
            .orElseThrow(() -> new IllegalStateException("No current level to serialize."));

    if (currentLevel instanceof DungeonLevel dungeonLevel) {
      return LevelParser.serializeLevel(dungeonLevel);
    }

    throw new IllegalStateException("Current level is not a DungeonLevel.");
  }
}
