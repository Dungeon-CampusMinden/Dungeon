package engine.network.messages.s2c;

import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.level.loader.DungeonLoader;
import engine.level.loader.DungeonSaver;
import engine.level.loader.LevelParser;
import engine.network.messages.NetworkMessage;

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
   * <p>The level data contains the prefab instances that are active in the current game session,
   * including runtime changes made through {@link feature.prefabs.PrefabRuntime}, so clients
   * joining late see the same prefabs as everyone else.
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
      return LevelParser.serializeActiveLevel(dungeonLevel);
    }

    throw new IllegalStateException("Current level is not a DungeonLevel.");
  }
}
