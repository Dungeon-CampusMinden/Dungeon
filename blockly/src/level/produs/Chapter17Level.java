package level.produs;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter17Level extends BlocklyLevel {

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
  public Chapter17Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 7");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.cameraFocusOn(new Coordinate(11, 7));
    Entity s1 = LeverFactory.createLever(customPoints().get(0).toCenteredPoint());
    Entity s2 = LeverFactory.createLever(customPoints().get(1).toCenteredPoint());
    Entity s3 = LeverFactory.createLever(customPoints().get(2).toCenteredPoint());
    Entity s4 = LeverFactory.createLever(customPoints().get(3).toCenteredPoint());
    switch1 = s1.fetch(LeverComponent.class).get();
    switch2 = s2.fetch(LeverComponent.class).get();
    switch3 = s3.fetch(LeverComponent.class).get();
    switch4 = s4.fetch(LeverComponent.class).get();
    Game.add(s1);
    Game.add(s2);
    Game.add(s3);
    Game.add(s4);

    door1 = (DoorTile) Game.tileAT(new Coordinate(10, 7));
    door2 = (DoorTile) Game.tileAT(new Coordinate(4, 7));
    door3 = (DoorTile) Game.tileAT(new Coordinate(7, 4));
    door4 = (DoorTile) Game.tileAT(new Coordinate(7, 10));
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
