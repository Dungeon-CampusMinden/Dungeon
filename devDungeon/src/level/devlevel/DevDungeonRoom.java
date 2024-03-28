package level.devlevel;

import components.TorchComponent;
import contrib.components.AIComponent;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import java.util.List;
import utils.EntityUtils;

/**
 * The DevDungeonRoom class represents a room inside a DevDungeon. It contains information about the
 * room's boundaries, torch and mob spawn points, and the entities (torches and mobs) within the
 * room.
 *
 * @see IllusionRiddleLevel
 */
public class DevDungeonRoom {
  // The top left and bottom right coordinates define the boundaries of the room.
  private final Coordinate topLeft;
  private final Coordinate bottomRight;
  // The spawn points for torches and mobs within the room.
  private final Coordinate[] torchSpawns;
  private final Coordinate[] mobSpawns;
  // The torch and mob entities within the room.
  private Entity[] torches;
  private Entity[] mobs;

  /**
   * Constructs a new DevDungeonRoom with the given boundaries and spawn points. Checks if the spawn
   * points are within the room boundaries. The room can contain torches and mobs.
   *
   * @param topLeft The top left coordinate of the room.
   * @param bottomRight The bottom right coordinate of the room.
   * @param torchSpawns The spawn points for torches within the room. (Can be empty)
   * @param mobSpawns The spawn points for mobs within the room. (Can be empty)
   * @throws IllegalArgumentException if a spawn point is outside the room boundaries.
   */
  public DevDungeonRoom(
      Coordinate topLeft,
      Coordinate bottomRight,
      Coordinate[] torchSpawns,
      Coordinate[] mobSpawns) {
    this.topLeft = topLeft;
    this.bottomRight = bottomRight;
    for (Coordinate torchSpawn : torchSpawns) {
      if (!this.contains(torchSpawn)) {
        throw new IllegalArgumentException("Torch spawn must be within room bounds");
      }
    }
    this.torchSpawns = torchSpawns;
    for (Coordinate mobSpawn : mobSpawns) {
      if (!this.contains(mobSpawn)) {
        throw new IllegalArgumentException("Mob spawn must be within room bounds");
      }
    }
    this.mobSpawns = mobSpawns;
  }

  /**
   * Constructs a new DevDungeonRoom with the given boundaries and torch spawn points. Checks if the
   * spawn points are within the room boundaries. The room will not contain any torches.
   *
   * @param topLeft The top left coordinate of the room.
   * @param bottomRight The bottom right coordinate of the room.
   * @param mobSpawns The spawn points for mobs within the room.
   */
  public DevDungeonRoom(Coordinate topLeft, Coordinate bottomRight, Coordinate[] mobSpawns) {
    this(topLeft, bottomRight, new Coordinate[0], mobSpawns);
  }

  /**
   * Checks if any torch in the room is active.
   *
   * @return true if any torch is active, false otherwise.
   */
  public boolean isAnyTorchActive() {
    for (Entity torch : this.torches) {
      if (torch.fetch(TorchComponent.class).map(TorchComponent::lit).orElse(false)) {
        return true;
      }
    }
    return false;
  }

  /** Spawns the torches and mobs in the room at their respective spawn points. */
  public void spawnEntities() {
    this.torches = this.spawnTorches(this.torchSpawns);
    this.mobs = this.spawnMobs(this.mobSpawns);
  }

  private Entity[] spawnTorches(Coordinate[] torchSpawns) {
    Entity[] torches = new Entity[torchSpawns.length];
    for (int i = 0; i < torchSpawns.length; i++) {
      torches[i] =
          EntityUtils.spawnAntiLightTorch(
              torchSpawns[i].toCenteredPoint(), true, true, (x, y) -> {});
    }
    return torches;
  }

  private Entity[] spawnMobs(Coordinate[] mobSpawns) {
    Entity[] mobs = new Entity[mobSpawns.length];
    for (int i = 0; i < mobSpawns.length; i++) {
      mobs[i] =
          EntityUtils.spawnMonster(
              IllusionRiddleLevel.MONSTER_TYPES[
                  (int) (Math.random() * IllusionRiddleLevel.MONSTER_TYPES.length)],
              mobSpawns[i]);
      if (mobs[i] != null)
        mobs[i]
            .fetch(AIComponent.class)
            .ifPresent(ai -> ai.active(false)); // Disable AI while not in same room
    }
    return mobs;
  }

  /**
   * Returns the top left coordinate of the room.
   *
   * @return the top left coordinate of the room.
   */
  public Coordinate topLeft() {
    return this.topLeft;
  }

  /**
   * Returns the bottom right coordinate of the room.
   *
   * @return the bottom right coordinate of the room.
   */
  public Coordinate bottomRight() {
    return this.bottomRight;
  }

  /**
   * Returns the array of torch entities in the room.
   *
   * @return the array of torch entities in the room.
   */
  public Entity[] torches() {
    return this.torches;
  }

  /**
   * Returns the array of mob entities in the room.
   *
   * @return the array of mob entities in the room.
   */
  public Entity[] mobs() {
    return this.mobs;
  }

  /**
   * Checks if a given coordinate is within the room's boundaries.
   *
   * @return true if the coordinate is within the room, false otherwise.
   */
  public boolean contains(Coordinate coordinate) {
    return coordinate.x >= this.topLeft.x
        && coordinate.x <= this.bottomRight.x
        && coordinate.y <= this.topLeft.y
        && coordinate.y >= this.bottomRight.y;
  }

  /**
   * Gets the tiles within the room's boundaries.
   *
   * @return a list of tiles within the room.
   */
  public List<Tile> tiles() {
    return Game.currentLevel().tilesInArea(this.topLeft, this.bottomRight);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || this.getClass() != other.getClass()) return false;
    DevDungeonRoom otherRoom = (DevDungeonRoom) other;
    return this.topLeft.equals(otherRoom.topLeft) && this.bottomRight.equals(otherRoom.bottomRight);
  }

  /**
   * Toggles the AI of the mobs in the room.
   *
   * @param active if true, activates the mobs' AI; if false, deactivates the mobs' AI.
   */
  public void mobAI(boolean active) {
    for (Entity mob : this.mobs) {
      mob.fetch(AIComponent.class).ifPresent(ai -> ai.active(active));
    }
  }
}
