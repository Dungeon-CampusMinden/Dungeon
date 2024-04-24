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
import core.utils.IVoidFunction;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.builder.RoomBuilder;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/** Starter for the dojo-dungeon game. */
public class DojoStarter {
  private static final String BACKGROUND_MUSIC = "sounds/background.wav";

  private interface BuildRoomMethod {
    Room buildRoom(LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel);
  }

  private record BuildRoomData(BuildRoomMethod buildRoomMethod, IVoidFunction roomEnterCallback) {}

  private record BuildRoom(
      LevelRoomLevel levelRoomLevel, BuildRoomData buildRoomData, LevelRoom levelRoom) {}

  private static class LevelRoomLevel {
    private final DesignLabel designLabel;
    private final List<BuildRoom> buildRooms;

    private LevelRoomLevel(
        LevelGraph graph, DesignLabel designLabel, BuildRoomData[] buildRoomData) {
      this.designLabel = designLabel;
      buildRooms =
          Arrays.stream(buildRoomData)
              .map(brm -> new BuildRoom(this, brm, new LevelRoom(graph)))
              .toList();
    }
  }

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
    // Create a customised level comprising rooms
    LevelRoomLevel[] allLevels = getLevelRoomLevels();

    // Connect the rooms, this is needed to build the rooms in next steps
    LevelRoom previousLevelRoom = null;
    for (LevelRoomLevel level : allLevels) {
      for (BuildRoom buildRoom : level.buildRooms) {
        LevelRoom levelRoom = buildRoom.levelRoom;
        if (previousLevelRoom != null) {
          connectBidirectional(previousLevelRoom, levelRoom);
        }
        previousLevelRoom = levelRoom;
      }
    }

    // Build the rooms (reverse order)
    List<LevelRoomLevel> tempReversedLevels = Arrays.asList(allLevels).reversed();
    RoomGenerator gen = new RoomGenerator();
    Room nextRoom = null;
    Deque<Room> rooms = new ArrayDeque<>();
    for (LevelRoomLevel level : tempReversedLevels) {
      List<BuildRoom> tempReversedBuildRooms = level.buildRooms.reversed();
      for (BuildRoom buildRoom : tempReversedBuildRooms) {
        LevelRoom levelRoom = buildRoom.levelRoom;
        BuildRoomMethod buildRoomMethod = buildRoom.buildRoomData.buildRoomMethod;
        nextRoom = buildRoomMethod.buildRoom(levelRoom, gen, nextRoom, level.designLabel);
        rooms.addFirst(nextRoom);
      }
    }

    // Configure the doors
    for (Room room : rooms) {
      room.configDoors();
    }

    // Now after the doors are properly configured, we can close or open them:
    for (Room room : rooms) {
      room.closeDoors();
    }

    // Room "Fehler_Refactoring" should not be closed:
    rooms.stream()
        .filter(r -> r.getClass().getSimpleName().contains("Fehler_Refactoring"))
        .findFirst()
        .orElseThrow()
        .openDoors();

    // Add a room description popup dialog on room enter for each room
    Game.userOnLevelLoad(
        (wasAlreadyLoaded) -> {
          ILevel il = Game.currentLevel();
          for (LevelRoomLevel level : allLevels) {
            for (BuildRoom buildRoom : level.buildRooms) {
              if (buildRoom.levelRoom.level() == il) {
                // Execute the roomEnterCallback
                buildRoom.buildRoomData.roomEnterCallback.execute();
              }
            }
          }
        });

