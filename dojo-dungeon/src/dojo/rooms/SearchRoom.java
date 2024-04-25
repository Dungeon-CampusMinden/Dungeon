package dojo.rooms;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import dojo.item.ItemKey;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;

/** Class to drop an item in the room to open the door. */
public class SearchRoom extends MonsterRoom {
  private final String keyType;
  private final String keyDescription;
  private final IPath keyTexture;

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
   * @param keyType the type of the key
   * @param keyDescription the description of the key
   * @param keyTexture the path of the texture of the key
   */
  public SearchRoom(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel,
      int monsterCount,
      IPath[] monsterPaths,
      String keyType,
      String keyDescription,
      IPath keyTexture) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel, monsterCount, monsterPaths);
    this.keyType = keyType;
    this.keyDescription = keyDescription;
    this.keyTexture = keyTexture;

    try {
      generate();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate key room: " + e.getMessage(), e);
    }
  }

  private void generate() throws IOException {
    // add entities to room
    Set<Entity> roomEntities = populateMonsters(getMonsterCount(), getMonsterPaths());
    // get random monster from roomEntities
    Entity randomMonster =
        (Entity) roomEntities.toArray()[(int) (Math.random() * getMonsterCount())];

    Animation keyAnimation = Animation.fromSingleImage(keyTexture);
    ItemKey key =
        new ItemKey(keyType, keyDescription, keyAnimation, keyAnimation, this, getNextRoom());

    if (randomMonster.fetch(InventoryComponent.class).isPresent()) {
      randomMonster.remove(InventoryComponent.class);
    }
    randomMonster.add(new InventoryComponent());
    randomMonster.fetch(InventoryComponent.class).orElseThrow().add(key);

    // monster drops a sapphire on death
    BiConsumer<Entity, Entity> onDeath =
        (e, who) -> {
          new DropItemsInteraction().accept(e, who);
        };

    // TODO apply Monster Health dynamically, crashed trying to access HealthComponent
    randomMonster.remove(HealthComponent.class);
    randomMonster.add(new HealthComponent(1, (e) -> onDeath.accept(e, null)));

    // add a chest
    roomEntities.add(EntityFactory.newChest());

    // add the entities as payload to the LevelNode
    addRoomEntities(roomEntities);
  }
}
