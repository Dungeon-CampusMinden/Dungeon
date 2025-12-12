package level.portal;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import level.AdvancedLevel;
import produsAdvanced.abstraction.portals.PortalColor;
import produsAdvanced.abstraction.portals.PortalFactory;

/**
 * Portal level three. In this level there are three platforms. The player has to reach platform 3
 * over platform 2 to toggle a lever which unlocks the exit on the first platform.
 */
public class PortalLevel_3 extends AdvancedLevel {

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public PortalLevel_3(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Portal Level 3");
  }

  private LeverComponent lever;

  private ExitTile door;

  @Override
  protected void onFirstTick() {
    Entity leverEntity = LeverFactory.createLever(namedPoints.get("lever"));
    lever = leverEntity.fetch(LeverComponent.class).orElseThrow();

    door = (ExitTile) Game.randomTile(LevelElement.EXIT).orElseThrow();
    door.close();

    PortalFactory.createPortal(namedPoints.get("portal"), Direction.LEFT, PortalColor.BLUE);

    Game.add(leverEntity);
  }

  @Override
  protected void onTick() {
    if (lever.isOn()) door.open();
    else door.close();
  }
}
