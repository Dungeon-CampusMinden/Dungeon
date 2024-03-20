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
import contrib.systems.*;
import core.Game;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tasks.level_1.Room_1_1_Generator;
import tasks.level_1.Room_1_2_Generator;
import tasks.level_1.Room_1_3_Generator;

/** Starter for the dojo-dungeon game. */
public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final int MONSTERCOUNT = 5;

  /**
   * Start a new dojo-dungeon game.
   *
   * @param args the command line arguments, currently unused
   */
  public static void main(String[] args) {
    try {
      Game.initBaseLogger();
      configGame();
      onSetup();
      Game.run();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println("Exiting ...");
    }
  }

  private static void createLevel() throws IOException {
    // create a customised level comprising three nodes (rooms)
    LevelGraph graph = new LevelGraph();
    DojoRoom room1 = new DojoRoom(graph);
    DojoRoom room2 = new DojoRoom(graph);
    DojoRoom room3 = new DojoRoom(graph);

    // connect the rooms
    room1.connect(room2, Direction.SOUTH);
    room2.connect(room1, Direction.NORTH);
    room2.connect(room3, Direction.SOUTH);
    room3.connect(room2, Direction.NORTH);

    // link the rooms
    room1.setNextRoom(room2);
    room2.setNextRoom(room3);

    // create the rooms in custom level
    RoomGenerator gen = new RoomGenerator();
    createRoom_1(gen, room1, room2);
    createRoom_2(gen, room2, room3);
    createRoom_3(gen, room3, null);

    // remove trap doors, config doors
    configDoors(room1);
    configDoors(room2);
    configDoors(room3);

    // close the doors
    room1.closeDoors();
    room2.closeDoors();

    // set room1 as start level
    Game.currentLevel(room1.level());
  }

  private static void configDoors(DojoRoom node) {
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

          try {
            createLevel();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /** Pause the game. Perhaps for use in dialogs later. */
  public static void pauseGame() {
    Game.systems().values().forEach(core.System::stop);
  }

  /** Continue the game. Perhaps for use in dialogs later. */
  public static void unpauseGame() {
    Game.systems().values().forEach(core.System::run);
  }

  public static void openDoors(DojoRoom fromLevel, DojoRoom toLevel) {
    if (fromLevel == null || toLevel == null) {
      return;
    }
    DoorTile door12 = GeneratorUtils.doorAt(fromLevel.level(), Direction.SOUTH).orElseThrow();
    door12.open();
    DoorTile door21 = GeneratorUtils.doorAt(toLevel.level(), Direction.NORTH).orElseThrow();
    door21.open();
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(false);
    Game.windowTitle("Dojo Dungeon");
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

  private static void createRoom_1(RoomGenerator gen, DojoRoom room, DojoRoom nextRoom)
      throws IOException {
    new Room_1_1_Generator(gen, room, nextRoom, MONSTERCOUNT).generateRoom();
  }

  private static void createRoom_2(RoomGenerator gen, DojoRoom room, DojoRoom nextRoom)
      throws IOException {
    new Room_1_2_Generator(gen, room, nextRoom).generateRoom();
  }

  private static void createRoom_3(RoomGenerator gen, DojoRoom room, DojoRoom nextRoom)
      throws IOException {
    new Room_1_3_Generator(gen, room, nextRoom).generateRoom();
  }
}
