package dojo.rooms.level_1;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.SearchRoom;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum muss man den Schlüssel, der zufällig von einem Monster fallengelassen wird,
 * finden, um in den nächsten Raum zu kommen.
 */
public class L1_R1_Monster_1 extends SearchRoom {
  private static final int monsterCount = 5;
  private static final IPath[] monsterPaths = {
    new SimpleIPath("character/monster/imp"), new SimpleIPath("character/monster/goblin")
  };

  private static final String keyType = "Golden Key";
  private static final String keyDescription = "A key to unlock the next room.";
  private static final IPath keyTexture = new SimpleIPath("items/key/gold_key.png");

  public L1_R1_Monster_1(
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
