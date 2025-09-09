package produsAdvanced.level;

import contrib.entities.*;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import level.AdvancedLevel;
import produsAdvanced.abstraction.AdvancedDungeonMonster;
import produsAdvanced.abstraction.ArrayRemover;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Modify an array based on the monster count in each room.
 *
 * @see produsAdvanced.riddles.MyArrayRemover
 */
public class ArrayRemoveLevel extends AdvancedLevel {
  private static boolean showMsg = true;
  private final int[] arrayToPass = {1, 5, 4, 3, 2}; // Summe = 44
  private final int[] correctArray = {2, 5, 3};
  private final Point doorPosition = new Point(28, 11); // Das erwartete Array
  private boolean isLeverActivated = false;

  private static final SimpleIPath ARRAY_REMOVER_PATH =
      new SimpleIPath("advancedDungeon/src/produsAdvanced/riddles/MyArrayRemover.java");
  private static final String ARRAY_REMOVER_CLASSNAME = "produsAdvanced.riddles.MyArrayRemover";
  private static String title = "Array-Aufgabe";
  private static String msg =
      "Schon wieder hier! Du bist wirklich hartnäckig! Finde wieder den Ausgang des Levels, die Lösung ist jetzt aber eine andere!.";
  private static String task =
      "Gehe in die Datei 'MyArrayRemover.java' und implementiere die Methode 'removePosition'.";

  /**
   * Konstruktor für das Array-Entfernungs-Level.
   *
   * @param layout Das Layout des Levels, bestehend aus LevelElementen.
   * @param designLabel Das Design-Label für das Level.
   * @param customPoints Eine Liste von benutzerdefinierten Punkten für das Level.
   */
  public ArrayRemoveLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Array-Entfernen");
  }

  @Override
  protected void onFirstTick() {

    spawnStaticMonsters();
    spawnSigns();
    closeDoor(doorPosition);
    spawnLeverWithAction(new Point(30, 12));

    if (showMsg)
      DialogUtils.showTextPopup(
          msg,
          title,
          () -> {
            showMsg = false;
            DialogUtils.showTextPopup(task, title);
          });
  }

  @Override
  protected void onTick() {}

  private void checkPlayerSolution() {
    int[] playerArray;

    try {
      playerArray =
          ((ArrayRemover)
                  DynamicCompiler.loadUserInstance(
                      ARRAY_REMOVER_PATH,
                      ARRAY_REMOVER_CLASSNAME,
                      new Tuple<>(int[].class, arrayToPass)))
              .removePosition();

    } catch (UnsupportedOperationException e) {
      // Spezifische Behandlung für nicht implementierte Methode
      DialogUtils.showTextPopup(
          "Die Methode 'entfernePositionen' wurde noch nicht implementiert. "
              + "Bitte implementiere sie zuerst!",
          "Nicht implementiert");
      return; // Methode beenden, aber Spiel weiterlaufen lassen
    } catch (Exception e) {
      // Andere Fehler abfangen
      DialogUtils.showTextPopup("Ein Fehler ist aufgetreten: " + e.getMessage(), "Fehler");
      return;
    }

    if (playerArray == null) {
      DialogUtils.showTextPopup("Die Methode 'entfernePositionen' gibt null zurück!", "Fehler");
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
    if (playerArray == null) {
      return false;
    }
    if (playerArray.length != correctArray.length) {
      DialogUtils.showTextPopup("Das Array hat nicht die richtige Länge!", "Fehler");
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

    monsterSpawnPoints.put(0, Arrays.asList(new Coordinate(50, 15), new Coordinate(48, 13)));

    monsterSpawnPoints.put(
        2,
        Arrays.asList(
            new Coordinate(19, 20),
            new Coordinate(17, 23),
            new Coordinate(23, 25),
            new Coordinate(24, 21),
            new Coordinate(22, 24)));

    monsterSpawnPoints.put(
        3, Arrays.asList(new Coordinate(58, 50), new Coordinate(52, 50), new Coordinate(48, 54)));

    monsterSpawnPoints.forEach(
        (monsterType, coordinates) ->
            coordinates.forEach(pos -> spawnMonsterByType(monsterType, pos)));
  }

  private void spawnMonsterByType(int monsterType, Coordinate pos) {
    Entity monster =
        switch (monsterType) {
          case 0 -> AdvancedDungeonMonster.ELEMENTAL.builder().build(pos.toPoint());
          case 1 -> AdvancedDungeonMonster.STATIC_CHORT.builder().build(pos.toPoint());
          case 2 -> AdvancedDungeonMonster.IMP.builder().build(pos.toPoint());
          case 3 -> AdvancedDungeonMonster.DOC.builder().build(pos.toPoint());
          case 4 -> AdvancedDungeonMonster.GOBLIN.builder().build(pos.toPoint());
          default -> throw new IllegalArgumentException("Unbekannter Monster-Typ: " + monsterType);
        };
    Game.add(monster);
  }

  private void spawnSigns() {
    Map<Point, String> signMessages = new HashMap<>();

    // Hier definieren wir die Positionen und Nachrichten für die Schilder
    signMessages.put(
        new Point(12, 54),
        "Es scheint als wären einige Monster umgezogen! Zähle alle Monster, aber merke dir auch die Räume in denen sie waren!");
    signMessages.put(new Point(17, 44), "Erster Raum"); // chorts
    signMessages.put(new Point(25, 23), "Zweiter Raum"); // goblins
    signMessages.put(new Point(47, 52), "Dritter Raum"); // mini
    signMessages.put(new Point(55, 31), "Vierter Raum"); // doc
    signMessages.put(
        new Point(48, 17),
        "Ist das überhaupt ein Raum? Merke dir trotzdem die Nummer an Monstern!");

    signMessages.put(
        new Point(31, 12),
        "Es sieht so aus als hätten sich die Monster verändert! Um durch diese Tür zu kommen musst du die Methode in der Klasse MyArrayRemover implementieren!");

    // Spawn die Schilder an den definierten Positionen
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

  // Oder mit einer bestimmten Aktion wenn der Hebel betätigt wird
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
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof DoorTile) {
      ((DoorTile) tile).close();
    }
  }

  private void openDoor(Point position) {
    Tile tile = Game.tileAt(position).orElse(null);
    if (tile instanceof DoorTile) {
      ((DoorTile) tile).open();
    }
  }
}
