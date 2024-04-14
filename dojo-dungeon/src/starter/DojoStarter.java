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
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.builder.RoomBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/** Starter for the dojo-dungeon game. */
public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";
  private static final DesignLabel DESIGN_LABEL_FOR_LEVEL_1 = DesignLabel.FOREST;
  private static final DesignLabel DESIGN_LABEL_FOR_LEVEL_2 = DesignLabel.TEMPLE;
  private static final DesignLabel DESIGN_LABEL_FOR_LEVEL_3 = DesignLabel.DEFAULT;
  private static final DesignLabel DESIGN_LABEL_FOR_LEVEL_4 = DesignLabel.FIRE;

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

    // level 1 has 3 rooms
    LevelRoom levelRoom1 = new LevelRoom(graph);
    LevelRoom levelRoom2 = new LevelRoom(graph);
    LevelRoom levelRoom3 = new LevelRoom(graph);

    // level 2 has 3 rooms
    LevelRoom levelRoom4 = new LevelRoom(graph);
    LevelRoom levelRoom5 = new LevelRoom(graph);
    LevelRoom levelRoom6 = new LevelRoom(graph);

    // level 3 has 3 rooms
    LevelRoom levelRoom7 = new LevelRoom(graph);
    LevelRoom levelRoom8 = new LevelRoom(graph);
    LevelRoom levelRoom9 = new LevelRoom(graph);

    // level 4 has 3 rooms
    LevelRoom levelRoom10 = new LevelRoom(graph);
    LevelRoom levelRoom11 = new LevelRoom(graph);
    LevelRoom levelRoom12 = new LevelRoom(graph);

    // connect the rooms, this is needed to build the rooms in next steps
    connectBidirectional(levelRoom1, levelRoom2);
    connectBidirectional(levelRoom2, levelRoom3);
    connectBidirectional(levelRoom3, levelRoom4);
    connectBidirectional(levelRoom4, levelRoom5);
    connectBidirectional(levelRoom5, levelRoom6);
    connectBidirectional(levelRoom6, levelRoom7);
    connectBidirectional(levelRoom7, levelRoom8);
    connectBidirectional(levelRoom8, levelRoom9);
    connectBidirectional(levelRoom9, levelRoom10);
    connectBidirectional(levelRoom10, levelRoom11);
    connectBidirectional(levelRoom11, levelRoom12);

    // build the rooms
    RoomGenerator gen = new RoomGenerator();
    Room room12 = buildRoom_L4_R3_Monster_Implement_2(levelRoom12, gen, null);
    Room room11 = buildRoom_L4_R2_Fragen_Pattern(levelRoom11, gen, room12);
    Room room10 = buildRoom_L4_R1_Monster_3(levelRoom10, gen, room11);
    Room room9 = buildRoom_L3_R3_Fragen_RegExes(levelRoom9, gen, room10);
    Room room8 = buildRoom_L3_R2_Fehler_Quader(levelRoom8, gen, room9);
    Room room7 = buildRoom_L3_R1_Fragen_Schriftrollen(levelRoom7, gen, room8);
    Room room6 = buildRoom_L2_R3_Fehler_Refactoring(levelRoom6, gen, room7);
    Room room5 = buildRoom_L2_R2_Monster_Implement_1(levelRoom5, gen, room6);
    Room room4 = buildRoom_L2_R1_Monster_2(levelRoom4, gen, room5);
    Room room3 = buildRoom_L1_R3_Fragen_Lambda(levelRoom3, gen, room4);
    Room room2 = buildRoom_L1_R2_Fehler_Syntax(levelRoom2, gen, room3);
    Room room1 = buildRoom_L1_R1_Monster_1(levelRoom1, gen, room2);

    room1.configDoors();
    room2.configDoors();
    room3.configDoors();
    room4.configDoors();
    room5.configDoors();
    room6.configDoors();
    room7.configDoors();
    room8.configDoors();
    room9.configDoors();
    room10.configDoors();
    room11.configDoors();
    room12.configDoors();

    // Now after the doors are properly configured, we can close or open them:
    room1.closeDoors();
    room2.closeDoors();
    room3.closeDoors();
    room4.closeDoors();
    room5.closeDoors();
    room6.closeDoors();
    room7.closeDoors();
    room8.closeDoors();
    room9.closeDoors();
    room10.closeDoors();
    room11.closeDoors();

    // room6 should not be closed:
    room6.openDoors();

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

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(false);
    Game.windowTitle("Dojo-Dungeon");
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

  private static Room buildRoom_L1_R1_Monster_1(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_1)
        .buildRoom_L1_R1_Monster_1();
  }

  private static Room buildRoom_L1_R2_Fehler_Syntax(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_1)
        .buildRoom_L1_R2_Fehler_Syntax();
  }

  private static Room buildRoom_L1_R3_Fragen_Lambda(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_1)
        .buildRoom_L1_R3_Fragen_Lambda();
  }

  private static Room buildRoom_L2_R1_Monster_2(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_2)
        .buildRoom_L2_R1_Monster_2();
  }

  private static Room buildRoom_L2_R2_Monster_Implement_1(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_2)
        .buildRoom_L2_R2_Monster_Implement_1();
  }

  private static Room buildRoom_L2_R3_Fehler_Refactoring(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_2)
        .buildRoom_L2_R3_Fehler_Refactoring();
  }

  private static Room buildRoom_L3_R1_Fragen_Schriftrollen(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    ArrayList<String> list1 = new ArrayList<>();
    list1.add("Builder");
    list1.add("Factory");
    list1.add("Observer");
    list1.add("Singleton");

    ArrayList<String> list2 = new ArrayList<>();
    list2.add("DRY");
    list2.add("KISS");
    list2.add("YAGNI");

    HashMap<String, ArrayList<String>> sortables = new HashMap<>();
    sortables.put("Programming Patterns", list1);
    sortables.put("Software Development Principles", list2);

    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_3)
        .monsterCount(1)
        .monsterPaths(
            new IPath[] {
              new SimpleIPath("character/monster/pumpkin_dude"),
              new SimpleIPath("character/monster/zombie")
            })
        .sortables(sortables)
        .buildRoom_L3_R1_Fragen_Schriftrollen();
  }

  private static Room buildRoom_L3_R2_Fehler_Quader(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_3)
        .buildRoom_L3_R2_Fehler_Quader();
  }

  private static Room buildRoom_L3_R3_Fragen_RegExes(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_3)
        .buildRoom_L3_R3_Fragen_RegExes();
  }

  private static Room buildRoom_L4_R1_Monster_3(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_4)
        .buildRoom_L4_R1_Monster_3();
  }

  private static Room buildRoom_L4_R2_Fragen_Pattern(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_4)
        .buildRoom_L4_R2_Fragen_Pattern();
  }

  private static Room buildRoom_L4_R3_Monster_Implement_2(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(DESIGN_LABEL_FOR_LEVEL_4)
        .buildRoom_L4_R3_Monster_Implement_2();
  }
}
