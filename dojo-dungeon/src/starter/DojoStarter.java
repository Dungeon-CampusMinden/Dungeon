package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.Direction;
import contrib.level.generator.graphBased.levelGraph.LevelGraph;
import contrib.systems.*;
import core.Game;
import core.level.elements.ILevel;
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
import java.util.List;
import java.util.logging.Level;

/** Starter for the dojo-dungeon game. */
public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  private interface BuildRoomMethod {
    Room buildRoom(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel);
  }

  private record LevelRooms(
      DesignLabel designLabel, LevelRoom[] levelRooms, BuildRoomMethod[] buildRoomMethods) {}

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
    LevelRooms[] dojoLevels = getLevelRooms();

    // connect the rooms, this is needed to build the rooms in next steps
    LevelRoom previousLevelRoom = null;
    for (LevelRooms levelRooms : dojoLevels) {
      for (LevelRoom levelRoom : levelRooms.levelRooms()) {
        if (previousLevelRoom != null) {
          connectBidirectional(previousLevelRoom, levelRoom);
        }
        previousLevelRoom = levelRoom;
      }
    }

    // build the rooms (reverse order)
    List<Room> rooms = new ArrayList<>();
    RoomGenerator gen = new RoomGenerator();
    Room nextRoom = null;
    for (int i = dojoLevels.length - 1; i >= 0; i--) {
      LevelRooms levelRooms = dojoLevels[i];
      for (int j = levelRooms.levelRooms().length - 1; j >= 0; j--) {
        LevelRoom levelRoom = levelRooms.levelRooms()[j];
        BuildRoomMethod buildRoomMethod = levelRooms.buildRoomMethods()[j];
        nextRoom = buildRoomMethod.buildRoom(levelRoom, gen, nextRoom, levelRooms.designLabel());
        rooms.add(nextRoom);
      }
    }
    rooms = rooms.reversed();

    // configure the doors
    for (Room room : rooms) {
      room.configDoors();
    }

    // Now after the doors are properly configured, we can close or open them:
    for (Room room : rooms) {
      room.closeDoors();
    }

    // Fehler_Refactoring should not be closed:
    rooms.get(8).openDoors();

    // add a room description popup dialog on room enter for each room
    final List<Room> finalRooms = rooms;
    Game.userOnLevelLoad(
        (wasAlreadyLoaded) -> {
          ILevel il = Game.currentLevel();
          for (Room room : finalRooms) {
            if (room.hasLevel(il)) {
              String roomTitle = room.getRoomTitle();
              String roomDescription = room.getRoomDescription();
              if (roomTitle != null && roomDescription != null) {
                OkDialog.showOkDialog(roomDescription, roomTitle, () -> {});
              }
            }
          }
        });

    // set level 1, room 1 as start level
    Game.currentLevel(dojoLevels[0].levelRooms()[0].level());
  }

  private static LevelRooms[] getLevelRooms() {
    LevelGraph graph = new LevelGraph();

    return new LevelRooms[] {
      new LevelRooms(
          DesignLabel.FOREST,
          new LevelRoom[] {new LevelRoom(graph), new LevelRoom(graph), new LevelRoom(graph)},
          new BuildRoomMethod[] {
            DojoStarter::buildRoom_Monster_1,
            DojoStarter::buildRoom_Fehler_Syntax,
            DojoStarter::buildRoom_Fragen_Lambda
          }),
      new LevelRooms(
          DesignLabel.FIRE,
          new LevelRoom[] {new LevelRoom(graph), new LevelRoom(graph), new LevelRoom(graph)},
          new BuildRoomMethod[] {
            DojoStarter::buildRoom_Monster_3,
            DojoStarter::buildRoom_Fragen_Pattern,
            DojoStarter::buildRoom_Monster_Implement_2
          }),
      new LevelRooms(
          DesignLabel.TEMPLE,
          new LevelRoom[] {new LevelRoom(graph), new LevelRoom(graph), new LevelRoom(graph)},
          new BuildRoomMethod[] {
            DojoStarter::buildRoom_Monster_2,
            DojoStarter::buildRoom_Monster_Implement_1,
            DojoStarter::buildRoom_Fehler_Refactoring
          }),
      new LevelRooms(
          DesignLabel.DEFAULT,
          new LevelRoom[] {new LevelRoom(graph), new LevelRoom(graph), new LevelRoom(graph)},
          new BuildRoomMethod[] {
            DojoStarter::buildRoom_Fragen_Schriftrollen,
            DojoStarter::buildRoom_Fehler_Quader,
            DojoStarter::buildRoom_Fragen_RegExes
          })
    };
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

  private static Room buildRoom_Monster_1(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(designLabel)
        .buildRoom_Monster_1();
  }

  private static Room buildRoom_Fehler_Syntax(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Fehler_Syntax();
  }

  private static Room buildRoom_Fragen_Lambda(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(designLabel)
        .buildRoom_Fragen_Lambda();
  }

  private static Room buildRoom_Monster_2(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(designLabel)
        .buildRoom_Monster_2();
  }

  private static Room buildRoom_Monster_Implement_1(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Monster_Implement_1();
  }

  private static Room buildRoom_Fehler_Refactoring(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(designLabel)
        .buildRoom_Fehler_Refactoring();
  }

  private static Room buildRoom_Fragen_Schriftrollen(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
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
        .designLabel(designLabel)
        .monsterCount(1)
        .monsterPaths(
            new IPath[] {
              new SimpleIPath("character/monster/pumpkin_dude"),
              new SimpleIPath("character/monster/zombie")
            })
        .sortables(sortables)
        .buildRoom_Fragen_Schriftrollen();
  }

  private static Room buildRoom_Fehler_Quader(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Fehler_Quader();
  }

  private static Room buildRoom_Fragen_RegExes(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.SMALL)
        .designLabel(designLabel)
        .buildRoom_Fragen_RegExes();
  }

  private static Room buildRoom_Monster_3(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Monster_3();
  }

  private static Room buildRoom_Fragen_Pattern(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Fragen_Pattern();
  }

  private static Room buildRoom_Monster_Implement_2(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Monster_Implement_2();
  }
}
