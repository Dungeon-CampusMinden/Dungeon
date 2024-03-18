package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.level.generator.GeneratorUtils;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import contrib.systems.*;
import core.Game;
import core.System;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tasks.Room_1_1_Generator;
import tasks.Room_1_2_Generator;
import tasks.Room_1_3_Generator;

public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int MONSTERCOUNT = 5;

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
    Game.currentLevel(room2.level());
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
    new Room_1_1_Generator(gen, room, nextRoom, MONSTERCOUNT).generateRoom();
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
