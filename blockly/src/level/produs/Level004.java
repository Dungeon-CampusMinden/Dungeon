package level.produs;

import static level.LevelManagementUtils.cameraFocusOn;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.components.MissingComponentException;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, the use, push, and pull blocks are unlocked. The player must use the first lever
 * with the "use" block to open a door, then move a stone onto a pressure plate by combining "pull"
 * and "push" actions to reveal the exit.
 */
public class Level004 extends BlocklyLevel {
  private static boolean showText = true;
  private LeverComponent switch1, switch2;
  private DoorTile door1, door2;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level004(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 4");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        // Schleifen
        "while_loop",
        // Inventar und Charakter
        "fireball",
        "wait",
        "pickup",
        "drop_item",
        "Items",
        // Kategorien
        "Abfragen",
        "Bedingung",
        "Wahrheitsausdruecke",
        "Variablen",
        "Bedingungen",
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup("Versuch mal die Schalter zu benutzen.", "Kapitel 1: Ausbruch");
      showText = false;
    }
    cameraFocusOn(new Coordinate(12, 5));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    door1 = (DoorTile) Game.tileAt(new Coordinate(8, 3)).orElse(null);
    door1.close();
    door2 = (DoorTile) Game.tileAt(new Coordinate(16, 3)).orElse(null);
    door1.close();
    door2.close();
    Entity s1 = LeverFactory.createLever(customPoints().get(0).toPoint());
    Entity s2 = LeverFactory.pressurePlate(customPoints().get(1).toPoint());
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s2, LeverComponent.class));
    Game.add(MiscFactory.stone(customPoints().get(2).toPoint()));
    Game.add(s1);
    Game.add(s2);
  }

  @Override
  protected void onTick() {
    if (switch1.isOn()) door1.open();
    else door1.close();
    if (switch2.isOn()) door2.open();
    else door2.close();
  }
}
