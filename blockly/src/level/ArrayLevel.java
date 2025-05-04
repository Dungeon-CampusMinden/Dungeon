package level;

import contrib.components.InteractionComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import entities.MiscFactory;

import java.util.List;

public class ArrayLevel extends BlocklyLevel {
  private static boolean showText = true;
  private Entity door;
  private final int[] correctArray = {1, 2, 3, 4, 5}; // Das erwartete Array

  public ArrayLevel(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Array-Sortierung");
  }

  @Override
  protected void onFirstTick() {
    if (showText) {
      DialogUtils.showTextPopup(
        "Implementiere die Methode 'createSortedArray' und erstelle ein Array mit den Zahlen von 1 bis 5 in der richtigen Reihenfolge.",
        "Array-Aufgabe"
      );
      showText = false;
    }

    // Erstelle einen Hebel als interaktiven Knopf
    Entity lever = MiscFactory.blocklyLever(new Point(10, 51));
    // Entferne die alte InteractionComponent falls vorhanden
    lever.remove(InteractionComponent.class);
    // Füge neue InteractionComponent mit größerem Radius hinzu
    lever.add(new InteractionComponent(
      5.0f,  // Größerer Interaktionsradius
      true,  // Wiederholbare Interaktion
      (entity, hero) -> checkPlayerSolution()
    ));
    Game.add(lever);

    // Erstelle ein Buch-Pickup als Tür
    door = MiscFactory.bookPickup(
      new Point(11, 51),
      "Tür",
      "Diese Tür öffnet sich nur mit dem richtig sortierten Array."
    );
    Game.add(door);
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
}
