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

/** Class for a monster spawning room. */
public class MonsterRoom extends Room {

  private final int monsterCount;
  private final IPath[] monsterPaths;

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   * @param monsterCount the number of monsters in this room
   * @param monsterPaths the paths of the monsters in this room
   */
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

  /**
   * Number of monsters in this room.
   *
   * @return the number of monsters
   */
  public int getMonsterCount() {
    return monsterCount;
  }

  /**
   * Paths of the monsters in this room.
   *
   * @return the monster paths
   */
  public IPath[] getMonsterPaths() {
    return monsterPaths;
  }
}
