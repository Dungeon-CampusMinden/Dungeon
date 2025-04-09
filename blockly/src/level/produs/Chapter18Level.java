package level.produs;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import entities.BlocklyMonsterFactory;
import entities.MiscFactory;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter18Level extends BlocklyLevel {

  private LeverComponent switch1, switch2, switch3, switch4, switch5;
  private DoorTile door1, door2, door3, door4, door5;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter18Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 1: Level 8");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.heroViewDiretion(PositionComponent.Direction.RIGHT);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.zoomDefault();
    Game.add(MiscFactory.stone(customPoints().get(0).toCenteredPoint()));
    Game.add(MiscFactory.stone(customPoints().get(1).toCenteredPoint()));
    Game.add(BlocklyMonsterFactory.guard(customPoints().get(7), PositionComponent.Direction.UP, 5));
    Game.add(
        BlocklyMonsterFactory.guard(customPoints().get(8), PositionComponent.Direction.LEFT, 5));

    Entity s1 = MiscFactory.pressurePlate(customPoints().get(2).toCenteredPoint());
    Entity s2 = LeverFactory.createLever(customPoints().get(3).toCenteredPoint());
    Entity s3 = LeverFactory.createLever(customPoints().get(4).toCenteredPoint());
    Entity s4 = LeverFactory.createLever(customPoints().get(5).toCenteredPoint());
    Entity s5 = LeverFactory.createLever(customPoints().get(6).toCenteredPoint());
    switch1 = s1.fetch(LeverComponent.class).get();
    switch2 = s2.fetch(LeverComponent.class).get();
    switch3 = s3.fetch(LeverComponent.class).get();
    switch4 = s4.fetch(LeverComponent.class).get();
    switch5 = s5.fetch(LeverComponent.class).get();
    Game.add(s1);
    Game.add(s2);
    Game.add(s3);
    Game.add(s4);
    Game.add(s5);

    door1 = (DoorTile) Game.tileAT(new Coordinate(7, 15));
    door1.close();

    door2 = (DoorTile) Game.tileAT(new Coordinate(12, 17));
    door2.close();
    door3 = (DoorTile) Game.tileAT(new Coordinate(12, 12));
    door3.close();
    door4 = (DoorTile) Game.tileAT(new Coordinate(10, 9));
    door4.close();
    door5 = (DoorTile) Game.tileAT(new Coordinate(8, 5));
    door5.close();
  }

  @Override
  protected void onTick() {
    if (EntityUtils.getHeroCoordinate().y < 10)
      LevelManagementUtils.cameraFocusOn(new Coordinate(10, 5));
    else LevelManagementUtils.cameraFocusOn(new Coordinate(10, 15));

    if (switch1.isOn()) door1.open();
    else door1.close();

    if (!switch1.isOn()) door2.open();
    else door2.close();
    if (!switch2.isOn()) door3.open();
    else door3.close();
    if (switch2.isOn() && switch1.isOn()) door4.open();
    else door4.close();

    if ((switch3.isOn() ^ switch4.isOn() && switch5.isOn())) door5.open();
    else door5.close();
    ;
  }
}
