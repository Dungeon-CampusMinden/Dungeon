package level.devlevel;

import components.TorchComponent;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import java.util.List;
import utils.EntityUtils;

public class IllusionRiddleRoom {
  private final Coordinate topLeft;
  private final Coordinate bottomRight;
  private final Coordinate[] torchSpawns;
  private final Coordinate[] mobSpawns;
  private Entity[] torches;
  private Entity[] mobs;

  public IllusionRiddleRoom(
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

  public boolean isAnyTorchActive() {
    for (Entity torch : this.torches) {
      if (torch.fetch(TorchComponent.class).map(TorchComponent::lit).orElse(false)) {
        return true;
      }
    }
    return false;
  }

  public void spawnEntities() {
    this.torches = this.spawnTorches(this.torchSpawns);
    this.mobs = this.spawnMobs(this.mobSpawns);
  }

  private Entity[] spawnTorches(Coordinate[] torchSpawns) {
    Entity[] torches = new Entity[torchSpawns.length];
    for (int i = 0; i < torchSpawns.length; i++) {
      torches[i] =
          EntityUtils.spawnTorch(torchSpawns[i].toCenteredPoint(), true, true, (x, y) -> {}, 0);
    }
    return torches;
  }

  private Entity[] spawnMobs(Coordinate[] mobSpawns) {
    Entity[] mobs = new Entity[mobSpawns.length];
    for (int i = 0; i < mobSpawns.length; i++) {
      mobs[i] =
          EntityUtils.spawnMonster(
              IllusionRiddleLevel.MONSTER_TYPES[i % IllusionRiddleLevel.MONSTER_TYPES.length],
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
    IllusionRiddleRoom that = (IllusionRiddleRoom) other;
    return this.topLeft.equals(that.topLeft) && this.bottomRight.equals(that.bottomRight);
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
