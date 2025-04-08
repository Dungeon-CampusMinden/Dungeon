package level.produs;

import static level.LevelManagementUtils.cameraFocusOn;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;

public class Chapter16Level extends BlocklyLevel {

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
  public Chapter16Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 6");
  }

  @Override
  protected void onFirstTick() {
    cameraFocusOn(new Coordinate(12, 5));
    door1 = (DoorTile) Game.tileAT(new Coordinate(8, 3));
    door1.close();
    door2 = (DoorTile) Game.tileAT(new Coordinate(16, 3));
    door1.close();
    door2.close();
    Entity s1 = LeverFactory.createLever(customPoints().get(0).toCenteredPoint());
    Entity s2 = MiscFactory.pressurePlate(customPoints().get(1).toCenteredPoint());
    switch1 = s1.fetch(LeverComponent.class).get();
    switch2 = s2.fetch(LeverComponent.class).get();
    Game.add(MiscFactory.stone(customPoints().get(2).toCenteredPoint()));
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
