package dojo.rooms;

import contrib.entities.MonsterFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MonsterRoom extends Room {

  private final int monsterCount;
  private final IPath[] monsterPaths;

  public MonsterRoom(
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

  protected Set<Entity> populateMonsters(int monsterCount, IPath[] monsterPaths)
      throws IOException {
    Set<Entity> roomEntities = new HashSet<>();

    for (int i = 0; i < getMonsterCount(); i++) {
      roomEntities.add(
          MonsterFactory.randomMonster(
              getMonsterPaths()[new Random().nextInt(getMonsterPaths().length)]));
    }

    return roomEntities;
  }

  public int getMonsterCount() {
    return monsterCount;
  }

  public IPath[] getMonsterPaths() {
    return monsterPaths;
  }
}
