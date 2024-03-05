package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.entities.MonsterFactory;
import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndresWelt {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  public static void main(String[] args) throws IOException {
    Game.initBaseLogger();
    configGame();
    onSetup();
    Game.run();
  }

  private static void createLevel() {
    // make the graph
    LevelGraph graph =
        new LevelGraph(); // NOTE: The graph normally has a Set of all nodes in the graph. Normally
    // you would add a Node via LevelGraph.add(node), but this is for random
    // graphs. tldr: The nodes list in the LevelGraph does not contain our
    // Nodes
    LevelNode room1 = new LevelNode(graph);
    LevelNode room2 = new LevelNode(graph);
    LevelNode room3 = new LevelNode(graph);

    // connect room 1 and room 2 on the east side
    room1.connect(room2, Direction.EAST);
    room2.connect(room1, Direction.WEST);

    room2.connect(room3, Direction.SOUTH);
    room3.connect(room2, Direction.NORTH);

    // generate the rooms
    RoomGenerator roomG = new RoomGenerator();
    room1.level(
        new TileLevel(
            roomG.layout(LevelSize.SMALL, room1.neighbours()), DesignLabel.randomDesign()));
    room2.level(
        new TileLevel(
            roomG.layout(LevelSize.SMALL, room2.neighbours()), DesignLabel.randomDesign()));
    room3.level(
        new TileLevel(
            roomG.layout(LevelSize.SMALL, room3.neighbours()), DesignLabel.randomDesign()));

    // remove trap doors, config doors
    configDoors(room1);
    configDoors(room2);
    configDoors(room3);

    // close the doors
    DoorTile room1ToRoom2 = GeneratorUtils.doorAt(room1.level(), Direction.EAST).orElseThrow();
    DoorTile room2ToRoom3 = GeneratorUtils.doorAt(room2.level(), Direction.SOUTH).orElseThrow();
    room1ToRoom2.close();
    room2ToRoom3.close();

    // add entities to rooms
    Set<Entity> room1Entities = new HashSet<>();
    Set<Entity> room2Entities = new HashSet<>();
    Set<Entity> room3Entities = new HashSet<>();
    try {
      // Door 1 will open after killing the monster
      Entity monster = MonsterFactory.randomMonster();
      monster.fetch(HealthComponent.class).orElseThrow().onDeath(entity -> room1ToRoom2.open());
      room1Entities.add(monster);

      // Door 2 will open/close after talking to the knight
      Entity talkToMe = new Entity();
      talkToMe.add(new PositionComponent());
      talkToMe.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
      talkToMe.add(
          new InteractionComponent(
              1,
              true,
              (entity, entity2) -> {
                if (room2ToRoom3.isOpen()) room2ToRoom3.close();
                else room2ToRoom3.open();
              }));
      room2Entities.add(MonsterFactory.randomMonster());
      room2Entities.add(talkToMe);

      // EPIC LOOT
      room3Entities.add(EntityFactory.newChest());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // add the entities as payload to the LevelNode
    room1.entities(room1Entities);
    room2.entities(room2Entities);
    room3.entities(room3Entities);
    // this will add the entities (in the node payload) to the game, at the moment the level get
    // loaded for the first time
    room1.level().onFirstLoad(() -> room1.entities().forEach(Game::add));
    room2.level().onFirstLoad(() -> room2.entities().forEach(Game::add));
    room3.level().onFirstLoad(() -> room3.entities().forEach(Game::add));

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
}
