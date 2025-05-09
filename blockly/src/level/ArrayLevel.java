package level;

import contrib.components.AIComponent;
import contrib.components.InteractionComponent;
import contrib.entities.*;
import contrib.hud.DialogUtils;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import entities.MiscFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayLevel extends BlocklyLevel {
  private static boolean showText = true;
  private Entity door;
  private final int[] correctArray = {1, 2, 3, 4, 5}; // Das erwartete Array

  public ArrayLevel(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Array-Sortierung");
  }

  @Override
  protected void onFirstTick() {

    spawnStaticMonsters();
    spawnSigns();
    spawnLeverWithAction(new Point(30, 12));

    if (showText) {
      DialogUtils.showTextPopup(
        "Implementiere die Methode 'createSortedArray' und erstelle ein Array mit den Zahlen von 1 bis 5 in der richtigen Reihenfolge.",
        "Array-Aufgabe"
      );
      showText = false;
    }

  }
  // Diese Methode soll vom Spieler implementiert werden
  public int[] createSortedArray() {
    // TODO: Implementiere diese Methode
    // Erstelle und gib ein Array mit den Zahlen 1 bis 5 in der richtigen Reihenfolge zurück

    //return null;
    return new int[] {1, 2, 3, 4, 5};

  }

  private void checkPlayerSolution() {
    int[] playerArray = createSortedArray();

    if (playerArray == null) {
      DialogUtils.showTextPopup(
        "Du musst zuerst die Methode 'createSortedArray' implementieren!",
        "Fehler"
      );
      return;
    }

    if (arrayIsCorrect(playerArray)) {
      Game.remove(door);
      DialogUtils.showTextPopup(
        "Sehr gut! Das Array ist korrekt sortiert. Der Weg ist nun frei.",
        "Erfolg"
      );
    } else {
      DialogUtils.showTextPopup(
        "Das Array ist nicht korrekt. Versuche es nochmal!",
        "Fehler"
      );
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

  @Override
  protected void onTick() {}

  private void spawnStaticMonsters() {
    Map<Integer, List<Coordinate>> monsterSpawnPoints = new HashMap<>();

    // Koordinaten für Chorts (Typ 1)
    monsterSpawnPoints.put(1, Arrays.asList(
      new Coordinate(15, 42),
      new Coordinate(18, 40),
      new Coordinate(12, 40),
      new Coordinate(12, 36),
      new Coordinate(17, 35)
    ));

    // Koordinaten für Imps (Typ 2)
    monsterSpawnPoints.put(2, Arrays.asList(
      new Coordinate(58, 50),
      new Coordinate(52, 50),
      new Coordinate(48, 54)
    ));

    // Koordinaten für Docs (Typ 3)
    monsterSpawnPoints.put(3, Arrays.asList(
      new Coordinate(57, 36),
      new Coordinate(54, 26)
    ));

    // Koordinaten für Goblins (Typ 4)
    monsterSpawnPoints.put(4, Arrays.asList(
      new Coordinate(19, 20),
      new Coordinate(17, 23),
      new Coordinate(23, 25),
      new Coordinate(24, 21)
    ));

    // Koordinaten für Monster Elemental Small (Typ 5)
    monsterSpawnPoints.put(5, Arrays.asList(
      new Coordinate(50, 15)
    ));

    monsterSpawnPoints.forEach((monsterType, coordinates) -> {
      coordinates.forEach(pos -> spawnMonsterByType(monsterType, pos));
    });
  }

  private void spawnMonsterByType(int monsterType, Coordinate pos) {
    try {
      Entity monster = switch (monsterType) {
        case 1 -> // Chort
          MonsterFactory.buildMonster(
            "Static Chort",
            new SimpleIPath("character/monster/chort"),
            1,
            0.0f,
            0.0f,
            MonsterDeathSound.LOWER_PITCH.sound(),
            new AIComponent(
              entity -> {
              },
              entity -> {
              },
              entity -> false),
            0,
            0,
            MonsterIdleSound.BURP.path()
          );
        case 2 -> // Imp
          MonsterFactory.buildMonster(
            "Imp",
            new SimpleIPath("character/monster/imp"),
            4,
            5.0f,
            0.1f,
            MonsterDeathSound.HIGH_PITCH.sound(),
            new AIComponent(
              entity -> {
              },  // keine Kampf-KI
              entity -> {
              },  // keine Idle-KI
              entity -> false), // keine Übergänge
            0,                 // kein Kollisionsschaden
            0,                 // keine Kollisions-Abklingzeit
            MonsterIdleSound.BURP.path()
          );
        case 3 -> //Doc
          MonsterFactory.buildMonster(
            "Doc",
            new SimpleIPath("character/monster/doc"),
            4,
            5.0f,
            0.1f,
            MonsterDeathSound.HIGH_PITCH.sound(),
            new AIComponent(
              entity -> {
              },  // keine Kampf-KI
              entity -> {
              },  // keine Idle-KI
              entity -> false), // keine Übergänge
            0,                 // kein Kollisionsschaden
            0,                 // keine Kollisions-Abklingzeit
            MonsterIdleSound.BURP.path()
          );
        case 4 -> //Goblin
          MonsterFactory.buildMonster(
            "Goblin",
            new SimpleIPath("character/monster/goblin"),
            4,
            5.0f,
            0.1f,
            MonsterDeathSound.HIGH_PITCH.sound(),
            new AIComponent(
              entity -> {
              },  // keine Kampf-KI
              entity -> {
              },  // keine Idle-KI
              entity -> false), // keine Übergänge
            0,                 // kein Kollisionsschaden
            0,                 // keine Kollisions-Abklingzeit
            MonsterIdleSound.BURP.path()
          );
        case 5 -> //monster elemental
          MonsterFactory.buildMonster(
            "Monster Elemental",
            new SimpleIPath("character/monster/elemental_goo_small"),
            4,
            5.0f,
            0.1f,
            MonsterDeathSound.HIGH_PITCH.sound(),
            new AIComponent(
              entity -> {
              },  // keine Kampf-KI
              entity -> {
              },  // keine Idle-KI
              entity -> false), // keine Übergänge
            0,                 // kein Kollisionsschaden
            0,                 // keine Kollisions-Abklingzeit
            MonsterIdleSound.BURP.path()
          );
        default -> throw new IllegalArgumentException("Unbekannter Monster-Typ: " + monsterType);
      };

      monster.fetch(PositionComponent.class)
        .ifPresent(pc -> pc.position(pos.toCenteredPoint()));

      Game.add(monster);

    } catch (IOException e) {
      throw new RuntimeException("Failed to create monster entity at " + pos, e);
    }
  }

  private void spawnSigns() {
    Map<Point, String> signMessages = new HashMap<>();

    // Hier definieren wir die Positionen und Nachrichten für die Schilder
    signMessages.put(new Point(12, 54), "Zähle alle Monster, aber merke dir auch die Räume in denen sie waren!");
    signMessages.put(new Point(17, 44), "Erster Raum"); //chorts
    signMessages.put(new Point(25, 23), "Zweiter Raum"); //goblins
    signMessages.put(new Point(47, 52), "Dritter Raum"); //mini
    signMessages.put(new Point(55, 31), "Vierter Raum"); //doc
    signMessages.put(new Point(48, 17), "Ist das überhaupt ein Raum? Merke dir trotzdem die Nummer an Monstern!");

    signMessages.put(new Point(31, 12), "Gehe in den Code an die Stelle ... und implementiere ein Array, dass den Raum und die Anzahl der Monster an der korrekten Stelle hat!");

    // Spawn die Schilder an den definierten Positionen
    signMessages.forEach(this::createSign);
  }

  private void createSign(Point position, String message) {
    Entity sign = SignFactory.createSign(
      message,    // Der Text, der angezeigt werden soll
      "Hinweis", // Titel
      position,   // Position des Schildes
      (entity, hero) -> {} // Optional: zusätzliche Aktion bei Interaktion
    );

    Game.add(sign);
  }

  // Oder mit einer bestimmten Aktion wenn der Hebel betätigt wird
  private void spawnLeverWithAction(Point position) {
    ICommand leverAction = new ICommand() {
      @Override
      public void execute() {
        // Wird ausgeführt, wenn der Hebel eingeschaltet wird
        DialogUtils.showTextPopup("Der Hebel wurde aktiviert!", "Hebel-Status");
      }

      @Override
      public void undo() {
        // Wird ausgeführt, wenn der Hebel ausgeschaltet wird
        DialogUtils.showTextPopup("Der Hebel wurde deaktiviert!", "Hebel-Status");
      }
    };

    Entity lever = LeverFactory.createLever(position, leverAction);
    Game.add(lever);
  }

}