    // Set level 1, room 1 as start level (or start room)
    Game.currentLevel(allLevels[0].buildRooms.getFirst().levelRoom.level());
  }

  private static LevelRoomLevel[] getLevelRoomLevels() {
    final LevelGraph graph = new LevelGraph();
    /*
     * One level consists of three rooms:
     * First room: one room each from "search".
     * Second room: one room each from "riddle".
     * Third room: one room each from "boss".
     */
    return new LevelRoomLevel[] {
      new LevelRoomLevel(
          graph,
          DesignLabel.FOREST,
          new BuildRoomData[] {
            new BuildRoomData(
                DojoStarter::buildRoom_Key,
                () ->
                    OkDialog.showOkDialog(
                        "Ein geheimnisvoller Wald mit dichtem Unterholz und moosbedecktem Boden. - Der Wald besteht aus drei Räumen.",
                        "Umgebung (Level 1):",
                        () ->
                            OkDialog.showOkDialog(
                                "In diesem Raum muss man den Schlüssel, der zufällig von einem Monster fallengelassen wird, finden, um in den nächsten Raum zu kommen. - Besiege die Monster und finde den Schlüssel für den nächsten Raum.",
                                "\"Der vergessene Wald\" (Raum 1)",
                                () -> {}))),
            new BuildRoomData(
                DojoStarter::buildRoom_Fehler_Syntax,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum soll eine fehlerhafte Java-Klasse korrigiert werden, während man spielt. Erst, wenn man alle Fehler gefunden und alle Überprüfungen geschafft hat, kann man in den nächsten Raum weitergehen. Es gibt dabei drei \"Prüfstufen\". - Gehe zum Ritter für die Raumaufgabe.",
                        "\"Der vergessene Wald\" (Raum 2)",
                        () -> {})),
            new BuildRoomData(
                DojoStarter::buildRoom_Fragen_Lambda,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum muss der Spieler alle Fragen eines NPCs zu Lambda-Ausdrücken und Funktionsinterfaces richtig beantworten, um in den nächsten Raum zu gelangen. - Gehe zum pumpkin dude und beantworte alle Fragen.",
                        "\"Der vergessene Wald\" (Raum 3)",
                        () -> {})),
          }),
      new LevelRoomLevel(
          graph,
          DesignLabel.FIRE,
          new BuildRoomData[] {
            new BuildRoomData(
                DojoStarter::buildRoom_Monster_Kill,
                () ->
                    OkDialog.showOkDialog(
                        "Eine unterirdische Höhle mit fließender Lava und glühenden Steinen. - Die Vulkanhöhle besteht aus drei Räumen.",
                        "Umgebung (Level 2):",
                        () ->
                            OkDialog.showOkDialog(
                                "In diesem Raum müssen alle Monster erledigt werden, um weiterzukommen. - Besiege alle Monster, um in den nächsten Raum zu kommen.",
                                "\"Die Vulkanhöhle\" (Raum 1)",
                                () -> {}))),
            new BuildRoomData(
                DojoStarter::buildRoom_Fragen_Pattern,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum müssen verschiedene Design Patterns anhand eines UML-Klassendiagramms erkannt werden. Die erkannten Design Patterns müssen dann dem Zauberer mitgeteilt werden. - Sprich mit dem Zauberer.",
                        "\"Die Vulkanhöhle\" (Raum 2)",
                        () -> {})),
            new BuildRoomData(
                DojoStarter::buildRoom_Implement_MyImp,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum erhält man eine Beschreibung des erwarteten Verhaltens, die implementiert werden muss. Der Dämon erteilt die Aufgabe, Methoden zu schreiben, die dieses Verhalten implementieren. Danach muss der Dämon angegriffen werden. - Sprich mit dem Imp und danach mit der Truhe!",
                        "\"Die Vulkanhöhle\" (Raum 3)",
                        () -> {})),
          }),
      new LevelRoomLevel(
          graph,
          DesignLabel.TEMPLE,
          new BuildRoomData[] {
            new BuildRoomData(
                DojoStarter::buildRoom_Saphire,
                () ->
                    OkDialog.showOkDialog(
                        "Ein uralter Tempel mit geheimnisvollen Inschriften. - Der Tempel besteht aus drei Räumen.",
                        "Umgebung (Level 3):",
                        () ->
                            OkDialog.showOkDialog(
                                "In diesem Raum muss man den Saphir, der zufällig von einem Monster fallengelassen wird, finden, um in den nächsten Raum zu kommen. - Besiege die Monster und finde den Saphir für den nächsten Raum.",
                                "\"Tempel der verlorenen Geheimnisse\" (Raum 1)",
                                () -> {}))),
            new BuildRoomData(
                DojoStarter::buildRoom_Implement_MyMonster,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum muss ein Monster mit verschiedenen Eigenschaften erstellt und danach besiegt werden, um in den nächsten Raum zu gelangen. - Gehe zum Ritter für die Raumaufgabe.",
                        "\"Tempel der verlorenen Geheimnisse\" (Raum 2)",
                        () -> {})),
            new BuildRoomData(
                DojoStarter::buildRoom_Fehler_Refactoring,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum ist das Ziel, den vorgegebenen Code zu optimieren, Fehler zu beheben und die Lesbarkeit zu verbessern. Nur wenn der Code korrekt ist, kann man in den nächsten Raum weitergehen. - Gehe zum Ritter für die Raumaufgabe.",
                        "\"Tempel der verlorenen Geheimnisse\" (Raum 3)",
                        () -> {})),
          }),
      new LevelRoomLevel(
          graph,
          DesignLabel.DEFAULT,
          new BuildRoomData[] {
            new BuildRoomData(
                DojoStarter::buildRoom_Fragen_Schriftrollen,
                () ->
                    OkDialog.showOkDialog(
                        "Ein düsterer, verfallener Kerker mit steinernen Wänden und alten Türen. - Der Kerker besteht aus drei Räumen.",
                        "Umgebung (Level 4):",
                        () ->
                            OkDialog.showOkDialog(
                                "Der Spieler muss Schriftrollen, die verschiedene Programming Patterns und Software Development Principles repräsentieren, den entsprechenden Truhen zuordnen. Die Truhen sind mit den Kategorien der Programming Patterns und Software Development Principles beschriftet. Erst wenn alle Schriftrollen korrekt zugeordnet sind, kann der Spieler in den nächsten Raum weitergehen. - Ordne die Schriftrollen den entsprechenden Truhen zu.",
                                "\"Kerker des Grauens\" (Raum 1)",
                                () -> {}))),
            new BuildRoomData(
                DojoStarter::buildRoom_Fehler_Quader,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum muss eine Klasse mit mathematischen Funktionen verbessert werden. Der Spieler hat jedoch nur eine begrenzte Anzahl an Versuchen, symbolisiert durch seine Lebenspunkte. Wenn die Klasse richtig verbessert wurde, gilt der Imp als besiegt und der Spieler kann in den nächsten Raum weitergehen. - Sprich mit der Truhe für die Raumaufgabe.",
                        "\"Kerker des Grauens\" (Raum 2)",
                        () -> {})),
            new BuildRoomData(
                DojoStarter::buildRoom_Fragen_RegExes,
                () ->
                    OkDialog.showOkDialog(
                        "In diesem Raum muss ein String eingegeben werden, der zum regulären Ausdruck passt. Wenn der passende String eingegeben wurde, lässt OgreX den Spieler zur nächsten Ebene weitergehen. - Gehe zu OgreX für die Raumaufgabe.",
                        "\"Kerker des Grauens\" (Raum 3)",
                        () -> {})),
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

  private static Room buildRoom_Key(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(designLabel)
        .buildRoom_Key();
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

  private static Room buildRoom_Saphire(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.LARGE)
        .designLabel(designLabel)
        .buildRoom_Saphire();
  }

  private static Room buildRoom_Implement_MyMonster(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Implement_MyMonster();
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

  private static Room buildRoom_Monster_Kill(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Monster_Kill();
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

  private static Room buildRoom_Implement_MyImp(
      LevelRoom levelRoom, RoomGenerator gen, Room nextRoom, DesignLabel designLabel) {
    return new RoomBuilder()
        .levelRoom(levelRoom)
        .roomGenerator(gen)
        .nextRoom(nextRoom)
        .levelSize(LevelSize.MEDIUM)
        .designLabel(designLabel)
        .buildRoom_Implement_MyImp();
  }
}
