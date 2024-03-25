package level.rooms;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;

public class MonsterRoom extends Room {

  private final int monsterCount;
  private final IPath[] monsterPaths;

  MonsterRoom(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel,
      int monsterCount,
      IPath[] monsterPaths) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);
    this.monsterCount = monsterCount;
    this.monsterPaths = monsterPaths;
  }

  public int getMonsterCount() {
    return monsterCount;
  }

  public IPath[] getMonsterPaths() {
    return monsterPaths;
  }
}
