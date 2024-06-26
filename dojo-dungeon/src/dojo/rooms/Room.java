package dojo.rooms;

import contrib.components.HealthComponent;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.utils.IVoidFunction;
import java.util.*;

/**
 * Our basic room type.
 *
 * <p>This class ist the base class for all room types. It holds one LevelNode and its entities. You
 * can:
 *
 * <ul>
 *   <li>add entities to the holding LevelNode
 *   <li>also add entities immediately to the LevelNode at runtime
 *   <li>generate and configure the room
 *   <li>configure the doors
 *   <li>close or open the doors when this room and the next room are fully connected and generated
 * </ul>
 *
 * <p>You cannot connect two rooms together with this class. This must be done with {@link
 * LevelRoom}, before genrating the room.
 */
public class Room {
  private final LevelRoom levelRoom;
  private final RoomGenerator gen;
  private final Room nextRoom;
  private final LevelSize levelSize;
  private final DesignLabel designLabel;

  /**
   * Generate a new base room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public Room(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    this.levelRoom = levelRoom;
    this.gen = gen;
    this.nextRoom = nextRoom;
    this.levelSize = levelSize;
    this.designLabel = designLabel;
    generate();
  }

  private void generate() {
    // generate the room
    // the levelRooms must be connected first!
    levelRoom.level(new TileLevel(gen.layout(levelSize, levelRoom.neighbours()), designLabel));

    // initialize room entities
    addRoomEntities(Collections.emptySet());
  }

  /** Configuration step to connect the doors of this room. */
  public void configDoors() {
    // the levelRooms must be generated first!

    ILevel level = levelRoom.level();
    // remove trapdoor exit, in rooms we only use doors
    List<Tile> exits = new ArrayList<>(level.exitTiles());
    exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));
    RoomBasedLevelGenerator.configureDoors(levelRoom);
  }

  /**
   * Add the entities as payload to the LevelNode.
   *
   * <p>This will add the entities (in the node payload) to the game, at the moment the level get
   * loaded for the first time.
   *
   * @param roomEntities the entities to add.
   */
  public void addRoomEntities(Set<Entity> roomEntities) {
    levelRoom.addRoomEntities(roomEntities);
  }

  /**
   * Adds an entity to the set of entities.
   *
   * @param entity the entity to add
   */
  public void addEntityImmediately(final Entity entity) {
    levelRoom.addEntityImmediately(entity);
  }

  /**
   * Gets the neighbour room.
   *
   * @return the next room
   */
  public Room getNextRoom() {
    return nextRoom;
  }

  /** Method to open doors between this room and next room. */
  public void openDoors() {
    if (nextRoom == null) {
      return;
    }
    GeneratorUtils.doorAt(levelRoom.level(), Direction.SOUTH).orElseThrow().open();
    GeneratorUtils.doorAt(nextRoom.levelRoom.level(), Direction.NORTH).orElseThrow().open();
  }

  /** Method to close doors between this room and next room. */
  public void closeDoors() {
    if (nextRoom == null) {
      return;
    }
    GeneratorUtils.doorAt(levelRoom.level(), Direction.SOUTH).orElseThrow().close();
    GeneratorUtils.doorAt(nextRoom.levelRoom.level(), Direction.NORTH).orElseThrow().close();
  }

  /**
   * Delegates to {@link LevelNode} start tile method.
   *
   * @return the tile for entities to spawn
   */
  public Tile getStartTile() {
    return levelRoom.level().startTile();
  }

  /**
   * Decreases the heros health at a wrong try ({@code - ceil(maxPoints / maxTries)}).
   *
   * <p>Informs the player with an OK dialog first.
   *
   * @param maxTries maximal number of wrong tries (the player then dies because he does not have
   *     sufficient health points, and game over)
   * @param onDialogDone the on dialog done callback (or the next dialog) (can be empty but not
   *     null)
   */
  protected void decreaseHerosHealthAtWrongTry(int maxTries, IVoidFunction onDialogDone) {
    OkDialog.showOkDialog(
        "Das war falsch. Deine Gesundheit wird um "
            + (int) Math.ceil(100.0 / maxTries)
            + " % verringert.",
        "Gesundheit",
        onDialogDone);
    HealthComponent hc = Game.hero().orElseThrow().fetch(HealthComponent.class).orElseThrow();
    int toReduce = (int) Math.ceil((double) hc.maximalHealthpoints() / maxTries);
    hc.currentHealthpoints(hc.currentHealthpoints() - toReduce);
  }
}
