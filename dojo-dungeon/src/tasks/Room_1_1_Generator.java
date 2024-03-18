package tasks;

import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.entities.MonsterFactory;
import contrib.item.concreteItem.ItemKey;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.Game;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

public class Room_1_1_Generator extends TaskRoomGenerator {
  private final int monsterCount;

  public Room_1_1_Generator(
      RoomGenerator gen, LevelNode room, LevelNode nextNeighbour, int monsterCount) {
    super(gen, room, nextNeighbour);
    this.monsterCount = monsterCount;
  }

  @Override
  public void generateRoom() throws IOException {
    // generate the room
    getRoom()
        .level(
            new TileLevel(
                getGen().layout(LevelSize.LARGE, getRoom().neighbours()), DesignLabel.FOREST));

    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    IPath[] monsterPaths = {
      new SimpleIPath("character/monster/imp"), new SimpleIPath("character/monster/goblin")
    };

    for (int i = 0; i < monsterCount; i++) {
      roomEntities.add(
          MonsterFactory.randomMonster(monsterPaths[new Random().nextInt(monsterPaths.length)]));
    }
    // get random monster from roomEntities
    Entity randomMonster = (Entity) roomEntities.toArray()[(int) (Math.random() * monsterCount)];

    SimpleIPath KeyTexture = new SimpleIPath("items/key/gold_key.png");
    Animation sapphireAnimation = Animation.fromSingleImage(KeyTexture);
    ItemKey key =
        new ItemKey(
            "Sapphire",
            "A blue gemstone",
            sapphireAnimation,
            sapphireAnimation,
            getRoom(),
            getNextNeighbour());

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
    getRoom().entities(roomEntities);
    // this will add the entities (in the node payload) to the game, at the moment the level get
    // loaded for the first time
    getRoom().level().onFirstLoad(() -> getRoom().entities().forEach(Game::add));
  }
}
