package level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.AdvancedFactory;
import entities.LaserFactory;
import java.util.List;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;

/**
 * This is the start of the game. It is designed to help players get comfortable with the Blockly
 * controls. In this level, the hero can only move and turn; no monsters are present.
 */
public class Level001 extends AdvancedLevel {

  LeverComponent switch1;
  Entity laser;
  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level001(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 1");
  }

  @Override
  protected void onFirstTick() {
    laser = LaserFactory.createLaser(customPoints.getFirst().toPoint(), Direction.UP, true);
    Game.add(AdvancedFactory.laserCube(new Point(2, 6), Direction.RIGHT));
    Game.add(AdvancedFactory.laserReceiver(new Point(6, 6)));
    Entity s1 = LeverFactory.createLever(new Point(4, 1));
    switch1 =
      s1.fetch(LeverComponent.class)
        .orElseThrow(() -> MissingComponentException.build(s1, LeverComponent.class));
    PortalFactory.createPortal(new Point(2, 9), new Point(2, 7), PortalColor.BLUE);
    PortalFactory.createPortal(new Point(10, 4), new Point( 8, 4), PortalColor.GREEN);
    Game.add(s1);

  }

  @Override
  protected void onTick() {
    if (switch1.isOn()) LaserFactory.activate(laser);
    else LaserFactory.deactivate(laser);
  }
}
