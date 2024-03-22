package level;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.entities.MonsterFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import item.ItemKey;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import level.room.DojoRoom;

public class Key_Room_Generator extends TaskRoomGenerator {
  private final int monsterCount;
  private IPath keyTexture;
  private IPath[] monsterPaths;
  private String keyType;
  private String keyDescription;
  private DesignLabel designLabel;

  public Key_Room_Generator(
      RoomGenerator gen,
      DojoRoom room,
      DojoRoom nextNeighbour,
      int monsterCount,
      IPath keyTexture,
      IPath[] monsterPaths,
      String keyType,
      String keyDescription,
      DesignLabel designLabel) {
    super(gen, room, nextNeighbour);
    this.monsterCount = monsterCount;
    this.keyTexture = keyTexture;
    this.monsterPaths = monsterPaths;
    this.keyType = keyType;
    this.keyDescription = keyDescription;
    this.designLabel = designLabel;
  }

  @Override
  public void generateRoom() throws IOException {
    // generate the room
    getRoom()
        .level(
            new TileLevel(getGen().layout(LevelSize.LARGE, getRoom().neighbours()), designLabel));

    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    for (int i = 0; i < monsterCount; i++) {
      roomEntities.add(
          MonsterFactory.randomMonster(monsterPaths[new Random().nextInt(monsterPaths.length)]));
    }
    // get random monster from roomEntities
    Entity randomMonster = (Entity) roomEntities.toArray()[(int) (Math.random() * monsterCount)];

    Animation keyAnimation = Animation.fromSingleImage(keyTexture);
    ItemKey key =
        new ItemKey(
            keyType, keyDescription, keyAnimation, keyAnimation, getRoom(), getNextNeighbour());

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
