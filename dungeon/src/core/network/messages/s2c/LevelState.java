package core.network.messages.s2c;

import core.Game;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.network.messages.NetworkMessage;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the state of a level, including door states and design labels.
 *
 * @param doorStates An array representing the open/closed states of doors in the level. (can be
 *     null)
 * @param designLabelBytes A map of coordinates to design label bytes for tiles in the level. (can
 *     be null)
 */
public record LevelState(
    Map<Coordinate, Boolean> doorStates, Map<Coordinate, Byte> designLabelBytes)
    implements NetworkMessage {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Captures the current state of the level, including door states and design labels.
   *
   * @return The current LevelState of the game.
   */
  public static LevelState currentLevelStateFull() {
    return new LevelState(getDoorStates(), getDesignLabels());
  }

  /**
   * Creates a delta LevelState containing only changes since the previous state.
   *
   * @param previous the previous LevelState to compare against, or null for full state
   * @return a new LevelState containing only changed doors and design labels
   */
  public static LevelState createDelta(LevelState previous) {
    if (previous == null) {
      return currentLevelStateFull();
    }
    Map<Coordinate, Boolean> deltaDoors =
        generateDoorStateDelta(previous.doorStates() != null ? previous.doorStates() : Map.of());
    Map<Coordinate, Byte> deltaLabels =
        generateDesignLabelDelta(
            previous.designLabelBytes() != null ? previous.designLabelBytes() : Map.of());
    return new LevelState(deltaDoors, deltaLabels);
  }

  /**
   * Returns true if this LevelState has no data.
   *
   * @return true if both doorStates and designLabelBytes are empty or null
   */
  public boolean isEmpty() {
    boolean doorsEmpty = doorStates == null || doorStates.isEmpty();
    boolean labelsEmpty = designLabelBytes == null || designLabelBytes.isEmpty();
    return doorsEmpty && labelsEmpty;
  }

  private static Map<Coordinate, Boolean> generateDoorStateDelta(
      Map<Coordinate, Boolean> previousDoorStates) {
    Map<Coordinate, Boolean> currentDoorStates = getDoorStates();
    Map<Coordinate, Boolean> deltaDoorStates = new HashMap<>();
    for (var entry : currentDoorStates.entrySet()) {
      Coordinate coord = entry.getKey();
      Boolean currentState = entry.getValue();
      Boolean previousState = previousDoorStates.get(coord);
      if (previousState == null || !previousState.equals(currentState)) {
        deltaDoorStates.put(coord, currentState);
      }
    }
    return deltaDoorStates;
  }

  private static Map<Coordinate, Byte> generateDesignLabelDelta(
      Map<Coordinate, Byte> previousDesignLabels) {
    Map<Coordinate, Byte> currentDesignLabels = getDesignLabels();
    Map<Coordinate, Byte> deltaDesignLabels = new HashMap<>();
    for (var entry : currentDesignLabels.entrySet()) {
      Coordinate coord = entry.getKey();
      Byte currentLabel = entry.getValue();
      Byte previousLabel = previousDesignLabels.get(coord);
      if (previousLabel == null || !previousLabel.equals(currentLabel)) {
        deltaDesignLabels.put(coord, currentLabel);
      }
    }
    return deltaDesignLabels;
  }

  private static Map<Coordinate, Boolean> getDoorStates() {
    List<DoorTile> doorTiles = Game.currentLevel().orElseThrow().doorTiles();
    Map<Coordinate, Boolean> doorStates = new HashMap<>();
    for (DoorTile doorTile : doorTiles) {
      doorStates.put(doorTile.coordinate(), doorTile.isOpen());
    }
    return doorStates;
  }

  private static Map<Coordinate, Byte> getDesignLabels() {
    ILevel level = Game.currentLevel().orElseThrow();
    int width = level.layout().length;
    int height = level.layout()[0].length;
    Map<Coordinate, Byte> designLabels = new HashMap<>();
    Tile[][] levelLayout = level.layout();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Tile tile = levelLayout[x][y];
        designLabels.put(tile.coordinate(), tile.designLabel().toByte());
      }
    }
    return designLabels;
  }
}
