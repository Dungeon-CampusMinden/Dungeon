package level.produs;

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
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, multiple levers must be operated in the correct order. To solve the puzzle, the
 * player needs to consult the wiring diagram provided in the materials.
 */
public class Level007 extends BlocklyLevel {
  private static boolean showText = true;
  DoorTile door1, door2, door3, door4;
  LeverComponent switch1, switch2, switch3, switch4;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level007(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 7");
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
    if (showText) {
      DialogUtils.showTextPopup(
          "Ganz schön viele Schalter, hätten wir doch nur einen Schaltplan.",
          "Kapitel 1: Ausbruch");
      showText = false;
    }

    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(11, 7));
    LevelManagementUtils.heroViewDirection(Direction.LEFT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    Entity s1 = LeverFactory.createLever(customPoints().get(0).toPoint());
    Entity s2 = LeverFactory.createLever(customPoints().get(1).toPoint());
    Entity s3 = LeverFactory.createLever(customPoints().get(2).toPoint());
    Entity s4 = LeverFactory.createLever(customPoints().get(3).toPoint());
    switch1 =
        s1.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    switch2 =
        s2.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s2, LeverComponent.class));
    switch3 =
        s3.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s3, LeverComponent.class));
    switch4 =
        s4.fetch(LeverComponent.class)
            .orElseThrow(() -> MissingComponentException.build(s4, LeverComponent.class));
    Game.add(s1);
    Game.add(s2);
    Game.add(s3);
    Game.add(s4);

    door1 = (DoorTile) Game.tileAt(new Coordinate(10, 7)).orElse(null);
    door2 = (DoorTile) Game.tileAt(new Coordinate(4, 7)).orElse(null);
    door3 = (DoorTile) Game.tileAt(new Coordinate(7, 4)).orElse(null);
    door4 = (DoorTile) Game.tileAt(new Coordinate(7, 10)).orElse(null);
    door1.close();
    door2.close();
    door3.close();
    door4.close();
  }

  @Override
  protected void onTick() {
    if (switch1.isOn()) door1.open();
    else door1.close();
    if (switch2.isOn()) door2.open();
    else door2.close();
    if (switch3.isOn()) door3.open();
    else door3.close();
    if (switch4.isOn()) door4.open();
    else door4.close();
  }
}
