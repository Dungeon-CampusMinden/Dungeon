package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.entities.MonsterFactory;
import contrib.item.concreteItem.ItemKey;
import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import contrib.systems.*;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.Game;
import core.System;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import tasks.Room_1_2_Generator;
import tasks.Room_1_3_Generator;

public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  public static void main(String[] args) throws IOException {
    Game.initBaseLogger();
    configGame();
    onSetup();
    Game.run();
  }

  private static void createLevel() {
    // make the graph
    LevelGraph graph = new LevelGraph();
    // NOTE: The graph normally has a Set of all nodes in the graph. Normally
    // you would add a Node via LevelGraph.add(node), but this is for random
    // graphs. tl;dr: The nodes list in the LevelGraph does not contain our
    // Nodes
    LevelNode room1 = new LevelNode(graph);
    LevelNode room2 = new LevelNode(graph);
    LevelNode room3 = new LevelNode(graph);

    // connect the rooms
    room1.connect(room2, Direction.SOUTH);
    room2.connect(room1, Direction.NORTH);

    room2.connect(room3, Direction.SOUTH);
    room3.connect(room2, Direction.NORTH);

    RoomGenerator gen = new RoomGenerator();

    try {
      createRoom_1(gen, room1, room2);
      createRoom_2(gen, room2, room3);
      createRoom_3(gen, room3, room2);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // remove trap doors, config doors
    configDoors(room1);
    configDoors(room2);
    configDoors(room3);

    // close the doors
    GeneratorUtils.doorAt(room1.level(), Direction.SOUTH).orElseThrow().close();
    GeneratorUtils.doorAt(room2.level(), Direction.NORTH).orElseThrow().close();

    GeneratorUtils.doorAt(room2.level(), Direction.SOUTH).orElseThrow().close();
    GeneratorUtils.doorAt(room3.level(), Direction.NORTH).orElseThrow().close();

    // set room1 as start level
    Game.currentLevel(room1.level());
  }

  private static void configDoors(LevelNode node) {
    ILevel level = node.level();
    // remove trapdoor exit, in rooms we only use doors
    List<Tile> exits = new ArrayList<>(level.exitTiles());
    exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));
    RoomBasedLevelGenerator.configureDoors(node);
  }

  public static void openDoors(LevelNode fromLevel, LevelNode toLevel) {
    if (fromLevel == null || toLevel == null) {
      return;
    }
    DoorTile door12 = GeneratorUtils.doorAt(fromLevel.level(), Direction.SOUTH).orElseThrow();
    door12.open();
    DoorTile door21 = GeneratorUtils.doorAt(toLevel.level(), Direction.NORTH).orElseThrow();
    door21.open();
  }

  private static void onSetup() {
    Game.userOnSetup(
        () -> {
          createSystems();
          try {
            Game.add(EntityFactory.newHero());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          setupMusic();
          Crafting.loadRecipes();

          createLevel();
        });
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(false);
    Game.windowTitle("Andres Freezer");
  }

  private static void setupMusic() {
    Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(.1f);
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
  }

  private static void pauseGame() {
    Game.systems().values().forEach(System::stop);
  }

  private static void unpauseGame() {
    Game.systems().values().forEach(System::run);
  }

  private static void createRoom_1(RoomGenerator gen, LevelNode room, LevelNode nextRoom)
      throws IOException {

    final int numMonsters = 1;
    // generate the room
    room.level(
        new TileLevel(gen.layout(LevelSize.LARGE, room.neighbours()), DesignLabel.randomDesign()));

    // add entities to room
    Set<Entity> roomEntities = new HashSet<>();

    for (int i = 0; i < numMonsters; i++) {
      roomEntities.add(MonsterFactory.randomMonster());
    }
    // get random monster from roomEntities
    Entity randomMonster = (Entity) roomEntities.toArray()[(int) (Math.random() * numMonsters)];

    SimpleIPath sapphireTexture = new SimpleIPath("items/resource/saphire.png");
    Animation sapphireAnimation = Animation.fromSingleImage(sapphireTexture);
    ItemKey sapphire =
        new ItemKey(
            "Sapphire", "A blue gemstone", sapphireAnimation, sapphireAnimation, room, nextRoom);

    if (randomMonster.fetch(InventoryComponent.class).isPresent()) {
      randomMonster.remove(InventoryComponent.class);
    }
    randomMonster.add(new InventoryComponent());
    randomMonster.fetch(InventoryComponent.class).orElseThrow().add(sapphire);

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
    room.entities(roomEntities);
    // this will add the entities (in the node payload) to the game, at the moment the level get
    // loaded for the first time
    room.level().onFirstLoad(() -> room.entities().forEach(Game::add));
  }

  private static void createRoom_2(RoomGenerator gen, LevelNode room, LevelNode nextRoom)
      throws IOException {
    new Room_1_2_Generator(gen, room, nextRoom).generateRoom();
  }

  private static void createRoom_3(RoomGenerator gen, LevelNode room, LevelNode prevRoom)
      throws IOException {
    new Room_1_3_Generator(gen, room, prevRoom).generateRoom();
  }
}
