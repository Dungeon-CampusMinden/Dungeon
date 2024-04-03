package level.rooms;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public class Room1 extends SearchRoom {
  private static final int monsterCount = 5;
  private static final IPath[] monsterPaths = {
    new SimpleIPath("character/monster/imp"), new SimpleIPath("character/monster/goblin")
  };

  private static final String keyType = "Golden Key";
  private static final String keyDescription = "A key to unlock the next room.";
  private static final IPath keyTexture = new SimpleIPath("items/key/gold_key.png");

  Room1(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(
        levelRoom,
        gen,
        nextRoom,
        levelSize,
        designLabel,
        monsterCount,
        monsterPaths,
        keyType,
        keyDescription,
        keyTexture);
  }
}
