package dojo.rooms.level_2;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.SearchRoom;

public class Room4 extends SearchRoom {
  private static final int monsterCount = 5;
  private static final IPath[] monsterPaths = {
    new SimpleIPath("character/monster/orc_shaman"),
    new SimpleIPath("character/monster/orc_warrior")
  };

  private static final String keyType = "A blue gemstone";
  private static final String keyDescription = "This gem opens the door to the next room.";
  private static final IPath keyTexture = new SimpleIPath("items/resource/saphire.png");

  public Room4(
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
