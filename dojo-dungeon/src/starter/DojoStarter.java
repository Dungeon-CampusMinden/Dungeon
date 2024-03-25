package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.systems.*;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;
import level.rooms.LevelRoom;
import level.rooms.Room;
import level.rooms.RoomBuilder;

/** Starter for the dojo-dungeon game. */
public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  /**
   * Start a new dojo-dungeon game.
   *
   * @param args the command line arguments, currently unused
   */
  public static void main(String[] args) {
    try {
      Game.initBaseLogger(Level.WARNING);
      configGame();
      onSetup();
      Game.run();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      System.err.println("Exiting ...");
    }
  }

  private static void createLevel() throws IOException {
    // create a customised level comprising rooms
    LevelGraph graph = new LevelGraph();

    // level 1 has 3 rooms: room1, room2, room3
    LevelRoom levelRoom1 = new LevelRoom(graph);
    LevelRoom levelRoom2 = new LevelRoom(graph);
    LevelRoom levelRoom3 = new LevelRoom(graph);

    // level 2 has 3 rooms: room4, room5, room6
    LevelRoom levelRoom4 = new LevelRoom(graph);
    LevelRoom levelRoom5 = new LevelRoom(graph);
    LevelRoom levelRoom6 = new LevelRoom(graph);

    // connect the rooms, this is needed to build the rooms in next steps
    connectBidirectional(levelRoom1, levelRoom2);
    connectBidirectional(levelRoom2, levelRoom3);
    connectBidirectional(levelRoom3, levelRoom4);
    connectBidirectional(levelRoom4, levelRoom5);
    connectBidirectional(levelRoom5, levelRoom6);

    // build the rooms
    RoomGenerator gen = new RoomGenerator();
    Room room6 = buildRoom6(levelRoom6, gen, null);
    Room room5 = buildRoom5(levelRoom5, gen, room6);
    Room room4 = buildRoom4(levelRoom4, gen, room5);
    Room room3 = buildRoom3(levelRoom3, gen, room4);
    Room room2 = buildRoom2(levelRoom2, gen, room3);
    Room room1 = buildRoom1(levelRoom1, gen, room2);

    room1.configDoors();
    room2.configDoors();
    room3.configDoors();
    room4.configDoors();
    room5.configDoors();
    room6.configDoors();

    // Now after the doors are properly configured, we can close or open them:
    room1.closeDoors();
    room2.closeDoors();
    room3.closeDoors();
    room4.closeDoors();
    room5.closeDoors();
    room6.closeDoors();

    // set room1 as start level
    Game.currentLevel(levelRoom1.level());
  }

  private static void connectBidirectional(LevelRoom levelRoom, LevelRoom nextRoom) {
    // connect the rooms
    levelRoom.connect(nextRoom, Direction.SOUTH);
    nextRoom.connect(levelRoom, Direction.NORTH);
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

  private static Room buildRoom1(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    final int monsterCount = 5;
    final IPath[] monsterPaths = {
      new SimpleIPath("character/monster/imp"), new SimpleIPath("character/monster/goblin")
    };

    final String keyType = "Golden Key";
    final String keyDescription = "A key to unlock the next room.";
    final IPath keyTexture = new SimpleIPath("items/key/gold_key.png");

    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(DesignLabel.FOREST)
        .monsterCount(monsterCount)
        .monsterPaths(monsterPaths)
        .keyType(keyType)
        .keyDescription(keyDescription)
        .keyTexture(keyTexture)
        .buildKeyRoom();
  }

  private static Room buildRoom2(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DesignLabel.FOREST)
        .buildRoom2();
  }

  private static Room buildRoom3(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(DesignLabel.FOREST)
        .buildRoom3();
  }

  private static Room buildRoom4(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    final int monsterCount = 5;
    final IPath[] monsterPaths = {
      new SimpleIPath("character/monster/orc_shaman"),
      new SimpleIPath("character/monster/orc_warrior")
    };

    final String keyType = "A blue gemstone";
    final String keyDescription = "This gem opens the door to the next room.";
    final IPath keyTexture = new SimpleIPath("items/resource/saphire.png");

    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(DesignLabel.TEMPLE)
        .monsterCount(monsterCount)
        .monsterPaths(monsterPaths)
        .keyType(keyType)
        .keyDescription(keyDescription)
        .keyTexture(keyTexture)
        .buildKeyRoom();
  }

  private static Room buildRoom5(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    // TODO: build level 2, room 2 (room 5)
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(DesignLabel.TEMPLE)
        .buildRoom5();
  }

  private static Room buildRoom6(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    // TODO: build level 2, room 3 (room 6)
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(DesignLabel.TEMPLE)
        .buildRoom();
  }
}
