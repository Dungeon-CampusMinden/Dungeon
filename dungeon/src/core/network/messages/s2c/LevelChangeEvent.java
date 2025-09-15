package core.network.messages.s2c;

import core.level.loader.DungeonLoader;
import core.level.loader.DungeonSaver;
import core.network.messages.NetworkMessage;

/**
 * This event is sent by the server to all clients when the level changes. It contains the name of
 * the new level and its data.
 */
public record LevelChangeEvent(String levelName, String levelData) implements NetworkMessage {

  /**
   * Creates a LevelChangeEvent for the current level in the game.
   *
   * @return A LevelChangeEvent containing the current level's name and data.
   */
  public static LevelChangeEvent currentLevel() {
    return new LevelChangeEvent(DungeonLoader.currentLevel(), DungeonSaver.saveCurrentDungeon());
  }
}
