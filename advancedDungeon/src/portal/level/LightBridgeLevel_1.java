package portal.level;

import core.Entity;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import java.util.Map;
import portal.lightBridge.LightBridgeFactory;
import portal.util.AdvancedLevel;

/** Level in the portal dungeon. */
public class LightBridgeLevel_1 extends AdvancedLevel {

  private static final String NAME = "Portal Level";

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the player to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param namedPoints The custom points of the level.
   */
  public LightBridgeLevel_1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, NAME);
  }

  @Override
  protected void onFirstTick() {
    Entity emitter = LightBridgeFactory.createEmitter(getPoint("bridge"), Direction.RIGHT, false);
    Game.add(emitter);
    Game.add(LevelCreatorTools.bridgeLever(emitter, getPoint("switch")));
  }

  @Override
  protected void onTick() {}
}
