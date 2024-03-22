package level.room;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.utils.DesignLabel;
import core.utils.components.path.IPath;
import java.io.IOException;
import level.Key_Room_Generator;

/** A utility class for generating rooms with progression via {@link item.ItemKey} */
public class KeyRoomBuilder {
  private int monsterCount;
  private RoomGenerator roomGenerator;
  private DojoRoom room;
  private DojoRoom nextNeighbour;

  private IPath keyTexture;
  private IPath[] monsterPaths;
  private String keyType;
  private String keyDescription;
  private DesignLabel designLabel;

  public KeyRoomBuilder() {}

  /**
   * Set the essentials information for the Room that {@link level.TaskRoomGenerator} needs to
   * generate the room.
   *
   * @param gen the room generator
   * @param room the room to generate
   * @param nextNeighbour the next room to connect the doors
   * @param monsterCount the number of monsters to spawn
   * @return this builder for chaining
   */
  public KeyRoomBuilder setRoomEssentials(
      RoomGenerator gen, DojoRoom room, DojoRoom nextNeighbour, int monsterCount) {
    this.roomGenerator = gen;
    this.room = room;
    this.nextNeighbour = nextNeighbour;
    this.monsterCount = monsterCount;

    return this;
  }

  /**
   * Set the design label for the room
   *
   * @param designLabel the design label
   * @return this builder for chaining
   */
  public KeyRoomBuilder designLabel(DesignLabel designLabel) {
    this.designLabel = designLabel;
    return this;
  }

  /**
   * Set the information for the creation of the key item
   *
   * @param keyTexture the texture of the key
   * @param keyType the String describing the type of key
   * @param keyDescription the description of the key
   * @return this builder for chaining
   */
  public KeyRoomBuilder keyInfo(IPath keyTexture, String keyType, String keyDescription) {
    this.keyTexture = keyTexture;
    this.keyType = keyType;
    this.keyDescription = keyDescription;
    return this;
  }

  /**
   * Set the paths for the monster
   *
   * @param monsterPaths the paths for the monsters
   * @return this builder for chaining
   */
  public KeyRoomBuilder monsterPaths(IPath[] monsterPaths) {
    this.monsterPaths = monsterPaths;
    return this;
  }

  /**
   * Build the room
   *
   * @throws IllegalStateException if any of the information is not set
   * @throws IOException if the room generation fails
   */
  public void build() throws IllegalStateException, IOException {

    checkState();

    new Key_Room_Generator(
            roomGenerator,
            room,
            nextNeighbour,
            monsterCount,
            keyTexture,
            monsterPaths,
            keyType,
            keyDescription,
            designLabel)
        .generateRoom();
  }

  private void checkState() throws IllegalStateException {
    if (roomGenerator == null || room == null || nextNeighbour == null) {
      throw new IllegalStateException("Room essentials not set");
    }

    if (designLabel == null) {
      throw new IllegalStateException("Design label not set");
    }

    if (keyTexture == null || keyType == null || keyDescription == null) {
      throw new IllegalStateException("Key info not set");
    }

    if (monsterPaths == null) {
      throw new IllegalStateException("Monster paths not set");
    }
  }
}
