package level.produs;

import contrib.components.LeverComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter16Level extends BlocklyLevel {
  private static boolean showText = true;
  private DoorTile door;
  private LeverComponent switch1, switch2;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter16Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 6");
  }

  @Override
  protected void onFirstTick() {
    if (showText) {
      DialogUtils.showTextPopup("Versuch mal die Schalter zu benutzen.", "Kapitel 1: Ausbruch");
      showText = false;
    }
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.centerHero();
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    Coordinate stone1C = customPoints().get(0);
    Coordinate stone2C = customPoints().get(1);
    Coordinate switch1C = customPoints().get(2);
    Coordinate switch2C = customPoints().get(3);
    Game.add(MiscFactory.stone(stone1C.toCenteredPoint()));
    Game.add(MiscFactory.stone(stone2C.toCenteredPoint()));
    Entity s1 = MiscFactory.pressurePlate(switch1C.toCenteredPoint());
    Entity s2 = MiscFactory.pressurePlate(switch2C.toCenteredPoint());
    Game.add(s1);
    Game.add(s2);
    switch1 = s1.fetch(LeverComponent.class).get();
    switch2 = s2.fetch(LeverComponent.class).get();
    door = (DoorTile) Game.tileAT(new Coordinate(5, 12));
    door.close();
  }

  @Override
  protected void onTick() {
    if (switch1.isOn() && switch2.isOn()) door.open();
    else door.close();
  }
}
