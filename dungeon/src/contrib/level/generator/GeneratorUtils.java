package contrib.level.generator;

import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.level.utils.TileTextureFactory;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for level generation containing methods to parse tile layouts, determine door
 * directions, find doors in a given direction and generate levels.
 */
public final class GeneratorUtils {

  /**
   * Get the LevelElement[][] for a Tile[][].
   *
   * @param tileLayout Tile layout to parse.
   * @return The parsed LevelElement layout.
   */
  public static LevelElement[][] parseToElementLayout(final Tile[][] tileLayout) {
    int ySize = tileLayout.length;
    int xSize = tileLayout[0].length;
    LevelElement[][] elementLayout = new LevelElement[ySize][xSize];
    for (int x = 0; x < xSize; x++) {
      for (int y = 0; y < ySize; y++) {
        Tile tile = tileLayout[y][x];
        elementLayout[y][x] = tile.levelElement();
      }
    }
    return elementLayout;
  }

  /**
   * Get the direction where a door is placed.
   *
   * @param level Level that contains the door.
   * @param door Door-tile where to find the direction for.
   * @return The direction of the door.
   */
  public static Direction doorDirection(final ILevel level, final DoorTile door) {
    LevelElement[][] layout = parseToElementLayout(level.layout());
    if (TileTextureFactory.isTopWall(door.coordinate(), layout)) return Direction.NORTH;
    if (TileTextureFactory.isRightWall(door.coordinate(), layout)) return Direction.EAST;
    if (TileTextureFactory.isBottomWall(door.coordinate(), layout)) return Direction.SOUTH;
    return Direction.WEST;
  }

  /**
   * Get the DoorTile at the given direction if it exists.
   *
   * <p>Returns an empty Optional if no level is generated for this room, or if no door is at that
   * direction.
   *
   * @param level WTF? .
   * @param direction Direction in which to find the door.
   * @return DoorTile in the room at the given direction.
   */
  public static Optional<DoorTile> doorAt(final ILevel level, final Direction direction) {
    for (DoorTile door : level.doorTiles())
      if (doorDirection(level, door) == direction) return Optional.of(door);
    return Optional.empty();
  }

  /**
   * Creates a room based level with the given number of rooms, monsters and chests.
   *
   * <p>This method creates a room based level with the given number of rooms, monsters and chests.
   * It generates a random design label for the level and adds a crafting cauldron to the middle
   * room. It also adds the specified number of monsters and chests to each room.
   *
   * @param roomcount The amount of rooms in the level.
   * @param monstercount The amount of monsters in each room.
   * @param chestcount The amount of chests in each room.
   */
  public static void createRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
    // create entity sets
    Set<Set<Entity>> entities = new HashSet<>();
    for (int i = 0; i < roomcount; i++) {
      Set<Entity> set = new HashSet<>();
      entities.add(set);
      if (i == roomcount / 2) {
        try {
          set.add(EntityFactory.newCraftingCauldron());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      for (int j = 0; j < monstercount; j++) {
        try {
          set.add(EntityFactory.randomMonster());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      for (int k = 0; k < chestcount; k++) {
        try {
          set.add(EntityFactory.newChest());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    ILevel level = RoomBasedLevelGenerator.level(entities, DesignLabel.randomDesign());
    Game.currentLevel(level);
  }

  /**
   * Creates a random dungeon with the given number of monsters and chests.
   *
   * <p>This method creates a random dungeon with the given number of monsters and chests. It
   * generates a random level size and adds one crafting cauldron to the level.
   *
   * @param monstercount The amount of monsters in the level.
   * @param chestcount The amount of chests in the level.
   */
  public static void createRandomDungeon(int monstercount, int chestcount) {
    try {
      for (int i = 0; i < monstercount; i++) Game.add(EntityFactory.randomMonster());
      for (int i = 0; i < chestcount; i++) Game.add(EntityFactory.newChest());
      Game.add(EntityFactory.newCraftingCauldron());
    } catch (IOException e) {
      throw new RuntimeException();
    }
    Game.levelSize(LevelSize.randomSize());
  }
}
