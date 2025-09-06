package produsAdvanced.level;

import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
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
import java.util.List;
import level.AdvancedLevel;
import produsAdvanced.abstraction.ArraySummarizer;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Iterate over an array.
 *
 * @see produsAdvanced.riddles.MyArraySummarizer
 */
public class ArrayIterateLevel extends AdvancedLevel {

  private final Point doorPosition = new Point(18, 3); // Das erwartete Array
  private final Point leverPosition = new Point(17, 2);
  private final Point signPosition = new Point(16, 2);
  private boolean isLeverActivated = false;
  private static boolean showMsg = true;

  private final int[] arrayToPass = {15, 5, 9, 7, 8}; // Summe = 44

  private static final SimpleIPath ARRAY_SUMMARIZER_PATH =
      new SimpleIPath("advancedDungeon/src/produsAdvanced/riddles/MyArraySummarizer.java");
  private static final String ARRAY_SUMMARIZER_CLASSNAME =
      "produsAdvanced.riddles.MyArraySummarizer";

  private static String title = "Array-Aufgabe";
  private static String msg =
      "Der Ausgang ist verschlossen! Übergebe die richtige Lösung, um dieses Level zu Meistern.";
  private static String task =
      "Gehe in die Datei 'MyArraySummarizer.java' und implementiere die Methode 'summarizeArray'. Berechne dort die Summe des Arrays.";

  /**
   * Konstruktor für das Array-Iterations-Level.
   *
   * @param layout Das Layout des Levels, bestehend aus LevelElementen.
   * @param designLabel Das Design-Label für das Level.
   * @param customPoints Eine Liste von benutzerdefinierten Punkten für das Level.
   */
  public ArrayIterateLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Array-Iterieren");
  }

  @Override
  protected void onFirstTick() {

    if (showMsg)
      DialogUtils.showTextPopup(
          msg,
          title,
          () -> {
            showMsg = false;
            DialogUtils.showTextPopup(task, title);
          });

    closeDoor(doorPosition);
    spawnLeverWithAction(leverPosition);

    spawnSign(
        signPosition,
        "Um diese Tür zu öffnen brauche ich die Summe eines Arrays von dir! Gehe in den Code und implementiere die Methode in der Klasse MyArraySummarizer.");
  }

  @Override
  protected void onTick() {}

  private void spawnSign(Point position, String message) {
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

  private void checkPlayerSolution() {
    int playerSolution;

    try {
      playerSolution =
          ((ArraySummarizer)
                  DynamicCompiler.loadUserInstance(
                      ARRAY_SUMMARIZER_PATH,
                      ARRAY_SUMMARIZER_CLASSNAME,
                      new Tuple<>(int[].class, arrayToPass)))
              .summarizeArray();
    } catch (UnsupportedOperationException e) {
      // Spezifische Behandlung für nicht implementierte Methode
      DialogUtils.showTextPopup(
          "Die Methode 'SummarizeArray' wurde noch nicht implementiert. "
              + "Bitte implementiere sie zuerst!",
          "Nicht implementiert");
      return; // Methode beenden, aber Spiel weiterlaufen lassen
    } catch (Exception e) {
      // Andere Fehler abfangen
      DialogUtils.showTextPopup("Ein Fehler ist aufgetreten: " + e.getMessage(), "Fehler");
      return;
    }

    int correctSolution = 44;
    if (playerSolution == correctSolution) {
      openDoor(doorPosition);
      DialogUtils.showTextPopup(
          "Sehr gut! Du hast die korrekte Summe zurückgegeben! Der Weg ist nun frei.", "Erfolg");
    } else {
      DialogUtils.showTextPopup(
          "Deine Summe ist leider nicht korrekt. Versuche es nochmal!", "Fehler");
    }
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
