package produsAdvanced;

import abstraction.ArrayCreator;
import contrib.components.AIComponent;
import contrib.entities.*;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import level.BlocklyLevel;

public class ArrayCreateLevel extends BlocklyLevel {
  private static boolean showText = true;
  private final int[] correctArray = {1, 5, 4, 2, 3};
  private final Point doorPosition = new Point(28, 11); // Das erwartete Array
  private boolean isLeverActivated = false;

  private static final SimpleIPath ARRAY_CREATOR_PATH =
      new SimpleIPath("blockly/src/riddles/MyArrayCreator.java");
  private static final String ARRAY_CREATOR_CLASSNAME = "riddles.MyArrayCreator";

  public ArrayCreateLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Array-Sortierung");
  }

  @Override
  protected void onFirstTick() {

    spawnStaticMonsters();
    spawnSigns();
    closeDoor(doorPosition);
    spawnLeverWithAction(new Point(30, 12));

    if (showText) {
      DialogUtils.showTextPopup(
          "Implementiere die Methode 'createSortedArray' und erstelle ein Array mit den Zahlen von 1 bis 5 in der richtigen Reihenfolge.",
          "Array-Aufgabe");
      showText = false;
    }
  }

  @Override
  protected void onTick() {}

  private void checkPlayerSolution() {
    int[] playerArray;

    try {
      playerArray =
          ((ArrayCreator)
                  DynamicCompiler.loadUserInstance(ARRAY_CREATOR_PATH, ARRAY_CREATOR_CLASSNAME))
              .countMonstersInRooms();
    } catch (UnsupportedOperationException e) {
      // Spezifische Behandlung für nicht implementierte Methode
      DialogUtils.showTextPopup(
          "Die Methode 'countMonstersInRooms' wurde noch nicht implementiert. "
              + "Bitte implementiere sie zuerst!",
          "Nicht implementiert");
      return; // Methode beenden, aber Spiel weiterlaufen lassen
    } catch (Exception e) {
      // Andere Fehler abfangen
      DialogUtils.showTextPopup("Ein Fehler ist aufgetreten: " + e.getMessage(), "Fehler");
      return;
    }

    if (playerArray == null) {
      DialogUtils.showTextPopup("Die Methode 'countMonstersInRooms' gibt null zurück!", "Fehler");
      return;
    }

    if (arrayIsCorrect(playerArray)) {
      openDoor(doorPosition);
      DialogUtils.showTextPopup("Sehr gut! Das Array ist korrekt. Der Weg ist nun frei.", "Erfolg");
    } else {
      DialogUtils.showTextPopup("Das Array ist nicht korrekt. Versuche es nochmal!", "Fehler");
    }
  }

  private boolean arrayIsCorrect(int[] playerArray) {
    if (playerArray == null || playerArray.length != correctArray.length) {
      return false;
    }

    for (int i = 0; i < correctArray.length; i++) {
      if (playerArray[i] != correctArray[i]) {
        return false;
      }
    }
    return true;
  }

  private void spawnStaticMonsters() {
    Map<Integer, List<Coordinate>> monsterSpawnPoints = new HashMap<>();

    monsterSpawnPoints.put(0, Arrays.asList(new Coordinate(50, 15)));

    monsterSpawnPoints.put(
        1,
        Arrays.asList(
            new Coordinate(15, 42),
            new Coordinate(18, 40),
            new Coordinate(12, 40),
            new Coordinate(12, 36),
            new Coordinate(17, 35)));

    monsterSpawnPoints.put(
        2,
        Arrays.asList(
            new Coordinate(19, 20),
            new Coordinate(17, 23),
            new Coordinate(23, 25),
            new Coordinate(24, 21)));

    monsterSpawnPoints.put(
        3, Arrays.asList(new Coordinate(58, 50), new Coordinate(52, 50), new Coordinate(48, 54)));

    // Koordinaten für Docs (Typ 3)
    monsterSpawnPoints.put(4, Arrays.asList(new Coordinate(57, 36), new Coordinate(54, 26)));

    monsterSpawnPoints.forEach(
        (monsterType, coordinates) ->
            coordinates.forEach(pos -> spawnMonsterByType(monsterType, pos)));
  }

  private void spawnMonsterByType(int monsterType, Coordinate pos) {
    try {
      Entity monster =
          switch (monsterType) {
            case 0 -> // monster elemental
                MonsterFactory.buildMonster(
                    "Monster Elemental",
                    new SimpleIPath("character/monster/elemental_goo_small"),
                    4,
                    5.0f,
                    0.1f,
                    MonsterDeathSound.HIGH_PITCH.sound(),
                    new AIComponent(
                        entity -> {}, // keine Kampf-KI
                        entity -> {}, // keine Idle-KI
                        entity -> false), // keine Übergänge
                    0, // kein Kollisionsschaden
                    0, // keine Kollisions-Abklingzeit
                    MonsterIdleSound.BURP.path());
            case 1 -> // Chort
                MonsterFactory.buildMonster(
                    "Static Chort",
                    new SimpleIPath("character/monster/chort"),
                    1,
                    0.0f,
                    0.0f,
                    MonsterDeathSound.LOWER_PITCH.sound(),
                    new AIComponent(entity -> {}, entity -> {}, entity -> false),
                    0,
                    0,
                    MonsterIdleSound.BURP.path());
            case 2 -> // Imp
                MonsterFactory.buildMonster(
                    "Imp",
                    new SimpleIPath("character/monster/imp"),
                    4,
                    5.0f,
                    0.1f,
                    MonsterDeathSound.HIGH_PITCH.sound(),
                    new AIComponent(
                        entity -> {}, // keine Kampf-KI
                        entity -> {}, // keine Idle-KI
                        entity -> false), // keine Übergänge
                    0, // kein Kollisionsschaden
                    0, // keine Kollisions-Abklingzeit
                    MonsterIdleSound.BURP.path());
            case 3 -> // Doc
                MonsterFactory.buildMonster(
                    "Doc",
                    new SimpleIPath("character/monster/doc"),
                    4,
                    5.0f,
                    0.1f,
                    MonsterDeathSound.HIGH_PITCH.sound(),
                    new AIComponent(
                        entity -> {}, // keine Kampf-KI
                        entity -> {}, // keine Idle-KI
                        entity -> false), // keine Übergänge
                    0, // kein Kollisionsschaden
                    0, // keine Kollisions-Abklingzeit
                    MonsterIdleSound.BURP.path());
            case 4 -> // Goblin
                MonsterFactory.buildMonster(
                    "Goblin",
                    new SimpleIPath("character/monster/goblin"),
                    4,
                    5.0f,
                    0.1f,
                    MonsterDeathSound.HIGH_PITCH.sound(),
                    new AIComponent(
                        entity -> {}, // keine Kampf-KI
                        entity -> {}, // keine Idle-KI
                        entity -> false), // keine Übergänge
                    0, // kein Kollisionsschaden
                    0, // keine Kollisions-Abklingzeit
                    MonsterIdleSound.BURP.path());
            default ->
                throw new IllegalArgumentException("Unbekannter Monster-Typ: " + monsterType);
          };

      monster.fetch(PositionComponent.class).ifPresent(pc -> pc.position(pos.toCenteredPoint()));

      Game.add(monster);

    } catch (IOException e) {
      throw new RuntimeException("Failed to create monster entity at " + pos, e);
    }
  }

  private void spawnSigns() {
    Map<Point, String> signMessages = new HashMap<>();

    signMessages.put(
        new Point(12, 54), "Zähle alle Monster, aber merke dir auch die Räume in denen sie waren!");
    signMessages.put(
        new Point(48, 17),
        "Ist das überhaupt ein Raum? Man könnte fast schon 0er Raum sagen! Merke dir trotzdem die Nummer an Monstern!");
    signMessages.put(new Point(17, 44), "Erster Raum");
    signMessages.put(new Point(25, 23), "Zweiter Raum");
    signMessages.put(new Point(47, 52), "Dritter Raum");
    signMessages.put(new Point(55, 31), "Vierter Raum");

    signMessages.put(
        new Point(31, 12),
        "Ich hoffe du hast dir die Anzahl und Räume der Monster gemerkt! Um durch diese Tür zu kommen brauche ich das richtige Array von dir! Gehe in den Code und implementiere die Methoden der Klasse MyArrayCreator!");

    signMessages.forEach(this::createSign);
  }

  private void createSign(Point position, String message) {
    Entity sign =
        SignFactory.createSign(
            message, // Der Text, der angezeigt werden soll
            "Hinweis", // Titel
            position, // Position des Schildes
            (entity, hero) -> {} // Optional: zusätzliche Aktion bei Interaktion
            );

    Game.add(sign);
  }

  private void spawnLeverWithAction(Point position) {
    ICommand leverAction =
        new ICommand() {
          @Override
          public void execute() {
            if (!isLeverActivated) {
              isLeverActivated = true;
              System.out.println("Lever executed at: " + System.currentTimeMillis());
              checkPlayerSolution();
            }
          }

          @Override
          public void undo() {
            isLeverActivated = false;
          }
        };

    Entity lever = LeverFactory.createLever(position, leverAction);
    Game.add(lever);
  }

  private void closeDoor(Point position) {
    Tile tile = Game.tileAT(position);
    if (tile instanceof DoorTile) {
      ((DoorTile) tile).close();
    }
  }

  private void openDoor(Point position) {
    Tile tile = Game.tileAT(position);
    if (tile instanceof DoorTile) {
      ((DoorTile) tile).open();
    }
  }
}
